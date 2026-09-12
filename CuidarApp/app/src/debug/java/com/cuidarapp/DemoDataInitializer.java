package com.cuidarapp;

import com.cuidarapp.model.*;
import com.cuidarapp.service.PasswordService;

import java.time.LocalDate;
import java.time.LocalTime;

public class DemoDataInitializer {
    
    public static void init(CuidarApplication app) {
        PasswordService passwordService = new PasswordService();
        
        // Criar Cuidador de demonstração
        String saltCuidador = passwordService.generateSalt();
        String hashCuidador = passwordService.hashPassword("cuidador123", saltCuidador);
        
        Cuidador cuidador = new Cuidador();
        cuidador.setNome("Maria Silva");
        cuidador.setEmail("cuidador@demo.com");
        cuidador.setSenhaHash(hashCuidador);
        cuidador.setSenhaSalt(saltCuidador);
        long cuidadorId = app.getDatabase().cuidadorDao().insert(cuidador);
        
        // Criar Idoso de demonstração
        String saltIdoso = passwordService.generateSalt();
        String hashIdoso = passwordService.hashPassword("idoso123", saltIdoso);
        
        Idoso idoso = new Idoso();
        idoso.setNome("José Santos");
        idoso.setEmail("idoso@demo.com");
        idoso.setSenhaHash(hashIdoso);
        idoso.setSenhaSalt(saltIdoso);
        idoso.setCuidadorId(cuidadorId);
        idoso.setTelefoneEmergencia("192");
        idoso.setObservacoes("Idoso de demonstração para testes");
        long idosoId = app.getDatabase().idosoDao().insert(idoso);
        
        // Criar dieta semanal de demonstração
        criarDietaSemanal(app, idosoId);
        
        // Criar medicamentos de demonstração
        criarMedicamentos(app, idosoId);
        
        // Criar exercícios de demonstração
        criarExercicios(app, idosoId);
        
        // Criar visitas de demonstração
        criarVisitas(app, idosoId, cuidadorId);
    }
    
    private static void criarDietaSemanal(CuidarApplication app, long idosoId) {
        String[] cafes = {
            "Pão integral com queijo branco, 1 fruta, chá sem açúcar",
            "Mingau de aveia com banana, café com leite desnatado",
            "Tapioca com queijo cottage, suco de laranja natural",
            "Pão de forma integral com requeijão light, 1 maçã",
            "Vitamina de banana com aveia, 2 torradas integrais",
            "Omelete de claras com espinafre, pão integral",
            "Panqueca de banana com aveia, café com leite"
        };
        
        String[] almocos = {
            "Arroz integral, feijão, frango grelhado, salada verde, legumes refogados",
            "Arroz, lentilha, peixe assado, couve refogada, cenoura",
            "Macarrão integral, carne moída magra, salada de tomate, brócolis",
            "Arroz integral, feijão, frango desfiado, alface, beterraba",
            "Purê de batata, peixe grelhado, salada verde, chuchu",
            "Arroz, feijão preto, carne assada, agrião, abóbora",
            "Nhoque de batata, molho de tomate caseiro, salada, vagem"
        };
        
        String[] jantares = {
            "Sopa de legumes com frango desfiado, torrada integral",
            "Omelete com legumes, salada verde, suco natural",
            "Sopa de abóbora com gengibre, pão integral",
            "Salada completa com atum, ovo cozido, legumes",
            "Creme de espinafre com frango, torrada",
            "Sopa de feijão com legumes, salada verde",
            "Caldo verde light, pão de milho, fruta"
        };
        
        for (int i = 1; i <= 7; i++) {
            DietaSemanal dieta = new DietaSemanal(idosoId, i);
            dieta.setCafeManha(cafes[i-1]);
            dieta.setLancheManha("1 fruta da estação ou iogurte natural");
            dieta.setAlmoco(almocos[i-1]);
            dieta.setLancheTarde("Castanhas (punhado pequeno) ou chá com biscoito integral");
            dieta.setJantar(jantares[i-1]);
            dieta.setCeia("Chá de camomila ou erva-doce, sem açúcar");
            dieta.setObservacoes("Beber pelo menos 8 copos de água ao longo do dia");
            
            app.getDatabase().dietaSemanalDao().insert(dieta);
        }
    }
    
    private static void criarMedicamentos(CuidarApplication app, long idosoId) {
        Medicamento med1 = new Medicamento(idosoId, "Losartana", "50mg", LocalTime.of(8, 0));
        med1.setFrequencia("diario");
        med1.setInstrucoes("Tomar com água, em jejum");
        app.getDatabase().medicamentoDao().insert(med1);
        
        Medicamento med2 = new Medicamento(idosoId, "Metformina", "500mg", LocalTime.of(12, 0));
        med2.setFrequencia("diario");
        med2.setInstrucoes("Tomar junto com o almoço");
        app.getDatabase().medicamentoDao().insert(med2);
        
        Medicamento med3 = new Medicamento(idosoId, "Sinvastatina", "20mg", LocalTime.of(21, 0));
        med3.setFrequencia("diario");
        med3.setInstrucoes("Tomar à noite, antes de dormir");
        app.getDatabase().medicamentoDao().insert(med3);
        
        Medicamento med4 = new Medicamento(idosoId, "Vitamina D", "2000 UI", LocalTime.of(8, 0));
        med4.setFrequencia("diario");
        med4.setInstrucoes("Tomar junto com o café da manhã");
        app.getDatabase().medicamentoDao().insert(med4);
    }
    
    private static void criarExercicios(CuidarApplication app, long idosoId) {
        Exercicio ex1 = new Exercicio(idosoId, "Caminhada leve", 
            "Caminhada em ritmo confortável no parque ou em ambiente plano", 30);
        ex1.setFrequenciaSemanal(5);
        ex1.setDiasSemana("1,2,3,4,5");
        ex1.setInstrucoes("Use calçado confortável. Pare se sentir dor ou falta de ar.");
        app.getDatabase().exercicioDao().insert(ex1);
        
        Exercicio ex2 = new Exercicio(idosoId, "Alongamento", 
            "Alongamento suave de membros superiores e inferiores", 15);
        ex2.setFrequenciaSemanal(7);
        ex2.setDiasSemana("1,2,3,4,5,6,7");
        ex2.setInstrucoes("Fazer movimentos lentos, sem forçar. Respirar profundamente.");
        app.getDatabase().exercicioDao().insert(ex2);
        
        Exercicio ex3 = new Exercicio(idosoId, "Hidroginástica", 
            "Exercícios na piscina com acompanhamento", 45);
        ex3.setFrequenciaSemanal(2);
        ex3.setDiasSemana("2,5");
        ex3.setInstrucoes("Levar toalha, roupa de banho e chinelo. Não ir em jejum.");
        app.getDatabase().exercicioDao().insert(ex3);
    }
    
    private static void criarVisitas(CuidarApplication app, long idosoId, long cuidadorId) {
        LocalDate hoje = LocalDate.now();
        
        Visita v1 = new Visita(idosoId, cuidadorId, hoje.plusDays(2), 
            LocalTime.of(10, 0), "Visita de acompanhamento semanal");
        v1.setHoraFim(LocalTime.of(12, 0));
        v1.setStatus(StatusVisita.AGENDADA);
        app.getDatabase().visitaDao().insert(v1);
        
        Visita v2 = new Visita(idosoId, cuidadorId, hoje.plusDays(5), 
            LocalTime.of(14, 0), "Acompanhamento a consulta médica");
        v2.setHoraFim(LocalTime.of(17, 0));
        v2.setStatus(StatusVisita.CONFIRMADA);
        app.getDatabase().visitaDao().insert(v2);
        
        Visita v3 = new Visita(idosoId, cuidadorId, hoje.plusDays(9), 
            LocalTime.of(9, 0), "Visita de acompanhamento semanal");
        v3.setHoraFim(LocalTime.of(11, 0));
        v3.setStatus(StatusVisita.AGENDADA);
        app.getDatabase().visitaDao().insert(v3);
    }
}
