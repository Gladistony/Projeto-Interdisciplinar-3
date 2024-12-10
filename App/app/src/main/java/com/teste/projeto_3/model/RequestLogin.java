package com.teste.projeto_3.model;

import com.google.gson.annotations.SerializedName;

public class RequestLogin {
    public RequestLogin(String status, int code, String message, String usuario, String email, String nome_completo, String criacao, String ultimo_login, String url_foto) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.usuario = usuario;
        this.email = email;
        this.nome_completo = nome_completo;
        this.criacao = criacao;
        this.ultimo_login = ultimo_login;
        this.url_foto = url_foto;
    }

    @SerializedName("status")
    private String status;

    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    @SerializedName("usuario")
    private String usuario;

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

    public String getStatus(){
        return status;
    }
    public String getMessage(){
        return message;
    }
}
