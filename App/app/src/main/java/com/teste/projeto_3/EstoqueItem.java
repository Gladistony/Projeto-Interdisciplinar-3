package com.teste.projeto_3;

import android.widget.ImageView;

public class EstoqueItem {
    public String nomeEstoque;
    public String descricaoEstoque;
    public String quantidadeItemEstoque;
    public ImageView imagemEstoque;

    public EstoqueItem(String nomeEstoque, String descricaoEstoque, String quantidadeItemEstoque) {
        this.nomeEstoque = nomeEstoque;
        this.descricaoEstoque = descricaoEstoque;
        this.quantidadeItemEstoque = quantidadeItemEstoque;
        //this.imagemEstoque = imagemEstoque;
    }

    public String getNomeEstoque() {
        return nomeEstoque;
    }

    public String getDescricaoEstoque() {
        return descricaoEstoque;
    }

    public String getQuantidadeItemEstoque() {
        return quantidadeItemEstoque;
    }

    public ImageView getImagemEstoque() {
        return imagemEstoque;
    }
}
