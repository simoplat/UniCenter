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

    public static Clock getClock() {
        return clock;
    }

    public static void setClock(Clock newClock) {
        clock = newClock;
    }

    public static void setFixedDateTime(LocalDateTime dateTime) {
        clock = Clock.fixed(dateTime.atZone(DEFAULT_ZONE).toInstant(), DEFAULT_ZONE);
    }

    public static void setFixedDate(LocalDate date) {
        clock = Clock.fixed(date.atStartOfDay(DEFAULT_ZONE).toInstant(), DEFAULT_ZONE);
    }

    public static void resetClock() {
        clock = null;
    }

    public static LocalDate nowLocalDate() {
        return (clock != null) ? LocalDate.now(clock) : LocalDate.now();
    }

    public static LocalDateTime nowLocalDateTime() {
        return (clock != null) ? LocalDateTime.now(clock) : LocalDateTime.now();
    }
}
