package com.teste.projeto_3.retrofitconnection;

import com.teste.projeto_3.model.PostModel;
import com.teste.projeto_3.model.RequestResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiInterface {

    @POST("/give/") // Para obtenção de ID: {"id":"null"}
    Call<RequestResponse> give(@Body PostModel postModel);

    @POST("/login/")
    Call<RequestResponse> login(@Body PostModel postModel);

    @POST("/cadastro/")
    Call<RequestResponse> cadastro(@Body PostModel postModel);

    @POST("/ativar/")
    Call<RequestResponse> ativar(@Body PostModel postModel);

    @POST("/logout/")
    Call<RequestResponse> logout(@Body PostModel postModel);

    @POST("/get_dados/")
    Call<RequestResponse> get_dados(@Body PostModel postModel);

    @POST("/set_img_url/")
    Call<RequestResponse> set_img_url(@Body PostModel postModel);

    @POST("/upload-image/") // Verificar o underline caso mude depois. Atualmente é um traço.
    Call<RequestResponse> upload_image(@Body PostModel postModel);

    @POST("/recover/")
    Call<RequestResponse> recover(@Body PostModel postModel);

    @POST("/get_email/")
    Call<RequestResponse> get_email(@Body PostModel postModel);

    @GET("/get_img_url/")
    Call<RequestResponse> get_img_url(@Body PostModel postModel);

}
