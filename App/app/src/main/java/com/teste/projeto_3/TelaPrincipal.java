package com.teste.projeto_3;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.teste.projeto_3.http.HttpHelper;
import com.teste.projeto_3.model.PostModel;
import com.teste.projeto_3.model.RequestResponse;
import com.teste.projeto_3.retrofitconnection.ApiInterface;
import com.teste.projeto_3.retrofitconnection.RetrofitClient;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TelaPrincipal extends AppCompatActivity {

    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_principal);

        // Configuração para barras de status (ajuste de insets)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicialize o launcher da câmera
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                            ImageView imageView = findViewById(R.id.imageView);
                            if (imageView != null) {
                                imageView.setImageBitmap(imageBitmap);
                            }
                        }
                    } else {
                        Toast.makeText(this, "Captura de imagem cancelada", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Configurar botão para abrir a câmera
        Button cameraButton = findViewById(R.id.ligarCamera);
        cameraButton.setOnClickListener(v -> abrirCamera());

        // Configurar informações do usuário logado
        onLoggedIn();
    }

    private void abrirCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(intent);
        } else {
            Toast.makeText(this, "Câmera indisponível no dispositivo", Toast.LENGTH_SHORT).show();
        }
    }

    public void onLoggedIn() {
        TextView nome = findViewById(R.id.textNomeUsuario);
        TextView email = findViewById(R.id.textEmailUsuario);
        Intent intentInfoLogin = getIntent();
        nome.setText(intentInfoLogin.getStringExtra("nome_completo"));
        email.setText(intentInfoLogin.getStringExtra("email"));
    }

    public void deslogar(View v) {
        sendData(obterIdConexao(), "logout", "", "").thenAccept(requestResponse -> {
            if ("Usuario deslogado".equals(requestResponse.getStatus())) {
                gerarNovoID();
                Intent intentMainActivity = new Intent(this, MainActivity.class);
                startActivity(intentMainActivity);
                finish();
            }
        }).exceptionally(e -> null);
    }

    public CompletableFuture<RequestResponse> sendData(String id, String request, String usuario, String senha) {
        PostModel postModel = new PostModel(id, request, usuario, senha, "", "");
        CompletableFuture<RequestResponse> future = new CompletableFuture<>();
        ApiInterface apiInterface = RetrofitClient.getRetrofit().create(ApiInterface.class);
        Call<RequestResponse> call = apiInterface.postData(postModel);

        call.enqueue(new Callback<RequestResponse>() {
            @Override
            public void onResponse(Call<RequestResponse> call, Response<RequestResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    future.complete(response.body());
                } else {
                    future.completeExceptionally(new Exception("Erro de requisição: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<RequestResponse> call, Throwable t) {
                future.completeExceptionally(t);
            }
        });

        return future;
    }

    private String obterIdConexao() {
        FileWriter fw = new FileWriter();
        return fw.lerDeArquivo(this);
    }

    private void salvarIdConexao(@NotNull String idConexao) {
        FileWriter fw = new FileWriter();
        fw.escreverEmArquivo(this, idConexao);
    }

    private void gerarNovoID() {
        sendData("null", "", "", "").thenAccept(requestResponse -> {
            if ("Conexao criada".equals(requestResponse.getStatus())) {
                salvarIdConexao(requestResponse.getId());
            }
        }).exceptionally(e -> null);
    }
}
