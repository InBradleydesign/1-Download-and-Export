package com.cuidarapp.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.cuidarapp.model.DietaSemanal;

import java.util.List;

@Dao
public interface DietaSemanalDao {
    
    @Insert
    long insert(DietaSemanal dieta);
    
    @Update
    void update(DietaSemanal dieta);
    
    @Delete
    void delete(DietaSemanal dieta);
    
    @Query("SELECT * FROM dietas_semanais WHERE id = :id")
    DietaSemanal getById(long id);
    
    @Query("SELECT * FROM dietas_semanais WHERE idoso_id = :idosoId ORDER BY dia_semana ASC")
    List<DietaSemanal> getByIdosoId(long idosoId);
    
    @Query("SELECT * FROM dietas_semanais WHERE idoso_id = :idosoId AND dia_semana = :diaSemana")
    DietaSemanal getByIdosoIdAndDia(long idosoId, int diaSemana);
    
    @Query("DELETE FROM dietas_semanais WHERE idoso_id = :idosoId")
    void deleteByIdosoId(long idosoId);
    
    @Query("DELETE FROM dietas_semanais WHERE id = :id")
    void deleteById(long id);
}
