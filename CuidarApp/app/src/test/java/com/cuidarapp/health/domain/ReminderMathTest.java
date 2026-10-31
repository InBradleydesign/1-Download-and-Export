package com.cuidarapp.health.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;

public class ReminderMathTest {

    private static final ZoneId SP = ZoneId.of("America/Sao_Paulo");
    private static final int EVERY_DAY = 0x7F;

    private static long at(String isoLocal, ZoneId zone) {
        return LocalDateTime.parse(isoLocal).atZone(zone).toInstant().toEpochMilli();
    }

    @Test
    public void parseTimes_sortsAndSkipsInvalid() {
        assertEquals(Arrays.asList(LocalTime.of(8, 0), LocalTime.of(20, 30)),
            ReminderMath.parseTimes("20:30, 08:00,99:99,"));
    }

    @Test
    public void nextTrigger_sameDayLaterTime() {
        long from = at("2026-10-30T09:00", SP);
        assertEquals(Long.valueOf(at("2026-10-30T20:00", SP)),
            ReminderMath.nextTrigger("08:00,20:00", EVERY_DAY, null, from, SP));
    }

    @Test
    public void nextTrigger_isStrictlyAfterFrom() {
        long from = at("2026-10-30T20:00", SP);
        assertEquals(Long.valueOf(at("2026-10-31T08:00", SP)),
            ReminderMath.nextTrigger("08:00,20:00", EVERY_DAY, null, from, SP));
    }

    @Test
    public void nextTrigger_respectsWeekdays() {
        // 30/10/2026 é sexta-feira; só segunda (bit 1) => 02/11.
        long from = at("2026-10-30T09:00", SP);
        assertEquals(Long.valueOf(at("2026-11-02T08:00", SP)),
            ReminderMath.nextTrigger("08:00", 1 << 1, null, from, SP));
    }

    @Test
    public void nextTrigger_sameWeekdayNextWeek() {
        // Sexta 09:00, lembrete só às sextas 08:00 => próxima sexta.
        long from = at("2026-10-30T09:00", SP);
        assertEquals(Long.valueOf(at("2026-11-06T08:00", SP)),
            ReminderMath.nextTrigger("08:00", 1 << 5, null, from, SP));
    }

    @Test
    public void nextTrigger_usesGivenZone() {
        ZoneId manaus = ZoneId.of("America/Manaus");
        long from = at("2026-10-30T09:00", manaus);
        assertEquals(Long.valueOf(at("2026-10-30T10:00", manaus)),
            ReminderMath.nextTrigger("10:00", EVERY_DAY, null, from, manaus));
    }

    @Test
    public void nextTrigger_handlesDaylightSavingGap() {
        // Em Nova York, 08/03/2026 02:30 não existe; o horário é ajustado para 03:30.
        ZoneId ny = ZoneId.of("America/New_York");
        long from = at("2026-03-08T00:00", ny);
        long expected = at("2026-03-08T03:30", ny);
        assertEquals(Long.valueOf(expected), ReminderMath.nextTrigger("02:30", EVERY_DAY, null, from, ny));
    }

    @Test
    public void nextTrigger_oneShot() {
        long from = at("2026-10-30T09:00", SP);
        long once = at("2026-11-05T14:00", SP);
        assertEquals(Long.valueOf(once), ReminderMath.nextTrigger(null, 0, once, from, SP));
        assertNull(ReminderMath.nextTrigger(null, 0, from - 1, from, SP));
    }

    @Test
    public void nextTrigger_withoutTimesOrDays_isNull() {
        long from = at("2026-10-30T09:00", SP);
        assertNull(ReminderMath.nextTrigger("", EVERY_DAY, null, from, SP));
        assertNull(ReminderMath.nextTrigger("08:00", 0, null, from, SP));
    }
}
