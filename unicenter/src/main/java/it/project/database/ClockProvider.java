package it.project.database;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Pattern: Pure Fabrication
 * Fornisce un orologio globale configurabile per simulare l'avanzamento temporale
 * durante il popolamento dati, i test e l'esecuzione dell'applicazione,
 * consentendo di testare e rispettare rigorosamente i vincoli temporali di dominio.
 */
public class ClockProvider {
    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();
    private static Clock clock = null;

    private ClockProvider() {}

    /**
     * Restituisce l'istanza corrente di Clock o null se usa il tempo reale.
     *
     * @return Clock configurato o null
     */
    public static Clock getClock() {
        return clock;
    }

    /**
     * Imposta un nuovo Clock custom.
     *
     * @param newClock nuova istanza di Clock
     */
    public static void setClock(Clock newClock) {
        clock = newClock;
    }

    /**
     * Fissa l'orologio su una data e ora specifica.
     *
     * @param dateTime data e ora fissa
     */
    public static void setFixedDateTime(LocalDateTime dateTime) {
        clock = Clock.fixed(dateTime.atZone(DEFAULT_ZONE).toInstant(), DEFAULT_ZONE);
    }

    /**
     * Fissa l'orologio a mezzanotte di una data specifica.
     *
     * @param date data fissa
     */
    public static void setFixedDate(LocalDate date) {
        clock = Clock.fixed(date.atStartOfDay(DEFAULT_ZONE).toInstant(), DEFAULT_ZONE);
    }

    /**
     * Resetta l'orologio al tempo reale di sistema.
     */
    public static void resetClock() {
        clock = null;
    }

    /**
     * Restituisce la data corrente (simulata o reale).
     *
     * @return LocalDate corrente
     */
    public static LocalDate nowLocalDate() {
        return (clock != null) ? LocalDate.now(clock) : LocalDate.now();
    }

    /**
     * Restituisce la data e ora corrente (simulata o reale).
     *
     * @return LocalDateTime corrente
     */
    public static LocalDateTime nowLocalDateTime() {
        return (clock != null) ? LocalDateTime.now(clock) : LocalDateTime.now();
    }
}
