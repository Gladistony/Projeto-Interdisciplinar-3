package com.teste.projeto_3;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.teste.projeto_3.http.EnviarRequisicao;
import com.teste.projeto_3.model.Estoque;
import com.teste.projeto_3.model.User;

import java.util.ArrayList;

public class FragStock extends Fragment implements RecyclerViewInterface{
    public static AdaptadorEstoqueRecyclerView adaptadorItemEstoque;
    public SharedViewModel viewModel;
    public ArrayList<Estoque> arrayEstoque;
    private EnviarRequisicao er;
    private CameraGaleria cg;
    private final Gson gson = new Gson();
    private String imagemBase64 = "";

    public interface CallbackImagem {
        void onComplete(String urlImagem);
    }

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

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewEstoque);
        viewModel.getUser().observe(getViewLifecycleOwner(), dados -> {
            if (dados.getData() == null) { // Login por requisição de get_dados
                arrayEstoque = new ArrayList<>(dados.getEstoque());
            } else { // Login por requisição de login
                arrayEstoque = new ArrayList<>(dados.getData().getEstoque());
            }

            if (!arrayEstoque.isEmpty()) {
                TextView textoEstoqueVazio = view.findViewById(R.id.textoEstoqueVazio);
                textoEstoqueVazio.setVisibility(View.GONE);
            }

            adaptadorItemEstoque = new AdaptadorEstoqueRecyclerView(requireContext(), this, arrayEstoque);
            recyclerView.setAdapter(adaptadorItemEstoque);
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        });

        return view;  // Retorna a view inflada
    }

    @Override
    public void onItemClick(int position) {
        Intent intentProduto = new Intent(requireContext(), TelaProduto.class);
        viewModel.getUser().observe(getViewLifecycleOwner(), dados -> {
            if (dados.getData() == null) { // Login por requisição de get_dados
                intentProduto.putParcelableArrayListExtra("produto", new ArrayList<>(dados.getEstoque().get(position).getProdutos()));
                intentProduto.putParcelableArrayListExtra("estoque", new ArrayList<>(dados.getEstoque()));
            } else { // Login por requisição de login
                intentProduto.putParcelableArrayListExtra("produto", new ArrayList<>(dados.getData().getEstoque().get(position).getProdutos()));
                intentProduto.putParcelableArrayListExtra("estoque", new ArrayList<>(dados.getData().getEstoque()));
            }
            intentProduto.putExtra("tituloEstoque", dados.getEstoque().get(position).getNome());
            intentProduto.putExtra("idEstoque", dados.getEstoque().get(position).getId());
            intentProduto.putExtra("position", position);
            startActivity(intentProduto);
        });
    }

    @Override
    public void onItemLongClick(int position) {
        arrayEstoque.remove(position);
        adaptadorItemEstoque.notifyItemRemoved(position);
    }

    private void criarEstoque(String nome, String descricao) {
        if (er.possuiInternet(requireContext())) {
            if (imagemBase64.isEmpty()) {
                enviarEstoque(nome, descricao, "");
            } else {
                upload_img(imagemBase64, new CallbackImagem() {
                    @Override
                    public void onComplete(String urlImagem) {
                        if (urlImagem != null) {
                            enviarEstoque(nome, descricao, urlImagem);
                        } else {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Falha no upload da imagem", Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                });
            }
        }
    }

    private void enviarEstoque(String nome, String descricao, String urlImagem) {
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
                            if (responseEstoque.getCode() == 0) {
                                    requireActivity().runOnUiThread(() -> {
                                        adaptadorItemEstoque.adicionarArrayEstoque(estoque);
                                        Toast.makeText(requireContext(), "Novo Stock adicionado com sucesso", Toast.LENGTH_LONG).show();
                                    });
                            } else {
                                requireActivity().runOnUiThread(() -> {
                                    Toast.makeText(requireContext(), "Falha em adicionar o produto ao Stock", Toast.LENGTH_LONG).show();
                                });
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

    private void upload_img(String imagemBase64, CallbackImagem callback) {
        if (er.possuiInternet(requireContext())) {
            User user = new User();
            user.setId(er.obterMemoriaInterna("idConexao"));
            user.setDestino("outro");
            user.setFile(imagemBase64);

            // Converter o objeto User para JSON
            String userJson = gson.toJson(user);

            er.post("upload_img", userJson, response -> {
                if (response.startsWith("Erro")) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), response, Toast.LENGTH_LONG).show()
                    );
                    callback.onComplete(null); // Erro no upload
                } else if (response.startsWith("<html>")) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "O arquivo de imagem é muito grande", Toast.LENGTH_LONG).show()
                    );
                    callback.onComplete(null); // Arquivo muito grande
                } else {
                    try {
                        // Processar resposta da requisição
                        User responseUpload = gson.fromJson(response, User.class);
                        if (responseUpload.getCode() == 0) {
                            callback.onComplete(responseUpload.getUrl()); // Sucesso
                        } else {
                            callback.onComplete(null);
                        }
                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Erro ao processar o upload da imagem", Toast.LENGTH_SHORT).show()
                        );
                        callback.onComplete(null);
                    }
                }
            });
        } else {
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "Verifique sua conexão com a internet.", Toast.LENGTH_SHORT).show()
            );
            callback.onComplete(null);
        }
    }


    private void dialogCriarEstoque() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_criar_estoque);
        dialog.setCanceledOnTouchOutside(false);

        ImageView imagemCriarEstoque = dialog.findViewById(R.id.imagemRegistrarProduto);
        Button selecionarCamera = dialog.findViewById(R.id.selecionarCameraProduto);
        Button selecionarGaleria = dialog.findViewById(R.id.selecionarGaleriaProduto);
        Button cancelarCriarEstoque = dialog.findViewById(R.id.cancelarRegistroProduto);
        Button criarEstoque = dialog.findViewById(R.id.registrarProduto);

        selecionarCamera.setOnClickListener(v -> cg.pedirPermissaoCamera(new CameraGaleria.CallbackCameraGaleria() {
            @Override
            public void onImageSelected(Uri uri) {
                imagemBase64 = cg.converterUriParaBase64(uri);
                // Método assíncrono para exibir a imagem na tela e apagar o arquivo logo em seguida
                Glide.with(requireContext()).load(uri).diskCacheStrategy(DiskCacheStrategy.ALL).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Toast.makeText(requireContext(), "Erro ao carregar a imagem na tela", Toast.LENGTH_LONG).show();
                        return false;
                    }
                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        cg.deletarImagemUri(uri);
                        return false;
                    }
                }).into(imagemCriarEstoque);
            }
        }));

        selecionarGaleria.setOnClickListener(v -> cg.pedirPermissaoGaleria(new CameraGaleria.CallbackCameraGaleria() {
            @Override
            public void onImageSelected(Uri uri) {
                imagemBase64 = cg.converterUriParaBase64(uri);
                Glide.with(requireContext()).load(uri).diskCacheStrategy(DiskCacheStrategy.ALL).into(imagemCriarEstoque);
            }
        }));

        cancelarCriarEstoque.setOnClickListener(v-> dialog.dismiss());

        criarEstoque.setOnClickListener(v-> {
            EditText nomeEstoque = dialog.findViewById(R.id.inserirNomeProduto);
            EditText descricaoEstoque = dialog.findViewById(R.id.inserirDescricaoProduto);

            if (nomeEstoque.getText().toString().isEmpty() || descricaoEstoque.getText().toString().isEmpty()) {
                Toast.makeText(requireContext(), "Insira nome e descrição para o Stock.", Toast.LENGTH_SHORT).show();
            } else {
                criarEstoque(nomeEstoque.getText().toString(), descricaoEstoque.getText().toString());
                dialog.dismiss();
            }
                });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.CENTER);
    }
}