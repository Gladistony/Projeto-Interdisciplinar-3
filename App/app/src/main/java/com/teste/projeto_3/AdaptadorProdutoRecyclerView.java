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

import java.util.ArrayList;

public class AdaptadorProdutoRecyclerView extends RecyclerView.Adapter<AdaptadorProdutoRecyclerView.MyViewHolder> {
    Context context;
    ArrayList<EstoqueItem> arrayEstoque;
    private final RecyclerViewInterface recyclerViewInterface;


    public AdaptadorProdutoRecyclerView(Context context, ArrayList<EstoqueItem> arrayEstoque, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.arrayEstoque = arrayEstoque;
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
        holder.nomeProduto.setText(arrayEstoque.get(position).getNomeEstoque());
        holder.descricaoProduto.setText(arrayEstoque.get(position).getDescricaoEstoque());
        holder.quantidadeProduto.setText(arrayEstoque.get(position).getQuantidadeItemEstoque());
        Glide.with(context)
                .load(arrayEstoque.get(position).getImagemEstoque())
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Mantém cache em disco e memória
                .into(holder.imagemProduto);
        //holder.imagemEstoque.setImageResource(arrayEstoque.get(position.getImage()));
    }

    @Override
    public int getItemCount() {
        return arrayEstoque.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imagemProduto;
        public TextView nomeProduto;
        public TextView descricaoProduto;
        public TextView quantidadeProduto;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            imagemProduto = itemView.findViewById(R.id.imagemProduto);
            nomeProduto = itemView.findViewById(R.id.nomeProduto);
            descricaoProduto = itemView.findViewById(R.id.descricaoProduto);
            quantidadeProduto = itemView.findViewById(R.id.quantidadeProduto);

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

        }
    }
}
