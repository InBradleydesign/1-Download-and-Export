package com.cuidarapp.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.LocalDate;

@Entity(
    tableName = "dietas_semanais",
    foreignKeys = @ForeignKey(
        entity = Idoso.class,
        parentColumns = "id",
        childColumns = "idoso_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("idoso_id")}
)
public class DietaSemanal {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @ColumnInfo(name = "idoso_id")
    private long idosoId;
    
    @ColumnInfo(name = "dia_semana")
    private int diaSemana; // 1=Segunda, 7=Domingo
    
    @ColumnInfo(name = "cafe_manha")
    private String cafeManha;
    
    @ColumnInfo(name = "lanche_manha")
    private String lancheManha;
    
    @ColumnInfo(name = "almoco")
    private String almoco;
    
    @ColumnInfo(name = "lanche_tarde")
    private String lancheTarde;
    
    @ColumnInfo(name = "jantar")
    private String jantar;
    
    @ColumnInfo(name = "ceia")
    private String ceia;
    
    @ColumnInfo(name = "observacoes")
    private String observacoes;
    
    @ColumnInfo(name = "data_criacao")
    private LocalDate dataCriacao;
    
    public DietaSemanal() {
        this.dataCriacao = LocalDate.now();
    }
    
    @Ignore
    public DietaSemanal(long idosoId, int diaSemana) {
        this.idosoId = idosoId;
        this.diaSemana = diaSemana;
        this.dataCriacao = LocalDate.now();
    }
    
    public String getNomeDiaSemana() {
        switch (diaSemana) {
            case 1: return "Segunda-feira";
            case 2: return "Terça-feira";
            case 3: return "Quarta-feira";
            case 4: return "Quinta-feira";
            case 5: return "Sexta-feira";
            case 6: return "Sábado";
            case 7: return "Domingo";
            default: return "Dia inválido";
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
    
    public int getDiaSemana() {
        return diaSemana;
    }
    
    public void setDiaSemana(int diaSemana) {
        this.diaSemana = Math.max(1, Math.min(7, diaSemana));
    }
    
    public String getCafeManha() {
        return cafeManha;
    }
    
    public void setCafeManha(String cafeManha) {
        this.cafeManha = cafeManha;
    }
    
    public String getLancheManha() {
        return lancheManha;
    }
    
    public void setLancheManha(String lancheManha) {
        this.lancheManha = lancheManha;
    }
    
    public String getAlmoco() {
        return almoco;
    }
    
    public void setAlmoco(String almoco) {
        this.almoco = almoco;
    }
    
    public String getLancheTarde() {
        return lancheTarde;
    }
    
    public void setLancheTarde(String lancheTarde) {
        this.lancheTarde = lancheTarde;
    }
    
    public String getJantar() {
        return jantar;
    }
    
    public void setJantar(String jantar) {
        this.jantar = jantar;
    }
    
    public String getCeia() {
        return ceia;
    }
    
    public void setCeia(String ceia) {
        this.ceia = ceia;
    }
    
    public String getObservacoes() {
        return observacoes;
    }
    
    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
    
    public LocalDate getDataCriacao() {
        return dataCriacao;
    }
    
    public void setDataCriacao(LocalDate dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}
