package com.teste.projeto_3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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

import java.util.concurrent.CompletableFuture;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TelaValidacao extends AppCompatActivity {
    DataHandler dh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_validacao);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dh = new DataHandler(getApplicationContext());

    }

    public void validarPorEmail(View v) {
        Intent telaLogin = getIntent();
        dh.ativarRequest(telaLogin.getStringExtra("usuario"), "").thenAccept(requestResponseValidate -> {
            if (requestResponseValidate.getCode() == 11) {
                dh.loginRequest(telaLogin.getStringExtra("usuario"), telaLogin.getStringExtra("senha")).thenAccept(requestResponse -> {
                    if (requestResponse.getCode() == 0) {
                        Intent intentTelaPrincipal = new Intent(this, TelaPrincipal.class);
                        intentTelaPrincipal.putExtra("nome_completo", requestResponse.getNome_completo());
                        intentTelaPrincipal.putExtra("email", requestResponse.getEmail());
                        startActivity(intentTelaPrincipal);
                        finish();
                    }
                }).exceptionally(e -> {
                    return null;
                });
            } else {
                Toast.makeText(this, "Erro na validação por email. Se persistir, utilize o código.", Toast.LENGTH_LONG).show();
            }
        }).exceptionally(e -> {
            return null;
        });
    }

    public void validarPorCodigo(View v) {
        EditText validacao = findViewById(R.id.codigo_validacao);
        Intent intentInfoTelaLogin = getIntent();
        String usuario = intentInfoTelaLogin.getStringExtra("usuario");

        if (validacao.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Insira o código de validação antes de prosseguir", Toast.LENGTH_SHORT).show();
        } else {
            dh.ativarRequest(usuario, validacao.getText().toString()).thenAccept(requestResponseValidate -> {
                switch (requestResponseValidate.getCode()) {

                    case 0: // Conta ativada com sucesso
                        dh.loginRequest(intentInfoTelaLogin.getStringExtra("usuario"), intentInfoTelaLogin.getStringExtra("senha")).thenAccept(requestResponseAfter -> {
                            Intent intentTelaPerfil = new Intent(this, TelaPrincipal.class);
                            intentTelaPerfil.putExtra("nome_completo", requestResponseAfter.getNome_completo());
                            intentTelaPerfil.putExtra("email", requestResponseAfter.getEmail());
                            Toast.makeText(this, "Conta ativada com sucesso", Toast.LENGTH_LONG).show();
                            startActivity(intentTelaPerfil);
                            finish();
                        }).exceptionally(e -> {
                            return null;
                        });
                        break;

                    case 4: //Conta não encontrada
                        Toast.makeText(this, requestResponseValidate.getMessage(), Toast.LENGTH_SHORT).show();
                        break;

                    case 7: // Código de ativação incorreta
                        Toast.makeText(this, requestResponseValidate.getMessage(), Toast.LENGTH_SHORT).show();
                        break;

                    case 11: // Conta já está ativa
                        dh.loginRequest(intentInfoTelaLogin.getStringExtra("usuario"), intentInfoTelaLogin.getStringExtra("senha")).thenAccept(requestResponseAfter -> {
                            Intent intentTelaPerfil = new Intent(this, TelaPrincipal.class);
                            intentTelaPerfil.putExtra("nome_completo", requestResponseValidate.getNome_completo());
                            intentTelaPerfil.putExtra("email", requestResponseValidate.getEmail());
                            Toast.makeText(this, "Conta ativada com sucesso", Toast.LENGTH_LONG).show();
                            startActivity(intentTelaPerfil);
                            finish();
                        }).exceptionally(e -> {
                            return null;
                        });
                        break;

                }
            }).exceptionally(e -> {
                return null;
            });
        }
    }
}

