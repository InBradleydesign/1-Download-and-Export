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
    tableName = "medicamentos",
    foreignKeys = @ForeignKey(
        entity = Idoso.class,
        parentColumns = "id",
        childColumns = "idoso_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("idoso_id")}
)
public class Medicamento {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @ColumnInfo(name = "idoso_id")
    private long idosoId;
    
    @ColumnInfo(name = "nome")
    private String nome;
    
    @ColumnInfo(name = "dosagem")
    private String dosagem;
    
    @ColumnInfo(name = "horario")
    private LocalTime horario;
    
    @ColumnInfo(name = "frequencia")
    private String frequencia; // diario, a_cada_x_horas, dias_especificos
    
    @ColumnInfo(name = "dias_semana")
    private String diasSemana; // "1,2,3,4,5" para dias específicos
    
    @ColumnInfo(name = "instrucoes")
    private String instrucoes;
    
    @ColumnInfo(name = "data_inicio")
    private LocalDate dataInicio;
    
    @ColumnInfo(name = "data_fim")
    private LocalDate dataFim;
    
    @ColumnInfo(name = "ativo")
    private boolean ativo;
    
    public Medicamento() {
        this.ativo = true;
        this.dataInicio = LocalDate.now();
    }
    
    @Ignore
    public Medicamento(long idosoId, String nome, String dosagem, LocalTime horario) {
        this.idosoId = idosoId;
        this.nome = nome != null && !nome.trim().isEmpty() ? nome.trim() : "Não informado";
        this.dosagem = dosagem;
        this.horario = horario;
        this.ativo = true;
        this.dataInicio = LocalDate.now();
        this.frequencia = "diario";
    }
    
    public boolean isVigente() {
        LocalDate hoje = LocalDate.now();
        if (!ativo) return false;
        if (dataInicio != null && hoje.isBefore(dataInicio)) return false;
        if (dataFim != null && hoje.isAfter(dataFim)) return false;
        return true;
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
    
    public String getDosagem() {
        return dosagem;
    }
    
    public void setDosagem(String dosagem) {
        this.dosagem = dosagem;
    }
    
    public LocalTime getHorario() {
        return horario;
    }
    
    public void setHorario(LocalTime horario) {
        this.horario = horario;
    }
    
    public String getFrequencia() {
        return frequencia;
    }
    
    public void setFrequencia(String frequencia) {
        this.frequencia = frequencia;
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
    
    public LocalDate getDataInicio() {
        return dataInicio;
    }
    
    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }
    
    public LocalDate getDataFim() {
        return dataFim;
    }
    
    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }
    
    public boolean isAtivo() {
        return ativo;
    }
    
    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
