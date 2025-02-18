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
import com.teste.projeto_3.http.EnviarRequisicao;
import com.teste.projeto_3.http.HttpHelper;
import com.teste.projeto_3.model.User;
import com.teste.projeto_3.retrofitconnection.DataHandler;

public class FormCadastro extends AppCompatActivity {

    EnviarRequisicao er;

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
        er = new EnviarRequisicao(getApplicationContext());
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

        // Criar o objeto User para a requisição
        User user = new User();
        user.setId(er.obterMemoriaInterna("idConexao"));
        user.setNome_completo(editTextNome.getText().toString());
        user.setEmail(editTextEmail.getText().toString());
        user.setSenha(editTextSenha.getText().toString());
        user.setUsuario(editTextUsuario.getText().toString());

        // Converter o objeto User para JSON
        Gson gson = new Gson();
        String userJson = gson.toJson(user);

        // Validar campos obrigatórios
        if (editTextNome.getText().toString().isEmpty() || editTextEmail.getText().toString().isEmpty() ||
                editTextSenha.getText().toString().isEmpty() || editTextUsuario.getText().toString().isEmpty()) {
            runOnUiThread(() -> Toast.makeText(this, "Por favor, preencha todos os campos obrigatórios!", Toast.LENGTH_SHORT).show());
        } else {
            er.post("cadastro", userJson, response -> {
                if (response.startsWith("Erro")) {
                    runOnUiThread(() -> Toast.makeText(this, response, Toast.LENGTH_LONG).show());
                } else {
                    try {
                        // Processar resposta da requisição
                        User responseCadastro = gson.fromJson(response, User.class);
                        switch (responseCadastro.getCode()) {
                            case 0:
                                runOnUiThread(() -> Toast.makeText(getApplicationContext(), responseCadastro.getMessage() + "! Entre para continuar.", Toast.LENGTH_LONG).show());
                                Intent intentTelaLogin = new Intent(this, MainActivity.class);
                                startActivity(intentTelaLogin);
                                finish();
                                break;
                            case 6:
                            case 7:
                            case 8:
                            case 9:
                            case 10:
                                runOnUiThread(() -> Toast.makeText(getApplicationContext(), responseCadastro.getMessage(), Toast.LENGTH_LONG).show());
                                break;
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(this, "Erro ao processar a resposta. Tente novamente.", Toast.LENGTH_SHORT).show());
                    }
                }
            });
        }
    }
}
