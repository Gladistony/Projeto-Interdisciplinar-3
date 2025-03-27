package com.teste.projeto_3;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import com.teste.projeto_3.model.ResultadoRequisicaoEstoque;
import com.teste.projeto_3.model.User;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FragStock extends Fragment implements RecyclerViewInterface {
    public AdaptadorEstoqueRecyclerView adaptadorItemEstoque;

    public AdaptadorResultadoEstoqueRecyclerView adaptadorResultadoEstoqueRecyclerView;
    public static SharedViewModel viewModel;
    private EnviarRequisicao er;
    private CameraGaleria cg;
    private final Gson gson = new Gson();
    private String imagemBase64 = "";
    private SwipeRefreshLayout swipeRefreshLayoutEstoque;
    private TextView textoEstoqueVazio;

    public FloatingActionButton botaoCriarEstoque;

    private ConstraintLayout backgroundEnviandoEstoque;

    ProgressBar progressBarEnviandoEstoque;
    ImageView imagemSucesso;
    ImageView imagemFalha;

    ImageView imagemAviso;

    TextView textoEnviandoRequisicao;

    int quantidadeRequisicoesEnviando = 0;
    int quantidadeRequisicoesSucesso = 0;
    int quantidadeRequisicoesFalha = 0;

    ImageView iconeExpandirEnviandoRequisicaoEstoque;

    ImageView iconeFecharEnviandoRequisicaoEstoque;

    RecyclerView recyclerViewResultadoRequisicaoEnviandoEstoque;

    ConstraintLayout layoutElementosRequisicaoEstoque;

    float alturaEmPixels;

    float alturaEmPixelsEnviandoRequisicao;

    boolean detalheEnviarRequisicaoExibido = false;

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

        botaoCriarEstoque = view.findViewById(R.id.botaoCriarEstoque);
        botaoCriarEstoque.setOnClickListener(v -> dialogCriarEstoque());

        swipeRefreshLayoutEstoque = view.findViewById(R.id.swipeRefreshLayoutEstoque);
        swipeRefreshLayoutEstoque.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                get_estoque();
            }
        });

        progressBarEnviandoEstoque = view.findViewById(R.id.progressBarEnviandoEstoque);
        imagemSucesso = view.findViewById(R.id.imagemEnviandoEstoqueSucesso);
        imagemFalha = view.findViewById(R.id.imagemEnviandoEstoqueFalha);
        imagemAviso = view.findViewById(R.id.imagemEnviandoEstoqueAviso);
        textoEnviandoRequisicao = view.findViewById(R.id.textoEnviandoEstoque);

        // Abaixo, 40 é o tamanho vertical em dp do ConstraintLayout (que é 40) para exibir resultado da requisição. Se mudar lá, mudar aqui também.
        alturaEmPixels = getResources().getDisplayMetrics().density * 40;

        // Abaixo, 140 é o tamanho vertical em dp do RecyclerView (que é 140) + ConstraintLayout (que é 40) para exibir resultado da requisição. Se mudar lá, mudar aqui também.
        alturaEmPixelsEnviandoRequisicao = getResources().getDisplayMetrics().density * 180;

        backgroundEnviandoEstoque = view.findViewById(R.id.frameLayoutEnviandoEstoque);

        iconeExpandirEnviandoRequisicaoEstoque = view.findViewById(R.id.iconeExpandirEnviandoRequisicaoEstoque);
        iconeFecharEnviandoRequisicaoEstoque = view.findViewById(R.id.iconeFecharEnviadoRequisicaoEstoque);
        layoutElementosRequisicaoEstoque = view.findViewById(R.id.layoutElementosRequisicaoEstoque);
        recyclerViewResultadoRequisicaoEnviandoEstoque = view.findViewById(R.id.recyclerViewResultadoRequisicaoEstoque);

        layoutElementosRequisicaoEstoque.setOnClickListener(v-> toggleDetalheProcessoRequisicao());

        iconeFecharEnviandoRequisicaoEstoque.setOnClickListener(v-> fecharProcessoRequisicao());

        textoEstoqueVazio = view.findViewById(R.id.textoEstoqueVazio);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewEstoque);
        if (viewModel.getUser().getValue().getData() == null) { // Login por requisição de get_dados
            adaptadorItemEstoque = new AdaptadorEstoqueRecyclerView(requireContext(), this, viewModel.getUser().getValue().getEstoque());
            if (!viewModel.getUser().getValue().getEstoque().isEmpty()) {
                textoEstoqueVazio.setVisibility(View.GONE);
            }
        } else { // Login por requisição de login
            adaptadorItemEstoque = new AdaptadorEstoqueRecyclerView(requireContext(), this, viewModel.getUser().getValue().getData().getEstoque());
            if (!viewModel.getUser().getValue().getData().getEstoque().isEmpty()) {
                textoEstoqueVazio.setVisibility(View.GONE);
            }
        }
        recyclerView.setAdapter(adaptadorItemEstoque);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        RecyclerView recyclerViewEnviandoEstoque = view.findViewById(R.id.recyclerViewResultadoRequisicaoEstoque);
        adaptadorResultadoEstoqueRecyclerView = new AdaptadorResultadoEstoqueRecyclerView(requireContext(), this, recyclerViewEnviandoEstoque);
        recyclerViewEnviandoEstoque.setAdapter(adaptadorResultadoEstoqueRecyclerView);

        // Criando um layoutManager que adiciona itens no topo da lista das requisições ao invés do fim
        LinearLayoutManager layoutManagerEnviandoEstoque = new LinearLayoutManager(requireContext());
        layoutManagerEnviandoEstoque.setStackFromEnd(true);
        layoutManagerEnviandoEstoque.setReverseLayout(true);
        recyclerViewEnviandoEstoque.setLayoutManager(layoutManagerEnviandoEstoque);

        return view;
    }

    private void mostrarProcessoRequisicao() {
        // Atualiza o título para "Enviando requisição"
        atualizarTituloRequisicao();

        // Executa a animação apenas se não está exibido na tela ou se está saindo ou entrando na tela
        if (backgroundEnviandoEstoque.getTranslationY() > -alturaEmPixels) {

            ObjectAnimator animLayout = ObjectAnimator.ofFloat(backgroundEnviandoEstoque, "translationY", 0f, -alturaEmPixels);
            ObjectAnimator animBotao = ObjectAnimator.ofFloat(botaoCriarEstoque, "translationY", 0f, -alturaEmPixels);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(animLayout, animBotao);
            animatorSet.setDuration(200);
            animatorSet.start();
        }
    }

    // Atualiza o título do resultado das requisições
    private void atualizarTituloRequisicao() {
        // Verifica se há requisições sendo enviadas no momento
        if (quantidadeRequisicoesEnviando > 0) {
            progressBarEnviandoEstoque.setVisibility(View.VISIBLE);
            imagemSucesso.setVisibility(View.GONE);
            imagemFalha.setVisibility(View.GONE);
            imagemAviso.setVisibility(View.GONE);
            backgroundEnviandoEstoque.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gray));
            textoEnviandoRequisicao.setText("Processando " + quantidadeRequisicoesEnviando + " requisições...");
        }
        else {
            // Não há requisições sendo enviadas e exibe o resultado das requisições
            progressBarEnviandoEstoque.setVisibility(View.GONE);
            textoEnviandoRequisicao.setText(quantidadeRequisicoesSucesso + " sucesso(s), " + quantidadeRequisicoesFalha + " falha(s)");

            // Verifica se há requisições com sucesso e com falha
            if (quantidadeRequisicoesFalha > 0 && quantidadeRequisicoesSucesso > 0) {
                imagemAviso.setVisibility(View.VISIBLE);
                imagemFalha.setVisibility(View.GONE);
                imagemSucesso.setVisibility(View.GONE);
                backgroundEnviandoEstoque.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.aviso));
            }
            // Verifica se há apenas resultados das requisições com falhas
             else if (quantidadeRequisicoesFalha > 0 && quantidadeRequisicoesSucesso == 0) {
                imagemAviso.setVisibility(View.GONE);
                imagemFalha.setVisibility(View.VISIBLE);
                imagemSucesso.setVisibility(View.GONE);
                backgroundEnviandoEstoque.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.falha));
            } else {
                // Todos os resultados das requisições são com sucesso
                imagemAviso.setVisibility(View.GONE);
                imagemFalha.setVisibility(View.GONE);
                imagemSucesso.setVisibility(View.VISIBLE);
                backgroundEnviandoEstoque.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.sucesso));
            }
        }
    }

    // Remove a lista de requisições da tela
    private void fecharProcessoRequisicao() {
        detalheEnviarRequisicaoExibido = false;
        backgroundEnviandoEstoque.setTranslationY(0);
        recyclerViewResultadoRequisicaoEnviandoEstoque.setTranslationY(0);
        botaoCriarEstoque.setTranslationY(0);
    }

    private void toggleDetalheProcessoRequisicao() {
        ObjectAnimator animLayout;
        ObjectAnimator animRecyclerView;
        ObjectAnimator animRotacao;
        if (detalheEnviarRequisicaoExibido) {
            animLayout = ObjectAnimator.ofFloat(backgroundEnviandoEstoque, "translationY", -alturaEmPixelsEnviandoRequisicao, -alturaEmPixels);
            animRecyclerView = ObjectAnimator.ofFloat(recyclerViewResultadoRequisicaoEnviandoEstoque, "translationY", -alturaEmPixelsEnviandoRequisicao, -alturaEmPixels);
            animRotacao = ObjectAnimator.ofFloat(iconeExpandirEnviandoRequisicaoEstoque, "rotation", 270f, 90f);
            detalheEnviarRequisicaoExibido = false;
        } else {
            animLayout = ObjectAnimator.ofFloat(backgroundEnviandoEstoque, "translationY", -alturaEmPixels, -alturaEmPixelsEnviandoRequisicao);
            animRecyclerView = ObjectAnimator.ofFloat(recyclerViewResultadoRequisicaoEnviandoEstoque, "translationY", -alturaEmPixels, -alturaEmPixelsEnviandoRequisicao);
            animRotacao = ObjectAnimator.ofFloat(iconeExpandirEnviandoRequisicaoEstoque, "rotation", 90f, 270f);
            detalheEnviarRequisicaoExibido = true;
        }

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animLayout, animRecyclerView, animRotacao);
        animatorSet.setDuration(200);
        animatorSet.start();
    }

    // Atualiza o conteúdo dentro do recyclerview de lista de requisições
    private void sucessoCriarEstoque(int indiceRequisicaoEnviando) {
        quantidadeRequisicoesEnviando--;
        quantidadeRequisicoesSucesso++;
        ResultadoRequisicaoEstoque resultadoAtual = adaptadorResultadoEstoqueRecyclerView.getResultado(indiceRequisicaoEnviando);
        adaptadorResultadoEstoqueRecyclerView.alterarRequisicaoEstoque("Criado Stock " + resultadoAtual.getNomeEstoque(), 0, indiceRequisicaoEnviando);
    }

    // Atualiza o conteúdo dentro do recyclerview de lista de requisições
    private void falhaCriarEstoque(int indiceRequisicaoEnviando) {
        quantidadeRequisicoesEnviando--;
        quantidadeRequisicoesFalha++;
        ResultadoRequisicaoEstoque resultadoAtual = adaptadorResultadoEstoqueRecyclerView.getResultado(indiceRequisicaoEnviando);
        adaptadorResultadoEstoqueRecyclerView.alterarRequisicaoEstoque("Falha ao criar o Stock " + resultadoAtual.getNomeEstoque(), 1, indiceRequisicaoEnviando);
    }

    @Override
    public void onItemClick(int position) {
        Intent intentProduto = new Intent(requireContext(), TelaProduto.class);
            if (viewModel.getUser().getValue().getData() == null) {// Login por requisição de get_dados
                intentProduto.putExtra("tituloEstoque", viewModel.getUser().getValue().getEstoque().get(position).getNome());
                intentProduto.putExtra("idEstoque", viewModel.getUser().getValue().getEstoque().get(position).getId());
            } else { // Login por requisição de login
                intentProduto.putExtra("tituloEstoque", viewModel.getUser().getValue().getData().getEstoque().get(position).getNome());
                intentProduto.putExtra("idEstoque", viewModel.getUser().getValue().getData().getEstoque().get(position).getId());
            }
            intentProduto.putExtra("position", position);
            startActivity(intentProduto);
        }

    @Override
    public void onItemLongClick(int position) {
        if (viewModel.getUser().getValue().getData() == null) {
            dialogEditarEstoque(viewModel.getUser().getValue().getEstoque().get(position).getId(), position);
        } else {
            dialogEditarEstoque(viewModel.getUser().getValue().getData().getEstoque().get(position).getId(), position);
        }
    }

    private void criarEstoque(String nome, String descricao) {
        if (er.possuiInternet(requireContext())) {
            // Criando o elemento para exibir na lista de requisições
            quantidadeRequisicoesEnviando++;
            ResultadoRequisicaoEstoque res = new ResultadoRequisicaoEstoque();
            res.setIconeResultado(2);
            res.setTextoResultado("Criando " + nome + "...");
            res.setNomeEstoque(nome);
            adaptadorResultadoEstoqueRecyclerView.adicionarRequisicaoEstoque(res);
            int indiceEnviandoAtual = adaptadorResultadoEstoqueRecyclerView.getItemCount() - 1;
            mostrarProcessoRequisicao();

            if (imagemBase64.isEmpty()) {
                enviarEstoque(nome, descricao, imagemBase64, indiceEnviandoAtual);
            } else {
                upload_img(imagemBase64, new CallbackImagem() {
                    @Override
                    public void onComplete(String urlImagem) {
                        if (urlImagem != null) {
                            enviarEstoque(nome, descricao, urlImagem, indiceEnviandoAtual);
                        } else {
                            requireActivity().runOnUiThread(() -> {
                                falhaCriarEstoque(indiceEnviandoAtual);
                                mostrarProcessoRequisicao();
                                Toast.makeText(requireContext(), "Falha no upload da imagem", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                });
            }
        }  else {
            requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Verifique sua conexão com a internet.", Toast.LENGTH_SHORT).show());
        }
    }

    private void enviarEstoque(String nome, String descricao, String urlImagem, int indiceRequisicaoEnviando) {
        if (er.possuiInternet(requireContext())) {
            // Criando o objeto User
            Estoque estoque = new Estoque();
            estoque.setId(er.obterMemoriaInterna("idConexao"));
            estoque.setNome(nome);
            estoque.setDescricao(descricao);
            estoque.setImagem(urlImagem);

            imagemBase64 = "";

            // Converter o objeto User para JSON
            String userJson = gson.toJson(estoque);

            // Fazer a requisição
            er.post("criar_estoque", userJson, response -> {
                if (response.startsWith("Erro")) {
                    requireActivity().runOnUiThread(() -> {
                        falhaCriarEstoque(indiceRequisicaoEnviando);
                        mostrarProcessoRequisicao();
                        Toast.makeText(requireContext(), response, Toast.LENGTH_LONG).show();
                    });
                } else {
                    try {
                        // Processar resposta da requisição
                        Estoque responseEstoque = gson.fromJson(response, Estoque.class);
                        if (responseEstoque.getCode() == 0) {
                            requireActivity().runOnUiThread(() -> {
                                estoque.setId(responseEstoque.getId_estoque());
                                adaptadorItemEstoque.adicionarArrayEstoque(estoque);
                                sucessoCriarEstoque(indiceRequisicaoEnviando);
                                mostrarProcessoRequisicao();

                                // Atualizando o objeto principal User com o novo estoque
                                if (viewModel.getUser().getValue().getData() == null) { // Login por get_dados
                                    if (!viewModel.getUser().getValue().getEstoque().isEmpty()) {
                                        textoEstoqueVazio.setVisibility(View.GONE);
                                    }
                                } else {// Login por login
                                    if (!viewModel.getUser().getValue().getData().getEstoque().isEmpty()) {
                                        textoEstoqueVazio.setVisibility(View.GONE);
                                    }
                                }
                            });
                        } else {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Erro ao criar o Stock", Toast.LENGTH_SHORT).show();
                                falhaCriarEstoque(indiceRequisicaoEnviando);
                                mostrarProcessoRequisicao();
                            });
                        }
                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Erro ao processar a resposta. Tente novamente.", Toast.LENGTH_SHORT).show();
                            falhaCriarEstoque(indiceRequisicaoEnviando);
                            mostrarProcessoRequisicao();
                        });
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
                            Glide.with(requireActivity()).load(uri).diskCacheStrategy(DiskCacheStrategy.ALL).listener(new RequestListener<>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                                    Toast.makeText(requireContext(), "Erro ao carregar a imagem na tela", Toast.LENGTH_LONG).show();
                                    imagemCarregandoCriarEstoque.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
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
                            Glide.with(requireContext()).load(uri).diskCacheStrategy(DiskCacheStrategy.ALL).listener(new RequestListener<>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                                    Toast.makeText(requireContext(), "Erro ao carregar a imagem na tela", Toast.LENGTH_LONG).show();
                                    imagemCarregandoCriarEstoque.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
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
        TextView textoRemoverImagemEstoque = dialog.findViewById(R.id.textoRemoverImagemEstoque);
        Button botaoSelecionarCamera = dialog.findViewById(R.id.botaoEditarInfoEstoqueCamera);
        Button botaoSelecionarGaleria = dialog.findViewById(R.id.botaoEditarInfoEstoqueGaleria);
        Button botaoEnviarImagem = dialog.findViewById(R.id.botaoEditarInfoEstoqueEnviarImagem);
        ProgressBar progressBarImagemEditarEstoque = dialog.findViewById(R.id.progressBarImagemEditarInfoEstoque);

        botaoEditarImagemEstoque.setOnClickListener(v -> {
            if (imagemEditarEstoque.getVisibility() == View.GONE) {
                textoRemoverImagemEstoque.setVisibility(View.VISIBLE);
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
                                    Glide.with(requireActivity()).load(uri).diskCacheStrategy(DiskCacheStrategy.ALL).listener(new RequestListener<>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                                            requireActivity().runOnUiThread(()->Toast.makeText(requireContext(), "Erro ao carregar a imagem na tela", Toast.LENGTH_LONG).show());
                                            progressBarImagemEditarEstoque.setVisibility(View.GONE);
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
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
                                    Glide.with(requireContext()).load(uri).diskCacheStrategy(DiskCacheStrategy.ALL).listener(new RequestListener<>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                                            requireActivity().runOnUiThread(()->Toast.makeText(requireContext(), "Erro ao carregar a imagem na tela", Toast.LENGTH_LONG).show());
                                            progressBarImagemEditarEstoque.setVisibility(View.GONE);
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
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
                textoRemoverImagemEstoque.setVisibility(View.GONE);
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
                                adaptadorItemEstoque.removerEstoque(position);
                                if (viewModel.getUser().getValue().getData() == null) { // Login por get_dados
                                    if (viewModel.getUser().getValue().getEstoque().isEmpty()) {
                                        textoEstoqueVazio.setVisibility(View.VISIBLE);
                                    }
                                } else { // Login por login
                                    if (viewModel.getUser().getValue().getData().getEstoque().isEmpty()) {
                                        textoEstoqueVazio.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        } else if (responseUpload.getCode() == 26) {
                            requireActivity().runOnUiThread(() -> popupAvisarEstoqueApagado());
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
                                adaptadorItemEstoque.alterarImagemEstoque(position, urlImagem);
                            });
                        } else if (responseEstoque.getCode() == 26) {
                            requireActivity().runOnUiThread(() -> popupAvisarEstoqueApagado());
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

    public void popupAvisarEstoqueApagado() {
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Aviso")
                .setMessage("Não foi possível concluir a operação, pois este Stock foi apagado através de outro dispositivo. Por favor, atualize sua lista de Stocks.")
                .setPositiveButton("OK", (dialogConfirmar, which) -> {
                })
                .setCancelable(false)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.green2));
            positiveButton.setOnClickListener(c -> dialog.dismiss());
        });

        dialog.show();
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

    private void get_estoque() {
        if (er.possuiInternet(requireContext())) {

            Estoque estoque = new Estoque();
            estoque.setId(er.obterMemoriaInterna("idConexao"));

            // Converter o objeto User para JSON
            String jsonGetEstoque = gson.toJson(estoque);

            // Fazer a requisição
            er.post("get_estoque", jsonGetEstoque, response -> {
                if (response.startsWith("Erro")) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), response, Toast.LENGTH_LONG).show();
                        swipeRefreshLayoutEstoque.setRefreshing(false);
                    });
                } else {
                    try {
                        // Processar resposta da requisição
                        User responseEstoque = gson.fromJson(response, User.class);
                        if (responseEstoque.getCode() == 0) {
                            requireActivity().runOnUiThread(() -> {
                                if (viewModel.getUser().getValue().getData() == null) { // Login por get_dados
                                    viewModel.getUser().getValue().setEstoque(responseEstoque.getEstoque());
                                } else { // Login por login
                                    viewModel.getUser().getValue().getData().setEstoque(responseEstoque.getEstoque());
                                }
                                if (responseEstoque.getEstoque().isEmpty()) {
                                    textoEstoqueVazio.setVisibility(View.VISIBLE);
                                } else {
                                    textoEstoqueVazio.setVisibility(View.GONE);
                                }
                                adaptadorItemEstoque.atualizarEstoque(responseEstoque.getEstoque());
                                swipeRefreshLayoutEstoque.setRefreshing(false);
                            });
                        }
                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Erro ao alterar a imagem do Stock", Toast.LENGTH_SHORT).show();
                            swipeRefreshLayoutEstoque.setRefreshing(false);
                        });
                    }
                }
            });
        } else {
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Verifique sua conexão com a internet.", Toast.LENGTH_SHORT).show();
                swipeRefreshLayoutEstoque.setRefreshing(false);
            });
        }
    }
}