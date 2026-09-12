package com.cuidarapp.repository;

import com.cuidarapp.data.dao.MedicamentoDao;
import com.cuidarapp.model.Medicamento;

import java.util.List;

public class MedicamentoRepository {
    
    private final MedicamentoDao medicamentoDao;
    
    public MedicamentoRepository(MedicamentoDao medicamentoDao) {
        this.medicamentoDao = medicamentoDao;
    }
    
    public long insert(Medicamento medicamento) {
        return medicamentoDao.insert(medicamento);
    }
    
    public void update(Medicamento medicamento) {
        medicamentoDao.update(medicamento);
    }
    
    public void delete(Medicamento medicamento) {
        medicamentoDao.delete(medicamento);
    }
    
    public Medicamento getById(long id) {
        return medicamentoDao.getById(id);
    }
    
    public List<Medicamento> getByIdosoId(long idosoId) {
        return medicamentoDao.getByIdosoId(idosoId);
    }
    
    public List<Medicamento> getAtivosByIdosoId(long idosoId) {
        return medicamentoDao.getAtivosByIdosoId(idosoId);
    }
    
    public int countAtivosByIdosoId(long idosoId) {
        return medicamentoDao.countAtivosByIdosoId(idosoId);
    }
    
    public void deleteById(long id) {
        medicamentoDao.deleteById(id);
    }
}
