package com.teste.projeto_3;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.teste.projeto_3.http.EnviarRequisicao;
import com.teste.projeto_3.model.User;

public class TelaRecuperarSenha extends AppCompatActivity {

    EnviarRequisicao er;
    Button botaoEnviarRecuperacao;
    ProgressBar progressBarEnviarRecuperacao;

    EditText caixaTextoRecuperacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_recuperar_senha);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        er = new EnviarRequisicao(this);
        botaoEnviarRecuperacao = findViewById(R.id.botaoEnviarRecuperarConta);
        progressBarEnviarRecuperacao = findViewById(R.id.progressBarEnviarRecuperarSenha);
        caixaTextoRecuperacao = findViewById(R.id.usuarioRecuperarSenha);

        Button voltar = findViewById(R.id.botaoVoltarRecuperarSenha);
        voltar.setOnClickListener(v -> finish());

        botaoEnviarRecuperacao.setOnClickListener(v -> recover());
    }

    public void recover() {
        if (er.possuiInternet(getApplicationContext())) {
            runOnUiThread(() -> changeButtonMode(botaoEnviarRecuperacao, progressBarEnviarRecuperacao, getString(R.string.botaoEnviarEmailEsqueciASenha)));

            if (caixaTextoRecuperacao.getText().toString().isEmpty()) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Insira um nome de usuário válido", Toast.LENGTH_SHORT).show();
                    changeButtonMode(botaoEnviarRecuperacao, progressBarEnviarRecuperacao, getString(R.string.botaoEnviarEmailEsqueciASenha));
                });
            } else {
                User userRecuperacao = new User();
                userRecuperacao.setId(er.obterMemoriaInterna("idConexao"));
                userRecuperacao.setUsuario(caixaTextoRecuperacao.getText().toString());

                // Converter o objeto User para JSON
                Gson gson = new Gson();
                String userJson = gson.toJson(userRecuperacao);

                // Fazer a requisição
                er.post("recover", userJson, response -> {
                    if (response.startsWith("Erro")) {
                        runOnUiThread(() -> {
                            Toast.makeText(this, response, Toast.LENGTH_LONG).show();
                            changeButtonMode(botaoEnviarRecuperacao, progressBarEnviarRecuperacao, getString(R.string.botaoEnviarEmailEsqueciASenha));
                        });
                    } else {
                        try {
                            // Processar resposta da requisição
                            User responseRecuperacao = gson.fromJson(response, User.class);
                            if (responseRecuperacao.getCode() == 21) {
                                runOnUiThread(() -> popupAvisarSucessoRequisicao());
                            } else if (responseRecuperacao.getCode() == 4) {
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Conta não encontrada. Verifique se digitou corretamente.", Toast.LENGTH_SHORT).show();
                                    changeButtonMode(botaoEnviarRecuperacao, progressBarEnviarRecuperacao, getString(R.string.botaoEnviarEmailEsqueciASenha));
                                });
                            }
                        } catch (Exception e) {
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Erro ao processar a resposta. Tente novamente.", Toast.LENGTH_SHORT).show();
                                changeButtonMode(botaoEnviarRecuperacao, progressBarEnviarRecuperacao, getString(R.string.botaoEnviarEmailEsqueciASenha));
                            });
                        }
                    }
                });
            }
        } else {
            runOnUiThread(() -> {
                Toast.makeText(this, "Verifique sua conexão com a internet.", Toast.LENGTH_SHORT).show();
            });
        }
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

    public void popupAvisarSucessoRequisicao() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Aviso")
                .setMessage("Um e-mail foi enviado com uma senha temporária e um código de ativação para a conta. Você pode redefinir sua nova senha nas configurações do perfil.")
                .setPositiveButton("OK", (dialogConfirmar, which) -> {
                    finish();
                })
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.green2));
        });

        dialog.show();
    }
}