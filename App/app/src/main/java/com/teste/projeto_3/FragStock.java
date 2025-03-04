package com.teste.projeto_3;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.teste.projeto_3.model.Estoque;
import com.teste.projeto_3.model.Produto;

import java.util.ArrayList;

public class FragStock extends Fragment implements RecyclerViewInterface{

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ConstraintLayout mainLayout;
    private int boxCounter = 0;
    private AdaptadorEstoqueRecyclerView adaptadorItem;
    public SharedViewModel viewModel;
    public ArrayList<Estoque> estoque;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Infla o layout do fragment
        View view = inflater.inflate(R.layout.fragment_frag_stock, container, false);

        // Inicializa o layout principal
        mainLayout = view.findViewById(R.id.main);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewEstoque);
        viewModel.getUser().observe(getViewLifecycleOwner(), dados -> {
            if (dados.getData() == null) { // Login por requisição de get_dados
                estoque = new ArrayList<>(dados.getEstoque());
            } else { // Login por requisição de login
                estoque = new ArrayList<>(dados.getData().getEstoque());
            }

            if (!estoque.isEmpty()) {
                TextView textoEstoqueVazio = view.findViewById(R.id.textoEstoqueVazio);
                textoEstoqueVazio.setVisibility(View.GONE);
            }

            adaptadorItem = new AdaptadorEstoqueRecyclerView(requireContext(), this, estoque);
            recyclerView.setAdapter(adaptadorItem);
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        });

        // Configura o botão para adicionar novos "boxes"
        Button buttonAdd = view.findViewById(R.id.button_add);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup();
            }
        });

        FloatingActionButton fabCamera = view.findViewById(R.id.botaoCamera);

        // Inicializa o launcher da câmera
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");

                            // Aqui você pode definir onde deseja exibir a imagem capturada
                            requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Imagem capturada!", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Captura de imagem cancelada", Toast.LENGTH_SHORT).show());
                    }
                }
        );
        fabCamera.setOnClickListener(v -> abrirCamera());

        return view;  // Retorna a view inflada
    }

    private void abrirCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getContext().checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                    // Se o usuário já negou antes, mostra o diálogo para ativar manualmente a permissão
                    mostrarDialogoPermissao();
                } else {
                    // Solicita a permissão
                    requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 100);
                }
                return; // Sai da função até que a permissão seja concedida
            }
        }

        // Se já tem permissão, abre a câmera normalmente
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            cameraLauncher.launch(intent);
        } else {
            Toast.makeText(getContext(), "Câmera indisponível no dispositivo", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarDialogoPermissao() {
        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Permissão necessária")
                .setMessage("O aplicativo precisa da permissão para acessar a câmera. Por favor, ative-a nas configurações.")
                .setPositiveButton("Ir para Configurações", (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(android.net.Uri.parse("package:" + getContext().getPackageName()));
                    getContext().startActivity(intent);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Captura a resposta da solicitação de permissão
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamera();
            } else {
                Toast.makeText(getContext(), "Permissão para a câmera negada", Toast.LENGTH_SHORT).show();
            }
        }
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

    @Override
    public void onItemClick(int position) {
        Intent intentProduto = new Intent(requireContext(), ProdutoItem.class);
        viewModel.getUser().observe(getViewLifecycleOwner(), dados -> {
            if (dados.getData() == null) { // Login por requisição de get_dados
                intentProduto.putParcelableArrayListExtra("produto", new ArrayList<>(dados.getEstoque().get(position).getProdutos()));
            } else { // Login por requisição de login
                intentProduto.putParcelableArrayListExtra("produto", new ArrayList<>(dados.getData().getEstoque().get(position).getProdutos()));
            }
            startActivity(intentProduto);
        });
    }

    @Override
    public void onItemLongClick(int position) {
        estoque.remove(position);
        adaptadorItem.notifyItemRemoved(position);
    }
}