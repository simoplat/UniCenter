package it.project.database;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import it.project.Amministratore;
import it.project.Appello;
import it.project.ConsoleUI;
import it.project.CorsoDiLaurea;
import it.project.EsameSostenuto;
import it.project.Materia;
import it.project.Notifica;
import it.project.Professore;
import it.project.Studente;
import it.project.Unicenter;
import it.project.controller.GestioneAppelliController;
import it.project.controller.GestioneCorsiLaureaController;
import it.project.controller.GestioneVotoController;
import it.project.controller.GestoreMaterieController;
import it.project.factory.CorsoDiLaureaFactory;

/**
 * Popolatore del Database in-memory di UniCenter.
 * Inserisce un ricco dataset di prova (almeno 30 record per entità: Materie, Corsi, Utenti, Appelli, Esiti).
 * Utilizza ClockProvider per simulare il passaggio del tempo rispettando tutte le regole di business
 * e i vincoli temporali (immatricolazione a settembre, creazione appelli nel futuro, iscrizioni entro termine,
 * pubblicazione esiti solo il giorno dell'appello, verbalizzazione voti nel libretto),
 * ripristinando infine l'orologio al tempo reale di sistema.
 */
public class DatabasePopulator {

    private final Unicenter unicenter;
    private final ConsoleUI console = ConsoleUI.getInstance();

    public DatabasePopulator(Unicenter unicenter) {
        this.unicenter = unicenter;
    }

    public void popolaDataBase() {
        if (unicenter.esisteUtente("admin@unicenter.it") || unicenter.esisteUtente("mario.rossi@studenti.it")) {
            return;
        }

        try {
            console.mostraMessaggio("[DB POPULATION] Avvio popolamento database strutturato con simulazione temporale...");

            GestoreMaterieController gestoreMaterie = unicenter.getGestoreMaterie();
            GestioneCorsiLaureaController gestioneCorsi = unicenter.getGestioneCorsiLaureaController();
            GestioneAppelliController gestioneAppelli = unicenter.getGestioneAppelliController();
            GestioneVotoController gestioneVoto = unicenter.getGestioneVotoController();

            // =========================================================================
            // 1. INSERIMENTO MATERIE (36 Materie >= 30)
            // =========================================================================
            console.mostraMessaggio("[DB POPULATION] Creazione di 36 materie...");
            List<Materia> materieList = new ArrayList<>();
            Object[][] materieData = {
                {"IS01", "Ingegneria del Software", 9},
                {"BD01", "Basi di Dati", 6},
                {"AR01", "Architettura dei Calcolatori", 6},
                {"AM01", "Analisi Matematica 1", 9},
                {"AM02", "Analisi Matematica 2", 9},
                {"FIS01", "Fisica Generale 1", 9},
                {"FIS02", "Fisica Generale 2", 6},
                {"ALG01", "Algebra Lineare e Geometria", 6},
                {"PRG01", "Programmazione 1", 9},
                {"PRG02", "Programmazione 2", 9},
                {"SO01", "Sistemi Operativi", 9},
                {"RET01", "Reti di Calcolatori", 9},
                {"ASD01", "Algoritmi e Strutture Dati", 9},
                {"WEB01", "Tecnologie Web", 6},
                {"SIC01", "Sicurezza Informatica", 6},
                {"IA01", "Intelligenza Artificiale", 6},
                {"ML01", "Machine Learning", 6},
                {"ROB01", "Robotica e Automazione", 6},
                {"ELE01", "Elettrotecnica", 6},
                {"ELN01", "Elettronica Applicata", 9},
                {"MEC01", "Meccanica Razionale", 6},
                {"TD01", "Termodinamica Applicata", 6},
                {"GES01", "Economia e Gestione Aziendale", 6},
                {"FIN01", "Economia e Finanza", 9},
                {"DIR01", "Diritto Privato", 9},
                {"DIR02", "Diritto Pubblico", 9},
                {"BIO01", "Biologia Generale", 6},
                {"CHM01", "Chimica Generale", 6},
                {"STAT01", "Statistica e Calcolo delle Probabilita", 6},
                {"CLOUD01", "Cloud Computing", 6},
                {"IOT01", "Internet of Things", 6},
                {"GRAF01", "Computer Graphics", 6},
                {"LING01", "Lingua Inglese B2", 6},
                {"MAT01", "Metodi Numerici per l'Ingegneria", 6},
                {"ECO01", "Microeconomia", 6},
                {"BIOINF01", "Bioinformatica", 6}
            };

            for (Object[] row : materieData) {
                Materia m = new Materia((String) row[0], (String) row[1], (Integer) row[2]);
                gestoreMaterie.addMateria(m);
                materieList.add(m);
            }

            // =========================================================================
            // 2. INSERIMENTO DOCENTI E AMMINISTRATORI (12 Docenti + 2 Admin)
            // =========================================================================
            console.mostraMessaggio("[DB POPULATION] Creazione docenti e amministratori...");
            List<Professore> docenti = new ArrayList<>();

            docenti.add(new Professore("1", "Mario", "Rossi", "mario.rossi@unicenter.it", "pass123", "RSSMRA80A01H501U"));
            docenti.add(new Professore("2", "Giuseppe", "Verdi", "giuseppe.verdi@unicenter.it", "pass123", "VRDGPP75B02F205X"));
            docenti.add(new Professore("3", "Laura", "Bianchi", "laura.bianchi@unicenter.it", "pass123", "BNCLRA78C43H501W"));
            docenti.add(new Professore("4", "Roberto", "Neri", "roberto.neri@unicenter.it", "pass123", "NRERBT72D15F205K"));
            docenti.add(new Professore("5", "Elena", "Ferrari", "elena.ferrari@unicenter.it", "pass123", "FRRLNE82E55H501Z"));
            docenti.add(new Professore("6", "Marco", "Romano", "marco.romano@unicenter.it", "pass123", "RMNMRC76F22F205M"));
            docenti.add(new Professore("7", "Francesca", "Colombo", "francesca.colombo@unicenter.it", "pass123", "CLMFNC85G61H501Y"));
            docenti.add(new Professore("8", "Antonio", "Ricci", "antonio.ricci@unicenter.it", "pass123", "RCCNTN79H18F205Q"));
            docenti.add(new Professore("9", "Silvia", "Marino", "silvia.marino@unicenter.it", "pass123", "MRNSLV83I49H501P"));
            docenti.add(new Professore("10", "Davide", "Conti", "davide.conti@unicenter.it", "pass123", "CNTDVD74L11F205T"));
            docenti.add(new Professore("11", "Chiara", "Greco", "chiara.greco@unicenter.it", "pass123", "GRCCHR88M52H501R"));
            docenti.add(new Professore("12", "Alessandro", "De Luca", "alessandro.deluca@unicenter.it", "pass123", "DLCLSS81N25F205S"));

            for (Professore p : docenti) {
                unicenter.addUtente(p);
            }

            // Associazione Docenti <-> Materie
            gestoreMaterie.associaProfessoreAMateria("1", "IS01");
            gestoreMaterie.associaProfessoreAMateria("1", "BD01");
            gestoreMaterie.associaProfessoreAMateria("1", "AR01");
            gestoreMaterie.associaProfessoreAMateria("1", "PRG01");
            gestoreMaterie.associaProfessoreAMateria("1", "PRG02");

            gestoreMaterie.associaProfessoreAMateria("2", "AR01");
            gestoreMaterie.associaProfessoreAMateria("2", "SO01");
            gestoreMaterie.associaProfessoreAMateria("2", "RET01");
            gestoreMaterie.associaProfessoreAMateria("2", "SIC01");

            gestoreMaterie.associaProfessoreAMateria("3", "AM01");
            gestoreMaterie.associaProfessoreAMateria("3", "AM02");
            gestoreMaterie.associaProfessoreAMateria("3", "ALG01");
            gestoreMaterie.associaProfessoreAMateria("3", "STAT01");

            gestoreMaterie.associaProfessoreAMateria("4", "FIS01");
            gestoreMaterie.associaProfessoreAMateria("4", "FIS02");
            gestoreMaterie.associaProfessoreAMateria("4", "MEC01");

            gestoreMaterie.associaProfessoreAMateria("5", "ASD01");
            gestoreMaterie.associaProfessoreAMateria("5", "IA01");
            gestoreMaterie.associaProfessoreAMateria("5", "ML01");

            gestoreMaterie.associaProfessoreAMateria("6", "WEB01");
            gestoreMaterie.associaProfessoreAMateria("6", "CLOUD01");
            gestoreMaterie.associaProfessoreAMateria("6", "IOT01");

            gestoreMaterie.associaProfessoreAMateria("7", "ELE01");
            gestoreMaterie.associaProfessoreAMateria("7", "ELN01");
            gestoreMaterie.associaProfessoreAMateria("7", "ROB01");

            gestoreMaterie.associaProfessoreAMateria("8", "TD01");
            gestoreMaterie.associaProfessoreAMateria("8", "CHM01");

            gestoreMaterie.associaProfessoreAMateria("9", "GES01");
            gestoreMaterie.associaProfessoreAMateria("9", "FIN01");
            gestoreMaterie.associaProfessoreAMateria("9", "ECO01");

            gestoreMaterie.associaProfessoreAMateria("10", "DIR01");
            gestoreMaterie.associaProfessoreAMateria("10", "DIR02");

            gestoreMaterie.associaProfessoreAMateria("11", "BIO01");
            gestoreMaterie.associaProfessoreAMateria("11", "BIOINF01");

            gestoreMaterie.associaProfessoreAMateria("12", "GRAF01");
            gestoreMaterie.associaProfessoreAMateria("12", "MAT01");
            gestoreMaterie.associaProfessoreAMateria("12", "LING01");

            // Amministratori
            Amministratore admin1 = new Amministratore("ADM-001", "Admin", "Sistema", "admin@unicenter.it", "admin123", "ADMSST80A01H501X");
            Amministratore admin2 = new Amministratore("ADM-002", "Segreteria", "Studenti", "segreteria@unicenter.it", "admin123", "SGRSTD85B02F205Z");
            unicenter.addUtente(admin1);
            unicenter.addUtente(admin2);

            // =========================================================================
            // 3. CREAZIONE CORSI DI LAUREA (32 Corsi >= 30)
            // =========================================================================
            console.mostraMessaggio("[DB POPULATION] Creazione di 32 corsi di laurea...");

            String[][] corsiInfo = {
                {"ING-INF", "Ingegneria Informatica", "Laurea Triennale", "3"},
                {"ING-MEC", "Ingegneria Meccanica", "Laurea Triennale", "3"},
                {"ING-GES", "Ingegneria Gestionale", "Laurea Triennale", "3"},
                {"ING-ELE", "Ingegneria Elettronica", "Laurea Triennale", "3"},
                {"ING-CIV", "Ingegneria Civile", "Laurea Triennale", "3"},
                {"ING-AER", "Ingegneria Aerospaziale", "Laurea Triennale", "3"},
                {"ING-BIO", "Ingegneria Biomedica", "Laurea Triennale", "3"},
                {"ING-CHM", "Ingegneria Chimica", "Laurea Triennale", "3"},
                {"ING-ENE", "Ingegneria Energetica", "Laurea Triennale", "3"},
                {"ING-TEL", "Ingegneria delle Telecomunicazioni", "Laurea Triennale", "3"},
                {"ING-AUT", "Ingegneria dell'Automazione", "Laurea Triennale", "3"},
                {"INF-01", "Informatica", "Laurea Triennale", "3"},
                {"MAT-01", "Matematica", "Laurea Triennale", "3"},
                {"FIS-01", "Fisica", "Laurea Triennale", "3"},
                {"CHM-01", "Chimica", "Laurea Triennale", "3"},
                {"BIO-01", "Biologia", "Laurea Triennale", "3"},
                {"BT-01", "Biotecnologie", "Laurea Triennale", "3"},
                {"ECO-AZ", "Economia Aziendale", "Laurea Triennale", "3"},
                {"ECO-FIN", "Economia e Finanza", "Laurea Triennale", "3"},
                {"STAT-01", "Statistica e Data Science", "Laurea Triennale", "3"},
                {"COM-01", "Scienze della Comunicazione", "Laurea Triennale", "3"},
                {"POL-01", "Scienze Politiche", "Laurea Triennale", "3"},
                {"LET-01", "Lettere Moderne", "Laurea Triennale", "3"},
                {"FIL-01", "Filosofia", "Laurea Triennale", "3"},
                {"LIN-01", "Lingue e Letterature Straniere", "Laurea Triennale", "3"},
                {"ING-MAG-SW", "Ingegneria del Software Avanzata", "Laurea Magistrale", "2"},
                {"MAG-DS", "Data Science and Artificial Intelligence", "Laurea Magistrale", "2"},
                {"GIU-01", "Giurisprudenza", "Laurea Magistrale a Ciclo Unico (5 anni)", "5"},
                {"ARC-01", "Architettura", "Laurea Magistrale a Ciclo Unico (5 anni)", "5"},
                {"FAR-01", "Farmacia", "Laurea Magistrale a Ciclo Unico (5 anni)", "5"},
                {"ING-NAV", "Ingegneria Navale", "Laurea Triennale", "3"}, // Bozza non finalizzata
                {"MAT-OLD", "Scienze dei Materiali Antica", "Laurea Triennale", "3"} // Obsoleto
            };

            for (String[] cData : corsiInfo) {
                String idCorso = cData[0];
                String nomeCorso = cData[1];
                int anni = Integer.parseInt(cData[3]);

                CorsoDiLaurea cdl = new CorsoDiLaurea(idCorso, nomeCorso, anni);

                if ("Ingegneria Informatica".equals(nomeCorso)) {
                    cdl.aggiungiMateriaAdAnno(1, gestoreMaterie.trovaMaterieByCodice("AM01"));
                    cdl.aggiungiMateriaAdAnno(1, gestoreMaterie.trovaMaterieByCodice("ALG01"));
                    cdl.aggiungiMateriaAdAnno(1, gestoreMaterie.trovaMaterieByCodice("PRG01"));
                    cdl.aggiungiMateriaAdAnno(1, gestoreMaterie.trovaMaterieByCodice("AR01"));
                    cdl.aggiungiMateriaAdAnno(2, gestoreMaterie.trovaMaterieByCodice("AM02"));
                    cdl.aggiungiMateriaAdAnno(2, gestoreMaterie.trovaMaterieByCodice("PRG02"));
                    cdl.aggiungiMateriaAdAnno(2, gestoreMaterie.trovaMaterieByCodice("IS01"));
                    cdl.aggiungiMateriaAdAnno(2, gestoreMaterie.trovaMaterieByCodice("BD01"));
                    cdl.aggiungiMateriaAdAnno(3, gestoreMaterie.trovaMaterieByCodice("SO01"));
                    cdl.aggiungiMateriaAdAnno(3, gestoreMaterie.trovaMaterieByCodice("RET01"));
                    cdl.aggiungiMateriaAdAnno(3, gestoreMaterie.trovaMaterieByCodice("SIC01"));
                    cdl.aggiungiMateriaAdAnno(3, gestoreMaterie.trovaMaterieByCodice("IA01"));
                    cdl.finalizza();
                } else if (!"Ingegneria Navale".equals(nomeCorso) && !"Scienze dei Materiali Antica".equals(nomeCorso)) {
                    // Associa materie base di default per ogni corso
                    cdl.aggiungiMateriaAdAnno(1, gestoreMaterie.trovaMaterieByCodice("AM01"));
                    cdl.aggiungiMateriaAdAnno(1, gestoreMaterie.trovaMaterieByCodice("FIS01"));
                    cdl.aggiungiMateriaAdAnno(2, gestoreMaterie.trovaMaterieByCodice("STAT01"));
                    cdl.finalizza();
                }

                if ("Scienze dei Materiali Antica".equals(nomeCorso)) {
                    cdl.rendiObsoleto();
                }

                gestioneCorsi.addCorsoDiLaurea(cdl);
            }

            // =========================================================================
            // SIMULAZIONE TEMPORALE CON CLOCK: FASE 1 - IMMATRICOLAZIONI (Agosto 2026)
            // =========================================================================
            console.mostraMessaggio("[DB POPULATION] Simulazione temporale: Finestra Immatricolazioni (Agosto 2026)...");
            ClockProvider.setFixedDateTime(LocalDateTime.of(2026, 8, 1, 10, 0));

            // IMMATRICOLAZIONE STUDENTI (32 Studenti >= 30)
            String[][] studentiData = {
                {"Mario", "Rossi", "mario.rossi@studenti.it", "pass123", "Ingegneria Informatica", "CODICEFISCALEMARIOROSSI"},
                {"Luigi", "Verdi", "luigi.verdi@studenti.it", "pass123", "Ingegneria Informatica", "CODICEFISCALELUIGIVERDI"},
                {"Anna", "Bianchi", "anna.bianchi@studenti.it", "pass123", "Ingegneria Informatica", "CODICEFISCALEANNABIANCHI"},
                {"Simo", "Plata", "simo.plata@studenti.it", "pass123", "Ingegneria Informatica", "SIMO"},
                {"Paolo", "Neri", "paolo.neri@studenti.it", "pass123", "Ingegneria Informatica", "CFPAOLONERI001A"},
                {"Giulia", "Ferrari", "giulia.ferrari@studenti.it", "pass123", "Ingegneria Informatica", "CFGIULIAFERRARI02B"},
                {"Davide", "Romano", "davide.romano@studenti.it", "pass123", "Ingegneria Informatica", "CFDAVIDEROMANO03C"},
                {"Federica", "Colombo", "federica.colombo@studenti.it", "pass123", "Ingegneria Informatica", "CFFEDERICOCOLOMBO4D"},
                {"Matteo", "Ricci", "matteo.ricci@studenti.it", "pass123", "Ingegneria Informatica", "CFMATTEORICCI05E"},
                {"Silvia", "Marino", "silvia.marino.st@studenti.it", "pass123", "Ingegneria Informatica", "CFSILVIAMARINO06F"},
                {"Andrea", "Conti", "andrea.conti@studenti.it", "pass123", "Ingegneria Informatica", "CFANDREACONTI07G"},
                {"Chiara", "Greco", "chiara.greco.st@studenti.it", "pass123", "Ingegneria Informatica", "CFCHIARAGRECO08H"},
                {"Marco", "De Luca", "marco.deluca@studenti.it", "pass123", "Ingegneria Informatica", "CFMARCODELUCA09I"},
                {"Sara", "Costa", "sara.costa@studenti.it", "pass123", "Ingegneria Informatica", "CFSARACOSTA10L"},
                {"Luca", "Giordano", "luca.giordano@studenti.it", "pass123", "Ingegneria Informatica", "CFLUCAGIORDANO11M"},
                {"Martina", "Mancini", "martina.mancini@studenti.it", "pass123", "Ingegneria Informatica", "CFMARTINAMANCINI12N"},
                {"Simone", "Rizzo", "simone.rizzo@studenti.it", "pass123", "Ingegneria Informatica", "CFSIMONERIZZO13O"},
                {"Elena", "Lombardi", "elena.lombardi@studenti.it", "pass123", "Ingegneria Informatica", "CFELENALOMBARDI14P"},
                {"Giacomo", "Moretti", "giacomo.moretti@studenti.it", "pass123", "Ingegneria Informatica", "CFGIACOMOMORETTI15Q"},
                {"Alessia", "Barbieri", "alessia.barbieri@studenti.it", "pass123", "Ingegneria Informatica", "CFALESSIABARBIERI16R"},
                {"Lorenzo", "Fontana", "lorenzo.fontana@studenti.it", "pass123", "Ingegneria Informatica", "CFLORENZOFONTANA17S"},
                {"Valentina", "Santoro", "valentina.santoro@studenti.it", "pass123", "Ingegneria Informatica", "CFVALENTINASANTORO18T"},
                {"Tommaso", "Mariani", "tommaso.mariani@studenti.it", "pass123", "Ingegneria Informatica", "CFTOMMASOMARIANI19U"},
                {"Elisa", "Rinaldi", "elisa.rinaldi@studenti.it", "pass123", "Ingegneria Informatica", "CFELISARINALDI20V"},
                {"Filippo", "Caruso", "filippo.caruso@studenti.it", "pass123", "Ingegneria Informatica", "CFFILIPPOCARUSO21Z"},
                {"Beatrice", "Ferraro", "beatrice.ferraro@studenti.it", "pass123", "Ingegneria Informatica", "CFBEATRICEFERRARO22A"},
                {"Gabriele", "Galli", "gabriele.galli@studenti.it", "pass123", "Ingegneria Informatica", "CFGABRIELEGALLI23B"},
                {"Camilla", "Martini", "camilla.martini@studenti.it", "pass123", "Ingegneria Informatica", "CFCAMILLAMARTINI24C"},
                {"Christian", "Leone", "christian.leone@studenti.it", "pass123", "Ingegneria Informatica", "CFCHRISTIANLEONE25D"},
                {"Alice", "Longo", "alice.longo@studenti.it", "pass123", "Ingegneria Informatica", "CFALICELONGO26E"},
                {"Edoardo", "Gentile", "edoardo.gentile@studenti.it", "pass123", "Ingegneria Informatica", "CFEDOARDOGENTILE27F"},
                {"Giorgia", "Martinelli", "giorgia.martinelli@studenti.it", "pass123", "Ingegneria Informatica", "CFGIORGIAMARTINELLI28G"}
            };

            List<Studente> studentiList = new ArrayList<>();
            for (String[] stData : studentiData) {
                Studente st = unicenter.immatricolaStudente(stData[0], stData[1], stData[2], stData[3], stData[4], stData[5]);
                // Tasse pagate per tutti tranne gli ultimi due per testare i validatori tasse
                boolean pagaTasse = !stData[0].equals("Edoardo") && !stData[0].equals("Giorgia");
                st.setTassePagate(pagaTasse);
                studentiList.add(st);
            }

            // =========================================================================
            // SIMULAZIONE TEMPORALE: FASE 2 - CREAZIONE APPELLI (2 Agosto 2026)
            // =========================================================================
            console.mostraMessaggio("[DB POPULATION] Simulazione temporale: Creazione appelli futuri d'esame (Agosto 2026)...");
            ClockProvider.setFixedDateTime(LocalDateTime.of(2026, 8, 2, 9, 0));

            // CREAZIONE APPELLI (32 Appelli con appelli per il 15 Agosto e date successive)
            Object[][] appelliData = {
                {"IS01", LocalDateTime.of(2026, 8, 10, 9, 0), "Aula Magna", 50, "A-Z", LocalDate.of(2026, 8, 8)},
                {"AR01", LocalDateTime.of(2026, 8, 12, 9, 30), "Aula 102", 30, "A-Z", LocalDate.of(2026, 8, 10)},
                {"AM01", LocalDateTime.of(2026, 8, 14, 11, 0), "Aula Magna", 60, "A-Z", LocalDate.of(2026, 8, 12)},
                {"PRG01", LocalDateTime.of(2026, 8, 15, 9, 0), "Lab Inf 1", 40, "A-Z", LocalDate.of(2026, 8, 13)}, // Appello 15 Agosto
                {"BD01", LocalDateTime.of(2026, 8, 15, 14, 30), "Aula 101", 40, "A-Z", LocalDate.of(2026, 8, 13)},  // Appello 15 Agosto
                {"PRG02", LocalDateTime.of(2026, 8, 18, 14, 30), "Lab Inf 2", 35, "A-Z", LocalDate.of(2026, 8, 16)},
                {"AM02", LocalDateTime.of(2026, 8, 22, 9, 0), "Aula 201", 35, "A-Z", LocalDate.of(2026, 8, 20)},
                {"SO01", LocalDateTime.of(2026, 8, 25, 9, 0), "Aula 103", 40, "A-Z", LocalDate.of(2026, 8, 23)},
                {"RET01", LocalDateTime.of(2026, 8, 28, 15, 0), "Aula 104", 30, "A-Z", LocalDate.of(2026, 8, 26)},
                {"SIC01", LocalDateTime.of(2026, 9, 2, 9, 0), "Aula Magna", 30, "A-Z", LocalDate.of(2026, 8, 30)},
                {"IA01", LocalDateTime.of(2026, 9, 5, 11, 0), "Aula 202", 25, "A-Z", LocalDate.of(2026, 9, 3)},
                {"ML01", LocalDateTime.of(2026, 9, 8, 9, 0), "Aula 203", 25, "A-Z", LocalDate.of(2026, 9, 6)},
                {"ASD01", LocalDateTime.of(2026, 9, 11, 10, 30), "Aula 105", 40, "A-Z", LocalDate.of(2026, 9, 9)},
                {"ALG01", LocalDateTime.of(2026, 9, 14, 9, 0), "Aula 106", 50, "A-Z", LocalDate.of(2026, 9, 12)},
                {"FIS01", LocalDateTime.of(2026, 9, 16, 14, 0), "Aula Magna", 60, "A-Z", LocalDate.of(2026, 9, 14)},
                {"FIS02", LocalDateTime.of(2026, 9, 18, 9, 0), "Aula 107", 35, "A-Z", LocalDate.of(2026, 9, 16)},
                {"STAT01", LocalDateTime.of(2026, 9, 20, 10, 0), "Aula 204", 40, "A-Z", LocalDate.of(2026, 9, 18)},
                {"WEB01", LocalDateTime.of(2026, 9, 22, 15, 0), "Lab Inf 3", 30, "A-Z", LocalDate.of(2026, 9, 20)},
                {"CLOUD01", LocalDateTime.of(2026, 9, 25, 9, 0), "Aula 205", 25, "A-Z", LocalDate.of(2026, 9, 23)},
                {"IOT01", LocalDateTime.of(2026, 9, 28, 11, 30), "Lab Inf 4", 25, "A-Z", LocalDate.of(2026, 9, 26)},
                {"ROB01", LocalDateTime.of(2026, 10, 2, 9, 0), "Aula Magna", 30, "A-Z", LocalDate.of(2026, 9, 30)},
                {"ELE01", LocalDateTime.of(2026, 10, 5, 14, 0), "Aula 108", 35, "A-Z", LocalDate.of(2026, 10, 3)},
                {"ELN01", LocalDateTime.of(2026, 10, 8, 9, 0), "Aula 109", 30, "A-Z", LocalDate.of(2026, 10, 6)},
                {"TD01", LocalDateTime.of(2026, 10, 10, 10, 0), "Aula 110", 30, "A-Z", LocalDate.of(2026, 10, 8)},
                {"GES01", LocalDateTime.of(2026, 10, 12, 15, 0), "Aula 206", 40, "A-Z", LocalDate.of(2026, 10, 10)},
                {"FIN01", LocalDateTime.of(2026, 10, 15, 9, 0), "Aula 207", 35, "A-Z", LocalDate.of(2026, 10, 13)},
                {"ECO01", LocalDateTime.of(2026, 10, 18, 11, 0), "Aula 208", 40, "A-Z", LocalDate.of(2026, 10, 16)},
                {"DIR01", LocalDateTime.of(2026, 10, 20, 9, 0), "Aula Magna", 50, "A-Z", LocalDate.of(2026, 10, 18)},
                {"DIR02", LocalDateTime.of(2026, 10, 22, 14, 30), "Aula 111", 45, "A-Z", LocalDate.of(2026, 10, 20)},
                {"BIO01", LocalDateTime.of(2026, 10, 25, 9, 0), "Aula 112", 30, "A-Z", LocalDate.of(2026, 10, 23)},
                {"CHM01", LocalDateTime.of(2026, 10, 28, 10, 0), "Lab Chimica", 30, "A-Z", LocalDate.of(2026, 10, 26)},
                {"MAT01", LocalDateTime.of(2026, 10, 30, 9, 0), "Aula 113", 35, "A-Z", LocalDate.of(2026, 10, 28)}
            };

            for (Object[] apRow : appelliData) {
                gestioneAppelli.creaNuovoAppello(
                    (String) apRow[0],
                    (LocalDateTime) apRow[1],
                    (String) apRow[2],
                    (Integer) apRow[3],
                    (String) apRow[4],
                    (LocalDate) apRow[5]
                );
            }

            // =========================================================================
            // SIMULAZIONE TEMPORALE: FASE 3 - ISCRIZIONE STUDENTI AGLI APPELLI (5 Agosto 2026)
            // =========================================================================
            console.mostraMessaggio("[DB POPULATION] Simulazione temporale: Iscrizioni studenti agli appelli (Agosto 2026)...");
            ClockProvider.setFixedDateTime(LocalDateTime.of(2026, 8, 5, 12, 0));

            List<Appello> appelliIS01 = gestioneAppelli.trovaAppelliByIdMateria(List.of("IS01"));
            String codAppelloIS01 = !appelliIS01.isEmpty() ? appelliIS01.get(0).getCodiceAppello() : "APP-00001";

            List<Appello> appelliAR01 = gestioneAppelli.trovaAppelliByIdMateria(List.of("AR01"));
            String codAppelloAR01 = !appelliAR01.isEmpty() ? appelliAR01.get(0).getCodiceAppello() : "APP-00002";

            List<Appello> appelliAM01 = gestioneAppelli.trovaAppelliByIdMateria(List.of("AM01"));
            String codAppelloAM01 = !appelliAM01.isEmpty() ? appelliAM01.get(0).getCodiceAppello() : "APP-00003";

            List<Appello> appelliPRG01 = gestioneAppelli.trovaAppelliByIdMateria(List.of("PRG01"));
            String codAppelloPRG01 = !appelliPRG01.isEmpty() ? appelliPRG01.get(0).getCodiceAppello() : "APP-00004";

            List<Appello> appelliBD01 = gestioneAppelli.trovaAppelliByIdMateria(List.of("BD01"));
            String codAppelloBD01 = !appelliBD01.isEmpty() ? appelliBD01.get(0).getCodiceAppello() : "APP-00005";

            // Iscrizione studenti all'appello IS01
            for (int i = 0; i < 20; i++) {
                Studente st = studentiList.get(i);
                if (st.isTassePagate()) {
                    gestioneAppelli.iscriviStudente(st, codAppelloIS01);
                }
            }

            // Iscrizione Mario Rossi (index 0) e altri studenti agli appelli del 15 Agosto (PRG01 e BD01) e altri
            for (int i = 0; i < 20; i++) {
                Studente st = studentiList.get(i);
                if (st.isTassePagate()) {
                    gestioneAppelli.iscriviStudente(st, codAppelloPRG01);
                    gestioneAppelli.iscriviStudente(st, codAppelloBD01);
                    gestioneAppelli.iscriviStudente(st, codAppelloAR01);
                    gestioneAppelli.iscriviStudente(st, codAppelloAM01);
                }
            }

            // =========================================================================
            // SIMULAZIONE TEMPORALE: FASE 4 - SVOLGIMENTO ESAMI E PUBBLICAZIONE ESITI
            // =========================================================================
            console.mostraMessaggio("[DB POPULATION] Simulazione temporale: Svolgimento esami e pubblicazione esiti...");

            // 10 Agosto 2026: Giorno appello IS01
            ClockProvider.setFixedDateTime(LocalDateTime.of(2026, 8, 10, 18, 0));

            // Pubblicazione esiti per IS01 (Prof. Rossi - ID: "1") (Oltre 30 esiti complessivi)
            int[] votiIS = {28, 15, 30, 24, 27, 14, 30, 26, 22, 17, 29, 25, 23, 16, 28, 30, 24, 18, 20, 26};
            boolean[] lodiIS = {false, false, true, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false};

            List<EsameSostenuto> esamiPubblicatiList = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                Studente st = studentiList.get(i);
                if (st.isTassePagate()) {
                    EsameSostenuto esm = gestioneVoto.pubblicaEsito(
                        codAppelloIS01,
                        st.getMatricola(),
                        "IS01",
                        "1",
                        votiIS[i],
                        lodiIS[i],
                        7
                    );
                    esamiPubblicatiList.add(esm);
                }
            }

            // 15 Agosto 2026: Giorno appello PRG01 / BD01
            // Per questi appelli del 15 Agosto, Mario Rossi (index 0) è regolarmente iscritto ma NON ha ancora
            // un esito registrato, così che il docente possa inserirlo direttamente da interfaccia grafica!
            ClockProvider.setFixedDateTime(LocalDateTime.of(2026, 8, 15, 18, 0));

            // Pubblichiamo gli esiti solo per alcuni studenti (dal 5 al 19), lasciando Mario Rossi (index 0) disponibile per l'inserimento
            int[] votiPRG = {30, 28, 25, 14, 27, 22, 30, 15, 26, 24, 29, 18, 21, 23, 27};
            for (int i = 5; i < 20; i++) {
                Studente st = studentiList.get(i);
                if (st.isTassePagate()) {
                    EsameSostenuto esm = gestioneVoto.pubblicaEsito(
                        codAppelloPRG01,
                        st.getMatricola(),
                        "PRG01",
                        "1",
                        votiPRG[i - 5],
                        (votiPRG[i - 5] == 30 && (i % 2 == 0)),
                        7
                    );
                    esamiPubblicatiList.add(esm);
                }
            }

            // =========================================================================
            // SIMULAZIONE TEMPORALE: FASE 5 - ACCETTAZIONE / RIFIUTO VOTI E LIBRETTO
            // =========================================================================
            console.mostraMessaggio("[DB POPULATION] Simulazione temporale: Registrazione scelte studenti nel libretto...");
            ClockProvider.setFixedDateTime(LocalDateTime.of(2026, 8, 16, 11, 0));

            // Alcuni studenti accettano il voto (verbalizzato nel libretto), altri lo rifiutano, altri rimangono pendenti
            for (int i = 0; i < esamiPubblicatiList.size(); i++) {
                EsameSostenuto esame = esamiPubblicatiList.get(i);
                if ("In attesa di conferma".equals(esame.getNomeStato())) {
                    if (i % 3 == 0) {
                        // Accetta voto -> verbalizzato in Libretto
                        gestioneVoto.accettaVoto(esame.getIdEsame());
                    } else if (i % 5 == 0) {
                        // Rifiuta voto
                        gestioneVoto.rifiutaVoto(esame.getIdEsame());
                    }
                    // Gli altri rimangono in "In attesa di conferma" per testare la UI!
                }
            }

            console.mostraMessaggio("[DB POPULATION] Popolamento completato con successo!");
            console.mostraMessaggio("[DB POPULATION] Totale Materie: " + gestoreMaterie.getTutteLeMaterie().size());
            console.mostraMessaggio("[DB POPULATION] Totale Corsi di Laurea: " + gestioneCorsi.getTuttiCorsi().size());
            console.mostraMessaggio("[DB POPULATION] Totale Studenti Immatricolati: " + unicenter.getStudentiIscritti().size());
            console.mostraMessaggio("[DB POPULATION] Totale Appelli Creati: " + gestioneAppelli.trovaAppelliByIdMateria(List.of("IS01", "BD01", "AR01", "AM01", "PRG01")).size() + "+");
            console.mostraMessaggio("[DB POPULATION] Totale Esiti Pubblicati: " + gestioneVoto.getTuttiGliEsiti().size());

        } catch (Exception e) {
            console.mostraMessaggio("[DB POPULATION ERROR] Errore durante il popolamento: " + e.getMessage());
        } finally {
            // Ripristino rigoroso del clock al tempo reale di sistema
            ClockProvider.resetClock();
            console.mostraMessaggio("[DB POPULATION] Clock di sistema ripristinato al tempo reale.");
        }
    }
}
