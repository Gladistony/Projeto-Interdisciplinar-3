package com.teste.projeto_3;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
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
import com.teste.projeto_3.model.PostModel;
import com.teste.projeto_3.model.RequestResponse;
import com.teste.projeto_3.model.User;
import com.teste.projeto_3.retrofitconnection.ApiInterface;
import com.teste.projeto_3.retrofitconnection.RetrofitClient;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TelaLogin extends AppCompatActivity {
    EditText usuario;
    EditText senha;

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

    public void login(View v) {
        usuario = findViewById(R.id.usuario);
        senha = findViewById(R.id.senha);

        if (usuario.getText().toString().isEmpty() || senha.getText().toString().isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos obrigatórios", Toast.LENGTH_SHORT).show();
        } else {
            if (obterIdConexao().equals("defaultString")) {
                gerarNovoID();
            }
            sendData(v, obterIdConexao(),usuario.getText().toString(), senha.getText().toString()).thenAccept(requestResponse -> {
                Toast.makeText(this, requestResponse.getMessage(), Toast.LENGTH_SHORT).show();
            }).exceptionally(e -> {
                return null;
            });

        } /* else if (requestResponse.getCode() == 1) { //senha incorreta

            } else if (requestResponse.getCode() == 2) { //conta bloqueada excesso tentativas

            } else if (requestResponse.getCode() == 3) { // conta não está ativa

            }  else if (requestResponse.getCode() == 4) { // conta não encontrada

            }*/
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
                User responseUser = gson.fromJson(response, User.class);
                try {
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

    public void togglePassword(View v) {
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

    public CompletableFuture<RequestResponse> sendData(View v, String id, String usuario, String senha) {
        // Cria o JSON a ser enviado
        PostModel postModel = new PostModel(id, "login", usuario, senha);

        // Retorno assíncrono do método
        CompletableFuture<RequestResponse> future = new CompletableFuture<>();

        // Configura a API no método
        ApiInterface apiInterface = RetrofitClient.getRetrofit().create(ApiInterface.class);
        Call<RequestResponse> call = apiInterface.postData(postModel);

        // Chama a API
        call.enqueue(new Callback<RequestResponse>() {
            @Override
            public void onResponse(Call<RequestResponse> call, Response<RequestResponse> response) {
                if (response.isSuccessful()) {
                    try {
                        RequestResponse requestLoginResponse = response.body();

                        if (requestLoginResponse != null) {
                            // Imprime o status do resultado da conexão
                            System.out.println("Status: " + requestLoginResponse.getMessage());

                            // Define como completada a requisição quando há sucesso
                            future.complete(requestLoginResponse);
                        } else {
                            future.completeExceptionally(new Exception("Resposta nula"));
                        }
                    } catch (Exception e) {
                        future.completeExceptionally(e);
                    }
                } else {
                    future.completeExceptionally(new Exception("Erro de requisição: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<RequestResponse> call, Throwable t) {
                future.completeExceptionally(t);
            }
        });

        // Por fim, retorna o objeto com os resultados da conexão
        return future;
    }
}