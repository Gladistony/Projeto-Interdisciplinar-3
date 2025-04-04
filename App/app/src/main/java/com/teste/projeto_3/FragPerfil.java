package com.teste.projeto_3;

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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.text.InputType;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.teste.projeto_3.http.EnviarRequisicao;
import com.teste.projeto_3.model.Data;
import com.teste.projeto_3.model.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FragPerfil extends Fragment {
    EnviarRequisicao er;
    private final Gson gson = new Gson();
    private ImageView fotoPerfil;
    private CameraGaleria cg;
    private ProgressBar animacaoCarregandoImagem;
    private SharedViewModel viewModel;
    private String imagemBase64 = "";

    public FragPerfil() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        er = new EnviarRequisicao(requireContext());
        cg = new CameraGaleria(requireActivity(), requireActivity().getActivityResultRegistry(), requireActivity());
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button logoff = view.findViewById(R.id.buttonLogOff);
        Button alterarSenha = view.findViewById(R.id.buttonAlterarSenha);
        Button alterarFoto = view.findViewById(R.id.buttonAlterarFotoPerfil);
        animacaoCarregandoImagem = view.findViewById(R.id.animacaoCarregandoImagem);
        logoff.setOnClickListener(v -> confirmarLogOff());
        alterarSenha.setOnClickListener(v -> abrirTelaAlterarSenha());
        alterarFoto.setOnClickListener(v -> escolherGaleriaCamera());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_frag_perfil, container, false);
        fotoPerfil = view.findViewById(R.id.imageView);
        TextView nome = view.findViewById(R.id.textNomeUsuario);
        TextView email = view.findViewById(R.id.textEmailUsuario);

        // Obter o ViewModel compartilhado
        viewModel.getUser().observe(getViewLifecycleOwner(), dados -> {

            String nomeUsuario;
            String emailUsuario;
            String urlFoto;

            if (dados.getData() == null) { // Login por requisição de get_dados
                nomeUsuario = dados.getNome_completo();
                emailUsuario = dados.getEmail();
                if (dados.getUrl_foto() != null) {
                    urlFoto = dados.getUrl_foto();
                } else {
                    urlFoto = "";
                }
            } else { // Login por requisição de login
                Data dadosData = dados.getData();
                nomeUsuario = dadosData.getNome_completo();
                emailUsuario = dadosData.getEmail();
                if (dadosData.getUrl_foto() != null) {
                    urlFoto = dadosData.getUrl_foto();
                } else {
                    urlFoto = "";
                }
            }

            // Atualiza o nome e o email
            nome.setText(nomeUsuario);
            email.setText(emailUsuario);

            // Atualiza a foto de perfil
            if (!urlFoto.isEmpty()) {
                Glide.with(requireContext()).load(urlFoto).diskCacheStrategy(DiskCacheStrategy.ALL).listener(new RequestListener<>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                        animacaoCarregandoImagem.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                        animacaoCarregandoImagem.setVisibility(View.GONE);
                        return false;
                    }
                }).into(fotoPerfil);
            }
            else {
                fotoPerfil.setImageResource(R.drawable.icon_perfil);
            }
        });

        return view;
    }

    private void deslogar() {
        if (er.possuiInternet(requireContext())) {
            Dialog dialog = dialogSaindoConta();
            User userLogin = new User();
            userLogin.setId(er.obterMemoriaInterna("idConexao"));
            Log.d("ID obtido", er.obterMemoriaInterna("idConexao"));

            // Converter o objeto User para JSON
            String userJson = gson.toJson(userLogin);

            er.post("logout", userJson, response -> {
                if (response.startsWith("Erro")) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), response, Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    });
                } else {
                    try {
                        // Processar resposta da requisição
                        User responseLogin = gson.fromJson(response, User.class);
                        if (responseLogin.getCode() == 0) {
                            Intent intentLoginCadastro = new Intent(requireContext(), TelaLogin.class);
                            dialog.dismiss();
                            startActivity(intentLoginCadastro);
                            requireActivity().finish();
                        } else {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Erro ao sair da conta", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            });
                        }
                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Erro ao processar a resposta. Tente novamente.", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        });
                    }
                }
            });
        } else {
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "Verifique sua conexão com a internet.", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void confirmarLogOff() {
        AlertDialog dialog = new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Sair da conta")
                .setMessage("Você tem certeza que deseja sair?")
                .setPositiveButton("Sair", (dialogConfirmar, which) -> {
                    deslogar();
                    })
                .setNegativeButton("Cancelar", (dialogCancelar, which) -> dialogCancelar.dismiss())
                .create();

        // Altera a cor do botão exibido
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.green2));
            negativeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.modern_red));
        });

        dialog.show();
    }

    private void abrirTelaAlterarSenha() {
        Intent telaAlterar = new Intent(getContext(), TelaAlterarSenha.class);
        startActivity(telaAlterar);
    }

    private void escolherGaleriaCamera() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_bottom_sheet);

        LinearLayout escolherCamera = dialog.findViewById(R.id.escolherCamera);
        LinearLayout escolherGaleria = dialog.findViewById(R.id.escolherGaleria);
        LinearLayout escolherUrl = dialog.findViewById(R.id.escolherUrl);

        escolherCamera.setOnClickListener(v -> cg.pedirPermissaoCamera(new CameraGaleria.CallbackCameraGaleria() {
            @Override
            public void onImageSelected(Uri uri) {
                animacaoCarregandoImagem.setVisibility(View.VISIBLE);
                dialog.dismiss();
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    String base64 = cg.converterUriParaBase64(uri);
                    requireActivity().runOnUiThread(() -> {
                        if (!requireActivity().isDestroyed() || !requireActivity().isFinishing()) {
                            imagemBase64 = base64;
                            cg.deletarImagemUri(uri);
                            upload_img(base64);
                            dialog.dismiss();
                        }
                    });
                });
                executor.shutdown();
            }
        }));

        escolherGaleria.setOnClickListener(v -> cg.pedirPermissaoGaleria(new CameraGaleria.CallbackCameraGaleria() {
            @Override
            public void onImageSelected(Uri uri) {
                animacaoCarregandoImagem.setVisibility(View.VISIBLE);
                dialog.dismiss();
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    String base64 = cg.converterUriParaBase64(uri);
                    requireActivity().runOnUiThread(() -> {
                        if (!requireActivity().isDestroyed() || !requireActivity().isFinishing()) {
                            imagemBase64 = base64;
                            upload_img(base64);
                            dialog.dismiss();
                        }
                    });
                });
                executor.shutdown();
            }
        }));
        escolherUrl.setOnClickListener(v -> {
            popupUrlImagem();
            dialog.dismiss();
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void upload_img(String imagemBase64) {
        if (er.possuiInternet(requireContext())) {
            User user = new User();
            user.setId(er.obterMemoriaInterna("idConexao"));
            user.setDestino("perfil");
            user.setFile(imagemBase64);

            // Converter o objeto User para JSON
            String userJson = gson.toJson(user);

            imagemBase64 = ""; // Limpa a base64 de imagem

            er.post("upload_img", userJson, response -> {
                if (response.startsWith("Erro")) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), response, Toast.LENGTH_LONG).show();
                        animacaoCarregandoImagem.setVisibility(View.GONE);
                    });
                } else if (response.startsWith("<html>")) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "O arquivo de imagem é muito grande", Toast.LENGTH_LONG).show();
                        animacaoCarregandoImagem.setVisibility(View.GONE);
                    });
                } else {
                    try {
                        // Processar resposta da requisição
                        User responseUpload = gson.fromJson(response, User.class);
                        if (responseUpload.getCode() == 0) {
                            try {
                                requireActivity().runOnUiThread(() -> {
                                    Glide.with(requireContext()).load(responseUpload.getUrl()).diskCacheStrategy(DiskCacheStrategy.ALL).listener(new RequestListener<Drawable>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                                            Toast.makeText(requireContext(), "Erro ao carregar a imagem na tela", Toast.LENGTH_LONG).show();
                                            animacaoCarregandoImagem.setVisibility(View.GONE);
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                                            animacaoCarregandoImagem.setVisibility(View.GONE);
                                            return false;
                                        }
                                    }).into(fotoPerfil);
                                });
                                if (viewModel.getUser().getValue().getData() == null) {
                                    viewModel.getUser().getValue().setUrl_foto(responseUpload.getUrl());
                                } else {
                                    viewModel.getUser().getValue().getData().setUrl_foto(responseUpload.getUrl());
                                }
                            } catch (Exception e) {
                                requireActivity().runOnUiThread(() -> {
                                    Toast.makeText(requireContext(), "Erro ao definir a imagem de perfil na interface gráfica", Toast.LENGTH_SHORT).show();
                                    animacaoCarregandoImagem.setVisibility(View.GONE);
                                });
                            }
                        } else if (responseUpload.getCode() == 23) { // Erro ao carregar imagem
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Erro ao carregar imagem.", Toast.LENGTH_SHORT).show();
                                animacaoCarregandoImagem.setVisibility(View.GONE);
                            });
                        }
                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Erro ao processar a resposta. Tente novamente.", Toast.LENGTH_SHORT).show();
                            animacaoCarregandoImagem.setVisibility(View.GONE);
                        });
                    }
                }
            });
        } else {
            requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Verifique sua conexão com a internet.", Toast.LENGTH_SHORT).show();
                    animacaoCarregandoImagem.setVisibility(View.GONE);
            });
        }
    }

    private void popupUrlImagem() {
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        input.setHint("Insira a URL aqui");

        AlertDialog dialog = new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Inserir URL")
                .setMessage("Insira uma URL de imagem válida abaixo")
                .setView(input)
                .setPositiveButton("Alterar", (dialogConfirmar, which) -> {
                    String url = input.getText().toString();
                    if (url.length() > 250) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "A URL não pode ter mais de 250 caracteres", Toast.LENGTH_SHORT).show());
                    } else if (!url.startsWith("http")) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "URL inválida. Deve começar com HTTP ou HTTPS", Toast.LENGTH_SHORT).show());
                    } else {
                        animacaoCarregandoImagem.setVisibility(View.VISIBLE);
                        set_img_url(url);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.green2));
            negativeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.modern_red));
        });

        dialog.show();
    }

    private void set_img_url(String url) {
        if (er.possuiInternet(requireContext())) {
            User user = new User();
            user.setId(er.obterMemoriaInterna("idConexao"));
            user.setUrl_foto(url);

            // Converter o objeto User para JSON
            String userJson = gson.toJson(user);

            er.post("set_img_url", userJson, response -> {
                if (response.startsWith("Erro")) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), response, Toast.LENGTH_LONG).show();
                        animacaoCarregandoImagem.setVisibility(View.GONE);
                    });
                } else {
                    try {
                        // Processar resposta da requisição
                        User responseUpload = gson.fromJson(response, User.class);
                        if (responseUpload.getCode() == 0) {
                            try {
                                requireActivity().runOnUiThread(() -> {
                                    Glide.with(requireContext()).load(url).diskCacheStrategy(DiskCacheStrategy.ALL).listener(new RequestListener<>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                                            Toast.makeText(requireContext(), "Erro ao carregar a imagem na tela", Toast.LENGTH_LONG).show();
                                            animacaoCarregandoImagem.setVisibility(View.GONE);
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                                            animacaoCarregandoImagem.setVisibility(View.GONE);
                                            return false;
                                        }
                                    }).into(fotoPerfil);
                                });

                                if (viewModel.getUser().getValue().getData() == null) {
                                    viewModel.getUser().getValue().setUrl_foto(url);
                                } else {
                                    viewModel.getUser().getValue().getData().setUrl_foto(url);
                                }

                            } catch (Exception e) {
                                requireActivity().runOnUiThread(() -> {
                                    Toast.makeText(requireContext(), "Erro ao definir a imagem de perfil na interface gráfica", Toast.LENGTH_SHORT).show();
                                    animacaoCarregandoImagem.setVisibility(View.GONE);
                                });
                            }
                        }
                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Erro ao processar a resposta. Tente novamente.", Toast.LENGTH_SHORT).show();
                            animacaoCarregandoImagem.setVisibility(View.GONE);
                        });
                    }
                }
            });
        } else {
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Verifique sua conexão com a internet.", Toast.LENGTH_SHORT).show();
                animacaoCarregandoImagem.setVisibility(View.GONE);
            });
        }
    }

    private Dialog dialogSaindoConta() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_saindo_logoff);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.FadeInOutMiddle;
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.show();
        return dialog;
    }
}