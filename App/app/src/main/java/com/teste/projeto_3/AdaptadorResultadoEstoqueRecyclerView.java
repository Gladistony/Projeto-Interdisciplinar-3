package com.teste.projeto_3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.teste.projeto_3.model.ResultadoRequisicaoEstoque;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorResultadoEstoqueRecyclerView extends RecyclerView.Adapter<AdaptadorResultadoEstoqueRecyclerView.MyViewHolder> {
    Context context;
    private final RecyclerViewInterface recyclerViewInterface;

    RecyclerView recyclerView; // Adicionado apenas para o smoothScroll do método adicionarRequisicaoEstoque
    List<ResultadoRequisicaoEstoque> resultado = new ArrayList<>();
    public AdaptadorResultadoEstoqueRecyclerView(Context context, RecyclerViewInterface recyclerViewInterface, RecyclerView recyclerView) {
        this.context = context;
        this.recyclerViewInterface = recyclerViewInterface;
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public AdaptadorResultadoEstoqueRecyclerView.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_resultado_requisicao_recyclerview, parent, false);

        return new AdaptadorResultadoEstoqueRecyclerView.MyViewHolder(view, recyclerViewInterface);
    }

    public void adicionarRequisicaoEstoque(ResultadoRequisicaoEstoque resultadoRequisicaoEstoque) {
        resultado.add(resultadoRequisicaoEstoque);
        notifyItemInserted(resultado.size() -1);
        recyclerView.smoothScrollToPosition(resultado.size() - 1);
    }

    public void alterarRequisicaoEstoque(String texto, int icone, int position) {
        resultado.get(position).setTextoResultado(texto);
        resultado.get(position).setIconeResultado(icone);
        notifyItemChanged(position);
    }

    public ResultadoRequisicaoEstoque getResultado(int index) {
        return resultado.get(index);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorResultadoEstoqueRecyclerView.MyViewHolder holder, int position) {
        holder.textoResultadoRequisicaoEstoque.setText(resultado.get(position).getTextoResultado());
        int icone = resultado.get(position).getIconeResultado();
        if (icone == 0) { // 0 = Sucesso
            holder.imagemSucessoEstoque.setVisibility(View.VISIBLE);
            holder.imagemFalhaEstoque.setVisibility(View.GONE);
            holder.progressBarResultadoResquisicaoEstoque.setVisibility(View.GONE);
        } else if (icone == 1) { // 1 = Falha
            holder.imagemSucessoEstoque.setVisibility(View.GONE);
            holder.imagemFalhaEstoque.setVisibility(View.VISIBLE);
            holder.progressBarResultadoResquisicaoEstoque.setVisibility(View.GONE);
        } else { // 2 = Enviando
            holder.imagemSucessoEstoque.setVisibility(View.GONE);
            holder.imagemFalhaEstoque.setVisibility(View.GONE);
            holder.progressBarResultadoResquisicaoEstoque.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
            return resultado.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imagemSucessoEstoque;
        public ImageView imagemFalhaEstoque;
        public ProgressBar progressBarResultadoResquisicaoEstoque;
        public TextView textoResultadoRequisicaoEstoque;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            imagemSucessoEstoque = itemView.findViewById(R.id.imagemResultadoRequisicaoSucessoEstoque);
            imagemFalhaEstoque = itemView.findViewById(R.id.imagemResultadoRequisicaoFalhaEstoque);
            progressBarResultadoResquisicaoEstoque = itemView.findViewById(R.id.progressBarResultadoRequisicaoEstoque);
            textoResultadoRequisicaoEstoque = itemView.findViewById(R.id.textoResultadoRequisicaoEstoque);
        }
    }
}
