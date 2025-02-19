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
import com.teste.projeto_3.model.Data;
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
        if (er.possuiInternet(getApplicationContext())) {
            Intent intentInfoTelaLogin = getIntent();
            // Criando o objeto User
            User userLogin = new User();
            userLogin.setId(er.obterMemoriaInterna("idConexao"));
            userLogin.setUsuario(intentInfoTelaLogin.getStringExtra("usuario"));
            userLogin.setSenha(intentInfoTelaLogin.getStringExtra("senha"));

            // Converter o objeto User para JSON
            Gson gson = new Gson();
            String userJson = gson.toJson(userLogin);

            try {
                // Fazer a requisição
                er.post("login", userJson, response -> {
                    if (response.startsWith("Erro")) {
                        runOnUiThread(() -> Toast.makeText(this, response, Toast.LENGTH_LONG).show());
                    } else {
                        // Processar resposta da requisição
                        User responseLogin = gson.fromJson(response, User.class);
                        if (responseLogin != null) {
                            switch (responseLogin.getCode()) {
                                case 0: // Login bem sucedido
                                    Intent intentTelaPrincipal = new Intent(this, TelaPrincipal.class);
                                    Data data = responseLogin.getData();
                                    intentTelaPrincipal.putExtra("nome_completo", responseLogin.getData().getNome_completo());
                                    intentTelaPrincipal.putExtra("email", responseLogin.getData().getEmail());
                                    intentTelaPrincipal.putExtra("url_foto", responseLogin.getData().getUrl_foto());
                                    runOnUiThread(() -> Toast.makeText(this, "Conta ativada com sucesso!", Toast.LENGTH_LONG).show());
                                    startActivity(intentTelaPrincipal);
                                    finish();
                                    break;

                                case 3: // Conta não está ativa
                                    runOnUiThread(() -> Toast.makeText(this, "Conta ainda não ativada. Aguarde e tente novamente.", Toast.LENGTH_LONG).show());
                                    break;

                                case 4: // Conta não encontrada
                                    runOnUiThread(() -> Toast.makeText(this, responseLogin.getMessage(), Toast.LENGTH_SHORT).show());
                                    break;

                                case 12: // Conexão não encontrada
                                    runOnUiThread(() -> Toast.makeText(this, "Houve um problema na conexão. Por favor, reinicie o aplicativo.", Toast.LENGTH_SHORT).show());
                                    break;
                            }
                        }
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Ocorreu um erro ao conectar-se ao servidor.", Toast.LENGTH_SHORT).show());
            }
        } else {
            runOnUiThread(() -> Toast.makeText(this, "Verifique sua conexão com a internet.", Toast.LENGTH_SHORT).show());
        }
    }

    public void validarPorCodigo(View v) {
        if (er.possuiInternet(getApplicationContext())) {
            EditText validacao = findViewById(R.id.codigo_validacao);
            Intent intentInfoTelaLogin = getIntent();

            if (validacao.getText().toString().isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos obrigatórios", Toast.LENGTH_SHORT).show();
            } else {

                String usuario = intentInfoTelaLogin.getStringExtra("usuario");
                String senha = intentInfoTelaLogin.getStringExtra("senha");
                String codigoValidacao = validacao.getText().toString();

                try {
                    // Fazer a requisição
                    er.ativarConta(usuario, codigoValidacao, response -> {
                        if (response.startsWith("Erro")) {
                            runOnUiThread(() -> Toast.makeText(this, response, Toast.LENGTH_LONG).show());
                        } else {
                            // Processar resposta da requisição
                            String resultado = response.substring(905); // 905 é o índice da String HTML em que diz o resultado da requisição
                            if (resultado.startsWith("Código de ativação incorreto")) {
                                runOnUiThread(() -> Toast.makeText(this, "Código de ativação incorreto", Toast.LENGTH_LONG).show());
                            } else if (resultado.startsWith("Operação realizada com sucesso") || resultado.startsWith("Conta já está ativa")) {
                                login(usuario, senha);
                            } else {
                                runOnUiThread(() -> Toast.makeText(this, "Erro na validação por código.", Toast.LENGTH_LONG).show());
                            }
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(this, "Ocorreu um erro ao conectar-se ao servidor.", Toast.LENGTH_LONG).show());
                }
            }
        } else {
            runOnUiThread(() -> Toast.makeText(this, "Verifique sua conexão com a internet.", Toast.LENGTH_SHORT).show());
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

        try {
            // Fazer a requisição
            er.post("login", userJson, response -> {
                if (response.startsWith("Erro")) {
                    runOnUiThread(() -> Toast.makeText(this, response, Toast.LENGTH_LONG).show());
                } else {
                    // Processar resposta da requisição
                    User responseLogin = gson.fromJson(response, User.class);
                    if (responseLogin != null) {
                        switch (responseLogin.getCode()) {
                            case 0: // Login bem sucedido
                                Intent intentTelaPrincipal = new Intent(this, TelaPrincipal.class);
                                Data data = responseLogin.getData();
                                intentTelaPrincipal.putExtra("nome_completo", responseLogin.getData().getNome_completo());
                                intentTelaPrincipal.putExtra("email", responseLogin.getData().getEmail());
                                intentTelaPrincipal.putExtra("url_foto", responseLogin.getData().getUrl_foto());
                                runOnUiThread(() -> Toast.makeText(this, "Conta ativada com sucesso!", Toast.LENGTH_LONG).show());
                                startActivity(intentTelaPrincipal);
                                finish();
                                break;

                            case 1: // Senha incorreta
                                runOnUiThread(() -> Toast.makeText(this, "Senha incorreta. Entre novamente e verifique-a.", Toast.LENGTH_SHORT).show());
                                break;

                            case 2: // Conta bloqueada por excesso de tentativas
                                if (responseLogin.getMessage().endsWith("ativação novamente necessária")) {
                                    runOnUiThread(() -> Toast.makeText(this, "Ativação de conta necessária por excesso de tentativas.", Toast.LENGTH_SHORT).show());
                                } else {
                                    runOnUiThread(() -> Toast.makeText(this, "Conta bloqueada por excesso de tentativas. Aguarde " + segToMin(responseLogin.getRestante()) + ".", Toast.LENGTH_SHORT).show());
                                }
                                break;

                            case 3: // Conta não está ativa
                                runOnUiThread(() -> Toast.makeText(this, "Conta ainda não ativada. Aguarde e tente novamente.", Toast.LENGTH_LONG).show());
                                break;

                            case 4: // Conta não encontrada
                                runOnUiThread(() -> Toast.makeText(this, responseLogin.getMessage(), Toast.LENGTH_SHORT).show());
                                break;

                            case 12: // Conexão não encontrada
                                runOnUiThread(() -> Toast.makeText(this, "Houve um problema na conexão. Por favor, reinicie o aplicativo.", Toast.LENGTH_SHORT).show());
                                break;
                        }
                    }
                }
            });
        } catch (Exception e) {
            runOnUiThread(() -> Toast.makeText(this, "Ocorreu um erro ao conectar-se ao servidor.", Toast.LENGTH_SHORT).show());
        }
    }

    private String segToMin(double seg) {
        int minutos = (int) (seg / 60);
        long segundos = Math.round(seg % 60);
        return minutos + "m " + segundos + "s";
    }
}

