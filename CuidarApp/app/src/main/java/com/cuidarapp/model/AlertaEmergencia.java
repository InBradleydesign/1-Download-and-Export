package com.cuidarapp.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;

@Entity(
    tableName = "alertas_emergencia",
    foreignKeys = @ForeignKey(
        entity = Idoso.class,
        parentColumns = "id",
        childColumns = "idoso_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("idoso_id")}
)
public class AlertaEmergencia {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @ColumnInfo(name = "idoso_id")
    private long idosoId;
    
    @ColumnInfo(name = "data_hora")
    private LocalDateTime dataHora;
    
    @ColumnInfo(name = "status")
    private StatusAlerta status;
    
    @ColumnInfo(name = "descricao")
    private String descricao;
    
    @ColumnInfo(name = "telefone_discado")
    private String telefoneDiscado;
    
    @ColumnInfo(name = "observacoes")
    private String observacoes;
    
    public AlertaEmergencia() {
        this.dataHora = LocalDateTime.now();
        this.status = StatusAlerta.ATIVO;
    }
    
    @Ignore
    public AlertaEmergencia(long idosoId, String descricao) {
        this.idosoId = idosoId;
        this.descricao = descricao;
        this.dataHora = LocalDateTime.now();
        this.status = StatusAlerta.ATIVO;
    }
    
    public String getStatusDescricao() {
        if (status == null) return "Desconhecido";
        switch (status) {
            case ATIVO: return "Ativo";
            case ATENDIDO: return "Atendido";
            case CANCELADO: return "Cancelado";
            default: return "Desconhecido";
        }
    }
    
    public void marcarComoAtendido() {
        this.status = StatusAlerta.ATENDIDO;
    }
    
    public void cancelar() {
        this.status = StatusAlerta.CANCELADO;
    }
    
    // Getters e Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public long getIdosoId() {
        return idosoId;
    }
    
    public void setIdosoId(long idosoId) {
        this.idosoId = idosoId;
    }
    
    public LocalDateTime getDataHora() {
        return dataHora;
    }
    
    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
    
    public StatusAlerta getStatus() {
        return status;
    }
    
    public void setStatus(StatusAlerta status) {
        this.status = status;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public String getTelefoneDiscado() {
        return telefoneDiscado;
    }
    
    public void setTelefoneDiscado(String telefoneDiscado) {
        this.telefoneDiscado = telefoneDiscado;
    }
    
    public String getObservacoes() {
        return observacoes;
    }
    
    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
