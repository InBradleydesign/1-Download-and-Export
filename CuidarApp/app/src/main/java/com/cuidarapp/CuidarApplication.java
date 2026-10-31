package com.cuidarapp;

import android.app.Application;

import com.cuidarapp.data.AppDatabase;
import com.cuidarapp.health.HealthRepository;
import com.cuidarapp.health.reminders.ReminderNotifier;
import com.cuidarapp.health.reminders.ReminderScheduler;
import com.cuidarapp.repository.*;
import com.cuidarapp.service.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CuidarApplication extends Application {
    
    private static CuidarApplication instance;
    
    private AppDatabase database;
    
    private CuidadorRepository cuidadorRepository;
    private IdosoRepository idosoRepository;
    private DietaSemanalRepository dietaSemanalRepository;
    private RegistroRefeicaoRepository registroRefeicaoRepository;
    private MedicamentoRepository medicamentoRepository;
    private ExercicioRepository exercicioRepository;
    private CheckInDiarioRepository checkInDiarioRepository;
    private VisitaRepository visitaRepository;
    private AlertaEmergenciaRepository alertaEmergenciaRepository;
    
    private AuthService authService;
    private CuidadorService cuidadorService;
    private IdosoService idosoService;
    private DietaService dietaService;
    private MedicamentoService medicamentoService;
    private ExercicioService exercicioService;
    private VisitaService visitaService;
    private CheckInService checkInService;
    private EmergenciaService emergenciaService;
    private ProgressoService progressoService;

    private HealthRepository healthRepository;
    private ReminderScheduler reminderScheduler;
    private ReminderNotifier reminderNotifier;
    private final ExecutorService healthExecutor = Executors.newSingleThreadExecutor();
    private volatile Runnable healthChangedListener;
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        initDatabase();
        initRepositories();
        initServices();
        initHealthDiary();
        
        if (BuildConfig.DEBUG) {
            initDemoData();
        }
    }

    private void initHealthDiary() {
        healthRepository = new HealthRepository(database);
        reminderScheduler = new ReminderScheduler(this);
        reminderNotifier = new ReminderNotifier(this);
        reminderNotifier.ensureChannel();
        // Alarmes somem quando o app é forçado a parar; recalcula a cada abertura.
        healthExecutor.execute(() -> reminderScheduler.rescheduleAll(healthRepository));
    }

    public HealthRepository getHealthRepository() {
        return healthRepository;
    }

    public ReminderScheduler getReminderScheduler() {
        return reminderScheduler;
    }

    public ReminderNotifier getReminderNotifier() {
        return reminderNotifier;
    }

    public ExecutorService getHealthExecutor() {
        return healthExecutor;
    }

    public void setHealthChangedListener(Runnable listener) {
        healthChangedListener = listener;
    }

    /** Avisa a tela aberta que um lembrete disparou ou foi respondido pela notificação. */
    public void notifyHealthChanged() {
        Runnable listener = healthChangedListener;
        if (listener != null) listener.run();
    }
    
    private void initDatabase() {
        database = AppDatabase.getInstance(this);
    }
    
    private void initRepositories() {
        cuidadorRepository = new CuidadorRepository(database.cuidadorDao());
        idosoRepository = new IdosoRepository(database.idosoDao());
        dietaSemanalRepository = new DietaSemanalRepository(database.dietaSemanalDao());
        registroRefeicaoRepository = new RegistroRefeicaoRepository(database.registroRefeicaoDao());
        medicamentoRepository = new MedicamentoRepository(database.medicamentoDao());
        exercicioRepository = new ExercicioRepository(database.exercicioDao());
        checkInDiarioRepository = new CheckInDiarioRepository(database.checkInDiarioDao());
        visitaRepository = new VisitaRepository(database.visitaDao());
        alertaEmergenciaRepository = new AlertaEmergenciaRepository(database.alertaEmergenciaDao());
    }
    
    private void initServices() {
        authService = new AuthService(cuidadorRepository, idosoRepository);
        cuidadorService = new CuidadorService(cuidadorRepository, idosoRepository);
        idosoService = new IdosoService(idosoRepository);
        dietaService = new DietaService(dietaSemanalRepository, registroRefeicaoRepository);
        medicamentoService = new MedicamentoService(medicamentoRepository, checkInDiarioRepository);
        exercicioService = new ExercicioService(exercicioRepository, checkInDiarioRepository);
        visitaService = new VisitaService(visitaRepository);
        checkInService = new CheckInService(checkInDiarioRepository);
        emergenciaService = new EmergenciaService(alertaEmergenciaRepository, idosoRepository);
        progressoService = new ProgressoService(checkInDiarioRepository, medicamentoRepository, 
                                                 exercicioRepository, registroRefeicaoRepository);
    }
    
    private void initDemoData() {
        if (cuidadorRepository.count() == 0) {
            try {
                Class<?> clazz = Class.forName("com.cuidarapp.DemoDataInitializer");
                java.lang.reflect.Method method = clazz.getMethod("init", CuidarApplication.class);
                method.invoke(null, this);
            } catch (ClassNotFoundException e) {
                // DemoDataInitializer não existe em builds release - ignorar
            } catch (Exception e) {
                android.util.Log.e("CuidarApp", "Erro ao inicializar dados demo", e);
            }
        }
    }
    
    public static CuidarApplication getInstance() {
        return instance;
    }
    
    public AppDatabase getDatabase() {
        return database;
    }
    
    public AuthService getAuthService() {
        return authService;
    }
    
    public CuidadorService getCuidadorService() {
        return cuidadorService;
    }
    
    public IdosoService getIdosoService() {
        return idosoService;
    }
    
    public DietaService getDietaService() {
        return dietaService;
    }
    
    public MedicamentoService getMedicamentoService() {
        return medicamentoService;
    }
    
    public ExercicioService getExercicioService() {
        return exercicioService;
    }
    
    public VisitaService getVisitaService() {
        return visitaService;
    }
    
    public CheckInService getCheckInService() {
        return checkInService;
    }
    
    public EmergenciaService getEmergenciaService() {
        return emergenciaService;
    }
    
    public ProgressoService getProgressoService() {
        return progressoService;
    }
    
    public CuidadorRepository getCuidadorRepository() {
        return cuidadorRepository;
    }
    
    public IdosoRepository getIdosoRepository() {
        return idosoRepository;
    }
}
