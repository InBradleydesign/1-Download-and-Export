package com.cuidarapp.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.cuidarapp.model.RegistroRefeicao;

import java.time.LocalDate;
import java.util.List;

@Dao
public interface RegistroRefeicaoDao {
    
    @Insert
    long insert(RegistroRefeicao registro);
    
    @Update
    void update(RegistroRefeicao registro);
    
    @Delete
    void delete(RegistroRefeicao registro);
    
    @Query("SELECT * FROM registros_refeicao WHERE id = :id")
    RegistroRefeicao getById(long id);
    
    @Query("SELECT * FROM registros_refeicao WHERE idoso_id = :idosoId ORDER BY data DESC, hora DESC")
    List<RegistroRefeicao> getByIdosoId(long idosoId);
    
    @Query("SELECT * FROM registros_refeicao WHERE idoso_id = :idosoId AND data = :data ORDER BY hora ASC")
    List<RegistroRefeicao> getByIdosoIdAndData(long idosoId, LocalDate data);
    
    @Query("SELECT * FROM registros_refeicao WHERE idoso_id = :idosoId AND data BETWEEN :dataInicio AND :dataFim ORDER BY data DESC, hora DESC")
    List<RegistroRefeicao> getByIdosoIdAndPeriodo(long idosoId, LocalDate dataInicio, LocalDate dataFim);
    
    @Query("SELECT COUNT(*) FROM registros_refeicao WHERE idoso_id = :idosoId AND data BETWEEN :dataInicio AND :dataFim")
    int countByIdosoIdAndPeriodo(long idosoId, LocalDate dataInicio, LocalDate dataFim);
    
    @Query("DELETE FROM registros_refeicao WHERE id = :id")
    void deleteById(long id);
}
