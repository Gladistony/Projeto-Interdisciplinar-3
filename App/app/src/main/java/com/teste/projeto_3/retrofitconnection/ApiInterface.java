package com.teste.projeto_3.retrofitconnection;

import com.teste.projeto_3.model.PostModel;
import com.teste.projeto_3.model.RequestResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiInterface {

    @POST("/give")
    Call<RequestResponse> postData(@Body PostModel postModel);
}
