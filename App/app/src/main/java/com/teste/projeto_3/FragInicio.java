package com.teste.projeto_3;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.teste.projeto_3.model.Data;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragInicio#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragInicio extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FragInicio() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragInicio.
     */
    // TODO: Rename and change types and number of parameters
    public static FragInicio newInstance(String param1, String param2) {
        FragInicio fragment = new FragInicio();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_frag_inicio, container, false);

        /*
        // Obter o ViewModel compartilhado
        SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        viewModel.getUser().observe(getViewLifecycleOwner(), dados -> {
            if (dados.getData() == null) {
                TextView bemVindo = view.findViewById(R.id.bemVindo);
                bemVindo.setText("Bem vindo(a), " + dados.getNome_completo());
            }
            else {
                Data dadosData = dados.getData();
                TextView bemVindo = view.findViewById(R.id.bemVindo);
                bemVindo.setText("Bem vindo(a), " + dadosData.getNome_completo());
            }
        });

         */

        return view;
    }
}