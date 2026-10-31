package com.cuidarapp.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.cuidarapp.data.dao.AlertaEmergenciaDao;
import com.cuidarapp.data.dao.CheckInDiarioDao;
import com.cuidarapp.data.dao.CuidadorDao;
import com.cuidarapp.data.dao.DietaSemanalDao;
import com.cuidarapp.data.dao.ExercicioDao;
import com.cuidarapp.data.dao.IdosoDao;
import com.cuidarapp.data.dao.MedicamentoDao;
import com.cuidarapp.data.dao.RegistroRefeicaoDao;
import com.cuidarapp.data.dao.VisitaDao;
import com.cuidarapp.health.data.HealthDao;
import com.cuidarapp.health.data.HealthMigrations;
import com.cuidarapp.health.data.ReminderDao;
import com.cuidarapp.health.model.HealthMeasurement;
import com.cuidarapp.health.model.HealthProfile;
import com.cuidarapp.health.model.HealthReminder;
import com.cuidarapp.health.model.MedicationIntake;
import com.cuidarapp.health.model.ReminderEvent;
import com.cuidarapp.health.model.SymptomRecord;
import com.cuidarapp.model.AlertaEmergencia;
import com.cuidarapp.model.CheckInDiario;
import com.cuidarapp.model.Cuidador;
import com.cuidarapp.model.DietaSemanal;
import com.cuidarapp.model.Exercicio;
import com.cuidarapp.model.Idoso;
import com.cuidarapp.model.Medicamento;
import com.cuidarapp.model.RegistroRefeicao;
import com.cuidarapp.model.Visita;

@Database(
    entities = {
        Cuidador.class,
        Idoso.class,
        DietaSemanal.class,
        RegistroRefeicao.class,
        Medicamento.class,
        Exercicio.class,
        CheckInDiario.class,
        Visita.class,
        AlertaEmergencia.class,
        HealthProfile.class,
        HealthMeasurement.class,
        SymptomRecord.class,
        MedicationIntake.class,
        HealthReminder.class,
        ReminderEvent.class
    },
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {
    
    private static volatile AppDatabase INSTANCE;
    public static final String DATABASE_NAME = "cuidar_app_database";
    
    public abstract CuidadorDao cuidadorDao();
    public abstract IdosoDao idosoDao();
    public abstract DietaSemanalDao dietaSemanalDao();
    public abstract RegistroRefeicaoDao registroRefeicaoDao();
    public abstract MedicamentoDao medicamentoDao();
    public abstract ExercicioDao exercicioDao();
    public abstract CheckInDiarioDao checkInDiarioDao();
    public abstract VisitaDao visitaDao();
    public abstract AlertaEmergenciaDao alertaEmergenciaDao();
    public abstract HealthDao healthDao();
    public abstract ReminderDao reminderDao();
    
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        DATABASE_NAME
                    )
                    .addMigrations(HealthMigrations.MIGRATION_1_2)
                    .allowMainThreadQueries()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
    
    public static void destroyInstance() {
        INSTANCE = null;
    }
}
