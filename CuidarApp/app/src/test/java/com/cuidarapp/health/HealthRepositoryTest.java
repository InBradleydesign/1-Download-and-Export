package com.cuidarapp.health;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.app.Application;
import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.cuidarapp.data.AppDatabase;
import com.cuidarapp.health.domain.AlertRule;
import com.cuidarapp.health.domain.ValidationException;
import com.cuidarapp.health.model.Author;
import com.cuidarapp.health.model.HealthMeasurement;
import com.cuidarapp.health.model.HealthReminder;
import com.cuidarapp.health.model.MeasurementType;
import com.cuidarapp.health.model.MedicationIntake;
import com.cuidarapp.health.model.ReminderEvent;
import com.cuidarapp.health.model.SymptomRecord;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(application = Application.class, sdk = 34)
public class HealthRepositoryTest {

    private static final ZoneId SP = ZoneId.of("America/Sao_Paulo");
    private static final long P = HealthRepository.LOCAL_PATIENT_ID;
    private static final Author PATIENT = new Author(Author.ROLE_PATIENT, "José");
    private static final Author CAREGIVER = new Author(Author.ROLE_CAREGIVER, "Ana");

    private AppDatabase db;
    private HealthRepository repo;
    private long now = at("2026-10-30T12:00");

    private static long at(String isoLocal) {
        return LocalDateTime.parse(isoLocal).atZone(SP).toInstant().toEpochMilli();
    }

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).allowMainThreadQueries().build();
        repo = new HealthRepository(db, () -> now, SP);
    }

    @After
    public void tearDown() {
        db.close();
    }

    private HealthMeasurement bp(double sys, double dia, String when) {
        HealthMeasurement m = new HealthMeasurement();
        m.type = MeasurementType.BLOOD_PRESSURE;
        m.value1 = sys;
        m.value2 = dia;
        m.measuredAt = at(when);
        return m;
    }

    private HealthMeasurement glucose(double value, String context, String when) {
        HealthMeasurement m = new HealthMeasurement();
        m.type = MeasurementType.GLUCOSE;
        m.value1 = value;
        m.context = context;
        m.measuredAt = at(when);
        return m;
    }

    private static String expectError(ThrowingRunnable action) {
        try {
            action.run();
            fail("Esperava ValidationException");
            return null;
        } catch (ValidationException e) {
            return e.getField();
        }
    }

    interface ThrowingRunnable {
        void run() throws ValidationException;
    }

    // ==================== CONSENTIMENTO E PERMISSÕES ====================

    @Test
    public void withoutConsent_nothingIsSaved() {
        assertFalse(repo.getProfile(P).hasConsent());
        assertEquals("consent", expectError(() -> repo.saveMeasurement(P, bp(120, 80, "2026-10-30T08:00"), PATIENT)));
        assertTrue(repo.listMeasurements(P, null, 0, Long.MAX_VALUE).isEmpty());
    }

    @Test
    public void consent_recordsWhoAndWhen() throws Exception {
        ProfileData profile = repo.giveConsent(P, false, PATIENT);
        assertEquals(Long.valueOf(now), profile.consentAt);
        assertEquals("José (idoso)", profile.consentBy);
        assertEquals(Arrays.asList("bp", "glucose"), profile.indicators);
        assertFalse(profile.caregiverMayRecord);
    }

    @Test
    public void caregiver_needsAuthorization() throws Exception {
        repo.giveConsent(P, false, PATIENT);
        assertEquals("permission", expectError(() -> repo.saveMeasurement(P, bp(120, 80, "2026-10-30T08:00"), CAREGIVER)));

        ProfileData input = repo.getProfile(P);
        input.caregiverMayRecord = true;
        repo.saveProfile(P, input, PATIENT);

        HealthMeasurement saved = repo.saveMeasurement(P, bp(120, 80, "2026-10-30T08:00"), CAREGIVER).record;
        assertEquals(Author.ROLE_CAREGIVER, saved.createdByRole);
        assertEquals("Ana", saved.createdByName);
    }

    @Test
    public void profile_validation() throws Exception {
        repo.giveConsent(P, true, PATIENT);
        ProfileData input = repo.getProfile(P);
        input.indicators = Collections.emptyList();
        assertEquals("indicators", expectError(() -> repo.saveProfile(P, input, CAREGIVER)));
        input.indicators = Arrays.asList("bp", "spo2", "bp");
        input.conditions = Arrays.asList("diabetes", "other");
        input.conditionsOther = "  Artrose ";
        input.doctorNotes = "Medir sentado";
        ProfileData saved = repo.saveProfile(P, input, CAREGIVER);
        assertEquals(Arrays.asList("bp", "spo2"), saved.indicators);
        assertEquals("Artrose", saved.conditionsOther);
        assertEquals("Ana (cuidador)", saved.updatedBy);
        input.conditions = Collections.singletonList("cancer_x");
        assertEquals("conditions", expectError(() -> repo.saveProfile(P, input, CAREGIVER)));
    }

    // ==================== MEDIÇÕES: SALVAR, EDITAR, EXCLUIR, HISTÓRICO ====================

    @Test
    public void measurement_saveEditDelete_withAudit() throws Exception {
        repo.giveConsent(P, true, PATIENT);
        HealthMeasurement saved = repo.saveMeasurement(P, bp(130, 85, "2026-10-30T08:00"), PATIENT).record;
        assertTrue(saved.id > 0);
        assertEquals(now, saved.createdAt);
        assertNull(saved.updatedAt);

        now += 60_000;
        HealthMeasurement edit = bp(128, 84, "2026-10-30T08:05");
        edit.id = saved.id;
        edit.note = "  remedi depois de descansar ";
        HealthMeasurement edited = repo.saveMeasurement(P, edit, CAREGIVER).record;
        assertEquals(saved.id, edited.id);
        assertEquals(128.0, edited.value1, 0);
        assertEquals("remedi depois de descansar", edited.note);
        assertEquals(Author.ROLE_PATIENT, edited.createdByRole);
        assertEquals(Author.ROLE_CAREGIVER, edited.updatedByRole);
        assertEquals(Long.valueOf(now), edited.updatedAt);

        List<HealthMeasurement> list = repo.listMeasurements(P, MeasurementType.BLOOD_PRESSURE, at("2026-10-30T00:00"), at("2026-10-30T23:59"));
        assertEquals(1, list.size());
        assertEquals(84.0, list.get(0).value2, 0);

        repo.deleteMeasurement(P, saved.id, PATIENT);
        assertTrue(repo.listMeasurements(P, null, 0, Long.MAX_VALUE).isEmpty());
        assertEquals("id", expectError(() -> repo.deleteMeasurement(P, saved.id, PATIENT)));
    }

    @Test
    public void measurement_cannotChangeType() throws Exception {
        repo.giveConsent(P, true, PATIENT);
        HealthMeasurement saved = repo.saveMeasurement(P, bp(130, 85, "2026-10-30T08:00"), PATIENT).record;
        HealthMeasurement edit = glucose(100, "fasting", "2026-10-30T08:00");
        edit.id = saved.id;
        assertEquals("type", expectError(() -> repo.saveMeasurement(P, edit, PATIENT)));
    }

    @Test
    public void history_filtersByTypeAndPeriod_inOrder() throws Exception {
        repo.giveConsent(P, true, PATIENT);
        repo.saveMeasurement(P, bp(140, 90, "2026-10-29T08:00"), PATIENT);
        repo.saveMeasurement(P, bp(120, 80, "2026-10-20T08:00"), PATIENT);
        repo.saveMeasurement(P, bp(130, 85, "2026-10-28T08:00"), PATIENT);
        repo.saveMeasurement(P, glucose(110, "fasting", "2026-10-29T07:00"), PATIENT);

        List<HealthMeasurement> week = repo.listMeasurements(P, "bp", at("2026-10-24T00:00"), at("2026-10-30T23:59"));
        assertEquals(2, week.size());
        assertEquals(130.0, week.get(0).value1, 0);
        assertEquals(140.0, week.get(1).value1, 0);
        assertEquals(4, repo.listMeasurements(P, null, 0, Long.MAX_VALUE).size());
    }

    @Test
    public void professionalRules_produceGuidance_onlyWhenRegistered() throws Exception {
        repo.giveConsent(P, true, PATIENT);
        assertTrue(repo.saveMeasurement(P, bp(200, 120, "2026-10-30T08:00"), PATIENT).guidance.isEmpty());

        AlertRule rule = new AlertRule();
        rule.metric = "systolic";
        rule.op = AlertRule.OP_AT_LEAST;
        rule.threshold = 180;
        rule.message = "Ligar para a UBS";
        ProfileData profile = repo.getProfile(P);
        profile.alertRules = Collections.singletonList(rule);
        repo.saveProfile(P, profile, CAREGIVER);

        assertEquals(Collections.singletonList("Ligar para a UBS"),
            repo.saveMeasurement(P, bp(185, 100, "2026-10-30T09:00"), PATIENT).guidance);
        assertTrue(repo.saveMeasurement(P, bp(150, 90, "2026-10-30T10:00"), PATIENT).guidance.isEmpty());
    }

    @Test
    public void patientIsolation() throws Exception {
        repo.giveConsent(P, true, PATIENT);
        repo.giveConsent(2, true, PATIENT);
        HealthMeasurement mine = repo.saveMeasurement(P, bp(130, 85, "2026-10-30T08:00"), PATIENT).record;
        repo.saveMeasurement(2, bp(150, 95, "2026-10-30T08:00"), PATIENT);

        assertEquals(1, repo.listMeasurements(P, null, 0, Long.MAX_VALUE).size());
        assertEquals("id", expectError(() -> repo.deleteMeasurement(2, mine.id, PATIENT)));
        HealthMeasurement steal = bp(100, 60, "2026-10-30T08:00");
        steal.id = mine.id;
        assertEquals("id", expectError(() -> repo.saveMeasurement(2, steal, PATIENT)));
        assertEquals(130.0, repo.listMeasurements(P, null, 0, Long.MAX_VALUE).get(0).value1, 0);
    }

    // ==================== SINTOMAS ====================

    @Test
    public void symptom_saveEditAndWarningSigns() throws Exception {
        repo.giveConsent(P, false, PATIENT);
        SymptomRecord s = new SymptomRecord();
        s.symptom = "dizziness";
        s.intensity = 1;
        s.occurredAt = at("2026-10-30T10:00");
        HealthRepository.SymptomResult result = repo.saveSymptom(P, s, PATIENT);
        assertFalse(result.seekCareNow);

        SymptomRecord edit = new SymptomRecord();
        edit.id = result.record.id;
        edit.symptom = "chest_pain";
        edit.intensity = 3;
        edit.durationMinutes = 30;
        edit.occurredAt = at("2026-10-30T10:00");
        HealthRepository.SymptomResult edited = repo.saveSymptom(P, edit, PATIENT);
        assertTrue(edited.seekCareNow);
        assertEquals(Integer.valueOf(30), repo.listSymptoms(P, 0, Long.MAX_VALUE).get(0).durationMinutes);

        repo.deleteSymptom(P, result.record.id, PATIENT);
        assertTrue(repo.listSymptoms(P, 0, Long.MAX_VALUE).isEmpty());
    }

    // ==================== DOSES E LEMBRETES ====================

    private MedicationIntake intake(String status) {
        MedicationIntake i = new MedicationIntake();
        i.medRef = "42";
        i.medName = "Losartana";
        i.dosage = "50 mg";
        i.scheduledDate = "2026-10-30";
        i.scheduledTime = "08:00";
        i.status = status;
        return i;
    }

    private HealthReminder medicationReminder() throws ValidationException {
        HealthReminder r = new HealthReminder();
        r.kind = HealthReminder.KIND_MEDICATION;
        r.refId = "42";
        r.title = "Losartana";
        r.times = "08:00";
        r.daysMask = 0x7F;
        return repo.saveReminder(P, r, CAREGIVER);
    }

    @Test
    public void intake_upsertsPerDay_andResolvesPendingReminder() throws Exception {
        repo.giveConsent(P, true, PATIENT);
        HealthReminder reminder = medicationReminder();
        ReminderEvent event = repo.recordReminderFired(reminder, at("2026-10-30T08:00"));
        assertEquals(ReminderEvent.STATUS_PENDING, event.status);
        assertEquals(event.id, repo.recordReminderFired(reminder, at("2026-10-30T08:00")).id);

        MedicationIntake missed = repo.recordIntake(P, intake(MedicationIntake.STATUS_MISSED), PATIENT);
        assertNull(missed.takenAt);
        assertEquals(ReminderEvent.STATUS_IGNORED, repo.getEvent(event.id).status);

        now = at("2026-10-30T12:00");
        MedicationIntake taken = repo.recordIntake(P, intake(MedicationIntake.STATUS_TAKEN), CAREGIVER);
        assertEquals(missed.id, taken.id);
        assertEquals(Long.valueOf(now), taken.takenAt);
        assertEquals(Author.ROLE_CAREGIVER, taken.updatedByRole);

        List<MedicationIntake> list = repo.listIntakes(P, "2026-10-30", "2026-10-30");
        assertEquals(1, list.size());
        assertEquals(MedicationIntake.STATUS_TAKEN, list.get(0).status);

        repo.deleteIntake(P, "42", "2026-10-30", PATIENT);
        assertTrue(repo.listIntakes(P, "2026-10-01", "2026-10-31").isEmpty());
    }

    @Test
    public void reminderEvent_notResolvedWithoutAction() throws Exception {
        repo.giveConsent(P, true, PATIENT);
        HealthReminder reminder = medicationReminder();
        ReminderEvent event = repo.recordReminderFired(reminder, at("2026-10-30T08:00"));
        assertTrue(repo.listIntakes(P, "2026-10-30", "2026-10-30").isEmpty());
        assertEquals(ReminderEvent.STATUS_PENDING, repo.getEvent(event.id).status);

        repo.resolveEvent(P, event.id, ReminderEvent.STATUS_IGNORED, PATIENT);
        ReminderEvent resolved = repo.getEvent(event.id);
        assertEquals(ReminderEvent.STATUS_IGNORED, resolved.status);
        assertEquals("José (idoso)", resolved.resolvedBy);
        assertEquals("id", expectError(() -> repo.resolveEvent(2, event.id, ReminderEvent.STATUS_DONE, PATIENT)));
    }

    @Test
    public void linkedReminder_isUpsertedByReference() throws Exception {
        repo.giveConsent(P, true, PATIENT);
        HealthReminder first = medicationReminder();
        HealthReminder second = medicationReminder();
        assertEquals(first.id, second.id);
        assertEquals(1, repo.listReminders(P).size());
        assertNotNull(repo.getLinkedReminder(P, HealthReminder.KIND_MEDICATION, "42"));
        assertTrue(repo.deleteReminder(P, first.id));
        assertTrue(repo.listReminders(P).isEmpty());
    }

    @Test
    public void reminder_requiresConsent() {
        HealthReminder r = new HealthReminder();
        r.kind = HealthReminder.KIND_MEASUREMENT;
        r.title = "Pressão";
        r.times = "08:00";
        r.daysMask = 0x7F;
        assertEquals("consent", expectError(() -> repo.saveReminder(P, r, CAREGIVER)));
    }

    // ==================== EXCLUSÃO (LGPD) ====================

    @Test
    public void wipe_removesEverythingOfThePatientOnly() throws Exception {
        repo.giveConsent(P, true, PATIENT);
        repo.giveConsent(2, true, PATIENT);
        repo.saveMeasurement(P, bp(130, 85, "2026-10-30T08:00"), PATIENT);
        repo.saveMeasurement(2, bp(130, 85, "2026-10-30T08:00"), PATIENT);
        HealthReminder reminder = medicationReminder();
        repo.recordReminderFired(reminder, at("2026-10-30T08:00"));
        repo.recordIntake(P, intake(MedicationIntake.STATUS_TAKEN), PATIENT);

        List<Long> cancelled = repo.wipe(P);
        assertEquals(Collections.singletonList(reminder.id), cancelled);
        assertFalse(repo.getProfile(P).hasConsent());
        assertTrue(repo.listMeasurements(P, null, 0, Long.MAX_VALUE).isEmpty());
        assertTrue(repo.listReminders(P).isEmpty());
        assertTrue(repo.listEvents(P, 0, Long.MAX_VALUE).isEmpty());
        assertTrue(repo.listIntakes(P, "2000-01-01", "2100-01-01").isEmpty());
        assertEquals(1, repo.listMeasurements(2, null, 0, Long.MAX_VALUE).size());
    }
}
