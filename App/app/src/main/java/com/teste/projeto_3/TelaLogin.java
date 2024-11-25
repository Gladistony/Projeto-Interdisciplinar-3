package com.teste.projeto_3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;

public class TelaLogin extends AppCompatActivity {

    String emailDigitado;
    String senhaDigitada;

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
    }

    public void logar(View v){
        Intent intent = new Intent(this, TelaValidacao.class);
        TextInputLayout emailLayout = findViewById(R.id.layoutTypeEmail);
        TextInputLayout senhaLayout = findViewById(R.id.layoutTypePassword);
        emailDigitado = String.valueOf(emailLayout.getEditText().getText());
        senhaDigitada = String.valueOf(senhaLayout.getEditText().getText());
        intent.putExtra("email", emailDigitado);
        intent.putExtra("senha", senhaDigitada);
        startActivity(intent);
    }
}