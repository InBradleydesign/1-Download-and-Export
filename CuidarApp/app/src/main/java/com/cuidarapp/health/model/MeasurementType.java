package com.cuidarapp.health.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Tipos de medição suportados, com rótulo e unidade usados na interface e no PDF. */
public final class MeasurementType {

    public static final String BLOOD_PRESSURE = "bp";
    public static final String GLUCOSE = "glucose";
    public static final String WEIGHT = "weight";
    public static final String TEMPERATURE = "temperature";
    public static final String SPO2 = "spo2";
    public static final String HEART_RATE = "heart_rate";
    public static final String URINE = "urine";

    public static final List<String> ALL = Collections.unmodifiableList(Arrays.asList(
        BLOOD_PRESSURE, GLUCOSE, WEIGHT, TEMPERATURE, SPO2, HEART_RATE, URINE));

    public static final String CONTEXT_FASTING = "fasting";
    public static final String CONTEXT_BEFORE_MEAL = "before_meal";
    public static final String CONTEXT_AFTER_MEAL = "after_meal";
    public static final String CONTEXT_BEDTIME = "bedtime";
    public static final String CONTEXT_OTHER = "other";

    public static final List<String> GLUCOSE_CONTEXTS = Collections.unmodifiableList(Arrays.asList(
        CONTEXT_FASTING, CONTEXT_BEFORE_MEAL, CONTEXT_AFTER_MEAL, CONTEXT_BEDTIME, CONTEXT_OTHER));

    private MeasurementType() {
    }

    public static boolean isValid(String type) {
        return type != null && ALL.contains(type);
    }

    public static String label(String type) {
        if (type == null) return "";
        switch (type) {
            case BLOOD_PRESSURE: return "Pressão arterial";
            case GLUCOSE: return "Glicemia";
            case WEIGHT: return "Peso";
            case TEMPERATURE: return "Temperatura";
            case SPO2: return "Saturação de oxigênio";
            case HEART_RATE: return "Frequência cardíaca";
            case URINE: return "Volume de urina";
            default: return type;
        }
    }

    public static String unit(String type) {
        if (type == null) return "";
        switch (type) {
            case BLOOD_PRESSURE: return "mmHg";
            case GLUCOSE: return "mg/dL";
            case WEIGHT: return "kg";
            case TEMPERATURE: return "°C";
            case SPO2: return "%";
            case HEART_RATE: return "bpm";
            case URINE: return "mL";
            default: return "";
        }
    }

    public static String contextLabel(String context) {
        if (context == null) return "";
        switch (context) {
            case CONTEXT_FASTING: return "Em jejum";
            case CONTEXT_BEFORE_MEAL: return "Antes de refeição";
            case CONTEXT_AFTER_MEAL: return "Depois de refeição";
            case CONTEXT_BEDTIME: return "Ao deitar";
            case CONTEXT_OTHER: return "Outro momento";
            default: return context;
        }
    }
}
