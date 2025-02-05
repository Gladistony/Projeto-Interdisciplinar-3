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

import com.teste.projeto_3.retrofitconnection.DataHandler;

public class TelaLogin extends AppCompatActivity {
    DataHandler dh;
    EditText usuario;
    EditText senha;
    private boolean isLoginInProgress = false;

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
        dh = new DataHandler(getApplicationContext());
    }

    public void login(View v) {
        if (isLoginInProgress) {
            return; // Se um login já está em andamento, saia do método
        }

        isLoginInProgress = true; // Marque que um login está em andamento

        usuario = findViewById(R.id.usuario);
        senha = findViewById(R.id.senha);

        if (usuario.getText().toString().isEmpty() || senha.getText().toString().isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos obrigatórios", Toast.LENGTH_SHORT).show();
            isLoginInProgress = false; // Libere o estado de login em andamento
        } else {
            if (dh.obterIdConexao().equals("defaultString") || dh.obterIdConexao().isEmpty()) {
                dh.novoIdRequest();
            }
            dh.loginRequest(usuario.getText().toString(), senha.getText().toString()).thenAccept(requestResponse -> {
                switch (requestResponse.getCode()) {
                    case 0: // Login bem sucedido
                        Intent intentTelaPrincipal = new Intent(this, TelaPrincipal.class);
                        intentTelaPrincipal.putExtra("nome_completo", requestResponse.getNome_completo());
                        intentTelaPrincipal.putExtra("email", requestResponse.getEmail());
                        startActivity(intentTelaPrincipal);
                        finish();
                        break;

                    case 1: // Senha incorreta
                        Toast.makeText(this, requestResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        break;

                    case 2: // Conta bloqueada por 5 minutos
                        Toast.makeText(this, requestResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        break;

                    case 3: // Conta não está ativa
                        Intent intentTelaValidacao = new Intent(this, TelaValidacao.class);
                        intentTelaValidacao.putExtra("usuario", usuario.getText().toString());
                        intentTelaValidacao.putExtra("senha", senha.getText().toString());
                        startActivity(intentTelaValidacao);
                        finish();
                        break;

                    case 4: // Conta não encontrada
                        Toast.makeText(this, requestResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        break;

                    case 12: // Conexão não encontrada
                        dh.salvarIdConexao("defaultString");
                        Toast.makeText(this, "Houve um problema na conexão. Por favor, tente novamente.", Toast.LENGTH_SHORT).show();
                        break;
                }
                isLoginInProgress = false; // Libere o estado de login em andamento após a resposta
            }).exceptionally(e -> {
                Toast.makeText(this, "Ocorreu um erro inesperado. Por favor, tente novamente.", Toast.LENGTH_SHORT).show();
                isLoginInProgress = false; // Libere o estado de login em andamento em caso de erro
                return null;
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
