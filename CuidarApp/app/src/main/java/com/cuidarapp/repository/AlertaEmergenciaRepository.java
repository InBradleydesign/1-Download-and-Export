package com.cuidarapp.repository;

import com.cuidarapp.data.dao.AlertaEmergenciaDao;
import com.cuidarapp.model.AlertaEmergencia;
import com.cuidarapp.model.StatusAlerta;

import java.util.List;

public class AlertaEmergenciaRepository {
    
    private final AlertaEmergenciaDao alertaEmergenciaDao;
    
    public AlertaEmergenciaRepository(AlertaEmergenciaDao alertaEmergenciaDao) {
        this.alertaEmergenciaDao = alertaEmergenciaDao;
    }
    
    public long insert(AlertaEmergencia alerta) {
        return alertaEmergenciaDao.insert(alerta);
    }
    
    public void update(AlertaEmergencia alerta) {
        alertaEmergenciaDao.update(alerta);
    }
    
    public void delete(AlertaEmergencia alerta) {
        alertaEmergenciaDao.delete(alerta);
    }
    
    public AlertaEmergencia getById(long id) {
        return alertaEmergenciaDao.getById(id);
    }
    
    public List<AlertaEmergencia> getByIdosoId(long idosoId) {
        return alertaEmergenciaDao.getByIdosoId(idosoId);
    }
    
    public List<AlertaEmergencia> getAtivosByIdosoId(long idosoId) {
        return alertaEmergenciaDao.getAtivosByIdosoId(idosoId);
    }
    
    public void updateStatus(long id, StatusAlerta status) {
        alertaEmergenciaDao.updateStatus(id, status);
    }
    
    public void deleteById(long id) {
        alertaEmergenciaDao.deleteById(id);
    }
}
