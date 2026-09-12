package com.cuidarapp.service;

import com.cuidarapp.model.Cuidador;
import com.cuidarapp.model.Idoso;
import com.cuidarapp.model.TipoPerfil;
import com.cuidarapp.model.Usuario;
import com.cuidarapp.repository.CuidadorRepository;
import com.cuidarapp.repository.IdosoRepository;

public class AuthService {
    
    private final CuidadorRepository cuidadorRepository;
    private final IdosoRepository idosoRepository;
    private final PasswordService passwordService;
    
    private Usuario usuarioLogado;
    
    public AuthService(CuidadorRepository cuidadorRepository, IdosoRepository idosoRepository) {
        this.cuidadorRepository = cuidadorRepository;
        this.idosoRepository = idosoRepository;
        this.passwordService = new PasswordService();
    }
    
    public AuthResult login(String email, String senha, TipoPerfil tipoPerfil) {
        if (email == null || email.trim().isEmpty()) {
            return new AuthResult(false, "E-mail não informado", null);
        }
        
        if (senha == null || senha.isEmpty()) {
            return new AuthResult(false, "Senha não informada", null);
        }
        
        email = email.trim().toLowerCase();
        
        Usuario usuario = null;
        
        if (tipoPerfil == TipoPerfil.CUIDADOR) {
            usuario = cuidadorRepository.getByEmail(email);
        } else if (tipoPerfil == TipoPerfil.IDOSO) {
            usuario = idosoRepository.getByEmail(email);
        }
        
        if (usuario == null) {
            return new AuthResult(false, "Usuário não encontrado", null);
        }
        
        if (!passwordService.verifyPassword(senha, usuario.getSenhaHash(), usuario.getSenhaSalt())) {
            return new AuthResult(false, "Senha incorreta", null);
        }
        
        this.usuarioLogado = usuario;
        return new AuthResult(true, "Login realizado com sucesso", usuario);
    }
    
    public void logout() {
        this.usuarioLogado = null;
    }
    
    public Usuario getUsuarioLogado() {
        return usuarioLogado;
    }
    
    public boolean isLogado() {
        return usuarioLogado != null;
    }
    
    public TipoPerfil getPerfilLogado() {
        return usuarioLogado != null ? usuarioLogado.getTipoPerfil() : null;
    }
    
    public AuthResult registrarCuidador(String nome, String email, String senha) {
        if (nome == null || nome.trim().isEmpty()) {
            return new AuthResult(false, "Nome não informado", null);
        }
        
        if (email == null || email.trim().isEmpty()) {
            return new AuthResult(false, "E-mail não informado", null);
        }
        
        if (senha == null || senha.length() < 6) {
            return new AuthResult(false, "Senha deve ter no mínimo 6 caracteres", null);
        }
        
        email = email.trim().toLowerCase();
        
        if (cuidadorRepository.getByEmail(email) != null) {
            return new AuthResult(false, "E-mail já cadastrado", null);
        }
        
        String salt = passwordService.generateSalt();
        String hash = passwordService.hashPassword(senha, salt);
        
        if (hash == null) {
            return new AuthResult(false, "Erro ao processar senha", null);
        }
        
        Cuidador cuidador = new Cuidador(nome.trim(), email, hash, salt);
        long id = cuidadorRepository.insert(cuidador);
        cuidador.setId(id);
        
        return new AuthResult(true, "Cadastro realizado com sucesso", cuidador);
    }
    
    public void atualizarTamanhoFonte(float tamanhoFonte) {
        if (usuarioLogado == null) return;
        
        usuarioLogado.setTamanhoFonte(tamanhoFonte);
        
        if (usuarioLogado.getTipoPerfil() == TipoPerfil.CUIDADOR) {
            cuidadorRepository.update((Cuidador) usuarioLogado);
        } else {
            idosoRepository.update((Idoso) usuarioLogado);
        }
    }
    
    public static class AuthResult {
        private final boolean success;
        private final String message;
        private final Usuario usuario;
        
        public AuthResult(boolean success, String message, Usuario usuario) {
            this.success = success;
            this.message = message;
            this.usuario = usuario;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public Usuario getUsuario() {
            return usuario;
        }
    }
}
