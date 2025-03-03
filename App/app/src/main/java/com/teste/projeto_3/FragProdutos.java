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
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class FragProdutos extends Fragment implements RecyclerViewInterface{

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ConstraintLayout mainLayout;
    private int boxCounter = 0;

    ArrayList<EstoqueItem> arrayEstoque = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Infla o layout do fragment
        View rootView = inflater.inflate(R.layout.fragment_frag_produtos, container, false);

        // Inicializa o layout principal
        mainLayout = rootView.findViewById(R.id.main);

        Button teste = rootView.findViewById(R.id.testee);
        teste.setOnClickListener(v-> trocarFotos());

        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerViewEstoque);
        criarListaEstoque();
        AdaptadorItemRecyclerView adaptadorItem = new AdaptadorItemRecyclerView(requireContext(), arrayEstoque, this);
        recyclerView.setAdapter(adaptadorItem);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Configura o botão para adicionar novos "boxes"
        Button buttonAdd = rootView.findViewById(R.id.button_add);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup();
            }
        });

        FloatingActionButton fabCamera = rootView.findViewById(R.id.botaoCamera);

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

        return rootView;  // Retorna a view inflada
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

    private void criarListaEstoque() {
        String[] nomeEstoque = new String[] {"a", "b", "c", "d", "e", "f", "g", "h"};
        String[] descricaoEstoque = new String[] {"a", "b", "c", "d", "e", "f", "g", "h"};
        String[] quantidadeEstoque = new String[] {"a", "b", "c", "d", "e", "f", "g", "h"};
        String[] imagemEstoque = new String[] {
                "https://plus.unsplash.com/premium_photo-1711434824963-ca894373272e?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MXx8aW1hZ2VtJTIwZGUlMjBmdW5kbyUyMGJvbml0YXxlbnwwfHwwfHx8MA%3D%3D",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRs5yf9d35OUjrxlQIpIhXop9rc9DN749axHenYIPlMk-aAzQrRDy94Ciins7zcfZfhE6o",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRs5yf9d35OUjrxlQIpIhXop9rc9DN749axHenYIPlMk-aAzQrRDy94Ciins7zcfZfhE6o",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRs5yf9d35OUjrxlQIpIhXop9rc9DN749axHenYIPlMk-aAzQrRDy94Ciins7zcfZfhE6o",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRs5yf9d35OUjrxlQIpIhXop9rc9DN749axHenYIPlMk-aAzQrRDy94Ciins7zcfZfhE6o",
                "https://s2-techtudo.glbimg.com/L9wb1xt7tjjL-Ocvos-Ju0tVmfc=/0x0:1200x800/984x0/smart/filters:strip_icc()/i.s3.glbimg.com/v1/AUTH_08fbf48bc0524877943fe86e43087e7a/internal_photos/bs/2023/q/l/TIdfl2SA6J16XZAy56Mw/canvaai.png",
                "https://d1muf25xaso8hp.cloudfront.net/https://img.criativodahora.com.br/2024/04/criativo-660da8df17c75img-2024-04-03660da8df17c7a.jpg?w=1000&h=&auto=compress&dpr=1&fit=max",
                "https://thumbs.dreamstime.com/b/imagem-de-fundo-bonita-do-c%C3%A9u-da-natureza-64743176.jpg"
        };

        for (int i = 0; i < nomeEstoque.length; i++) {
            arrayEstoque.add(new EstoqueItem(nomeEstoque[i], descricaoEstoque[i], quantidadeEstoque[i], imagemEstoque[i]));
        }

    }

    public void trocarFotos(){
        String[] novasFotos = new String[] {"https://static.vecteezy.com/ti/fotos-gratis/t2/9273280-conceito-de-solidao-e-decepcao-no-amor-homem-triste-sentado-elemento-da-imagem-e-decorado-pela-nasa-gratis-foto.jpg",
        "https://img.cdndsgni.com/preview/10097610.jpg",
        "https://alfredonegreirosadvocacia.adv.br/wp-content/uploads/2023/12/Direito-de-Imagem-do-Jogador-de-Futebol.jpg",
        "https://s1.static.brasilescola.uol.com.br/be/conteudo/images/imagem-em-lente-convexa.jpg",
        "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRNzygUVAv4t3nwtW8UGyp0jdOOwJU3Fl5elA&s",
        "https://t.ctcdn.com.br/JlHwiRHyv0mTD7GfRkIlgO6eQX8=/640x360/smart/i257652.jpeg",
        "https://tm.ibxk.com.br/2017/07/13/13160112901226.jpg",
        "https://s2-techtudo.glbimg.com/SkyLTd6VJy8WiUMg5L6EeUwgyMw=/0x0:620x548/984x0/smart/filters:strip_icc()/i.s3.glbimg.com/v1/AUTH_08fbf48bc0524877943fe86e43087e7a/internal_photos/bs/2021/B/t/limPwzQmSeI4WJO7haZg/2012-08-15-mf1.jpg"};

        for (int i = 0; i < novasFotos.length; i++) {
            arrayEstoque.get(i).setImagemEstoque(novasFotos[i]);
        }

    }


    @Override
    public void onItemClick(int position) {
        Intent intentProduto = new Intent(requireContext(), ProdutoItem.class);
        startActivity(intentProduto);
    }
}