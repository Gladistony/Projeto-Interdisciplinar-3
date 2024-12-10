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




    public PostModel(String id, String request, String usuario, String senha) {
        this.id = id;
        this.request = request;
        this.usuario = usuario;
        this.senha = senha;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }
}
