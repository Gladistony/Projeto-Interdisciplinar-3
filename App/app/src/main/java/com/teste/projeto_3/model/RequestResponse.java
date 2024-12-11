package com.teste.projeto_3.model;

import com.google.gson.annotations.SerializedName;

public class RequestResponse {
    public RequestResponse(String id,String status, int code, String message, String usuario, String senha, String email, String nome_completo, String criacao, String ultimo_login, String url_foto) {
        this.id = id;
        this.status = status;
        this.code = code;
        this.message = message;
        this.usuario = usuario;
        this.senha = senha;
        this.email = email;
        this.nome_completo = nome_completo;
        this.criacao = criacao;
        this.ultimo_login = ultimo_login;
        this.url_foto = url_foto;
    }

    @SerializedName("id")
    private String id;

    @SerializedName("status")
    private String status;

    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    @SerializedName("usuario")
    private String usuario;

    @SerializedName("senha")
    private String senha;

    @SerializedName("email")
    private String email;

    @SerializedName("nome_completo")
    private String nome_completo;

    @SerializedName("criacao")
    private String criacao;

    @SerializedName("ultimo_login")
    private String ultimo_login;

    @SerializedName("url_foto")
    private String url_foto;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getSenha(){
        return senha;
    }

    public void setSenha(String senha){
        this.senha = senha;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNome_completo() {
        return nome_completo;
    }

    public void setNome_completo(String nome_completo) {
        this.nome_completo = nome_completo;
    }

    public String getCriacao() {
        return criacao;
    }

    public void setCriacao(String criacao) {
        this.criacao = criacao;
    }

    public String getUltimo_login() {
        return ultimo_login;
    }

    public void setUltimo_login(String ultimo_login) {
        this.ultimo_login = ultimo_login;
    }

    public String getUrl_foto() {
        return url_foto;
    }

    public void setUrl_foto(String url_foto) {
        this.url_foto = url_foto;
    }

    public String getStatus(){
        return status;
    }
    public String getMessage(){
        return message;
    }
}
