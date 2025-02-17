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
import com.google.gson.Gson;
import com.teste.projeto_3.http.EnviarRequisicao;
import com.teste.projeto_3.model.User;

public class TelaValidacao extends AppCompatActivity {
    EnviarRequisicao er;

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
        er = new EnviarRequisicao(getApplicationContext());

    }

    public void validarPorEmail(View v) {
        Intent intentInfoTelaLogin = getIntent();

        String usuario = intentInfoTelaLogin.getStringExtra("usuario");
        String senha = intentInfoTelaLogin.getStringExtra("senha");

        // Fazer a requisição
        er.ativarConta(usuario, "", response -> {
            if (response.startsWith("Erro")) {
                runOnUiThread(() -> Toast.makeText(this, response, Toast.LENGTH_LONG).show());
            } else {
                try {
                    // Processar resposta da requisição
                    Gson gson = new Gson();
                    User responseValidacao = gson.fromJson(response, User.class);
                    if (responseValidacao.getCode() == 11) {
                        login(usuario, senha);
                    } else {
                        Toast.makeText(this, "Erro na validação por email. Se persistir, utilize o código.", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Erro na validação por email. Se persistir, utilize o código.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void validarPorCodigo(View v) {
        EditText validacao = findViewById(R.id.codigo_validacao);
        Intent intentInfoTelaLogin = getIntent();

        if (validacao.getText().toString().isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos obrigatórios", Toast.LENGTH_SHORT).show();
        } else {

            String usuario = intentInfoTelaLogin.getStringExtra("usuario");
            String senha = intentInfoTelaLogin.getStringExtra("senha");
            String codigoValidacao = validacao.getText().toString();

            // Fazer a requisição
            er.ativarConta(usuario, codigoValidacao, response -> {
                if (response.startsWith("Erro")) {
                    runOnUiThread(() -> Toast.makeText(this, response, Toast.LENGTH_LONG).show());
                } else {
                    try {
                        // Processar resposta da requisição
                        Gson gson = new Gson();
                        User responseValidacao = gson.fromJson(response, User.class);
                        if (responseValidacao.getCode() == 11) {
                            login(usuario, senha);
                        } else {
                            Toast.makeText(this, "Erro na validação por email. Se persistir, utilize o código.", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Erro na validação por email. Se persistir, utilize o código.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
                public void login(String usuario, String senha) {
                    // Criando o objeto User
                    User userLogin = new User();
                    userLogin.setId(er.obterMemoriaInterna("idConexao"));
                    userLogin.setUsuario(usuario);
                    userLogin.setSenha(senha);

                    // Converter o objeto User para JSON
                    Gson gson = new Gson();
                    String userJson = gson.toJson(userLogin);

                    // Fazer a requisição
                    er.post("login", userJson, response -> {
                        if (response.startsWith("Erro")) {
                            runOnUiThread(() -> Toast.makeText(this, response, Toast.LENGTH_LONG).show());
                        } else {
                            try {
                                // Processar resposta da requisição
                                User responseLogin = gson.fromJson(response, User.class);
                                if (responseLogin != null) {
                                    switch (responseLogin.getCode()) {
                                        case 0: // Login bem sucedido
                                            Intent intentTelaPrincipal = new Intent(this, TelaPrincipal.class);
                                            intentTelaPrincipal.putExtra("nome_completo", responseLogin.getNome_completo());
                                            intentTelaPrincipal.putExtra("email", responseLogin.getEmail());
                                            intentTelaPrincipal.putExtra("url_foto", responseLogin.getUrl_foto());
                                            runOnUiThread(() -> Toast.makeText(this, "Conta ativada com sucesso!", Toast.LENGTH_LONG).show());
                                            startActivity(intentTelaPrincipal);
                                            finish();
                                            break;

                                        case 3: // Conta não está ativa
                                            Intent intentTelaValidacao = new Intent(this, TelaValidacao.class);
                                            intentTelaValidacao.putExtra("usuario", usuario);
                                            intentTelaValidacao.putExtra("senha", senha);
                                            startActivity(intentTelaValidacao);
                                            finish();
                                            break;

                                        case 4: // Conta não encontrada
                                            Toast.makeText(this, responseLogin.getMessage(), Toast.LENGTH_SHORT).show();
                                            break;

                                        case 7: // Código de ativação incorreta
                                            Toast.makeText(this, responseLogin.getMessage(), Toast.LENGTH_SHORT).show();
                                            break;

                                        case 11: // Conta já está ativa
                                            login(usuario, senha);

                                        case 12: // Conexão não encontrada
                                            Toast.makeText(this, "Houve um problema na conexão. Por favor, reinicie o aplicativo.", Toast.LENGTH_SHORT).show();
                                            break;
                                    }
                                }
                            } catch (Exception e) {
                                runOnUiThread(() -> Toast.makeText(this, "Erro ao processar a resposta. Tente novamente.", Toast.LENGTH_SHORT).show());
                            }
                        }
                    });
                }
    }

