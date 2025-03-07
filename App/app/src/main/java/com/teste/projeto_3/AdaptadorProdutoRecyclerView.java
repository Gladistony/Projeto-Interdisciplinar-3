package com.teste.projeto_3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.teste.projeto_3.model.Estoque;
import com.teste.projeto_3.model.Produto;

import java.util.ArrayList;

public class AdaptadorProdutoRecyclerView extends RecyclerView.Adapter<AdaptadorProdutoRecyclerView.MyViewHolder> {
    Context context;
    ArrayList<Produto> produto;
    private final RecyclerViewInterface recyclerViewInterface;



    public AdaptadorProdutoRecyclerView(Context context, ArrayList<Produto> produto, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.produto = produto;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public AdaptadorProdutoRecyclerView.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_produto_item_recyclerview, parent, false);

        return new AdaptadorProdutoRecyclerView.MyViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorProdutoRecyclerView.MyViewHolder holder, int position) {
        if (!produto.get(position).getFoto().isEmpty()) {
            Glide.with(context)
                    .load(produto.get(position).getFoto())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imagemProduto);
        }
        holder.nomeProduto.setText(produto.get(position).getNome());
        holder.descricaoProduto.setText(produto.get(position).getDescricao());
        holder.quantidadeProduto.setText("Quantidade: " + produto.get(position).getQuantidade());
        holder.precoMedioProduto.setText("Preço médio do produto: " + Double.toString(produto.get(position).getPreco_medio()));
        if (!produto.get(position).getLista_precos().isEmpty()) {
            ArrayList<Double> precos = new ArrayList<>(produto.get(position).getLista_precos());
            int precosLista = produto.get(position).getLista_precos().size();
            Double totalPrecos = 0.0;
            for (int i = 0; i < precosLista; i++) {
                totalPrecos += precos.get(i);
            }
            holder.precoProduto.setText("Preço: " + totalPrecos);
        }
        else {
            holder.precoProduto.setText("Preço: ainda não fornecido");
        }
    }

    @Override
    public int getItemCount() {
        return produto.size();
    }

    public void adicionarArrayProduto(Produto novoProduto) {
        produto.add(novoProduto);
        notifyItemInserted(produto.size() - 1);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imagemProduto;
        public TextView nomeProduto;
        public TextView descricaoProduto;
        public TextView quantidadeProduto;
        public TextView precoProduto;
        public TextView precoMedioProduto;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            imagemProduto = itemView.findViewById(R.id.imagemProduto);
            nomeProduto = itemView.findViewById(R.id.nomeProduto);
            descricaoProduto = itemView.findViewById(R.id.descricaoProduto);
            quantidadeProduto = itemView.findViewById(R.id.quantidadeProduto);
            precoProduto = itemView.findViewById(R.id.precoProduto);
            precoMedioProduto = itemView.findViewById(R.id.precoMedioProduto);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view){
                    if (recyclerViewInterface != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            recyclerViewInterface.onItemClick(position);
                        }
                    }
                }
            });

            /*
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (recyclerViewInterface != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            recyclerViewInterface.onItemLongClick(position);
                        }
                    }
                    return true;
                }
            });

             */

        }
    }
}
