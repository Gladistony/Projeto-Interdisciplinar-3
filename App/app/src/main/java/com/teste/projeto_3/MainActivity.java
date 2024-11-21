package com.teste.projeto_3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    String typedEmail;
    String typedPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void nextScreen(View v){
        TextInputLayout textInputLayout = findViewById(R.id.layoutTypeEmail);
        typedEmail = String.valueOf(textInputLayout.getEditText().getText());

        TextInputLayout textInputLayout2 = findViewById(R.id.layoutTypePassword);
        typedPassword = String.valueOf(textInputLayout2.getEditText().getText());
        Intent intentNext = new Intent(this, LoggedInScreenTest.class);
        intentNext.putExtra("typedEmail", typedEmail);
        intentNext.putExtra("typedPassword", typedPassword);
        startActivity(intentNext);

    }
}
