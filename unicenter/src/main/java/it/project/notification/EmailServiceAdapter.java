package it.project.notification;

/**
 * Adapter & Indirection: Disaccoppia la logica di dominio dal provider email esterno.
 */
public class EmailServiceAdapter implements INotificaService {

    @Override
    public void inviaNotifica(String destinatarioEmail, String messaggio) {
        // Simulazione invio email tramite client/servizio esterno
        System.out.println("[EMAIL ADAPTER] Inviata email a: " + destinatarioEmail + " | Testo: " + messaggio);
    }
}