package com.teste.projeto_3;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import com.teste.projeto_3.model.EstoqueDataValidadeString;
import com.teste.projeto_3.model.Produto;
import com.teste.projeto_3.model.ResultadoRequisicaoEstoque;
import com.teste.projeto_3.model.User;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TelaProduto extends AppCompatActivity implements RecyclerViewInterface, RecyclerViewResultadoRequisicaoInterface {

    private interface CallbackResponseUpload {
        void onCompleteUploadImg(String urlImagem);
    }

    private interface CallbackResponseProduto{
        void onCompleteRegistroProduto(Produto produto);
    }

    private AdaptadorProdutoRecyclerView adaptadorItemProduto;

    public AdaptadorResultadoEstoqueRecyclerView adaptadorResultadoProdutoRecyclerView;
    DecimalFormat decimalFormat;
    EnviarRequisicao er;
    CameraGaleria cg;
    String imagemBase64 = "";
    private final Gson gson = new Gson();
    NumberFormat formatadorPontoVirgula = NumberFormat.getInstance(new Locale("pt", "BR"));

    int posicaoEstoque;
    TextView textoProdutoVazio;

    private SwipeRefreshLayout swipeRefreshLayoutProduto;

    private FloatingActionButton botaoDialogRegistrarProduto;

    private ConstraintLayout backgroundEnviandoProduto;

    ProgressBar progressBarEnviandoProduto;
    ImageView imagemSucesso;
    ImageView imagemFalha;
    ImageView imagemAviso;
    TextView textoEnviandoRequisicao;
    int quantidadeRequisicoesEnviando = 0;
    int quantidadeRequisicoesSucesso = 0;
    int quantidadeRequisicoesFalha = 0;
    ImageView iconeExpandirEnviandoRequisicaoProduto;
    ImageView iconeFecharEnviandoRequisicaoProduto;
    RecyclerView recyclerViewResultadoRequisicaoEnviandoProduto;
    ConstraintLayout layoutElementosRequisicaoProduto;
    float alturaEmPixels;
    float alturaEmPixelsEnviandoRequisicao;
    boolean detalheEnviarRequisicaoExibido = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_produto);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        er = new EnviarRequisicao(this);
        cg = new CameraGaleria(this, getActivityResultRegistry(),this);
        decimalFormat = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(new Locale("pt", "BR")));

        textoProdutoVazio = findViewById(R.id.textoProdutoVazio);

        posicaoEstoque = getIntent().getIntExtra("position", -1);

        TextView tituloEstoque = findViewById(R.id.tituloEstoque);
        tituloEstoque.setText(getIntent().getStringExtra("tituloEstoque"));

        botaoDialogRegistrarProduto = findViewById(R.id.botaoDialogRegistrarProduto);
        botaoDialogRegistrarProduto.setOnClickListener(v -> {
            dialogRegistrarProduto();
        });

        Button botaoFecharTelaProduto = findViewById(R.id.botaoVoltarTelaProduto);
        botaoFecharTelaProduto.setOnClickListener(v -> finish());

        swipeRefreshLayoutProduto = findViewById(R.id.swipeRefreshLayoutProduto);
        swipeRefreshLayoutProduto.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                get_estoque();
            }
        });


        RecyclerView recyclerView = findViewById(R.id.recyclerViewProduto);
        if (FragStock.viewModel.getUser().getValue().getData() == null) {
            adaptadorItemProduto = new AdaptadorProdutoRecyclerView(this, FragStock.viewModel.getUser().getValue().getEstoque().get(posicaoEstoque).getProdutos(), this);
            if (!FragStock.viewModel.getUser().getValue().getEstoque().get(posicaoEstoque).getProdutos().isEmpty()) {
                textoProdutoVazio.setVisibility(View.GONE);
            }
        } else {
            adaptadorItemProduto = new AdaptadorProdutoRecyclerView(this, FragStock.viewModel.getUser().getValue().getData().getEstoque().get(posicaoEstoque).getProdutos(), this);
            if (!FragStock.viewModel.getUser().getValue().getData().getEstoque().get(posicaoEstoque).getProdutos().isEmpty()) {
                textoProdutoVazio.setVisibility(View.GONE);
            }
        }
        recyclerView.setAdapter(adaptadorItemProduto);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Abaixo, 40 é o tamanho vertical em dp do ConstraintLayout (que é 40) para exibir resultado da requisição. Se mudar lá, mudar aqui também.
        alturaEmPixels = getResources().getDisplayMetrics().density * 40;

        // Abaixo, 140 é o tamanho vertical em dp do RecyclerView (que é 140) + ConstraintLayout (que é 40) para exibir resultado da requisição. Se mudar lá, mudar aqui também.
        alturaEmPixelsEnviandoRequisicao = getResources().getDisplayMetrics().density * 180;

        textoEnviandoRequisicao = findViewById(R.id.textoEnviandoProduto);
        imagemSucesso = findViewById(R.id.imagemEnviandoProdutoSucesso);
        imagemAviso = findViewById(R.id.imagemEnviandoProdutoAviso);
        imagemFalha = findViewById(R.id.imagemEnviandoProdutoFalha);

        backgroundEnviandoProduto = findViewById(R.id.frameLayoutEnviandoProduto);
        progressBarEnviandoProduto = findViewById(R.id.progressBarEnviandoProduto);

        iconeExpandirEnviandoRequisicaoProduto = findViewById(R.id.iconeExpandirEnviandoRequisicaoProduto);
        iconeFecharEnviandoRequisicaoProduto = findViewById(R.id.iconeFecharEnviadoRequisicaoProduto);
        layoutElementosRequisicaoProduto = findViewById(R.id.layoutElementosRequisicaoProduto);
        recyclerViewResultadoRequisicaoEnviandoProduto = findViewById(R.id.recyclerViewResultadoRequisicaoProduto);

        layoutElementosRequisicaoProduto.setOnClickListener(v-> toggleDetalheProcessoRequisicao());

        iconeFecharEnviandoRequisicaoProduto.setOnClickListener(v-> fecharProcessoRequisicao());

        RecyclerView recyclerViewEnviandoProduto = findViewById(R.id.recyclerViewResultadoRequisicaoProduto);
        adaptadorResultadoProdutoRecyclerView = new AdaptadorResultadoEstoqueRecyclerView(this, this, recyclerViewEnviandoProduto);
        recyclerViewEnviandoProduto.setAdapter(adaptadorResultadoProdutoRecyclerView);

        // Criando um layoutManager que adiciona itens no topo da lista das requisições ao invés do fim
        LinearLayoutManager layoutManagerEnviandoProduto = new LinearLayoutManager(this);
        layoutManagerEnviandoProduto.setStackFromEnd(true);
        layoutManagerEnviandoProduto.setReverseLayout(true);
        recyclerViewEnviandoProduto.setLayoutManager(layoutManagerEnviandoProduto);

    }

    @Override
    public void onItemClick(int position) {
    }

    @Override
    public void onItemLongClick(int position) {
        dialogEditarProduto(position);
    }

    @Override
    public void onItemClickResultadoRequisicao(int position) {
        if (adaptadorResultadoProdutoRecyclerView.getResultado(position).getIconeResultado() == 2) {
            runOnUiThread(() -> Toast.makeText(this, "Por favor, aguarde a requisição ser concluída", Toast.LENGTH_SHORT).show());
        } else {
            dialogResultadoRequisicao(position);
        }
    }

    private void dialogEditarProduto(int position) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_editar_info_produto);

        int quantidadeAtual;
        if (FragStock.viewModel.getUser().getValue().getData() == null) {
            quantidadeAtual = FragStock.viewModel.getUser().getValue().getEstoque().get(posicaoEstoque)
                    .getProdutos().get(position).getQuantidade();
        } else {
            quantidadeAtual = FragStock.viewModel.getUser().getValue().getData()
                    .getEstoque().get(posicaoEstoque).getProdutos().get(position).getQuantidade();
        }

        Button botaoEditarQuantidadeProduto = dialog.findViewById(R.id.botaoEditarQuantidadeProduto);
        botaoEditarQuantidadeProduto.setOnClickListener(v-> {
            EditText editarTextoNovaQuantidade = dialog.findViewById(R.id.editTextNovaQuantidade);
            Button botaoEnviarNovaQuantidade = dialog.findViewById(R.id.botaoEnviarNovaQuantidade);
            TextView textoAdicionarRemoverProduto = dialog.findViewById(R.id.textoAdicionarRemoverProduto);
            if (editarTextoNovaQuantidade.getVisibility() == View.GONE) {
                editarTextoNovaQuantidade.setVisibility(View.VISIBLE);
                botaoEnviarNovaQuantidade.setVisibility(View.VISIBLE);
                textoAdicionarRemoverProduto.setVisibility(View.VISIBLE);

                botaoEnviarNovaQuantidade.setOnClickListener(w->{
                    if (editarTextoNovaQuantidade.getText().toString().isEmpty()) {
                        Toast.makeText(this, "Insira uma quantidade antes de alterar", Toast.LENGTH_LONG).show();
                    } else {
                        int novaQuantidade = Integer.parseInt(editarTextoNovaQuantidade.getText().toString());
                        if (Math.abs(novaQuantidade) == 0) {
                            Toast.makeText(this, "Adicione ou remova uma quantidade válida", Toast.LENGTH_LONG).show();
                        } else {
                            String idProduto;
                            if (FragStock.viewModel.getUser().getValue().getData() == null) {
                                idProduto = Integer.toString(FragStock.viewModel.getUser().getValue().getEstoque().get(posicaoEstoque).getProdutos().get(position).getId());
                            } else {
                                idProduto = Integer.toString(FragStock.viewModel.getUser().getValue().getData().getEstoque().get(posicaoEstoque).getProdutos().get(position).getId());
                            }
                            String idEstoque = getIntent().getStringExtra("idEstoque");
                            int subtracao = quantidadeAtual + novaQuantidade;
                            if (subtracao <= 0) {
                                popupConfirmarQuantidadeProduto(subtracao, quantidadeAtual, idProduto, idEstoque, novaQuantidade, position);
                            } else {
                                mudar_produto(idProduto, idEstoque, novaQuantidade, position, subtracao);
                            }
                            dialog.dismiss();
                        }
                    }
                });
            } else {
                editarTextoNovaQuantidade.setVisibility(View.GONE);
                botaoEnviarNovaQuantidade.setVisibility(View.GONE);
                textoAdicionarRemoverProduto.setVisibility(View.GONE);
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.FadeInOutMiddle;
        dialog.getWindow().setGravity(Gravity.CENTER);
    }

    public void popupConfirmarQuantidadeProduto(int subtracao, int quantidadeAtual, String idProduto, String idEstoque, int novaQuantidade, int position) {
        String faltou = ". ";
        if (subtracao == -1) {
            faltou = ", devendo apenas 1 produto para ser removido. ";
        } else if (subtracao < 0) {
            faltou = ", devendo " + Math.abs(subtracao) + " produtos para serem removidos. ";
        }
            AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Aviso")
                .setMessage("A quantidade " + novaQuantidade +  " irá remover o produto do estoque" + faltou + "Tem certeza que deseja continuar?")
                .setPositiveButton("Sim", (dialogConfirmar, which) -> {
                    mudar_produto(idProduto, idEstoque, novaQuantidade, position, subtracao);
                })
                .setNegativeButton("Cancelar", (dialogCancelar, which) -> dialogCancelar.dismiss())
                .create();

        // Altera a cor do botão exibido
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.modern_red));
            negativeButton.setTextColor(ContextCompat.getColor(this, R.color.green2));
        });

        dialog.show();
    }

    public void popupAvisarEstoqueApagado() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Aviso")
                .setMessage("Não foi possível concluir a operação, pois este Stock foi apagado através de outro dispositivo. Por favor, volte e atualize sua tela de Stocks.")
                .setPositiveButton("OK", (dialogConfirmar, which) -> {
                    finish();
                })
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.green2));
        });

        dialog.show();
    }

    public void mudar_produto(String idProduto, String idEstoque, int novaQuantidade, int position, int subtracao){
        if (er.possuiInternet(this)) {

            Estoque produtoNovaQuantidade = new Estoque();
            produtoNovaQuantidade.setId(er.obterMemoriaInterna("idConexao"));
            produtoNovaQuantidade.setId_estoque(idEstoque);
            produtoNovaQuantidade.setId_produto(idProduto);
            produtoNovaQuantidade.setQuantidade(novaQuantidade);

            // Converter o objeto User para JSON
            String userJson = gson.toJson(produtoNovaQuantidade);

            // Fazer a requisição
            er.post("mudar_produto", userJson, response -> {
                if (response.startsWith("Erro")) {
                    runOnUiThread(() -> Toast.makeText(this, response, Toast.LENGTH_LONG).show());
                } else {
                    try {
                        // Processar resposta da requisição
                        Estoque responseEstoque = gson.fromJson(response, Estoque.class);
                        if (responseEstoque.getCode() == 0 || responseEstoque.getCode() == 28) {
                            runOnUiThread(() -> {
                                if (subtracao <= 0) { // Produto é removido do estoque
                                    adaptadorItemProduto.removerProduto(position);

                                    textoProdutoVazio.post(() -> {
                                        if (FragStock.viewModel.getUser().getValue().getData() == null) {
                                            if (FragStock.viewModel.getUser().getValue().getEstoque().get(posicaoEstoque).getProdutos().isEmpty()) {
                                                textoProdutoVazio.setVisibility(View.VISIBLE);
                                            }
                                        } else {
                                            if (FragStock.viewModel.getUser().getValue().getData().getEstoque().get(posicaoEstoque).getProdutos().isEmpty()) {
                                                textoProdutoVazio.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    });
                                } else {
                                    adaptadorItemProduto.editarQuantidadeProduto(novaQuantidade, position);
                                }
                            });
                        } else if (responseEstoque.getCode() == 26) {
                            runOnUiThread(() -> popupAvisarEstoqueApagado());
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(this, "Erro ao modificar o produto do Stock", Toast.LENGTH_SHORT).show());
                    }
                }
            });
        } else {
            runOnUiThread(() -> Toast.makeText(this, "Verifique sua conexão com a internet.", Toast.LENGTH_SHORT).show());
        }
    }
    private void dialogRegistrarProduto() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_registrar_produto);
        dialog.setCanceledOnTouchOutside(false);

        ImageView imagemProduto = dialog.findViewById(R.id.imagemRegistrarProduto);
        ProgressBar imagemCarregandoRegistrarProduto = dialog.findViewById(R.id.imagemCarregandoRegistrarProduto);

        EditText nomeProduto = dialog.findViewById(R.id.inserirNomeProduto);
        EditText descricaoProduto = dialog.findViewById(R.id.inserirDescricaoProduto);
        EditText quantidadeProduto = dialog.findViewById(R.id.quantidadeProdutoRegistro);

        EditText textoData = dialog.findViewById(R.id.inserirDataValidadeProduto);
        textoData.setOnClickListener(v -> dialogCalendarioValidade(textoData));

        EditText editTextTextoProduto = dialog.findViewById(R.id.editTextPrecoProduto);
        formatarPreco(editTextTextoProduto);

        Button cameraProduto = dialog.findViewById(R.id.selecionarCameraProduto);
        Button galeriaProduto = dialog.findViewById(R.id.selecionarGaleriaProduto);
        Button cancelarRegistroProduto = dialog.findViewById(R.id.cancelarRegistroProduto);
        Button registrarProduto = dialog.findViewById(R.id.registrarProdutoEmEstoque);

        cameraProduto.setOnClickListener(v -> cg.pedirPermissaoCamera(new CameraGaleria.CallbackCameraGaleria() {
            @Override
            public void onImageSelected(Uri uri) {
                imagemCarregandoRegistrarProduto.setVisibility(View.VISIBLE);
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    String base64 = cg.converterUriParaBase64(uri);
                    runOnUiThread(() -> {
                        if (!isDestroyed() || !isFinishing()) {
                            imagemBase64 = base64;

                            // Método assíncrono para exibir a imagem na tela e apagar o arquivo logo em seguida
                            Glide.with(TelaProduto.this).load(uri).diskCacheStrategy(DiskCacheStrategy.ALL).listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                                    Toast.makeText(TelaProduto.this, "Erro ao carregar a imagem na tela", Toast.LENGTH_LONG).show();
                                    imagemCarregandoRegistrarProduto.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                                    imagemCarregandoRegistrarProduto.setVisibility(View.GONE);
                                    cg.deletarImagemUri(uri);
                                    return false;
                                }
                            }).into(imagemProduto);
                        }
                    });
                });
                executor.shutdown();
            }
        }));

        galeriaProduto.setOnClickListener(v -> cg.pedirPermissaoGaleria(new CameraGaleria.CallbackCameraGaleria() {
            @Override
            public void onImageSelected(Uri uri) {
                imagemCarregandoRegistrarProduto.setVisibility(View.VISIBLE);
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    String base64 = cg.converterUriParaBase64(uri);
                    runOnUiThread(() -> {
                        if (!isDestroyed() || !isFinishing()) {
                            imagemBase64 = base64;

                            // Método assíncrono para exibir a imagem na tela e apagar o arquivo logo em seguida
                            Glide.with(TelaProduto.this).load(uri).diskCacheStrategy(DiskCacheStrategy.ALL).listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                                    Toast.makeText(TelaProduto.this, "Erro ao carregar a imagem na tela", Toast.LENGTH_LONG).show();
                                    imagemCarregandoRegistrarProduto.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                                    imagemCarregandoRegistrarProduto.setVisibility(View.GONE);
                                    return false;
                                }
                            }).into(imagemProduto);
                        }
                    });
                });
                executor.shutdown();
            }
        }));

        cancelarRegistroProduto.setOnClickListener(v-> dialog.dismiss());

        registrarProduto.setOnClickListener(v->{

            String stringNomeProduto = nomeProduto.getText().toString();
            String stringDescricaoProduto = descricaoProduto.getText().toString();
            String stringQuantidadeProduto = quantidadeProduto.getText().toString();
            String stringPrecoProduto = editTextTextoProduto.getText().toString();
            String stringDataValidadeProduto = textoData.getText().toString();
            int idEstoque = Integer.parseInt(getIntent().getStringExtra("idEstoque"));

            if (stringNomeProduto.isEmpty() || stringDescricaoProduto.isEmpty() ||
                    stringQuantidadeProduto.isEmpty() || stringPrecoProduto.isEmpty() || stringDataValidadeProduto.isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, "Preencha todas as informações do produto", Toast.LENGTH_SHORT).show());
            } else if (Integer.parseInt(stringQuantidadeProduto) == 0){
                runOnUiThread(() -> Toast.makeText(this, "Insira uma quantidade maior que 0", Toast.LENGTH_SHORT).show());
            } else {
                if (imagemCarregandoRegistrarProduto.getVisibility() == View.VISIBLE) {
                    runOnUiThread(()->Toast.makeText(this, "Por favor, aguarde a imagem ser carregada.", Toast.LENGTH_LONG).show());
                } else {
                    try {
                        adicionarProdutoEmEstoque(idEstoque, Integer.parseInt(stringQuantidadeProduto), formatadorPontoVirgula.parse(stringPrecoProduto).doubleValue(), stringDataValidadeProduto, stringNomeProduto, stringDescricaoProduto, imagemBase64, false, adaptadorResultadoProdutoRecyclerView.getItemCount());
                        dialog.dismiss();
                    } catch (ParseException pe) {
                        runOnUiThread(()->Toast.makeText(this, "Erro interno.", Toast.LENGTH_LONG).show());
                    }
                }
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.CENTER);
    }

    private void dialogCalendarioValidade(EditText textoValidade) {
        final Calendar c = Calendar.getInstance();

        int ano = c.get(Calendar.YEAR);
        int mes = c.get(Calendar.MONTH);
        int dia = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int anoEscolhido,
                                  int mesEscolhido, int diaEscolhido) {
                textoValidade.setText(String.format("%02d", diaEscolhido) + "/" + String.format("%02d", mesEscolhido + 1) + "/" + anoEscolhido);
            }
        },
                ano, mes, dia);
        datePickerDialog.show();
    }
    private void formatarPreco(EditText preco) {
        String precoZerado = "0,00";
        preco.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (preco.getText().toString().isEmpty()) {
                    preco.setText(precoZerado);
                }
                preco.setSelection(preco.getText().length());
            }
        });

        preco.setOnClickListener(v -> preco.setSelection(preco.getText().length()));

        preco.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) {
                    return;
                }
                isUpdating = true;
                String str = s.toString().replaceAll("[^\\d]", "");

                if (str.isEmpty()) {
                    preco.setText(precoZerado);
                } else {
                    try {
                        double valor = Double.parseDouble(str) / 100.0;
                        String formatado = decimalFormat.format(valor);
                        preco.removeTextChangedListener(this);
                        preco.setText(formatado);
                        preco.setSelection(preco.getText().length());
                        preco.addTextChangedListener(this);
                    } catch (NumberFormatException e) {
                        preco.setText(precoZerado);
                        preco.setSelection(preco.getText().length());
                    }
                }

                isUpdating = false;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void adicionarProdutoEmEstoque(int idEstoque, int quantidade, Double preco, String dataValidade, String nomeProduto, String descricaoProduto, String imagemBase64, boolean requisicaoFalha, int indiceDetalheRequisicao) {
        if (er.possuiInternet(this)) {
            // Criando o elemento para exibir na lista de requisições
            quantidadeRequisicoesEnviando++;
            ResultadoRequisicaoEstoque res = new ResultadoRequisicaoEstoque();
            res.setIconeResultado(2);
            res.setTituloResultado("Registrando produto: " + nomeProduto);
            res.setNomeProduto(nomeProduto);
            res.setDescricaoProduto(descricaoProduto);
            res.setIdEstoque(Integer.toString(idEstoque));
            res.setQuantidadeProduto(Integer.toString(quantidade));
            res.setPrecoProduto(preco);
            res.setDataValidadeProduto(dataValidade);
            res.setMetodoDeEnvio("Registro de novo produto");
            res.setImagemBase64(imagemBase64);
            int indiceEnviandoAtual;
            if (requisicaoFalha) {
                indiceEnviandoAtual = indiceDetalheRequisicao;
                adaptadorResultadoProdutoRecyclerView.alterarDetalheRequisicaoEstoque(res, indiceDetalheRequisicao);
            } else {
                indiceEnviandoAtual = adaptadorResultadoProdutoRecyclerView.getItemCount();
                adaptadorResultadoProdutoRecyclerView.adicionarRequisicaoEstoque(res);
            }
            mostrarProcessoRequisicao();

            if (imagemBase64.isEmpty()) { // Enviar produto sem imagem
                registroProduto(nomeProduto, descricaoProduto, imagemBase64, new CallbackResponseProduto() {
                    @Override
                    public void onCompleteRegistroProduto(Produto produtoRegistrado) {
                        if (produtoRegistrado != null) {
                            registroProdutoEmEstoque(idEstoque, produtoRegistrado.getId_produto(), quantidade, dataValidade, preco, nomeProduto, descricaoProduto, imagemBase64, requisicaoFalha, indiceEnviandoAtual);
                        } else {
                            runOnUiThread(() -> {
                                falhaRequisicaoProduto("Falha ao criar o produto ", indiceEnviandoAtual, "erro", "Falha ao registrar o novo produto", requisicaoFalha);
                                mostrarProcessoRequisicao();
                            });
                        }
                    }
                }, indiceEnviandoAtual, requisicaoFalha);
            } else { //Enviar imagem e registrar produto com imagem
                if (imagemBase64.startsWith("http://")) {
                    registroProduto(nomeProduto, descricaoProduto, imagemBase64, new CallbackResponseProduto() {
                        @Override
                        public void onCompleteRegistroProduto(Produto produtoRegistrado) {
                            if (produtoRegistrado != null) {
                                registroProdutoEmEstoque(idEstoque, produtoRegistrado.getId_produto(), quantidade, dataValidade, preco, nomeProduto, descricaoProduto, imagemBase64, requisicaoFalha, indiceEnviandoAtual);
                            }
                            else {
                                runOnUiThread(() -> {
                                    falhaRequisicaoProduto("Falha ao criar o produto ", indiceEnviandoAtual, "erro", "Falha ao registrar o novo produto", requisicaoFalha);
                                    mostrarProcessoRequisicao();
                                });
                            }
                        }
                    }, indiceEnviandoAtual, requisicaoFalha);
                } else {
                    upload_img(imagemBase64, new CallbackResponseUpload() {
                        @Override
                        public void onCompleteUploadImg(String urlImagem) {
                            if (urlImagem != null) {
                                if (urlImagem.equals("O arquivo")) {
                                    runOnUiThread(() -> {
                                        falhaRequisicaoProduto("Falha ao criar o produto ", indiceEnviandoAtual, "erro", urlImagem, requisicaoFalha);
                                        mostrarProcessoRequisicao();
                                    });
                                } else {
                                    adaptadorResultadoProdutoRecyclerView.resultado.get(indiceDetalheRequisicao).setImagemUrl(urlImagem);
                                    adaptadorResultadoProdutoRecyclerView.resultado.get(indiceDetalheRequisicao).setImagemBase64("");
                                registroProduto(nomeProduto, descricaoProduto, urlImagem, new CallbackResponseProduto() {
                                    @Override
                                    public void onCompleteRegistroProduto(Produto produtoRegistrado) {
                                        if (produtoRegistrado != null) {
                                            registroProdutoEmEstoque(idEstoque, produtoRegistrado.getId_produto(), quantidade, dataValidade, preco, nomeProduto, descricaoProduto, urlImagem, requisicaoFalha, indiceEnviandoAtual);
                                        } else {
                                            runOnUiThread(() -> {
                                                falhaRequisicaoProduto("Falha ao criar o produto ", indiceEnviandoAtual, "erro", "Falha ao registrar o novo produto", requisicaoFalha);
                                                mostrarProcessoRequisicao();
                                            });
                                        }
                                    }
                                }, indiceEnviandoAtual, requisicaoFalha);
                            }
                        }else {
                                runOnUiThread(() -> {
                                    falhaRequisicaoProduto("Falha ao criar o produto ", indiceEnviandoAtual, "erro", "Falha no upload de imagem", requisicaoFalha);
                                    mostrarProcessoRequisicao();
                                });
                            }
                        }
                    });
                }
            }
        }  else {
            runOnUiThread(() -> Toast.makeText(this, "Verifique sua conexão com a internet.", Toast.LENGTH_SHORT).show());
        }
    }

    private void registroProdutoEmEstoque(int idEstoque, int idProduto, int quantidade, String dataValidade, Double preco, String nomeProduto, String descricaoProduto, String urlImagem, boolean requisicaoFalha, int indiceRequisicaoEnviando) {
        if (er.possuiInternet(this)) {

            EstoqueDataValidadeString estoque = new EstoqueDataValidadeString();
            estoque.setId(er.obterMemoriaInterna("idConexao"));
            estoque.setId_estoque(Integer.toString(idEstoque));
            estoque.setId_produto(Integer.toString(idProduto));
            estoque.setQuantidade(quantidade);
            estoque.setData_validade(dataValidade);
            estoque.setPreco(preco);

            // Converter o objeto User para JSON
            String userJson = gson.toJson(estoque);

            // Fazer a requisição
            er.post("registro_produto_estoque", userJson, response -> {
                if (response.startsWith("Erro")) {
                    runOnUiThread(() -> {
                    falhaRequisicaoProduto("Falha ao criar o produto ", indiceRequisicaoEnviando, "erro", response, requisicaoFalha);
                    mostrarProcessoRequisicao();
                    });
                } else {
                    try {
                        // Processar resposta da requisição
                        Estoque responseEstoque = gson.fromJson(response, Estoque.class);
                        if (responseEstoque.getCode() == 0) {
                                runOnUiThread(() -> {
                                    Produto produtoInfo = new Produto();
                                    produtoInfo.setId(idProduto);
                                    produtoInfo.setQuantidade(quantidade);
                                    produtoInfo.setNome(nomeProduto);
                                    produtoInfo.setDescricao(descricaoProduto);
                                    produtoInfo.setFoto(urlImagem);
                                    produtoInfo.setPreco_medio(preco);
                                    produtoInfo.getLista_precos().add(preco);
                                    produtoInfo.getLista_quantidades().add(quantidade);
                                    produtoInfo.getData_validade().add(dataValidade);

                                    sucessoRequisicaoProduto("Produto registrado: ", indiceRequisicaoEnviando, responseEstoque.getStatus(), responseEstoque.getMessage(), requisicaoFalha);
                                    mostrarProcessoRequisicao();

                                    adaptadorItemProduto.adicionarArrayProduto(produtoInfo);
                                    textoProdutoVazio.setVisibility(View.GONE);
                                });
                        } else if (responseEstoque.getCode() == 26) {
                            /*
                            Ainda é possível adicionar produtos à estoques não existentes.
                            Quando atualizar, verificar se esta verificação ainda funciona.
                             */
                            runOnUiThread(() -> popupAvisarEstoqueApagado());
                        } else {
                            runOnUiThread(() -> {
                                falhaRequisicaoProduto("Falha ao criar o produto ", indiceRequisicaoEnviando, "erro", responseEstoque.getMessage(), requisicaoFalha);
                                mostrarProcessoRequisicao();
                            });
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Erro ao registrar o produto no Stock", Toast.LENGTH_SHORT).show();
                            falhaRequisicaoProduto("Falha ao criar o produto ", indiceRequisicaoEnviando, "erro", "Falha ao adicionar o produto na tela. Tente atualizar a tela.", requisicaoFalha);
                            mostrarProcessoRequisicao();
                        });
                    }
                }
            });
        } else {
            runOnUiThread(() -> Toast.makeText(this, "Verifique sua conexão com a internet.", Toast.LENGTH_SHORT).show());
        }
    }

    private void registroProduto(String nome, String descricao, String urlImagem, CallbackResponseProduto callback, int indiceRequisicaoEnviando, boolean requisicaoFalha) {
        if (er.possuiInternet(this)) {

            Estoque estoque = new Estoque();
            estoque.setId(er.obterMemoriaInterna("idConexao"));
            estoque.setNome(nome);
            estoque.setDescricao(descricao);
            estoque.setImagem(urlImagem);

            // Converter o objeto User para JSON
            String userJson = gson.toJson(estoque);

            // Fazer a requisição
            er.post("registro_produto", userJson, response -> {
                if (response.startsWith("Erro")) {
                    callback.onCompleteRegistroProduto(null);
                } else {
                    try {
                        // Processar resposta da requisição
                        Produto responseEstoque = gson.fromJson(response, Produto.class);
                        if (responseEstoque.getCode() == 0) {
                            callback.onCompleteRegistroProduto(responseEstoque);
                        } else {
                            callback.onCompleteRegistroProduto(null);
                        }
                    } catch (Exception e) {
                        callback.onCompleteRegistroProduto(null);
                    }
                }
            });
        } else {
            runOnUiThread(() -> Toast.makeText(this, "Verifique sua conexão com a internet.", Toast.LENGTH_SHORT).show());
            callback.onCompleteRegistroProduto(null);
        }
    }

    private void upload_img(String imagemBase64, CallbackResponseUpload callback) {
        if (er.possuiInternet(this)) {
            User user = new User();
            user.setId(er.obterMemoriaInterna("idConexao"));
            user.setDestino("outro");
            user.setFile(imagemBase64);

            // Converter o objeto User para JSON
            String userJson = gson.toJson(user);

            er.post("upload_img", userJson, response -> {
                if (response.startsWith("Erro")) {
                    callback.onCompleteUploadImg(null); // Erro no upload
                } else if (response.startsWith("<html>")) {
                    callback.onCompleteUploadImg("O arquivo de imagem é muito grande. Tente carregar outra imagem.");
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
                        callback.onCompleteUploadImg(null);
                    }
                }
            });
        } else {
            runOnUiThread(() ->
                    Toast.makeText(this, "Verifique sua conexão com a internet.", Toast.LENGTH_SHORT).show()
            );
            callback.onCompleteUploadImg(null);
        }
    }

    private void get_estoque() {
        if (er.possuiInternet(this)) {

            Estoque estoque = new Estoque();
            estoque.setId(er.obterMemoriaInterna("idConexao"));

            // Converter o objeto User para JSON
            String jsonGetEstoqueAtualizarProduto = gson.toJson(estoque);

            // Fazer a requisição
            er.post("get_estoque", jsonGetEstoqueAtualizarProduto, response -> {
                if (response.startsWith("Erro")) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, response, Toast.LENGTH_LONG).show();
                        swipeRefreshLayoutProduto.setRefreshing(false);
                    });
                } else {
                    try {
                        // Processar resposta da requisição
                        User responseEstoque = gson.fromJson(response, User.class);
                        if (responseEstoque.getCode() == 0) {
                            posicaoEstoque = getIntent().getIntExtra("position", -1);
                            runOnUiThread(() -> {
                                String idEstoqueAtual;
                                if (FragStock.viewModel.getUser().getValue().getData() == null) { // Login por get_dados
                                    idEstoqueAtual = FragStock.viewModel.getUser().getValue().getEstoque().get(posicaoEstoque).getId();
                                } else { // Login por login
                                    idEstoqueAtual = FragStock.viewModel.getUser().getValue().getData().getEstoque().get(posicaoEstoque).getId();
                                }

                                int posicaoEstoqueResponse = -1;
                                for (int i = 0; i < responseEstoque.getEstoque().size(); i++) {
                                    if (responseEstoque.getEstoque().get(i).getId().equals(idEstoqueAtual)) {
                                        posicaoEstoqueResponse = i;
                                        break;
                                    }
                                }

                                if (posicaoEstoqueResponse != -1) { // Se é diferente, o estoque ainda existe

                                    if (FragStock.viewModel.getUser().getValue().getData() == null) {
                                        FragStock.viewModel.getUser().getValue().getEstoque().get(posicaoEstoque).setProdutos(responseEstoque.getEstoque().get(posicaoEstoqueResponse).getProdutos());
                                    } else {
                                        FragStock.viewModel.getUser().getValue().getData().getEstoque().get(posicaoEstoque).setProdutos(responseEstoque.getEstoque().get(posicaoEstoqueResponse).getProdutos());
                                    }

                                    if (responseEstoque.getEstoque().get(posicaoEstoqueResponse).getProdutos().isEmpty()) {
                                        textoProdutoVazio.setVisibility(View.VISIBLE);
                                    } else {
                                        textoProdutoVazio.setVisibility(View.GONE);
                                    }
                                    adaptadorItemProduto.atualizarListaProduto(responseEstoque.getEstoque().get(posicaoEstoqueResponse).getProdutos());
                                    swipeRefreshLayoutProduto.setRefreshing(false);
                                } else {
                                    popupAvisarEstoqueApagado();
                                }
                            });
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Erro ao atualizar a lista de produtos", Toast.LENGTH_SHORT).show();
                            swipeRefreshLayoutProduto.setRefreshing(false);
                        });
                    }
                }
            });
        } else {
            runOnUiThread(() -> {
                Toast.makeText(this, "Verifique sua conexão com a internet.", Toast.LENGTH_SHORT).show();
                swipeRefreshLayoutProduto.setRefreshing(false);
            });
        }
    }

    private void mostrarProcessoRequisicao() {
        // Atualiza o título para "Enviando requisição"
        atualizarTituloRequisicao();

        // Executa a animação apenas se não está exibido na tela ou se está saindo ou entrando na tela
        if (backgroundEnviandoProduto.getTranslationY() > -alturaEmPixels) {

            ObjectAnimator animLayout = ObjectAnimator.ofFloat(backgroundEnviandoProduto, "translationY", 0f, -alturaEmPixels);
            ObjectAnimator animBotao = ObjectAnimator.ofFloat(botaoDialogRegistrarProduto, "translationY", 0f, -alturaEmPixels);

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
            progressBarEnviandoProduto.setVisibility(View.VISIBLE);
            imagemSucesso.setVisibility(View.GONE);
            imagemFalha.setVisibility(View.GONE);
            imagemAviso.setVisibility(View.GONE);
            backgroundEnviandoProduto.setBackgroundColor(ContextCompat.getColor(this, R.color.gray));
            if (quantidadeRequisicoesEnviando == 1) {
                textoEnviandoRequisicao.setText("Processando " + quantidadeRequisicoesEnviando + " requisição...");
            } else {
                textoEnviandoRequisicao.setText("Processando " + quantidadeRequisicoesEnviando + " requisições...");
            }
        }
        else {
            // Não há requisições sendo enviadas e exibe o resultado das requisições
            progressBarEnviandoProduto.setVisibility(View.GONE);
            textoEnviandoRequisicao.setText(quantidadeRequisicoesSucesso + " sucesso(s), " + quantidadeRequisicoesFalha + " falha(s)");

            // Verifica se há requisições com sucesso e com falha
            if (quantidadeRequisicoesFalha > 0 && quantidadeRequisicoesSucesso > 0) {
                imagemAviso.setVisibility(View.VISIBLE);
                imagemFalha.setVisibility(View.GONE);
                imagemSucesso.setVisibility(View.GONE);
                backgroundEnviandoProduto.setBackgroundColor(ContextCompat.getColor(this, R.color.aviso));
            }
            // Verifica se há apenas resultados das requisições com falhas
            else if (quantidadeRequisicoesFalha > 0 && quantidadeRequisicoesSucesso == 0) {
                imagemAviso.setVisibility(View.GONE);
                imagemFalha.setVisibility(View.VISIBLE);
                imagemSucesso.setVisibility(View.GONE);
                backgroundEnviandoProduto.setBackgroundColor(ContextCompat.getColor(this, R.color.falha));
            } else {
                // Todos os resultados das requisições são com sucesso
                imagemAviso.setVisibility(View.GONE);
                imagemFalha.setVisibility(View.GONE);
                imagemSucesso.setVisibility(View.VISIBLE);
                backgroundEnviandoProduto.setBackgroundColor(ContextCompat.getColor(this, R.color.sucesso));
            }
        }
    }

    // Remove a lista de requisições da tela
    private void fecharProcessoRequisicao() {
        detalheEnviarRequisicaoExibido = false;
        backgroundEnviandoProduto.setTranslationY(0);
        recyclerViewResultadoRequisicaoEnviandoProduto.setTranslationY(0);
        botaoDialogRegistrarProduto.setTranslationY(0);
    }

    private void toggleDetalheProcessoRequisicao() {
        ObjectAnimator animLayout;
        ObjectAnimator animRecyclerView;
        ObjectAnimator animRotacao;
        if (detalheEnviarRequisicaoExibido) {
            animLayout = ObjectAnimator.ofFloat(backgroundEnviandoProduto, "translationY", -alturaEmPixelsEnviandoRequisicao, -alturaEmPixels);
            animRecyclerView = ObjectAnimator.ofFloat(recyclerViewResultadoRequisicaoEnviandoProduto, "translationY", -alturaEmPixelsEnviandoRequisicao, -alturaEmPixels);
            animRotacao = ObjectAnimator.ofFloat(iconeExpandirEnviandoRequisicaoProduto, "rotation", 270f, 90f);
            detalheEnviarRequisicaoExibido = false;
        } else {
            animLayout = ObjectAnimator.ofFloat(backgroundEnviandoProduto, "translationY", -alturaEmPixels, -alturaEmPixelsEnviandoRequisicao);
            animRecyclerView = ObjectAnimator.ofFloat(recyclerViewResultadoRequisicaoEnviandoProduto, "translationY", -alturaEmPixels, -alturaEmPixelsEnviandoRequisicao);
            animRotacao = ObjectAnimator.ofFloat(iconeExpandirEnviandoRequisicaoProduto, "rotation", 90f, 270f);
            detalheEnviarRequisicaoExibido = true;
        }

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animLayout, animRecyclerView, animRotacao);
        animatorSet.setDuration(200);
        animatorSet.start();
    }

    // Atualiza o conteúdo dentro do recyclerview de lista de requisições
    private void sucessoRequisicaoProduto(String descricao, int indiceRequisicaoEnviando, String status, String descricaoStatus, boolean requisicaoFalha) {
        quantidadeRequisicoesEnviando--;
        quantidadeRequisicoesSucesso++;
        if (requisicaoFalha) {
            quantidadeRequisicoesFalha--;
        }
        ResultadoRequisicaoEstoque resultadoAtual = adaptadorResultadoProdutoRecyclerView.getResultado(indiceRequisicaoEnviando);
        adaptadorResultadoProdutoRecyclerView.alterarRequisicaoEstoque(
                descricao + resultadoAtual.getNomeProduto(),
                0,
                "Status: " + status,
                "Descrição do resultado: " + descricaoStatus,
                indiceRequisicaoEnviando
        );
    }

    // Atualiza o conteúdo dentro do recyclerview de lista de requisições
    private void falhaRequisicaoProduto(String descricao, int indiceRequisicaoEnviando, String status, String descricaoStatus, boolean requisicaoFalha) {
        quantidadeRequisicoesEnviando--;
        if (!requisicaoFalha) {
            quantidadeRequisicoesFalha++;
        }
        ResultadoRequisicaoEstoque resultadoAtual = adaptadorResultadoProdutoRecyclerView.getResultado(indiceRequisicaoEnviando);
        adaptadorResultadoProdutoRecyclerView.alterarRequisicaoEstoque(descricao + resultadoAtual.getNomeProduto(),
                1, "Status: " + status,
                "Descrição do resultado: " + descricaoStatus,
                indiceRequisicaoEnviando);
    }

    private void dialogResultadoRequisicao(int position) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_status_requisicao_dialog);

        TextView nomeEstoque = dialog.findViewById(R.id.textoNomeEstoque);
        TextView nomeMetodoRequisicao = dialog.findViewById(R.id.nomeMetodoRequisicao);
        TextView statusEstoque = dialog.findViewById(R.id.textoStatusRequisicao);
        TextView descricaoStatusRequisicao = dialog.findViewById(R.id.textoDescricaoRequisicao);
        Button botaoFecharResultadoRequisicao = dialog.findViewById(R.id.botaoFecharResultadoRequisicao);
        Button botaoTentarNovamenteRequisicao = dialog.findViewById(R.id.botaoTentarNovamenteResultadoRequisicao);

        nomeEstoque.setText(adaptadorResultadoProdutoRecyclerView.getResultado(position).getNomeProduto());
        nomeMetodoRequisicao.setText(adaptadorResultadoProdutoRecyclerView.getResultado(position).getMetodoDeEnvio());
        statusEstoque.setText(adaptadorResultadoProdutoRecyclerView.getResultado(position).getStatus());
        descricaoStatusRequisicao.setText(adaptadorResultadoProdutoRecyclerView.getResultado(position).getDescricaoStatus());
        if (adaptadorResultadoProdutoRecyclerView.getResultado(position).getStatus().equals("Status: erro")) {
            botaoTentarNovamenteRequisicao.setVisibility(View.VISIBLE);
            botaoTentarNovamenteRequisicao.setOnClickListener(v-> {
                String metodo = adaptadorResultadoProdutoRecyclerView.getResultado(position).getMetodoDeEnvio();
                if (metodo.startsWith("Registro de")) {
                    if (!adaptadorResultadoProdutoRecyclerView.getResultado(position).getImagemUrl().isEmpty()) {
                        adicionarProdutoEmEstoque(
                                Integer.parseInt(adaptadorResultadoProdutoRecyclerView.getResultado(position).getIdEstoque()),
                                Integer.parseInt(adaptadorResultadoProdutoRecyclerView.getResultado(position).getQuantidadeProduto()),
                                adaptadorResultadoProdutoRecyclerView.getResultado(position).getPrecoProduto(),
                                adaptadorResultadoProdutoRecyclerView.getResultado(position).getDataValidadeProduto(),
                                adaptadorResultadoProdutoRecyclerView.getResultado(position).getNomeProduto(),
                                adaptadorResultadoProdutoRecyclerView.getResultado(position).getDescricaoProduto(),
                                adaptadorResultadoProdutoRecyclerView.getResultado(position).getImagemUrl(),
                                true,
                                position
                        );
                    } else {
                        adicionarProdutoEmEstoque(
                                Integer.parseInt(adaptadorResultadoProdutoRecyclerView.getResultado(position).getIdEstoque()),
                                Integer.parseInt(adaptadorResultadoProdutoRecyclerView.getResultado(position).getQuantidadeProduto()),
                                adaptadorResultadoProdutoRecyclerView.getResultado(position).getPrecoProduto(),
                                adaptadorResultadoProdutoRecyclerView.getResultado(position).getDataValidadeProduto(),
                                adaptadorResultadoProdutoRecyclerView.getResultado(position).getNomeProduto(),
                                adaptadorResultadoProdutoRecyclerView.getResultado(position).getDescricaoProduto(),
                                adaptadorResultadoProdutoRecyclerView.getResultado(position).getImagemBase64(),
                                true,
                                position
                        );
                    }
                } else {

                }
                dialog.dismiss();
            });
        }

        botaoFecharResultadoRequisicao.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.FadeInOutMiddle;
        dialog.getWindow().setGravity(Gravity.CENTER);
    }

}