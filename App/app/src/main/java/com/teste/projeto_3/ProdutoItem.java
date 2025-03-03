package com.teste.projeto_3;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ProdutoItem extends AppCompatActivity implements RecyclerViewInterface{

    ArrayList<EstoqueItem> arrayEstoque = new ArrayList<>();

    private AdaptadorProdutoRecyclerView adaptadorItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_produto_item);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerViewProduto);
        criarListaEstoque();
        adaptadorItem = new AdaptadorProdutoRecyclerView(this, arrayEstoque, this);
        recyclerView.setAdapter(adaptadorItem);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void criarListaEstoque() {
        String[] nomeEstoque = new String[] {"a", "b", "c", "d", "e", "f", "g", "h"};
        String[] descricaoEstoque = new String[] {"aaaa", "bbbb", "cccc", "dddd", "eeee", "ffff", "gggg", "hhhh"};
        String[] quantidadeEstoque = new String[] {"10", "20", "30", "40", "50", "60", "70", "80"};
        String[] imagemEstoque = new String[] {
                "https://plus.unsplash.com/premium_photo-1711434824963-ca894373272e?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MXx8aW1hZ2VtJTIwZGUlMjBmdW5kbyUyMGJvbml0YXxlbnwwfHwwfHx8MA%3D%3D",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRs5yf9d35OUjrxlQIpIhXop9rc9DN749axHenYIPlMk-aAzQrRDy94Ciins7zcfZfhE6o",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRs5yf9d35OUjrxlQIpIhXop9rc9DN749axHenYIPlMk-aAzQrRDy94Ciins7zcfZfhE6o",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRs5yf9d35OUjrxlQIpIhXop9rc9DN749axHenYIPlMk-aAzQrRDy94Ciins7zcfZfhE6o",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRs5yf9d35OUjrxlQIpIhXop9rc9DN749axHenYIPlMk-aAzQrRDy94Ciins7zcfZfhE6o",
                "https://s2-techtudo.glbimg.com/L9wb1xt7tjjL-Ocvos-Ju0tVmfc=/0x0:1200x800/984x0/smart/filters:strip_icc()/i.s3.glbimg.com/v1/AUTH_08fbf48bc0524877943fe86e43087e7a/internal_photos/bs/2023/q/l/TIdfl2SA6J16XZAy56Mw/canvaai.png",
                "https://d1muf25xaso8hp.cloudfront.net/https://img.criativodahora.com.br/2024/04/criativo-660da8df17c75img-2024-04-03660da8df17c7a.jpg?w=1000&h=&auto=compress&dpr=1&fit=max",
                "https://thumbs.dreamstime.com/b/imagem-de-fundo-bonita-do-c%C3%A9u-da-natureza-64743176.jpg"
        };

        for (int i = 0; i < nomeEstoque.length; i++) {
            arrayEstoque.add(new EstoqueItem(nomeEstoque[i], descricaoEstoque[i], quantidadeEstoque[i], imagemEstoque[i]));
        }

    }

    @Override
    public void onItemClick(int position) {
    }

    @Override
    public void onItemLongClick(int position) {
        arrayEstoque.remove(position);
        adaptadorItem.notifyItemRemoved(position);
    }
}