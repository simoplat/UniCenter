package it.project.view.server;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import it.project.Unicenter;

/**
 * Server HTTP integrato (JDK standard) per la gestione dell'interfaccia grafica Web.
 * Serve file statici da src/web e instrada le chiamate REST verso UniCenterApiController.
 */
public class UniCenterWebServer {

    private final Unicenter unicenter;
    private final UniCenterApiController apiController;
    private HttpServer server;
    private int port = 8080;
    private Path webRoot;

    /**
     * Costruttore del web server integrato.
     *
     * @param unicenter istanza centrale del sistema UniCenter
     */
    public UniCenterWebServer(Unicenter unicenter) {
        this.unicenter = unicenter;
        this.apiController = new UniCenterApiController(unicenter);
        resolveWebRoot();
    }

    private void resolveWebRoot() {
        // Cerca la cartella src/web a partire dalla directory corrente o genitore
        Path[] candidatePaths = new Path[]{
                Paths.get("src/web"),
                Paths.get("unicenter/src/web"),
                Paths.get("../src/web"),
                Paths.get("web")
        };

        for (Path p : candidatePaths) {
            if (Files.exists(p) && Files.isDirectory(p)) {
                this.webRoot = p.toAbsolutePath().normalize();
                return;
            }
        }
        // Fallback default
        this.webRoot = Paths.get("src/web").toAbsolutePath().normalize();
    }

    /**
     * Avvia il server HTTP sulla porta preferita o sulla prima successiva disponibile.
     *
     * @param preferredPort porta desiderata (es. 8080)
     * @return true se avviato con successo
     */
    public synchronized boolean start(int preferredPort) {
        if (server != null) {
            return true; // Già avviato
        }

        int currentPort = preferredPort;
        while (currentPort < preferredPort + 20) {
            try {
                server = HttpServer.create(new InetSocketAddress(currentPort), 0);
                this.port = currentPort;
                break;
            } catch (IOException e) {
                currentPort++;
            }
        }

        if (server == null) {
            System.err.println("[SERVER ERROR] Impossibile avviare il server HTTP su nessuna porta disponibile.");
            return false;
        }

        server.setExecutor(Executors.newCachedThreadPool());

        // Handler globale per API e file statici
        server.createContext("/", new UnifiedHttpHandler());

        server.start();
        return true;
    }

    /**
     * Arresta il server HTTP.
     */
    public synchronized void stop() {
        if (server != null) {
            server.stop(1);
            server = null;
        }
    }

    /**
     * Restituisce la porta effettiva su cui il server è in ascolto.
     *
     * @return porta TCP
     */
    public int getPort() {
        return port;
    }

    /**
     * Restituisce l'URL di base dell'applicazione.
     *
     * @return URL base (es. http://localhost:8080)
     */
    public String getBaseUrl() {
        return "http://localhost:" + port;
    }

    /**
     * Tenta di aprire automaticamente l'applicazione web nel browser predefinito del sistema operativo.
     */
    public void openBrowser() {
        String url = getBaseUrl();
        try {
            if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                java.awt.Desktop.getDesktop().browse(new URI(url));
                return;
            }
        } catch (Exception ignored) {
        }

        // Fallback tramite comandi di sistema operativi
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{"open", url});
            } else if (os.contains("win")) {
                Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
            } else if (os.contains("nix") || os.contains("nux")) {
                Runtime.getRuntime().exec(new String[]{"xdg-open", url});
            }
        } catch (Exception ignored) {
        }
    }

    private class UnifiedHttpHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod().toUpperCase();
            String path = exchange.getRequestURI().getPath();

            // CORS headers pre-flight
            Headers responseHeaders = exchange.getResponseHeaders();
            responseHeaders.set("Access-Control-Allow-Origin", "*");
            responseHeaders.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            responseHeaders.set("Access-Control-Allow-Headers", "Content-Type, Authorization");

            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            // 1. API Endpoints
            if (path.startsWith("/api/")) {
                handleApi(exchange, path, method);
                return;
            }

            // 2. Static Web Assets
            handleStatic(exchange, path);
        }

        private void handleApi(HttpExchange exchange, String path, String method) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

            // Leggi Body se presente
            Map<String, Object> body = new HashMap<>();
            if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
                InputStream is = exchange.getRequestBody();
                String bodyStr = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                if (!bodyStr.trim().isEmpty()) {
                    body = JsonHelper.parseJsonObject(bodyStr);
                }
            }

            // Parsing Query Params
            Map<String, String> queryParams = new HashMap<>();
            String rawQuery = exchange.getRequestURI().getRawQuery();
            if (rawQuery != null && !rawQuery.trim().isEmpty()) {
                String[] pairs = rawQuery.split("&");
                for (String pair : pairs) {
                    int idx = pair.indexOf("=");
                    if (idx > 0) {
                        String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                        String val = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                        queryParams.put(key, val);
                    }
                }
            }

            // GESTIONE DOWNLOAD BINARIO DIRETTO (PDF, TXT, SLIDE, ECC.)
            if ("/api/materiale/download".equals(path) && "GET".equalsIgnoreCase(method)) {
                String id = queryParams.get("id");
                if (id == null) id = queryParams.get("idElemento");
                try {
                    var downloadResp = unicenter.scaricaMaterialeDidattico(id);
                    byte[] fileBytes = downloadResp.getBytes();
                    String mime = downloadResp.getMimeType() != null ? downloadResp.getMimeType() : "application/octet-stream";
                    String filename = downloadResp.getNomeFile();

                    Headers respHeaders = exchange.getResponseHeaders();
                    respHeaders.set("Content-Type", mime);
                    respHeaders.set("Content-Disposition", "inline; filename=\"" + filename + "\"");
                    exchange.sendResponseHeaders(200, fileBytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(fileBytes);
                    }
                    return;
                } catch (Exception e) {
                    exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                    Map<String, Object> errResp = Map.of("success", false, "error", "Download fallito: " + e.getMessage());
                    byte[] errBytes = JsonHelper.toJson(errResp).getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(404, errBytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(errBytes);
                    }
                    return;
                }
            }

            Map<String, Object> response = apiController.handleRequest(path, method, body, queryParams);
            String jsonResp = JsonHelper.toJson(response);
            byte[] bytes = jsonResp.getBytes(StandardCharsets.UTF_8);

            boolean isSuccess = Boolean.TRUE.equals(response.get("success"));
            int statusCode = isSuccess ? 200 : 400;
            if (path.equals("/api/auth/current") || path.equals("/api/immatricolazione/status")) {
                statusCode = 200;
            }

            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private void handleStatic(HttpExchange exchange, String rawPath) throws IOException {
            String sanitized = rawPath.equals("/") ? "/index.html" : rawPath;
            if (sanitized.startsWith("/")) sanitized = sanitized.substring(1);

            Path target = webRoot.resolve(sanitized).normalize();

            // Protezione path traversal
            if (!target.startsWith(webRoot) || !Files.exists(target) || Files.isDirectory(target)) {
                // Fallback SPA verso index.html se non è un file di asset
                if (!sanitized.contains(".")) {
                    target = webRoot.resolve("index.html").normalize();
                }
            }

            if (!Files.exists(target) || Files.isDirectory(target)) {
                String notFound = "404 Not Found";
                exchange.sendResponseHeaders(404, notFound.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFound.getBytes(StandardCharsets.UTF_8));
                }
                return;
            }

            String mime = getMimeType(target.toString());
            exchange.getResponseHeaders().set("Content-Type", mime);

            byte[] content = Files.readAllBytes(target);
            exchange.sendResponseHeaders(200, content.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(content);
            }
        }

        private String getMimeType(String path) {
            String lower = path.toLowerCase();
            if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html; charset=UTF-8";
            if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
            if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
            if (lower.endsWith(".svg")) return "image/svg+xml";
            if (lower.endsWith(".png")) return "image/png";
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
            if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
            if (lower.endsWith(".ico")) return "image/x-icon";
            return "application/octet-stream";
        }
    }
}
