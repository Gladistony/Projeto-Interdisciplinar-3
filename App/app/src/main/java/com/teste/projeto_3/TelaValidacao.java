package com.teste.projeto_3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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

    ProgressBar progressBarValidarCodigo;
    ProgressBar progressBarValidarEmail;
    Button botaoValidarCodigo;
    Button botaoValidarEmail;

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
        progressBarValidarCodigo = findViewById(R.id.progressBarValidarCodigo);
        progressBarValidarEmail = findViewById(R.id.progressBarValidarEmail);
        botaoValidarCodigo = findViewById(R.id.botaoValidarCodigo);
        botaoValidarEmail = findViewById(R.id.botaoValidarEmail);

        Button voltar = findViewById(R.id.botaoVoltarValidacao);
        voltar.setOnClickListener(v -> finish());
    }

    public void validarPorEmail(View v) {
        if (er.possuiInternet(getApplicationContext())) {
            runOnUiThread(() -> changeButtonMode(botaoValidarEmail, progressBarValidarEmail, getString(R.string.text_validar_email)));
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
                        runOnUiThread(() -> {
                            Toast.makeText(this, response, Toast.LENGTH_LONG).show();
                            changeButtonMode(botaoValidarEmail, progressBarValidarEmail, getString(R.string.text_validar_email));
                        });
                    } else {
                        // Processar resposta da requisição
                        User responseLogin = gson.fromJson(response, User.class);
                            switch (responseLogin.getCode()) {
                                case 0: // Login bem sucedido
                                    Intent intentTelaPrincipal = new Intent(this, TelaPrincipalActivity.class);
                                    intentTelaPrincipal.putExtra("dados", responseLogin);
                                    startActivity(intentTelaPrincipal);
                                    finishAffinity();
                                    break;

                                case 3: // Conta não está ativa
                                    runOnUiThread(() -> {
                                        Toast.makeText(this, "Conta ainda não ativada. Aguarde e tente novamente.", Toast.LENGTH_LONG).show();
                                        changeButtonMode(botaoValidarEmail, progressBarValidarEmail, getString(R.string.text_validar_email));
                                    });
                                    break;

                                case 4: // Conta não encontrada
                                    runOnUiThread(() -> {
                                        Toast.makeText(this, responseLogin.getMessage(), Toast.LENGTH_SHORT).show();
                                        changeButtonMode(botaoValidarEmail, progressBarValidarEmail, getString(R.string.text_validar_email));
                                    });
                                    break;

                                case 12: // Conexão não encontrada
                                    runOnUiThread(() -> {
                                        Toast.makeText(this, "Houve um problema na conexão. Por favor, reinicie o aplicativo.", Toast.LENGTH_SHORT).show();
                                        changeButtonMode(botaoValidarEmail, progressBarValidarEmail, getString(R.string.text_validar_email));
                                    });
                                    break;
                            }
                        }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Ocorreu um erro ao conectar-se ao servidor.", Toast.LENGTH_SHORT).show();
                    changeButtonMode(botaoValidarEmail, progressBarValidarEmail, getString(R.string.text_validar_email));
                });
            }
        } else {
            runOnUiThread(() -> {
                Toast.makeText(this, "Verifique sua conexão com a internet.", Toast.LENGTH_SHORT).show();
            });
        }
    }

    public void validarPorCodigo(View v) {
        if (er.possuiInternet(getApplicationContext())) {
            runOnUiThread(() -> changeButtonMode(botaoValidarCodigo, progressBarValidarCodigo, getString(R.string.validar_conta)));
            EditText validacao = findViewById(R.id.codigo_validacao);
            Intent intentInfoTelaLogin = getIntent();

            if (validacao.getText().toString().isEmpty()) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Por favor, preencha todos os campos obrigatórios", Toast.LENGTH_SHORT).show();
                    changeButtonMode(botaoValidarCodigo, progressBarValidarCodigo, getString(R.string.validar_conta));
                });
            } else {

                String usuario = intentInfoTelaLogin.getStringExtra("usuario");
                String senha = intentInfoTelaLogin.getStringExtra("senha");
                String codigoValidacao = validacao.getText().toString();

                try {
                    // Fazer a requisição
                    er.get("ativar", usuario + "/" + codigoValidacao, response -> {
                        if (response.startsWith("Erro")) {
                            runOnUiThread(() -> {
                                Toast.makeText(this, response, Toast.LENGTH_LONG).show();
                                changeButtonMode(botaoValidarCodigo, progressBarValidarCodigo, getString(R.string.validar_conta));
                            });
                        } else {
                            // Processar resposta da requisição
                            String resultado = response.substring(905); // 905 é o índice da String HTML em que diz o resultado da requisição
                            if (resultado.startsWith("Código de ativação incorreto")) {
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Código de ativação incorreto", Toast.LENGTH_LONG).show();
                                    changeButtonMode(botaoValidarCodigo, progressBarValidarCodigo, getString(R.string.validar_conta));
                                });
                            } else if (resultado.startsWith("Operação realizada com sucesso") || resultado.startsWith("Conta já está ativa")) {
                                login(usuario, senha);
                            } else {
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Erro na validação por código.", Toast.LENGTH_LONG).show();
                                    changeButtonMode(botaoValidarCodigo, progressBarValidarCodigo, getString(R.string.validar_conta));
                                });
                            }
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Ocorreu um erro ao conectar-se ao servidor.", Toast.LENGTH_LONG).show();
                        changeButtonMode(botaoValidarCodigo, progressBarValidarCodigo, getString(R.string.validar_conta));
                    });
                }
            }
        } else {
            runOnUiThread(() -> {
                Toast.makeText(this, "Verifique sua conexão com a internet.", Toast.LENGTH_SHORT).show();
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

        try {
            // Fazer a requisição
            er.post("login", userJson, response -> {
                if (response.startsWith("Erro")) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, response, Toast.LENGTH_LONG).show();
                        changeButtonMode(botaoValidarCodigo, progressBarValidarCodigo, getString(R.string.validar_conta));
                    });
                } else {
                    // Processar resposta da requisição
                    User responseLogin = gson.fromJson(response, User.class);
                    if (responseLogin != null) {
                        switch (responseLogin.getCode()) {
                            case 0: // Login bem sucedido
                                Intent intentTelaPrincipal = new Intent(this, TelaPrincipalActivity.class);
                                intentTelaPrincipal.putExtra("dados", responseLogin);
                                startActivity(intentTelaPrincipal);
                                finishAffinity();
                                break;

                            case 1: // Senha incorreta
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Senha incorreta. Entre novamente e verifique-a.", Toast.LENGTH_LONG).show();
                                    changeButtonMode(botaoValidarCodigo, progressBarValidarCodigo, getString(R.string.validar_conta));
                                    botaoValidarEmail.setAlpha(0.5f);
                                    botaoValidarCodigo.setAlpha(0.5f);
                                    botaoValidarCodigo.setClickable(false);
                                    botaoValidarEmail.setClickable(false);
                                });
                                break;

                            case 2: // Conta bloqueada por excesso de tentativas
                                if (responseLogin.getMessage().endsWith("ativação novamente necessária")) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(this, "Ativação de conta necessária por excesso de tentativas.", Toast.LENGTH_SHORT).show();
                                        changeButtonMode(botaoValidarCodigo, progressBarValidarCodigo, getString(R.string.validar_conta));
                                    });
                                } else {
                                    runOnUiThread(() -> {
                                        Toast.makeText(this, "Conta bloqueada por excesso de tentativas. Aguarde " + segToMin(responseLogin.getRestante()) + ".", Toast.LENGTH_SHORT).show();
                                        changeButtonMode(botaoValidarCodigo, progressBarValidarCodigo, getString(R.string.validar_conta));
                                    });
                                }
                                break;

                            case 3: // Conta não está ativa
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Conta ainda não ativada. Aguarde e tente novamente.", Toast.LENGTH_LONG).show();
                                    changeButtonMode(botaoValidarCodigo, progressBarValidarCodigo, getString(R.string.validar_conta));
                                });
                                break;

                            case 4: // Conta não encontrada
                                runOnUiThread(() -> {
                                    Toast.makeText(this, responseLogin.getMessage(), Toast.LENGTH_SHORT).show();
                                    changeButtonMode(botaoValidarCodigo, progressBarValidarCodigo, getString(R.string.validar_conta));
                                });
                                break;

                            case 12: // Conexão não encontrada
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Houve um problema na conexão. Por favor, reinicie o aplicativo.", Toast.LENGTH_SHORT).show();
                                    changeButtonMode(botaoValidarCodigo, progressBarValidarCodigo, getString(R.string.validar_conta));
                                });
                                break;
                        }
                    }
                }
            });
        } catch (Exception e) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Ocorreu um erro ao conectar-se ao servidor.", Toast.LENGTH_SHORT).show();
                changeButtonMode(botaoValidarCodigo, progressBarValidarCodigo, getString(R.string.validar_conta));
            });
        }
    }

    private String segToMin(double seg) {
        int minutos = (int) (seg / 60);
        long segundos = Math.round(seg % 60);
        return minutos + "m " + segundos + "s";
    }

    private void changeButtonMode(Button button, ProgressBar progressBar, String buttonString) {
        if (progressBar.getVisibility() == View.GONE){
            progressBar.setVisibility(View.VISIBLE);
            button.setText("");
            button.setClickable(false);
            button.setAlpha(0.8f);
        } else {
            progressBar.setVisibility(View.GONE);
            button.setText(buttonString);
            button.setClickable(true);
            button.setAlpha(1f);
        }
    }
}

