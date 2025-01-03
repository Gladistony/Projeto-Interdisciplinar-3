package com.teste.projeto_3;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.teste.projeto_3.model.PostModel;
import com.teste.projeto_3.model.RequestResponse;
import com.teste.projeto_3.retrofitconnection.ApiInterface;
import com.teste.projeto_3.retrofitconnection.RetrofitClient;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        checkLoggedIn();
    }

    public void checkLoggedIn() {
        if (obterIdConexao().equals("defaultString") || obterIdConexao().isEmpty()) {
            gerarNovoID();
        } else {
            sendData(obterIdConexao(),"get_dados", "", "").thenAccept(requestResponseAutomatic -> {
                if (!requestResponseAutomatic.getStatus().equals("Usuario nao logado")) {
                        switch (requestResponseAutomatic.getCode()) {
                            case 0: // Login bem sucedido
                                Intent intentTelaPrincipal = new Intent(this, TelaPrincipal.class);
                                intentTelaPrincipal.putExtra("nome_completo", requestResponseAutomatic.getNome_completo());
                                intentTelaPrincipal.putExtra("email", requestResponseAutomatic.getEmail());
                                startActivity(intentTelaPrincipal);
                                finish();
                                break;

                            case 3: // Conta não está ativa
                                Intent intentTelaValidacao = new Intent(this, TelaValidacao.class);
                                intentTelaValidacao.putExtra("usuario", requestResponseAutomatic.getUsuario());
                                intentTelaValidacao.putExtra("senha", requestResponseAutomatic.getSenha());
                                startActivity(intentTelaValidacao);
                                finish();
                                break;

                            case 4: // Conta não encontrada
                                Toast.makeText(this, "Erro ao conectar-se automaticamente à sua conta. Por favor, entre novamente.", Toast.LENGTH_SHORT).show();
                                Intent intentTelaLoginNaoEncontrado = new Intent(this, TelaLogin.class);
                                startActivity(intentTelaLoginNaoEncontrado);
                                finish();
                                break;
                        }
                }
            }).exceptionally(e -> {
                return null;
            });
        }

    }

    private String obterIdConexao() {
        FileWriter fw = new FileWriter();
        return fw.lerDeArquivo(this);
    }

    public void abrirTelaLogin(View v){
        Intent intentLogin = new Intent(this, TelaLogin.class);
        startActivity(intentLogin);
    }

    public void abrirTelaCadastro(View v){
        Intent intentCadastro = new Intent(this, FormCadastro.class);
        startActivity(intentCadastro);
    }

    public CompletableFuture<RequestResponse> sendData(String id, String request, String usuario, String senha) {
        // Cria o JSON a ser enviado
        PostModel postModel = new PostModel(id, request, usuario, senha,"","");

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

    private void gerarNovoID() {
        sendData("null","","","").thenAccept(requestResponse -> {
            if (requestResponse.getStatus().equals("Conexao criada")) {
                salvarIdConexao(requestResponse.getId());
            }
        }).exceptionally(e -> {
            return null;
        });
    }

    private void salvarIdConexao(@NotNull String idConexao) {
        FileWriter fw = new FileWriter();
        fw.escreverEmArquivo(this,idConexao);
    }
}
