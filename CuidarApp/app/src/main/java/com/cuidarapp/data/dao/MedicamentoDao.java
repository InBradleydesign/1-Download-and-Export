package com.cuidarapp.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.cuidarapp.model.Medicamento;

import java.util.List;

@Dao
public interface MedicamentoDao {
    
    @Insert
    long insert(Medicamento medicamento);
    
    @Update
    void update(Medicamento medicamento);
    
    @Delete
    void delete(Medicamento medicamento);
    
    @Query("SELECT * FROM medicamentos WHERE id = :id")
    Medicamento getById(long id);
    
    @Query("SELECT * FROM medicamentos WHERE idoso_id = :idosoId ORDER BY horario ASC")
    List<Medicamento> getByIdosoId(long idosoId);
    
    @Query("SELECT * FROM medicamentos WHERE idoso_id = :idosoId AND ativo = 1 ORDER BY horario ASC")
    List<Medicamento> getAtivosByIdosoId(long idosoId);
    
    @Query("SELECT COUNT(*) FROM medicamentos WHERE idoso_id = :idosoId AND ativo = 1")
    int countAtivosByIdosoId(long idosoId);
    
    @Query("DELETE FROM medicamentos WHERE id = :id")
    void deleteById(long id);
}
