package com.cuidarapp;

import com.cuidarapp.model.*;
import com.cuidarapp.service.PasswordService;

import org.junit.Test;
import static org.junit.Assert.*;

import java.time.LocalDate;
import java.time.LocalTime;

public class CuidarAppUnitTests {
    
    // ==================== TESTES DE VALIDAÇÃO DE NOME ====================
    
    @Test
    public void testValidarNome_valido() {
        Cuidador cuidador = new Cuidador();
        assertEquals("João", cuidador.validarNome("João"));
    }
    
    @Test
    public void testValidarNome_nulo() {
        Cuidador cuidador = new Cuidador();
        assertEquals("Não informado", cuidador.validarNome(null));
    }
    
    @Test
    public void testValidarNome_vazio() {
        Cuidador cuidador = new Cuidador();
        assertEquals("Não informado", cuidador.validarNome(""));
    }
    
    @Test
    public void testValidarNome_espacos() {
        Cuidador cuidador = new Cuidador();
        assertEquals("Não informado", cuidador.validarNome("    "));
    }
    
    @Test
    public void testValidarNome_trimado() {
        Cuidador cuidador = new Cuidador();
        assertEquals("João Silva", cuidador.validarNome("  João Silva  "));
    }
    
    // ==================== TESTES DE AJUSTE DE FONTE ====================
    
    @Test
    public void testAjustarFonte_incremento() {
        Idoso idoso = new Idoso();
        idoso.setTamanhoFonte(1.0f);
        idoso.ajustarFonte(0.2f);
        assertEquals(1.2f, idoso.getTamanhoFonte(), 0.01f);
    }
    
    @Test
    public void testAjustarFonte_decremento() {
        Idoso idoso = new Idoso();
        idoso.setTamanhoFonte(1.0f);
        idoso.ajustarFonte(-0.2f);
        assertEquals(0.8f, idoso.getTamanhoFonte(), 0.01f);
    }
    
    @Test
    public void testAjustarFonte_limiteMaximo() {
        Idoso idoso = new Idoso();
        idoso.setTamanhoFonte(1.9f);
        idoso.ajustarFonte(0.5f);
        assertEquals(2.0f, idoso.getTamanhoFonte(), 0.01f);
    }
    
    @Test
    public void testAjustarFonte_limiteMinimo() {
        Idoso idoso = new Idoso();
        idoso.setTamanhoFonte(0.9f);
        idoso.ajustarFonte(-0.5f);
        assertEquals(0.8f, idoso.getTamanhoFonte(), 0.01f);
    }
    
    // ==================== TESTES DE AUTENTICAÇÃO ====================
    
    @Test
    public void testAutenticacao_senhaCorreta() {
        PasswordService ps = new PasswordService();
        String salt = ps.generateSalt();
        String hash = ps.hashPassword("minhasenha", salt);
        
        assertTrue(ps.verifyPassword("minhasenha", hash, salt));
    }
    
    @Test
    public void testAutenticacao_senhaIncorreta() {
        PasswordService ps = new PasswordService();
        String salt = ps.generateSalt();
        String hash = ps.hashPassword("minhasenha", salt);
        
        assertFalse(ps.verifyPassword("senhaerrada", hash, salt));
    }
    
    @Test
    public void testAutenticacao_senhaNula() {
        PasswordService ps = new PasswordService();
        assertFalse(ps.verifyPassword(null, "hash", "salt"));
    }
    
    // ==================== TESTES DE VÍNCULO CUIDADOR-IDOSO ====================
    
    @Test
    public void testVinculo_cadastrarIdoso() {
        Cuidador cuidador = new Cuidador();
        cuidador.setId(1L);
        
        Idoso idoso = new Idoso();
        assertTrue(cuidador.cadastrarIdoso(idoso));
        assertEquals(1L, idoso.getCuidadorId());
    }
    
    @Test
    public void testVinculo_idosoNulo() {
        Cuidador cuidador = new Cuidador();
        cuidador.setId(1L);
        
        assertFalse(cuidador.cadastrarIdoso(null));
    }
    
    @Test
    public void testVinculo_idosoVinculado() {
        Idoso idoso = new Idoso();
        idoso.setCuidadorId(5L);
        
        assertEquals(5L, idoso.getCuidadorId());
    }
    
    // ==================== TESTES DE MEDICAMENTO ====================
    
    @Test
    public void testMedicamento_nomeValido() {
        Medicamento med = new Medicamento(1L, "Paracetamol", "500mg", LocalTime.of(8, 0));
        assertEquals("Paracetamol", med.getNome());
    }
    
    @Test
    public void testMedicamento_nomeNulo() {
        Medicamento med = new Medicamento();
        med.setNome(null);
        assertEquals("Não informado", med.getNome());
    }
    
    @Test
    public void testMedicamento_nomeVazio() {
        Medicamento med = new Medicamento();
        med.setNome("   ");
        assertEquals("Não informado", med.getNome());
    }
    
    @Test
    public void testMedicamento_vigente() {
        Medicamento med = new Medicamento();
        med.setAtivo(true);
        med.setDataInicio(LocalDate.now().minusDays(1));
        med.setDataFim(LocalDate.now().plusDays(1));
        
        assertTrue(med.isVigente());
    }
    
    @Test
    public void testMedicamento_naoVigente_inativo() {
        Medicamento med = new Medicamento();
        med.setAtivo(false);
        
        assertFalse(med.isVigente());
    }
    
    @Test
    public void testMedicamento_naoVigente_dataFimPassada() {
        Medicamento med = new Medicamento();
        med.setAtivo(true);
        med.setDataFim(LocalDate.now().minusDays(1));
        
        assertFalse(med.isVigente());
    }
    
    // ==================== TESTES DE EXERCÍCIO ====================
    
    @Test
    public void testExercicio_nomeValido() {
        Exercicio ex = new Exercicio(1L, "Caminhada", "30 minutos no parque", 30);
        assertEquals("Caminhada", ex.getNome());
    }
    
    @Test
    public void testExercicio_nomeNulo() {
        Exercicio ex = new Exercicio();
        ex.setNome(null);
        assertEquals("Não informado", ex.getNome());
    }
    
    @Test
    public void testExercicio_duracaoNegativa() {
        Exercicio ex = new Exercicio();
        ex.setDuracaoMinutos(-10);
        assertEquals(0, ex.getDuracaoMinutos());
    }
    
    @Test
    public void testExercicio_frequenciaLimite() {
        Exercicio ex = new Exercicio();
        ex.setFrequenciaSemanal(10);
        assertEquals(7, ex.getFrequenciaSemanal());
    }
    
    // ==================== TESTES DE ENUMS ====================
    
    @Test
    public void testTipoPerfil_valores() {
        assertEquals(2, TipoPerfil.values().length);
        assertNotNull(TipoPerfil.valueOf("CUIDADOR"));
        assertNotNull(TipoPerfil.valueOf("IDOSO"));
    }
    
    @Test
    public void testStatusVisita_valores() {
        assertEquals(4, StatusVisita.values().length);
        assertNotNull(StatusVisita.valueOf("AGENDADA"));
        assertNotNull(StatusVisita.valueOf("CONFIRMADA"));
        assertNotNull(StatusVisita.valueOf("REALIZADA"));
        assertNotNull(StatusVisita.valueOf("CANCELADA"));
    }
    
    @Test
    public void testStatusAlerta_valores() {
        assertEquals(3, StatusAlerta.values().length);
        assertNotNull(StatusAlerta.valueOf("ATIVO"));
        assertNotNull(StatusAlerta.valueOf("ATENDIDO"));
        assertNotNull(StatusAlerta.valueOf("CANCELADO"));
    }
    
    // ==================== TESTES DE DIETA SEMANAL ====================
    
    @Test
    public void testDietaSemanal_diaSemanaValido() {
        DietaSemanal dieta = new DietaSemanal(1L, 3);
        assertEquals("Quarta-feira", dieta.getNomeDiaSemana());
    }
    
    @Test
    public void testDietaSemanal_diaSemanaInvalido() {
        DietaSemanal dieta = new DietaSemanal();
        dieta.setDiaSemana(10);
        assertEquals(7, dieta.getDiaSemana());
    }
    
    @Test
    public void testDietaSemanal_diaSemanaZero() {
        DietaSemanal dieta = new DietaSemanal();
        dieta.setDiaSemana(0);
        assertEquals(1, dieta.getDiaSemana());
    }
    
    // ==================== TESTES DE VISITA ====================
    
    @Test
    public void testVisita_statusInicial() {
        Visita visita = new Visita();
        assertEquals(StatusVisita.AGENDADA, visita.getStatus());
    }
    
    @Test
    public void testVisita_isPendente_agendada() {
        Visita visita = new Visita();
        visita.setStatus(StatusVisita.AGENDADA);
        assertTrue(visita.isPendente());
    }
    
    @Test
    public void testVisita_isPendente_confirmada() {
        Visita visita = new Visita();
        visita.setStatus(StatusVisita.CONFIRMADA);
        assertTrue(visita.isPendente());
    }
    
    @Test
    public void testVisita_isPendente_realizada() {
        Visita visita = new Visita();
        visita.setStatus(StatusVisita.REALIZADA);
        assertFalse(visita.isPendente());
    }
    
    // ==================== TESTES DE ALERTA EMERGÊNCIA ====================
    
    @Test
    public void testAlertaEmergencia_statusInicial() {
        AlertaEmergencia alerta = new AlertaEmergencia();
        assertEquals(StatusAlerta.ATIVO, alerta.getStatus());
    }
    
    @Test
    public void testAlertaEmergencia_marcarComoAtendido() {
        AlertaEmergencia alerta = new AlertaEmergencia();
        alerta.marcarComoAtendido();
        assertEquals(StatusAlerta.ATENDIDO, alerta.getStatus());
    }
    
    @Test
    public void testAlertaEmergencia_cancelar() {
        AlertaEmergencia alerta = new AlertaEmergencia();
        alerta.cancelar();
        assertEquals(StatusAlerta.CANCELADO, alerta.getStatus());
    }
    
    // ==================== TESTES DE CHECK-IN DIÁRIO ====================
    
    @Test
    public void testCheckInDiario_estadoHumorDescricao() {
        CheckInDiario checkIn = new CheckInDiario();
        checkIn.setEstadoHumor("otimo");
        assertEquals("Ótimo", checkIn.getDescricaoEstadoHumor());
    }
    
    @Test
    public void testCheckInDiario_estadoFisicoDescricao() {
        CheckInDiario checkIn = new CheckInDiario();
        checkIn.setEstadoFisico("regular");
        assertEquals("Regular", checkIn.getDescricaoEstadoFisico());
    }
    
    @Test
    public void testCheckInDiario_estadoNulo() {
        CheckInDiario checkIn = new CheckInDiario();
        assertEquals("Não informado", checkIn.getDescricaoEstadoHumor());
        assertEquals("Não informado", checkIn.getDescricaoEstadoFisico());
    }
    
    // ==================== TESTES DE REGISTRO REFEIÇÃO ====================
    
    @Test
    public void testRegistroRefeicao_tipoRefeicaoNome() {
        RegistroRefeicao registro = new RegistroRefeicao();
        registro.setTipoRefeicao("almoco");
        assertEquals("Almoço", registro.getNomeTipoRefeicao());
    }
    
    @Test
    public void testRegistroRefeicao_tipoRefeicaoNulo() {
        RegistroRefeicao registro = new RegistroRefeicao();
        assertEquals("Refeição", registro.getNomeTipoRefeicao());
    }
}
