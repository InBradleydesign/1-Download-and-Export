package com.cuidarapp.service;

import org.junit.Test;
import static org.junit.Assert.*;

import java.time.LocalDate;

public class ProgressoServiceTest {
    
    @Test
    public void progressoResumo_percentuais_calculadosCorretamente() {
        ProgressoService.ProgressoResumo resumo = new ProgressoService.ProgressoResumo(
            3, 6,   // refeições
            5, 10,  // medicamentos
            2, 4,   // exercícios
            3, 7,   // check-ins
            LocalDate.now().minusDays(6),
            LocalDate.now()
        );
        
        assertEquals(50.0, resumo.getPercentualRefeicoes(), 0.01);
        assertEquals(50.0, resumo.getPercentualMedicamentos(), 0.01);
        assertEquals(50.0, resumo.getPercentualExercicios(), 0.01);
        assertTrue(resumo.getPercentualCheckIns() > 40 && resumo.getPercentualCheckIns() < 45);
    }
    
    @Test
    public void progressoResumo_percentual_limitadoEm100() {
        ProgressoService.ProgressoResumo resumo = new ProgressoService.ProgressoResumo(
            10, 5,  // mais que o esperado
            15, 10, // mais que o esperado
            8, 4,   // mais que o esperado
            10, 7,  // mais que o esperado
            LocalDate.now().minusDays(6),
            LocalDate.now()
        );
        
        assertEquals(100.0, resumo.getPercentualRefeicoes(), 0.01);
        assertEquals(100.0, resumo.getPercentualMedicamentos(), 0.01);
        assertEquals(100.0, resumo.getPercentualExercicios(), 0.01);
        assertEquals(100.0, resumo.getPercentualCheckIns(), 0.01);
    }
    
    @Test
    public void progressoResumo_esperadoZero_percentualZero() {
        ProgressoService.ProgressoResumo resumo = new ProgressoService.ProgressoResumo(
            0, 0,
            0, 0,
            0, 0,
            0, 0,
            LocalDate.now(),
            LocalDate.now()
        );
        
        assertEquals(0.0, resumo.getPercentualRefeicoes(), 0.01);
        assertEquals(0.0, resumo.getPercentualMedicamentos(), 0.01);
        assertEquals(0.0, resumo.getPercentualExercicios(), 0.01);
        assertEquals(0.0, resumo.getPercentualCheckIns(), 0.01);
    }
    
    @Test
    public void progressoResumo_getters_retornamValoresCorretos() {
        LocalDate dataInicio = LocalDate.of(2024, 1, 1);
        LocalDate dataFim = LocalDate.of(2024, 1, 7);
        
        ProgressoService.ProgressoResumo resumo = new ProgressoService.ProgressoResumo(
            5, 10,
            8, 14,
            3, 7,
            4, 7,
            dataInicio,
            dataFim
        );
        
        assertEquals(5, resumo.getRefeicoesRegistradas());
        assertEquals(10, resumo.getRefeicoesEsperadas());
        assertEquals(8, resumo.getMedicamentosConfirmados());
        assertEquals(14, resumo.getMedicamentosEsperados());
        assertEquals(3, resumo.getExerciciosConcluidos());
        assertEquals(7, resumo.getExerciciosEsperados());
        assertEquals(4, resumo.getCheckInsRealizados());
        assertEquals(7, resumo.getCheckInsEsperados());
        assertEquals(dataInicio, resumo.getDataInicio());
        assertEquals(dataFim, resumo.getDataFim());
    }
}
