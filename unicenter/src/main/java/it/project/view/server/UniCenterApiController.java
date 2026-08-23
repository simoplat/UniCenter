package it.project.view.server;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import it.project.Amministratore;
import it.project.Appello;
import it.project.CorsoDiLaurea;
import it.project.EsameSostenuto;
import it.project.Libretto;
import it.project.Materia;
import it.project.Notifica;
import it.project.PianoDiStudi;
import it.project.Professore;
import it.project.Studente;
import it.project.Unicenter;
import it.project.Utente;
import it.project.database.ClockProvider;
import it.project.exceptions.CorsoDiLaureaNonTrovatoException;
import it.project.exceptions.DataNonValidaException;

/**
 * Controller per la gestione di tutte le richieste REST API inviate dalla Web UI.
 * Interagisce direttamente con la facade Unicenter senza alterare logica di business o controlli.
 */
public class UniCenterApiController {

    private final Unicenter unicenter;
    private final DateTimeFormatter formatterInput = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DateTimeFormatter formatterInputData = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public UniCenterApiController(Unicenter unicenter) {
        this.unicenter = unicenter;
    }

    public Map<String, Object> handleRequest(String path, String method, Map<String, Object> body, Map<String, String> queryParams) {
        try {
            // ==========================================
            // AUTH ENDPOINTS
            // ==========================================
            if (path.equals("/api/auth/login") && "POST".equalsIgnoreCase(method)) {
                String email = (String) body.get("email");
                String password = (String) body.get("password");
                if (email == null || password == null) {
                    return error("Email e password sono obbligatorie.");
                }
                if (!unicenter.esisteUtente(email)) {
                    return error("Email non registrata.");
                }
                if (!unicenter.passwordCorretta(email, password)) {
                    return error("Password errata. Riprova.");
                }
                return ok(buildUserData(unicenter.getCurrentUser()));
            }

            if (path.equals("/api/auth/logout") && "POST".equalsIgnoreCase(method)) {
                unicenter.setCurrentUser(null);
                return ok(Map.of("message", "Logout effettuato"));
            }

            if (path.equals("/api/auth/current") && "GET".equalsIgnoreCase(method)) {
                Utente user = unicenter.getCurrentUser();
                if (user == null) {
                    return ok(Map.of("authenticated", false));
                }
                Map<String, Object> data = new HashMap<>(buildUserData(user));
                data.put("authenticated", true);
                return ok(data);
            }

            if (path.equals("/api/auth/demo-users") && "GET".equalsIgnoreCase(method)) {
                List<Map<String, Object>> list = new ArrayList<>();
                list.add(Map.of("role", "studente", "email", "mario.rossi@studenti.it", "nome", "Mario Rossi (Studente)"));
                list.add(Map.of("role", "studente", "email", "simo.plata@studenti.it", "nome", "Simo Plata (Studente)"));
                list.add(Map.of("role", "professore", "email", "mario.rossi@unicenter.it", "nome", "Prof. Mario Rossi (Docente IS01, BD01, PRG01)"));
                list.add(Map.of("role", "professore", "email", "giuseppe.verdi@unicenter.it", "nome", "Prof. Giuseppe Verdi (Docente SO01, RET01, SIC01)"));
                list.add(Map.of("role", "amministratore", "email", "admin@unicenter.it", "nome", "Amministratore di Sistema"));
                return ok(list);
            }

            // ==========================================
            // SYSTEM CLOCK ENDPOINTS
            // ==========================================
            if (path.equals("/api/system/clock") && "GET".equalsIgnoreCase(method)) {
                LocalDateTime now = ClockProvider.nowLocalDateTime();
                boolean isSimulated = (ClockProvider.getClock() != null);
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                DateTimeFormatter dtfDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                DateTimeFormatter dtfTime = DateTimeFormatter.ofPattern("HH:mm");
                DateTimeFormatter dtfIso = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

                Map<String, Object> clockData = new HashMap<>();
                clockData.put("isoDateTime", now.format(dtfIso));
                clockData.put("formattedDateTime", now.format(dtf));
                clockData.put("date", now.format(dtfDate));
                clockData.put("time", now.format(dtfTime));
                clockData.put("isSimulated", isSimulated);
                clockData.put("realIsoDateTime", LocalDateTime.now().format(dtfIso));
                return ok(clockData);
            }

            if (path.equals("/api/system/clock/set") && "POST".equalsIgnoreCase(method)) {
                String dateTimeStr = (String) body.get("dateTime");
                if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
                    return error("Specificare una data e ora valida (formato ISO: YYYY-MM-DDTHH:mm).");
                }

                LocalDateTime targetDateTime;
                try {
                    if (dateTimeStr.length() == 10) {
                        targetDateTime = LocalDate.parse(dateTimeStr).atTime(12, 0);
                    } else {
                        targetDateTime = LocalDateTime.parse(dateTimeStr);
                    }
                } catch (Exception e) {
                    return error("Formato data/ora non valido: " + e.getMessage());
                }

                LocalDateTime realNow = LocalDateTime.now();
                if (targetDateTime.isBefore(realNow)) {
                    return error("È possibile impostare solo date nel futuro rispetto all'orario reale corrente ("
                            + realNow.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + ").");
                }

                ClockProvider.setFixedDateTime(targetDateTime);
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                DateTimeFormatter dtfIso = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                Map<String, Object> clockData = new HashMap<>();
                clockData.put("isoDateTime", targetDateTime.format(dtfIso));
                clockData.put("formattedDateTime", targetDateTime.format(dtf));
                clockData.put("isSimulated", true);
                clockData.put("message", "Data di sistema impostata a: " + targetDateTime.format(dtf));
                return ok(clockData);
            }

            if (path.equals("/api/system/clock/reset") && "POST".equalsIgnoreCase(method)) {
                ClockProvider.resetClock();
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                DateTimeFormatter dtfIso = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                Map<String, Object> clockData = new HashMap<>();
                clockData.put("isoDateTime", now.format(dtfIso));
                clockData.put("formattedDateTime", now.format(dtf));
                clockData.put("isSimulated", false);
                clockData.put("message", "Orologio di sistema ripristinato al tempo reale.");
                return ok(clockData);
            }

            // ==========================================
            // IMMATRICOLAZIONE
            // ==========================================
            if (path.equals("/api/immatricolazione/status") && "GET".equalsIgnoreCase(method)) {
                boolean aperta = false;
                String msg = "";
                try {
                    aperta = unicenter.validaDataImmatricolazione();
                    msg = "Finestra temporale per l'immatricolazione aperta.";
                } catch (DataNonValidaException e) {
                    aperta = false;
                    msg = e.getMessage();
                }
                return ok(Map.of("aperta", aperta, "messaggio", msg));
            }

            if (path.equals("/api/immatricolazione/corsi") && "GET".equalsIgnoreCase(method)) {
                List<CorsoDiLaurea> corsi = unicenter.getCorsiDiLaureaAttivi();
                List<Map<String, Object>> result = new ArrayList<>();
                if (corsi != null) {
                    for (CorsoDiLaurea c : corsi) {
                        result.add(Map.of(
                                "id", c.getId(),
                                "nome", c.getNome(),
                                "tipologia", c.getTipologia(),
                                "anni", c.getAnniAccademici(),
                                "finalizzato", c.isFinalizzato()
                        ));
                    }
                }
                return ok(result);
            }

            if (path.equals("/api/immatricolazione") && "POST".equalsIgnoreCase(method)) {
                String nome = (String) body.get("nome");
                String cognome = (String) body.get("cognome");
                String email = (String) body.get("email");
                String password = (String) body.get("password");
                String corso = (String) body.get("corso");
                String codiceFiscale = (String) body.get("codiceFiscale");

                if (nome == null || cognome == null || email == null || password == null || corso == null || codiceFiscale == null) {
                    return error("Tutti i campi sono obbligatori.");
                }

                try {
                    unicenter.validaDataImmatricolazione();
                } catch (DataNonValidaException e) {
                    return error(e.getMessage());
                }

                Studente nuovo = unicenter.immatricolaStudente(nome, cognome, email, password, corso, codiceFiscale);
                return ok(Map.of(
                        "success", true,
                        "matricola", nuovo.getMatricola(),
                        "nome", nuovo.getNome() + " " + nuovo.getCognome(),
                        "email", nuovo.getEmail(),
                        "corso", nuovo.getIdCorsoDiLaurea(),
                        "tasse", nuovo.getTasse(),
                        "codiceFiscale", nuovo.getCodiceFiscale()
                ));
            }

            // ==========================================
            // STUDENTE ENDPOINTS
            // ==========================================
            if (path.startsWith("/api/student/")) {
                if (!(unicenter.getCurrentUser() instanceof Studente)) {
                    return error("Accesso non autorizzato. È richiesto il login come Studente.");
                }
                Studente studente = (Studente) unicenter.getCurrentUser();

                if (path.equals("/api/student/dashboard")) {
                    Libretto libretto = studente.getLibretto();
                    int cfu = (libretto != null) ? libretto.getTotaleCfu() : 0;
                    int superati = (libretto != null) ? libretto.getNumeroEsamiSuperati() : 0;
                    double media = (libretto != null && superati > 0) ? libretto.getMediaPonderata() : 0.0;
                    List<Notifica> notif = unicenter.getNotifichePerStudente();
                    List<EsameSostenuto> pendenti = unicenter.getEsitiPendentiStudente();
                    List<Appello> prenotati = unicenter.trovaAppelliPrenotatiDalloStudente();

                    Map<String, Object> dash = new HashMap<>();
                    dash.put("matricola", studente.getMatricola());
                    dash.put("corso", studente.getIdCorsoDiLaurea());
                    dash.put("cfu", cfu);
                    dash.put("media", media);
                    dash.put("esamiSuperati", superati);
                    dash.put("tasseImporto", studente.getTasse());
                    dash.put("tassePagate", studente.isTassePagate());
                    dash.put("annoCorrente", studente.getAnnoCorrente());
                    dash.put("isFuoriCorso", studente.isFuoriCorso());
                    dash.put("rinnovoEffettuato", studente.isRinnovoEffettuatoPerAnnoCorrente());
                    dash.put("notificheCount", notif != null ? notif.size() : 0);
                    dash.put("esitiPendentiCount", pendenti != null ? pendenti.size() : 0);
                    dash.put("appelliPrenotatiCount", prenotati != null ? prenotati.size() : 0);
                    return ok(dash);
                }

                if (path.equals("/api/student/appelli-disponibili")) {
                    List<Appello> appelli = unicenter.trovaAppelliStudentePrenotabili();
                    List<Map<String, Object>> result = new ArrayList<>();
                    if (appelli != null) {
                        for (Appello a : appelli) {
                            Materia m = unicenter.getGestoreMaterie().trovaMaterieByCodice(a.getCodiceMateria());
                            result.add(Map.of(
                                    "codiceAppello", a.getCodiceAppello(),
                                    "codiceMateria", a.getCodiceMateria(),
                                    "nomeMateria", m != null ? m.getNome() : a.getCodiceMateria(),
                                    "cfu", m != null ? m.getCfu() : 0,
                                    "dataOra", a.getDataOra().toString(),
                                    "aula", a.getAula(),
                                    "posti", a.getPostiDisponibili(),
                                    "iscrittiCount", a.getIscritti() != null ? a.getIscritti().size() : 0,
                                    "vincolo", a.getVincoloLetteraCognome() != null ? a.getVincoloLetteraCognome() : "",
                                    "termineIscrizione", a.getTermineIscrizione().toString()
                            ));
                        }
                    }
                    return ok(result);
                }

                if (path.equals("/api/student/appelli-prenotati")) {
                    List<Appello> prenotati = unicenter.trovaAppelliPrenotatiDalloStudente();
                    List<Map<String, Object>> result = new ArrayList<>();
                    if (prenotati != null) {
                        for (Appello a : prenotati) {
                            Materia m = unicenter.getGestoreMaterie().trovaMaterieByCodice(a.getCodiceMateria());
                            result.add(Map.of(
                                    "codiceAppello", a.getCodiceAppello(),
                                    "codiceMateria", a.getCodiceMateria(),
                                    "nomeMateria", m != null ? m.getNome() : a.getCodiceMateria(),
                                    "cfu", m != null ? m.getCfu() : 0,
                                    "dataOra", a.getDataOra().toString(),
                                    "aula", a.getAula(),
                                    "termineIscrizione", a.getTermineIscrizione().toString()
                            ));
                        }
                    }
                    return ok(result);
                }

                if (path.equals("/api/student/prenota-appello") && "POST".equalsIgnoreCase(method)) {
                    String codiceAppello = (String) body.get("codiceAppello");
                    if (codiceAppello == null) return error("Codice appello mancante.");
                    boolean ok = unicenter.iscriviStudenteAdAppello(codiceAppello);
                    if (ok) return ok(Map.of("message", "Iscrizione avvenuta con successo!"));
                    return error("Iscrizione all'appello non riuscita.");
                }

                if (path.equals("/api/student/disiscrivi-appello") && "POST".equalsIgnoreCase(method)) {
                    String codiceAppello = (String) body.get("codiceAppello");
                    if (codiceAppello == null) return error("Codice appello mancante.");
                    boolean ok = unicenter.disiscriviStudenteDaAppello(codiceAppello);
                    if (ok) return ok(Map.of("message", "Prenotazione eliminata con successo."));
                    return error("Impossibile eliminare la prenotazione.");
                }

                if (path.equals("/api/student/notifiche")) {
                    List<Notifica> list = unicenter.getNotifichePerStudente();
                    List<Map<String, Object>> result = new ArrayList<>();
                    if (list != null) {
                        List<Notifica> sortedList = new ArrayList<>(list);
                        sortedList.sort((n1, n2) -> {
                            if (n1.getDataOra() == null && n2.getDataOra() == null) return 0;
                            if (n1.getDataOra() == null) return 1;
                            if (n2.getDataOra() == null) return -1;
                            return n2.getDataOra().compareTo(n1.getDataOra());
                        });
                        for (Notifica n : sortedList) {
                            result.add(Map.of(
                                    "titolo", n.getOggetto() != null ? n.getOggetto() : "Avviso",
                                    "messaggio", n.getMessaggio() != null ? n.getMessaggio() : "",
                                    "data", n.getDataOra() != null ? n.getDataOra().toString() : ""
                            ));
                        }
                    }
                    return ok(result);
                }

                if (path.equals("/api/student/esiti")) {
                    int scaduti = unicenter.verificaScadenzeVoti();
                    List<EsameSostenuto> pendenti = unicenter.getEsitiPendentiStudente();
                    List<EsameSostenuto> tutti = unicenter.getTuttiEsitiStudente();

                    List<Map<String, Object>> pendentiList = new ArrayList<>();
                    if (pendenti != null) {
                        for (EsameSostenuto e : pendenti) {
                            Materia m = unicenter.getGestoreMaterie().trovaMaterieByCodice(e.getCodiceMateria());
                            pendentiList.add(Map.of(
                                    "idVerbale", e.getIdVerbale(),
                                    "codiceMateria", e.getCodiceMateria(),
                                    "nomeMateria", m != null ? m.getNome() : e.getCodiceMateria(),
                                    "voto", e.getVotoNumerico(),
                                    "lode", e.isLode(),
                                    "stato", e.getNomeStato(),
                                    "scadenza", e.getScadenzaConferma() != null ? e.getScadenzaConferma().toString() : ""
                            ));
                        }
                    }

                    List<Map<String, Object>> tuttiList = new ArrayList<>();
                    if (tutti != null) {
                        for (EsameSostenuto e : tutti) {
                            Materia m = unicenter.getGestoreMaterie().trovaMaterieByCodice(e.getCodiceMateria());
                            tuttiList.add(Map.of(
                                    "idVerbale", e.getIdVerbale(),
                                    "codiceMateria", e.getCodiceMateria(),
                                    "nomeMateria", m != null ? m.getNome() : e.getCodiceMateria(),
                                    "voto", e.getVotoNumerico(),
                                    "lode", e.isLode(),
                                    "stato", e.getNomeStato(),
                                    "dataRegistrazione", e.getDataRegistrazione() != null ? e.getDataRegistrazione().toString() : ""
                            ));
                        }
                    }

                    return ok(Map.of("scadutiAuto", scaduti, "pendenti", pendentiList, "storico", tuttiList));
                }

                if (path.equals("/api/student/accetta-voto") && "POST".equalsIgnoreCase(method)) {
                    String idVerbale = (String) body.get("idVerbale");
                    if (idVerbale == null) return error("ID verbale mancante.");
                    boolean ok = unicenter.accettaVoto(idVerbale);
                    if (ok) return ok(Map.of("message", "Voto accettato e registrato nel libretto!"));
                    return error("Impossibile accettare il voto.");
                }

                if (path.equals("/api/student/rifiuta-voto") && "POST".equalsIgnoreCase(method)) {
                    String idVerbale = (String) body.get("idVerbale");
                    if (idVerbale == null) return error("ID verbale mancante.");
                    boolean ok = unicenter.rifiutaVoto(idVerbale);
                    if (ok) return ok(Map.of("message", "Voto rifiutato con successo."));
                    return error("Impossibile rifiutare il voto.");
                }

                if (path.equals("/api/student/libretto")) {
                    Libretto libretto = studente.getLibretto();
                    PianoDiStudi piano = studente.getPianoDiStudi();
                    String statoPiano = (piano != null) ? piano.getNomeStato() : "N/D";
                    boolean isRifiutato = "Rifiutato".equalsIgnoreCase(statoPiano);

                    List<String> obbligatorie = (piano != null) ? new ArrayList<>(piano.getIdMaterieObbligatorie()) : new ArrayList<>();
                    List<String> aScelta = (piano != null && !isRifiutato) ? new ArrayList<>(piano.getIdMaterieAScelta()) : new ArrayList<>();
                    // Se rifiutato ma ci sono esami verbalizzati tra le materie a scelta, mostrali comunque
                    if (piano != null && isRifiutato && libretto != null) {
                        for (String cod : piano.getIdMaterieAScelta()) {
                            if (libretto.isEsameSuperato(cod)) {
                                aScelta.add(cod);
                            }
                        }
                    }

                    List<String> tutteMateriePiano = new ArrayList<>(obbligatorie);
                    tutteMateriePiano.addAll(aScelta);

                    CorsoDiLaurea corso = unicenter.getGestioneCorsiLaureaController().trovaCorsoDiLaureaById(studente.getIdCorsoDiLaurea());

                    int cfuTotaliPiano = 0;
                    for (String cod : tutteMateriePiano) {
                        Materia m = unicenter.getGestoreMaterie().trovaMaterieByCodice(cod);
                        if (m != null) cfuTotaliPiano += m.getCfu();
                    }

                    int esamiSuperati = (libretto != null) ? libretto.getNumeroEsamiSuperati() : 0;
                    int cfuAcquisiti = (libretto != null) ? libretto.getTotaleCfu() : 0;
                    double media = (libretto != null && esamiSuperati > 0) ? libretto.getMediaPonderata() : 0.0;

                    final CorsoDiLaurea finalCorso = corso;
                    obbligatorie.sort((c1, c2) -> {
                        int a1 = (finalCorso != null) ? finalCorso.getAnnoDellaMateria(c1) : 0;
                        int a2 = (finalCorso != null) ? finalCorso.getAnnoDellaMateria(c2) : 0;
                        if (a1 != a2) return Integer.compare(a1, a2);
                        return c1.compareToIgnoreCase(c2);
                    });
                    aScelta.sort(String::compareToIgnoreCase);

                    List<Map<String, Object>> obbList = new ArrayList<>();
                    for (String cod : obbligatorie) {
                        Materia m = unicenter.getGestoreMaterie().trovaMaterieByCodice(cod);
                        int anno = (finalCorso != null) ? finalCorso.getAnnoDellaMateria(cod) : 1;
                        if (anno == 0) anno = 1;
                        EsameSostenuto sup = (libretto != null && libretto.isEsameSuperato(cod)) ? libretto.getEsameSuperato(cod) : null;
                        obbList.add(Map.of(
                                "codice", cod,
                                "nome", m != null ? m.getNome() : cod,
                                "cfu", m != null ? m.getCfu() : 0,
                                "anno", anno,
                                "superato", sup != null,
                                "voto", sup != null ? sup.getVotoNumerico() : 0,
                                "lode", sup != null && sup.isLode(),
                                "dataRegistrazione", (sup != null && sup.getDataRegistrazione() != null) ? sup.getDataRegistrazione().toString() : ""
                        ));
                    }

                    List<Map<String, Object>> sceltaList = new ArrayList<>();
                    for (String cod : aScelta) {
                        Materia m = unicenter.getGestoreMaterie().trovaMaterieByCodice(cod);
                        EsameSostenuto sup = (libretto != null && libretto.isEsameSuperato(cod)) ? libretto.getEsameSuperato(cod) : null;
                        sceltaList.add(Map.of(
                                "codice", cod,
                                "nome", m != null ? m.getNome() : cod,
                                "cfu", m != null ? m.getCfu() : 0,
                                "superato", sup != null,
                                "voto", sup != null ? sup.getVotoNumerico() : 0,
                                "lode", sup != null && sup.isLode(),
                                "dataRegistrazione", (sup != null && sup.getDataRegistrazione() != null) ? sup.getDataRegistrazione().toString() : ""
                        ));
                    }

                    return ok(Map.of(
                            "esamiSuperati", esamiSuperati,
                            "esamiTotali", tutteMateriePiano.size(),
                            "cfuAcquisiti", cfuAcquisiti,
                            "cfuTotali", cfuTotaliPiano,
                            "mediaPonderata", media,
                            "obbligatorie", obbList,
                            "aScelta", sceltaList,
                            "statoPiano", statoPiano
                    ));
                }

                if (path.equals("/api/student/tasse")) {
                    return ok(Map.of(
                            "importo", unicenter.getTasseStudente(),
                            "pagate", unicenter.isTassePagateStudente()
                    ));
                }

                if (path.equals("/api/student/paga-tasse") && "POST".equalsIgnoreCase(method)) {
                    boolean ok = unicenter.pagaTasseStudente();
                    if (ok) return ok(Map.of("message", "Pagamento completato con successo! Le tasse sono saldate."));
                    return error("Errore durante il pagamento delle tasse.");
                }

                if (path.equals("/api/student/rinnovo/status") || path.equals("/api/student/rinnovo-status")) {
                    return ok(unicenter.getStatoRinnovoStudenteCorrente());
                }

                if (path.equals("/api/student/rinnova-iscrizione") && "POST".equalsIgnoreCase(method)) {
                    try {
                        unicenter.rinnovaIscrizioneStudenteCorrente();
                        return ok(Map.of(
                                "message", "Rinnovo dell'iscrizione completato con successo!",
                                "stato", unicenter.getStatoRinnovoStudenteCorrente()
                        ));
                    } catch (Exception e) {
                        return error(e.getMessage());
                    }
                }

                if (path.equals("/api/student/piano-studi")) {
                    PianoDiStudi piano = studente.getPianoDiStudi();
                    List<Materia> disponibili = unicenter.getMaterieASceltaDisponibili();
                    List<String> verbalizzate = unicenter.getMaterieASceltaVerbalizzate();

                    CorsoDiLaurea corso = unicenter.getGestioneCorsiLaureaController().trovaCorsoDiLaureaById(studente.getIdCorsoDiLaurea());

                    List<Map<String, Object>> dispList = new ArrayList<>();
                    if (disponibili != null) {
                        for (Materia m : disponibili) {
                            boolean pre = (corso != null) && corso.isPreApprovata(m);
                            dispList.add(Map.of(
                                    "codice", m.getCodiceMateria(),
                                    "nome", m.getNome(),
                                    "cfu", m.getCfu(),
                                    "preApprovata", pre
                            ));
                        }
                    }

                    List<Map<String, Object>> scelteAttuali = new ArrayList<>();
                    if (piano != null) {
                        for (String cod : piano.getIdMaterieAScelta()) {
                            Materia m = unicenter.getGestoreMaterie().trovaMaterieByCodice(cod);
                            boolean pre = (corso != null && m != null) && corso.isPreApprovata(m);
                            scelteAttuali.add(Map.of(
                                    "codice", cod,
                                    "nome", m != null ? m.getNome() : cod,
                                    "cfu", m != null ? m.getCfu() : 0,
                                    "preApprovata", pre,
                                    "verbalizzata", verbalizzate.contains(cod)
                            ));
                        }
                    }

                    List<Map<String, Object>> obbList = new ArrayList<>();
                    if (piano != null) {
                        for (String cod : piano.getIdMaterieObbligatorie()) {
                            Materia m = unicenter.getGestoreMaterie().trovaMaterieByCodice(cod);
                            obbList.add(Map.of(
                                    "codice", cod,
                                    "nome", m != null ? m.getNome() : cod,
                                    "cfu", m != null ? m.getCfu() : 0
                            ));
                        }
                    }

                    return ok(Map.of(
                            "statoPiano", piano != null ? piano.getNomeStato() : "N/D",
                            "obbligatorie", obbList,
                            "aSceltaAttuali", scelteAttuali,
                            "materieDisponibili", dispList,
                            "verbalizzate", verbalizzate
                    ));
                }

                if (path.equals("/api/student/compila-piano") && "POST".equalsIgnoreCase(method)) {
                    List<?> codiciRaw = (List<?>) body.get("codici");
                    List<String> codici = new ArrayList<>();
                    if (codiciRaw != null) {
                        for (Object o : codiciRaw) {
                            if (o != null) codici.add(o.toString());
                        }
                    }
                    boolean ok = unicenter.compilaPianoDiStudi(codici);
                    String nuovoStato = studente.getPianoDiStudi() != null ? studente.getPianoDiStudi().getNomeStato() : "";
                    return ok(Map.of(
                            "success", ok,
                            "statoPiano", nuovoStato,
                            "message", "Piano di studi salvato con stato: " + nuovoStato
                    ));
                }
            }

            // ==========================================
            // PROFESSORE ENDPOINTS
            // ==========================================
            if (path.startsWith("/api/professor/")) {
                if (!(unicenter.getCurrentUser() instanceof Professore)) {
                    return error("Accesso non autorizzato. È richiesto il login come Professore.");
                }
                Professore professore = (Professore) unicenter.getCurrentUser();

                if (path.equals("/api/professor/materie")) {
                    List<Materia> materie = unicenter.getMaterieDelProfessore();
                    List<Map<String, Object>> result = new ArrayList<>();
                    if (materie != null) {
                        for (Materia m : materie) {
                            result.add(Map.of(
                                    "codice", m.getCodiceMateria(),
                                    "nome", m.getNome(),
                                    "cfu", m.getCfu()
                            ));
                        }
                    }
                    return ok(result);
                }

                if (path.equals("/api/professor/appelli")) {
                    List<Appello> appelli = unicenter.trovaAppelliProfessore();
                    List<Map<String, Object>> result = new ArrayList<>();
                    if (appelli != null) {
                        for (Appello a : appelli) {
                            Materia m = unicenter.getGestoreMaterie().trovaMaterieByCodice(a.getCodiceMateria());
                            result.add(Map.of(
                                    "codiceAppello", a.getCodiceAppello(),
                                    "codiceMateria", a.getCodiceMateria(),
                                    "nomeMateria", m != null ? m.getNome() : a.getCodiceMateria(),
                                    "dataOra", a.getDataOra().toString(),
                                    "aula", a.getAula(),
                                    "postiDisponibili", a.getPostiDisponibili(),
                                    "iscrittiCount", a.getIscritti() != null ? a.getIscritti().size() : 0,
                                    "vincolo", a.getVincoloLetteraCognome() != null ? a.getVincoloLetteraCognome() : "",
                                    "termineIscrizione", a.getTermineIscrizione().toString()
                            ));
                        }
                    }
                    return ok(result);
                }

                if (path.equals("/api/professor/crea-appello") && "POST".equalsIgnoreCase(method)) {
                    String codMateria = (String) body.get("codiceMateria");
                    String dataOraStr = (String) body.get("dataOra");
                    String aula = (String) body.get("aula");
                    Number postiNum = (Number) body.get("posti");
                    String vincolo = (String) body.get("vincolo");
                    String termineStr = (String) body.get("termineIscrizione");

                    if (codMateria == null || dataOraStr == null || aula == null || postiNum == null || termineStr == null) {
                        return error("Tutti i campi obbligatori devono essere compilati.");
                    }

                    if (!unicenter.isProfessoreAbilitatoAMateria(codMateria)) {
                        return error("Non sei abilitato a gestire la materia specificata.");
                    }

                    LocalDateTime dataOra;
                    try {
                        dataOra = (dataOraStr.contains("T")) ? LocalDateTime.parse(dataOraStr) : LocalDateTime.parse(dataOraStr, formatterInput);
                    } catch (Exception e) {
                        return error("Formato data e ora non valido. Utilizzare: dd/MM/yyyy HH:mm");
                    }

                    LocalDate termine;
                    try {
                        termine = (termineStr.contains("-")) ? LocalDate.parse(termineStr) : LocalDate.parse(termineStr, formatterInputData);
                    } catch (Exception e) {
                        return error("Formato data termine non valido. Utilizzare: dd/MM/yyyy");
                    }

                    unicenter.creaNuovoAppello(codMateria, dataOra, aula, postiNum.intValue(), vincolo != null ? vincolo : "", termine);
                    return ok(Map.of("message", "Appello creato con successo!"));
                }

                if (path.equals("/api/professor/modifica-appello") && "POST".equalsIgnoreCase(method)) {
                    String codAppello = (String) body.get("codiceAppello");
                    String dataOraStr = (String) body.get("dataOra");
                    String aula = (String) body.get("aula");
                    Number postiNum = (Number) body.get("posti");
                    String vincolo = (String) body.get("vincolo");
                    String termineStr = (String) body.get("termineIscrizione");

                    LocalDateTime dataOra;
                    try {
                        dataOra = LocalDateTime.parse(dataOraStr, formatterInput);
                    } catch (Exception e) {
                        dataOra = LocalDateTime.parse(dataOraStr);
                    }

                    LocalDate termine;
                    try {
                        termine = LocalDate.parse(termineStr, formatterInputData);
                    } catch (Exception e) {
                        termine = LocalDate.parse(termineStr);
                    }

                    boolean ok = unicenter.modificaAppello(codAppello, dataOra, aula, postiNum.intValue(), vincolo != null ? vincolo : "", termine);
                    if (ok) return ok(Map.of("message", "Appello modificato con successo!"));
                    return error("Impossibile modificare l'appello.");
                }

                if (path.equals("/api/professor/elimina-appello") && "POST".equalsIgnoreCase(method)) {
                    String codAppello = (String) body.get("codiceAppello");
                    if (codAppello == null) return error("Codice appello mancante.");
                    boolean ok = unicenter.eliminaAppello(codAppello);
                    if (ok) return ok(Map.of("message", "Appello eliminato con successo."));
                    return error("Errore durante l'eliminazione dell'appello.");
                }

                if (path.equals("/api/professor/iscritti-appello")) {
                    String codAppello = queryParams.get("codiceAppello");
                    if (codAppello == null) return error("Codice appello mancante.");
                    List<Studente> iscritti = unicenter.trovaIscrittiByAppello(codAppello);
                    List<Map<String, Object>> result = new ArrayList<>();
                    if (iscritti != null) {
                        for (Studente s : iscritti) {
                            result.add(Map.of(
                                    "matricola", s.getMatricola(),
                                    "nome", s.getNome(),
                                    "cognome", s.getCognome(),
                                    "email", s.getEmail(),
                                    "corso", s.getIdCorsoDiLaurea()
                            ));
                        }
                    }
                    return ok(result);
                }

                if (path.equals("/api/professor/iscritti-per-esito")) {
                    String codAppello = queryParams.get("codiceAppello");
                    if (codAppello == null) return error("Codice appello mancante.");

                    List<Appello> appelliProf = unicenter.trovaAppelliProfessore();
                    String codMateria = null;
                    for (Appello a : appelliProf) {
                        if (a.getCodiceAppello().equals(codAppello)) {
                            codMateria = a.getCodiceMateria();
                            break;
                        }
                    }
                    if (codMateria == null) return error("Appello non valido o non associato al docente.");

                    List<Studente> tuttiIscritti = unicenter.trovaIscrittiByAppello(codAppello);
                    List<Map<String, Object>> filtrati = new ArrayList<>();
                    if (tuttiIscritti != null) {
                        for (Studente s : tuttiIscritti) {
                            if (s.getLibretto() != null && s.getLibretto().isEsameSuperato(codMateria)) {
                                continue;
                            }
                            boolean haPendente = false;
                            List<EsameSostenuto> esitiPendenti = unicenter.getEsitiPendentiByMatricola(s.getMatricola());
                            if (esitiPendenti != null) {
                                for (EsameSostenuto e : esitiPendenti) {
                                    if (e.getCodiceMateria().equals(codMateria)) {
                                        haPendente = true;
                                        break;
                                    }
                                }
                            }
                            if (!haPendente) {
                                filtrati.add(Map.of(
                                        "matricola", s.getMatricola(),
                                        "nome", s.getNome(),
                                        "cognome", s.getCognome(),
                                        "email", s.getEmail(),
                                        "corso", s.getIdCorsoDiLaurea()
                                ));
                            }
                        }
                    }
                    return ok(Map.of("codiceMateria", codMateria, "studenti", filtrati));
                }

                if (path.equals("/api/professor/pubblica-esito") && "POST".equalsIgnoreCase(method)) {
                    String codAppello = (String) body.get("codiceAppello");
                    String matricola = (String) body.get("matricola");
                    String codMateria = (String) body.get("codiceMateria");
                    Number votoNum = (Number) body.get("voto");
                    Boolean lode = (Boolean) body.get("lode");
                    Number giorni = (Number) body.get("giorni");

                    if (codAppello == null || matricola == null || codMateria == null || votoNum == null) {
                        return error("Dati incompleti per la pubblicazione dell'esito.");
                    }

                    int gScad = giorni != null ? giorni.intValue() : 7;
                    EsameSostenuto esito = unicenter.pubblicaEsitoEsame(
                            codAppello, matricola, codMateria,
                            votoNum.intValue(), Boolean.TRUE.equals(lode), gScad
                    );

                    return ok(Map.of(
                            "message", "Esito pubblicato con successo!",
                            "idVerbale", esito.getIdVerbale(),
                            "stato", esito.getNomeStato(),
                            "scadenza", esito.getScadenzaConferma() != null ? esito.getScadenzaConferma().toString() : ""
                    ));
                }

                if (path.equals("/api/professor/esiti-pubblicati")) {
                    List<EsameSostenuto> list = unicenter.getEsitiProfessore();
                    List<Map<String, Object>> result = new ArrayList<>();
                    if (list != null) {
                        for (EsameSostenuto e : list) {
                            Materia m = unicenter.getGestoreMaterie().trovaMaterieByCodice(e.getCodiceMateria());
                            Studente s = unicenter.trovaStudente(e.getMatricolaStudente()).orElse(null);
                            result.add(Map.of(
                                    "idVerbale", e.getIdVerbale(),
                                    "codiceMateria", e.getCodiceMateria(),
                                    "nomeMateria", m != null ? m.getNome() : e.getCodiceMateria(),
                                    "matricola", e.getMatricolaStudente(),
                                    "nomeStudente", s != null ? (s.getNome() + " " + s.getCognome()) : "N/D",
                                    "voto", e.getVotoNumerico(),
                                    "lode", e.isLode(),
                                    "stato", e.getNomeStato(),
                                    "scadenza", e.getScadenzaConferma() != null ? e.getScadenzaConferma().toString() : ""
                            ));
                        }
                    }
                    return ok(result);
                }

                if (path.equals("/api/professor/destinatari-comunicazione")) {
                    String codMateria = queryParams.get("codiceMateria");
                    if (codMateria == null) return error("Codice materia mancante.");
                    List<Studente> dest = unicenter.getStudentiDestinatariComunicazione(codMateria);
                    return ok(Map.of("conteggio", dest != null ? dest.size() : 0));
                }

                if (path.equals("/api/professor/invia-comunicazione") && "POST".equalsIgnoreCase(method)) {
                    String codMateria = (String) body.get("codiceMateria");
                    String titolo = (String) body.get("titolo");
                    String msg = (String) body.get("messaggio");

                    if (codMateria == null || titolo == null || msg == null) {
                        return error("Tutti i campi sono obbligatori.");
                    }

                    int inviati = unicenter.inviaComunicazioneMateria(codMateria, titolo, msg);
                    return ok(Map.of("message", "Comunicazione pubblicata e inviata a " + inviati + " studenti."));
                }
            }

            // ==========================================
            // AMMINISTRATORE ENDPOINTS
            // ==========================================
            if (path.startsWith("/api/admin/")) {
                if (!(unicenter.getCurrentUser() instanceof Amministratore)) {
                    return error("Accesso non autorizzato. È richiesto il login come Amministratore.");
                }

                if (path.equals("/api/admin/stats")) {
                    return ok(Map.of(
                            "corsiCount", unicenter.getCorsiDiLaurea().size(),
                            "materieCount", unicenter.getTutteLeMaterie().size(),
                            "studentiCount", unicenter.getStudentiIscritti().size(),
                            "professoriCount", unicenter.getTuttiProfessori().size(),
                            "pianiInAttesaCount", unicenter.getPianiInAttesaApprovazione().size()
                    ));
                }

                if (path.equals("/api/admin/corsi")) {
                    List<CorsoDiLaurea> corsi = unicenter.getCorsiDiLaurea();
                    List<Map<String, Object>> result = new ArrayList<>();
                    if (corsi != null) {
                        for (CorsoDiLaurea c : corsi) {
                            List<Map<String, Object>> matList = new ArrayList<>();
                            for (Materia m : c.getMaterie()) {
                                matList.add(Map.of("codice", m.getCodiceMateria(), "nome", m.getNome(), "cfu", m.getCfu(), "anno", c.getAnnoDellaMateria(m.getCodiceMateria())));
                            }
                            result.add(Map.of(
                                    "id", c.getId(),
                                    "nome", c.getNome(),
                                    "tipologia", c.getTipologia(),
                                    "anni", c.getAnniAccademici(),
                                    "finalizzato", c.isFinalizzato(),
                                    "obsoleto", c.isObsoleto(),
                                    "materie", matList,
                                    "preApprovateCount", c.getMateriePreApprovate().size()
                            ));
                        }
                    }
                    return ok(result);
                }

                if (path.equals("/api/admin/crea-corso") && "POST".equalsIgnoreCase(method)) {
                    String nome = (String) body.get("nome");
                    String tipologia = (String) body.get("tipologia");
                    Number anni = (Number) body.get("anni");

                    if (nome == null || tipologia == null || anni == null) {
                        return error("Tutti i campi sono obbligatori.");
                    }

                    CorsoDiLaurea creato = unicenter.creaCorsoDiLaurea(nome, tipologia, anni.intValue());
                    return ok(Map.of("message", "Corso di laurea creato con successo!", "id", creato.getId()));
                }

                if (path.equals("/api/admin/aggiorna-corso") && "POST".equalsIgnoreCase(method)) {
                    String cod = (String) body.get("codice");
                    String nuovoNome = (String) body.get("nome");
                    String nuovaTipo = (String) body.get("tipologia");

                    boolean ok = unicenter.aggiornaCorsoDiLaurea(cod, nuovoNome, nuovaTipo);
                    if (ok) return ok(Map.of("message", "Corso aggiornato con successo."));
                    return error("Impossibile aggiornare il corso.");
                }

                if (path.equals("/api/admin/rendi-obsoleto-corso") && "POST".equalsIgnoreCase(method)) {
                    String cod = (String) body.get("codice");
                    boolean ok = unicenter.rendiObsoletoCorsoDiLaurea(cod);
                    if (ok) return ok(Map.of("message", "Corso reso obsoleto."));
                    return error("Impossibile rendere obsoleto il corso.");
                }

                if (path.equals("/api/admin/elimina-corso") && "POST".equalsIgnoreCase(method)) {
                    String cod = (String) body.get("codice");
                    boolean ok = unicenter.eliminaCorsoDiLaurea(cod);
                    if (ok) return ok(Map.of("message", "Corso eliminato."));
                    return error("Impossibile eliminare il corso.");
                }

                if (path.equals("/api/admin/finalizza-corso") && "POST".equalsIgnoreCase(method)) {
                    String cod = (String) body.get("codice");
                    unicenter.finalizzaCorso(cod);
                    return ok(Map.of("message", "Corso finalizzato con successo!"));
                }

                if (path.equals("/api/admin/corsi-non-finalizzati")) {
                    List<CorsoDiLaurea> lista = unicenter.getCorsiNonFinalizzati();
                    List<Map<String, Object>> result = new ArrayList<>();
                    if (lista != null) {
                        for (CorsoDiLaurea c : lista) {
                            result.add(Map.of("id", c.getId(), "nome", c.getNome(), "anni", c.getAnniAccademici()));
                        }
                    }
                    return ok(result);
                }

                if (path.equals("/api/admin/associa-materia-corso") && "POST".equalsIgnoreCase(method)) {
                    String codCorso = (String) body.get("codiceCorso");
                    Number anno = (Number) body.get("anno");
                    String codMateria = (String) body.get("codiceMateria");

                    Materia m = unicenter.getGestoreMaterie().trovaMaterieByCodice(codMateria);
                    if (m == null) return error("Materia non trovata.");

                    unicenter.associaMateriaACorso(codCorso, anno.intValue(), m);
                    return ok(Map.of("message", "Materia associata al corso con successo!"));
                }

                if (path.equals("/api/admin/materie")) {
                    List<Materia> list = unicenter.getTutteLeMaterie();
                    List<Map<String, Object>> result = new ArrayList<>();
                    if (list != null) {
                        for (Materia m : list) {
                            List<String> profIds = unicenter.getGestoreMaterie().trovaProfessoriDellaMateria(m.getCodiceMateria());
                            List<String> profNomi = new ArrayList<>();
                            for (String pid : profIds) {
                                unicenter.trovaProfessore(pid).ifPresent(p -> profNomi.add(p.getNome() + " " + p.getCognome()));
                            }
                            result.add(Map.of(
                                    "codice", m.getCodiceMateria(),
                                    "nome", m.getNome(),
                                    "cfu", m.getCfu(),
                                    "professori", profNomi
                            ));
                        }
                    }
                    return ok(result);
                }

                if (path.equals("/api/admin/crea-materia") && "POST".equalsIgnoreCase(method)) {
                    String nome = (String) body.get("nome");
                    Number cfu = (Number) body.get("cfu");
                    if (nome == null || cfu == null) return error("Nome e CFU sono obbligatori.");
                    Materia m = unicenter.creaMateria(nome, cfu.intValue());
                    return ok(Map.of("message", "Materia creata con successo!", "codice", m.getCodiceMateria()));
                }

                if (path.equals("/api/admin/professori")) {
                    List<Professore> list = unicenter.getTuttiProfessori();
                    List<Map<String, Object>> result = new ArrayList<>();
                    if (list != null) {
                        for (Professore p : list) {
                            result.add(Map.of(
                                    "id", p.getIdProfessore(),
                                    "nome", p.getNome(),
                                    "cognome", p.getCognome(),
                                    "email", p.getEmail()
                            ));
                        }
                    }
                    return ok(result);
                }

                if (path.equals("/api/admin/professori-non-associati")) {
                    String codMateria = queryParams.get("codiceMateria");
                    if (codMateria == null) return error("Codice materia mancante.");
                    List<Professore> list = unicenter.getProfessoriNonAssociatiAMateria(codMateria);
                    List<Map<String, Object>> result = new ArrayList<>();
                    if (list != null) {
                        for (Professore p : list) {
                            result.add(Map.of("id", p.getIdProfessore(), "nome", p.getNome(), "cognome", p.getCognome()));
                        }
                    }
                    return ok(result);
                }

                if (path.equals("/api/admin/associa-professore") && "POST".equalsIgnoreCase(method)) {
                    String idProf = (String) body.get("idProfessore");
                    String codMateria = (String) body.get("codiceMateria");
                    unicenter.associaProfessoreAMateriaAdmin(idProf, codMateria);
                    return ok(Map.of("message", "Professore associato alla materia!"));
                }

                if (path.equals("/api/admin/piani-in-attesa")) {
                    Map<String, PianoDiStudi> map = unicenter.getPianiInAttesaApprovazione();
                    List<Map<String, Object>> result = new ArrayList<>();
                    if (map != null) {
                        for (Map.Entry<String, PianoDiStudi> entry : map.entrySet()) {
                            String mat = entry.getKey();
                            PianoDiStudi piano = entry.getValue();
                            Studente st = unicenter.trovaStudente(mat).orElse(null);

                            CorsoDiLaurea corso = (st != null) ? unicenter.getGestioneCorsiLaureaController().trovaCorsoDiLaureaById(st.getIdCorsoDiLaurea()) : null;

                            List<Map<String, Object>> matScelta = new ArrayList<>();
                            for (String cM : piano.getIdMaterieAScelta()) {
                                Materia m = unicenter.getGestoreMaterie().trovaMaterieByCodice(cM);
                                boolean pre = (corso != null && m != null) && corso.isPreApprovata(m);
                                matScelta.add(Map.of(
                                        "codice", cM,
                                        "nome", m != null ? m.getNome() : cM,
                                        "cfu", m != null ? m.getCfu() : 0,
                                        "preApprovata", pre
                                ));
                            }

                            result.add(Map.of(
                                    "matricola", mat,
                                    "studenteNome", st != null ? (st.getNome() + " " + st.getCognome()) : "N/D",
                                    "corso", st != null ? st.getIdCorsoDiLaurea() : "N/D",
                                    "materieScelta", matScelta
                            ));
                        }
                    }
                    return ok(result);
                }

                if (path.equals("/api/admin/approva-piano") && "POST".equalsIgnoreCase(method)) {
                    String mat = (String) body.get("matricola");
                    boolean ok = unicenter.approvaPianoDiStudi(mat);
                    if (ok) return ok(Map.of("message", "Piano di studi approvato con successo!"));
                    return error("Impossibile approvare il piano di studi.");
                }

                if (path.equals("/api/admin/rifiuta-piano") && "POST".equalsIgnoreCase(method)) {
                    String mat = (String) body.get("matricola");
                    boolean ok = unicenter.rifiutaPianoDiStudi(mat);
                    if (ok) return ok(Map.of("message", "Piano di studi rifiutato."));
                    return error("Impossibile rifiutare il piano di studi.");
                }

                if (path.equals("/api/admin/materie-preapprovate")) {
                    String codCorso = queryParams.get("codiceCorso");
                    if (codCorso == null) return error("Codice corso mancante.");
                    List<Materia> pre = unicenter.getMateriePreApprovateByCorso(codCorso);
                    List<Map<String, Object>> result = new ArrayList<>();
                    if (pre != null) {
                        for (Materia m : pre) {
                            result.add(Map.of("codice", m.getCodiceMateria(), "nome", m.getNome(), "cfu", m.getCfu()));
                        }
                    }
                    return ok(result);
                }

                if (path.equals("/api/admin/aggiungi-preapprovata") && "POST".equalsIgnoreCase(method)) {
                    String codCorso = (String) body.get("codiceCorso");
                    String codMateria = (String) body.get("codiceMateria");
                    Materia m = unicenter.getGestoreMaterie().trovaMaterieByCodice(codMateria);
                    if (m == null) return error("Materia non trovata.");
                    unicenter.aggiungiMateriaPreApprovata(codCorso, m);
                    return ok(Map.of("message", "Materia aggiunta alle pre-approvate!"));
                }

                if (path.equals("/api/admin/rimuovi-preapprovata") && "POST".equalsIgnoreCase(method)) {
                    String codCorso = (String) body.get("codiceCorso");
                    String codMateria = (String) body.get("codiceMateria");
                    Materia m = unicenter.getGestoreMaterie().trovaMaterieByCodice(codMateria);
                    if (m == null) return error("Materia non trovata.");
                    unicenter.rimuoviMateriaPreApprovata(codCorso, m);
                    return ok(Map.of("message", "Materia rimossa dalle pre-approvate."));
                }
            }

            // ==========================================
            // MATERIALE DIDATTICO (UC6 & UC10)
            // ==========================================
            if (path.startsWith("/api/materiale/")) {
                Utente user = unicenter.getCurrentUser();

                // 1. Materie disponibili per materiale didattico
                if (path.equals("/api/materiale/materie") && "GET".equalsIgnoreCase(method)) {
                    List<Materia> materie;
                    if (user instanceof Professore) {
                        materie = unicenter.getMaterieDelProfessore();
                    } else if (user instanceof Studente s && s.getPianoDiStudi() != null) {
                        List<String> codici = new ArrayList<>(s.getPianoDiStudi().getIdMaterieObbligatorie());
                        codici.addAll(s.getPianoDiStudi().getIdMaterieAScelta());
                        materie = new ArrayList<>();
                        for (String cod : codici) {
                            Materia m = unicenter.getGestoreMaterie().trovaMaterieByCodice(cod);
                            if (m != null && !materie.contains(m)) materie.add(m);
                        }
                        // Se il piano è vuoto, mostra tutte le materie
                        if (materie.isEmpty()) {
                            materie = unicenter.getTutteLeMaterie();
                        }
                    } else {
                        materie = unicenter.getTutteLeMaterie();
                    }

                    List<Map<String, Object>> result = new ArrayList<>();
                    for (Materia m : materie) {
                        it.project.materiale.Cartella radice = unicenter.getAlberoMaterialeMateria(m.getCodiceMateria());
                        List<String> docentiIds = unicenter.getGestoreMaterie().trovaProfessoriDellaMateria(m.getCodiceMateria());
                        List<String> docentiNomi = new ArrayList<>();
                        for (String idDoc : docentiIds) {
                            unicenter.trovaProfessore(idDoc).ifPresent(p -> docentiNomi.add("Prof. " + p.getNome() + " " + p.getCognome()));
                        }

                        String tipoMateria = "Insegnamento";
                        if (user instanceof Studente s && s.getPianoDiStudi() != null) {
                            if (s.getPianoDiStudi().getIdMaterieObbligatorie().contains(m.getCodiceMateria())) {
                                tipoMateria = "Obbligatoria";
                            } else if (s.getPianoDiStudi().getIdMaterieAScelta().contains(m.getCodiceMateria())) {
                                tipoMateria = "A Scelta";
                            }
                        }

                        int totFile = contaFileRicorsivi(radice);
                        int totCartelle = contaCartelleRicorsive(radice);

                        result.add(Map.of(
                                "codice", m.getCodiceMateria(),
                                "nome", m.getNome(),
                                "cfu", m.getCfu(),
                                "tipo", tipoMateria,
                                "docenti", docentiNomi,
                                "totaleElementi", radice.elenca().size(),
                                "totaleFile", totFile,
                                "totaleCartelle", totCartelle,
                                "dimensioneBytes", radice.getDimensioneBytes()
                        ));
                    }
                    return ok(result);
                }

                // 2. Albero Composite di una materia
                if (path.equals("/api/materiale/albero") && "GET".equalsIgnoreCase(method)) {
                    String codMateria = queryParams.get("codiceMateria");
                    if (codMateria == null || codMateria.trim().isEmpty()) {
                        return error("Parametro 'codiceMateria' obbligatorio.");
                    }
                    it.project.materiale.Cartella radice = unicenter.getAlberoMaterialeMateria(codMateria);
                    Studente st = (user instanceof Studente) ? (Studente) user : null;
                    return ok(serializeElemento(radice, st));
                }

                // 3. Creazione cartella (Professore UC6)
                if (path.equals("/api/materiale/cartella") && "POST".equalsIgnoreCase(method)) {
                    if (!(user instanceof Professore)) {
                        return error("Solo un professore può creare cartelle.");
                    }
                    String codMateria = (String) body.get("codiceMateria");
                    String idCartellaGenitore = (String) body.get("idCartellaGenitore");
                    String nome = (String) body.get("nome");
                    String descrizione = (String) body.get("descrizione");

                    it.project.materiale.Cartella nuova = unicenter.creaCartellaMateriale(codMateria, idCartellaGenitore, nome, descrizione);
                    return ok(Map.of(
                            "message", "Cartella creata con successo!",
                            "cartella", serializeElemento(nuova, null)
                    ));
                }

                // 4. Upload materiale didattico (Professore UC6)
                if (path.equals("/api/materiale/upload") && "POST".equalsIgnoreCase(method)) {
                    if (!(user instanceof Professore)) {
                        return error("Solo un professore può caricare materiale didattico.");
                    }
                    String codMateria = (String) body.get("codiceMateria");
                    String idCartellaGenitore = (String) body.get("idCartellaGenitore");
                    String nome = (String) body.get("nome");
                    String descrizione = (String) body.get("descrizione");
                    String tipoStr = (String) body.get("tipo");
                    String contenutoTesto = (String) body.get("contenutoTesto");
                    String contenutoBase64 = (String) body.get("contenutoBase64");
                    String url = (String) body.get("url");

                    it.project.materiale.TipoMateriale tipo;
                    try {
                        tipo = it.project.materiale.TipoMateriale.valueOf(tipoStr.toUpperCase());
                    } catch (Exception e) {
                        tipo = it.project.materiale.TipoMateriale.TESTO;
                    }

                    byte[] bytes = new byte[0];
                    if (contenutoBase64 != null && !contenutoBase64.trim().isEmpty()) {
                        try {
                            bytes = Base64.getDecoder().decode(contenutoBase64);
                        } catch (IllegalArgumentException ex) {
                            bytes = contenutoBase64.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                        }
                    } else if (contenutoTesto != null) {
                        bytes = contenutoTesto.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    } else if (url != null) {
                        bytes = url.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    }

                    it.project.materiale.MaterialeDidattico mat = unicenter.caricaMaterialeDidattico(
                            codMateria, idCartellaGenitore, nome, descrizione, tipo, bytes);

                    return ok(Map.of(
                            "message", "Materiale didattico caricato con successo!",
                            "materiale", serializeElemento(mat, null)
                    ));
                }

                // 5. Eliminazione materiale o cartella (Professore UC6)
                if ((path.equals("/api/materiale/elimina") || path.equals("/api/materiale/rimuovi")) &&
                        ("DELETE".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method))) {
                    if (!(user instanceof Professore)) {
                        return error("Solo un professore può eliminare materiale didattico.");
                    }
                    String codMateria = (String) body.get("codiceMateria");
                    if (codMateria == null) codMateria = queryParams.get("codiceMateria");

                    String idElemento = (String) body.get("idElemento");
                    if (idElemento == null) idElemento = queryParams.get("idElemento");

                    boolean rimosso = unicenter.eliminaMaterialeDidattico(codMateria, idElemento);
                    if (rimosso) {
                        return ok(Map.of("message", "Elemento eliminato con successo!"));
                    } else {
                        return error("Impossibile eliminare l'elemento selezionato.");
                    }
                }

                // 6. Anteprima polimorfica (UC10)
                if (path.equals("/api/materiale/anteprima") && "GET".equalsIgnoreCase(method)) {
                    String id = queryParams.get("id");
                    if (id == null) id = queryParams.get("idElemento");
                    if (id == null) return error("Parametro 'id' mancante.");

                    it.project.materiale.AnteprimaRisultato ant = unicenter.consultaMaterialeDidattico(id);
                    Map<String, Object> antMap = new HashMap<>();
                    antMap.put("id", ant.getId());
                    antMap.put("nome", ant.getNome());
                    antMap.put("descrizione", ant.getDescrizione());
                    antMap.put("tipo", ant.getTipo() != null ? ant.getTipo().name() : "CARTELLA");
                    antMap.put("mimeType", ant.getMimeType());
                    antMap.put("contenutoTestuale", ant.getContenutoTestuale());
                    antMap.put("urlEsterno", ant.getUrlEsterno());
                    antMap.put("downloadUrl", ant.getDownloadUrl());
                    antMap.put("dimensioneBytes", ant.getDimensioneBytes());
                    antMap.put("metadati", ant.getMetadatiExtra());

                    return ok(antMap);
                }

                // 7. Gestione Preferiti Studente (UC10)
                if (path.equals("/api/materiale/preferiti") && "GET".equalsIgnoreCase(method)) {
                    if (!(user instanceof Studente s)) {
                        return error("Solo uno studente autenticato può accedere alla sezione preferiti.");
                    }
                    List<it.project.materiale.ElementoDidattico> prefs = unicenter.getPreferitiMaterialeStudente();
                    List<Map<String, Object>> result = new ArrayList<>();
                    for (it.project.materiale.ElementoDidattico e : prefs) {
                        result.add(serializeElemento(e, s));
                    }
                    return ok(result);
                }

                // 8. Toggle Preferito Studente (UC10)
                if (path.equals("/api/materiale/preferiti/toggle") && "POST".equalsIgnoreCase(method)) {
                    if (!(user instanceof Studente)) {
                        return error("Solo uno studente autenticato può salvare preferiti.");
                    }
                    String idElemento = (String) body.get("idElemento");
                    if (idElemento == null) return error("Parametro 'idElemento' mancante.");

                    boolean preferito = unicenter.togglePreferitoMateriale(idElemento);
                    return ok(Map.of(
                            "preferito", preferito,
                            "message", preferito ? "Elemento aggiunto ai tuoi preferiti!" : "Elemento rimosso dai preferiti."
                    ));
                }
            }

            return error("Endpoint non trovato: " + path);
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("Errore interno del server: " + e.getMessage());
        }
    }

    private Map<String, Object> serializeElemento(it.project.materiale.ElementoDidattico elem, Studente currentStudent) {
        if (elem == null) return Collections.emptyMap();
        Map<String, Object> map = new HashMap<>();
        map.put("id", elem.getId());
        map.put("nome", elem.getNome());
        map.put("descrizione", elem.getDescrizione());
        map.put("pathRelativo", elem.getPathRelativo());
        map.put("dataCreazione", elem.getDataCreazione() != null ? elem.getDataCreazione().toString() : "");
        map.put("dimensioneBytes", elem.getDimensioneBytes());
        map.put("isCartella", elem.isCartella());
        map.put("ownerProfessoreId", elem.getOwnerProfessoreId());
        map.put("codiceMateria", elem.getCodiceMateria());

        if (currentStudent != null) {
            map.put("isPreferito", currentStudent.isPreferito(elem.getId()));
        } else {
            map.put("isPreferito", false);
        }

        if (elem instanceof it.project.materiale.Cartella c) {
            List<Map<String, Object>> figli = new ArrayList<>();
            for (it.project.materiale.ElementoDidattico child : c.elenca()) {
                figli.add(serializeElemento(child, currentStudent));
            }
            map.put("elementi", figli);
            map.put("tipo", "CARTELLA");
            map.put("icona", "📁");
        } else if (elem instanceof it.project.materiale.MaterialeDidattico m) {
            map.put("tipo", m.getTipo().name());
            map.put("tipoDescrizione", m.getTipo().getDescrizione());
            map.put("mimeType", m.getMimeType());
            map.put("icona", m.getTipo().getIcona());
        }
        return map;
    }

    private int contaFileRicorsivi(it.project.materiale.Cartella cartella) {
        if (cartella == null) return 0;
        int count = 0;
        for (it.project.materiale.ElementoDidattico e : cartella.elenca()) {
            if (e instanceof it.project.materiale.Cartella c) {
                count += contaFileRicorsivi(c);
            } else {
                count++;
            }
        }
        return count;
    }

    private int contaCartelleRicorsive(it.project.materiale.Cartella cartella) {
        if (cartella == null) return 0;
        int count = 0;
        for (it.project.materiale.ElementoDidattico e : cartella.elenca()) {
            if (e instanceof it.project.materiale.Cartella c) {
                count += 1 + contaCartelleRicorsive(c);
            }
        }
        return count;
    }

    private Map<String, Object> buildUserData(Utente user) {
        if (user == null) return Collections.emptyMap();
        Map<String, Object> map = new HashMap<>();
        map.put("nome", user.getNome());
        map.put("cognome", user.getCognome());
        map.put("email", user.getEmail());
        map.put("codiceFiscale", user.getCodiceFiscale());

        if (user instanceof Studente s) {
            map.put("ruolo", "studente");
            map.put("matricola", s.getMatricola());
            map.put("corsoDiLaurea", s.getIdCorsoDiLaurea());
            map.put("tassePagate", s.isTassePagate());
        } else if (user instanceof Professore p) {
            map.put("ruolo", "professore");
            map.put("idProfessore", p.getIdProfessore());
        } else if (user instanceof Amministratore a) {
            map.put("ruolo", "amministratore");
            map.put("idAmministratore", a.getIdAmministratore());
        }
        return map;
    }

    private Map<String, Object> ok(Object data) {
        return Map.of("success", true, "data", data);
    }

    private Map<String, Object> error(String message) {
        return Map.of("success", false, "error", message != null ? message : "Errore sconosciuto");
    }
}
