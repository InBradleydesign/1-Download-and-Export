package com.cuidarapp.health.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import com.cuidarapp.health.model.Author;
import com.cuidarapp.health.model.HealthMeasurement;
import com.cuidarapp.health.model.HealthReminder;
import com.cuidarapp.health.model.MeasurementType;
import com.cuidarapp.health.model.MedicationIntake;
import com.cuidarapp.health.model.SymptomRecord;

import org.junit.Test;

public class HealthValidatorTest {

    private static final long NOW = 1_790_000_000_000L;

    private static HealthMeasurement bp(double sys, Double dia, Double pulse) {
        HealthMeasurement m = new HealthMeasurement();
        m.type = MeasurementType.BLOOD_PRESSURE;
        m.value1 = sys;
        m.value2 = dia;
        m.value3 = pulse;
        m.measuredAt = NOW - 60_000;
        return m;
    }

    private static HealthMeasurement single(String type, double value, String context) {
        HealthMeasurement m = new HealthMeasurement();
        m.type = type;
        m.value1 = value;
        m.context = context;
        m.measuredAt = NOW - 60_000;
        return m;
    }

    private static void expectField(String field, ThrowingRunnable action) {
        try {
            action.run();
            fail("Esperava erro no campo " + field);
        } catch (ValidationException e) {
            assertEquals(field, e.getField());
        }
    }

    interface ThrowingRunnable {
        void run() throws ValidationException;
    }

    @Test
    public void bloodPressure_valid_isRoundedAndDropsContext() throws ValidationException {
        HealthMeasurement m = bp(128.4, 82.6, 71.2);
        m.context = "fasting";
        HealthValidator.validateMeasurement(m, NOW);
        assertEquals(128.0, m.value1, 0);
        assertEquals(83.0, m.value2, 0);
        assertEquals(71.0, m.value3, 0);
        assertNull(m.context);
    }

    @Test
    public void bloodPressure_pulseIsOptional() throws ValidationException {
        HealthValidator.validateMeasurement(bp(120, 80.0, null), NOW);
    }

    @Test
    public void bloodPressure_rejectsTypos() {
        expectField("value1", () -> HealthValidator.validateMeasurement(bp(1200, 80.0, null), NOW));
        expectField("value2", () -> HealthValidator.validateMeasurement(bp(120, null, null), NOW));
        expectField("value2", () -> HealthValidator.validateMeasurement(bp(120, 800.0, null), NOW));
        expectField("value3", () -> HealthValidator.validateMeasurement(bp(120, 80.0, 400.0), NOW));
    }

    @Test
    public void bloodPressure_diastolicMustBeLowerThanSystolic() {
        expectField("value2", () -> HealthValidator.validateMeasurement(bp(80, 120.0, null), NOW));
    }

    @Test
    public void glucose_requiresContext() throws ValidationException {
        expectField("context", () -> HealthValidator.validateMeasurement(single(MeasurementType.GLUCOSE, 110, null), NOW));
        expectField("context", () -> HealthValidator.validateMeasurement(single(MeasurementType.GLUCOSE, 110, "lunch"), NOW));
        HealthValidator.validateMeasurement(single(MeasurementType.GLUCOSE, 110, MeasurementType.CONTEXT_FASTING), NOW);
    }

    @Test
    public void weightAndTemperature_keepOneDecimal() throws ValidationException {
        HealthMeasurement w = single(MeasurementType.WEIGHT, 72.46, null);
        HealthValidator.validateMeasurement(w, NOW);
        assertEquals(72.5, w.value1, 1e-9);
        HealthMeasurement t = single(MeasurementType.TEMPERATURE, 36.84, "fasting");
        HealthValidator.validateMeasurement(t, NOW);
        assertEquals(36.8, t.value1, 1e-9);
        assertNull(t.context);
    }

    @Test
    public void otherIndicators_rangeChecked() {
        expectField("value1", () -> HealthValidator.validateMeasurement(single(MeasurementType.SPO2, 101, null), NOW));
        expectField("value1", () -> HealthValidator.validateMeasurement(single(MeasurementType.HEART_RATE, 5, null), NOW));
        expectField("value1", () -> HealthValidator.validateMeasurement(single(MeasurementType.URINE, 0, null), NOW));
        expectField("value1", () -> HealthValidator.validateMeasurement(single(MeasurementType.TEMPERATURE, Double.NaN, null), NOW));
    }

    @Test
    public void unknownType_rejected() {
        expectField("type", () -> HealthValidator.validateMeasurement(single("cholesterol", 100, null), NOW));
    }

    @Test
    public void timestamp_cannotBeInFutureOrMissing() throws ValidationException {
        HealthMeasurement future = bp(120, 80.0, null);
        future.measuredAt = NOW + HealthValidator.FUTURE_TOLERANCE_MS + 1;
        expectField("measuredAt", () -> HealthValidator.validateMeasurement(future, NOW));

        HealthMeasurement missing = bp(120, 80.0, null);
        missing.measuredAt = 0;
        expectField("measuredAt", () -> HealthValidator.validateMeasurement(missing, NOW));

        HealthMeasurement slightlyAhead = bp(120, 80.0, null);
        slightlyAhead.measuredAt = NOW + 60_000;
        HealthValidator.validateMeasurement(slightlyAhead, NOW);
    }

    @Test
    public void note_limitedTo500() {
        HealthMeasurement m = bp(120, 80.0, null);
        m.note = new String(new char[501]).replace('\0', 'a');
        expectField("note", () -> HealthValidator.validateMeasurement(m, NOW));
    }

    @Test
    public void symptom_rules() throws ValidationException {
        SymptomRecord s = new SymptomRecord();
        s.symptom = "headache";
        s.intensity = 2;
        s.occurredAt = NOW;
        s.customLabel = "ignorado";
        HealthValidator.validateSymptom(s, NOW);
        assertNull(s.customLabel);

        SymptomRecord other = new SymptomRecord();
        other.symptom = SymptomCatalog.OTHER;
        other.intensity = 1;
        other.occurredAt = NOW;
        expectField("customLabel", () -> HealthValidator.validateSymptom(other, NOW));

        SymptomRecord noIntensity = new SymptomRecord();
        noIntensity.symptom = "pain";
        noIntensity.occurredAt = NOW;
        expectField("intensity", () -> HealthValidator.validateSymptom(noIntensity, NOW));

        SymptomRecord badDuration = new SymptomRecord();
        badDuration.symptom = "pain";
        badDuration.intensity = 1;
        badDuration.occurredAt = NOW;
        badDuration.durationMinutes = 0;
        expectField("durationMinutes", () -> HealthValidator.validateSymptom(badDuration, NOW));

        SymptomRecord unknown = new SymptomRecord();
        unknown.symptom = "xyz";
        expectField("symptom", () -> HealthValidator.validateSymptom(unknown, NOW));
    }

    @Test
    public void intake_rules() throws ValidationException {
        MedicationIntake i = new MedicationIntake();
        i.medRef = "17";
        i.medName = "Losartana";
        i.scheduledDate = "2026-10-30";
        i.scheduledTime = "08:00";
        i.status = MedicationIntake.STATUS_TAKEN;
        HealthValidator.validateIntake(i);

        i.status = "maybe";
        expectField("status", () -> HealthValidator.validateIntake(i));
        i.status = MedicationIntake.STATUS_MISSED;
        i.scheduledDate = "30/10/2026";
        expectField("scheduledDate", () -> HealthValidator.validateIntake(i));
        i.scheduledDate = "2026-10-30";
        i.scheduledTime = "25:00";
        expectField("scheduledTime", () -> HealthValidator.validateIntake(i));
    }

    @Test
    public void reminder_requiresProfessionalTimesAndDays() throws ValidationException {
        HealthReminder r = new HealthReminder();
        r.kind = HealthReminder.KIND_MEASUREMENT;
        r.title = "Pressão arterial";
        expectField("times", () -> HealthValidator.validateReminder(r));
        r.times = "08:00,7:5";
        expectField("times", () -> HealthValidator.validateReminder(r));
        r.times = "08:00,20:00";
        expectField("daysMask", () -> HealthValidator.validateReminder(r));
        r.daysMask = 0xFF;
        r.onceAt = 123L;
        HealthValidator.validateReminder(r);
        assertEquals(0x7F, r.daysMask);
        assertNull(r.onceAt);
    }

    @Test
    public void appointmentReminder_requiresDate() throws ValidationException {
        HealthReminder r = new HealthReminder();
        r.kind = HealthReminder.KIND_APPOINTMENT;
        r.title = "Cardiologista";
        expectField("onceAt", () -> HealthValidator.validateReminder(r));
        r.onceAt = NOW;
        HealthValidator.validateReminder(r);
    }

    @Test
    public void author_mustHaveKnownRole() throws ValidationException {
        expectField("author", () -> HealthValidator.validateAuthor(null));
        expectField("author", () -> HealthValidator.validateAuthor(new Author("admin", "x")));
        HealthValidator.validateAuthor(new Author(Author.ROLE_CAREGIVER, "Ana"));
    }
}
