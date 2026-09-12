package com.cuidarapp.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.cuidarapp.model.StatusVisita;
import com.cuidarapp.model.Visita;

import java.time.LocalDate;
import java.util.List;

@Dao
public interface VisitaDao {
    
    @Insert
    long insert(Visita visita);
    
    @Update
    void update(Visita visita);
    
    @Delete
    void delete(Visita visita);
    
    @Query("SELECT * FROM visitas WHERE id = :id")
    Visita getById(long id);
    
    @Query("SELECT * FROM visitas WHERE idoso_id = :idosoId ORDER BY data DESC, hora_inicio ASC")
    List<Visita> getByIdosoId(long idosoId);
    
    @Query("SELECT * FROM visitas WHERE cuidador_id = :cuidadorId ORDER BY data DESC, hora_inicio ASC")
    List<Visita> getByCuidadorId(long cuidadorId);
    
    @Query("SELECT * FROM visitas WHERE idoso_id = :idosoId AND data >= :dataAtual AND (status = 'AGENDADA' OR status = 'CONFIRMADA') ORDER BY data ASC, hora_inicio ASC")
    List<Visita> getProximasByIdosoId(long idosoId, LocalDate dataAtual);
    
    @Query("SELECT * FROM visitas WHERE idoso_id = :idosoId AND data BETWEEN :dataInicio AND :dataFim ORDER BY data ASC, hora_inicio ASC")
    List<Visita> getByIdosoIdAndPeriodo(long idosoId, LocalDate dataInicio, LocalDate dataFim);
    
    @Query("UPDATE visitas SET status = :status WHERE id = :id")
    void updateStatus(long id, StatusVisita status);
    
    @Query("DELETE FROM visitas WHERE id = :id")
    void deleteById(long id);
}
