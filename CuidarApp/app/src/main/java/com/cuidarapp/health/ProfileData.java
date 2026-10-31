package com.cuidarapp.health;

import com.cuidarapp.health.domain.AlertRule;
import com.cuidarapp.health.model.HealthProfile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Perfil de saúde no formato trocado com a interface e usado no relatório. */
public class ProfileData {

    public static final Map<String, String> CONDITIONS = new LinkedHashMap<>();

    static {
        CONDITIONS.put("hypertension", "Hipertensão");
        CONDITIONS.put("diabetes", "Diabetes");
        CONDITIONS.put("heart", "Doença do coração");
        CONDITIONS.put("liver", "Doença do fígado");
        CONDITIONS.put("respiratory", "Doença respiratória");
        CONDITIONS.put("kidney", "Doença dos rins");
        CONDITIONS.put("other", "Outra");
    }

    public static final List<String> CONDITION_CODES =
        Collections.unmodifiableList(new ArrayList<>(CONDITIONS.keySet()));

    public List<String> conditions = new ArrayList<>();
    public String conditionsOther;
    public List<String> indicators = new ArrayList<>();
    public String doctorNotes;
    public boolean caregiverMayRecord;
    public List<AlertRule> alertRules = new ArrayList<>();
    public Long consentAt;
    public String consentBy;
    public Long updatedAt;
    public String updatedBy;

    public boolean hasConsent() {
        return consentAt != null;
    }

    /** Condições em texto corrido para o relatório. */
    public String conditionsText() {
        List<String> labels = new ArrayList<>();
        for (String code : conditions) {
            if ("other".equals(code)) continue;
            String label = CONDITIONS.get(code);
            if (label != null) labels.add(label);
        }
        if (conditions.contains("other") && conditionsOther != null && !conditionsOther.trim().isEmpty()) {
            labels.add(conditionsOther.trim());
        }
        return labels.isEmpty() ? "Não informadas" : String.join(", ", labels);
    }

    static List<String> defaultIndicators() {
        return new ArrayList<>(HealthProfile.DEFAULT_INDICATORS);
    }
}
