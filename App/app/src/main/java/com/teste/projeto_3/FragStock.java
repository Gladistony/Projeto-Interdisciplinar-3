package com.teste.projeto_3;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.teste.projeto_3.http.EnviarRequisicao;
import com.teste.projeto_3.model.Estoque;
import com.teste.projeto_3.model.User;
import java.util.ArrayList;

public class FragStock extends Fragment implements RecyclerViewInterface{
    private ConstraintLayout mainLayout;
    private int boxCounter = 0;
    private AdaptadorEstoqueRecyclerView adaptadorItem;
    public SharedViewModel viewModel;
    public ArrayList<Estoque> estoque;
    private EnviarRequisicao er;
    private CameraGaleria cg;
    private final Gson gson = new Gson();

    private Uri uriImagem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        er = new EnviarRequisicao(requireContext());
        cg = new CameraGaleria(requireActivity(), requireActivity().getActivityResultRegistry(), requireActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Infla o layout do fragment
        View view = inflater.inflate(R.layout.fragment_frag_stock, container, false);

        FloatingActionButton criarEstoque = view.findViewById(R.id.botaoCriarEstoque);
        criarEstoque.setOnClickListener(v -> dialogCriarEstoque());

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

        return view;  // Retorna a view inflada
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
        Intent intentProduto = new Intent(requireContext(), TelaProduto.class);
        viewModel.getUser().observe(getViewLifecycleOwner(), dados -> {
            if (dados.getData() == null) { // Login por requisição de get_dados
                intentProduto.putParcelableArrayListExtra("produto", new ArrayList<>(dados.getEstoque().get(position).getProdutos()));
            } else { // Login por requisição de login
                intentProduto.putParcelableArrayListExtra("produto", new ArrayList<>(dados.getData().getEstoque().get(position).getProdutos()));
            }
            intentProduto.putExtra("tituloEstoque", dados.getEstoque().get(position).getNome());
            startActivity(intentProduto);
        });
    }

    @Override
    public void onItemLongClick(int position) {
        estoque.remove(position);
        adaptadorItem.notifyItemRemoved(position);
    }

    private void criarEstoque(String nome, String descricao, String urlImagem) {
        if (er.possuiInternet(requireContext())) {
                // Criando o objeto User
                Estoque estoque = new Estoque();
                estoque.setId(er.obterMemoriaInterna("idConexao"));
                estoque.setNome(nome);
                estoque.setDescricao(descricao);
                estoque.setImagem(urlImagem);

                // Converter o objeto User para JSON
                String userJson = gson.toJson(estoque);

                // Fazer a requisição
                er.post("criar_estoque", userJson, response -> {
                    if (response.startsWith("Erro")) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), response, Toast.LENGTH_LONG).show());
                    } else {
                        try {
                            // Processar resposta da requisição
                            User responseEstoque = gson.fromJson(response, User.class);
                            switch (responseEstoque.getCode()) {
                                case 0:

                            }
                        } catch (Exception e) {
                            requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Erro ao processar a resposta. Tente novamente.", Toast.LENGTH_SHORT).show());
                        }
                }
            });
        } else {
            requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Verifique sua conexão com a internet.", Toast.LENGTH_SHORT).show());
        }
    }

    private void upload_img(String imagemBase64) {
        if (er.possuiInternet(requireContext())) {
            User user = new User();
            user.setId(er.obterMemoriaInterna("idConexao"));
            user.setDestino("perfil");
            user.setFile(imagemBase64);

            // Converter o objeto User para JSON
            String userJson = gson.toJson(user);

            er.post("upload_img", userJson, response -> {
                if (response.startsWith("Erro")) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), response, Toast.LENGTH_LONG).show();
                    });
                } else if (response.startsWith("<html>")) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "O arquivo de imagem é muito grande", Toast.LENGTH_LONG).show();
                    });
                } else {
                    try {
                        // Processar resposta da requisição
                        User responseUpload = gson.fromJson(response, User.class);
                        if (responseUpload.getCode() == 0) {

                                }
                            } catch (Exception e) {
                                requireActivity().runOnUiThread(() -> {
                                    Toast.makeText(requireContext(), "Erro ao definir a imagem de perfil na interface gráfica", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
            });
        } else {
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Verifique sua conexão com a internet.", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void dialogCriarEstoque() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_criar_estoque);

        ImageView imageView = dialog.findViewById(R.id.selecionarImagemEstoque);
        imageView.setOnClickListener(v -> cg.pedirPermissaoCamera(new CameraGaleria.CallbackCameraGaleria() {
            @Override
            public void onImageSelected(Uri uri) {
                uriImagem = uri;
            }
        }));



        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.CENTER);
    }
}