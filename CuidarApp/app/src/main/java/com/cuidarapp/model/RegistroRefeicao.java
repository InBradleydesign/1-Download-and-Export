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
    tableName = "registros_refeicao",
    foreignKeys = @ForeignKey(
        entity = Idoso.class,
        parentColumns = "id",
        childColumns = "idoso_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("idoso_id")}
)
public class RegistroRefeicao {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @ColumnInfo(name = "idoso_id")
    private long idosoId;
    
    @ColumnInfo(name = "data")
    private LocalDate data;
    
    @ColumnInfo(name = "hora")
    private LocalTime hora;
    
    @ColumnInfo(name = "tipo_refeicao")
    private String tipoRefeicao; // cafe_manha, lanche_manha, almoco, lanche_tarde, jantar, ceia
    
    @ColumnInfo(name = "descricao")
    private String descricao;
    
    @ColumnInfo(name = "observacoes")
    private String observacoes;
    
    public RegistroRefeicao() {
        this.data = LocalDate.now();
        this.hora = LocalTime.now();
    }
    
    @Ignore
    public RegistroRefeicao(long idosoId, String tipoRefeicao, String descricao) {
        this.idosoId = idosoId;
        this.tipoRefeicao = tipoRefeicao;
        this.descricao = descricao;
        this.data = LocalDate.now();
        this.hora = LocalTime.now();
    }
    
    public String getNomeTipoRefeicao() {
        if (tipoRefeicao == null) return "Refeição";
        switch (tipoRefeicao) {
            case "cafe_manha": return "Café da manhã";
            case "lanche_manha": return "Lanche da manhã";
            case "almoco": return "Almoço";
            case "lanche_tarde": return "Lanche da tarde";
            case "jantar": return "Jantar";
            case "ceia": return "Ceia";
            default: return "Outra refeição";
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
    
    public LocalTime getHora() {
        return hora;
    }
    
    public void setHora(LocalTime hora) {
        this.hora = hora;
    }
    
    public String getTipoRefeicao() {
        return tipoRefeicao;
    }
    
    public void setTipoRefeicao(String tipoRefeicao) {
        this.tipoRefeicao = tipoRefeicao;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public String getObservacoes() {
        return observacoes;
    }
    
    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
