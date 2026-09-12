package com.cuidarapp.service;

import com.cuidarapp.model.Idoso;
import com.cuidarapp.repository.IdosoRepository;

public class IdosoService {
    
    private final IdosoRepository idosoRepository;
    
    public IdosoService(IdosoRepository idosoRepository) {
        this.idosoRepository = idosoRepository;
    }
    
    public Idoso getById(long id) {
        return idosoRepository.getById(id);
    }
    
    public ServiceResult atualizarTelefoneEmergencia(long idosoId, String telefone) {
        Idoso idoso = idosoRepository.getById(idosoId);
        if (idoso == null) {
            return new ServiceResult(false, "Idoso não encontrado", null);
        }
        
        if (telefone == null || telefone.trim().isEmpty()) {
            return new ServiceResult(false, "Telefone não informado", null);
        }
        
        idoso.setTelefoneEmergencia(telefone.trim());
        idosoRepository.update(idoso);
        
        return new ServiceResult(true, "Telefone atualizado com sucesso", idosoId);
    }
    
    public String getTelefoneEmergencia(long idosoId) {
        Idoso idoso = idosoRepository.getById(idosoId);
        return idoso != null ? idoso.getTelefoneEmergencia() : "192";
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
