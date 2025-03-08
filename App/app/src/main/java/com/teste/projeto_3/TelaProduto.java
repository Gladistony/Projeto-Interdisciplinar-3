package com.teste.projeto_3;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.teste.projeto_3.model.Produto;
import com.teste.projeto_3.model.User;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TelaProduto extends AppCompatActivity implements RecyclerViewInterface {

    private interface CallbackResponseUpload {
        void onCompleteUploadImg(String urlImagem);
    }

    private interface CallbackResponseProduto{
        void onCompleteRegistroProduto(Produto produto);
    }

    private AdaptadorProdutoRecyclerView adaptadorItemProduto;
    public ArrayList<Produto> produto;
    DecimalFormat decimalFormat;
    EnviarRequisicao er;
    CameraGaleria cg;
    String imagemBase64 = "";
    private final Gson gson = new Gson();
    NumberFormat formatadorPontoVirgula = NumberFormat.getInstance(new Locale("pt", "BR"));

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

        produto = getIntent().getParcelableArrayListExtra("produto");

        if (!produto.isEmpty()) {
            TextView textoProdutoVazio = findViewById(R.id.textoProdutoVazio);
            textoProdutoVazio.setVisibility(View.GONE);
        }

        TextView tituloEstoque = findViewById(R.id.tituloEstoque);
        tituloEstoque.setText(getIntent().getStringExtra("tituloEstoque"));

        FloatingActionButton botaoCriarProduto = findViewById(R.id.botaoDialogRegistrarProduto);
        botaoCriarProduto.setOnClickListener(v -> {
            dialogRegistrarProduto();
        });

        Button botaoFecharTelaProduto = findViewById(R.id.botaoVoltarTelaProduto);
        botaoFecharTelaProduto.setOnClickListener(v -> finish());


        RecyclerView recyclerView = findViewById(R.id.recyclerViewProduto);
        adaptadorItemProduto = new AdaptadorProdutoRecyclerView(this, produto, this);
        recyclerView.setAdapter(adaptadorItemProduto);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }


    @Override
    public void onItemClick(int position) {
    }


    @Override
    public void onItemLongClick(int position) {
        produto.remove(position);
        adaptadorItemProduto.notifyItemRemoved(position);
    }

    private void dialogRegistrarProduto() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_registrar_produto);
        dialog.setCanceledOnTouchOutside(false);

        ImageView imagemProduto = dialog.findViewById(R.id.imagemRegistrarProduto);

        EditText nomeProduto = dialog.findViewById(R.id.inserirNomeProduto);
        EditText descricaoProduto = dialog.findViewById(R.id.inserirDescricaoProduto);
        EditText quantidadeProduto = dialog.findViewById(R.id.quantidadeProdutoRegistro);

        EditText textoData = dialog.findViewById(R.id.dataValidadeProduto);
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
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    String base64 = cg.converterUriParaBase64(uri);
                    runOnUiThread(() -> {
                        if (!isDestroyed() || !isFinishing()) {
                            imagemBase64 = base64;

                            // Método assíncrono para exibir a imagem na tela e apagar o arquivo logo em seguida
                            Glide.with(TelaProduto.this).load(uri).diskCacheStrategy(DiskCacheStrategy.ALL).listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    Toast.makeText(TelaProduto.this, "Erro ao carregar a imagem na tela", Toast.LENGTH_LONG).show();
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    cg.deletarImagemUri(uri);
                                    return false;
                                }
                            }).into(imagemProduto);
                        }
                    });
                });
                executor.shutdown();
                //imagemBase64 = cg.converterUriParaBase64(uri);
            }
        }));

        galeriaProduto.setOnClickListener(v -> cg.pedirPermissaoGaleria(new CameraGaleria.CallbackCameraGaleria() {
            @Override
            public void onImageSelected(Uri uri) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    String base64 = cg.converterUriParaBase64(uri);
                    runOnUiThread(() -> {
                        if (!isDestroyed() || !isFinishing()) {
                            imagemBase64 = base64;
                            Glide.with(TelaProduto.this).load(uri).diskCacheStrategy(DiskCacheStrategy.ALL).into(imagemProduto);
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
                runOnUiThread(() -> Toast.makeText(TelaProduto.this, "Preencha todas as informações do produto", Toast.LENGTH_SHORT).show());
            }else if (idEstoque != -1){
                try {
                    adicionarProdutoEmEstoque(idEstoque, Integer.parseInt(stringQuantidadeProduto), formatadorPontoVirgula.parse(stringPrecoProduto).doubleValue(), stringDataValidadeProduto, stringNomeProduto, stringDescricaoProduto);
                    dialog.dismiss();
                } catch (ParseException e) {
                    e.printStackTrace();
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
        preco.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                String texto = preco.getText().toString();
                if (texto.isEmpty()) {
                    preco.setText("0,00");
                    preco.setSelection(preco.getText().length());
                }
            }
        });

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
                    preco.setText("0,00");
                } else {
                    try {
                        double valor = Double.parseDouble(str) / 100.0;
                        String formatado = decimalFormat.format(valor);
                        preco.removeTextChangedListener(this);
                        preco.setText(formatado);
                        preco.setSelection(preco.getText().length());
                        preco.addTextChangedListener(this);
                    } catch (NumberFormatException e) {
                        preco.setText("0,00");
                        preco.setSelection(preco.getText().length());
                    }
                }

                isUpdating = false;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void adicionarProdutoEmEstoque(int idEstoque, int quantidade, Double preco, String dataValidade, String nomeProduto, String descricaoProduto) {
        if (er.possuiInternet(this)) {
            if (imagemBase64.isEmpty()) { // Enviar produto sem imagem
                registroProduto(nomeProduto, descricaoProduto, imagemBase64, new CallbackResponseProduto() {
                    @Override
                    public void onCompleteRegistroProduto(Produto produtoRegistrado) {
                        if (produtoRegistrado != null) {
                            registroProdutoEmEstoque(idEstoque, produtoRegistrado.getId_produto(), quantidade, dataValidade, preco, produtoRegistrado, nomeProduto, descricaoProduto, imagemBase64);
                        }
                    }
                });
            } else { //Enviar imagem e registrar produto com imagem
                upload_img(imagemBase64, new CallbackResponseUpload() {
                    @Override
                    public void onCompleteUploadImg(String urlImagem) {
                        if (urlImagem != null) {
                            imagemBase64 = "";
                            registroProduto(nomeProduto, descricaoProduto, urlImagem, new CallbackResponseProduto() {
                                @Override
                                public void onCompleteRegistroProduto(Produto produtoRegistrado) {
                                    registroProdutoEmEstoque(idEstoque, produtoRegistrado.getId_produto(), quantidade, dataValidade, preco, produtoRegistrado, nomeProduto, descricaoProduto, urlImagem);
                                }
                            });
                        } else {
                            runOnUiThread(() ->
                                    Toast.makeText(TelaProduto.this, "Falha no upload de imagem", Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                });
            }
        }
    }

    private void registroProdutoEmEstoque(int idEstoque, int idProduto, int quantidade, String dataValidade, Double preco, Produto produtoRegistrado, String nomeProduto, String descricaoProduto, String urlImagem) {
        if (er.possuiInternet(this)) {

            Estoque estoque = new Estoque();
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
                    runOnUiThread(() -> Toast.makeText(this, response, Toast.LENGTH_LONG).show());
                } else {
                    try {
                        // Processar resposta da requisição
                        Estoque responseEstoque = gson.fromJson(response, Estoque.class);
                        if (responseEstoque.getCode() == 0) {
                                runOnUiThread(() -> {
                                    Produto produtoInfo = new Produto();
                                    produtoInfo.setId(idProduto);
                                    produtoInfo.setQuantidade(Integer.toString(quantidade));
                                    produtoInfo.setNome(nomeProduto);
                                    produtoInfo.setDescricao(descricaoProduto);
                                    produtoInfo.setFoto(urlImagem);
                                    produtoInfo.setPreco_medio(preco);
                                    produtoInfo.getLista_precos().add(preco);
                                    produtoInfo.getLista_quantidades().add(quantidade);

                                    adaptadorItemProduto.adicionarArrayProduto(produtoInfo);
                                    FragStock.adaptadorItemEstoque.notificarNovoProdutoEstoque(produtoInfo, getIntent().getIntExtra("position", -1));
                                    Toast.makeText(TelaProduto.this, "Novo Stock adicionado com sucesso", Toast.LENGTH_LONG).show();
                                });
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(this, "Erro ao registrar o produto no Stock", Toast.LENGTH_SHORT).show());
                    }
                }
            });
        } else {
            runOnUiThread(() -> Toast.makeText(this, "Verifique sua conexão com a internet.", Toast.LENGTH_SHORT).show());
        }
    }

    private void registroProduto(String nome, String descricao, String urlImagem, CallbackResponseProduto callback) {
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
                    runOnUiThread(() -> Toast.makeText(this, response, Toast.LENGTH_LONG).show());
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
                        runOnUiThread(() -> Toast.makeText(this, "Erro ao processar a resposta. Tente novamente.", Toast.LENGTH_SHORT).show());
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
                    runOnUiThread(() ->
                            Toast.makeText(this, response, Toast.LENGTH_LONG).show()
                    );
                    callback.onCompleteUploadImg(null); // Erro no upload
                } else if (response.startsWith("<html>")) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "O arquivo de imagem é muito grande", Toast.LENGTH_LONG).show()
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
                        runOnUiThread(() ->
                                Toast.makeText(this, "Erro ao processar o upload da imagem", Toast.LENGTH_SHORT).show()
                        );
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

    private void editarQuantidadeProduto(){

    }
}