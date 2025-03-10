package com.teste.projeto_3;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
import android.widget.ProgressBar;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FragStock extends Fragment implements RecyclerViewInterface {
    public static AdaptadorEstoqueRecyclerView adaptadorItemEstoque;
    public static SharedViewModel viewModel;
    public ArrayList<Estoque> arrayEstoque;
    private EnviarRequisicao er;
    private CameraGaleria cg;
    private final Gson gson = new Gson();
    private String imagemBase64 = "";

    private TextView textoEstoqueVazio;

    public interface CallbackImagem {
        void onComplete(String urlImagem);
    }
    public interface ConfirmacaoApagarEstoque {
        void onConfirmacao(boolean confirmou);
    }

    private interface CallbackResponseUpload {
        void onCompleteUploadImg(String urlImagem);
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
        if (viewModel.getUser().getValue().getData() == null) { // Login por requisição de get_dados
            arrayEstoque = new ArrayList<>(viewModel.getUser().getValue().getEstoque());
        } else { // Login por requisição de login
            arrayEstoque = new ArrayList<>(viewModel.getUser().getValue().getData().getEstoque());
        }
        adaptadorItemEstoque = new AdaptadorEstoqueRecyclerView(requireContext(), this, arrayEstoque);
        recyclerView.setAdapter(adaptadorItemEstoque);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        textoEstoqueVazio = view.findViewById(R.id.textoEstoqueVazio);
        if (!arrayEstoque.isEmpty()) {
            textoEstoqueVazio.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onItemClick(int position) {
        Intent intentProduto = new Intent(requireContext(), TelaProduto.class);
            if (viewModel.getUser().getValue().getData() == null) {// Login por requisição de get_dados
                intentProduto.putParcelableArrayListExtra("produto", new ArrayList<>(viewModel.getUser().getValue().getEstoque().get(position).getProdutos()));
                intentProduto.putExtra("tituloEstoque", viewModel.getUser().getValue().getEstoque().get(position).getNome());
                intentProduto.putExtra("idEstoque", viewModel.getUser().getValue().getEstoque().get(position).getId());
            } else { // Login por requisição de login
                intentProduto.putParcelableArrayListExtra("produto", new ArrayList<>(viewModel.getUser().getValue().getData().getEstoque().get(position).getProdutos()));
                intentProduto.putExtra("tituloEstoque", viewModel.getUser().getValue().getData().getEstoque().get(position).getNome());
                intentProduto.putExtra("idEstoque", viewModel.getUser().getValue().getData().getEstoque().get(position).getId());
            }
            intentProduto.putExtra("position", position);
            startActivity(intentProduto);
        }

    @Override
    public void onItemLongClick(int position) {
        dialogEditarEstoque(arrayEstoque.get(position).getId(), position);
    }

    private void criarEstoque(String nome, String descricao) {
        if (er.possuiInternet(requireContext())) {
            if (imagemBase64.isEmpty()) {
                enviarEstoque(nome, descricao, imagemBase64);
            } else {
                upload_img(imagemBase64, new CallbackImagem() {
                    @Override
                    public void onComplete(String urlImagem) {
                        if (urlImagem != null) {
                            imagemBase64 = "";
                            enviarEstoque(nome, descricao, urlImagem);
                        } else {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Falha no upload da imagem", Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                });
            }
        }  else {
            requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Verifique sua conexão com a internet.", Toast.LENGTH_SHORT).show());
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
                        Estoque responseEstoque = gson.fromJson(response, Estoque.class);
                        if (responseEstoque.getCode() == 0) {
                            requireActivity().runOnUiThread(() -> {
                                estoque.setId(responseEstoque.getId_estoque());
                                // Atualizando o objeto principal User com o novo estoque
                                if (viewModel.getUser().getValue().getData() == null) { // Login por get_dados
                                    viewModel.getUser().getValue().getEstoque().add(estoque);
                                    if (viewModel.getUser().getValue().getEstoque().isEmpty()) {
                                        textoEstoqueVazio.setVisibility(View.GONE);
                                    }
                                } else {// Login por login
                                    viewModel.getUser().getValue().getData().getEstoque().add(estoque);
                                    if (viewModel.getUser().getValue().getData().getEstoque().isEmpty()) {
                                        textoEstoqueVazio.setVisibility(View.GONE);
                                    }
                                }
                                adaptadorItemEstoque.adicionarArrayEstoque(estoque);

                                Toast.makeText(requireContext(), "Novo Stock adicionado com sucesso", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Erro ao remover o Stock", Toast.LENGTH_SHORT).show();
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
        ProgressBar imagemCarregandoCriarEstoque = dialog.findViewById(R.id.imagemCarregandoCriarEstoque);
        Button selecionarCamera = dialog.findViewById(R.id.selecionarCameraProduto);
        Button selecionarGaleria = dialog.findViewById(R.id.selecionarGaleriaProduto);
        Button cancelarCriarEstoque = dialog.findViewById(R.id.cancelarRegistroProduto);
        Button criarEstoque = dialog.findViewById(R.id.registrarProduto);

        selecionarCamera.setOnClickListener(v -> cg.pedirPermissaoCamera(new CameraGaleria.CallbackCameraGaleria() {
            @Override
            public void onImageSelected(Uri uri) {
                imagemCarregandoCriarEstoque.setVisibility(View.VISIBLE);
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    String base64 = cg.converterUriParaBase64(uri);
                    requireActivity().runOnUiThread(() -> {
                        if (!requireActivity().isDestroyed() || !requireActivity().isFinishing()) {
                            imagemBase64 = base64;

                            // Método assíncrono para exibir a imagem na tela e apagar o arquivo logo em seguida
                            Glide.with(requireActivity()).load(uri).diskCacheStrategy(DiskCacheStrategy.ALL).listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    Toast.makeText(requireContext(), "Erro ao carregar a imagem na tela", Toast.LENGTH_LONG).show();
                                    imagemCarregandoCriarEstoque.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    imagemCarregandoCriarEstoque.setVisibility(View.GONE);
                                    cg.deletarImagemUri(uri);
                                    return false;
                                }
                            }).into(imagemCriarEstoque);
                        }
                    });
                });
                executor.shutdown();
            }
        }));

        selecionarGaleria.setOnClickListener(v -> cg.pedirPermissaoGaleria(new CameraGaleria.CallbackCameraGaleria() {
            @Override
            public void onImageSelected(Uri uri) {
                imagemCarregandoCriarEstoque.setVisibility(View.VISIBLE);
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    String base64 = cg.converterUriParaBase64(uri);
                    requireActivity().runOnUiThread(() -> {
                        if (!requireActivity().isDestroyed() || !requireActivity().isFinishing()) {
                            imagemBase64 = base64;
                            Glide.with(requireContext()).load(uri).diskCacheStrategy(DiskCacheStrategy.ALL).listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    Toast.makeText(requireContext(), "Erro ao carregar a imagem na tela", Toast.LENGTH_LONG).show();
                                    imagemCarregandoCriarEstoque.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    imagemCarregandoCriarEstoque.setVisibility(View.GONE);
                                    return false;
                                }
                            }).into(imagemCriarEstoque);
                        }
                    });
                });
                executor.shutdown();
            }
        }));


        cancelarCriarEstoque.setOnClickListener(v -> dialog.dismiss());

        criarEstoque.setOnClickListener(v -> {
            EditText nomeEstoque = dialog.findViewById(R.id.inserirNomeProduto);
            EditText descricaoEstoque = dialog.findViewById(R.id.inserirDescricaoProduto);

            if (nomeEstoque.getText().toString().isEmpty() || descricaoEstoque.getText().toString().isEmpty()) {
                Toast.makeText(requireContext(), "Insira o nome e a descrição para o Stock", Toast.LENGTH_SHORT).show();
            } else {
                if (imagemCarregandoCriarEstoque.getVisibility() == View.VISIBLE) {
                    requireActivity().runOnUiThread(()->Toast.makeText(requireContext(), "Por favor, aguarde a imagem ser carregada.", Toast.LENGTH_LONG).show());
                } else {
                    criarEstoque(nomeEstoque.getText().toString(), descricaoEstoque.getText().toString());
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.CENTER);
    }

    private void dialogEditarEstoque(String idEstoque, int position) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_editar_info_estoque);

        Button botaoEditarImagemEstoque = dialog.findViewById(R.id.botaoEditarImagemEstoque);
        Button botaoApagarEstoque = dialog.findViewById(R.id.botaoApagarEstoque);

        ImageView imagemEditarEstoque = dialog.findViewById(R.id.imageViewImagemEditarEstoque);
        Button botaoSelecionarCamera = dialog.findViewById(R.id.botaoEditarInfoEstoqueCamera);
        Button botaoSelecionarGaleria = dialog.findViewById(R.id.botaoEditarInfoEstoqueGaleria);
        Button botaoEnviarImagem = dialog.findViewById(R.id.botaoEditarInfoEstoqueEnviarImagem);
        ProgressBar progressBarImagemEditarEstoque = dialog.findViewById(R.id.progressBarImagemEditarInfoEstoque);

        botaoEditarImagemEstoque.setOnClickListener(v -> {
            if (imagemEditarEstoque.getVisibility() == View.GONE) {
                imagemEditarEstoque.setVisibility(View.VISIBLE);
                botaoSelecionarCamera.setVisibility(View.VISIBLE);
                botaoSelecionarGaleria.setVisibility(View.VISIBLE);
                botaoEnviarImagem.setVisibility(View.VISIBLE);

                botaoSelecionarCamera.setOnClickListener(w -> cg.pedirPermissaoCamera(new CameraGaleria.CallbackCameraGaleria() {
                    @Override
                    public void onImageSelected(Uri uri) {
                        progressBarImagemEditarEstoque.setVisibility(View.VISIBLE);
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        executor.execute(() -> {
                            String base64 = cg.converterUriParaBase64(uri);
                            requireActivity().runOnUiThread(() -> {
                                if (!requireActivity().isDestroyed() || !requireActivity().isFinishing()) {
                                    imagemBase64 = base64;

                                    // Método assíncrono para exibir a imagem na tela e apagar o arquivo logo em seguida
                                    Glide.with(requireActivity()).load(uri).diskCacheStrategy(DiskCacheStrategy.ALL).listener(new RequestListener<Drawable>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                            requireActivity().runOnUiThread(()->Toast.makeText(requireContext(), "Erro ao carregar a imagem na tela", Toast.LENGTH_LONG).show());
                                            progressBarImagemEditarEstoque.setVisibility(View.GONE);
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                            progressBarImagemEditarEstoque.setVisibility(View.GONE);
                                            cg.deletarImagemUri(uri);
                                            return false;
                                        }
                                    }).into(imagemEditarEstoque);
                                }
                            });
                        });
                        executor.shutdown();
                    }
                }));

                botaoSelecionarGaleria.setOnClickListener(w -> cg.pedirPermissaoGaleria(new CameraGaleria.CallbackCameraGaleria() {
                    @Override
                    public void onImageSelected(Uri uri) {
                        progressBarImagemEditarEstoque.setVisibility(View.VISIBLE);
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        executor.execute(() -> {
                            String base64 = cg.converterUriParaBase64(uri);
                            requireActivity().runOnUiThread(() -> {
                                if (!requireActivity().isDestroyed() || !requireActivity().isFinishing()) {
                                    imagemBase64 = base64;
                                    Glide.with(requireContext()).load(uri).diskCacheStrategy(DiskCacheStrategy.ALL).listener(new RequestListener<Drawable>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                            requireActivity().runOnUiThread(()->Toast.makeText(requireContext(), "Erro ao carregar a imagem na tela", Toast.LENGTH_LONG).show());
                                            progressBarImagemEditarEstoque.setVisibility(View.GONE);
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                            progressBarImagemEditarEstoque.setVisibility(View.GONE);
                                            return false;
                                        }
                                    }).into(imagemEditarEstoque);
                                }
                            });
                        });
                        executor.shutdown();
                    }
                }));

                botaoEnviarImagem.setOnClickListener(x -> {
                    if (progressBarImagemEditarEstoque.getVisibility() == View.VISIBLE) {
                        requireActivity().runOnUiThread(()->Toast.makeText(requireContext(), "Por favor, aguarde a imagem ser carregada.", Toast.LENGTH_LONG).show());
                    } else {
                        uploadImgChargeEstoque(idEstoque, position);
                        dialog.dismiss();
                    }
                });
            } else {
                imagemEditarEstoque.setVisibility(View.GONE);
                botaoSelecionarCamera.setVisibility(View.GONE);
                botaoSelecionarGaleria.setVisibility(View.GONE);
                botaoEnviarImagem.setVisibility(View.GONE);
            }
        });

        botaoApagarEstoque.setOnClickListener(v -> {
            popupConfirmarExclusao(new ConfirmacaoApagarEstoque() {
                @Override
                public void onConfirmacao(boolean confirmou) {
                    if (confirmou) {
                        apagar_estoque(idEstoque, position);
                        dialog.dismiss();
                    }
                }
            });
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.FadeInOutMiddle;
        dialog.getWindow().setGravity(Gravity.CENTER);
    }

    public void popupConfirmarExclusao(ConfirmacaoApagarEstoque confirmacao) {
        AlertDialog dialog = new AlertDialog.Builder(requireActivity())
                .setTitle("Confirmar exclusão")
                .setMessage("Tem certeza que excluir este Stock?")
                .setPositiveButton("Confirmar", (dialogConfirmar, which) -> {
                    confirmacao.onConfirmacao(true);
                })
                .setNegativeButton("Cancelar", (dialogCancelar, which) -> {
                    confirmacao.onConfirmacao(false);
                    dialogCancelar.dismiss();
                })
                .create();

        // Altera a cor do botão exibido
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            positiveButton.setTextColor(ContextCompat.getColor(requireActivity().getApplicationContext(), R.color.modern_red));
            negativeButton.setTextColor(ContextCompat.getColor(requireActivity().getApplicationContext(), R.color.green2));
        });

        dialog.show();
    }

    private void apagar_estoque(String idEstoque, int position) {
        if (er.possuiInternet(requireContext())) {
            Estoque estoque = new Estoque();
            estoque.setId(er.obterMemoriaInterna("idConexao"));
            estoque.setId_estoque(idEstoque);

            // Converter o objeto User para JSON
            String userJson = gson.toJson(estoque);

            er.post("apagar_estoque", userJson, response -> {
                if (response.startsWith("Erro")) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), response, Toast.LENGTH_LONG).show()
                    );
                } else {
                    try {
                        // Processar resposta da requisição
                        Estoque responseUpload = gson.fromJson(response, Estoque.class);
                        if (responseUpload.getCode() == 0) {
                            requireActivity().runOnUiThread(()-> {
                                if (viewModel.getUser().getValue().getData() == null) { // Login por get_dados
                                    viewModel.getUser().getValue().getEstoque().remove(position);
                                    if (viewModel.getUser().getValue().getEstoque().isEmpty()) {
                                        textoEstoqueVazio.setVisibility(View.VISIBLE);
                                    }
                                } else { // Login por login
                                    viewModel.getUser().getValue().getData().getEstoque().remove(position);
                                    if (viewModel.getUser().getValue().getData().getEstoque().isEmpty()) {
                                        textoEstoqueVazio.setVisibility(View.VISIBLE);
                                    }
                                }
                                adaptadorItemEstoque.removerEstoque(position);
                            });
                        }
                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Erro ao processar a resposta", Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            });
        } else {
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "Verifique sua conexão com a internet.", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void uploadImgChargeEstoque(String idEstoque, int position) {
        if (er.possuiInternet(requireContext())) {
            if (imagemBase64.isEmpty()) {
                charge_estoque_url(idEstoque, imagemBase64, position);
            } else {
                upload_img(imagemBase64, new CallbackResponseUpload() {
                    @Override
                    public void onCompleteUploadImg(String urlImagem) {
                        if (urlImagem != null) {
                            imagemBase64 = "";
                            charge_estoque_url(idEstoque, urlImagem, position);
                        } else {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Falha no upload de imagem", Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                });
            }
        } else {
            requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Verifique sua conexão com a internet.", Toast.LENGTH_SHORT).show());
        }
    }

    private void charge_estoque_url(String idEstoque, String urlImagem, int position) {
        if (er.possuiInternet(requireContext())) {

            Estoque estoque = new Estoque();
            estoque.setId(er.obterMemoriaInterna("idConexao"));
            estoque.setId_estoque(idEstoque);
            estoque.setUrl_foto(urlImagem);

            // Converter o objeto User para JSON
            String editarImagemEstoque = gson.toJson(estoque);

            // Fazer a requisição
            er.post("charge_estoque_url", editarImagemEstoque, response -> {
                if (response.startsWith("Erro")) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), response, Toast.LENGTH_LONG).show());
                } else {
                    try {
                        // Processar resposta da requisição
                        Estoque responseEstoque = gson.fromJson(response, Estoque.class);
                        if (responseEstoque.getCode() == 0) {
                            requireActivity().runOnUiThread(() -> {
                                if (viewModel.getUser().getValue().getData() == null) { // Login por get_dados
                                    viewModel.getUser().getValue().getEstoque().get(position).setImagem(urlImagem);
                                } else { // Login por login
                                    viewModel.getUser().getValue().getData().getEstoque().get(position).setImagem(urlImagem);
                                }
                                adaptadorItemEstoque.alterarImagemEstoque(position, urlImagem);

                            });
                        }
                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Erro ao alterar a imagem do Stock", Toast.LENGTH_SHORT).show());
                    }
                }
            });
        } else {
            requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Verifique sua conexão com a internet.", Toast.LENGTH_SHORT).show());
        }
    }

    private void upload_img(String imagemBase64, CallbackResponseUpload callback) {
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
                    callback.onCompleteUploadImg(null); // Erro no upload
                } else if (response.startsWith("<html>")) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "O arquivo de imagem é muito grande", Toast.LENGTH_LONG).show()
                    );
                    callback.onCompleteUploadImg(null); // Arquivo muito grande
                } else {
                    try {
                        // Processar resposta da requisição
                        User responseUpload = gson.fromJson(response, User.class);
                        if (responseUpload.getCode() == 0) {
                            callback.onCompleteUploadImg(responseUpload.getUrl()); // Sucesso
                        } else {
                            callback.onCompleteUploadImg(null);
                        }
                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Erro ao processar o upload da imagem", Toast.LENGTH_SHORT).show()
                        );
                        callback.onCompleteUploadImg(null);
                    }
                }
            });
        } else {
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "Verifique sua conexão com a internet.", Toast.LENGTH_SHORT).show()
            );
            callback.onCompleteUploadImg(null);
        }
    }
}