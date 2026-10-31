package com.cuidarapp.health.report;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Desenha o {@link ReportData} em um PDF A4 (várias páginas, tabelas e gráficos de linha). */
public class PdfReportRenderer {

    private static final int PAGE_W = 595;
    private static final int PAGE_H = 842;
    private static final float MARGIN = 40f;
    private static final float CONTENT_W = PAGE_W - 2 * MARGIN;
    private static final float FOOTER_H = 46f;
    private static final float CHART_H = 150f;

    private static final int COLOR_TEXT = Color.rgb(31, 41, 55);
    private static final int COLOR_MUTED = Color.rgb(75, 85, 99);
    private static final int COLOR_LINE = Color.rgb(209, 213, 219);
    private static final int COLOR_HEADER_BG = Color.rgb(239, 235, 255);
    private static final int COLOR_PRIMARY = Color.rgb(91, 63, 217);
    private static final int[] SERIES_COLORS = {Color.rgb(29, 78, 216), Color.rgb(194, 65, 12)};

    private final Paint title = text(18f, true, COLOR_TEXT);
    private final Paint h2 = text(13f, true, COLOR_PRIMARY);
    private final Paint body = text(9.5f, false, COLOR_TEXT);
    private final Paint bold = text(9.5f, true, COLOR_TEXT);
    private final Paint small = text(8f, false, COLOR_MUTED);
    private final Paint cell = text(8.2f, false, COLOR_TEXT);
    private final Paint cellBold = text(8.2f, true, COLOR_TEXT);
    private final Paint line = stroke(0.6f, COLOR_LINE);

    private PdfDocument document;
    private PdfDocument.Page page;
    private Canvas canvas;
    private int pageNumber;
    private float y;
    private ReportData data;

    public File render(ReportData data, File out) throws IOException {
        this.data = data;
        document = new PdfDocument();
        pageNumber = 0;
        try {
            newPage();
            drawCover();
            if (data.totalRecords() == 0) {
                paragraph("Nenhum registro encontrado neste período.", bold);
            }
            for (ReportData.IndicatorSection section : data.indicators) {
                drawIndicator(section);
            }
            if (!data.symptomRows.isEmpty()) {
                heading("Sintomas relatados");
                table(ReportBuilder.SYMPTOM_HEADERS, new float[]{0.16f, 0.2f, 0.11f, 0.1f, 0.18f, 0.25f}, data.symptomRows);
            }
            if (!data.intakeRows.isEmpty()) {
                heading("Remédios: histórico de confirmações");
                paragraph("Mostra apenas o que foi marcado no app. Dose conforme digitada pelo usuário.", small);
                table(ReportBuilder.INTAKE_HEADERS, new float[]{0.12f, 0.09f, 0.2f, 0.17f, 0.24f, 0.18f}, data.intakeRows);
            }
            finishPage();
            try (FileOutputStream fos = new FileOutputStream(out)) {
                document.writeTo(fos);
            }
        } finally {
            document.close();
            document = null;
        }
        return out;
    }

    // ==================== PÁGINAS ====================

    private void newPage() {
        if (page != null) finishPage();
        pageNumber++;
        page = document.startPage(new PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNumber).create());
        canvas = page.getCanvas();
        y = MARGIN;
        if (pageNumber > 1) {
            canvas.drawText("Diário de Saúde • " + data.patientName + " • " + data.periodText, MARGIN, y + 8, small);
            y += 22;
        }
    }

    private void finishPage() {
        if (page == null) return;
        float top = PAGE_H - MARGIN - FOOTER_H + 10;
        canvas.drawLine(MARGIN, top, PAGE_W - MARGIN, top, line);
        float ty = top + 12;
        for (String l : wrap(ReportData.DISCLAIMER, small, CONTENT_W - 60)) {
            canvas.drawText(l, MARGIN, ty, small);
            ty += 10;
        }
        Paint right = new Paint(small);
        right.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Página " + pageNumber, PAGE_W - MARGIN, top + 12, right);
        document.finishPage(page);
        page = null;
    }

    private float bottom() {
        return PAGE_H - MARGIN - FOOTER_H;
    }

    private void ensureSpace(float needed) {
        if (y + needed > bottom()) newPage();
    }

    // ==================== BLOCOS ====================

    private void drawCover() {
        canvas.drawText("Diário de Saúde", MARGIN, y + 18, title);
        y += 26;
        canvas.drawText("Relatório para levar à consulta", MARGIN, y + 10, small);
        y += 24;

        String counts = data.totalRecords() + " registros (" + data.measurementCount + " medições, "
            + data.symptomCount + " sintomas, " + data.intakeCount + " confirmações de remédio)";
        keyValue("Paciente", data.patientName);
        keyValue("Período", data.periodText);
        keyValue("Total", counts);
        keyValue("Emitido em", data.issuedText);
        keyValue("Condições informadas", data.conditionsText);
        if (data.doctorNotes != null && !data.doctorNotes.isEmpty()) {
            keyValue("Observações do profissional", data.doctorNotes);
        }
        y += 6;

        List<String> lines = wrap(ReportData.DISCLAIMER, body, CONTENT_W - 20);
        float boxH = lines.size() * 13 + 14;
        Paint fill = new Paint();
        fill.setColor(Color.rgb(254, 245, 220));
        canvas.drawRect(MARGIN, y, PAGE_W - MARGIN, y + boxH, fill);
        float ty = y + 16;
        for (String l : lines) {
            canvas.drawText(l, MARGIN + 10, ty, body);
            ty += 13;
        }
        y += boxH + 10;
    }

    private void keyValue(String key, String value) {
        float keyW = 150;
        List<String> lines = wrap(value, body, CONTENT_W - keyW);
        ensureSpace(lines.size() * 13 + 4);
        canvas.drawText(key, MARGIN, y + 10, bold);
        for (String l : lines) {
            canvas.drawText(l, MARGIN + keyW, y + 10, body);
            y += 13;
        }
        y += 3;
    }

    private void heading(String text) {
        ensureSpace(60);
        y += 12;
        canvas.drawText(text, MARGIN, y + 12, h2);
        y += 20;
    }

    private void paragraph(String text, Paint paint) {
        for (String l : wrap(text, paint, CONTENT_W)) {
            ensureSpace(13);
            canvas.drawText(l, MARGIN, y + 10, paint);
            y += 12;
        }
        y += 4;
    }

    private void drawIndicator(ReportData.IndicatorSection section) {
        heading(section.title);
        for (String s : section.statsLines) paragraph(s, small);
        ensureSpace(CHART_H + 30);
        drawChart(section);
        table(section.headers, new float[]{0.17f, 0.22f, 0.15f, 0.2f, 0.26f}, section.rows);
    }

    // ==================== GRÁFICO ====================

    private void drawChart(ReportData.IndicatorSection section) {
        long minT = Long.MAX_VALUE;
        long maxT = Long.MIN_VALUE;
        double minV = Double.MAX_VALUE;
        double maxV = -Double.MAX_VALUE;
        for (ReportData.Series s : section.series) {
            for (int i = 0; i < s.times.size(); i++) {
                Double v = s.values.get(i);
                if (v == null) continue;
                minT = Math.min(minT, s.times.get(i));
                maxT = Math.max(maxT, s.times.get(i));
                minV = Math.min(minV, v);
                maxV = Math.max(maxV, v);
            }
        }
        if (minT == Long.MAX_VALUE) return;
        if (maxT == minT) {
            minT -= 3_600_000L;
            maxT += 3_600_000L;
        }
        double pad = Math.max((maxV - minV) * 0.15, Math.max(1, Math.abs(maxV) * 0.05));
        minV = Math.max(0, minV - pad);
        maxV = maxV + pad;

        // Legenda
        float lx = MARGIN;
        float legendY = y + 8;
        for (int i = 0; i < section.series.size(); i++) {
            Paint p = seriesPaint(i);
            drawMarker(i, lx + 4, legendY - 3);
            canvas.drawLine(lx - 2, legendY - 3, lx + 10, legendY - 3, p);
            String label = section.series.get(i).name + " (" + section.unit + ")";
            canvas.drawText(label, lx + 16, legendY, small);
            lx += 26 + small.measureText(label);
        }
        y += 16;

        float left = MARGIN + 34;
        float right = PAGE_W - MARGIN - 6;
        float top = y + 4;
        float bottomY = y + CHART_H - 22;

        Paint grid = stroke(0.4f, COLOR_LINE);
        Paint axisLabel = new Paint(small);
        axisLabel.setTextAlign(Paint.Align.RIGHT);
        for (int i = 0; i <= 4; i++) {
            double v = minV + (maxV - minV) * i / 4.0;
            float gy = (float) (bottomY - (bottomY - top) * i / 4.0);
            canvas.drawLine(left, gy, right, gy, grid);
            canvas.drawText(ReportBuilder.number(v, maxV - minV < 10), left - 4, gy + 3, axisLabel);
        }
        canvas.drawLine(left, top, left, bottomY, line);
        canvas.drawLine(left, bottomY, right, bottomY, line);

        SimpleDateFormat dayFormat = new SimpleDateFormat("dd/MM", new Locale("pt", "BR"));
        long[] ticks = {minT, minT + (maxT - minT) / 2, maxT};
        Paint center = new Paint(small);
        center.setTextAlign(Paint.Align.CENTER);
        for (long t : ticks) {
            float tx = (float) (left + (right - left) * (t - minT) / (double) (maxT - minT));
            canvas.drawText(dayFormat.format(new Date(t)), tx, bottomY + 12, center);
        }

        for (int si = 0; si < section.series.size(); si++) {
            ReportData.Series s = section.series.get(si);
            Path path = new Path();
            boolean started = false;
            List<float[]> points = new ArrayList<>();
            for (int i = 0; i < s.times.size(); i++) {
                Double v = s.values.get(i);
                if (v == null) continue;
                float px = (float) (left + (right - left) * (s.times.get(i) - minT) / (double) (maxT - minT));
                float py = (float) (bottomY - (bottomY - top) * (v - minV) / (maxV - minV));
                if (!started) {
                    path.moveTo(px, py);
                    started = true;
                } else {
                    path.lineTo(px, py);
                }
                points.add(new float[]{px, py});
            }
            canvas.drawPath(path, seriesPaint(si));
            for (float[] p : points) drawMarker(si, p[0], p[1]);
        }
        y += CHART_H;
    }

    private Paint seriesPaint(int index) {
        Paint p = stroke(1.4f, SERIES_COLORS[index % SERIES_COLORS.length]);
        if (index % 2 == 1) p.setPathEffect(new DashPathEffect(new float[]{4, 3}, 0));
        return p;
    }

    /** Marcadores diferentes por série para não depender só da cor. */
    private void drawMarker(int index, float x, float cy) {
        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setColor(SERIES_COLORS[index % SERIES_COLORS.length]);
        if (index % 2 == 0) {
            canvas.drawCircle(x, cy, 2.6f, fill);
        } else {
            canvas.drawRect(x - 2.4f, cy - 2.4f, x + 2.4f, cy + 2.4f, fill);
        }
    }

    // ==================== TABELA ====================

    private void table(String[] headers, float[] widths, List<String[]> rows) {
        float[] colW = new float[widths.length];
        for (int i = 0; i < widths.length; i++) colW[i] = widths[i] * CONTENT_W;
        drawRow(headers, colW, true);
        for (String[] row : rows) {
            float h = rowHeight(row, colW, cell);
            if (y + h > bottom()) {
                newPage();
                drawRow(headers, colW, true);
            }
            drawRow(row, colW, false);
        }
        y += 8;
    }

    private float rowHeight(String[] row, float[] colW, Paint paint) {
        int maxLines = 1;
        for (int i = 0; i < row.length; i++) {
            maxLines = Math.max(maxLines, wrap(row[i], paint, colW[i] - 6).size());
        }
        return maxLines * 10.5f + 7;
    }

    private void drawRow(String[] row, float[] colW, boolean header) {
        Paint paint = header ? cellBold : cell;
        float h = rowHeight(row, colW, paint);
        ensureSpace(h);
        if (header) {
            Paint bg = new Paint();
            bg.setColor(COLOR_HEADER_BG);
            canvas.drawRect(MARGIN, y, PAGE_W - MARGIN, y + h, bg);
        }
        float x = MARGIN;
        for (int i = 0; i < row.length; i++) {
            float ty = y + 11;
            for (String l : wrap(row[i], paint, colW[i] - 6)) {
                canvas.drawText(l, x + 3, ty, paint);
                ty += 10.5f;
            }
            x += colW[i];
        }
        y += h;
        canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, line);
    }

    // ==================== TEXTO ====================

    static List<String> wrap(String text, Paint paint, float width) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            lines.add("");
            return lines;
        }
        for (String paragraph : text.split("\n")) {
            StringBuilder current = new StringBuilder();
            for (String word : paragraph.split(" ")) {
                String candidate = current.length() == 0 ? word : current + " " + word;
                if (paint.measureText(candidate) <= width) {
                    current.setLength(0);
                    current.append(candidate);
                    continue;
                }
                if (current.length() > 0) {
                    lines.add(current.toString());
                    current.setLength(0);
                }
                // Palavra maior que a coluna: quebra por caracteres.
                String rest = word;
                while (paint.measureText(rest) > width && rest.length() > 1) {
                    int n = paint.breakText(rest, true, width, null);
                    n = Math.max(1, n);
                    lines.add(rest.substring(0, n));
                    rest = rest.substring(n);
                }
                current.append(rest);
            }
            lines.add(current.toString());
        }
        return lines;
    }

    private static Paint text(float size, boolean isBold, int color) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTextSize(size);
        p.setColor(color);
        p.setTypeface(isBold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        return p;
    }

    private static Paint stroke(float width, int color) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(width);
        p.setColor(color);
        return p;
    }
}
