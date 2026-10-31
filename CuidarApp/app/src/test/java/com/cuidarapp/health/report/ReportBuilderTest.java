package com.cuidarapp.health.report;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.cuidarapp.health.ProfileData;
import com.cuidarapp.health.model.Author;
import com.cuidarapp.health.model.HealthMeasurement;
import com.cuidarapp.health.model.MeasurementType;
import com.cuidarapp.health.model.MedicationIntake;
import com.cuidarapp.health.model.SymptomRecord;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;

public class ReportBuilderTest {

    private static final ZoneId SP = ZoneId.of("America/Sao_Paulo");

    private static long at(String isoLocal) {
        return LocalDateTime.parse(isoLocal).atZone(SP).toInstant().toEpochMilli();
    }

    private static HealthMeasurement bp(String when, double sys, double dia, Double pulse, String role) {
        HealthMeasurement m = new HealthMeasurement();
        m.type = MeasurementType.BLOOD_PRESSURE;
        m.value1 = sys;
        m.value2 = dia;
        m.value3 = pulse;
        m.measuredAt = at(when);
        m.createdByRole = role;
        m.createdByName = role.equals(Author.ROLE_CAREGIVER) ? "Ana" : "José";
        return m;
    }

    private static HealthMeasurement glucose(String when, double value, String context) {
        HealthMeasurement m = new HealthMeasurement();
        m.type = MeasurementType.GLUCOSE;
        m.value1 = value;
        m.context = context;
        m.measuredAt = at(when);
        m.createdByRole = Author.ROLE_PATIENT;
        return m;
    }

    @Test
    public void build_withRealRecords() {
        ProfileData profile = new ProfileData();
        profile.conditions = Arrays.asList("hypertension", "diabetes");
        profile.doctorNotes = "Medir sentado";

        SymptomRecord s = new SymptomRecord();
        s.symptom = "headache";
        s.intensity = 2;
        s.durationMinutes = 90;
        s.occurredAt = at("2026-10-29T15:00");
        s.createdByRole = Author.ROLE_PATIENT;
        s.note = "depois do almoço";

        MedicationIntake late = new MedicationIntake();
        late.medRef = "1";
        late.medName = "Losartana";
        late.dosage = "50 mg";
        late.scheduledDate = "2026-10-29";
        late.scheduledTime = "08:00";
        late.status = MedicationIntake.STATUS_TAKEN;
        late.takenAt = at("2026-10-29T10:15");
        late.createdByRole = Author.ROLE_NOTIFICATION;

        MedicationIntake missed = new MedicationIntake();
        missed.medRef = "1";
        missed.medName = "Losartana";
        missed.scheduledDate = "2026-10-28";
        missed.scheduledTime = "08:00";
        missed.status = MedicationIntake.STATUS_MISSED;
        missed.createdByRole = Author.ROLE_CAREGIVER;
        missed.createdByName = "Ana";

        ReportData data = new ReportBuilder(SP).build(" José da Silva ", profile,
            at("2026-10-01T00:00"), at("2026-10-30T23:59"), at("2026-10-30T18:30"),
            Arrays.asList(
                bp("2026-10-29T08:00", 130, 85, 70.0, Author.ROLE_CAREGIVER),
                glucose("2026-10-28T07:00", 98, MeasurementType.CONTEXT_FASTING),
                bp("2026-10-27T08:00", 125, 80, null, Author.ROLE_PATIENT),
                bp("2026-10-28T08:00", 140, 90, null, Author.ROLE_PATIENT)),
            Collections.singletonList(s),
            Arrays.asList(late, missed));

        assertEquals("José da Silva", data.patientName);
        assertEquals("01/10/2026 a 30/10/2026", data.periodText);
        assertEquals("30/10/2026 18:30", data.issuedText);
        assertEquals("Hipertensão, Diabetes", data.conditionsText);
        assertEquals(7, data.totalRecords());

        assertEquals(2, data.indicators.size());
        ReportData.IndicatorSection pressure = data.indicators.get(0);
        assertEquals(MeasurementType.BLOOD_PRESSURE, pressure.type);
        assertEquals("mmHg", pressure.unit);
        assertEquals(2, pressure.series.size());
        assertEquals(Arrays.asList(125.0, 140.0, 130.0), pressure.series.get(0).values);
        assertEquals(Arrays.asList(80.0, 90.0, 85.0), pressure.series.get(1).values);
        assertEquals("27/10/2026 08:00", pressure.rows.get(0)[0]);
        assertEquals("130 x 85 mmHg • pulso 70 bpm", pressure.rows.get(2)[1]);
        assertEquals("Ana (cuidador)", pressure.rows.get(2)[3]);
        assertTrue(pressure.statsLines.get(0), pressure.statsLines.get(0).contains("3 registros; média 131,7 mmHg"));
        assertTrue(pressure.statsLines.get(2).contains("Pulso: 1 registro (média calculada a partir de 3"));

        ReportData.IndicatorSection glucose = data.indicators.get(1);
        assertEquals("Em jejum", glucose.rows.get(0)[2]);
        assertTrue(glucose.statsLines.get(0).startsWith("Em jejum: 1 registro"));

        assertEquals("Dor de cabeça", data.symptomRows.get(0)[1]);
        assertEquals("1 h 30 min", data.symptomRows.get(0)[3]);
        assertEquals("depois do almoço", data.symptomRows.get(0)[5]);

        assertEquals("28/10/2026", data.intakeRows.get(0)[0]);
        assertEquals("Não tomado", data.intakeRows.get(0)[4]);
        assertEquals("Ana (cuidador)", data.intakeRows.get(0)[5]);
        assertEquals("Tomado com atraso (confirmado às 10:15)", data.intakeRows.get(1)[4]);
        assertEquals("notificação", data.intakeRows.get(1)[5]);
    }

    @Test
    public void build_emptyPeriod() {
        ReportData data = new ReportBuilder(SP).build("", null, at("2026-10-01T00:00"), at("2026-10-07T23:59"),
            at("2026-10-08T10:00"), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertEquals("Não informado", data.patientName);
        assertEquals(0, data.totalRecords());
        assertTrue(data.indicators.isEmpty());
    }

    @Test
    public void valueText_usesUnitsAndDecimals() {
        HealthMeasurement w = new HealthMeasurement();
        w.type = MeasurementType.WEIGHT;
        w.value1 = 72.5;
        assertEquals("72,5 kg", ReportBuilder.valueText(w));
        HealthMeasurement spo2 = new HealthMeasurement();
        spo2.type = MeasurementType.SPO2;
        spo2.value1 = 97;
        assertEquals("97 %", ReportBuilder.valueText(spo2));
    }
}
