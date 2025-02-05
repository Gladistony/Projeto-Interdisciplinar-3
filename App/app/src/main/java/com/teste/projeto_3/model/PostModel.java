package com.teste.projeto_3.model;

import com.google.gson.annotations.SerializedName;
public class PostModel {
    @SerializedName("id")
    private String id;

    @SerializedName("usuario")
    private String usuario;

    @SerializedName("senha")
    private String senha;

    @SerializedName("nome_completo")
    private String nomeCompleto;

    @SerializedName("email")
    private String email;

    @SerializedName("urlFoto")
    private String urlFoto;


    public PostModel(String id, String usuario, String senha, String email, String nomeCompleto, String urlFoto) {
        this.id = id;
        this.usuario = usuario;
        this.senha = senha;
        this.email = email;
        this.nomeCompleto = nomeCompleto;
        this.urlFoto = urlFoto;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }
}
