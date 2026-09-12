package com.cuidarapp.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

@Entity(
    tableName = "idosos",
    foreignKeys = @ForeignKey(
        entity = Cuidador.class,
        parentColumns = "id",
        childColumns = "cuidador_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("cuidador_id")}
)
public class Idoso extends Usuario {
    
    @ColumnInfo(name = "cuidador_id")
    private long cuidadorId;
    
    @ColumnInfo(name = "telefone_emergencia")
    private String telefoneEmergencia;
    
    @ColumnInfo(name = "observacoes")
    private String observacoes;
    
    public Idoso() {
        super();
        setTipoPerfil(TipoPerfil.IDOSO);
        this.telefoneEmergencia = "192";
    }
    
    @Ignore
    public Idoso(String nome, String email, String senhaHash, String senhaSalt, long cuidadorId) {
        super(nome, email, senhaHash, senhaSalt, TipoPerfil.IDOSO);
        this.cuidadorId = cuidadorId;
        this.telefoneEmergencia = "192";
    }
    
    @Override
    public TipoPerfil getPerfil() {
        return TipoPerfil.IDOSO;
    }
    
    // Getters e Setters
    public long getCuidadorId() {
        return cuidadorId;
    }
    
    public void setCuidadorId(long cuidadorId) {
        this.cuidadorId = cuidadorId;
    }
    
    public String getTelefoneEmergencia() {
        return telefoneEmergencia;
    }
    
    public void setTelefoneEmergencia(String telefoneEmergencia) {
        this.telefoneEmergencia = telefoneEmergencia != null ? telefoneEmergencia : "192";
    }
    
    public String getObservacoes() {
        return observacoes;
    }
    
    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
