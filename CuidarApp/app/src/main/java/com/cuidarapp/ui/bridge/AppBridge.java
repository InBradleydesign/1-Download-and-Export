package com.cuidarapp.ui.bridge;

import android.webkit.JavascriptInterface;

import com.cuidarapp.CuidarApplication;
import com.cuidarapp.MainActivity;
import com.cuidarapp.model.*;
import com.cuidarapp.service.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppBridge {
    
    private final MainActivity activity;
    private final CuidarApplication app;
    private final Gson gson;
    
    public AppBridge(MainActivity activity) {
        this.activity = activity;
        this.app = CuidarApplication.getInstance();
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    }
    
    private String success(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", data);
        return gson.toJson(result);
    }
    
    private String error(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", message);
        return gson.toJson(result);
    }
    
    // ==================== AUTENTICAÇÃO ====================
    
    @JavascriptInterface
    public String login(String email, String senha, String tipoPerfil) {
        try {
            TipoPerfil perfil = TipoPerfil.valueOf(tipoPerfil.toUpperCase());
            AuthService.AuthResult result = app.getAuthService().login(email, senha, perfil);
            
            if (result.isSuccess()) {
                Usuario usuario = result.getUsuario();
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", usuario.getId());
                userData.put("nome", usuario.getNome());
                userData.put("email", usuario.getEmail());
                userData.put("tipoPerfil", usuario.getTipoPerfil().name());
                userData.put("tamanhoFonte", usuario.getTamanhoFonte());
                
                if (usuario instanceof Idoso) {
                    userData.put("cuidadorId", ((Idoso) usuario).getCuidadorId());
                    userData.put("telefoneEmergencia", ((Idoso) usuario).getTelefoneEmergencia());
                }
                
                return success(userData);
            } else {
                return error(result.getMessage());
            }
        } catch (Exception e) {
            return error("Erro ao realizar login");
        }
    }
    
    @JavascriptInterface
    public String logout() {
        try {
            app.getAuthService().logout();
            return success(true);
        } catch (Exception e) {
            return error("Erro ao realizar logout");
        }
    }
    
    @JavascriptInterface
    public String getUsuarioLogado() {
        try {
            Usuario usuario = app.getAuthService().getUsuarioLogado();
            if (usuario == null) {
                return success(null);
            }
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", usuario.getId());
            userData.put("nome", usuario.getNome());
            userData.put("email", usuario.getEmail());
            userData.put("tipoPerfil", usuario.getTipoPerfil().name());
            userData.put("tamanhoFonte", usuario.getTamanhoFonte());
            
            if (usuario instanceof Idoso) {
                userData.put("cuidadorId", ((Idoso) usuario).getCuidadorId());
                userData.put("telefoneEmergencia", ((Idoso) usuario).getTelefoneEmergencia());
            }
            
            return success(userData);
        } catch (Exception e) {
            return error("Erro ao obter usuário");
        }
    }
    
    @JavascriptInterface
    public String atualizarTamanhoFonte(float tamanho) {
        try {
            app.getAuthService().atualizarTamanhoFonte(tamanho);
            return success(true);
        } catch (Exception e) {
            return error("Erro ao atualizar tamanho da fonte");
        }
    }
    
    // ==================== CUIDADOR - GESTÃO DE IDOSOS ====================
    
    @JavascriptInterface
    public String cadastrarIdoso(String nome, String email, String senha, 
                                 String telefoneEmergencia, String observacoes) {
        try {
            Usuario usuario = app.getAuthService().getUsuarioLogado();
            if (usuario == null || usuario.getTipoPerfil() != TipoPerfil.CUIDADOR) {
                return error("Acesso não autorizado");
            }
            
            CuidadorService.ServiceResult result = app.getCuidadorService()
                .cadastrarIdoso(usuario.getId(), nome, email, senha, telefoneEmergencia, observacoes);
            
            if (result.isSuccess()) {
                return success(result.getData());
            } else {
                return error(result.getMessage());
            }
        } catch (Exception e) {
            return error("Erro ao cadastrar idoso");
        }
    }
    
    @JavascriptInterface
    public String atualizarIdoso(long idosoId, String nome, String telefoneEmergencia, String observacoes) {
        try {
            Usuario usuario = app.getAuthService().getUsuarioLogado();
            if (usuario == null || usuario.getTipoPerfil() != TipoPerfil.CUIDADOR) {
                return error("Acesso não autorizado");
            }
            
            if (!app.getCuidadorService().verificarVinculo(usuario.getId(), idosoId)) {
                return error("Idoso não vinculado a este cuidador");
            }
            
            CuidadorService.ServiceResult result = app.getCuidadorService()
                .atualizarIdoso(idosoId, nome, telefoneEmergencia, observacoes);
            
            if (result.isSuccess()) {
                return success(result.getData());
            } else {
                return error(result.getMessage());
            }
        } catch (Exception e) {
            return error("Erro ao atualizar idoso");
        }
    }
    
    @JavascriptInterface
    public String excluirIdoso(long idosoId) {
        try {
            Usuario usuario = app.getAuthService().getUsuarioLogado();
            if (usuario == null || usuario.getTipoPerfil() != TipoPerfil.CUIDADOR) {
                return error("Acesso não autorizado");
            }
            
            CuidadorService.ServiceResult result = app.getCuidadorService()
                .excluirIdoso(usuario.getId(), idosoId);
            
            if (result.isSuccess()) {
                return success(result.getData());
            } else {
                return error(result.getMessage());
            }
        } catch (Exception e) {
            return error("Erro ao excluir idoso");
        }
    }
    
    @JavascriptInterface
    public String listarIdosos() {
        try {
            Usuario usuario = app.getAuthService().getUsuarioLogado();
            if (usuario == null || usuario.getTipoPerfil() != TipoPerfil.CUIDADOR) {
                return error("Acesso não autorizado");
            }
            
            List<Idoso> idosos = app.getCuidadorService().listarIdosos(usuario.getId());
            return success(idosos);
        } catch (Exception e) {
            return error("Erro ao listar idosos");
        }
    }
    
    @JavascriptInterface
    public String getIdoso(long idosoId) {
        try {
            Idoso idoso = app.getCuidadorService().getIdoso(idosoId);
            if (idoso == null) {
                return error("Idoso não encontrado");
            }
            return success(idoso);
        } catch (Exception e) {
            return error("Erro ao obter idoso");
        }
    }
    
    // ==================== DIETA ====================
    
    @JavascriptInterface
    public String salvarDieta(long idosoId, int diaSemana, String cafeManha, String lancheManha,
                              String almoco, String lancheTarde, String jantar, String ceia,
                              String observacoes) {
        try {
            if (!verificarAcessoIdoso(idosoId)) {
                return error("Acesso não autorizado");
            }
            
            DietaService.ServiceResult result = app.getDietaService()
                .salvarDieta(idosoId, diaSemana, cafeManha, lancheManha, almoco, lancheTarde, jantar, ceia, observacoes);
            
            if (result.isSuccess()) {
                return success(result.getData());
            } else {
                return error(result.getMessage());
            }
        } catch (Exception e) {
            return error("Erro ao salvar dieta");
        }
    }
    
    @JavascriptInterface
    public String getDietaSemanal(long idosoId) {
        try {
            if (!verificarAcessoIdoso(idosoId)) {
                return error("Acesso não autorizado");
            }
            
            List<DietaSemanal> dietas = app.getDietaService().getDietaSemanal(idosoId);
            return success(dietas);
        } catch (Exception e) {
            return error("Erro ao obter dieta semanal");
        }
    }
    
    @JavascriptInterface
    public String getDietaHoje(long idosoId) {
        try {
            if (!verificarAcessoIdoso(idosoId)) {
                return error("Acesso não autorizado");
            }
            
            DietaSemanal dieta = app.getDietaService().getDietaHoje(idosoId);
            return success(dieta);
        } catch (Exception e) {
            return error("Erro ao obter dieta de hoje");
        }
    }
    
    @JavascriptInterface
    public String registrarRefeicao(long idosoId, String tipoRefeicao, String descricao, String observacoes) {
        try {
            if (!verificarAcessoIdoso(idosoId)) {
                return error("Acesso não autorizado");
            }
            
            DietaService.ServiceResult result = app.getDietaService()
                .registrarRefeicao(idosoId, tipoRefeicao, descricao, observacoes);
            
            if (result.isSuccess()) {
                return success(result.getData());
            } else {
                return error(result.getMessage());
            }
        } catch (Exception e) {
            return error("Erro ao registrar refeição");
        }
    }
    
    @JavascriptInterface
    public String getRegistrosRefeicaoHoje(long idosoId) {
        try {
            if (!verificarAcessoIdoso(idosoId)) {
                return error("Acesso não autorizado");
            }
            
            List<RegistroRefeicao> registros = app.getDietaService().getRegistrosHoje(idosoId);
            return success(registros);
        } catch (Exception e) {
            return error("Erro ao obter registros de refeição");
        }
    }
    
    // ==================== MEDICAMENTOS ====================
    
    @JavascriptInterface
    public String salvarMedicamento(long idosoId, String nome, String dosagem, String horario,
                                    String frequencia, String instrucoes, String dataFim) {
        try {
            if (!verificarAcessoCuidador(idosoId)) {
                return error("Acesso não autorizado");
            }
            
            MedicamentoService.ServiceResult result = app.getMedicamentoService()
                .salvarMedicamento(idosoId, nome, dosagem, horario, frequencia, instrucoes, dataFim);
            
            if (result.isSuccess()) {
                return success(result.getData());
            } else {
                return error(result.getMessage());
            }
        } catch (Exception e) {
            return error("Erro ao salvar medicamento");
        }
    }
    
    @JavascriptInterface
    public String atualizarMedicamento(long medicamentoId, String nome, String dosagem, String horario,
                                       String frequencia, String instrucoes, String dataFim, boolean ativo) {
        try {
            Medicamento medicamento = app.getMedicamentoService().getMedicamento(medicamentoId);
            if (medicamento == null) {
                return error("Medicamento não encontrado");
            }
            
            if (!verificarAcessoCuidador(medicamento.getIdosoId())) {
                return error("Acesso não autorizado");
            }
            
            MedicamentoService.ServiceResult result = app.getMedicamentoService()
                .atualizarMedicamento(medicamentoId, nome, dosagem, horario, frequencia, instrucoes, dataFim, ativo);
            
            if (result.isSuccess()) {
                return success(result.getData());
            } else {
                return error(result.getMessage());
            }
        } catch (Exception e) {
            return error("Erro ao atualizar medicamento");
        }
    }
    
    @JavascriptInterface
    public String excluirMedicamento(long medicamentoId) {
        try {
            Medicamento medicamento = app.getMedicamentoService().getMedicamento(medicamentoId);
            if (medicamento == null) {
                return error("Medicamento não encontrado");
            }
            
            if (!verificarAcessoCuidador(medicamento.getIdosoId())) {
                return error("Acesso não autorizado");
            }
            
            MedicamentoService.ServiceResult result = app.getMedicamentoService().excluirMedicamento(medicamentoId);
            
            if (result.isSuccess()) {
                return success(result.getData());
            } else {
                return error(result.getMessage());
            }
        } catch (Exception e) {
            return error("Erro ao excluir medicamento");
        }
    }
    
    @JavascriptInterface
    public String getMedicamentos(long idosoId) {
        try {
            if (!verificarAcessoIdoso(idosoId)) {
                return error("Acesso não autorizado");
            }
            
            List<Medicamento> medicamentos = app.getMedicamentoService().getMedicamentosAtivos(idosoId);
            return success(medicamentos);
        } catch (Exception e) {
            return error("Erro ao obter medicamentos");
        }
    }
    
    @JavascriptInterface
    public String confirmarMedicamento(long idosoId, long medicamentoId) {
        try {
            if (!verificarAcessoIdosoLogado(idosoId)) {
                return error("Acesso não autorizado");
            }
            
            MedicamentoService.ServiceResult result = app.getMedicamentoService()
                .confirmarMedicamento(idosoId, medicamentoId);
            
            if (result.isSuccess()) {
                return success(result.getData());
            } else {
                return error(result.getMessage());
            }
        } catch (Exception e) {
            return error("Erro ao confirmar medicamento");
        }
    }
    
    @JavascriptInterface
    public String isMedicamentoConfirmadoHoje(long idosoId, long medicamentoId) {
        try {
            if (!verificarAcessoIdoso(idosoId)) {
                return error("Acesso não autorizado");
            }
            
            boolean confirmado = app.getMedicamentoService().isMedicamentoConfirmadoHoje(idosoId, medicamentoId);
            return success(confirmado);
        } catch (Exception e) {
            return error("Erro ao verificar medicamento");
        }
    }
    
    // ==================== EXERCÍCIOS ====================
    
    @JavascriptInterface
    public String salvarExercicio(long idosoId, String nome, String descricao, int duracaoMinutos,
                                  int frequenciaSemanal, String diasSemana, String instrucoes) {
        try {
            if (!verificarAcessoCuidador(idosoId)) {
                return error("Acesso não autorizado");
            }
            
            ExercicioService.ServiceResult result = app.getExercicioService()
                .salvarExercicio(idosoId, nome, descricao, duracaoMinutos, frequenciaSemanal, diasSemana, instrucoes);
            
            if (result.isSuccess()) {
                return success(result.getData());
            } else {
                return error(result.getMessage());
            }
        } catch (Exception e) {
            return error("Erro ao salvar exercício");
        }
    }
    
    @JavascriptInterface
    public String atualizarExercicio(long exercicioId, String nome, String descricao, int duracaoMinutos,
                                     int frequenciaSemanal, String diasSemana, String instrucoes, boolean ativo) {
        try {
            Exercicio exercicio = app.getExercicioService().getExercicio(exercicioId);
            if (exercicio == null) {
                return error("Exercício não encontrado");
            }
            
            if (!verificarAcessoCuidador(exercicio.getIdosoId())) {
                return error("Acesso não autorizado");
            }
            
            ExercicioService.ServiceResult result = app.getExercicioService()
                .atualizarExercicio(exercicioId, nome, descricao, duracaoMinutos, frequenciaSemanal, diasSemana, instrucoes, ativo);
            
            if (result.isSuccess()) {
                return success(result.getData());
            } else {
                return error(result.getMessage());
            }
        } catch (Exception e) {
            return error("Erro ao atualizar exercício");
        }
    }
    
    @JavascriptInterface
    public String excluirExercicio(long exercicioId) {
        try {
            Exercicio exercicio = app.getExercicioService().getExercicio(exercicioId);
            if (exercicio == null) {
                return error("Exercício não encontrado");
            }
            
            if (!verificarAcessoCuidador(exercicio.getIdosoId())) {
                return error("Acesso não autorizado");
            }
            
            ExercicioService.ServiceResult result = app.getExercicioService().excluirExercicio(exercicioId);
            
            if (result.isSuccess()) {
                return success(result.getData());
            } else {
                return error(result.getMessage());
            }
        } catch (Exception e) {
            return error("Erro ao excluir exercício");
        }
    }
    
    @JavascriptInterface
    public String getExercicios(long idosoId) {
        try {
            if (!verificarAcessoIdoso(idosoId)) {
                return error("Acesso não autorizado");
            }
            
            List<Exercicio> exercicios = app.getExercicioService().getExerciciosAtivos(idosoId);
            return success(exercicios);
        } catch (Exception e) {
            return error("Erro ao obter exercícios");
        }
    }
    
    @JavascriptInterface
    public String confirmarExercicio(long idosoId, long exercicioId, String observacoes) {
        try {
            if (!verificarAcessoIdosoLogado(idosoId)) {
                return error("Acesso não autorizado");
            }
            
            ExercicioService.ServiceResult result = app.getExercicioService()
                .confirmarExercicio(idosoId, exercicioId, observacoes);
            
            if (result.isSuccess()) {
                return success(result.getData());
            } else {
                return error(result.getMessage());
            }
        } catch (Exception e) {
            return error("Erro ao confirmar exercício");
        }
    }
    
    @JavascriptInterface
    public String isExercicioConcluidoHoje(long idosoId, long exercicioId) {
        try {
            if (!verificarAcessoIdoso(idosoId)) {
                return error("Acesso não autorizado");
            }
            
            boolean concluido = app.getExercicioService().isExercicioConcluidoHoje(idosoId, exercicioId);
            return success(concluido);
        } catch (Exception e) {
            return error("Erro ao verificar exercício");
        }
    }
    
    // ==================== VISITAS ====================
    
    @JavascriptInterface
    public String agendarVisita(long idosoId, String data, String horaInicio, String horaFim, String descricao) {
        try {
            Usuario usuario = app.getAuthService().getUsuarioLogado();
            if (usuario == null || usuario.getTipoPerfil() != TipoPerfil.CUIDADOR) {
                return error("Acesso não autorizado");
            }
            
            if (!app.getCuidadorService().verificarVinculo(usuario.getId(), idosoId)) {
                return error("Idoso não vinculado a este cuidador");
            }
            
            VisitaService.ServiceResult result = app.getVisitaService()
                .agendarVisita(idosoId, usuario.getId(), data, horaInicio, horaFim, descricao);
            
            if (result.isSuccess()) {
                return success(result.getData());
            } else {
                return error(result.getMessage());
            }
        } catch (Exception e) {
            return error("Erro ao agendar visita");
        }
    }
    
    @JavascriptInterface
    public String atualizarVisita(long visitaId, String data, String horaInicio, String horaFim,
                                  String descricao, String observacoes) {
        try {
            Usuario usuario = app.getAuthService().getUsuarioLogado();
            if (usuario == null || usuario.getTipoPerfil() != TipoPerfil.CUIDADOR) {
                return error("Acesso não autorizado");
            }
            
            VisitaService.ServiceResult result = app.getVisitaService()
                .atualizarVisita(visitaId, data, horaInicio, horaFim, descricao, observacoes);
            
            if (result.isSuccess()) {
                return success(result.getData());
            } else {
                return error(result.getMessage());
            }
        } catch (Exception e) {
            return error("Erro ao atualizar visita");
        }
    }
    
    @JavascriptInterface
    public String atualizarStatusVisita(long visitaId, String status) {
        try {
            StatusVisita statusVisita = StatusVisita.valueOf(status.toUpperCase());
            
            VisitaService.ServiceResult result = app.getVisitaService()
                .atualizarStatus(visitaId, statusVisita);
            
            if (result.isSuccess()) {
                return success(result.getData());
            } else {
                return error(result.getMessage());
            }
        } catch (Exception e) {
            return error("Erro ao atualizar status da visita");
        }
    }
    
    @JavascriptInterface
    public String excluirVisita(long visitaId) {
        try {
            Usuario usuario = app.getAuthService().getUsuarioLogado();
            if (usuario == null || usuario.getTipoPerfil() != TipoPerfil.CUIDADOR) {
                return error("Acesso não autorizado");
            }
            
            VisitaService.ServiceResult result = app.getVisitaService().excluirVisita(visitaId);
            
            if (result.isSuccess()) {
                return success(result.getData());
            } else {
                return error(result.getMessage());
            }
        } catch (Exception e) {
            return error("Erro ao excluir visita");
        }
    }
    
    @JavascriptInterface
    public String getVisitas(long idosoId) {
        try {
            if (!verificarAcessoIdoso(idosoId)) {
                return error("Acesso não autorizado");
            }
            
            List<Visita> visitas = app.getVisitaService().getVisitasByIdoso(idosoId);
            return success(visitas);
        } catch (Exception e) {
            return error("Erro ao obter visitas");
        }
    }
    
    @JavascriptInterface
    public String getProximasVisitas(long idosoId) {
        try {
            if (!verificarAcessoIdoso(idosoId)) {
                return error("Acesso não autorizado");
            }
            
            List<Visita> visitas = app.getVisitaService().getProximasVisitas(idosoId);
            return success(visitas);
        } catch (Exception e) {
            return error("Erro ao obter próximas visitas");
        }
    }
    
    // ==================== CHECK-IN DIÁRIO (ESTADO) ====================
    
    @JavascriptInterface
    public String registrarEstado(long idosoId, String estadoHumor, String estadoFisico, String observacoes) {
        try {
            if (!verificarAcessoIdosoLogado(idosoId)) {
                return error("Acesso não autorizado");
            }
            
            CheckInService.ServiceResult result = app.getCheckInService()
                .registrarEstado(idosoId, estadoHumor, estadoFisico, observacoes);
            
            if (result.isSuccess()) {
                return success(result.getData());
            } else {
                return error(result.getMessage());
            }
        } catch (Exception e) {
            return error("Erro ao registrar estado");
        }
    }
    
    @JavascriptInterface
    public String getEstadoHoje(long idosoId) {
        try {
            if (!verificarAcessoIdoso(idosoId)) {
                return error("Acesso não autorizado");
            }
            
            CheckInDiario estado = app.getCheckInService().getEstadoHoje(idosoId);
            return success(estado);
        } catch (Exception e) {
            return error("Erro ao obter estado de hoje");
        }
    }
    
    // ==================== EMERGÊNCIA ====================
    
    @JavascriptInterface
    public String criarAlertaEmergencia(long idosoId, String descricao) {
        try {
            if (!verificarAcessoIdosoLogado(idosoId)) {
                return error("Acesso não autorizado");
            }
            
            EmergenciaService.ServiceResult result = app.getEmergenciaService()
                .criarAlerta(idosoId, descricao);
            
            if (result.isSuccess()) {
                return success(result.getData());
            } else {
                return error(result.getMessage());
            }
        } catch (Exception e) {
            return error("Erro ao criar alerta de emergência");
        }
    }
    
    @JavascriptInterface
    public String getTelefoneEmergencia(long idosoId) {
        try {
            String telefone = app.getEmergenciaService().getTelefoneEmergencia(idosoId);
            return success(telefone);
        } catch (Exception e) {
            return error("Erro ao obter telefone de emergência");
        }
    }
    
    // ==================== PROGRESSO ====================
    
    @JavascriptInterface
    public String getProgressoSemana(long idosoId) {
        try {
            if (!verificarAcessoIdoso(idosoId)) {
                return error("Acesso não autorizado");
            }
            
            ProgressoService.ProgressoResumo resumo = app.getProgressoService()
                .calcularProgressoSemana(idosoId);
            
            Map<String, Object> data = new HashMap<>();
            data.put("refeicoesRegistradas", resumo.getRefeicoesRegistradas());
            data.put("refeicoesEsperadas", resumo.getRefeicoesEsperadas());
            data.put("percentualRefeicoes", resumo.getPercentualRefeicoes());
            data.put("medicamentosConfirmados", resumo.getMedicamentosConfirmados());
            data.put("medicamentosEsperados", resumo.getMedicamentosEsperados());
            data.put("percentualMedicamentos", resumo.getPercentualMedicamentos());
            data.put("exerciciosConcluidos", resumo.getExerciciosConcluidos());
            data.put("exerciciosEsperados", resumo.getExerciciosEsperados());
            data.put("percentualExercicios", resumo.getPercentualExercicios());
            data.put("checkInsRealizados", resumo.getCheckInsRealizados());
            data.put("checkInsEsperados", resumo.getCheckInsEsperados());
            data.put("percentualCheckIns", resumo.getPercentualCheckIns());
            data.put("dataInicio", resumo.getDataInicio().toString());
            data.put("dataFim", resumo.getDataFim().toString());
            
            return success(data);
        } catch (Exception e) {
            return error("Erro ao calcular progresso");
        }
    }
    
    @JavascriptInterface
    public String getProgressoMes(long idosoId) {
        try {
            if (!verificarAcessoIdoso(idosoId)) {
                return error("Acesso não autorizado");
            }
            
            ProgressoService.ProgressoResumo resumo = app.getProgressoService()
                .calcularProgressoMes(idosoId);
            
            Map<String, Object> data = new HashMap<>();
            data.put("refeicoesRegistradas", resumo.getRefeicoesRegistradas());
            data.put("refeicoesEsperadas", resumo.getRefeicoesEsperadas());
            data.put("percentualRefeicoes", resumo.getPercentualRefeicoes());
            data.put("medicamentosConfirmados", resumo.getMedicamentosConfirmados());
            data.put("medicamentosEsperados", resumo.getMedicamentosEsperados());
            data.put("percentualMedicamentos", resumo.getPercentualMedicamentos());
            data.put("exerciciosConcluidos", resumo.getExerciciosConcluidos());
            data.put("exerciciosEsperados", resumo.getExerciciosEsperados());
            data.put("percentualExercicios", resumo.getPercentualExercicios());
            data.put("checkInsRealizados", resumo.getCheckInsRealizados());
            data.put("checkInsEsperados", resumo.getCheckInsEsperados());
            data.put("percentualCheckIns", resumo.getPercentualCheckIns());
            data.put("dataInicio", resumo.getDataInicio().toString());
            data.put("dataFim", resumo.getDataFim().toString());
            
            return success(data);
        } catch (Exception e) {
            return error("Erro ao calcular progresso");
        }
    }
    
    // ==================== MÉTODOS AUXILIARES ====================
    
    private boolean verificarAcessoIdoso(long idosoId) {
        Usuario usuario = app.getAuthService().getUsuarioLogado();
        if (usuario == null) return false;
        
        if (usuario.getTipoPerfil() == TipoPerfil.IDOSO) {
            return usuario.getId() == idosoId;
        } else if (usuario.getTipoPerfil() == TipoPerfil.CUIDADOR) {
            return app.getCuidadorService().verificarVinculo(usuario.getId(), idosoId);
        }
        
        return false;
    }
    
    private boolean verificarAcessoIdosoLogado(long idosoId) {
        Usuario usuario = app.getAuthService().getUsuarioLogado();
        if (usuario == null) return false;
        
        return usuario.getTipoPerfil() == TipoPerfil.IDOSO && usuario.getId() == idosoId;
    }
    
    private boolean verificarAcessoCuidador(long idosoId) {
        Usuario usuario = app.getAuthService().getUsuarioLogado();
        if (usuario == null) return false;
        
        if (usuario.getTipoPerfil() == TipoPerfil.CUIDADOR) {
            return app.getCuidadorService().verificarVinculo(usuario.getId(), idosoId);
        }
        
        return false;
    }
    
    // ==================== ADAPTADORES JSON ====================
    
    private static class LocalDateAdapter implements com.google.gson.JsonSerializer<LocalDate>,
            com.google.gson.JsonDeserializer<LocalDate> {
        
        @Override
        public com.google.gson.JsonElement serialize(LocalDate src, java.lang.reflect.Type typeOfSrc,
                com.google.gson.JsonSerializationContext context) {
            return new com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        
        @Override
        public LocalDate deserialize(com.google.gson.JsonElement json, java.lang.reflect.Type typeOfT,
                com.google.gson.JsonDeserializationContext context) {
            return LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE);
        }
    }
    
    private static class LocalTimeAdapter implements com.google.gson.JsonSerializer<LocalTime>,
            com.google.gson.JsonDeserializer<LocalTime> {
        
        @Override
        public com.google.gson.JsonElement serialize(LocalTime src, java.lang.reflect.Type typeOfSrc,
                com.google.gson.JsonSerializationContext context) {
            return new com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_TIME));
        }
        
        @Override
        public LocalTime deserialize(com.google.gson.JsonElement json, java.lang.reflect.Type typeOfT,
                com.google.gson.JsonDeserializationContext context) {
            return LocalTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_TIME);
        }
    }
    
    private static class LocalDateTimeAdapter implements com.google.gson.JsonSerializer<LocalDateTime>,
            com.google.gson.JsonDeserializer<LocalDateTime> {
        
        @Override
        public com.google.gson.JsonElement serialize(LocalDateTime src, java.lang.reflect.Type typeOfSrc,
                com.google.gson.JsonSerializationContext context) {
            return new com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        @Override
        public LocalDateTime deserialize(com.google.gson.JsonElement json, java.lang.reflect.Type typeOfT,
                com.google.gson.JsonDeserializationContext context) {
            return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }
}
