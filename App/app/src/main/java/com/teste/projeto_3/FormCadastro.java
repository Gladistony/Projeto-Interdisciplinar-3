package com.teste.projeto_3;

import android.content.Intent;
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
import com.teste.projeto_3.database.AppDatabase;
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

public class FormCadastro extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_form_cadastro);

        // Configurações de margens para Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Alternar exibição de senha
    public void togglePassword(View v) {
        EditText caixaTexto = findViewById(R.id.edit_senha);
        int posicaoCursor = caixaTexto.getSelectionStart();

        if (caixaTexto.getInputType() == InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            caixaTexto.setInputType(InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            Drawable eyeHiddenDrawable = ContextCompat.getDrawable(this, R.drawable.ic_eye_hidden);
            caixaTexto.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, eyeHiddenDrawable, null);
        } else {
            caixaTexto.setInputType(InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_PASSWORD);
            Drawable eyeOpenDrawable = ContextCompat.getDrawable(this, R.drawable.ic_eye);
            caixaTexto.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, eyeOpenDrawable, null);
        }

        caixaTexto.setSelection(posicaoCursor);
    }

    // Método de cadastro
    public void cadastrar(View v) {
        if (obterIdConexao().equals("defaultString") || obterIdConexao().isEmpty()) {
            gerarNovoID();
        }

        // Referência aos campos do formulário
        EditText editTextNome = findViewById(R.id.edit_nome);
        EditText editTextEmail = findViewById(R.id.edit_email);
        EditText editTextSenha = findViewById(R.id.edit_senha);
        EditText editTextUsuario = findViewById(R.id.edit_usuario);

        /*
        // Criar o objeto User para a primeira requisição
        User user = new User();
        user.setId(obterIdConexao());
        user.setRequest("cadastro"); // Tipo de requisição
        user.setNome_completo(editTextNome.getText().toString());
        user.setEmail(editTextEmail.getText().toString());
        user.setSenha(editTextSenha.getText().toString());
        user.setUsuario(editTextUsuario.getText().toString()); */

        // Validar campos obrigatórios
        if (editTextNome.getText().toString().isEmpty() || editTextEmail.getText().toString().isEmpty() ||
                editTextSenha.getText().toString().isEmpty() || editTextUsuario.getText().toString().isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos obrigatórios!", Toast.LENGTH_SHORT).show();
        } else {
            if (obterIdConexao().equals("defaultString") || obterIdConexao().isEmpty()) {
                gerarNovoID();
            }
            sendData(obterIdConexao(), "cadastro", editTextUsuario.getText().toString(), editTextSenha.getText().toString(), editTextEmail.getText().toString(), editTextNome.getText().toString()).thenAccept(requestResponse -> {
                switch (requestResponse.getCode()) {
                    case 0:
                        Toast.makeText(getApplicationContext(), requestResponse.getMessage() + "! Entre para continuar.", Toast.LENGTH_LONG).show();
                        Intent intentTelaLogin = new Intent(this, MainActivity.class);
                        startActivity(intentTelaLogin);
                        finish();
                        break;
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                    case 10:
                        Toast.makeText(getApplicationContext(), requestResponse.getMessage(), Toast.LENGTH_LONG).show();
                        break;
                }

            }).exceptionally(e -> {
                return null;
            });
        }
                /*
                // Criar uma nova requisição com o ID retornado
                User userAtualizado = new User();
                userAtualizado.setId(userId);
                userAtualizado.setRequest("outra_acao");
                String updatedUserJson = gson.toJson(userAtualizado);

                // Fazer a segunda requisição
                enviarRequisicao(updatedUserJson, updateResponse -> {
                    if (updateResponse.startsWith("Erro")) {
                        runOnUiThread(() -> Toast.makeText(this, updateResponse, Toast.LENGTH_LONG).show());
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Requisição subsequente realizada com sucesso!", Toast.LENGTH_SHORT).show();
                            abrirTelaLogin(); // Navegar para a próxima tela
                        });
                    }
                }); */
        }

    // Método auxiliar para enviar requisições
    private void enviarRequisicao(String json, Callback callback) {
        new Thread(() -> {
            HttpHelper httpHelper = new HttpHelper();
            String response = httpHelper.post(json);
            callback.onResponse(response);
        }).start();
    }

    // Método para salvar o usuário no banco local
    private void salvarUsuarioNoBanco(User user) {
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                db.userDao().insertUser(user);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Método para salvar o ID de conexão localmente
    private void salvarIdConexao(@NotNull String idConexao) {
        FileWriter fw = new FileWriter();
        fw.escreverEmArquivo(this,idConexao);
    }

    // Método para abrir a próxima tela
    private void abrirTelaLogin() {
        Intent intent = new Intent(this, TelaLogin.class);
        startActivity(intent);
        finish(); // Fecha a atividade atual
    }

    // Interface de callback para tratar respostas
    interface Callback {
        void onResponse(String response);
    }

    // Método para voltar à tela anterior
    public void voltar(View v) {
        finish();
    }

    private void gerarNovoID() {
        sendData("null","","","", "", "").thenAccept(requestResponse -> {
            if (requestResponse.getStatus().equals("Conexao criada")) {
                salvarIdConexao(requestResponse.getId());
            }
        }).exceptionally(e -> {
            return null;
        });
    }
    private String obterIdConexao() {
        FileWriter fw = new FileWriter();
        return fw.lerDeArquivo(this);
    }

    public CompletableFuture<RequestResponse> sendData(String id, String request, String usuario, String senha, String email, String nome) {
        // Cria o JSON a ser enviado
        PostModel postModel = new PostModel(id, request, usuario, senha, email, nome);

        // Retorno assíncrono do método
        CompletableFuture<RequestResponse> future = new CompletableFuture<>();

        // Configura a API no método
        ApiInterface apiInterface = RetrofitClient.getRetrofit().create(ApiInterface.class);
        Call<RequestResponse> call = apiInterface.postData(postModel);

        // Chama a API
        call.enqueue(new retrofit2.Callback<RequestResponse>() {
            @Override
            public void onResponse(Call<RequestResponse> call, Response<RequestResponse> response) {
                if (response.isSuccessful()) {
                    try {
                        RequestResponse requestLoginResponse = response.body();

                        if (requestLoginResponse != null) {
                            // Imprime o status do resultado da conexão
                            System.out.println("Status: " + requestLoginResponse.getStatus());

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