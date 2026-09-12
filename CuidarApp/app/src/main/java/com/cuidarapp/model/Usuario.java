package com.cuidarapp.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "usuarios")
public abstract class Usuario {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @ColumnInfo(name = "nome")
    private String nome;
    
    @ColumnInfo(name = "email")
    private String email;
    
    @ColumnInfo(name = "senha_hash")
    private String senhaHash;
    
    @ColumnInfo(name = "senha_salt")
    private String senhaSalt;
    
    @ColumnInfo(name = "tamanho_fonte")
    private float tamanhoFonte;
    
    @ColumnInfo(name = "tipo_perfil")
    private TipoPerfil tipoPerfil;
    
    private static final float FONTE_MIN = 0.8f;
    private static final float FONTE_MAX = 2.0f;
    private static final float FONTE_PADRAO = 1.0f;
    
    public Usuario() {
        this.tamanhoFonte = FONTE_PADRAO;
    }
    
    @Ignore
    public Usuario(String nome, String email, String senhaHash, String senhaSalt, TipoPerfil tipoPerfil) {
        this.nome = validarNome(nome);
        this.email = email;
        this.senhaHash = senhaHash;
        this.senhaSalt = senhaSalt;
        this.tipoPerfil = tipoPerfil;
        this.tamanhoFonte = FONTE_PADRAO;
    }
    
    public String validarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return "Não informado";
        }
        return nome.trim();
    }
    
    public void ajustarFonte(float delta) {
        float novoTamanho = this.tamanhoFonte + delta;
        if (novoTamanho < FONTE_MIN) {
            this.tamanhoFonte = FONTE_MIN;
        } else if (novoTamanho > FONTE_MAX) {
            this.tamanhoFonte = FONTE_MAX;
        } else {
            this.tamanhoFonte = novoTamanho;
        }
    }
    
    public void setTamanhoFonteDirectly(float tamanho) {
        if (tamanho < FONTE_MIN) {
            this.tamanhoFonte = FONTE_MIN;
        } else if (tamanho > FONTE_MAX) {
            this.tamanhoFonte = FONTE_MAX;
        } else {
            this.tamanhoFonte = tamanho;
        }
    }
    
    public abstract TipoPerfil getPerfil();
    
    // Getters e Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = validarNome(nome);
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getSenhaHash() {
        return senhaHash;
    }
    
    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }
    
    public String getSenhaSalt() {
        return senhaSalt;
    }
    
    public void setSenhaSalt(String senhaSalt) {
        this.senhaSalt = senhaSalt;
    }
    
    public float getTamanhoFonte() {
        return tamanhoFonte;
    }
    
    public void setTamanhoFonte(float tamanhoFonte) {
        setTamanhoFonteDirectly(tamanhoFonte);
    }
    
    public TipoPerfil getTipoPerfil() {
        return tipoPerfil;
    }
    
    public void setTipoPerfil(TipoPerfil tipoPerfil) {
        this.tipoPerfil = tipoPerfil;
    }
    
    public static float getFonteMin() {
        return FONTE_MIN;
    }
    
    public static float getFonteMax() {
        return FONTE_MAX;
    }
    
    public static float getFontePadrao() {
        return FONTE_PADRAO;
    }
}
