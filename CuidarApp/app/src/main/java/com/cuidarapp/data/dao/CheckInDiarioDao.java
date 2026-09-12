package com.cuidarapp.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.cuidarapp.model.CheckInDiario;

import java.time.LocalDate;
import java.util.List;

@Dao
public interface CheckInDiarioDao {
    
    @Insert
    long insert(CheckInDiario checkIn);
    
    @Update
    void update(CheckInDiario checkIn);
    
    @Delete
    void delete(CheckInDiario checkIn);
    
    @Query("SELECT * FROM checkins_diarios WHERE id = :id")
    CheckInDiario getById(long id);
    
    @Query("SELECT * FROM checkins_diarios WHERE idoso_id = :idosoId ORDER BY data_hora DESC")
    List<CheckInDiario> getByIdosoId(long idosoId);
    
    @Query("SELECT * FROM checkins_diarios WHERE idoso_id = :idosoId AND data = :data ORDER BY data_hora DESC")
    List<CheckInDiario> getByIdosoIdAndData(long idosoId, LocalDate data);
    
    @Query("SELECT * FROM checkins_diarios WHERE idoso_id = :idosoId AND tipo = :tipo AND data = :data")
    List<CheckInDiario> getByIdosoIdTipoAndData(long idosoId, String tipo, LocalDate data);
    
    @Query("SELECT * FROM checkins_diarios WHERE idoso_id = :idosoId AND tipo = :tipo AND referencia_id = :referenciaId AND data = :data")
    CheckInDiario getByIdosoIdTipoReferenciaAndData(long idosoId, String tipo, long referenciaId, LocalDate data);
    
    @Query("SELECT * FROM checkins_diarios WHERE idoso_id = :idosoId AND data BETWEEN :dataInicio AND :dataFim ORDER BY data_hora DESC")
    List<CheckInDiario> getByIdosoIdAndPeriodo(long idosoId, LocalDate dataInicio, LocalDate dataFim);
    
    @Query("SELECT COUNT(*) FROM checkins_diarios WHERE idoso_id = :idosoId AND tipo = 'estado' AND data BETWEEN :dataInicio AND :dataFim")
    int countEstadosByIdosoIdAndPeriodo(long idosoId, LocalDate dataInicio, LocalDate dataFim);
    
    @Query("SELECT COUNT(*) FROM checkins_diarios WHERE idoso_id = :idosoId AND tipo = 'medicamento' AND concluido = 1 AND data BETWEEN :dataInicio AND :dataFim")
    int countMedicamentosConfirmadosByIdosoIdAndPeriodo(long idosoId, LocalDate dataInicio, LocalDate dataFim);
    
    @Query("SELECT COUNT(*) FROM checkins_diarios WHERE idoso_id = :idosoId AND tipo = 'exercicio' AND concluido = 1 AND data BETWEEN :dataInicio AND :dataFim")
    int countExerciciosConcluidosByIdosoIdAndPeriodo(long idosoId, LocalDate dataInicio, LocalDate dataFim);
    
    @Query("DELETE FROM checkins_diarios WHERE id = :id")
    void deleteById(long id);
}
