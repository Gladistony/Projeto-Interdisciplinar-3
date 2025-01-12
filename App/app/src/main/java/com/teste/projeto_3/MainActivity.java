package com.teste.projeto_3;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity {
    DataHandler dh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.motionLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dh = new DataHandler(getApplicationContext());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkLoggedIn();
            }
        }, 2000);
    }

    public void checkLoggedIn() {
        if (!dh.obterIdConexao().equals("defaultString") || !dh.obterIdConexao().isEmpty()) {
            dh.getDadosRequest().thenAccept(requestResponseAutomatic -> {
                if (!requestResponseAutomatic.getStatus().equals("Usuario nao logado")) {
                        switch (requestResponseAutomatic.getCode()) {
                            case 0: // Login bem sucedido
                                Intent intentTelaPrincipal = new Intent(this, TelaPrincipal.class);
                                intentTelaPrincipal.putExtra("nome_completo", requestResponseAutomatic.getNome_completo());
                                intentTelaPrincipal.putExtra("email", requestResponseAutomatic.getEmail());
                                startActivity(intentTelaPrincipal);
                                finish();
                                break;

                            /*case 3: // Conta não está ativa
                                Intent intentTelaValidacao = new Intent(this, TelaValidacao.class);
                                intentTelaValidacao.putExtra("usuario", requestResponseAutomatic.getUsuario());
                                intentTelaValidacao.putExtra("senha", requestResponseAutomatic.getSenha());
                                startActivity(intentTelaValidacao);
                                finish();
                                break;*/

                            case 4: // Conta não encontrada
                                Toast.makeText(this, "Erro ao conectar-se automaticamente à sua conta. Por favor, entre novamente.", Toast.LENGTH_SHORT).show();
                                Intent intentTelaLoginNaoEncontrado = new Intent(this, TelaLogin.class);
                                startActivity(intentTelaLoginNaoEncontrado);
                                finish();
                                break;

                            default:
                                startActivity(new Intent(MainActivity.this, LoginCadastro.class));
                                finish();
                        }
                }
            }).exceptionally(e -> {
                return null;
            });
        } else {
            startActivity(new Intent(MainActivity.this, LoginCadastro.class));
            finish();
        }

    }

}
