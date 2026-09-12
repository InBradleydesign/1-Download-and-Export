package com.cuidarapp.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.cuidarapp.model.AlertaEmergencia;
import com.cuidarapp.model.StatusAlerta;

import java.util.List;

@Dao
public interface AlertaEmergenciaDao {
    
    @Insert
    long insert(AlertaEmergencia alerta);
    
    @Update
    void update(AlertaEmergencia alerta);
    
    @Delete
    void delete(AlertaEmergencia alerta);
    
    @Query("SELECT * FROM alertas_emergencia WHERE id = :id")
    AlertaEmergencia getById(long id);
    
    @Query("SELECT * FROM alertas_emergencia WHERE idoso_id = :idosoId ORDER BY data_hora DESC")
    List<AlertaEmergencia> getByIdosoId(long idosoId);
    
    @Query("SELECT * FROM alertas_emergencia WHERE idoso_id = :idosoId AND status = 'ATIVO' ORDER BY data_hora DESC")
    List<AlertaEmergencia> getAtivosByIdosoId(long idosoId);
    
    @Query("UPDATE alertas_emergencia SET status = :status WHERE id = :id")
    void updateStatus(long id, StatusAlerta status);
    
    @Query("DELETE FROM alertas_emergencia WHERE id = :id")
    void deleteById(long id);
}
