package com.cuidarapp.health.report;

import java.util.ArrayList;
import java.util.List;

/** Conteúdo do relatório já organizado e formatado, pronto para ser desenhado no PDF. */
public class ReportData {

    public static final String DISCLAIMER =
        "Dados registrados em casa pelo próprio paciente ou pelo cuidador. Este relatório não faz "
            + "diagnóstico e não substitui a avaliação de um profissional de saúde.";

    public String patientName;
    public String periodText;
    public String issuedText;
    public int measurementCount;
    public int symptomCount;
    public int intakeCount;
    public String conditionsText;
    public String doctorNotes;

    public final List<IndicatorSection> indicators = new ArrayList<>();
    public final List<String[]> symptomRows = new ArrayList<>();
    public final List<String[]> intakeRows = new ArrayList<>();

    public int totalRecords() {
        return measurementCount + symptomCount + intakeCount;
    }

    public static class Series {
        public final String name;
        public final List<Long> times = new ArrayList<>();
        public final List<Double> values = new ArrayList<>();

        public Series(String name) {
            this.name = name;
        }
    }

    public static class IndicatorSection {
        public String type;
        public String title;
        public String unit;
        public final List<Series> series = new ArrayList<>();
        public final List<String> statsLines = new ArrayList<>();
        public String[] headers;
        public final List<String[]> rows = new ArrayList<>();
    }
}
