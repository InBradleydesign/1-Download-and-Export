package com.cuidarapp.service;

import com.cuidarapp.model.CheckInDiario;
import com.cuidarapp.model.Exercicio;
import com.cuidarapp.repository.CheckInDiarioRepository;
import com.cuidarapp.repository.ExercicioRepository;

import java.time.LocalDate;
import java.util.List;

public class ExercicioService {
    
    private final ExercicioRepository exercicioRepository;
    private final CheckInDiarioRepository checkInDiarioRepository;
    
    public ExercicioService(ExercicioRepository exercicioRepository,
                            CheckInDiarioRepository checkInDiarioRepository) {
        this.exercicioRepository = exercicioRepository;
        this.checkInDiarioRepository = checkInDiarioRepository;
    }
    
    public ServiceResult salvarExercicio(long idosoId, String nome, String descricao,
                                         int duracaoMinutos, int frequenciaSemanal,
                                         String diasSemana, String instrucoes) {
        if (nome == null || nome.trim().isEmpty()) {
            return new ServiceResult(false, "Nome do exercício não informado", null);
        }
        
        Exercicio exercicio = new Exercicio(idosoId, sanitizeText(nome), sanitizeText(descricao), duracaoMinutos);
        exercicio.setFrequenciaSemanal(frequenciaSemanal);
        exercicio.setDiasSemana(diasSemana);
        exercicio.setInstrucoes(sanitizeText(instrucoes));
        
        long id = exercicioRepository.insert(exercicio);
        exercicio.setId(id);
        
        return new ServiceResult(true, "Exercício cadastrado com sucesso", id);
    }
    
    public ServiceResult atualizarExercicio(long exercicioId, String nome, String descricao,
                                            int duracaoMinutos, int frequenciaSemanal,
                                            String diasSemana, String instrucoes, boolean ativo) {
        Exercicio exercicio = exercicioRepository.getById(exercicioId);
        if (exercicio == null) {
            return new ServiceResult(false, "Exercício não encontrado", null);
        }
        
        if (nome == null || nome.trim().isEmpty()) {
            return new ServiceResult(false, "Nome do exercício não informado", null);
        }
        
        exercicio.setNome(sanitizeText(nome));
        exercicio.setDescricao(sanitizeText(descricao));
        exercicio.setDuracaoMinutos(duracaoMinutos);
        exercicio.setFrequenciaSemanal(frequenciaSemanal);
        exercicio.setDiasSemana(diasSemana);
        exercicio.setInstrucoes(sanitizeText(instrucoes));
        exercicio.setAtivo(ativo);
        
        exercicioRepository.update(exercicio);
        
        return new ServiceResult(true, "Exercício atualizado com sucesso", exercicioId);
    }
    
    public ServiceResult excluirExercicio(long exercicioId) {
        Exercicio exercicio = exercicioRepository.getById(exercicioId);
        if (exercicio == null) {
            return new ServiceResult(false, "Exercício não encontrado", null);
        }
        
        exercicioRepository.deleteById(exercicioId);
        return new ServiceResult(true, "Exercício excluído com sucesso", exercicioId);
    }
    
    public List<Exercicio> getExercicios(long idosoId) {
        return exercicioRepository.getByIdosoId(idosoId);
    }
    
    public List<Exercicio> getExerciciosAtivos(long idosoId) {
        return exercicioRepository.getAtivosByIdosoId(idosoId);
    }
    
    public Exercicio getExercicio(long exercicioId) {
        return exercicioRepository.getById(exercicioId);
    }
    
    public ServiceResult confirmarExercicio(long idosoId, long exercicioId, String observacoes) {
        Exercicio exercicio = exercicioRepository.getById(exercicioId);
        if (exercicio == null) {
            return new ServiceResult(false, "Exercício não encontrado", null);
        }
        
        if (exercicio.getIdosoId() != idosoId) {
            return new ServiceResult(false, "Exercício não pertence a este idoso", null);
        }
        
        LocalDate hoje = LocalDate.now();
        CheckInDiario existente = checkInDiarioRepository
            .getByIdosoIdTipoReferenciaAndData(idosoId, "exercicio", exercicioId, hoje);
        
        if (existente != null && existente.isConcluido()) {
            return new ServiceResult(false, "Exercício já confirmado hoje", existente.getId());
        }
        
        CheckInDiario checkIn;
        if (existente != null) {
            checkIn = existente;
        } else {
            checkIn = new CheckInDiario(idosoId, "exercicio");
            checkIn.setReferenciaId(exercicioId);
        }
        
        checkIn.setConcluido(true);
        checkIn.setObservacoes(sanitizeText(observacoes));
        
        if (existente != null) {
            checkInDiarioRepository.update(checkIn);
        } else {
            long id = checkInDiarioRepository.insert(checkIn);
            checkIn.setId(id);
        }
        
        return new ServiceResult(true, "Exercício concluído com sucesso", checkIn.getId());
    }
    
    public boolean isExercicioConcluidoHoje(long idosoId, long exercicioId) {
        CheckInDiario checkIn = checkInDiarioRepository
            .getByIdosoIdTipoReferenciaAndData(idosoId, "exercicio", exercicioId, LocalDate.now());
        return checkIn != null && checkIn.isConcluido();
    }
    
    public int countExerciciosConcluidos(long idosoId, LocalDate dataInicio, LocalDate dataFim) {
        return checkInDiarioRepository.countExerciciosConcluidosByIdosoIdAndPeriodo(idosoId, dataInicio, dataFim);
    }
    
    private String sanitizeText(String text) {
        if (text == null) return null;
        return text.trim()
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }
    
    public static class ServiceResult {
        private final boolean success;
        private final String message;
        private final Object data;
        
        public ServiceResult(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public Object getData() {
            return data;
        }
    }
}
