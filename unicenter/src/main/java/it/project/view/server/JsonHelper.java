package it.project.view.server;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Helper leggero e robusto per serializzazione e parsing JSON senza dipendenze esterne.
 */
public class JsonHelper {

    private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    public static String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String) {
            return "\"" + escape((String) obj) + "\"";
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        if (obj instanceof Character) {
            return "\"" + escape(obj.toString()) + "\"";
        }
        if (obj instanceof LocalDate) {
            return "\"" + ((LocalDate) obj).format(ISO_DATE) + "\"";
        }
        if (obj instanceof LocalDateTime) {
            return "\"" + ((LocalDateTime) obj).format(ISO_DATE_TIME) + "\"";
        }
        if (obj instanceof Collection<?>) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : (Collection<?>) obj) {
                if (!first) sb.append(",");
                sb.append(toJson(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }
        if (obj instanceof Map<?, ?>) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) obj).entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(escape(String.valueOf(entry.getKey()))).append("\":");
                sb.append(toJson(entry.getValue()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }

        // Serializzazione riflessiva per getter
        Map<String, Object> map = new LinkedHashMap<>();
        for (Method m : obj.getClass().getMethods()) {
            if (m.getParameterCount() == 0 && !m.getName().equals("getClass")) {
                String name = m.getName();
                String propName = null;
                if (name.startsWith("get") && name.length() > 3) {
                    propName = Character.toLowerCase(name.charAt(3)) + name.substring(4);
                } else if (name.startsWith("is") && name.length() > 2) {
                    propName = Character.toLowerCase(name.charAt(2)) + name.substring(3);
                }
                if (propName != null) {
                    try {
                        Object val = m.invoke(obj);
                        map.put(propName, val);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return toJson(map);
    }

    private static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * Parser JSON semplice per oggetti JSON di primo livello (Mappa chiave-valore).
     */
    public static Map<String, Object> parseJsonObject(String json) {
        Map<String, Object> result = new HashMap<>();
        if (json == null || json.trim().isEmpty()) return result;
        String trimmed = json.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) return result;

        int i = 1;
        int len = trimmed.length() - 1;
        while (i < len) {
            // Salta spazi
            while (i < len && Character.isWhitespace(trimmed.charAt(i))) i++;
            if (i >= len) break;

            if (trimmed.charAt(i) == ',') {
                i++;
                continue;
            }

            // Parsing chiave
            if (trimmed.charAt(i) != '"') break;
            i++;
            int keyStart = i;
            while (i < len && trimmed.charAt(i) != '"') {
                if (trimmed.charAt(i) == '\\') i++;
                i++;
            }
            String key = trimmed.substring(keyStart, i);
            i++; // Salta chiusura quote

            // Salta spazi fino a ':'
            while (i < len && Character.isWhitespace(trimmed.charAt(i))) i++;
            if (i < len && trimmed.charAt(i) == ':') i++;
            while (i < len && Character.isWhitespace(trimmed.charAt(i))) i++;

            // Parsing valore
            if (i >= len) break;
            char firstValChar = trimmed.charAt(i);

            if (firstValChar == '"') {
                // Stringa
                i++;
                StringBuilder sb = new StringBuilder();
                while (i < len) {
                    char c = trimmed.charAt(i);
                    if (c == '\\' && i + 1 < len) {
                        i++;
                        char next = trimmed.charAt(i);
                        if (next == 'n') sb.append('\n');
                        else if (next == 't') sb.append('\t');
                        else if (next == 'r') sb.append('\r');
                        else sb.append(next);
                    } else if (c == '"') {
                        break;
                    } else {
                        sb.append(c);
                    }
                    i++;
                }
                result.put(key, sb.toString());
                i++; // Salta chiusura quote
            } else if (firstValChar == '[') {
                // Array di stringhe/numeri
                int bracketDepth = 1;
                int arrStart = i;
                i++;
                while (i < len && bracketDepth > 0) {
                    if (trimmed.charAt(i) == '[') bracketDepth++;
                    else if (trimmed.charAt(i) == ']') bracketDepth--;
                    i++;
                }
                String arrStr = trimmed.substring(arrStart, i);
                result.put(key, parseJsonArray(arrStr));
            } else if (firstValChar == '{') {
                // Oggetto annidato
                int braceDepth = 1;
                int objStart = i;
                i++;
                while (i < len && braceDepth > 0) {
                    if (trimmed.charAt(i) == '{') braceDepth++;
                    else if (trimmed.charAt(i) == '}') braceDepth--;
                    i++;
                }
                String nestedStr = trimmed.substring(objStart, i);
                result.put(key, parseJsonObject(nestedStr));
            } else {
                // Booleano o Numero o null
                int valStart = i;
                while (i < len && trimmed.charAt(i) != ',' && trimmed.charAt(i) != '}') {
                    i++;
                }
                String raw = trimmed.substring(valStart, i).trim();
                if ("true".equalsIgnoreCase(raw)) {
                    result.put(key, Boolean.TRUE);
                } else if ("false".equalsIgnoreCase(raw)) {
                    result.put(key, Boolean.FALSE);
                } else if ("null".equalsIgnoreCase(raw)) {
                    result.put(key, null);
                } else {
                    try {
                        if (raw.contains(".")) {
                            result.put(key, Double.parseDouble(raw));
                        } else {
                            result.put(key, Long.parseLong(raw));
                        }
                    } catch (NumberFormatException e) {
                        result.put(key, raw);
                    }
                }
            }
        }
        return result;
    }

    public static List<Object> parseJsonArray(String json) {
        List<Object> list = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) return list;
        String trimmed = json.trim();
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) return list;

        int i = 1;
        int len = trimmed.length() - 1;
        while (i < len) {
            while (i < len && (Character.isWhitespace(trimmed.charAt(i)) || trimmed.charAt(i) == ',')) i++;
            if (i >= len) break;

            char c = trimmed.charAt(i);
            if (c == '"') {
                i++;
                StringBuilder sb = new StringBuilder();
                while (i < len) {
                    char ch = trimmed.charAt(i);
                    if (ch == '\\' && i + 1 < len) {
                        i++;
                        sb.append(trimmed.charAt(i));
                    } else if (ch == '"') {
                        break;
                    } else {
                        sb.append(ch);
                    }
                    i++;
                }
                list.add(sb.toString());
                i++;
            } else if (c == '{') {
                int braceDepth = 1;
                int start = i;
                i++;
                while (i < len && braceDepth > 0) {
                    if (trimmed.charAt(i) == '{') braceDepth++;
                    else if (trimmed.charAt(i) == '}') braceDepth--;
                    i++;
                }
                list.add(parseJsonObject(trimmed.substring(start, i)));
            } else if (c == '[') {
                int bracketDepth = 1;
                int start = i;
                i++;
                while (i < len && bracketDepth > 0) {
                    if (trimmed.charAt(i) == '[') bracketDepth++;
                    else if (trimmed.charAt(i) == ']') bracketDepth--;
                    i++;
                }
                list.add(parseJsonArray(trimmed.substring(start, i)));
            } else {
                int start = i;
                while (i < len && trimmed.charAt(i) != ',' && trimmed.charAt(i) != ']') {
                    i++;
                }
                String raw = trimmed.substring(start, i).trim();
                if ("true".equalsIgnoreCase(raw)) list.add(Boolean.TRUE);
                else if ("false".equalsIgnoreCase(raw)) list.add(Boolean.FALSE);
                else if ("null".equalsIgnoreCase(raw)) list.add(null);
                else {
                    try {
                        if (raw.contains(".")) list.add(Double.parseDouble(raw));
                        else list.add(Long.parseLong(raw));
                    } catch (NumberFormatException e) {
                        list.add(raw);
                    }
                }
            }
        }
        return list;
    }
}
