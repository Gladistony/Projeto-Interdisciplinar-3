package com.teste.projeto_3;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.teste.projeto_3.model.Estoque;
import com.teste.projeto_3.model.Produto;
import com.teste.projeto_3.model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TelaPrincipalActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;

    SharedViewModel viewModel;
    private Fragment perfilFrag;
    private Fragment inicioFrag;
    private Fragment stockFrag;
    private Fragment atualFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_principal_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Percorre todos os menus (botões) e desativa o tooltip (caixa de texto flutuante ao pressionar)
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            MenuItem item = bottomNavigationView.getMenu().getItem(i);
            View view = findViewById(item.getItemId());
            if (view != null) {
                view.setOnLongClickListener(v -> true);
            }
        }

        fragmentManager = getSupportFragmentManager();

        // Cria o objeto para compartilhar os dados do login para os fragments
        //Intent dados = getIntent();
        Produto produto1 = new Produto();
        produto1.setNome("Maçã");
        produto1.setDescricao("Maçãs agentinas e fuji");
        produto1.setFoto("https://superprix.vteximg.com.br/arquivos/ids/175207/Maca-Argentina--1-unidade-aprox.-200g-.png?v=636294203590200000");
        produto1.setDataValidade(new ArrayList<>(Arrays.asList("2025-03-29 até 2025-03-30")));
        produto1.setQuantidade("50");
        produto1.setPreco_medio(1.80);

        Produto produto2 = new Produto();
        produto2.setNome("Banana");
        produto2.setDescricao("Banana da terra");
        produto2.setFoto("");
        produto2.setDataValidade(new ArrayList<>(Arrays.asList("2025-03-28")));
        produto2.setQuantidade("100");

        Estoque estoque1 = new Estoque();
        estoque1.setNome("Frutas");
        estoque1.setDescricao("Estoque de frutas da empresa tal");
        estoque1.setImagem("https://media.istockphoto.com/id/529664572/photo/fruit-background.jpg?s=612x612&w=0&k=20&c=K7V0rVCGj8tvluXDqxJgu0AdMKF8axP0A15P-8Ksh3I=");

        estoque1.setProdutos(new ArrayList<>(Arrays.asList(produto1, produto2)));

        User user = new User();
        user.setEstoque(Arrays.asList(estoque1));

        viewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        viewModel.setUser(user);
        /*
        User user = dados.getParcelableExtra("dados"); // Dados da tela de login ou get_dados
        SharedViewModel viewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        viewModel.setUser(user);
         */

        inicioFrag = new FragInicio();
        perfilFrag = new FragPerfil();
        stockFrag = new FragStock();

        atualFrag = inicioFrag;
        fragmentManager.beginTransaction()
                .add(R.id.fragmentContainerView, inicioFrag, "Inicio")
                .add(R.id.fragmentContainerView, stockFrag, "Stock").hide(stockFrag)
                .add(R.id.fragmentContainerView, perfilFrag, "Perfil").hide(perfilFrag)
                .commit();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragmentSelecionado = null;

            if (item.getItemId() == R.id.fragInicio) {
                fragmentSelecionado = inicioFrag;
            } else if (item.getItemId() == R.id.fragPerfil) {
                fragmentSelecionado = perfilFrag;
            } else if (item.getItemId() == R.id.fragStock) {
                fragmentSelecionado = stockFrag;
            }

            if (fragmentSelecionado != atualFrag) {
                trocarFragmento(fragmentSelecionado);
            }

            return true;
        });
    }

    private void trocarFragmento(Fragment novoFragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.hide(atualFrag);
        transaction.show(novoFragment);
        transaction.commit();
        atualFrag = novoFragment;
    }
}
