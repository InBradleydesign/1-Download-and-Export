package com.cuidarapp.health.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Cálculo do próximo disparo de um lembrete, independente do Android para poder ser testado. */
public final class ReminderMath {

    private ReminderMath() {
    }

    public static List<LocalTime> parseTimes(String csv) {
        List<LocalTime> times = new ArrayList<>();
        if (csv == null) return times;
        for (String part : csv.split(",")) {
            String t = part.trim();
            if (HealthValidator.isValidTime(t)) times.add(LocalTime.parse(t));
        }
        Collections.sort(times);
        return times;
    }

    /** Bit do dia na máscara: domingo = 0 ... sábado = 6. */
    public static int dayBit(LocalDate date) {
        return 1 << (date.getDayOfWeek().getValue() % 7);
    }

    /**
     * Próximo instante (epoch ms) estritamente depois de {@code fromMillis}, ou null se não houver.
     * Usa o fuso informado, então mudanças de fuso/horário de verão são respeitadas ao reagendar.
     */
    public static Long nextTrigger(String timesCsv, int daysMask, Long onceAt, long fromMillis, ZoneId zone) {
        if (onceAt != null) {
            return onceAt > fromMillis ? onceAt : null;
        }
        List<LocalTime> times = parseTimes(timesCsv);
        if (times.isEmpty() || (daysMask & 0x7F) == 0) return null;

        LocalDate start = Instant.ofEpochMilli(fromMillis).atZone(zone).toLocalDate();
        for (int offset = 0; offset <= 7; offset++) {
            LocalDate date = start.plusDays(offset);
            if ((daysMask & dayBit(date)) == 0) continue;
            for (LocalTime time : times) {
                long candidate = ZonedDateTime.of(date, time, zone).toInstant().toEpochMilli();
                if (candidate > fromMillis) return candidate;
            }
        }
        return null;
    }
}
