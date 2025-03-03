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

public class AdaptadorEstoqueRecyclerView extends RecyclerView.Adapter<AdaptadorEstoqueRecyclerView.MyViewHolder> {
    Context context;
    ArrayList<EstoqueItem> arrayEstoque;
    private final RecyclerViewInterface recyclerViewInterface;


    public AdaptadorEstoqueRecyclerView(Context context, ArrayList<EstoqueItem> arrayEstoque, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.arrayEstoque = arrayEstoque;
        this.recyclerViewInterface = recyclerViewInterface;
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
        holder.nomeEstoque.setText(arrayEstoque.get(position).getNomeEstoque());
        holder.descricaoEstoque.setText(arrayEstoque.get(position).getDescricaoEstoque());
        holder.quantidadeProdutoEstoque.setText(arrayEstoque.get(position).getQuantidadeItemEstoque());
        Glide.with(context)
                .load(arrayEstoque.get(position).getImagemEstoque())
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Mantém cache em disco e memória
                .into(holder.imagemEstoque);
        //holder.imagemEstoque.setImageResource(arrayEstoque.get(position.getImage()));
    }

    @Override
    public int getItemCount() {
        return arrayEstoque.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imagemEstoque;
        public TextView nomeEstoque;
        public TextView descricaoEstoque;
        public TextView quantidadeProdutoEstoque;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            imagemEstoque = itemView.findViewById(R.id.imagemEstoque);
            nomeEstoque = itemView.findViewById(R.id.nomeEstoque);
            descricaoEstoque = itemView.findViewById(R.id.descricaoEstoque);
            quantidadeProdutoEstoque = itemView.findViewById(R.id.quantidadeProdutoEstoque);

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
