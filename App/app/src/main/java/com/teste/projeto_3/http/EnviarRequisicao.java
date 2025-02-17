package com.teste.projeto_3.http;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

public class EnviarRequisicao extends AppCompatActivity {

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

    public void salvarMemoriaInterna(String keyString, String stringSalvar) {
        getSharedPreferences("AppPrefs", MODE_PRIVATE)
                .edit()
                .putString(keyString, stringSalvar)
                .commit(); // commit faz ser síncrono, apply é assíncrono
    }

    public String obterMemoriaInterna(String keyString) {
        return getSharedPreferences("AppPrefs", MODE_PRIVATE)
                .getString(keyString, "Chave não possui valor"); // Default value is an empty string
    }

    // Interface de callback para tratar respostas
    public interface Callback {
        void onResponse(String response);
    }
}
