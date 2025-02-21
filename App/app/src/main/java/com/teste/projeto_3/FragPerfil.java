package com.teste.projeto_3;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.teste.projeto_3.http.EnviarRequisicao;
import com.teste.projeto_3.model.Data;
import com.teste.projeto_3.model.User;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragPerfil#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragPerfil extends Fragment {

    EnviarRequisicao er;
    private Gson gson = new Gson();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FragPerfil() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragPerfil.
     */
    // TODO: Rename and change types and number of parameters
    public static FragPerfil newInstance(String param1, String param2) {
        FragPerfil fragment = new FragPerfil();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        er = new EnviarRequisicao(requireContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button meuBotao = view.findViewById(R.id.deslogar);
        meuBotao.setOnClickListener(v -> deslogar());

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_frag_perfil, container, false);

        // Obter o ViewModel compartilhado
        SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        viewModel.getUser().observe(getViewLifecycleOwner(), dados -> {
            TextView nome = view.findViewById(R.id.textNomeUsuario);
            TextView email = view.findViewById(R.id.textEmailUsuario);
            ImageView fotoPerfil = view.findViewById(R.id.imageView);

            if (dados.getData() == null) {
            nome.setText(dados.getNome_completo());
            email.setText(dados.getEmail());;
            if (dados.getUrl_foto() != null) {
                try {
                    Picasso.get().load(dados.getUrl_foto()).into(fotoPerfil);
                } catch (Exception e) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Erro ao definir a imagem de perfil", Toast.LENGTH_SHORT).show());
                }
            }
            } else {
                Data dadosData = dados.getData();
                nome.setText(dadosData.getNome_completo());
                email.setText(dadosData.getEmail());;
                if (dadosData.getUrl_foto() != null) {
                    try {
                        Picasso.get().load(dadosData.getUrl_foto()).into(fotoPerfil);
                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Erro ao definir a imagem de perfil", Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });

        return view;
    }

    public void deslogar() {
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

}