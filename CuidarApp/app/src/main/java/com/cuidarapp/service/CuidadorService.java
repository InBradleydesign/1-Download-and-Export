package com.cuidarapp.service;

import com.cuidarapp.model.Cuidador;
import com.cuidarapp.model.Idoso;
import com.cuidarapp.repository.CuidadorRepository;
import com.cuidarapp.repository.IdosoRepository;

import java.util.List;

public class CuidadorService {
    
    private final CuidadorRepository cuidadorRepository;
    private final IdosoRepository idosoRepository;
    private final PasswordService passwordService;
    
    public CuidadorService(CuidadorRepository cuidadorRepository, IdosoRepository idosoRepository) {
        this.cuidadorRepository = cuidadorRepository;
        this.idosoRepository = idosoRepository;
        this.passwordService = new PasswordService();
    }
    
    public ServiceResult cadastrarIdoso(long cuidadorId, String nome, String email, String senha, 
                                        String telefoneEmergencia, String observacoes) {
        Cuidador cuidador = cuidadorRepository.getById(cuidadorId);
        if (cuidador == null) {
            return new ServiceResult(false, "Cuidador não encontrado", null);
        }
        
        if (nome == null || nome.trim().isEmpty()) {
            return new ServiceResult(false, "Nome não informado", null);
        }
        
        if (email == null || email.trim().isEmpty()) {
            return new ServiceResult(false, "E-mail não informado", null);
        }
        
        if (senha == null || senha.length() < 6) {
            return new ServiceResult(false, "Senha deve ter no mínimo 6 caracteres", null);
        }
        
        email = email.trim().toLowerCase();
        
        if (idosoRepository.getByEmail(email) != null) {
            return new ServiceResult(false, "E-mail já cadastrado para outro idoso", null);
        }
        
        String salt = passwordService.generateSalt();
        String hash = passwordService.hashPassword(senha, salt);
        
        if (hash == null) {
            return new ServiceResult(false, "Erro ao processar senha", null);
        }
        
        Idoso idoso = new Idoso(nome.trim(), email, hash, salt, cuidadorId);
        
        if (telefoneEmergencia != null && !telefoneEmergencia.trim().isEmpty()) {
            idoso.setTelefoneEmergencia(telefoneEmergencia.trim());
        }
        
        if (observacoes != null) {
            idoso.setObservacoes(observacoes.trim());
        }
        
        long id = idosoRepository.insert(idoso);
        idoso.setId(id);
        
        return new ServiceResult(true, "Idoso cadastrado com sucesso", id);
    }
    
    public ServiceResult atualizarIdoso(long idosoId, String nome, String telefoneEmergencia, String observacoes) {
        Idoso idoso = idosoRepository.getById(idosoId);
        if (idoso == null) {
            return new ServiceResult(false, "Idoso não encontrado", null);
        }
        
        if (nome == null || nome.trim().isEmpty()) {
            return new ServiceResult(false, "Nome não informado", null);
        }
        
        idoso.setNome(nome.trim());
        
        if (telefoneEmergencia != null && !telefoneEmergencia.trim().isEmpty()) {
            idoso.setTelefoneEmergencia(telefoneEmergencia.trim());
        }
        
        if (observacoes != null) {
            idoso.setObservacoes(observacoes.trim());
        }
        
        idosoRepository.update(idoso);
        
        return new ServiceResult(true, "Idoso atualizado com sucesso", idosoId);
    }
    
    public ServiceResult excluirIdoso(long cuidadorId, long idosoId) {
        Idoso idoso = idosoRepository.getById(idosoId);
        if (idoso == null) {
            return new ServiceResult(false, "Idoso não encontrado", null);
        }
        
        if (idoso.getCuidadorId() != cuidadorId) {
            return new ServiceResult(false, "Você não tem permissão para excluir este idoso", null);
        }
        
        idosoRepository.deleteById(idosoId);
        
        return new ServiceResult(true, "Idoso excluído com sucesso", idosoId);
    }
    
    public List<Idoso> listarIdosos(long cuidadorId) {
        return idosoRepository.getByCuidadorId(cuidadorId);
    }
    
    public Idoso getIdoso(long idosoId) {
        return idosoRepository.getById(idosoId);
    }
    
    public boolean verificarVinculo(long cuidadorId, long idosoId) {
        Idoso idoso = idosoRepository.getById(idosoId);
        return idoso != null && idoso.getCuidadorId() == cuidadorId;
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
