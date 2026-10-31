package com.cuidarapp.health.report;

import com.cuidarapp.health.HealthRepository;
import com.cuidarapp.health.ProfileData;
import com.cuidarapp.health.domain.HealthStats;
import com.cuidarapp.health.domain.IntakeStatus;
import com.cuidarapp.health.domain.SymptomCatalog;
import com.cuidarapp.health.model.Author;
import com.cuidarapp.health.model.HealthMeasurement;
import com.cuidarapp.health.model.MeasurementType;
import com.cuidarapp.health.model.MedicationIntake;
import com.cuidarapp.health.model.SymptomRecord;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Monta o {@link ReportData} a partir dos registros reais do período (ordem cronológica). */
public final class ReportBuilder {

    public static final String[] MEASUREMENT_HEADERS = {"Data e hora", "Valor", "Momento", "Registrado por", "Observação"};
    public static final String[] SYMPTOM_HEADERS = {"Data e hora", "Sintoma", "Intensidade", "Duração", "Registrado por", "Observação"};
    public static final String[] INTAKE_HEADERS = {"Data", "Previsto", "Remédio", "Dose informada", "Situação", "Registrado por"};

    private static final Locale PT_BR = new Locale("pt", "BR");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy", PT_BR);
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", PT_BR);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm", PT_BR);

    private final ZoneId zone;

    public ReportBuilder(ZoneId zone) {
        this.zone = zone;
    }

    public ReportData build(String patientName, ProfileData profile, long from, long to, long now,
                            List<HealthMeasurement> measurements, List<SymptomRecord> symptoms,
                            List<MedicationIntake> intakes) {
        ReportData data = new ReportData();
        data.patientName = patientName == null || patientName.trim().isEmpty() ? "Não informado" : patientName.trim();
        data.periodText = date(from) + " a " + date(to);
        data.issuedText = dateTime(now);
        data.conditionsText = profile != null ? profile.conditionsText() : "Não informadas";
        data.doctorNotes = profile != null ? profile.doctorNotes : null;

        List<HealthMeasurement> sorted = new ArrayList<>(measurements);
        sorted.sort(Comparator.comparingLong(m -> m.measuredAt));
        data.measurementCount = sorted.size();
        for (String type : MeasurementType.ALL) {
            List<HealthMeasurement> ofType = new ArrayList<>();
            for (HealthMeasurement m : sorted) if (type.equals(m.type)) ofType.add(m);
            if (!ofType.isEmpty()) data.indicators.add(indicator(type, ofType));
        }

        List<SymptomRecord> sortedSymptoms = new ArrayList<>(symptoms);
        sortedSymptoms.sort(Comparator.comparingLong(s -> s.occurredAt));
        data.symptomCount = sortedSymptoms.size();
        for (SymptomRecord s : sortedSymptoms) {
            data.symptomRows.add(new String[]{
                dateTime(s.occurredAt),
                SymptomCatalog.label(s.symptom, s.customLabel),
                SymptomCatalog.intensityLabel(s.intensity),
                s.durationMinutes != null ? duration(s.durationMinutes) : "—",
                author(s.createdByRole, s.createdByName, s.updatedAt != null),
                s.note != null ? s.note : ""
            });
        }

        List<MedicationIntake> sortedIntakes = new ArrayList<>(intakes);
        sortedIntakes.sort(Comparator.comparing((MedicationIntake i) -> i.scheduledDate)
            .thenComparing(i -> i.scheduledTime != null ? i.scheduledTime : ""));
        data.intakeCount = sortedIntakes.size();
        for (MedicationIntake i : sortedIntakes) {
            String status = IntakeStatus.label(IntakeStatus.of(i, zone));
            if (i.takenAt != null) status += " (confirmado às " + time(i.takenAt) + ")";
            data.intakeRows.add(new String[]{
                isoToBr(i.scheduledDate),
                i.scheduledTime != null ? i.scheduledTime : "—",
                i.medName,
                i.dosage != null ? i.dosage : "—",
                status,
                author(i.createdByRole, i.createdByName, i.updatedAt != null)
            });
        }
        return data;
    }

    private ReportData.IndicatorSection indicator(String type, List<HealthMeasurement> list) {
        ReportData.IndicatorSection section = new ReportData.IndicatorSection();
        section.type = type;
        section.unit = MeasurementType.unit(type);
        section.title = MeasurementType.label(type) + " (" + section.unit + ")";
        section.headers = MEASUREMENT_HEADERS;

        if (MeasurementType.BLOOD_PRESSURE.equals(type)) {
            ReportData.Series sys = new ReportData.Series("Máxima (sistólica)");
            ReportData.Series dia = new ReportData.Series("Mínima (diastólica)");
            List<Double> pulses = new ArrayList<>();
            for (HealthMeasurement m : list) {
                sys.times.add(m.measuredAt);
                sys.values.add(m.value1);
                dia.times.add(m.measuredAt);
                dia.values.add(m.value2);
                if (m.value3 != null) pulses.add(m.value3);
            }
            section.series.add(sys);
            section.series.add(dia);
            section.statsLines.add(statsLine("Máxima (sistólica)", sys.values, "mmHg", false));
            section.statsLines.add(statsLine("Mínima (diastólica)", dia.values, "mmHg", false));
            if (!pulses.isEmpty()) section.statsLines.add(statsLine("Pulso", pulses, "bpm", false));
        } else {
            ReportData.Series series = new ReportData.Series(MeasurementType.label(type));
            for (HealthMeasurement m : list) {
                series.times.add(m.measuredAt);
                series.values.add(m.value1);
            }
            section.series.add(series);
            boolean decimals = decimals(type);
            if (MeasurementType.GLUCOSE.equals(type)) {
                // Média de glicemias de momentos diferentes não tem significado: resumo por momento.
                for (String context : MeasurementType.GLUCOSE_CONTEXTS) {
                    List<Double> values = new ArrayList<>();
                    for (HealthMeasurement m : list) if (context.equals(m.context)) values.add(m.value1);
                    if (!values.isEmpty()) {
                        section.statsLines.add(statsLine(MeasurementType.contextLabel(context), values, section.unit, false));
                    }
                }
            } else {
                section.statsLines.add(statsLine("Registros", series.values, section.unit, decimals));
            }
        }

        for (HealthMeasurement m : list) {
            section.rows.add(new String[]{
                dateTime(m.measuredAt),
                valueText(m),
                m.context != null ? MeasurementType.contextLabel(m.context) : "—",
                author(m.createdByRole, m.createdByName, m.updatedAt != null),
                m.note != null ? m.note : ""
            });
        }
        return section;
    }

    static String statsLine(String label, List<Double> values, String unit, boolean decimals) {
        HealthStats stats = HealthStats.of(values);
        String count = stats.count + (stats.count == 1 ? " registro" : " registros");
        if (!stats.hasSummary()) {
            return label + ": " + count + " (média calculada a partir de " + HealthStats.MIN_SAMPLES + " registros)";
        }
        return label + ": " + count + "; média " + number(stats.mean, true) + " " + unit
            + "; menor " + number(stats.min, decimals) + "; maior " + number(stats.max, decimals);
    }

    public static String valueText(HealthMeasurement m) {
        String unit = MeasurementType.unit(m.type);
        if (MeasurementType.BLOOD_PRESSURE.equals(m.type)) {
            String text = number(m.value1, false) + " x " + number(m.value2, false) + " " + unit;
            if (m.value3 != null) text += " • pulso " + number(m.value3, false) + " bpm";
            return text;
        }
        return number(m.value1, decimals(m.type)) + " " + unit;
    }

    static boolean decimals(String type) {
        return MeasurementType.WEIGHT.equals(type) || MeasurementType.TEMPERATURE.equals(type);
    }

    static String number(Double value, boolean oneDecimal) {
        if (value == null) return "—";
        DecimalFormat format = new DecimalFormat(oneDecimal ? "0.0" : "0", DecimalFormatSymbols.getInstance(PT_BR));
        return format.format(value);
    }

    static String author(String role, String name, boolean edited) {
        String text = HealthRepository.describe(new Author(role, name));
        if (text == null) text = "—";
        return edited ? text + " • editado" : text;
    }

    static String duration(int minutes) {
        if (minutes < 60) return minutes + " min";
        int h = minutes / 60;
        int m = minutes % 60;
        return m == 0 ? h + " h" : h + " h " + m + " min";
    }

    private String date(long millis) {
        return Instant.ofEpochMilli(millis).atZone(zone).format(DATE);
    }

    private String dateTime(long millis) {
        return Instant.ofEpochMilli(millis).atZone(zone).format(DATE_TIME);
    }

    private String time(long millis) {
        return Instant.ofEpochMilli(millis).atZone(zone).format(TIME);
    }

    private static String isoToBr(String iso) {
        String[] p = iso.split("-");
        return p.length == 3 ? p[2] + "/" + p[1] + "/" + p[0] : iso;
    }
}
