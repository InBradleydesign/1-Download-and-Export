package com.cuidarapp.repository;

import com.cuidarapp.data.dao.VisitaDao;
import com.cuidarapp.model.StatusVisita;
import com.cuidarapp.model.Visita;

import java.time.LocalDate;
import java.util.List;

public class VisitaRepository {
    
    private final VisitaDao visitaDao;
    
    public VisitaRepository(VisitaDao visitaDao) {
        this.visitaDao = visitaDao;
    }
    
    public long insert(Visita visita) {
        return visitaDao.insert(visita);
    }
    
    public void update(Visita visita) {
        visitaDao.update(visita);
    }
    
    public void delete(Visita visita) {
        visitaDao.delete(visita);
    }
    
    public Visita getById(long id) {
        return visitaDao.getById(id);
    }
    
    public List<Visita> getByIdosoId(long idosoId) {
        return visitaDao.getByIdosoId(idosoId);
    }
    
    public List<Visita> getByCuidadorId(long cuidadorId) {
        return visitaDao.getByCuidadorId(cuidadorId);
    }
    
    public List<Visita> getProximasByIdosoId(long idosoId) {
        return visitaDao.getProximasByIdosoId(idosoId, LocalDate.now());
    }
    
    public List<Visita> getByIdosoIdAndPeriodo(long idosoId, LocalDate dataInicio, LocalDate dataFim) {
        return visitaDao.getByIdosoIdAndPeriodo(idosoId, dataInicio, dataFim);
    }
    
    public void updateStatus(long id, StatusVisita status) {
        visitaDao.updateStatus(id, status);
    }
    
    public void deleteById(long id) {
        visitaDao.deleteById(id);
    }
}
