package com.teste.projeto_3.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Produto(
    var id: Int = -1,
    var nome: String = "",
    var descricao: String = "",
    var foto: String = "",
    var quantidade: String = "",
    var dataValidade: MutableList<String> = mutableListOf(),
    var preco_medio: Double = -1.00,
    var lista_quantidades: MutableList<Int> = mutableListOf(),
) : Parcelable {
    override fun toString(): String {
        return "Produto(id='$id', nome='$nome', descricao='$descricao', foto='$foto', quantidade='$quantidade', dataValidade='$dataValidade', preco_medio='$preco_medio', lista_quantidades='$lista_quantidades')"
    }
}
