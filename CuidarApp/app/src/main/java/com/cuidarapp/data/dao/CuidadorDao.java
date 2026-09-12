package com.cuidarapp.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.cuidarapp.model.Cuidador;

import java.util.List;

@Dao
public interface CuidadorDao {
    
    @Insert
    long insert(Cuidador cuidador);
    
    @Update
    void update(Cuidador cuidador);
    
    @Delete
    void delete(Cuidador cuidador);
    
    @Query("SELECT * FROM cuidadores WHERE id = :id")
    Cuidador getById(long id);
    
    @Query("SELECT * FROM cuidadores WHERE email = :email")
    Cuidador getByEmail(String email);
    
    @Query("SELECT * FROM cuidadores")
    List<Cuidador> getAll();
    
    @Query("SELECT COUNT(*) FROM cuidadores")
    int count();
    
    @Query("DELETE FROM cuidadores WHERE id = :id")
    void deleteById(long id);
}
