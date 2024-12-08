package com.teste.projeto_3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.teste.projeto_3.http.HttpHelper;
import com.teste.projeto_3.model.User;

import org.jetbrains.annotations.NotNull;

public class TelaLogin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    public void logar(View v) {
        gerarNovoID();
        EditText usuario = findViewById(R.id.usuario);
        EditText senha = findViewById(R.id.senha);

        // Criar o objeto User para a primeira requisição
        User user = new User();
        user.setRequest("login");
        user.setUsuario(usuario.getText().toString());
        user.setSenha(senha.getText().toString());
        user.setId(obterIdConexao());

        // Converter o objeto User para JSON
        Gson gson = new Gson();
        String userJson = gson.toJson(user);
        System.out.println("nada");

        // Fazer a primeira requisição
        enviarRequisicao(userJson, response -> {
            // Verificar o conteúdo do JSON recebido
            if (response.startsWith("Erro")) {
                runOnUiThread(() -> Toast.makeText(this, response, Toast.LENGTH_LONG).show());
            } else {
                try {
                    User responseUser = gson.fromJson(response, User.class);
                    String userId = responseUser.getId(); // Captura o ID
                } catch (JsonSyntaxException e) {
                    runOnUiThread(() -> Toast.makeText(this, "Resposta inválida do servidor: " + response, Toast.LENGTH_LONG).show());
                }
            }
        });
    }


    // Método auxiliar para enviar requisições
    private void enviarRequisicao(String json, FormCadastro.Callback callback) {
        new Thread(() -> {
            HttpHelper httpHelper = new HttpHelper();
            String response = httpHelper.post(json);
            callback.onResponse(response);
        }).start();
    }

    private void gerarNovoID() {
        String retorno = "Erro na obtenção de um ID.";
        // Criar o objeto User para a primeira requisição
        User user = new User();
        user.setId("null");

        // Converter o objeto User para JSON
        Gson gson = new Gson();
        String userJson = gson.toJson(user);

        // Fazer a primeira requisição
        enviarRequisicao(userJson, response -> {
            if (response.startsWith("Erro")) {
                runOnUiThread(() -> Toast.makeText(this, response, Toast.LENGTH_LONG).show());
            } else {
                // Processar resposta da primeira requisição
                User responseUser = gson.fromJson(response, User.class);
                String userId = responseUser.getId(); // Captura o ID
                salvarIdConexao(userId);
            }
        });
    }

    // Método para salvar o ID de conexão localmente
    private void salvarIdConexao(@NotNull String idConexao) {
        getSharedPreferences("AppPrefs", MODE_PRIVATE)
                .edit()
                .putString("idConexao", idConexao)
                .apply();
    }


    private String obterIdConexao() {
        return getSharedPreferences("AppPrefs", MODE_PRIVATE)
                .getString("idConexao", "defaultString");
    }

    public void togglePassword(View v){
        EditText caixaTexto = findViewById(R.id.senha);
        int posicaoCursor = caixaTexto.getSelectionStart();

        if (caixaTexto.getInputType() == InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            // se inputType == 1 + 128 (código de textPassword) então inputType = 1 + 144 (código de textVisiblePassword)
            caixaTexto.setInputType(InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

            // troca o drawable pra olho fechado
            Drawable eyeHiddenDrawable = ContextCompat.getDrawable(this, R.drawable.ic_eye_hidden);
            caixaTexto.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, eyeHiddenDrawable, null);
        } else {
            // inputType é (código de textVisiblePassword) então inputType = 1 + 128 (código de textPassword)
            caixaTexto.setInputType(InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_PASSWORD);

            // troca o drawable pra olho aberto
            Drawable eyeOpenDrawable = ContextCompat.getDrawable(this, R.drawable.ic_eye);
            caixaTexto.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, eyeOpenDrawable, null);
        }

        // volta o cursor para a mesma posição antes de ocultar/exibir
        caixaTexto.setSelection(posicaoCursor);

    }

    public void voltar(View v) {
        finish();
    }

}