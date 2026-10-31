package com.cuidarapp.health.domain;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sintomas que podem ser registrados. Os marcados como sinais de alerta não geram diagnóstico:
 * apenas fazem a interface orientar a procurar atendimento imediato em vez de aguardar a consulta.
 * Mantenha os códigos iguais aos de health.js.
 */
public final class SymptomCatalog {

    public static final String OTHER = "other";

    private static final Map<String, String> LABELS = new LinkedHashMap<>();

    static {
        LABELS.put("pain", "Dor");
        LABELS.put("headache", "Dor de cabeça");
        LABELS.put("dizziness", "Tontura");
        LABELS.put("fatigue", "Cansaço");
        LABELS.put("nausea", "Enjoo ou vômito");
        LABELS.put("fever", "Febre ou calafrio");
        LABELS.put("swelling", "Inchaço");
        LABELS.put("appetite", "Falta de apetite");
        LABELS.put("sleep", "Dormiu mal");
        LABELS.put("chest_pain", "Dor ou aperto no peito");
        LABELS.put("breath", "Falta de ar");
        LABELS.put("confusion", "Confusão ou fala enrolada");
        LABELS.put("weakness_side", "Fraqueza ou dormência de um lado do corpo");
        LABELS.put("fainting", "Desmaio");
        LABELS.put(OTHER, "Outro");
    }

    private static final List<String> SEEK_CARE_NOW = Collections.unmodifiableList(Arrays.asList(
        "chest_pain", "breath", "confusion", "weakness_side", "fainting"));

    private SymptomCatalog() {
    }

    public static boolean isValid(String code) {
        return code != null && LABELS.containsKey(code);
    }

    public static String label(String code, String customLabel) {
        if (OTHER.equals(code) && customLabel != null && !customLabel.trim().isEmpty()) {
            return customLabel.trim();
        }
        String label = LABELS.get(code);
        return label != null ? label : String.valueOf(code);
    }

    public static boolean requiresImmediateCare(String code) {
        return SEEK_CARE_NOW.contains(code);
    }

    public static String intensityLabel(int intensity) {
        switch (intensity) {
            case 1: return "Leve";
            case 2: return "Moderada";
            case 3: return "Forte";
            default: return "";
        }
    }
}
