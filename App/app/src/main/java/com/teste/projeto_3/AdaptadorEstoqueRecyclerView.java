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
import java.util.List;

public class AdaptadorEstoqueRecyclerView extends RecyclerView.Adapter<AdaptadorEstoqueRecyclerView.MyViewHolder> {
    Context context;
    private final RecyclerViewInterface recyclerViewInterface;
    List<Estoque> estoque;
    public AdaptadorEstoqueRecyclerView(Context context, RecyclerViewInterface recyclerViewInterface, List<Estoque> estoque) {
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
        if (!estoque.get(position).getImagem().isEmpty()) {
            try {
                Glide.with(context)
                        .load(estoque.get(position).getImagem())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.imagemEstoque);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            // Carregamento desnecessário do drawable com o mesmo valor padrão. Verificar depois.
            holder.imagemEstoque.setImageResource(R.drawable.icon_image);
        }
    }

    @Override
    public int getItemCount() {
            return estoque.size();
    }

    public void removerEstoque(int position) {
        estoque.remove(position);
        notifyItemRemoved(position);
    }

    public void alterarImagemEstoque(int position, String urlImagem) {
        estoque.get(position).setImagem(urlImagem);
        notifyItemChanged(position);
    }

    public void adicionarArrayEstoque(Estoque novoEstoque) {
        estoque.add(novoEstoque);
        notifyItemInserted(estoque.size() - 1);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imagemEstoque;
        public TextView nomeEstoque;
        public TextView descricaoEstoque;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            imagemEstoque = itemView.findViewById(R.id.imagemEstoque);
            nomeEstoque = itemView.findViewById(R.id.nomeEstoque);
            descricaoEstoque = itemView.findViewById(R.id.descricaoEstoque);

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
