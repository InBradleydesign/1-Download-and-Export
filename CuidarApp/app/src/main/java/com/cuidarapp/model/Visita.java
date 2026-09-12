package com.cuidarapp.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity(
    tableName = "visitas",
    foreignKeys = {
        @ForeignKey(
            entity = Idoso.class,
            parentColumns = "id",
            childColumns = "idoso_id",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = Cuidador.class,
            parentColumns = "id",
            childColumns = "cuidador_id",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {@Index("idoso_id"), @Index("cuidador_id")}
)
public class Visita {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @ColumnInfo(name = "idoso_id")
    private long idosoId;
    
    @ColumnInfo(name = "cuidador_id")
    private long cuidadorId;
    
    @ColumnInfo(name = "data")
    private LocalDate data;
    
    @ColumnInfo(name = "hora_inicio")
    private LocalTime horaInicio;
    
    @ColumnInfo(name = "hora_fim")
    private LocalTime horaFim;
    
    @ColumnInfo(name = "descricao")
    private String descricao;
    
    @ColumnInfo(name = "status")
    private StatusVisita status;
    
    @ColumnInfo(name = "observacoes")
    private String observacoes;
    
    public Visita() {
        this.status = StatusVisita.AGENDADA;
    }
    
    @Ignore
    public Visita(long idosoId, long cuidadorId, LocalDate data, LocalTime horaInicio, String descricao) {
        this.idosoId = idosoId;
        this.cuidadorId = cuidadorId;
        this.data = data;
        this.horaInicio = horaInicio;
        this.descricao = descricao;
        this.status = StatusVisita.AGENDADA;
    }
    
    public String getStatusDescricao() {
        if (status == null) return "Desconhecido";
        switch (status) {
            case AGENDADA: return "Agendada";
            case CONFIRMADA: return "Confirmada";
            case REALIZADA: return "Realizada";
            case CANCELADA: return "Cancelada";
            default: return "Desconhecido";
        }
    }
    
    public boolean isPendente() {
        return status == StatusVisita.AGENDADA || status == StatusVisita.CONFIRMADA;
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
    
    public long getCuidadorId() {
        return cuidadorId;
    }
    
    public void setCuidadorId(long cuidadorId) {
        this.cuidadorId = cuidadorId;
    }
    
    public LocalDate getData() {
        return data;
    }
    
    public void setData(LocalDate data) {
        this.data = data;
    }
    
    public LocalTime getHoraInicio() {
        return horaInicio;
    }
    
    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }
    
    public LocalTime getHoraFim() {
        return horaFim;
    }
    
    public void setHoraFim(LocalTime horaFim) {
        this.horaFim = horaFim;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public StatusVisita getStatus() {
        return status;
    }
    
    public void setStatus(StatusVisita status) {
        this.status = status;
    }
    
    public String getObservacoes() {
        return observacoes;
    }
    
    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
