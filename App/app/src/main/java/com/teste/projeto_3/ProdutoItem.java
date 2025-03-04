package com.teste.projeto_3;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.teste.projeto_3.model.Produto;

import java.util.ArrayList;

public class ProdutoItem extends AppCompatActivity implements RecyclerViewInterface{

    private AdaptadorProdutoRecyclerView adaptadorItem;
    public ArrayList<Produto> produto;
    FragStock fragStock;

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

        fragStock = new FragStock();

        produto = getIntent().getParcelableArrayListExtra("produto");

        if (!produto.isEmpty()) {
            TextView textoProdutoVazio = findViewById(R.id.textoProdutoVazio);
            textoProdutoVazio.setVisibility(View.GONE);
        }
        RecyclerView recyclerView = findViewById(R.id.recyclerViewProduto);
        adaptadorItem = new AdaptadorProdutoRecyclerView(this, produto, this);
        recyclerView.setAdapter(adaptadorItem);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }


    @Override
    public void onItemClick(int position) {
    }

    @Override
    public void onItemLongClick(int position) {
        produto.remove(position);
        adaptadorItem.notifyItemRemoved(position);
    }
}