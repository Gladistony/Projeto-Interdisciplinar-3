package com.teste.projeto_3;


import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.teste.projeto_3.http.EnviarRequisicao;
import com.teste.projeto_3.model.User;

public class TelaAlterarSenha extends AppCompatActivity {

    EnviarRequisicao er;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_alterar_senha);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        er = new EnviarRequisicao(getApplicationContext());
    }

    public void voltar(View v){
        finish();
    }

    public void togglePassword(View v) {
        EditText caixaTexto = findViewById(R.id.senhaAtual);
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

    public void alterarSenha(View v) {
        if (er.possuiInternet(getApplicationContext())) {

            EditText senhaAtual = findViewById(R.id.senhaAtual);
            EditText novaSenha = findViewById(R.id.novaSenha);
            EditText novaSenhaRepetida = findViewById(R.id.novaSenhaRepetida);

            // Se algum dos campos estão vazios
            if (senhaAtual.getText().toString().isEmpty() || novaSenha.getText().toString().isEmpty() || novaSenhaRepetida.getText().toString().isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, "Por favor, preencha todos os campos obrigatórios", Toast.LENGTH_SHORT).show());
            }
            // A senha nova não coincide com a que foi digitada novamente
            else if (!novaSenha.getText().toString().equals(novaSenhaRepetida.getText().toString())){
                runOnUiThread(() -> Toast.makeText(this, "A nova senha digitada não coincide com a confirmação", Toast.LENGTH_LONG).show());
            }
            // Nova senha igual a senha atual
            else if (senhaAtual.getText().toString().equals(novaSenha.getText().toString())) {
                runOnUiThread(() -> Toast.makeText(this, "A senha atual e a nova senha não podem ser iguais", Toast.LENGTH_LONG).show());
            }
            // A senha é menor que 8 caracteres
            else if (novaSenha.getText().toString().length() < 8){
                runOnUiThread(() -> Toast.makeText(this, "A nova senha deve ter no mínimo 8 caracteres", Toast.LENGTH_LONG).show());
            } else {
                // Obtém o usuário através do ViewModel
                SharedViewModel viewModel = new ViewModelProvider(this).get(SharedViewModel.class);
                String usuario;
                if (viewModel.getUser().getValue().getData() == null) { // Login através da requisição get_dados
                    usuario = viewModel.getUser().getValue().getUsuario();
                } else { // Login através da requisição login
                    usuario = viewModel.getUser().getValue().getData().getUsuario();
                }

                // Criando o objeto User
                User user = new User();
                user.setId(er.obterMemoriaInterna("idConexao"));
                user.setUsuario(usuario);
                user.setSenha(senhaAtual.getText().toString());
                user.setNova_senha(novaSenha.getText().toString());

                // Converter o objeto User para JSON
                Gson gson = new Gson();
                String userJson = gson.toJson(user);

                // Fazer a requisição
                er.post("charge", userJson, response -> { // Provavelmente é um erro de digitação em "change"
                    if (response.startsWith("Erro")) {
                        runOnUiThread(() -> Toast.makeText(this, response, Toast.LENGTH_LONG).show());
                    } else {
                        try {
                            // Processar resposta da requisição
                            User responseAlterar = gson.fromJson(response, User.class);
                            switch (responseAlterar.getCode()) {
                                case 0: // Senha alterada com sucesso
                                    runOnUiThread(() -> showPopupSucesso());
                                    break;

                                case 1: // Senha incorreta
                                    runOnUiThread(() -> Toast.makeText(this, responseAlterar.getMessage(), Toast.LENGTH_SHORT).show());
                                    break;

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

    private void showPopupSucesso() {
        AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setTitle("Sucesso")
                .setMessage("Sua senha foi alterada com sucesso.")
                .setPositiveButton("OK", (dialogInterface, which) -> finish())
                .setCancelable(false)
                .create();

        // Altera a cor do botão exibido
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(getResources().getColor(R.color.green2));
        });

        dialog.show();
    }

}