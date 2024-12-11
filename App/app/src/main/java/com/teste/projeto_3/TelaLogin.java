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
import com.teste.projeto_3.model.PostModel;
import com.teste.projeto_3.model.RequestResponse;
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
            if (obterIdConexao().equals("defaultString") || obterIdConexao().isEmpty()) {
                gerarNovoID();
            }
            sendData(obterIdConexao(),"login", usuario.getText().toString(), senha.getText().toString()).thenAccept(requestResponse -> {
                    switch (requestResponse.getCode()) {
                        case 0: // Login bem sucedido
                            Intent intentTelaPrincipal = new Intent(this, TelaPrincipal.class);
                            intentTelaPrincipal.putExtra("nome_completo", requestResponse.getNome_completo());
                            intentTelaPrincipal.putExtra("email", requestResponse.getEmail());
                            startActivity(intentTelaPrincipal);
                            finish();
                            break;

                        case 1: // Senha incorreta
                            Toast.makeText(this, requestResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            break;

                        case 2: // Conta bloqueada por 5 minutos
                            Toast.makeText(this, requestResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            break;

                        case 3: // Conta não está ativa
                            Intent intentTelaValidacao = new Intent(this, TelaValidacao.class);
                            intentTelaValidacao.putExtra("usuario", usuario.getText().toString());
                            intentTelaValidacao.putExtra("senha", senha.getText().toString());
                            startActivity(intentTelaValidacao);
                            finish();
                            break;

                        case 4: // Conta não encontrada
                            Toast.makeText(this, requestResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            break;
                    }
            }).exceptionally(e -> {
                return null;
            });
        }
    }

    private void gerarNovoID() {
        sendData("null","","","").thenAccept(requestResponse -> {
            if (requestResponse.getStatus().equals("Conexao criada")) {
                salvarIdConexao(requestResponse.getId());
            }
        }).exceptionally(e -> {
            return null;
        });
    }


    // Método para salvar o ID de conexão localmente
    private void salvarIdConexao(@NotNull String idConexao) {
        FileWriter fw = new FileWriter();
        fw.escreverEmArquivo(this,idConexao);
    }

    private String obterIdConexao() {
        FileWriter fw = new FileWriter();
        return fw.lerDeArquivo(this);
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

    public CompletableFuture<RequestResponse> sendData(String id, String request, String usuario, String senha) {
        // Cria o JSON a ser enviado
        PostModel postModel = new PostModel(id, request, usuario, senha);

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