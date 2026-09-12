package com.cuidarapp.repository;

import com.cuidarapp.data.dao.ExercicioDao;
import com.cuidarapp.model.Exercicio;

import java.util.List;

public class ExercicioRepository {
    
    private final ExercicioDao exercicioDao;
    
    public ExercicioRepository(ExercicioDao exercicioDao) {
        this.exercicioDao = exercicioDao;
    }
    
    public long insert(Exercicio exercicio) {
        return exercicioDao.insert(exercicio);
    }
    
    public void update(Exercicio exercicio) {
        exercicioDao.update(exercicio);
    }
    
    public void delete(Exercicio exercicio) {
        exercicioDao.delete(exercicio);
    }
    
    public Exercicio getById(long id) {
        return exercicioDao.getById(id);
    }
    
    public List<Exercicio> getByIdosoId(long idosoId) {
        return exercicioDao.getByIdosoId(idosoId);
    }
    
    public List<Exercicio> getAtivosByIdosoId(long idosoId) {
        return exercicioDao.getAtivosByIdosoId(idosoId);
    }
    
    public int countAtivosByIdosoId(long idosoId) {
        return exercicioDao.countAtivosByIdosoId(idosoId);
    }
    
    public void deleteById(long id) {
        exercicioDao.deleteById(id);
    }
}
