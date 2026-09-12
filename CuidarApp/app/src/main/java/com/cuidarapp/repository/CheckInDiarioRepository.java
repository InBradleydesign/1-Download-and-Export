package com.cuidarapp.repository;

import com.cuidarapp.data.dao.CheckInDiarioDao;
import com.cuidarapp.model.CheckInDiario;

import java.time.LocalDate;
import java.util.List;

public class CheckInDiarioRepository {
    
    private final CheckInDiarioDao checkInDiarioDao;
    
    public CheckInDiarioRepository(CheckInDiarioDao checkInDiarioDao) {
        this.checkInDiarioDao = checkInDiarioDao;
    }
    
    public long insert(CheckInDiario checkIn) {
        return checkInDiarioDao.insert(checkIn);
    }
    
    public void update(CheckInDiario checkIn) {
        checkInDiarioDao.update(checkIn);
    }
    
    public void delete(CheckInDiario checkIn) {
        checkInDiarioDao.delete(checkIn);
    }
    
    public CheckInDiario getById(long id) {
        return checkInDiarioDao.getById(id);
    }
    
    public List<CheckInDiario> getByIdosoId(long idosoId) {
        return checkInDiarioDao.getByIdosoId(idosoId);
    }
    
    public List<CheckInDiario> getByIdosoIdAndData(long idosoId, LocalDate data) {
        return checkInDiarioDao.getByIdosoIdAndData(idosoId, data);
    }
    
    public List<CheckInDiario> getByIdosoIdTipoAndData(long idosoId, String tipo, LocalDate data) {
        return checkInDiarioDao.getByIdosoIdTipoAndData(idosoId, tipo, data);
    }
    
    public CheckInDiario getByIdosoIdTipoReferenciaAndData(long idosoId, String tipo, long referenciaId, LocalDate data) {
        return checkInDiarioDao.getByIdosoIdTipoReferenciaAndData(idosoId, tipo, referenciaId, data);
    }
    
    public List<CheckInDiario> getByIdosoIdAndPeriodo(long idosoId, LocalDate dataInicio, LocalDate dataFim) {
        return checkInDiarioDao.getByIdosoIdAndPeriodo(idosoId, dataInicio, dataFim);
    }
    
    public int countEstadosByIdosoIdAndPeriodo(long idosoId, LocalDate dataInicio, LocalDate dataFim) {
        return checkInDiarioDao.countEstadosByIdosoIdAndPeriodo(idosoId, dataInicio, dataFim);
    }
    
    public int countMedicamentosConfirmadosByIdosoIdAndPeriodo(long idosoId, LocalDate dataInicio, LocalDate dataFim) {
        return checkInDiarioDao.countMedicamentosConfirmadosByIdosoIdAndPeriodo(idosoId, dataInicio, dataFim);
    }
    
    public int countExerciciosConcluidosByIdosoIdAndPeriodo(long idosoId, LocalDate dataInicio, LocalDate dataFim) {
        return checkInDiarioDao.countExerciciosConcluidosByIdosoIdAndPeriodo(idosoId, dataInicio, dataFim);
    }
    
    public void deleteById(long id) {
        checkInDiarioDao.deleteById(id);
    }
}
