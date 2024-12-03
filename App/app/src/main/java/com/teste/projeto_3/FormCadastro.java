package com.teste.projeto_3;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
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
import com.teste.projeto_3.http.HttpHelper;
import com.teste.projeto_3.model.User;

import org.jetbrains.annotations.NotNull;

public class FormCadastro extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_form_cadastro);

        // Configurações de margens para Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Alternar exibição de senha
    public void togglePassword(View v) {
        EditText caixaTexto = findViewById(R.id.edit_senha);
        int posicaoCursor = caixaTexto.getSelectionStart();

        if (caixaTexto.getInputType() == InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            caixaTexto.setInputType(InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            Drawable eyeHiddenDrawable = ContextCompat.getDrawable(this, R.drawable.ic_eye_hidden);
            caixaTexto.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, eyeHiddenDrawable, null);
        } else {
            caixaTexto.setInputType(InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_PASSWORD);
            Drawable eyeOpenDrawable = ContextCompat.getDrawable(this, R.drawable.ic_eye);
            caixaTexto.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, eyeOpenDrawable, null);
        }

        caixaTexto.setSelection(posicaoCursor);
    }

    // Método de cadastro
    public void cadastrar(View v) {
        // Referência aos campos do formulário
        EditText editTextNome = findViewById(R.id.edit_nome);
        EditText editTextEmail = findViewById(R.id.edit_email);
        EditText editTextSenha = findViewById(R.id.edit_senha);
        EditText editTextUsuario = findViewById(R.id.edit_usuario);

        // Criar o objeto User
        User user = new User();
        user.setId(null); // ID inicial deve ser null
        user.setRequest("cadastro"); // Definir o tipo de requisição
        user.setNome_completo(editTextNome.getText().toString());
        user.setEmail(editTextEmail.getText().toString());
        user.setSenha(editTextSenha.getText().toString());
        user.setUsuario(editTextUsuario.getText().toString());

        // Validar campos obrigatórios
        if (user.getNome_completo().isEmpty() || user.getEmail().isEmpty() ||
                user.getSenha().isEmpty() || user.getUsuario().isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos obrigatórios!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Converter o objeto User para JSON usando Gson
        Gson gson = new Gson();
        String userJson = gson.toJson(user);

        // Enviar requisição para o servidor
        new Thread(() -> {
            HttpHelper httpHelper = new HttpHelper();
            String response = httpHelper.post(userJson); // Envia o JSON para a API

            // Atualizar a interface com o resultado
            runOnUiThread(() -> {
                if (response.startsWith("Erro")) {
                    Toast.makeText(this, response, Toast.LENGTH_LONG).show();
                } else {
                    User responseUser = gson.fromJson(response, User.class);
                    Toast.makeText(this, "Cadastro realizado com sucesso! ID: " + responseUser.getId(), Toast.LENGTH_LONG).show();

                    salvarIdConexao(responseUser.getId());

                    // Navegar para a próxima tela (ex: tela de login)
                    abrirTelaLogin(); // Método genérico para navegação
                }
            });
        }).start();
    }

    // Método para salvar a ID de conexão localmente
    private void salvarIdConexao(@NotNull String idConexao) {
        getSharedPreferences("AppPrefs", MODE_PRIVATE)
                .edit()
                .putString("idConexao", idConexao)
                .apply();
    }

    // Método para abrir a próxima tela
    private void abrirTelaLogin() {
        Intent intent = new Intent(this, TelaLogin.class);
        startActivity(intent);
        finish(); // Fecha a atividade atual
    }

    // Método para voltar à tela anterior
    public void voltar(View v) {
        finish();
    }
}