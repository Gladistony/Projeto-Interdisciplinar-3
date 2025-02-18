package com.teste.projeto_3;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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

public class TelaLogin extends AppCompatActivity{

    EnviarRequisicao er;
    EditText usuario;
    EditText senha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        er = new EnviarRequisicao(getApplicationContext());

    }

    public void login(View v) {
        usuario = findViewById(R.id.usuario);
        senha = findViewById(R.id.senha);

        if (usuario.getText().toString().isEmpty() || senha.getText().toString().isEmpty()) {
            runOnUiThread(() -> Toast.makeText(this, "Por favor, preencha todos os campos obrigatórios", Toast.LENGTH_SHORT).show());
        } else {
            Log.d("ID obtido", er.obterMemoriaInterna("idConexao"));
            // Criando o objeto User
            User userLogin = new User();
            userLogin.setId(er.obterMemoriaInterna("idConexao"));
            userLogin.setUsuario(usuario.getText().toString());
            userLogin.setSenha(senha.getText().toString());

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
                                    startActivity(intentTelaPrincipal);
                                    finish();
                                    break;

                                case 1: // Senha incorreta
                                    runOnUiThread(() -> Toast.makeText(this, responseLogin.getMessage(), Toast.LENGTH_SHORT).show());
                                    break;

                                case 2: // Conta bloqueada por 5 minutos
                                    runOnUiThread(() -> Toast.makeText(this, "Conta bloqueada por excesso de tentativas. Ative-a novamente.", Toast.LENGTH_SHORT).show());
                                    break;

                                case 3: // Conta não está ativa
                                    Intent intentTelaValidacao = new Intent(this, TelaValidacao.class);
                                    intentTelaValidacao.putExtra("usuario", usuario.getText().toString());
                                    intentTelaValidacao.putExtra("senha", senha.getText().toString());
                                    startActivity(intentTelaValidacao);
                                    finish();
                                    break;

                                case 4: // Conta não encontrada
                                    runOnUiThread(() -> Toast.makeText(this, responseLogin.getMessage(), Toast.LENGTH_SHORT).show());
                                    break;
                                case 12: // Conexão não encontrada
                                    runOnUiThread(() -> Toast.makeText(this, "Houve um problema na conexão. Por favor, reinicie o aplicativo.", Toast.LENGTH_SHORT).show());
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

    public void togglePassword(View v) {
        EditText caixaTexto = findViewById(R.id.senha);
        int posicaoCursor = caixaTexto.getSelectionStart();

        if (caixaTexto.getInputType() == InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            // se inputType == 1 + 128 (código de textPassword) então inputType = 1 + 144 (código de textVisiblePassword)
            caixaTexto.setInputType(InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

            // troca o drawable pra olho fechado
            Drawable eyeHiddenDrawable = ContextCompat.getDrawable(this, R.drawable.ic_eye_hidden);
            caixaTexto.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, eyeHiddenDrawable, null);
        } else {
            // inputType é (código de textVisiblePassword) então inputType = 1 + 128 (código de textPassword)
            caixaTexto.setInputType(InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_PASSWORD);

            // troca o drawable pra olho aberto
            Drawable eyeOpenDrawable = ContextCompat.getDrawable(this, R.drawable.ic_eye);
            caixaTexto.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, eyeOpenDrawable, null);
        }

        // volta o cursor para a mesma posição antes de ocultar/exibir
        caixaTexto.setSelection(posicaoCursor);

    }

    public void voltar(View v) {
        finish();
    }

}