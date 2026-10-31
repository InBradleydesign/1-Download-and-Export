package com.cuidarapp.health.domain;

import com.cuidarapp.health.model.MedicationIntake;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Situação de uma dose para exibição: tomado, tomado com atraso ou não tomado.
 * "Com atraso" significa apenas que a confirmação foi feita mais de {@link #LATE_TOLERANCE_MINUTES}
 * minutos depois do horário previsto; não é uma avaliação clínica.
 */
public final class IntakeStatus {

    public static final int LATE_TOLERANCE_MINUTES = 60;

    public static final String TAKEN = "taken";
    public static final String LATE = "late";
    public static final String MISSED = "missed";

    private IntakeStatus() {
    }

    public static String of(MedicationIntake intake, ZoneId zone) {
        if (MedicationIntake.STATUS_MISSED.equals(intake.status)) return MISSED;
        if (intake.takenAt == null || intake.scheduledTime == null || !HealthValidator.isValidTime(intake.scheduledTime)) {
            return TAKEN;
        }
        long scheduled = LocalDate.parse(intake.scheduledDate).atTime(LocalTime.parse(intake.scheduledTime))
            .atZone(zone).toInstant().toEpochMilli();
        return intake.takenAt > scheduled + LATE_TOLERANCE_MINUTES * 60_000L ? LATE : TAKEN;
    }

    public static String label(String status) {
        switch (status) {
            case LATE: return "Tomado com atraso";
            case MISSED: return "Não tomado";
            default: return "Tomado";
        }
    }
}
