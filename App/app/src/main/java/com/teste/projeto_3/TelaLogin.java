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
        if (er.possuiInternet(getApplicationContext())) {
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

                                case 2: // Conta bloqueada por excesso de tentativas
                                    if (responseLogin.getMessage().endsWith("ativação novamente necessária")) {
                                        runOnUiThread(() -> Toast.makeText(this, "Ativação de conta necessária por excesso de tentativas.", Toast.LENGTH_SHORT).show());
                                    } else {
                                        runOnUiThread(() -> Toast.makeText(this, "Conta bloqueada por excesso de tentativas. Aguarde " + segToMin(responseLogin.getRestante()) + ".", Toast.LENGTH_SHORT).show());
                                    }
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

                                case 14: // Usuário já logado
                                    get_dados();

                            }
                        } catch (Exception e) {
                            runOnUiThread(() -> Toast.makeText(this, "Erro ao processar a resposta. Tente novamente.", Toast.LENGTH_SHORT).show());
                        }
                    }
                });
            }
        } else {
            runOnUiThread(() -> Toast.makeText(this, "Verifique sua conexão com a internet.", Toast.LENGTH_SHORT).show());
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

    private String segToMin(double seg) {
        int minutos = (int) (seg / 60);
        long segundos = Math.round(seg % 60);
        return minutos + "m " + segundos + "s";
    }

    private void get_dados() {
        if (er.possuiInternet(getApplicationContext())) {
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
                } else {
                    try {
                        // Processar resposta da requisição
                        User responseAutoLogin = gson.fromJson(response, User.class);
                        if (responseAutoLogin.getCode() != 15) { // Code 15 = "Usuario nao logado"
                            switch (responseAutoLogin.getCode()) {
                                case 0: // Login bem sucedido
                                    Intent intentTelaPrincipal = new Intent(this, TelaPrincipal.class);
                                    intentTelaPrincipal.putExtra("nome_completo", responseAutoLogin.getNome_completo());
                                    intentTelaPrincipal.putExtra("email", responseAutoLogin.getEmail());
                                    intentTelaPrincipal.putExtra("url_foto", responseAutoLogin.getUrl_foto());
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
                                    runOnUiThread(() -> Toast.makeText(this, "Erro na conexão automática. Conta não encontrada.", Toast.LENGTH_SHORT).show());
                                    Intent intentTelaLoginNaoEncontrado = new Intent(this, TelaLogin.class);
                                    startActivity(intentTelaLoginNaoEncontrado);
                                    finish();
                                    break;

                            }
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(this, "Erro ao processar a resposta. Tente novamente.", Toast.LENGTH_SHORT).show());
                    }
                }
            });
        } else {
            runOnUiThread(() -> Toast.makeText(this, "Verifique sua conexão com a internet.", Toast.LENGTH_SHORT).show());
        }
    }
}