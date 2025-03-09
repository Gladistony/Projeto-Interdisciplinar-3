package com.teste.projeto_3.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Produto(
    var code: Int = -1, // Resultado da requisição
    var id_produto: Int = -1,
    var id: Int = -1,
    var nome: String = "",
    var descricao: String = "",
    var foto: String = "",
    var quantidade: String = "",
    var data_validade: MutableList<String> = mutableListOf(),
    var preco_medio: Double = -1.00,
    var lista_quantidades: MutableList<Int> = mutableListOf(),
    var lista_precos: MutableList<Double> = mutableListOf()
) : Parcelable {
    override fun toString(): String {
        return "Produto(code='$code', id_produto='$id_produto', id='$id', nome='$nome', descricao='$descricao', foto='$foto', quantidade='$quantidade', data_validade='$data_validade', preco_medio='$preco_medio', lista_quantidades='$lista_quantidades', lista_precos='$lista_precos')"
    }
}
