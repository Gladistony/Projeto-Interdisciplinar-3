package com.teste.projeto_3;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.teste.projeto_3.model.User;

public class TelaPrincipalActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private Fragment perfilFrag;
    private Fragment inicioFrag;
    private Fragment produtosFrag;
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
        Intent dados = getIntent();
        /*
        User user = dados.getParcelableExtra("dados"); // Dados da tela de login ou get_dados
        SharedViewModel viewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        viewModel.setUser(user);
         */

        inicioFrag = new FragInicio();
        perfilFrag = new FragPerfil();
        produtosFrag = new FragProdutos();

        atualFrag = inicioFrag;
        fragmentManager.beginTransaction()
                .add(R.id.fragmentContainerView, inicioFrag, "Inicio")
                .add(R.id.fragmentContainerView, produtosFrag, "Produtos").hide(produtosFrag)
                .add(R.id.fragmentContainerView, perfilFrag, "Perfil").hide(perfilFrag)
                .commit();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragmentSelecionado = null;

            if (item.getItemId() == R.id.fragInicio) {
                fragmentSelecionado = inicioFrag;
            } else if (item.getItemId() == R.id.fragPerfil) {
                fragmentSelecionado = perfilFrag;
            } else if (item.getItemId() == R.id.fragProdutos) {
                fragmentSelecionado = produtosFrag;
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
