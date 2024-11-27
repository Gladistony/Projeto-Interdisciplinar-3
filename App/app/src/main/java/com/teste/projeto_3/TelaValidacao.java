package com.teste.projeto_3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TelaValidacao extends AppCompatActivity {

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

        descricaoAviso();

    }

    public void descricaoAviso() {
        String email = getIntent().getStringExtra("email");
        TextView descricaoValidacao = findViewById(R.id.descricaoValidacao);
        String textoAviso = "Um código de validação foi enviado para " + email +
                ". Verifique sua caixa de entrada e insira-o abaixo.";
        descricaoValidacao.setText(textoAviso);
    }

    public void validar(View v){
        EditText validacao = findViewById(R.id.codigo_validacao);
        if (validacao.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), "Insira o código de validação antes de prosseguir", Toast.LENGTH_SHORT).show();
        } else {
            Intent verificado = new Intent(this, TelaPrincipal.class);
            startActivity(verificado);
        }

    }
}