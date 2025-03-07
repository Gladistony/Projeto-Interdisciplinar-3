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

public class AdaptadorEstoqueRecyclerView extends RecyclerView.Adapter<AdaptadorEstoqueRecyclerView.MyViewHolder> {
    Context context;
    private final RecyclerViewInterface recyclerViewInterface;
    ArrayList<Estoque> estoque;
    public AdaptadorEstoqueRecyclerView(Context context, RecyclerViewInterface recyclerViewInterface, ArrayList<Estoque> estoque) {
        this.context = context;
        this.recyclerViewInterface = recyclerViewInterface;
        this.estoque = estoque;
    }

    @NonNull
    @Override
    public AdaptadorEstoqueRecyclerView.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_estoque_item_recyclerview, parent, false);

        return new AdaptadorEstoqueRecyclerView.MyViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorEstoqueRecyclerView.MyViewHolder holder, int position) {
        holder.nomeEstoque.setText(estoque.get(position).getNome());
        holder.descricaoEstoque.setText(estoque.get(position).getDescricao());
        if (!estoque.get(position).getProdutos().isEmpty()) {
            holder.quantidadeProdutosCadastradosEstoque.setText("Quantidade de produtos cadastrados: " + estoque.get(position).getProdutos().size());
        } else {
            holder.quantidadeProdutosCadastradosEstoque.setText("Nenhum produto cadastrado neste Stock");
        }
        if (!estoque.get(position).getImagem().isEmpty()) {
            Glide.with(context)
                    .load(estoque.get(position).getImagem())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imagemEstoque);
        }
    }

    @Override
    public int getItemCount() {
            return estoque.size();
    }

    public void adicionarArrayEstoque(Estoque novoEstoque) {
        estoque.add(novoEstoque);
        notifyItemInserted(estoque.size() - 1);
    }

    public void notificarNovoProdutoEstoque(Produto novoProduto, int position) {
        estoque.get(position).getProdutos().add(novoProduto);
        notifyItemChanged(position);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imagemEstoque;
        public TextView nomeEstoque;
        public TextView descricaoEstoque;
        public TextView quantidadeProdutosCadastradosEstoque;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            imagemEstoque = itemView.findViewById(R.id.imagemEstoque);
            nomeEstoque = itemView.findViewById(R.id.nomeEstoque);
            descricaoEstoque = itemView.findViewById(R.id.descricaoEstoque);
            quantidadeProdutosCadastradosEstoque = itemView.findViewById(R.id.quantidadeProdutoEstoque);

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
