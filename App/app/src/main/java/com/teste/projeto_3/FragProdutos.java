package com.teste.projeto_3;

import android.os.Bundle;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

public class FragProdutos extends Fragment {

    private ConstraintLayout mainLayout;
    private int boxCounter = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Infla o layout do fragment
        View rootView = inflater.inflate(R.layout.fragment_frag_produtos, container, false);

        // Inicializa o layout principal
        mainLayout = rootView.findViewById(R.id.main);

        // Configura o botão para adicionar novos "boxes"
        Button buttonAdd = rootView.findViewById(R.id.button_add);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup();
            }
        });

        SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        viewModel.setNomeCompleto("Outro nome");

        return rootView;  // Retorna a view inflada
    }

    private void showPopup() {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_input, null);

        final PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        EditText editTextName = popupView.findViewById(R.id.editTextName);
        Button buttonSubmit = popupView.findViewById(R.id.buttonSubmit);

        editTextName.setTextColor(getResources().getColor(android.R.color.black));
        editTextName.setHintTextColor(getResources().getColor(android.R.color.darker_gray));

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
        AppCompatButton buttonBox = new AppCompatButton(new ContextThemeWrapper(getContext(), R.style.ContainerBox));
        buttonBox.setId(View.generateViewId());
        buttonBox.setText(name);
        buttonBox.setBackgroundResource(R.drawable.container_box);
        buttonBox.setTextColor(getResources().getColor(android.R.color.black));

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