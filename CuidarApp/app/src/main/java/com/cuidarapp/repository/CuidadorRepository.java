package com.cuidarapp.repository;

import com.cuidarapp.data.dao.CuidadorDao;
import com.cuidarapp.model.Cuidador;

import java.util.List;

public class CuidadorRepository {
    
    private final CuidadorDao cuidadorDao;
    
    public CuidadorRepository(CuidadorDao cuidadorDao) {
        this.cuidadorDao = cuidadorDao;
    }
    
    public long insert(Cuidador cuidador) {
        return cuidadorDao.insert(cuidador);
    }
    
    public void update(Cuidador cuidador) {
        cuidadorDao.update(cuidador);
    }
    
    public void delete(Cuidador cuidador) {
        cuidadorDao.delete(cuidador);
    }
    
    public Cuidador getById(long id) {
        return cuidadorDao.getById(id);
    }
    
    public Cuidador getByEmail(String email) {
        return cuidadorDao.getByEmail(email);
    }
    
    public List<Cuidador> getAll() {
        return cuidadorDao.getAll();
    }
    
    public int count() {
        return cuidadorDao.count();
    }
    
    public void deleteById(long id) {
        cuidadorDao.deleteById(id);
    }
}
