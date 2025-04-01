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
import com.teste.projeto_3.model.ResultadoRequisicao;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorResultadoRecyclerView extends RecyclerView.Adapter<AdaptadorResultadoRecyclerView.MyViewHolder> {
    Context context;
    private final RecyclerViewResultadoRequisicaoInterface recyclerViewResultadoRequisicaoInterface;

    RecyclerView recyclerView; // Adicionado apenas para o smoothScroll do método adicionarRequisicaoEstoque
    List<ResultadoRequisicao> resultado = new ArrayList<>();
    public AdaptadorResultadoRecyclerView(Context context, RecyclerViewResultadoRequisicaoInterface recyclerViewResultadoRequisicaoInterface, RecyclerView recyclerView) {
        this.context = context;
        this.recyclerViewResultadoRequisicaoInterface = recyclerViewResultadoRequisicaoInterface;
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public AdaptadorResultadoRecyclerView.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_resultado_requisicao_recyclerview, parent, false);

        return new AdaptadorResultadoRecyclerView.MyViewHolder(view, recyclerViewResultadoRequisicaoInterface);
    }

    public void adicionarRequisicaoEstoque(ResultadoRequisicao resultadoRequisicaoEstoque) {
        resultado.add(resultadoRequisicaoEstoque);
        notifyItemInserted(resultado.size() -1);
        recyclerView.smoothScrollToPosition(resultado.size() - 1);
    }

    public void removerRequisicaoEstoque(int indexPosition) {
        resultado.remove(indexPosition);
        notifyItemChanged(indexPosition);
    }

    public void alterarDetalheRequisicaoEstoque(ResultadoRequisicao resultadoRequisicaoEstoque, int position) {
        resultado.set(position, resultadoRequisicaoEstoque);
        notifyItemChanged(position);
    }

    public void alterarRequisicaoEstoque(String texto, int icone, String status, String descricaoStatus, int position) {
        resultado.get(position).setTituloResultado(texto);
        resultado.get(position).setIconeResultado(icone);
        resultado.get(position).setStatus(status);
        resultado.get(position).setDescricaoStatus(descricaoStatus);
        notifyItemChanged(position);
    }

    public ResultadoRequisicao getResultado(int index) {
        return resultado.get(index);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorResultadoRecyclerView.MyViewHolder holder, int position) {
        holder.textoResultadoRequisicaoEstoque.setText(resultado.get(position).getTituloResultado());
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

        public MyViewHolder(@NonNull View itemView, RecyclerViewResultadoRequisicaoInterface recyclerViewResultadoRequisicaoInterface) {
            super(itemView);
            imagemSucessoEstoque = itemView.findViewById(R.id.imagemResultadoRequisicaoSucessoEstoque);
            imagemFalhaEstoque = itemView.findViewById(R.id.imagemResultadoRequisicaoFalhaEstoque);
            progressBarResultadoResquisicaoEstoque = itemView.findViewById(R.id.progressBarResultadoRequisicaoEstoque);
            textoResultadoRequisicaoEstoque = itemView.findViewById(R.id.textoResultadoRequisicaoEstoque);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view){
                    if (recyclerViewResultadoRequisicaoInterface != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            recyclerViewResultadoRequisicaoInterface.onItemClickResultadoRequisicao(position);
                        }
                    }
                }
            });
        }
    }
}
