package com.cuidarapp.service;

import com.cuidarapp.model.DietaSemanal;
import com.cuidarapp.model.RegistroRefeicao;
import com.cuidarapp.repository.DietaSemanalRepository;
import com.cuidarapp.repository.RegistroRefeicaoRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class DietaService {
    
    private final DietaSemanalRepository dietaSemanalRepository;
    private final RegistroRefeicaoRepository registroRefeicaoRepository;
    
    public DietaService(DietaSemanalRepository dietaSemanalRepository,
                        RegistroRefeicaoRepository registroRefeicaoRepository) {
        this.dietaSemanalRepository = dietaSemanalRepository;
        this.registroRefeicaoRepository = registroRefeicaoRepository;
    }
    
    public ServiceResult salvarDieta(long idosoId, int diaSemana, String cafeManha, String lancheManha,
                                     String almoco, String lancheTarde, String jantar, String ceia,
                                     String observacoes) {
        if (diaSemana < 1 || diaSemana > 7) {
            return new ServiceResult(false, "Dia da semana inválido", null);
        }
        
        DietaSemanal dieta = dietaSemanalRepository.getByIdosoIdAndDia(idosoId, diaSemana);
        
        if (dieta == null) {
            dieta = new DietaSemanal(idosoId, diaSemana);
        }
        
        dieta.setCafeManha(sanitizeText(cafeManha));
        dieta.setLancheManha(sanitizeText(lancheManha));
        dieta.setAlmoco(sanitizeText(almoco));
        dieta.setLancheTarde(sanitizeText(lancheTarde));
        dieta.setJantar(sanitizeText(jantar));
        dieta.setCeia(sanitizeText(ceia));
        dieta.setObservacoes(sanitizeText(observacoes));
        
        if (dieta.getId() == 0) {
            long id = dietaSemanalRepository.insert(dieta);
            dieta.setId(id);
        } else {
            dietaSemanalRepository.update(dieta);
        }
        
        return new ServiceResult(true, "Dieta salva com sucesso", dieta.getId());
    }
    
    public List<DietaSemanal> getDietaSemanal(long idosoId) {
        return dietaSemanalRepository.getByIdosoId(idosoId);
    }
    
    public DietaSemanal getDietaDia(long idosoId, int diaSemana) {
        return dietaSemanalRepository.getByIdosoIdAndDia(idosoId, diaSemana);
    }
    
    public DietaSemanal getDietaHoje(long idosoId) {
        int diaSemana = LocalDate.now().getDayOfWeek().getValue();
        return dietaSemanalRepository.getByIdosoIdAndDia(idosoId, diaSemana);
    }
    
    public ServiceResult excluirDieta(long dietaId) {
        DietaSemanal dieta = dietaSemanalRepository.getById(dietaId);
        if (dieta == null) {
            return new ServiceResult(false, "Dieta não encontrada", null);
        }
        
        dietaSemanalRepository.deleteById(dietaId);
        return new ServiceResult(true, "Dieta excluída com sucesso", dietaId);
    }
    
    public ServiceResult registrarRefeicao(long idosoId, String tipoRefeicao, String descricao, 
                                           String observacoes) {
        if (descricao == null || descricao.trim().isEmpty()) {
            return new ServiceResult(false, "Descrição da refeição não informada", null);
        }
        
        RegistroRefeicao registro = new RegistroRefeicao(idosoId, tipoRefeicao, sanitizeText(descricao));
        registro.setObservacoes(sanitizeText(observacoes));
        registro.setData(LocalDate.now());
        registro.setHora(LocalTime.now());
        
        long id = registroRefeicaoRepository.insert(registro);
        registro.setId(id);
        
        return new ServiceResult(true, "Refeição registrada com sucesso", id);
    }
    
    public List<RegistroRefeicao> getRegistrosHoje(long idosoId) {
        return registroRefeicaoRepository.getByIdosoIdAndData(idosoId, LocalDate.now());
    }
    
    public List<RegistroRefeicao> getRegistrosPeriodo(long idosoId, LocalDate dataInicio, LocalDate dataFim) {
        return registroRefeicaoRepository.getByIdosoIdAndPeriodo(idosoId, dataInicio, dataFim);
    }
    
    public int countRegistrosPeriodo(long idosoId, LocalDate dataInicio, LocalDate dataFim) {
        return registroRefeicaoRepository.countByIdosoIdAndPeriodo(idosoId, dataInicio, dataFim);
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
