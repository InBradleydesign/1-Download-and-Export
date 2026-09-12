package com.cuidarapp.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.LocalDate;

@Entity(
    tableName = "exercicios",
    foreignKeys = @ForeignKey(
        entity = Idoso.class,
        parentColumns = "id",
        childColumns = "idoso_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("idoso_id")}
)
public class Exercicio {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @ColumnInfo(name = "idoso_id")
    private long idosoId;
    
    @ColumnInfo(name = "nome")
    private String nome;
    
    @ColumnInfo(name = "descricao")
    private String descricao;
    
    @ColumnInfo(name = "duracao_minutos")
    private int duracaoMinutos;
    
    @ColumnInfo(name = "frequencia_semanal")
    private int frequenciaSemanal;
    
    @ColumnInfo(name = "dias_semana")
    private String diasSemana; // "1,3,5" para seg, qua, sex
    
    @ColumnInfo(name = "instrucoes")
    private String instrucoes;
    
    @ColumnInfo(name = "data_criacao")
    private LocalDate dataCriacao;
    
    @ColumnInfo(name = "ativo")
    private boolean ativo;
    
    public Exercicio() {
        this.ativo = true;
        this.dataCriacao = LocalDate.now();
    }
    
    @Ignore
    public Exercicio(long idosoId, String nome, String descricao, int duracaoMinutos) {
        this.idosoId = idosoId;
        this.nome = nome != null && !nome.trim().isEmpty() ? nome.trim() : "Não informado";
        this.descricao = descricao;
        this.duracaoMinutos = duracaoMinutos;
        this.ativo = true;
        this.dataCriacao = LocalDate.now();
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
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome != null && !nome.trim().isEmpty() ? nome.trim() : "Não informado";
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public int getDuracaoMinutos() {
        return duracaoMinutos;
    }
    
    public void setDuracaoMinutos(int duracaoMinutos) {
        this.duracaoMinutos = Math.max(0, duracaoMinutos);
    }
    
    public int getFrequenciaSemanal() {
        return frequenciaSemanal;
    }
    
    public void setFrequenciaSemanal(int frequenciaSemanal) {
        this.frequenciaSemanal = Math.max(0, Math.min(7, frequenciaSemanal));
    }
    
    public String getDiasSemana() {
        return diasSemana;
    }
    
    public void setDiasSemana(String diasSemana) {
        this.diasSemana = diasSemana;
    }
    
    public String getInstrucoes() {
        return instrucoes;
    }
    
    public void setInstrucoes(String instrucoes) {
        this.instrucoes = instrucoes;
    }
    
    public LocalDate getDataCriacao() {
        return dataCriacao;
    }
    
    public void setDataCriacao(LocalDate dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
    
    public boolean isAtivo() {
        return ativo;
    }
    
    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
