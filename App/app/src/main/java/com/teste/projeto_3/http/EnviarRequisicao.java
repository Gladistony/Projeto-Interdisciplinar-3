package com.teste.projeto_3.http;

import android.content.Context;

public class EnviarRequisicao {

    public Context context;

    public EnviarRequisicao(Context context) {
        this.context = context;
    }

    public void post(String method, String json, Callback callback) {
        new Thread(() -> {
            HttpHelper httpHelper = new HttpHelper();
            String response = httpHelper.post(method, json);
            callback.onResponse(response);
        }).start();
    }

    public void get(String method, String json, Callback callback) {
        new Thread(() -> {
            HttpHelper httpHelper = new HttpHelper();
            String response = httpHelper.get(method, json);
            callback.onResponse(response);
        }).start();
    }

    public void ativarConta(String usuario, String senha, Callback callback) {
        new Thread(() -> {
            HttpHelper httpHelper = new HttpHelper();
            String response = httpHelper.ativarConta(usuario, senha);
            callback.onResponse(response);
        }).start();
    }

    public void salvarMemoriaInterna(String keyString, String stringSalvar) {
        context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                .edit()
                .putString(keyString, stringSalvar)
                .commit(); // commit faz ser síncrono, apply é assíncrono
    }

    public String obterMemoriaInterna(String keyString) {
        return context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                .getString(keyString, "Chave não possui valor");
    }

    // Interface de callback para tratar respostas
    public interface Callback {
        void onResponse(String response);
    }
}
