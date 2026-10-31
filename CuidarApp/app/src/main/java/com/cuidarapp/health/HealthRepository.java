package com.cuidarapp.health;

import com.cuidarapp.data.AppDatabase;
import com.cuidarapp.health.data.HealthDao;
import com.cuidarapp.health.data.ReminderDao;
import com.cuidarapp.health.domain.AlertRule;
import com.cuidarapp.health.domain.HealthValidator;
import com.cuidarapp.health.domain.SymptomCatalog;
import com.cuidarapp.health.domain.ValidationException;
import com.cuidarapp.health.model.Author;
import com.cuidarapp.health.model.HealthMeasurement;
import com.cuidarapp.health.model.HealthProfile;
import com.cuidarapp.health.model.HealthReminder;
import com.cuidarapp.health.model.MeasurementType;
import com.cuidarapp.health.model.MedicationIntake;
import com.cuidarapp.health.model.ReminderEvent;
import com.cuidarapp.health.model.SymptomRecord;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;

/**
 * Regras de negócio do Diário de Saúde: consentimento, permissão do cuidador, validação,
 * auditoria (quem e quando registrou) e isolamento por paciente.
 * O app é usado por um único paciente por aparelho ({@link #LOCAL_PATIENT_ID}), mas todas as
 * consultas recebem o paciente explicitamente.
 */
public class HealthRepository {

    public static final long LOCAL_PATIENT_ID = 1L;

    private static final Type STRING_LIST = new TypeToken<List<String>>() {}.getType();
    private static final Type RULE_LIST = new TypeToken<List<AlertRule>>() {}.getType();

    private final AppDatabase db;
    private final HealthDao dao;
    private final ReminderDao reminders;
    private final LongSupplier clock;
    private final ZoneId zone;
    private final Gson gson = new Gson();

    public HealthRepository(AppDatabase db) {
        this(db, System::currentTimeMillis, ZoneId.systemDefault());
    }

    public HealthRepository(AppDatabase db, LongSupplier clock, ZoneId zone) {
        this.db = db;
        this.dao = db.healthDao();
        this.reminders = db.reminderDao();
        this.clock = clock;
        this.zone = zone;
    }

    public long now() {
        return clock.getAsLong();
    }

    // ==================== PERFIL E CONSENTIMENTO ====================

    public ProfileData getProfile(long patientId) {
        return toData(dao.getProfile(patientId));
    }

    public ProfileData giveConsent(long patientId, boolean caregiverMayRecord, Author author) throws ValidationException {
        HealthValidator.validateAuthor(author);
        HealthProfile profile = dao.getProfile(patientId);
        if (profile == null) {
            profile = new HealthProfile();
            profile.patientId = patientId;
            profile.indicatorsJson = gson.toJson(ProfileData.defaultIndicators());
        }
        profile.consentAt = now();
        profile.consentBy = describe(author);
        profile.caregiverMayRecord = caregiverMayRecord;
        profile.updatedAt = profile.consentAt;
        profile.updatedBy = profile.consentBy;
        dao.saveProfile(profile);
        return toData(profile);
    }

    public ProfileData saveProfile(long patientId, ProfileData input, Author author) throws ValidationException {
        HealthValidator.validateAuthor(author);
        HealthProfile profile = requireConsent(patientId);
        if (input == null) throw new ValidationException("profile", "Perfil inválido.");

        List<String> conditions = new ArrayList<>();
        if (input.conditions != null) {
            for (String c : input.conditions) {
                if (!ProfileData.CONDITION_CODES.contains(c)) {
                    throw new ValidationException("conditions", "Condição desconhecida.");
                }
                if (!conditions.contains(c)) conditions.add(c);
            }
        }
        List<String> indicators = new ArrayList<>();
        if (input.indicators != null) {
            for (String i : input.indicators) {
                if (!MeasurementType.isValid(i)) {
                    throw new ValidationException("indicators", "Indicador desconhecido.");
                }
                if (!indicators.contains(i)) indicators.add(i);
            }
        }
        if (indicators.isEmpty()) {
            throw new ValidationException("indicators", "Escolha pelo menos uma medição para acompanhar.");
        }
        String other = trimToNull(input.conditionsOther);
        if (other != null && other.length() > HealthValidator.MAX_TEXT) {
            throw new ValidationException("conditionsOther", "Texto muito longo.");
        }
        String notes = trimToNull(input.doctorNotes);
        if (notes != null && notes.length() > 1000) {
            throw new ValidationException("doctorNotes", "Observações muito longas (máximo 1000 letras).");
        }
        List<AlertRule> rules = input.alertRules != null ? input.alertRules : new ArrayList<>();
        if (rules.size() > 20) throw new ValidationException("alertRules", "Máximo de 20 orientações.");
        for (AlertRule rule : rules) {
            if (rule == null) throw new ValidationException("alertRules", "Orientação inválida.");
            rule.validate();
        }

        profile.conditionsJson = gson.toJson(conditions);
        profile.conditionsOther = conditions.contains("other") ? other : null;
        profile.indicatorsJson = gson.toJson(indicators);
        profile.doctorNotes = notes;
        profile.caregiverMayRecord = input.caregiverMayRecord;
        profile.alertRulesJson = gson.toJson(rules);
        profile.updatedAt = now();
        profile.updatedBy = describe(author);
        dao.saveProfile(profile);
        return toData(profile);
    }

    // ==================== MEDIÇÕES ====================

    public static class MeasurementResult {
        public HealthMeasurement record;
        /** Orientações do profissional disparadas por esta leitura (vazio = nenhuma). */
        public List<String> guidance = new ArrayList<>();
    }

    public MeasurementResult saveMeasurement(long patientId, HealthMeasurement input, Author author)
        throws ValidationException {
        HealthProfile profile = requireRecordPermission(patientId, author);
        long now = now();
        HealthValidator.validateMeasurement(input, now);
        input.note = trimToNull(input.note);

        HealthMeasurement record;
        if (input.id > 0) {
            record = dao.getMeasurement(patientId, input.id);
            if (record == null) throw new ValidationException("id", "Registro não encontrado.");
            if (!record.type.equals(input.type)) throw new ValidationException("type", "Não é possível mudar o tipo do registro.");
            record.value1 = input.value1;
            record.value2 = input.value2;
            record.value3 = input.value3;
            record.context = input.context;
            record.measuredAt = input.measuredAt;
            record.note = input.note;
            record.updatedAt = now;
            record.updatedByRole = author.role;
            record.updatedByName = trimToNull(author.name);
            dao.updateMeasurement(record);
        } else {
            record = input;
            record.patientId = patientId;
            record.createdAt = now;
            record.createdByRole = author.role;
            record.createdByName = trimToNull(author.name);
            record.updatedAt = null;
            record.updatedByRole = null;
            record.updatedByName = null;
            record.id = dao.insertMeasurement(record);
        }

        MeasurementResult result = new MeasurementResult();
        result.record = record;
        result.guidance = AlertRule.evaluate(rules(profile), record);
        return result;
    }

    public void deleteMeasurement(long patientId, long id, Author author) throws ValidationException {
        requireRecordPermission(patientId, author);
        if (dao.deleteMeasurement(patientId, id) == 0) {
            throw new ValidationException("id", "Registro não encontrado.");
        }
    }

    public List<HealthMeasurement> listMeasurements(long patientId, String type, long from, long to) {
        if (type == null || type.isEmpty()) return dao.listMeasurements(patientId, from, to);
        return dao.listMeasurementsByType(patientId, type, from, to);
    }

    // ==================== SINTOMAS ====================

    public static class SymptomResult {
        public SymptomRecord record;
        /** Sinal de alerta: a interface orienta procurar atendimento imediato. */
        public boolean seekCareNow;
    }

    public SymptomResult saveSymptom(long patientId, SymptomRecord input, Author author) throws ValidationException {
        requireRecordPermission(patientId, author);
        long now = now();
        HealthValidator.validateSymptom(input, now);
        input.note = trimToNull(input.note);

        SymptomRecord record;
        if (input.id > 0) {
            record = dao.getSymptom(patientId, input.id);
            if (record == null) throw new ValidationException("id", "Registro não encontrado.");
            record.symptom = input.symptom;
            record.customLabel = input.customLabel;
            record.intensity = input.intensity;
            record.durationMinutes = input.durationMinutes;
            record.occurredAt = input.occurredAt;
            record.note = input.note;
            record.updatedAt = now;
            record.updatedByRole = author.role;
            record.updatedByName = trimToNull(author.name);
            dao.updateSymptom(record);
        } else {
            record = input;
            record.patientId = patientId;
            record.createdAt = now;
            record.createdByRole = author.role;
            record.createdByName = trimToNull(author.name);
            record.updatedAt = null;
            record.updatedByRole = null;
            record.updatedByName = null;
            record.id = dao.insertSymptom(record);
        }
        SymptomResult result = new SymptomResult();
        result.record = record;
        result.seekCareNow = SymptomCatalog.requiresImmediateCare(record.symptom);
        return result;
    }

    public void deleteSymptom(long patientId, long id, Author author) throws ValidationException {
        requireRecordPermission(patientId, author);
        if (dao.deleteSymptom(patientId, id) == 0) {
            throw new ValidationException("id", "Registro não encontrado.");
        }
    }

    public List<SymptomRecord> listSymptoms(long patientId, long from, long to) {
        return dao.listSymptoms(patientId, from, to);
    }

    // ==================== DOSES DE REMÉDIO ====================

    /** Registra (ou corrige) a situação de uma dose do dia. Nunca é criada automaticamente. */
    public MedicationIntake recordIntake(long patientId, MedicationIntake input, Author author) throws ValidationException {
        requireRecordPermission(patientId, author);
        if (input != null && input.scheduledTime != null && input.scheduledTime.isEmpty()) input.scheduledTime = null;
        HealthValidator.validateIntake(input);
        long now = now();

        final MedicationIntake[] saved = new MedicationIntake[1];
        db.runInTransaction(() -> {
            MedicationIntake record = dao.getIntake(patientId, input.medRef, input.scheduledDate);
            if (record == null) {
                record = new MedicationIntake();
                record.patientId = patientId;
                record.medRef = input.medRef;
                record.scheduledDate = input.scheduledDate;
                record.createdAt = now;
                record.createdByRole = author.role;
                record.createdByName = trimToNull(author.name);
            } else {
                record.updatedAt = now;
                record.updatedByRole = author.role;
                record.updatedByName = trimToNull(author.name);
            }
            record.medName = input.medName.trim();
            record.dosage = trimToNull(input.dosage);
            record.scheduledTime = input.scheduledTime;
            record.status = input.status;
            record.takenAt = MedicationIntake.STATUS_TAKEN.equals(input.status) ? now : null;
            if (record.id == 0) record.id = dao.insertIntake(record);
            else dao.updateIntake(record);
            resolvePendingMedicationEvents(patientId, record, author, now);
            saved[0] = record;
        });
        return saved[0];
    }

    public void deleteIntake(long patientId, String medRef, String date, Author author) throws ValidationException {
        requireRecordPermission(patientId, author);
        if (!HealthValidator.isValidDate(date)) throw new ValidationException("scheduledDate", "Data inválida.");
        dao.deleteIntake(patientId, medRef, date);
    }

    public List<MedicationIntake> listIntakes(long patientId, String fromDate, String toDate) {
        return dao.listIntakes(patientId, fromDate, toDate);
    }

    private void resolvePendingMedicationEvents(long patientId, MedicationIntake intake, Author author, long now) {
        LocalDate date = LocalDate.parse(intake.scheduledDate);
        long from = date.atStartOfDay(zone).toInstant().toEpochMilli();
        long to = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1;
        String status = MedicationIntake.STATUS_TAKEN.equals(intake.status)
            ? ReminderEvent.STATUS_DONE : ReminderEvent.STATUS_IGNORED;
        for (ReminderEvent e : reminders.listPendingFor(patientId, HealthReminder.KIND_MEDICATION, intake.medRef, from, to)) {
            e.status = status;
            e.resolvedAt = now;
            e.resolvedBy = describe(author);
            reminders.updateEvent(e);
        }
    }

    // ==================== LEMBRETES ====================

    public HealthReminder saveReminder(long patientId, HealthReminder input, Author author) throws ValidationException {
        HealthValidator.validateAuthor(author);
        requireConsent(patientId);
        HealthValidator.validateReminder(input);
        input.detail = trimToNull(input.detail);
        input.refId = trimToNull(input.refId);
        long now = now();

        HealthReminder existing = null;
        if (input.id > 0) {
            existing = reminders.getReminder(patientId, input.id);
            if (existing == null) throw new ValidationException("id", "Lembrete não encontrado.");
        } else if (input.refId != null) {
            existing = reminders.getLinkedReminder(patientId, input.kind, input.refId);
        }

        if (existing != null) {
            existing.title = input.title;
            existing.detail = input.detail;
            existing.times = input.times;
            existing.daysMask = input.daysMask;
            existing.onceAt = input.onceAt;
            existing.enabled = input.enabled;
            existing.updatedAt = now;
            reminders.updateReminder(existing);
            return existing;
        }
        input.patientId = patientId;
        input.createdAt = now;
        input.createdByName = describe(author);
        input.id = reminders.insertReminder(input);
        return input;
    }

    public HealthReminder getLinkedReminder(long patientId, String kind, String refId) {
        return reminders.getLinkedReminder(patientId, kind, refId);
    }

    public boolean deleteReminder(long patientId, long id) {
        return reminders.deleteReminder(patientId, id) > 0;
    }

    public List<HealthReminder> listReminders(long patientId) {
        return reminders.listReminders(patientId);
    }

    public List<HealthReminder> listEnabledReminders() {
        return reminders.listEnabledReminders();
    }

    public HealthReminder getReminderById(long id) {
        return reminders.getReminderById(id);
    }

    /** Chamado quando o alarme dispara: cria a ocorrência pendente (sem duplicar). */
    public ReminderEvent recordReminderFired(HealthReminder reminder, long scheduledAt) {
        ReminderEvent existing = reminders.getEvent(reminder.id, scheduledAt);
        if (existing != null) return existing;
        ReminderEvent event = new ReminderEvent();
        event.patientId = reminder.patientId;
        event.reminderId = reminder.id;
        event.kind = reminder.kind;
        event.refId = reminder.refId;
        event.title = reminder.title;
        event.detail = reminder.detail;
        event.scheduledAt = scheduledAt;
        event.status = ReminderEvent.STATUS_PENDING;
        event.id = reminders.insertEvent(event);
        return event;
    }

    public ReminderEvent getEvent(long eventId) {
        return reminders.getEventById(eventId);
    }

    public ReminderEvent resolveEvent(long patientId, long eventId, String status, Author author) throws ValidationException {
        HealthValidator.validateAuthor(author);
        if (!ReminderEvent.STATUS_DONE.equals(status) && !ReminderEvent.STATUS_IGNORED.equals(status)
            && !ReminderEvent.STATUS_PENDING.equals(status)) {
            throw new ValidationException("status", "Situação inválida.");
        }
        ReminderEvent event = reminders.getEventById(eventId);
        if (event == null || event.patientId != patientId) {
            throw new ValidationException("id", "Lembrete não encontrado.");
        }
        event.status = status;
        boolean pending = ReminderEvent.STATUS_PENDING.equals(status);
        event.resolvedAt = pending ? null : now();
        event.resolvedBy = pending ? null : describe(author);
        reminders.updateEvent(event);
        return event;
    }

    public List<ReminderEvent> listEvents(long patientId, long from, long to) {
        return reminders.listEvents(patientId, from, to);
    }

    // ==================== EXCLUSÃO (LGPD) ====================

    /** Apaga todo o diário do paciente. Retorna os ids de lembretes para cancelar os alarmes. */
    public List<Long> wipe(long patientId) {
        final List<Long> ids = new ArrayList<>();
        db.runInTransaction(() -> {
            ids.addAll(reminders.listReminderIds(patientId));
            reminders.deleteAllEvents(patientId);
            reminders.deleteAllReminders(patientId);
            dao.deleteAllMeasurements(patientId);
            dao.deleteAllSymptoms(patientId);
            dao.deleteAllIntakes(patientId);
            dao.deleteProfile(patientId);
        });
        return ids;
    }

    // ==================== APOIO ====================

    public String dateKey(long millis) {
        return Instant.ofEpochMilli(millis).atZone(zone).toLocalDate().toString();
    }

    public ZoneId zone() {
        return zone;
    }

    private HealthProfile requireConsent(long patientId) throws ValidationException {
        HealthProfile profile = dao.getProfile(patientId);
        if (profile == null || profile.consentAt == null) {
            throw new ValidationException("consent", "Ative o Diário de Saúde antes de registrar.");
        }
        return profile;
    }

    private HealthProfile requireRecordPermission(long patientId, Author author) throws ValidationException {
        HealthValidator.validateAuthor(author);
        HealthProfile profile = requireConsent(patientId);
        if (author.isCaregiver() && !profile.caregiverMayRecord) {
            throw new ValidationException("permission",
                "O cuidador ainda não tem autorização para registrar pelo idoso. Ative em Saúde > Perfil.");
        }
        return profile;
    }

    private List<AlertRule> rules(HealthProfile profile) {
        if (profile == null || profile.alertRulesJson == null) return new ArrayList<>();
        List<AlertRule> list = gson.fromJson(profile.alertRulesJson, RULE_LIST);
        return list != null ? list : new ArrayList<>();
    }

    private ProfileData toData(HealthProfile profile) {
        ProfileData data = new ProfileData();
        if (profile == null) {
            data.indicators = ProfileData.defaultIndicators();
            return data;
        }
        List<String> conditions = profile.conditionsJson != null ? gson.fromJson(profile.conditionsJson, STRING_LIST) : null;
        List<String> indicators = profile.indicatorsJson != null ? gson.fromJson(profile.indicatorsJson, STRING_LIST) : null;
        data.conditions = conditions != null ? conditions : new ArrayList<>();
        data.conditionsOther = profile.conditionsOther;
        data.indicators = indicators != null && !indicators.isEmpty() ? indicators : ProfileData.defaultIndicators();
        data.doctorNotes = profile.doctorNotes;
        data.caregiverMayRecord = profile.caregiverMayRecord;
        data.alertRules = rules(profile);
        data.consentAt = profile.consentAt;
        data.consentBy = profile.consentBy;
        data.updatedAt = profile.updatedAt;
        data.updatedBy = profile.updatedBy;
        return data;
    }

    public static String describe(Author author) {
        if (author == null) return null;
        String role = Author.ROLE_CAREGIVER.equals(author.role) ? "cuidador"
            : Author.ROLE_NOTIFICATION.equals(author.role) ? "notificação" : "idoso";
        String name = trimToNull(author.name);
        return name != null ? name + " (" + role + ")" : role;
    }

    static String trimToNull(String value) {
        if (value == null) return null;
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }
}
