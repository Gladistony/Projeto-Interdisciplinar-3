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

    ApiInterface apiInterface = RetrofitClient.getRetrofit().create(ApiInterface.class);

    public DataHandler(Context context) {
        this.context = context;
    }

    public PostModel createPostModel(String id, String usuario, String senha, String email, String nomeCompleto, String urlFoto) {
        return new PostModel(id, usuario, senha, email, nomeCompleto, urlFoto);
    }

    public void novoIdRequest(){
        Call<RequestResponse> callRequest = apiInterface.give(createPostModel("null", null,null,null,null,null));
        sendData(callRequest).thenAccept(requestResponse -> {
            if (requestResponse.getStatus().equals("Conexao criada")) {
                salvarIdConexao(requestResponse.getId());
            }
        }).exceptionally(e -> {
            return null;
        });
    }
    public CompletableFuture<RequestResponse> ativarRequest(String usuario, String senha){
        Call<RequestResponse> callRequest = apiInterface.ativar(createPostModel(obterIdConexao(), usuario,senha,null,null,null));
        return sendData(callRequest);
    }

    public CompletableFuture<RequestResponse> loginRequest(String usuario, String senha) {
        Call<RequestResponse> callRequest = apiInterface.login(createPostModel(obterIdConexao(), usuario,senha,null,null,null));
        return sendData(callRequest);
    }

    public CompletableFuture<RequestResponse> cadastroRequest(String usuario, String senha, String email, String nomeCompleto) {
        Call<RequestResponse> callRequest = apiInterface.cadastro(createPostModel(obterIdConexao(), usuario,senha,email,nomeCompleto,null));
        return sendData(callRequest);
    }
    public CompletableFuture<RequestResponse> getDadosRequest(){
        Call<RequestResponse> callRequest = apiInterface.get_dados(createPostModel(obterIdConexao(), null,null,null,null,null));
        return sendData(callRequest);
    }

    public CompletableFuture<RequestResponse> logoutRequest() {
        Call<RequestResponse> callRequest = apiInterface.logout(createPostModel(obterIdConexao(), null,null,null,null,null));
        return sendData(callRequest);
    }

    public CompletableFuture<RequestResponse> sendData(Call<RequestResponse> callRequest) {
        // Retorno assíncrono do método
        CompletableFuture<RequestResponse> future = new CompletableFuture<>();

        // Configura a API para requisição e e o método a ser requisitado
        Call<RequestResponse> call = callRequest;

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
