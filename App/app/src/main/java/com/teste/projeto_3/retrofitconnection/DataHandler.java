package com.teste.projeto_3.retrofitconnection;

import android.content.Context;
import android.util.Log;

import com.teste.projeto_3.FileEditor;
import com.teste.projeto_3.model.PostModel;
import com.teste.projeto_3.model.RequestResponse;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DataHandler {
    Context context;
    public DataHandler(Context context) {
        this.context = context;
    }

    public void novoIdRequest(){
        sendData("null",null,null,null,null, null, null).thenAccept(requestResponse -> {
            if (requestResponse.getStatus().equals("Conexao criada")) {
                salvarIdConexao(requestResponse.getId());
            }
        }).exceptionally(e -> {
            return null;
        });
    }
    public CompletableFuture<RequestResponse> ativarRequest(String usuario, String senha){
        return sendData(obterIdConexao(),"ativar", usuario,senha,null,null,null);
    }

    public CompletableFuture<RequestResponse> loginRequest(String usuario, String senha) {
        return sendData(obterIdConexao(), "login", usuario, senha, null,null, null);
    }

    public CompletableFuture<RequestResponse> cadastroRequest(String usuario, String senha, String email, String nomeCompleto) {
        return sendData(obterIdConexao(), "cadastro", usuario, senha, email,nomeCompleto,"");
    }
    public CompletableFuture<RequestResponse> getDadosRequest(){
        return sendData(obterIdConexao(), "get_dados", null,null,null,null,null);
    }

    public CompletableFuture<RequestResponse> logoutRequest() {
        return sendData(obterIdConexao(), "logout", null,null,null,null,null);
    }

    public CompletableFuture<RequestResponse> sendData(String id, String request, String usuario, String senha, String email, String nomeCompleto, String urlFoto) {
        // Cria o JSON a ser enviado
        PostModel postModel = new PostModel(id, request, usuario, senha, email, nomeCompleto, urlFoto);

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

    // Método para gerar um novo ID de conexão e salvar localmente

    // Método para salvar o ID de conexão localmente
    public void salvarIdConexao(@NotNull String idConexao) {
        FileEditor fe = new FileEditor(context);
        fe.writeFile(idConexao);
    }

    // Método para obter o ID de conexão salvo localmente
    public String obterIdConexao() {
        FileEditor fe = new FileEditor(context);
        Log.d("Id obtido", fe.readFile());
        return fe.readFile();
    }
}
