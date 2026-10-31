package com.cuidarapp.health.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.cuidarapp.data.AppDatabase;
import com.cuidarapp.health.HealthRepository;
import com.cuidarapp.health.model.Author;
import com.cuidarapp.health.model.HealthMeasurement;
import com.cuidarapp.health.model.MeasurementType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

/**
 * Cria um banco exatamente como a versão 1 (a partir do esquema exportado pelo Room) e abre
 * com a versão atual. O Room valida o esquema depois da migração e falha se algo divergir.
 */
@RunWith(RobolectricTestRunner.class)
@Config(application = Application.class, sdk = 34)
public class HealthMigrationTest {

    private static final String NAME = "migration-test.db";
    private final Context context = ApplicationProvider.getApplicationContext();
    private AppDatabase db;

    @After
    public void tearDown() {
        if (db != null) db.close();
        context.deleteDatabase(NAME);
    }

    private static JsonObject schema(int version) throws Exception {
        File file = new File("schemas/com.cuidarapp.data.AppDatabase/" + version + ".json");
        try (Reader reader = new FileReader(file)) {
            return JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("database");
        }
    }

    private void createVersion1() throws Exception {
        JsonObject v1 = schema(1);
        File path = context.getDatabasePath(NAME);
        path.getParentFile().mkdirs();
        SQLiteDatabase raw = SQLiteDatabase.openOrCreateDatabase(path, null);
        for (JsonElement e : v1.getAsJsonArray("entities")) {
            JsonObject entity = e.getAsJsonObject();
            String table = entity.get("tableName").getAsString();
            raw.execSQL(entity.get("createSql").getAsString().replace("${TABLE_NAME}", table));
            JsonArray indices = entity.getAsJsonArray("indices");
            if (indices != null) {
                for (JsonElement i : indices) {
                    raw.execSQL(i.getAsJsonObject().get("createSql").getAsString().replace("${TABLE_NAME}", table));
                }
            }
        }
        for (JsonElement q : v1.getAsJsonArray("setupQueries")) raw.execSQL(q.getAsString());
        raw.execSQL("INSERT INTO cuidadores (id, nome, tamanho_fonte, tipo_perfil) VALUES (1, 'Ana', 1.0, 'CUIDADOR')");
        raw.setVersion(1);
        raw.close();
    }

    @Test
    public void migrate1To2_keepsOldDataAndCreatesHealthTables() throws Exception {
        createVersion1();

        db = Room.databaseBuilder(context, AppDatabase.class, NAME)
            .addMigrations(HealthMigrations.MIGRATION_1_2)
            .allowMainThreadQueries()
            .build();

        try (Cursor c = db.query("SELECT nome FROM cuidadores WHERE id = 1", null)) {
            assertTrue(c.moveToFirst());
            assertEquals("Ana", c.getString(0));
        }
        assertEquals(2, db.getOpenHelper().getReadableDatabase().getVersion());

        HealthRepository repo = new HealthRepository(db);
        repo.giveConsent(1, false, new Author(Author.ROLE_PATIENT, "José"));
        HealthMeasurement m = new HealthMeasurement();
        m.type = MeasurementType.BLOOD_PRESSURE;
        m.value1 = 120;
        m.value2 = 80.0;
        m.measuredAt = System.currentTimeMillis() - 60_000;
        repo.saveMeasurement(1, m, new Author(Author.ROLE_PATIENT, "José"));
        assertEquals(1, repo.listMeasurements(1, null, 0, Long.MAX_VALUE).size());
    }

    @Test
    public void migrationSql_matchesExportedSchema() throws Exception {
        JsonObject v2 = schema(2);
        int expected = 0;
        for (JsonElement e : v2.getAsJsonArray("entities")) {
            JsonObject entity = e.getAsJsonObject();
            String table = entity.get("tableName").getAsString();
            boolean isHealth = table.startsWith("health_") || table.equals("medication_intakes") || table.equals("reminder_events");
            if (!isHealth) continue;
            String create = entity.get("createSql").getAsString().replace("${TABLE_NAME}", table);
            assertTrue("Sem CREATE para " + table, java.util.Arrays.asList(HealthMigrations.CREATE_V2).contains(create));
            expected++;
            JsonArray indices = entity.getAsJsonArray("indices");
            if (indices != null) {
                for (JsonElement i : indices) {
                    String sql = i.getAsJsonObject().get("createSql").getAsString().replace("${TABLE_NAME}", table);
                    assertTrue("Sem índice: " + sql, java.util.Arrays.asList(HealthMigrations.CREATE_V2).contains(sql));
                    expected++;
                }
            }
        }
        assertEquals(6 + 7, expected);
        assertEquals(expected, HealthMigrations.CREATE_V2.length);
    }
}
