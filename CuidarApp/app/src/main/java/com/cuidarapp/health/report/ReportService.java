package com.cuidarapp.health.report;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.util.Base64;

import com.cuidarapp.health.HealthRepository;
import com.cuidarapp.health.ProfileData;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Gera o PDF na pasta de cache interna do app (não acessível por outros apps) e cria a
 * pré-visualização. O arquivo só sai do aparelho se o usuário tocar em compartilhar.
 */
public class ReportService {

    public static final String REPORTS_DIR = "reports";
    private static final int PREVIEW_WIDTH = 900;
    private static final int MAX_PREVIEW_PAGES = 12;

    private final Context context;
    private final HealthRepository repository;

    public ReportService(Context context, HealthRepository repository) {
        this.context = context.getApplicationContext();
        this.repository = repository;
    }

    public static class Result {
        public File file;
        public int pageCount;
        public int totalRecords;
        public List<String> previewPages = new ArrayList<>();
    }

    public Result generate(long patientId, String patientName, long from, long to) throws IOException {
        ProfileData profile = repository.getProfile(patientId);
        long now = repository.now();
        ReportData data = new ReportBuilder(repository.zone()).build(patientName, profile, from, to, now,
            repository.listMeasurements(patientId, null, from, to),
            repository.listSymptoms(patientId, from, to),
            repository.listIntakes(patientId, repository.dateKey(from), repository.dateKey(to)));

        File dir = reportsDir(context);
        clear(dir);
        String stamp = new SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(new Date(now));
        File out = new File(dir, "diario-de-saude-" + stamp + ".pdf");
        new PdfReportRenderer().render(data, out);

        Result result = new Result();
        result.file = out;
        result.totalRecords = data.totalRecords();
        renderPreview(out, result);
        return result;
    }

    private void renderPreview(File pdf, Result result) throws IOException {
        try (ParcelFileDescriptor fd = ParcelFileDescriptor.open(pdf, ParcelFileDescriptor.MODE_READ_ONLY);
             PdfRenderer renderer = new PdfRenderer(fd)) {
            result.pageCount = renderer.getPageCount();
            for (int i = 0; i < Math.min(result.pageCount, MAX_PREVIEW_PAGES); i++) {
                try (PdfRenderer.Page page = renderer.openPage(i)) {
                    int height = Math.round(PREVIEW_WIDTH * page.getHeight() / (float) page.getWidth());
                    Bitmap bitmap = Bitmap.createBitmap(PREVIEW_WIDTH, height, Bitmap.Config.ARGB_8888);
                    new Canvas(bitmap).drawColor(Color.WHITE);
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 82, bytes);
                    bitmap.recycle();
                    result.previewPages.add("data:image/jpeg;base64," + Base64.encodeToString(bytes.toByteArray(), Base64.NO_WRAP));
                }
            }
        }
    }

    public static File reportsDir(Context context) {
        File dir = new File(context.getCacheDir(), REPORTS_DIR);
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    /** Remove relatórios antigos: só o último fica guardado, e apenas até o próximo. */
    public static void clear(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isFile()) f.delete();
        }
    }
}
