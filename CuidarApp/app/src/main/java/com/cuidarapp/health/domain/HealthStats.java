package com.cuidarapp.health.domain;

import java.util.List;

/**
 * Resumo descritivo de uma série. Média, mínimo e máximo só são calculados a partir de
 * {@link #MIN_SAMPLES} registros; com menos que isso a interface mostra apenas a contagem.
 */
public final class HealthStats {

    public static final int MIN_SAMPLES = 3;

    public final int count;
    public final Double mean;
    public final Double min;
    public final Double max;

    private HealthStats(int count, Double mean, Double min, Double max) {
        this.count = count;
        this.mean = mean;
        this.min = min;
        this.max = max;
    }

    public boolean hasSummary() {
        return mean != null;
    }

    public static HealthStats of(List<Double> values) {
        int n = 0;
        double sum = 0;
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        if (values != null) {
            for (Double v : values) {
                if (v == null || Double.isNaN(v)) continue;
                n++;
                sum += v;
                min = Math.min(min, v);
                max = Math.max(max, v);
            }
        }
        if (n < MIN_SAMPLES) {
            return new HealthStats(n, null, null, null);
        }
        return new HealthStats(n, sum / n, min, max);
    }
}
