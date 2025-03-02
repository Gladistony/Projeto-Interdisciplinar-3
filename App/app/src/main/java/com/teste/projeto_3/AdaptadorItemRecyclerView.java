package com.teste.projeto_3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdaptadorItemRecyclerView extends RecyclerView.Adapter<AdaptadorItemRecyclerView.MyViewHolder> {
    Context context;
    ArrayList<EstoqueItem> arrayEstoque;


    public AdaptadorItemRecyclerView(Context context, ArrayList<EstoqueItem> arrayEstoque) {
        this.context = context;
        this.arrayEstoque = arrayEstoque;


    }

    @NonNull
    @Override
    public AdaptadorItemRecyclerView.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_item_recyclerview, parent, false);

        return new AdaptadorItemRecyclerView.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorItemRecyclerView.MyViewHolder holder, int position) {
        holder.nomeEstoque.setText(arrayEstoque.get(position).getNomeEstoque());
        holder.descricaoEstoque.setText(arrayEstoque.get(position).getDescricaoEstoque());
        holder.quantidadeProdutoEstoque.setText(arrayEstoque.get(position).getQuantidadeItemEstoque());
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

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imagemEstoque = itemView.findViewById(R.id.imagemEstoque);
            nomeEstoque = itemView.findViewById(R.id.nomeEstoque);
            descricaoEstoque = itemView.findViewById(R.id.descricaoEstoque);
            quantidadeProdutoEstoque = itemView.findViewById(R.id.quantidadeProdutoEstoque);

        }
    }
}
