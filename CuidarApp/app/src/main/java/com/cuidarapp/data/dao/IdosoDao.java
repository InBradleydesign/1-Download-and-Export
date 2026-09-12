package com.cuidarapp.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.cuidarapp.model.Idoso;

import java.util.List;

@Dao
public interface IdosoDao {
    
    @Insert
    long insert(Idoso idoso);
    
    @Update
    void update(Idoso idoso);
    
    @Delete
    void delete(Idoso idoso);
    
    @Query("SELECT * FROM idosos WHERE id = :id")
    Idoso getById(long id);
    
    @Query("SELECT * FROM idosos WHERE email = :email")
    Idoso getByEmail(String email);
    
    @Query("SELECT * FROM idosos WHERE cuidador_id = :cuidadorId")
    List<Idoso> getByCuidadorId(long cuidadorId);
    
    @Query("SELECT * FROM idosos")
    List<Idoso> getAll();
    
    @Query("SELECT COUNT(*) FROM idosos")
    int count();
    
    @Query("SELECT COUNT(*) FROM idosos WHERE cuidador_id = :cuidadorId")
    int countByCuidadorId(long cuidadorId);
    
    @Query("DELETE FROM idosos WHERE id = :id")
    void deleteById(long id);
}
