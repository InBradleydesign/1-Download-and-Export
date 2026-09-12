package com.cuidarapp.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class UsuarioTest {
    
    @Test
    public void validarNome_comNomeValido_retornaNomeTrimado() {
        Cuidador cuidador = new Cuidador();
        String resultado = cuidador.validarNome("  Maria Silva  ");
        assertEquals("Maria Silva", resultado);
    }
    
    @Test
    public void validarNome_comNomeNulo_retornaNaoInformado() {
        Cuidador cuidador = new Cuidador();
        String resultado = cuidador.validarNome(null);
        assertEquals("Não informado", resultado);
    }
    
    @Test
    public void validarNome_comNomeVazio_retornaNaoInformado() {
        Cuidador cuidador = new Cuidador();
        String resultado = cuidador.validarNome("");
        assertEquals("Não informado", resultado);
    }
    
    @Test
    public void validarNome_comApenasEspacos_retornaNaoInformado() {
        Cuidador cuidador = new Cuidador();
        String resultado = cuidador.validarNome("   ");
        assertEquals("Não informado", resultado);
    }
    
    @Test
    public void ajustarFonte_incrementoValido_atualizaFonte() {
        Cuidador cuidador = new Cuidador();
        cuidador.setTamanhoFonte(1.0f);
        cuidador.ajustarFonte(0.1f);
        assertEquals(1.1f, cuidador.getTamanhoFonte(), 0.01f);
    }
    
    @Test
    public void ajustarFonte_decrementoValido_atualizaFonte() {
        Cuidador cuidador = new Cuidador();
        cuidador.setTamanhoFonte(1.0f);
        cuidador.ajustarFonte(-0.1f);
        assertEquals(0.9f, cuidador.getTamanhoFonte(), 0.01f);
    }
    
    @Test
    public void ajustarFonte_ultrapassaMaximo_limitaEmMaximo() {
        Cuidador cuidador = new Cuidador();
        cuidador.setTamanhoFonte(1.9f);
        cuidador.ajustarFonte(0.5f);
        assertEquals(Usuario.getFonteMax(), cuidador.getTamanhoFonte(), 0.01f);
    }
    
    @Test
    public void ajustarFonte_ultrapassaMinimo_limitaEmMinimo() {
        Cuidador cuidador = new Cuidador();
        cuidador.setTamanhoFonte(0.9f);
        cuidador.ajustarFonte(-0.5f);
        assertEquals(Usuario.getFonteMin(), cuidador.getTamanhoFonte(), 0.01f);
    }
    
    @Test
    public void setTamanhoFonte_valorAbaixoMinimo_setaMinimo() {
        Cuidador cuidador = new Cuidador();
        cuidador.setTamanhoFonte(0.5f);
        assertEquals(Usuario.getFonteMin(), cuidador.getTamanhoFonte(), 0.01f);
    }
    
    @Test
    public void setTamanhoFonte_valorAcimaMaximo_setaMaximo() {
        Cuidador cuidador = new Cuidador();
        cuidador.setTamanhoFonte(3.0f);
        assertEquals(Usuario.getFonteMax(), cuidador.getTamanhoFonte(), 0.01f);
    }
    
    @Test
    public void setTamanhoFonte_valorDentroLimites_setaValor() {
        Cuidador cuidador = new Cuidador();
        cuidador.setTamanhoFonte(1.5f);
        assertEquals(1.5f, cuidador.getTamanhoFonte(), 0.01f);
    }
    
    @Test
    public void getFonteMin_retorna08() {
        assertEquals(0.8f, Usuario.getFonteMin(), 0.01f);
    }
    
    @Test
    public void getFonteMax_retorna20() {
        assertEquals(2.0f, Usuario.getFonteMax(), 0.01f);
    }
    
    @Test
    public void getFontePadrao_retorna10() {
        assertEquals(1.0f, Usuario.getFontePadrao(), 0.01f);
    }
    
    @Test
    public void cuidador_getPerfil_retornaCUIDADOR() {
        Cuidador cuidador = new Cuidador();
        assertEquals(TipoPerfil.CUIDADOR, cuidador.getPerfil());
    }
    
    @Test
    public void idoso_getPerfil_retornaIDOSO() {
        Idoso idoso = new Idoso();
        assertEquals(TipoPerfil.IDOSO, idoso.getPerfil());
    }
    
    @Test
    public void cuidador_cadastrarIdoso_setaCuidadorId() {
        Cuidador cuidador = new Cuidador();
        cuidador.setId(1L);
        
        Idoso idoso = new Idoso();
        boolean resultado = cuidador.cadastrarIdoso(idoso);
        
        assertTrue(resultado);
        assertEquals(1L, idoso.getCuidadorId());
    }
    
    @Test
    public void cuidador_cadastrarIdosoNulo_retornaFalse() {
        Cuidador cuidador = new Cuidador();
        boolean resultado = cuidador.cadastrarIdoso(null);
        assertFalse(resultado);
    }
}
