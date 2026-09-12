package com.cuidarapp.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity(
    tableName = "checkins_diarios",
    foreignKeys = @ForeignKey(
        entity = Idoso.class,
        parentColumns = "id",
        childColumns = "idoso_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("idoso_id")}
)
public class CheckInDiario {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @ColumnInfo(name = "idoso_id")
    private long idosoId;
    
    @ColumnInfo(name = "data")
    private LocalDate data;
    
    @ColumnInfo(name = "data_hora")
    private LocalDateTime dataHora;
    
    @ColumnInfo(name = "tipo")
    private String tipo; // estado, medicamento, exercicio
    
    @ColumnInfo(name = "referencia_id")
    private long referenciaId; // ID do medicamento ou exercício, se aplicável
    
    @ColumnInfo(name = "estado_humor")
    private String estadoHumor; // otimo, bem, regular, mal, muito_mal
    
    @ColumnInfo(name = "estado_fisico")
    private String estadoFisico; // otimo, bem, regular, mal, muito_mal
    
    @ColumnInfo(name = "observacoes")
    private String observacoes;
    
    @ColumnInfo(name = "concluido")
    private boolean concluido;
    
    public CheckInDiario() {
        this.data = LocalDate.now();
        this.dataHora = LocalDateTime.now();
        this.concluido = false;
    }
    
    @Ignore
    public CheckInDiario(long idosoId, String tipo) {
        this.idosoId = idosoId;
        this.tipo = tipo;
        this.data = LocalDate.now();
        this.dataHora = LocalDateTime.now();
        this.concluido = false;
    }
    
    public String getDescricaoEstadoHumor() {
        if (estadoHumor == null) return "Não informado";
        switch (estadoHumor) {
            case "otimo": return "Ótimo";
            case "bem": return "Bem";
            case "regular": return "Regular";
            case "mal": return "Mal";
            case "muito_mal": return "Muito mal";
            default: return "Não informado";
        }
    }
    
    public String getDescricaoEstadoFisico() {
        if (estadoFisico == null) return "Não informado";
        switch (estadoFisico) {
            case "otimo": return "Ótimo";
            case "bem": return "Bem";
            case "regular": return "Regular";
            case "mal": return "Mal";
            case "muito_mal": return "Muito mal";
            default: return "Não informado";
        }
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
    
    public LocalDate getData() {
        return data;
    }
    
    public void setData(LocalDate data) {
        this.data = data;
    }
    
    public LocalDateTime getDataHora() {
        return dataHora;
    }
    
    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
    
    public String getTipo() {
        return tipo;
    }
    
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    
    public long getReferenciaId() {
        return referenciaId;
    }
    
    public void setReferenciaId(long referenciaId) {
        this.referenciaId = referenciaId;
    }
    
    public String getEstadoHumor() {
        return estadoHumor;
    }
    
    public void setEstadoHumor(String estadoHumor) {
        this.estadoHumor = estadoHumor;
    }
    
    public String getEstadoFisico() {
        return estadoFisico;
    }
    
    public void setEstadoFisico(String estadoFisico) {
        this.estadoFisico = estadoFisico;
    }
    
    public String getObservacoes() {
        return observacoes;
    }
    
    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
    
    public boolean isConcluido() {
        return concluido;
    }
    
    public void setConcluido(boolean concluido) {
        this.concluido = concluido;
    }
}
