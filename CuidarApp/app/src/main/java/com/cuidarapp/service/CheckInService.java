package com.cuidarapp.service;

import com.cuidarapp.model.CheckInDiario;
import com.cuidarapp.repository.CheckInDiarioRepository;

import java.time.LocalDate;
import java.util.List;

public class CheckInService {
    
    private final CheckInDiarioRepository checkInDiarioRepository;
    
    public CheckInService(CheckInDiarioRepository checkInDiarioRepository) {
        this.checkInDiarioRepository = checkInDiarioRepository;
    }
    
    public ServiceResult registrarEstado(long idosoId, String estadoHumor, String estadoFisico, String observacoes) {
        if (estadoHumor == null || estadoHumor.trim().isEmpty()) {
            return new ServiceResult(false, "Estado de humor não informado", null);
        }
        
        if (estadoFisico == null || estadoFisico.trim().isEmpty()) {
            return new ServiceResult(false, "Estado físico não informado", null);
        }
        
        if (!isEstadoValido(estadoHumor) || !isEstadoValido(estadoFisico)) {
            return new ServiceResult(false, "Estado inválido", null);
        }
        
        CheckInDiario checkIn = new CheckInDiario(idosoId, "estado");
        checkIn.setEstadoHumor(estadoHumor);
        checkIn.setEstadoFisico(estadoFisico);
        checkIn.setObservacoes(sanitizeText(observacoes));
        checkIn.setConcluido(true);
        
        long id = checkInDiarioRepository.insert(checkIn);
        checkIn.setId(id);
        
        return new ServiceResult(true, "Estado registrado com sucesso", id);
    }
    
    public List<CheckInDiario> getCheckInsHoje(long idosoId) {
        return checkInDiarioRepository.getByIdosoIdAndData(idosoId, LocalDate.now());
    }
    
    public List<CheckInDiario> getCheckInsPeriodo(long idosoId, LocalDate dataInicio, LocalDate dataFim) {
        return checkInDiarioRepository.getByIdosoIdAndPeriodo(idosoId, dataInicio, dataFim);
    }
    
    public CheckInDiario getEstadoHoje(long idosoId) {
        List<CheckInDiario> estados = checkInDiarioRepository
            .getByIdosoIdTipoAndData(idosoId, "estado", LocalDate.now());
        return estados.isEmpty() ? null : estados.get(0);
    }
    
    public int countEstadosPeriodo(long idosoId, LocalDate dataInicio, LocalDate dataFim) {
        return checkInDiarioRepository.countEstadosByIdosoIdAndPeriodo(idosoId, dataInicio, dataFim);
    }
    
    private boolean isEstadoValido(String estado) {
        return estado.equals("otimo") || estado.equals("bem") || 
               estado.equals("regular") || estado.equals("mal") || 
               estado.equals("muito_mal");
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
