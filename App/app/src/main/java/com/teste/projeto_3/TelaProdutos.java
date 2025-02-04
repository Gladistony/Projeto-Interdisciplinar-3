package com.teste.projeto_3;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.content.Context;

import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.appcompat.view.ContextThemeWrapper;

public class TelaProdutos extends Activity {
    private ConstraintLayout mainLayout;
    private int boxCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_produtos);
        mainLayout = findViewById(R.id.main);

        Button buttonAdd = findViewById(R.id.button_add);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup();
            }
        });
    }

    private void showPopup() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_input, null);

        final PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        EditText editTextName = popupView.findViewById(R.id.editTextName);
        Button buttonSubmit = popupView.findViewById(R.id.buttonSubmit);

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString();
                if (!name.isEmpty()) {
                    addBox(name);
                    popupWindow.dismiss();
                }
            }
        });

        popupWindow.showAtLocation(mainLayout, android.view.Gravity.CENTER, 0, 0);
    }

    private void addBox(String name) {
        // Criação do botão com o estilo definido
        AppCompatButton buttonBox = new AppCompatButton(new ContextThemeWrapper(this, R.style.ContainerBox));
        buttonBox.setId(View.generateViewId());
        buttonBox.setText(name);
        buttonBox.setBackgroundResource(R.drawable.container_box);

        // Define o layoutParams diretamente
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                getResources().getDimensionPixelSize(R.dimen.box_width),
                getResources().getDimensionPixelSize(R.dimen.box_height)
        );
        params.setMargins(
                getResources().getDimensionPixelSize(R.dimen.box_margin_horizontal),
                getResources().getDimensionPixelSize(R.dimen.box_margin_vertical),
                getResources().getDimensionPixelSize(R.dimen.box_margin_horizontal),
                0
        );
        buttonBox.setLayoutParams(params);

        // Adiciona o botão ao layout
        mainLayout.addView(buttonBox);

        // Configura as restrições
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(mainLayout);

        int row = boxCounter / 2;
        int column = boxCounter % 2;

        // Conecta o botão com as restrições do layout
        if (column == 0) {
            constraintSet.connect(buttonBox.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 10);
        } else {
            constraintSet.connect(buttonBox.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 10);
        }

        if (row == 0) {
            constraintSet.connect(buttonBox.getId(), ConstraintSet.TOP, R.id.containerTop, ConstraintSet.BOTTOM, 30);
        } else {
            View previousBox = mainLayout.getChildAt(mainLayout.getChildCount() - 3);
            constraintSet.connect(buttonBox.getId(), ConstraintSet.TOP, previousBox.getId(), ConstraintSet.BOTTOM, 30);
        }

        constraintSet.applyTo(mainLayout);
        boxCounter++;
    }

}
