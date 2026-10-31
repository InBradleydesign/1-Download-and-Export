package com.cuidarapp.health.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.cuidarapp.health.model.HealthMeasurement;
import com.cuidarapp.health.model.MeasurementType;
import com.cuidarapp.health.model.MedicationIntake;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Estatísticas, orientações do profissional e situação das doses. */
public class HealthDomainTest {

    private static final ZoneId SP = ZoneId.of("America/Sao_Paulo");

    // ==================== HealthStats ====================

    @Test
    public void stats_belowMinimumSamples_onlyCounts() {
        HealthStats s = HealthStats.of(Arrays.asList(120.0, 130.0));
        assertEquals(2, s.count);
        assertFalse(s.hasSummary());
        assertNull(s.mean);
    }

    @Test
    public void stats_summaryFromThreeSamples() {
        HealthStats s = HealthStats.of(Arrays.asList(120.0, 130.0, null, 140.0, Double.NaN));
        assertEquals(3, s.count);
        assertTrue(s.hasSummary());
        assertEquals(130.0, s.mean, 1e-9);
        assertEquals(120.0, s.min, 0);
        assertEquals(140.0, s.max, 0);
    }

    @Test
    public void stats_emptyOrNull() {
        assertEquals(0, HealthStats.of(null).count);
        assertEquals(0, HealthStats.of(Collections.emptyList()).count);
    }

    // ==================== AlertRule ====================

    private static AlertRule rule(String metric, String op, double threshold, String context, String message) {
        AlertRule r = new AlertRule();
        r.metric = metric;
        r.op = op;
        r.threshold = threshold;
        r.context = context;
        r.message = message;
        return r;
    }

    private static HealthMeasurement bp(double sys, double dia) {
        HealthMeasurement m = new HealthMeasurement();
        m.type = MeasurementType.BLOOD_PRESSURE;
        m.value1 = sys;
        m.value2 = dia;
        return m;
    }

    @Test
    public void noRules_noGuidance() {
        assertTrue(AlertRule.evaluate(Collections.emptyList(), bp(220, 130)).isEmpty());
        assertTrue(AlertRule.evaluate(null, bp(220, 130)).isEmpty());
    }

    @Test
    public void rules_matchOnlyTheirMetric() {
        List<AlertRule> rules = Arrays.asList(
            rule("systolic", AlertRule.OP_AT_LEAST, 160, null, "Ligar para a UBS"),
            rule("diastolic", AlertRule.OP_AT_MOST, 50, null, "Sentar e medir de novo"),
            rule(MeasurementType.GLUCOSE, AlertRule.OP_AT_MOST, 70, null, "Seguir plano de hipoglicemia"));
        assertEquals(Collections.singletonList("Ligar para a UBS"), AlertRule.evaluate(rules, bp(160, 90)));
        assertEquals(Collections.singletonList("Sentar e medir de novo"), AlertRule.evaluate(rules, bp(100, 50)));
        assertTrue(AlertRule.evaluate(rules, bp(159, 51)).isEmpty());
    }

    @Test
    public void glucoseRule_respectsContext() {
        AlertRule fasting = rule(MeasurementType.GLUCOSE, AlertRule.OP_AT_LEAST, 130, MeasurementType.CONTEXT_FASTING, "Anotar e avisar na consulta");
        HealthMeasurement m = new HealthMeasurement();
        m.type = MeasurementType.GLUCOSE;
        m.value1 = 150;
        m.context = MeasurementType.CONTEXT_AFTER_MEAL;
        assertFalse(fasting.matches(m));
        m.context = MeasurementType.CONTEXT_FASTING;
        assertTrue(fasting.matches(m));
    }

    @Test
    public void ruleValidation() throws ValidationException {
        AlertRule ok = rule("pulse", AlertRule.OP_AT_LEAST, 110, "", "  Repouso  ");
        ok.validate();
        assertNull(ok.context);
        assertEquals("Repouso", ok.message);

        String[][] invalid = {
            {"cholesterol", "gte", "100", "x"},
            {"systolic", "eq", "100", "x"},
            {"systolic", "gte", "0", "x"},
            {"systolic", "gte", "100", ""},
        };
        for (String[] c : invalid) {
            try {
                rule(c[0], c[1], Double.parseDouble(c[2]), null, c[3]).validate();
                fail("Regra inválida aceita: " + Arrays.toString(c));
            } catch (ValidationException expected) {
                // ok
            }
        }
    }

    // ==================== IntakeStatus ====================

    private static MedicationIntake intake(String status, String time, String takenAtLocal) {
        MedicationIntake i = new MedicationIntake();
        i.status = status;
        i.scheduledDate = "2026-10-30";
        i.scheduledTime = time;
        i.takenAt = takenAtLocal == null ? null
            : LocalDateTime.parse(takenAtLocal).atZone(SP).toInstant().toEpochMilli();
        return i;
    }

    @Test
    public void intake_takenWithinToleranceIsOnTime() {
        assertEquals(IntakeStatus.TAKEN, IntakeStatus.of(intake("taken", "08:00", "2026-10-30T09:00"), SP));
    }

    @Test
    public void intake_takenAfterToleranceIsLate() {
        assertEquals(IntakeStatus.LATE, IntakeStatus.of(intake("taken", "08:00", "2026-10-30T09:01"), SP));
    }

    @Test
    public void intake_withoutScheduledTimeIsTaken() {
        assertEquals(IntakeStatus.TAKEN, IntakeStatus.of(intake("taken", null, "2026-10-30T23:00"), SP));
    }

    @Test
    public void intake_missed() {
        assertEquals(IntakeStatus.MISSED, IntakeStatus.of(intake("missed", "08:00", null), SP));
        assertEquals("Não tomado", IntakeStatus.label(IntakeStatus.MISSED));
    }

    // ==================== SymptomCatalog ====================

    @Test
    public void warningSymptoms_requireImmediateCare() {
        for (String code : new String[]{"chest_pain", "breath", "confusion", "weakness_side", "fainting"}) {
            assertTrue(code, SymptomCatalog.requiresImmediateCare(code));
        }
        assertFalse(SymptomCatalog.requiresImmediateCare("headache"));
        assertEquals("Formigamento", SymptomCatalog.label(SymptomCatalog.OTHER, "Formigamento"));
    }
}
