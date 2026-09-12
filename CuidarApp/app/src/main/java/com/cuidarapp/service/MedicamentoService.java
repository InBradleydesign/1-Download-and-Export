package com.cuidarapp.service;

import com.cuidarapp.model.CheckInDiario;
import com.cuidarapp.model.Medicamento;
import com.cuidarapp.repository.CheckInDiarioRepository;
import com.cuidarapp.repository.MedicamentoRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class MedicamentoService {
    
    private final MedicamentoRepository medicamentoRepository;
    private final CheckInDiarioRepository checkInDiarioRepository;
    
    public MedicamentoService(MedicamentoRepository medicamentoRepository,
                              CheckInDiarioRepository checkInDiarioRepository) {
        this.medicamentoRepository = medicamentoRepository;
        this.checkInDiarioRepository = checkInDiarioRepository;
    }
    
    public ServiceResult salvarMedicamento(long idosoId, String nome, String dosagem, 
                                           String horario, String frequencia, String instrucoes,
                                           String dataFim) {
        if (nome == null || nome.trim().isEmpty()) {
            return new ServiceResult(false, "Nome do medicamento não informado", null);
        }
        
        if (dosagem == null || dosagem.trim().isEmpty()) {
            return new ServiceResult(false, "Dosagem não informada", null);
        }
        
        LocalTime horarioLocal = null;
        if (horario != null && !horario.trim().isEmpty()) {
            try {
                horarioLocal = LocalTime.parse(horario);
            } catch (Exception e) {
                return new ServiceResult(false, "Horário inválido", null);
            }
        }
        
        Medicamento medicamento = new Medicamento(idosoId, sanitizeText(nome), sanitizeText(dosagem), horarioLocal);
        medicamento.setFrequencia(frequencia != null ? frequencia : "diario");
        medicamento.setInstrucoes(sanitizeText(instrucoes));
        
        if (dataFim != null && !dataFim.trim().isEmpty()) {
            try {
                medicamento.setDataFim(LocalDate.parse(dataFim));
            } catch (Exception e) {
                // Ignora data fim inválida
            }
        }
        
        long id = medicamentoRepository.insert(medicamento);
        medicamento.setId(id);
        
        return new ServiceResult(true, "Medicamento cadastrado com sucesso", id);
    }
    
    public ServiceResult atualizarMedicamento(long medicamentoId, String nome, String dosagem,
                                              String horario, String frequencia, String instrucoes,
                                              String dataFim, boolean ativo) {
        Medicamento medicamento = medicamentoRepository.getById(medicamentoId);
        if (medicamento == null) {
            return new ServiceResult(false, "Medicamento não encontrado", null);
        }
        
        if (nome == null || nome.trim().isEmpty()) {
            return new ServiceResult(false, "Nome do medicamento não informado", null);
        }
        
        if (dosagem == null || dosagem.trim().isEmpty()) {
            return new ServiceResult(false, "Dosagem não informada", null);
        }
        
        LocalTime horarioLocal = null;
        if (horario != null && !horario.trim().isEmpty()) {
            try {
                horarioLocal = LocalTime.parse(horario);
            } catch (Exception e) {
                return new ServiceResult(false, "Horário inválido", null);
            }
        }
        
        medicamento.setNome(sanitizeText(nome));
        medicamento.setDosagem(sanitizeText(dosagem));
        medicamento.setHorario(horarioLocal);
        medicamento.setFrequencia(frequencia != null ? frequencia : "diario");
        medicamento.setInstrucoes(sanitizeText(instrucoes));
        medicamento.setAtivo(ativo);
        
        if (dataFim != null && !dataFim.trim().isEmpty()) {
            try {
                medicamento.setDataFim(LocalDate.parse(dataFim));
            } catch (Exception e) {
                // Ignora data fim inválida
            }
        } else {
            medicamento.setDataFim(null);
        }
        
        medicamentoRepository.update(medicamento);
        
        return new ServiceResult(true, "Medicamento atualizado com sucesso", medicamentoId);
    }
    
    public ServiceResult excluirMedicamento(long medicamentoId) {
        Medicamento medicamento = medicamentoRepository.getById(medicamentoId);
        if (medicamento == null) {
            return new ServiceResult(false, "Medicamento não encontrado", null);
        }
        
        medicamentoRepository.deleteById(medicamentoId);
        return new ServiceResult(true, "Medicamento excluído com sucesso", medicamentoId);
    }
    
    public List<Medicamento> getMedicamentos(long idosoId) {
        return medicamentoRepository.getByIdosoId(idosoId);
    }
    
    public List<Medicamento> getMedicamentosAtivos(long idosoId) {
        return medicamentoRepository.getAtivosByIdosoId(idosoId);
    }
    
    public Medicamento getMedicamento(long medicamentoId) {
        return medicamentoRepository.getById(medicamentoId);
    }
    
    public ServiceResult confirmarMedicamento(long idosoId, long medicamentoId) {
        Medicamento medicamento = medicamentoRepository.getById(medicamentoId);
        if (medicamento == null) {
            return new ServiceResult(false, "Medicamento não encontrado", null);
        }
        
        if (medicamento.getIdosoId() != idosoId) {
            return new ServiceResult(false, "Medicamento não pertence a este idoso", null);
        }
        
        LocalDate hoje = LocalDate.now();
        CheckInDiario existente = checkInDiarioRepository
            .getByIdosoIdTipoReferenciaAndData(idosoId, "medicamento", medicamentoId, hoje);
        
        if (existente != null && existente.isConcluido()) {
            return new ServiceResult(false, "Medicamento já confirmado hoje", existente.getId());
        }
        
        CheckInDiario checkIn;
        if (existente != null) {
            checkIn = existente;
        } else {
            checkIn = new CheckInDiario(idosoId, "medicamento");
            checkIn.setReferenciaId(medicamentoId);
        }
        
        checkIn.setConcluido(true);
        
        if (existente != null) {
            checkInDiarioRepository.update(checkIn);
        } else {
            long id = checkInDiarioRepository.insert(checkIn);
            checkIn.setId(id);
        }
        
        return new ServiceResult(true, "Medicamento confirmado com sucesso", checkIn.getId());
    }
    
    public boolean isMedicamentoConfirmadoHoje(long idosoId, long medicamentoId) {
        CheckInDiario checkIn = checkInDiarioRepository
            .getByIdosoIdTipoReferenciaAndData(idosoId, "medicamento", medicamentoId, LocalDate.now());
        return checkIn != null && checkIn.isConcluido();
    }
    
    public int countMedicamentosConfirmados(long idosoId, LocalDate dataInicio, LocalDate dataFim) {
        return checkInDiarioRepository.countMedicamentosConfirmadosByIdosoIdAndPeriodo(idosoId, dataInicio, dataFim);
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
