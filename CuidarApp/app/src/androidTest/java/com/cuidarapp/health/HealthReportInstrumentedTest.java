package com.cuidarapp.health;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.net.Uri;

import androidx.core.content.FileProvider;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.cuidarapp.data.AppDatabase;
import com.cuidarapp.health.model.Author;
import com.cuidarapp.health.model.HealthMeasurement;
import com.cuidarapp.health.model.MeasurementType;
import com.cuidarapp.health.model.MedicationIntake;
import com.cuidarapp.health.model.SymptomRecord;
import com.cuidarapp.health.report.ReportService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** Gera o PDF de verdade no aparelho (PdfDocument + PdfRenderer) a partir de registros salvos. */
@RunWith(AndroidJUnit4.class)
public class HealthReportInstrumentedTest {

    private static final ZoneId SP = ZoneId.of("America/Sao_Paulo");
    private static final long P = 99L;
    private static final Author PATIENT = new Author(Author.ROLE_PATIENT, "José");

    private final Context context = ApplicationProvider.getApplicationContext();
    private AppDatabase db;
    private HealthRepository repo;

    private static long at(String isoLocal) {
        return LocalDateTime.parse(isoLocal).atZone(SP).toInstant().toEpochMilli();
    }

    @Before
    public void setUp() {
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).allowMainThreadQueries().build();
        repo = new HealthRepository(db, () -> at("2026-10-30T18:00"), SP);
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void generatesMultiPagePdfInPrivateCache_withPreview() throws Exception {
        repo.giveConsent(P, true, PATIENT);
        for (int day = 1; day <= 30; day++) {
            String d = String.format("2026-10-%02d", day);
            HealthMeasurement bp = new HealthMeasurement();
            bp.type = MeasurementType.BLOOD_PRESSURE;
            bp.value1 = 120 + day % 15;
            bp.value2 = 75.0 + day % 10;
            bp.value3 = 70.0;
            bp.measuredAt = at(d + "T08:00");
            repo.saveMeasurement(P, bp, PATIENT);

            HealthMeasurement g = new HealthMeasurement();
            g.type = MeasurementType.GLUCOSE;
            g.value1 = 95 + day;
            g.context = MeasurementType.CONTEXT_FASTING;
            g.measuredAt = at(d + "T07:00");
            g.note = day == 3 ? "Acordou com fome" : null;
            repo.saveMeasurement(P, g, PATIENT);
        }
        SymptomRecord s = new SymptomRecord();
        s.symptom = "headache";
        s.intensity = 2;
        s.occurredAt = at("2026-10-15T14:00");
        repo.saveSymptom(P, s, PATIENT);
        MedicationIntake i = new MedicationIntake();
        i.medRef = "1";
        i.medName = "Losartana";
        i.dosage = "50 mg";
        i.scheduledDate = "2026-10-30";
        i.scheduledTime = "08:00";
        i.status = MedicationIntake.STATUS_TAKEN;
        repo.recordIntake(P, i, PATIENT);

        ReportService.Result result = new ReportService(context, repo)
            .generate(P, "José da Silva", at("2026-10-01T00:00"), at("2026-10-30T23:59"));

        assertEquals(62, result.totalRecords);
        assertTrue("Páginas: " + result.pageCount, result.pageCount >= 3);
        assertEquals(result.pageCount, result.previewPages.size());
        assertTrue(result.previewPages.get(0).startsWith("data:image/jpeg;base64,"));

        File file = result.file;
        assertTrue(file.exists());
        assertTrue(file.getCanonicalPath().startsWith(context.getCacheDir().getCanonicalPath()));
        byte[] header = new byte[5];
        try (FileInputStream in = new FileInputStream(file)) {
            assertEquals(5, in.read(header));
        }
        assertEquals("%PDF-", new String(header, "US-ASCII"));

        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
        assertEquals("content", uri.getScheme());
    }

    @Test
    public void emptyPeriod_stillProducesHonestReport() throws Exception {
        repo.giveConsent(P, false, PATIENT);
        ReportService.Result result = new ReportService(context, repo)
            .generate(P, "José", at("2026-10-01T00:00"), at("2026-10-07T23:59"));
        assertEquals(0, result.totalRecords);
        assertEquals(1, result.pageCount);
    }
}
