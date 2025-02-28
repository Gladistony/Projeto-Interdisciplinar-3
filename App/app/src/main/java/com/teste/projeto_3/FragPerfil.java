package com.teste.projeto_3;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.teste.projeto_3.http.EnviarRequisicao;
import com.teste.projeto_3.model.Data;
import com.teste.projeto_3.model.User;

public class FragPerfil extends Fragment {
    EnviarRequisicao er;
    private final Gson gson = new Gson();
    private ImageView fotoPerfil;
    private CameraGaleria cg;
    private Uri uriFotoPerfil;

    // Variáveis com métodos individuais da classe para câmera e galeria
    public final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    uriFotoPerfil = cg.getImagemUri();
                    if (uriFotoPerfil != null) {
                        try {
                            //Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), uriFotoPerfil);
                            String imagemBase64 = cg.converterUriParaBase64(uriFotoPerfil);
                            cg.deletarImagemUri(uriFotoPerfil);
                            upload_img(imagemBase64);
                        } catch (Exception e) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Erro ao processar a alteração da foto de perfil", Toast.LENGTH_SHORT).show());
                        }
                    }
                } else {
                    requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Erro ao obter a imagem", Toast.LENGTH_SHORT).show()
                    );
                }
            });
    public final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    uriFotoPerfil = result.getData().getData();
                    if (uriFotoPerfil != null) {
                        try {
                            //Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imagemSelecionada);
                            String imagemBase64 = cg.converterUriParaBase64(uriFotoPerfil);
                            upload_img(imagemBase64);
                        } catch (Exception e) {
                            requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Erro ao carregar imagem", Toast.LENGTH_SHORT).show());
                        }
                    }
                }
            });
    public final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    cg.abrirGaleria();
                } else {
                    requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Permissão necessária para acessar a galeria.", Toast.LENGTH_SHORT).show());
                }
            });

    public FragPerfil() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        er = new EnviarRequisicao(requireContext());
        cg = new CameraGaleria(requireContext(), cameraLauncher, galleryLauncher, requestPermissionLauncher);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button logoff = view.findViewById(R.id.buttonLogOff);
        Button alterarSenha = view.findViewById(R.id.buttonAlterarSenha);
        Button alterarFoto = view.findViewById(R.id.buttonAlterarFotoPerfil);
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
        SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        viewModel.getUser().observe(getViewLifecycleOwner(), dados -> {

            String nomeUsuario;
            String emailUsuario;
            String urlFoto;

            if (dados.getData() == null) { // Login por requisição de get_dados
                nomeUsuario = dados.getNome_completo();
                emailUsuario = dados.getEmail();
                urlFoto = dados.getUrl_foto();
            } else { // Login por requisição de login
                Data dadosData = dados.getData();
                nomeUsuario = dadosData.getNome_completo();
                emailUsuario = dadosData.getEmail();
                urlFoto = dadosData.getUrl_foto();
            }

            // Atualiza o nome e o email
            nome.setText(nomeUsuario);
            email.setText(emailUsuario);

            // Atualiza a foto de perfil
            if (urlFoto != null) {
                try {
                    Picasso.get().load(urlFoto).into(fotoPerfil);
                } catch (Exception e) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Erro ao definir a imagem de perfil", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });

        return view;
    }

    private void deslogar() {
        if (er.possuiInternet(requireContext())) {
            User userLogin = new User();
            userLogin.setId(er.obterMemoriaInterna("idConexao"));

            // Converter o objeto User para JSON
            String userJson = gson.toJson(userLogin);

            er.post("logout", userJson, response -> {
                if (response.startsWith("Erro")) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), response, Toast.LENGTH_LONG).show()
                    );
                } else {
                    try {
                        // Processar resposta da requisição
                        User responseLogin = gson.fromJson(response, User.class);
                        if (responseLogin.getCode() == 0) {
                            Intent intentLoginCadastro = new Intent(requireContext(), LoginCadastro.class);
                            startActivity(intentLoginCadastro);
                            requireActivity().finish(); // Fechar Activity associada
                        } else {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Erro ao deslogar", Toast.LENGTH_SHORT).show()
                            );
                        }
                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Erro ao processar a resposta. Tente novamente.", Toast.LENGTH_SHORT).show()
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

    private void confirmarLogOff() {
        AlertDialog dialog = new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Sair da conta")
                .setMessage("Você tem certeza que deseja sair?")
                .setPositiveButton("Sair", (dialogConfirmar, which) -> deslogar())
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

            TextView escolherGaleria = dialog.findViewById(R.id.chooseGallery);
            TextView escolherCamera = dialog.findViewById(R.id.chooseCamera);

            escolherGaleria.setOnClickListener(v -> {
                cg.pedirPermissaoGaleria();
                dialog.dismiss();
            });
            escolherCamera.setOnClickListener(v -> {
                cg.pedirPermissaoCamera();
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

            er.post("upload_img", userJson, response -> {
                if (response.startsWith("Erro")) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), response, Toast.LENGTH_LONG).show()
                    );
                } else {
                    try {
                        // Processar resposta da requisição
                        User responseUpload = gson.fromJson(response, User.class);
                        if (responseUpload.getCode() == 0) {
                            try {
                                requireActivity().runOnUiThread(() -> Picasso.get().load(responseUpload.getUrl()).into(fotoPerfil));
                            } catch (Exception e) {
                                requireActivity().runOnUiThread(() ->
                                        Toast.makeText(requireContext(), "Erro ao definir a imagem de perfil na interface gráfica", Toast.LENGTH_SHORT).show()
                                );
                            }
                        }
                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Erro ao processar a resposta. Tente novamente.", Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            });
        } else {
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "Verifique sua conexão com a internet.", Toast.LENGTH_SHORT).show());
        }
    }
}