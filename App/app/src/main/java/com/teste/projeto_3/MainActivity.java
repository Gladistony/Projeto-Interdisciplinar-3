package com.teste.projeto_3;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import com.google.gson.Gson;
import com.teste.projeto_3.http.EnviarRequisicao;
import com.teste.projeto_3.model.User;

public class MainActivity extends AppCompatActivity{

    EnviarRequisicao er;

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
        er = new EnviarRequisicao(getApplicationContext());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkLoggedIn();
            }
        }, 1500);
    }

    private void checkLoggedIn() {
        if (er.possuiInternet(getApplicationContext())) {
            if (er.obterMemoriaInterna("idConexao").equals("Chave não possui valor")) {
                criarNovoID();
                startActivity(new Intent(MainActivity.this, LoginCadastro.class));
                finish();
            } else {
                // Criando o objeto User
                User userLogin = new User();
                userLogin.setId(er.obterMemoriaInterna("idConexao"));

                // Converter o objeto User para JSON
                Gson gson = new Gson();
                String userJson = gson.toJson(userLogin);
                System.out.println(userJson);

                // Fazer a requisição
                er.post("get_dados", userJson, response -> {
                    if (response.startsWith("Erro")) {
                        runOnUiThread(() -> Toast.makeText(this, response, Toast.LENGTH_LONG).show());
                        startActivity(new Intent(MainActivity.this, LoginCadastro.class));
                        finish();
                    } else {
                        try {
                            // Processar resposta da requisição
                            User responseAutoLogin = gson.fromJson(response, User.class);
                            if (responseAutoLogin.getCode() != 15) { // Code 15 = "Usuario nao logado"
                                switch (responseAutoLogin.getCode()) {
                                    case 0: // Login bem sucedido
                                        Intent intentTelaPrincipal = new Intent(this, TelaPrincipalActivity.class);
                                        intentTelaPrincipal.putExtra("dados", responseAutoLogin);
                                        startActivity(intentTelaPrincipal);
                                        finish();
                                        break;

                                    case 3: // Conta não está ativa
                                        Intent intentTelaValidacao = new Intent(this, TelaValidacao.class);
                                        intentTelaValidacao.putExtra("usuario", responseAutoLogin.getUsuario());
                                        intentTelaValidacao.putExtra("senha", responseAutoLogin.getSenha());
                                        startActivity(intentTelaValidacao);
                                        finish();
                                        break;

                                    case 4: // Conta não encontrada
                                        criarNovoID();
                                        runOnUiThread(() -> Toast.makeText(this, "Erro na conexão automática. Conta não encontrada.", Toast.LENGTH_SHORT).show());
                                        Intent intentTelaLoginNaoEncontrado = new Intent(this, TelaLogin.class);
                                        startActivity(intentTelaLoginNaoEncontrado);
                                        finish();
                                        break;

                                    case 12: // Conexão não encontrada / ID inválido
                                        criarNovoID();
                                        runOnUiThread(() -> Toast.makeText(this, "Erro na conexão automática. Por favor, entre novamente.", Toast.LENGTH_SHORT).show());
                                        Intent intentConexaoNaoEncontrado = new Intent(this, TelaLogin.class);
                                        startActivity(intentConexaoNaoEncontrado);
                                        finish();
                                        break;

                                    default:
                                        startActivity(new Intent(MainActivity.this, LoginCadastro.class));
                                        finish();
                                }
                            } else {
                                startActivity(new Intent(MainActivity.this, LoginCadastro.class));
                                finish();
                            }
                        } catch (Exception e) {
                            runOnUiThread(() -> Toast.makeText(this, "Erro ao processar a resposta. Tente novamente.", Toast.LENGTH_SHORT).show());
                            startActivity(new Intent(MainActivity.this, LoginCadastro.class));
                            finish();
                        }
                    }
                });
            }
        } else {
            runOnUiThread(() -> Toast.makeText(this, "Verifique sua conexão com a internet.", Toast.LENGTH_SHORT).show());
            startActivity(new Intent(MainActivity.this, LoginCadastro.class));
            finish();
        }
    }

    private void criarNovoID(){
        User user = new User();
        user.setId("null");

        // Converter o objeto User para JSON
        Gson gson = new Gson();
        String userJson = gson.toJson(user);

        // Fazer a requisição
        er.post("give", userJson, response -> {
            if (response.startsWith("Erro")) {
                runOnUiThread(() -> Toast.makeText(this, response, Toast.LENGTH_LONG).show());
            } else {
                // Processar resposta da requisição
                User responseUser = gson.fromJson(response, User.class);
                String userId = responseUser.getId(); // Captura o ID
                Log.d("ID obtido", userId);
                er.salvarMemoriaInterna("idConexao", userId);
            }
        });
    }
}
