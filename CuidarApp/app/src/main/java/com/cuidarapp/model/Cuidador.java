package com.cuidarapp.model;

import androidx.room.Entity;
import androidx.room.Ignore;

@Entity(tableName = "cuidadores")
public class Cuidador extends Usuario {
    
    public Cuidador() {
        super();
        setTipoPerfil(TipoPerfil.CUIDADOR);
    }
    
    @Ignore
    public Cuidador(String nome, String email, String senhaHash, String senhaSalt) {
        super(nome, email, senhaHash, senhaSalt, TipoPerfil.CUIDADOR);
    }
    
    @Override
    public TipoPerfil getPerfil() {
        return TipoPerfil.CUIDADOR;
    }
    
    public boolean cadastrarIdoso(Idoso idoso) {
        if (idoso == null) {
            return false;
        }
        idoso.setCuidadorId(this.getId());
        return true;
    }
}
