package com.cuidarapp.health.domain;

import com.cuidarapp.health.model.HealthMeasurement;
import com.cuidarapp.health.model.MeasurementType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Orientação cadastrada pelo cuidador conforme o profissional de saúde, por exemplo:
 * "se a pressão máxima for igual ou maior que X, ligar para a UBS".
 * O app não tem limites próprios: sem regra cadastrada, nenhuma leitura gera alerta.
 */
public class AlertRule {

    public static final String OP_AT_LEAST = "gte";
    public static final String OP_AT_MOST = "lte";

    /** systolic, diastolic, pulse ou o tipo da medição (glucose, weight, ...). */
    public String metric;
    public String op;
    public double threshold;
    /** Para glicemia: aplica só a um momento (jejum etc.). Vazio = qualquer momento. */
    public String context;
    public String message;

    public static final List<String> METRICS = Collections.unmodifiableList(Arrays.asList(
        "systolic", "diastolic", "pulse", MeasurementType.GLUCOSE, MeasurementType.WEIGHT,
        MeasurementType.TEMPERATURE, MeasurementType.SPO2, MeasurementType.HEART_RATE, MeasurementType.URINE));

    public void validate() throws ValidationException {
        if (metric == null || !METRICS.contains(metric)) {
            throw new ValidationException("metric", "Escolha o que será observado na orientação.");
        }
        if (!OP_AT_LEAST.equals(op) && !OP_AT_MOST.equals(op)) {
            throw new ValidationException("op", "Escolha \"igual ou acima\" ou \"igual ou abaixo\".");
        }
        if (Double.isNaN(threshold) || threshold <= 0 || threshold > 5000) {
            throw new ValidationException("threshold", "Informe o valor indicado pelo profissional.");
        }
        if (message == null || message.trim().isEmpty() || message.length() > 200) {
            throw new ValidationException("message", "Escreva a orientação do profissional (até 200 letras).");
        }
        message = message.trim();
        if (context != null && context.isEmpty()) context = null;
        if (context != null && !MeasurementType.GLUCOSE_CONTEXTS.contains(context)) {
            throw new ValidationException("context", "Momento da glicemia inválido.");
        }
    }

    /** Valor da medição correspondente à métrica desta regra, ou null se não se aplica. */
    Double valueFor(HealthMeasurement m) {
        if (MeasurementType.BLOOD_PRESSURE.equals(m.type)) {
            switch (metric) {
                case "systolic": return m.value1;
                case "diastolic": return m.value2;
                case "pulse": return m.value3;
                default: return null;
            }
        }
        if (!metric.equals(m.type)) return null;
        if (context != null && !context.equals(m.context)) return null;
        return m.value1;
    }

    public boolean matches(HealthMeasurement m) {
        Double value = valueFor(m);
        if (value == null) return false;
        return OP_AT_LEAST.equals(op) ? value >= threshold : value <= threshold;
    }

    /** Mensagens das regras disparadas por uma medição, na ordem cadastrada. */
    public static List<String> evaluate(List<AlertRule> rules, HealthMeasurement m) {
        List<String> messages = new ArrayList<>();
        if (rules == null || m == null) return messages;
        for (AlertRule rule : rules) {
            if (rule != null && rule.metric != null && rule.matches(m)) {
                messages.add(rule.message);
            }
        }
        return messages;
    }
}
