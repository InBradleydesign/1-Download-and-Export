package com.cuidarapp.service;

import com.cuidarapp.model.AlertaEmergencia;
import com.cuidarapp.model.Idoso;
import com.cuidarapp.model.StatusAlerta;
import com.cuidarapp.repository.AlertaEmergenciaRepository;
import com.cuidarapp.repository.IdosoRepository;

import java.util.List;

public class EmergenciaService {
    
    private final AlertaEmergenciaRepository alertaEmergenciaRepository;
    private final IdosoRepository idosoRepository;
    
    public EmergenciaService(AlertaEmergenciaRepository alertaEmergenciaRepository,
                             IdosoRepository idosoRepository) {
        this.alertaEmergenciaRepository = alertaEmergenciaRepository;
        this.idosoRepository = idosoRepository;
    }
    
    public ServiceResult criarAlerta(long idosoId, String descricao) {
        Idoso idoso = idosoRepository.getById(idosoId);
        if (idoso == null) {
            return new ServiceResult(false, "Idoso não encontrado", null);
        }
        
        AlertaEmergencia alerta = new AlertaEmergencia(idosoId, sanitizeText(descricao));
        
        long id = alertaEmergenciaRepository.insert(alerta);
        alerta.setId(id);
        
        return new ServiceResult(true, "Alerta de emergência registrado", id);
    }
    
    public ServiceResult registrarDiscagem(long alertaId, String telefone) {
        AlertaEmergencia alerta = alertaEmergenciaRepository.getById(alertaId);
        if (alerta == null) {
            return new ServiceResult(false, "Alerta não encontrado", null);
        }
        
        alerta.setTelefoneDiscado(telefone);
        alertaEmergenciaRepository.update(alerta);
        
        return new ServiceResult(true, "Discagem registrada", alertaId);
    }
    
    public ServiceResult marcarComoAtendido(long alertaId) {
        AlertaEmergencia alerta = alertaEmergenciaRepository.getById(alertaId);
        if (alerta == null) {
            return new ServiceResult(false, "Alerta não encontrado", null);
        }
        
        alertaEmergenciaRepository.updateStatus(alertaId, StatusAlerta.ATENDIDO);
        
        return new ServiceResult(true, "Alerta marcado como atendido", alertaId);
    }
    
    public ServiceResult cancelarAlerta(long alertaId) {
        AlertaEmergencia alerta = alertaEmergenciaRepository.getById(alertaId);
        if (alerta == null) {
            return new ServiceResult(false, "Alerta não encontrado", null);
        }
        
        alertaEmergenciaRepository.updateStatus(alertaId, StatusAlerta.CANCELADO);
        
        return new ServiceResult(true, "Alerta cancelado", alertaId);
    }
    
    public List<AlertaEmergencia> getAlertasAtivos(long idosoId) {
        return alertaEmergenciaRepository.getAtivosByIdosoId(idosoId);
    }
    
    public List<AlertaEmergencia> getHistoricoAlertas(long idosoId) {
        return alertaEmergenciaRepository.getByIdosoId(idosoId);
    }
    
    public String getTelefoneEmergencia(long idosoId) {
        Idoso idoso = idosoRepository.getById(idosoId);
        return idoso != null ? idoso.getTelefoneEmergencia() : "192";
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
