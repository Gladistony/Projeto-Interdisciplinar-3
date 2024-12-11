package com.teste.projeto_3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
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

public class TelaPrincipal extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_principal);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        onLoggedIn();
    }

    public void onLoggedIn() {
        TextView nome = findViewById(R.id.textNomeUsuario);
        TextView email = findViewById(R.id.textEmailUsuario);
        Intent intentInfoLogin = getIntent();
        nome.setText(intentInfoLogin.getStringExtra("nome_completo"));
        email.setText(intentInfoLogin.getStringExtra("email"));
    }

    public void deslogar(View v){
        sendData(obterIdConexao(),"logout", "", "").thenAccept(requestResponse -> {
            if (requestResponse.getStatus().equals("Usuario deslogado")) {
                gerarNovoID();

                /* TODO: ao deslogar, o método deve criar um novo ID e salvá-lo. Porém, devido
                à problemas com assincronidade, o ID por enquanto permanece, agora com o status de deslogado
                (livre para um novo usuário). Resolver futuramente para que funcione corretamente */
                Intent intentMainActivity = new Intent(this, MainActivity.class);
                startActivity(intentMainActivity);
                finish();
            }
        }).exceptionally(e -> {
            return null;
        });
    }

    public CompletableFuture<RequestResponse> sendData(String id, String request, String usuario, String senha) {
        // Cria o JSON a ser enviado
        PostModel postModel = new PostModel(id, request, usuario, senha, "","");

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

    private String obterIdConexao() {
        FileWriter fw = new FileWriter();
        Log.d("ID lido", fw.lerDeArquivo(this));
        return fw.lerDeArquivo(this);
    }

    // Método para salvar o ID de conexão localmente
    private void salvarIdConexao(@NotNull String idConexao) {
        FileWriter fw = new FileWriter();
        fw.escreverEmArquivo(this,idConexao);
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

}