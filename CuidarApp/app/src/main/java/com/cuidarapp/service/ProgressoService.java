package com.cuidarapp.service;

import com.cuidarapp.repository.CheckInDiarioRepository;
import com.cuidarapp.repository.MedicamentoRepository;
import com.cuidarapp.repository.ExercicioRepository;
import com.cuidarapp.repository.RegistroRefeicaoRepository;

import java.time.LocalDate;

public class ProgressoService {
    
    private final CheckInDiarioRepository checkInDiarioRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final ExercicioRepository exercicioRepository;
    private final RegistroRefeicaoRepository registroRefeicaoRepository;
    
    public ProgressoService(CheckInDiarioRepository checkInDiarioRepository,
                            MedicamentoRepository medicamentoRepository,
                            ExercicioRepository exercicioRepository,
                            RegistroRefeicaoRepository registroRefeicaoRepository) {
        this.checkInDiarioRepository = checkInDiarioRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.exercicioRepository = exercicioRepository;
        this.registroRefeicaoRepository = registroRefeicaoRepository;
    }
    
    public ProgressoResumo calcularProgresso(long idosoId, LocalDate dataInicio, LocalDate dataFim) {
        int refeicoesRegistradas = registroRefeicaoRepository
            .countByIdosoIdAndPeriodo(idosoId, dataInicio, dataFim);
        
        int medicamentosConfirmados = checkInDiarioRepository
            .countMedicamentosConfirmadosByIdosoIdAndPeriodo(idosoId, dataInicio, dataFim);
        
        int exerciciosConcluidos = checkInDiarioRepository
            .countExerciciosConcluidosByIdosoIdAndPeriodo(idosoId, dataInicio, dataFim);
        
        int checkInsRealizados = checkInDiarioRepository
            .countEstadosByIdosoIdAndPeriodo(idosoId, dataInicio, dataFim);
        
        int totalMedicamentosAtivos = medicamentoRepository.countAtivosByIdosoId(idosoId);
        int totalExerciciosAtivos = exercicioRepository.countAtivosByIdosoId(idosoId);
        
        long dias = java.time.temporal.ChronoUnit.DAYS.between(dataInicio, dataFim) + 1;
        
        int medicamentosEsperados = (int) (totalMedicamentosAtivos * dias);
        int exerciciosEsperados = (int) (totalExerciciosAtivos * dias);
        int refeicoesEsperadas = (int) (6 * dias);
        int checkInsEsperados = (int) dias;
        
        return new ProgressoResumo(
            refeicoesRegistradas,
            refeicoesEsperadas,
            medicamentosConfirmados,
            medicamentosEsperados,
            exerciciosConcluidos,
            exerciciosEsperados,
            checkInsRealizados,
            checkInsEsperados,
            dataInicio,
            dataFim
        );
    }
    
    public ProgressoResumo calcularProgressoSemana(long idosoId) {
        LocalDate hoje = LocalDate.now();
        LocalDate inicioSemana = hoje.minusDays(hoje.getDayOfWeek().getValue() - 1);
        return calcularProgresso(idosoId, inicioSemana, hoje);
    }
    
    public ProgressoResumo calcularProgressoMes(long idosoId) {
        LocalDate hoje = LocalDate.now();
        LocalDate inicioMes = hoje.withDayOfMonth(1);
        return calcularProgresso(idosoId, inicioMes, hoje);
    }
    
    public static class ProgressoResumo {
        private final int refeicoesRegistradas;
        private final int refeicoesEsperadas;
        private final int medicamentosConfirmados;
        private final int medicamentosEsperados;
        private final int exerciciosConcluidos;
        private final int exerciciosEsperados;
        private final int checkInsRealizados;
        private final int checkInsEsperados;
        private final LocalDate dataInicio;
        private final LocalDate dataFim;
        
        public ProgressoResumo(int refeicoesRegistradas, int refeicoesEsperadas,
                               int medicamentosConfirmados, int medicamentosEsperados,
                               int exerciciosConcluidos, int exerciciosEsperados,
                               int checkInsRealizados, int checkInsEsperados,
                               LocalDate dataInicio, LocalDate dataFim) {
            this.refeicoesRegistradas = refeicoesRegistradas;
            this.refeicoesEsperadas = refeicoesEsperadas;
            this.medicamentosConfirmados = medicamentosConfirmados;
            this.medicamentosEsperados = medicamentosEsperados;
            this.exerciciosConcluidos = exerciciosConcluidos;
            this.exerciciosEsperados = exerciciosEsperados;
            this.checkInsRealizados = checkInsRealizados;
            this.checkInsEsperados = checkInsEsperados;
            this.dataInicio = dataInicio;
            this.dataFim = dataFim;
        }
        
        public int getRefeicoesRegistradas() {
            return refeicoesRegistradas;
        }
        
        public int getRefeicoesEsperadas() {
            return refeicoesEsperadas;
        }
        
        public int getMedicamentosConfirmados() {
            return medicamentosConfirmados;
        }
        
        public int getMedicamentosEsperados() {
            return medicamentosEsperados;
        }
        
        public int getExerciciosConcluidos() {
            return exerciciosConcluidos;
        }
        
        public int getExerciciosEsperados() {
            return exerciciosEsperados;
        }
        
        public int getCheckInsRealizados() {
            return checkInsRealizados;
        }
        
        public int getCheckInsEsperados() {
            return checkInsEsperados;
        }
        
        public LocalDate getDataInicio() {
            return dataInicio;
        }
        
        public LocalDate getDataFim() {
            return dataFim;
        }
        
        public double getPercentualRefeicoes() {
            return refeicoesEsperadas > 0 ? 
                Math.min(100, (refeicoesRegistradas * 100.0) / refeicoesEsperadas) : 0;
        }
        
        public double getPercentualMedicamentos() {
            return medicamentosEsperados > 0 ? 
                Math.min(100, (medicamentosConfirmados * 100.0) / medicamentosEsperados) : 0;
        }
        
        public double getPercentualExercicios() {
            return exerciciosEsperados > 0 ? 
                Math.min(100, (exerciciosConcluidos * 100.0) / exerciciosEsperados) : 0;
        }
        
        public double getPercentualCheckIns() {
            return checkInsEsperados > 0 ? 
                Math.min(100, (checkInsRealizados * 100.0) / checkInsEsperados) : 0;
        }
    }
}
