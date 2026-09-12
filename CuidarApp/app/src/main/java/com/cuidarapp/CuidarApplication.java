package com.cuidarapp;

import android.app.Application;

import com.cuidarapp.BuildConfig;
import com.cuidarapp.data.AppDatabase;
import com.cuidarapp.repository.*;
import com.cuidarapp.service.*;

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
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        initDatabase();
        initRepositories();
        initServices();
        
        if (BuildConfig.DEBUG) {
            initDemoData();
        }
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
            DemoDataInitializer.init(this);
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
