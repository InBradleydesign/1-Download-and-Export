package com.cuidarapp.service;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class PasswordServiceTest {
    
    private PasswordService passwordService;
    
    @Before
    public void setUp() {
        passwordService = new PasswordService();
    }
    
    @Test
    public void generateSalt_geraSaltNaoNulo() {
        String salt = passwordService.generateSalt();
        assertNotNull(salt);
        assertFalse(salt.isEmpty());
    }
    
    @Test
    public void generateSalt_geraSaltsDiferentes() {
        String salt1 = passwordService.generateSalt();
        String salt2 = passwordService.generateSalt();
        assertNotEquals(salt1, salt2);
    }
    
    @Test
    public void hashPassword_comSenhaValida_geraHash() {
        String salt = passwordService.generateSalt();
        String hash = passwordService.hashPassword("senha123", salt);
        
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
        assertNotEquals("senha123", hash);
    }
    
    @Test
    public void hashPassword_comSenhaNula_retornaNull() {
        String salt = passwordService.generateSalt();
        String hash = passwordService.hashPassword(null, salt);
        assertNull(hash);
    }
    
    @Test
    public void hashPassword_comSenhaVazia_retornaNull() {
        String salt = passwordService.generateSalt();
        String hash = passwordService.hashPassword("", salt);
        assertNull(hash);
    }
    
    @Test
    public void hashPassword_mesmaSenhaMesmoSalt_geraHashIgual() {
        String salt = passwordService.generateSalt();
        String hash1 = passwordService.hashPassword("senha123", salt);
        String hash2 = passwordService.hashPassword("senha123", salt);
        assertEquals(hash1, hash2);
    }
    
    @Test
    public void hashPassword_mesmaSenhaSaltsDiferentes_geraHashesDiferentes() {
        String salt1 = passwordService.generateSalt();
        String salt2 = passwordService.generateSalt();
        String hash1 = passwordService.hashPassword("senha123", salt1);
        String hash2 = passwordService.hashPassword("senha123", salt2);
        assertNotEquals(hash1, hash2);
    }
    
    @Test
    public void verifyPassword_senhaCorreta_retornaTrue() {
        String salt = passwordService.generateSalt();
        String hash = passwordService.hashPassword("senha123", salt);
        
        assertTrue(passwordService.verifyPassword("senha123", hash, salt));
    }
    
    @Test
    public void verifyPassword_senhaIncorreta_retornaFalse() {
        String salt = passwordService.generateSalt();
        String hash = passwordService.hashPassword("senha123", salt);
        
        assertFalse(passwordService.verifyPassword("senhaerrada", hash, salt));
    }
    
    @Test
    public void verifyPassword_senhaNula_retornaFalse() {
        String salt = passwordService.generateSalt();
        String hash = passwordService.hashPassword("senha123", salt);
        
        assertFalse(passwordService.verifyPassword(null, hash, salt));
    }
    
    @Test
    public void verifyPassword_hashNulo_retornaFalse() {
        String salt = passwordService.generateSalt();
        assertFalse(passwordService.verifyPassword("senha123", null, salt));
    }
    
    @Test
    public void verifyPassword_saltNulo_retornaFalse() {
        String salt = passwordService.generateSalt();
        String hash = passwordService.hashPassword("senha123", salt);
        
        assertFalse(passwordService.verifyPassword("senha123", hash, null));
    }
    
    @Test
    public void verifyPassword_comSaltErrado_retornaFalse() {
        String salt1 = passwordService.generateSalt();
        String salt2 = passwordService.generateSalt();
        String hash = passwordService.hashPassword("senha123", salt1);
        
        assertFalse(passwordService.verifyPassword("senha123", hash, salt2));
    }
}
