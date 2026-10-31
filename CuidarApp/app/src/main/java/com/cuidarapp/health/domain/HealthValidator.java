package com.cuidarapp.health.domain;

import com.cuidarapp.health.model.Author;
import com.cuidarapp.health.model.HealthMeasurement;
import com.cuidarapp.health.model.HealthReminder;
import com.cuidarapp.health.model.MeasurementType;
import com.cuidarapp.health.model.MedicationIntake;
import com.cuidarapp.health.model.SymptomRecord;

import java.util.regex.Pattern;

/**
 * Validação de tipo, unidade e faixa plausível de leitura.
 * As faixas existem apenas para barrar erros de digitação (ex.: 1200 em vez de 120); não são
 * limites clínicos e nenhum valor é classificado como normal ou alterado.
 */
public final class HealthValidator {

    public static final int MAX_NOTE = 500;
    public static final int MAX_TEXT = 80;
    /** Tolerância para relógios levemente adiantados entre aparelhos/medidores. */
    public static final long FUTURE_TOLERANCE_MS = 5 * 60 * 1000L;
    /** 01/01/2000: nada antes disso é aceito como data de medição. */
    public static final long MIN_TIMESTAMP = 946684800000L;

    private static final Pattern TIME = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d$");
    private static final Pattern DATE = Pattern.compile("^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$");

    private HealthValidator() {
    }

    /** Faixa de leitura possível de um aparelho doméstico, por tipo: {mín, máx}. */
    public static double[] plausibleRange(String type) {
        switch (type) {
            case MeasurementType.BLOOD_PRESSURE: return new double[]{50, 300};
            case MeasurementType.GLUCOSE: return new double[]{10, 800};
            case MeasurementType.WEIGHT: return new double[]{20, 350};
            case MeasurementType.TEMPERATURE: return new double[]{30, 45};
            case MeasurementType.SPO2: return new double[]{50, 100};
            case MeasurementType.HEART_RATE: return new double[]{20, 250};
            case MeasurementType.URINE: return new double[]{1, 5000};
            default: return new double[]{0, 0};
        }
    }

    public static final double[] DIASTOLIC_RANGE = {30, 200};
    public static final double[] PULSE_RANGE = {20, 250};

    public static void validateAuthor(Author author) throws ValidationException {
        if (author == null || !Author.isValidRole(author.role)) {
            throw new ValidationException("author", "Não foi possível identificar quem está registrando.");
        }
        if (author.name != null && author.name.length() > MAX_TEXT) {
            throw new ValidationException("author", "Nome muito longo.");
        }
    }

    public static void validateMeasurement(HealthMeasurement m, long now) throws ValidationException {
        if (m == null || !MeasurementType.isValid(m.type)) {
            throw new ValidationException("type", "Tipo de medição desconhecido.");
        }
        validateTimestamp("measuredAt", m.measuredAt, now);
        validateNote(m.note);

        String unit = MeasurementType.unit(m.type);
        if (MeasurementType.BLOOD_PRESSURE.equals(m.type)) {
            requireRange("value1", "A pressão máxima (sistólica)", m.value1, plausibleRange(m.type), unit);
            if (m.value2 == null) {
                throw new ValidationException("value2", "Informe a pressão mínima (diastólica).");
            }
            requireRange("value2", "A pressão mínima (diastólica)", m.value2, DIASTOLIC_RANGE, unit);
            if (m.value2 >= m.value1) {
                throw new ValidationException("value2",
                    "A pressão mínima deve ser menor que a máxima. Confira se os números não foram trocados.");
            }
            if (m.value3 != null) {
                requireRange("value3", "O pulso", m.value3, PULSE_RANGE, "bpm");
            }
            m.value1 = Math.round(m.value1);
            m.value2 = (double) Math.round(m.value2);
            if (m.value3 != null) m.value3 = (double) Math.round(m.value3);
            m.context = null;
            return;
        }

        requireRange("value1", "O valor", m.value1, plausibleRange(m.type), unit);
        m.value2 = null;
        m.value3 = null;
        if (MeasurementType.GLUCOSE.equals(m.type)) {
            if (m.context == null || !MeasurementType.GLUCOSE_CONTEXTS.contains(m.context)) {
                throw new ValidationException("context", "Escolha quando a glicemia foi medida.");
            }
            m.value1 = Math.round(m.value1);
        } else {
            m.context = null;
            boolean decimals = MeasurementType.WEIGHT.equals(m.type) || MeasurementType.TEMPERATURE.equals(m.type);
            m.value1 = decimals ? Math.round(m.value1 * 10) / 10.0 : Math.round(m.value1);
        }
    }

    public static void validateSymptom(SymptomRecord s, long now) throws ValidationException {
        if (s == null || !SymptomCatalog.isValid(s.symptom)) {
            throw new ValidationException("symptom", "Escolha o sintoma.");
        }
        if (SymptomCatalog.OTHER.equals(s.symptom)) {
            if (s.customLabel == null || s.customLabel.trim().isEmpty()) {
                throw new ValidationException("customLabel", "Descreva o sintoma.");
            }
            if (s.customLabel.trim().length() > MAX_TEXT) {
                throw new ValidationException("customLabel", "Descrição muito longa (máximo " + MAX_TEXT + " letras).");
            }
            s.customLabel = s.customLabel.trim();
        } else {
            s.customLabel = null;
        }
        if (s.intensity < 1 || s.intensity > 3) {
            throw new ValidationException("intensity", "Escolha a intensidade: leve, moderada ou forte.");
        }
        if (s.durationMinutes != null && (s.durationMinutes < 1 || s.durationMinutes > 60 * 24 * 30)) {
            throw new ValidationException("durationMinutes", "Duração inválida.");
        }
        validateTimestamp("occurredAt", s.occurredAt, now);
        validateNote(s.note);
    }

    public static void validateIntake(MedicationIntake i) throws ValidationException {
        if (i == null || i.medRef == null || i.medRef.trim().isEmpty()) {
            throw new ValidationException("medRef", "Remédio não identificado.");
        }
        if (i.medName == null || i.medName.trim().isEmpty() || i.medName.length() > MAX_TEXT) {
            throw new ValidationException("medName", "Nome do remédio inválido.");
        }
        if (i.dosage != null && i.dosage.length() > MAX_TEXT) {
            throw new ValidationException("dosage", "Dose muito longa.");
        }
        if (!isValidDate(i.scheduledDate)) {
            throw new ValidationException("scheduledDate", "Data inválida.");
        }
        if (i.scheduledTime != null && !i.scheduledTime.isEmpty() && !isValidTime(i.scheduledTime)) {
            throw new ValidationException("scheduledTime", "Horário inválido.");
        }
        if (!MedicationIntake.STATUS_TAKEN.equals(i.status) && !MedicationIntake.STATUS_MISSED.equals(i.status)) {
            throw new ValidationException("status", "Situação da dose inválida.");
        }
    }

    public static void validateReminder(HealthReminder r) throws ValidationException {
        if (r == null) throw new ValidationException("kind", "Lembrete inválido.");
        if (!HealthReminder.KIND_MEASUREMENT.equals(r.kind) && !HealthReminder.KIND_MEDICATION.equals(r.kind)
            && !HealthReminder.KIND_APPOINTMENT.equals(r.kind)) {
            throw new ValidationException("kind", "Tipo de lembrete inválido.");
        }
        if (r.title == null || r.title.trim().isEmpty() || r.title.length() > MAX_TEXT) {
            throw new ValidationException("title", "Informe o nome do lembrete.");
        }
        r.title = r.title.trim();
        if (r.detail != null && r.detail.length() > MAX_NOTE) {
            throw new ValidationException("detail", "Texto muito longo.");
        }
        if (HealthReminder.KIND_APPOINTMENT.equals(r.kind)) {
            if (r.onceAt == null || r.onceAt < MIN_TIMESTAMP) {
                throw new ValidationException("onceAt", "Informe data e horário do aviso.");
            }
            return;
        }
        r.onceAt = null;
        if (r.times == null || r.times.trim().isEmpty()) {
            throw new ValidationException("times", "Adicione pelo menos um horário, conforme a orientação do profissional.");
        }
        String[] parts = r.times.split(",");
        if (parts.length > 12) {
            throw new ValidationException("times", "No máximo 12 horários por lembrete.");
        }
        for (String t : parts) {
            if (!isValidTime(t.trim())) {
                throw new ValidationException("times", "Horário inválido: " + t.trim());
            }
        }
        if ((r.daysMask & 0x7F) == 0) {
            throw new ValidationException("daysMask", "Escolha pelo menos um dia da semana.");
        }
        r.daysMask &= 0x7F;
    }

    public static boolean isValidTime(String value) {
        return value != null && TIME.matcher(value).matches();
    }

    public static boolean isValidDate(String value) {
        return value != null && DATE.matcher(value).matches();
    }

    private static void validateTimestamp(String field, long value, long now) throws ValidationException {
        if (value < MIN_TIMESTAMP) {
            throw new ValidationException(field, "Informe a data e o horário.");
        }
        if (value > now + FUTURE_TOLERANCE_MS) {
            throw new ValidationException(field, "A data e o horário não podem estar no futuro.");
        }
    }

    private static void validateNote(String note) throws ValidationException {
        if (note != null && note.length() > MAX_NOTE) {
            throw new ValidationException("note", "Observação muito longa (máximo " + MAX_NOTE + " letras).");
        }
    }

    private static void requireRange(String field, String what, double value, double[] range, String unit)
        throws ValidationException {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new ValidationException(field, what + " precisa ser um número.");
        }
        if (value < range[0] || value > range[1]) {
            throw new ValidationException(field, what + " deve estar entre " + format(range[0]) + " e "
                + format(range[1]) + " " + unit + ". Confira o número digitado.");
        }
    }

    private static String format(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v).replace('.', ',');
    }
}
