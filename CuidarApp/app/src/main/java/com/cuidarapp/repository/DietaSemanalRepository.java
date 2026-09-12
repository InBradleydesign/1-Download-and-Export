package com.cuidarapp.repository;

import com.cuidarapp.data.dao.DietaSemanalDao;
import com.cuidarapp.model.DietaSemanal;

import java.util.List;

public class DietaSemanalRepository {
    
    private final DietaSemanalDao dietaSemanalDao;
    
    public DietaSemanalRepository(DietaSemanalDao dietaSemanalDao) {
        this.dietaSemanalDao = dietaSemanalDao;
    }
    
    public long insert(DietaSemanal dieta) {
        return dietaSemanalDao.insert(dieta);
    }
    
    public void update(DietaSemanal dieta) {
        dietaSemanalDao.update(dieta);
    }
    
    public void delete(DietaSemanal dieta) {
        dietaSemanalDao.delete(dieta);
    }
    
    public DietaSemanal getById(long id) {
        return dietaSemanalDao.getById(id);
    }
    
    public List<DietaSemanal> getByIdosoId(long idosoId) {
        return dietaSemanalDao.getByIdosoId(idosoId);
    }
    
    public DietaSemanal getByIdosoIdAndDia(long idosoId, int diaSemana) {
        return dietaSemanalDao.getByIdosoIdAndDia(idosoId, diaSemana);
    }
    
    public void deleteByIdosoId(long idosoId) {
        dietaSemanalDao.deleteByIdosoId(idosoId);
    }
    
    public void deleteById(long id) {
        dietaSemanalDao.deleteById(id);
    }
}
