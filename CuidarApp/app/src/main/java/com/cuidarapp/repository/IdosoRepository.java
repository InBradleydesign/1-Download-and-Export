package com.cuidarapp.repository;

import com.cuidarapp.data.dao.IdosoDao;
import com.cuidarapp.model.Idoso;

import java.util.List;

public class IdosoRepository {
    
    private final IdosoDao idosoDao;
    
    public IdosoRepository(IdosoDao idosoDao) {
        this.idosoDao = idosoDao;
    }
    
    public long insert(Idoso idoso) {
        return idosoDao.insert(idoso);
    }
    
    public void update(Idoso idoso) {
        idosoDao.update(idoso);
    }
    
    public void delete(Idoso idoso) {
        idosoDao.delete(idoso);
    }
    
    public Idoso getById(long id) {
        return idosoDao.getById(id);
    }
    
    public Idoso getByEmail(String email) {
        return idosoDao.getByEmail(email);
    }
    
    public List<Idoso> getByCuidadorId(long cuidadorId) {
        return idosoDao.getByCuidadorId(cuidadorId);
    }
    
    public List<Idoso> getAll() {
        return idosoDao.getAll();
    }
    
    public int count() {
        return idosoDao.count();
    }
    
    public int countByCuidadorId(long cuidadorId) {
        return idosoDao.countByCuidadorId(cuidadorId);
    }
    
    public void deleteById(long id) {
        idosoDao.deleteById(id);
    }
}
