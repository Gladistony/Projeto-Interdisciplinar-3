package com.teste.projeto_3.model;

import com.google.gson.annotations.SerializedName;
public class PostModel {
    @SerializedName("id")
    private String id;
    @SerializedName("request")
    private String request;

    @SerializedName("usuario")
    private String usuario;

    @SerializedName("senha")
    private String senha;

    @SerializedName("nome_completo")
    private String nome_completo;

    @SerializedName("email")
    private String email;




    public PostModel(String id, String request, String usuario, String senha, String email, String nome_completo) {
        this.id = id;
        this.request = request;
        this.usuario = usuario;
        this.senha = senha;
        this.email = email;
        this.nome_completo = nome_completo;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }
}
