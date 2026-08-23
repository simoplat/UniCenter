# 🎓 UniCenter

> **Progetto universitario di Ingegneria del Software** > Un ecosistema digitale integrato per la gestione della carriera accademica, progettato per connettere Studenti, Docenti e Amministratori.

---

## 📝 Descrizione del Progetto
Il progetto **UniCenter** nasce con l'obiettivo di fornire uno strumento flessibile ed efficace per digitalizzare i processi della comunità accademica. La piattaforma centralizza la gestione della didattica, delle iscrizioni e della carriera universitaria in un unico ambiente software robusto e scalabile.



---

## 🚀 Funzionalità Implementate (Casi d'Uso)

Il sistema è stato progettato seguendo i requisiti funzionali emersi in fase di analisi, suddivisi per aree di competenza:

### 👨‍🎓 Area Studente
* **`UC2` Iscriversi ad un Appello** Sistema di prenotazione esami con controllo automatico delle propedeuticità e verifica dello stato dei pagamenti (tasse).
* **`UC3` Accettare/Rifiutare Voto** Workflow per la gestione degli esiti dopo la pubblicazione da parte dei docenti.
* **`UC8/UC9` Carriera e Piano di Studi** Procedure di immatricolazione ai Corsi di Laurea e compilazione assistita del Piano di Studi con validazione dei crediti.
* **`UC10` Materiale Didattico** Accesso centralizzato alla documentazione e download delle risorse caricate dai docenti.

### 👨‍🏫 Area Docente
* **`UC1` Gestione Appelli** Ciclo di vita completo delle date d'esame: inserimento, aggiornamento e cancellazione, con gestione dei vincoli di iscrizione.
* **`UC6/UC7` Didattica e Comunicazioni** Strumenti per il caricamento del materiale didattico e invio di avvisi diretti agli studenti iscritti ai propri corsi.

### 🏛️ Area Amministrativa
* **`UC4/UC5` Offerta Formativa** Configurazione strutturale dell'ateneo: creazione e aggiornamento dei Corsi di Laurea e del catalogo delle Materie.

---

## 🛠 Requisiti di Sistema

Per compilare ed eseguire **UniCenter** sono necessari i seguenti prerequisiti software:

* **Java Development Kit (JDK)**: Versione **21** o superiore (supporta anche JDK 25+ / 26).
* **Apache Maven**: Versione **3.8+** per la gestione del ciclo di vita del software, risoluzione delle dipendenze e testing automatico.
* **Browser Web**: Qualsiasi browser moderno (Google Chrome, Mozilla Firefox, Apple Safari, Microsoft Edge) con JavaScript abilitato.

---

## ⚡ Installazione e Avvio Rapido

### 1. Clonazione del Repository
```bash
git clone https://github.com/simoplat/UniCenter.git
cd UniCenter/unicenter
```

### 2. Compilazione del Progetto
```bash
mvn clean compile
```

### 3. Esecuzione della Suite di Test Unitari e di Integrazione
Il progetto include una suite completa di oltre 280 test automatizzati:
```bash
mvn test
```

### 4. Avvio dell'Applicazione
Per avviare il sistema e il relativo server web integrato:
```bash
mvn exec:java -Dexec.mainClass="it.project.Main"
```

Una volta avviato, la console mostrerà la conferma di inizializzazione e sarà possibile accedere al portale web all'indirizzo:
👉 **[http://localhost:8080](http://localhost:8080)**

---

## 🏗️ Architettura e Design Patterns

Il progetto segue rigorosamente i principi di **Object-Oriented Design** e i pattern architetturali **GRASP** e **GoF**:

* **Chain of Responsibility**: validazione a catena per l'iscrizione agli appelli (controllo tasse, propedeuticità, fascia cognome, posti disponibili, scadenza).
* **State Pattern**: gestione del ciclo di vita dei voti e verbali d'esame (`InAttesaConferma`, `Approvato`, `Rifiutato`, `Bocciato`).
* **Strategy Pattern**: algoritmo di calcolo delle tasse universitarie e maggiorazioni fuori corso (`ICalcoloTasseStrategy`).
* **Observer Pattern**: sistema asincrono di notifiche ed eventi per studenti e docenti (`ObserverNotifica`).
* **Composite Pattern**: gestione gerarchica ad albero di cartelle e file del materiale didattico (`ElementoMateriale`, `Cartella`, `FileDidattico`).
* **Builder & Factory**: creazione controllata di carriere/studenti (`StudenteBuilder`) e corsi di laurea (`CorsoDiLaureaFactory`).
* **Pure Fabrication**: `ClockProvider` per la simulazione temporale e test manuali delle finestre di immatricolazione, rinnovo ed esami.

---

## 👥 Autori
* **Simone Platania**
* **Alessandra Scilio**
* **Alberto Calabrese**
