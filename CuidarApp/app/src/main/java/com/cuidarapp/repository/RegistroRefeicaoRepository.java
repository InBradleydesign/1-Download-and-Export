package com.cuidarapp.repository;

import com.cuidarapp.data.dao.RegistroRefeicaoDao;
import com.cuidarapp.model.RegistroRefeicao;

import java.time.LocalDate;
import java.util.List;

public class RegistroRefeicaoRepository {
    
    private final RegistroRefeicaoDao registroRefeicaoDao;
    
    public RegistroRefeicaoRepository(RegistroRefeicaoDao registroRefeicaoDao) {
        this.registroRefeicaoDao = registroRefeicaoDao;
    }
    
    public long insert(RegistroRefeicao registro) {
        return registroRefeicaoDao.insert(registro);
    }
    
    public void update(RegistroRefeicao registro) {
        registroRefeicaoDao.update(registro);
    }
    
    public void delete(RegistroRefeicao registro) {
        registroRefeicaoDao.delete(registro);
    }
    
    public RegistroRefeicao getById(long id) {
        return registroRefeicaoDao.getById(id);
    }
    
    public List<RegistroRefeicao> getByIdosoId(long idosoId) {
        return registroRefeicaoDao.getByIdosoId(idosoId);
    }
    
    public List<RegistroRefeicao> getByIdosoIdAndData(long idosoId, LocalDate data) {
        return registroRefeicaoDao.getByIdosoIdAndData(idosoId, data);
    }
    
    public List<RegistroRefeicao> getByIdosoIdAndPeriodo(long idosoId, LocalDate dataInicio, LocalDate dataFim) {
        return registroRefeicaoDao.getByIdosoIdAndPeriodo(idosoId, dataInicio, dataFim);
    }
    
    public int countByIdosoIdAndPeriodo(long idosoId, LocalDate dataInicio, LocalDate dataFim) {
        return registroRefeicaoDao.countByIdosoIdAndPeriodo(idosoId, dataInicio, dataFim);
    }
    
    public void deleteById(long id) {
        registroRefeicaoDao.deleteById(id);
    }
}
