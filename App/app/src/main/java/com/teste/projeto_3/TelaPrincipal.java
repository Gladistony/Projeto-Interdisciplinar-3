package com.teste.projeto_3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.teste.projeto_3.model.PostModel;
import com.teste.projeto_3.model.RequestResponse;
import com.teste.projeto_3.retrofitconnection.ApiInterface;
import com.teste.projeto_3.retrofitconnection.DataHandler;
import com.teste.projeto_3.retrofitconnection.RetrofitClient;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TelaPrincipal extends AppCompatActivity {
    DataHandler dh;

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
        dh = new DataHandler(getApplicationContext());
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
        dh.logoutRequest().thenAccept(requestResponse -> {
            if (requestResponse.getMessage().equals("Usuario deslogado")) {
                dh.novoIdRequest();
                Intent intentMainActivity = new Intent(this, MainActivity.class);
                startActivity(intentMainActivity);
                finish();
            }
        }).exceptionally(e -> {
            return null;
        });
    }
}