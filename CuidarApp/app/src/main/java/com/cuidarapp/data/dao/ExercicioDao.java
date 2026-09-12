package com.cuidarapp.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.cuidarapp.model.Exercicio;

import java.util.List;

@Dao
public interface ExercicioDao {
    
    @Insert
    long insert(Exercicio exercicio);
    
    @Update
    void update(Exercicio exercicio);
    
    @Delete
    void delete(Exercicio exercicio);
    
    @Query("SELECT * FROM exercicios WHERE id = :id")
    Exercicio getById(long id);
    
    @Query("SELECT * FROM exercicios WHERE idoso_id = :idosoId ORDER BY nome ASC")
    List<Exercicio> getByIdosoId(long idosoId);
    
    @Query("SELECT * FROM exercicios WHERE idoso_id = :idosoId AND ativo = 1 ORDER BY nome ASC")
    List<Exercicio> getAtivosByIdosoId(long idosoId);
    
    @Query("SELECT COUNT(*) FROM exercicios WHERE idoso_id = :idosoId AND ativo = 1")
    int countAtivosByIdosoId(long idosoId);
    
    @Query("DELETE FROM exercicios WHERE id = :id")
    void deleteById(long id);
}
