package com.cuidarapp.service;

import com.cuidarapp.model.StatusVisita;
import com.cuidarapp.model.Visita;
import com.cuidarapp.repository.VisitaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class VisitaService {
    
    private final VisitaRepository visitaRepository;
    
    public VisitaService(VisitaRepository visitaRepository) {
        this.visitaRepository = visitaRepository;
    }
    
    public ServiceResult agendarVisita(long idosoId, long cuidadorId, String data, 
                                       String horaInicio, String horaFim, String descricao) {
        if (data == null || data.trim().isEmpty()) {
            return new ServiceResult(false, "Data não informada", null);
        }
        
        LocalDate dataVisita;
        try {
            dataVisita = LocalDate.parse(data);
        } catch (Exception e) {
            return new ServiceResult(false, "Data inválida", null);
        }
        
        if (dataVisita.isBefore(LocalDate.now())) {
            return new ServiceResult(false, "Data não pode ser no passado", null);
        }
        
        LocalTime horaInicioLocal = null;
        if (horaInicio != null && !horaInicio.trim().isEmpty()) {
            try {
                horaInicioLocal = LocalTime.parse(horaInicio);
            } catch (Exception e) {
                return new ServiceResult(false, "Hora de início inválida", null);
            }
        }
        
        LocalTime horaFimLocal = null;
        if (horaFim != null && !horaFim.trim().isEmpty()) {
            try {
                horaFimLocal = LocalTime.parse(horaFim);
            } catch (Exception e) {
                return new ServiceResult(false, "Hora de fim inválida", null);
            }
        }
        
        Visita visita = new Visita(idosoId, cuidadorId, dataVisita, horaInicioLocal, sanitizeText(descricao));
        visita.setHoraFim(horaFimLocal);
        
        long id = visitaRepository.insert(visita);
        visita.setId(id);
        
        return new ServiceResult(true, "Visita agendada com sucesso", id);
    }
    
    public ServiceResult atualizarVisita(long visitaId, String data, String horaInicio, 
                                         String horaFim, String descricao, String observacoes) {
        Visita visita = visitaRepository.getById(visitaId);
        if (visita == null) {
            return new ServiceResult(false, "Visita não encontrada", null);
        }
        
        if (data != null && !data.trim().isEmpty()) {
            try {
                visita.setData(LocalDate.parse(data));
            } catch (Exception e) {
                return new ServiceResult(false, "Data inválida", null);
            }
        }
        
        if (horaInicio != null && !horaInicio.trim().isEmpty()) {
            try {
                visita.setHoraInicio(LocalTime.parse(horaInicio));
            } catch (Exception e) {
                return new ServiceResult(false, "Hora de início inválida", null);
            }
        }
        
        if (horaFim != null && !horaFim.trim().isEmpty()) {
            try {
                visita.setHoraFim(LocalTime.parse(horaFim));
            } catch (Exception e) {
                return new ServiceResult(false, "Hora de fim inválida", null);
            }
        }
        
        visita.setDescricao(sanitizeText(descricao));
        visita.setObservacoes(sanitizeText(observacoes));
        
        visitaRepository.update(visita);
        
        return new ServiceResult(true, "Visita atualizada com sucesso", visitaId);
    }
    
    public ServiceResult atualizarStatus(long visitaId, StatusVisita status) {
        Visita visita = visitaRepository.getById(visitaId);
        if (visita == null) {
            return new ServiceResult(false, "Visita não encontrada", null);
        }
        
        visitaRepository.updateStatus(visitaId, status);
        
        return new ServiceResult(true, "Status atualizado com sucesso", visitaId);
    }
    
    public ServiceResult excluirVisita(long visitaId) {
        Visita visita = visitaRepository.getById(visitaId);
        if (visita == null) {
            return new ServiceResult(false, "Visita não encontrada", null);
        }
        
        visitaRepository.deleteById(visitaId);
        return new ServiceResult(true, "Visita excluída com sucesso", visitaId);
    }
    
    public List<Visita> getVisitasByIdoso(long idosoId) {
        return visitaRepository.getByIdosoId(idosoId);
    }
    
    public List<Visita> getVisitasByCuidador(long cuidadorId) {
        return visitaRepository.getByCuidadorId(cuidadorId);
    }
    
    public List<Visita> getProximasVisitas(long idosoId) {
        return visitaRepository.getProximasByIdosoId(idosoId);
    }
    
    public Visita getVisita(long visitaId) {
        return visitaRepository.getById(visitaId);
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
