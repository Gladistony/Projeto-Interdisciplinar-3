package com.teste.projeto_3.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class EstoqueDataValidadeString(
    var code: Int = -1, // Resultado da requisição
    var id: String = "",
    var id_estoque: String = "",
    var id_produto: String = "",
    var quantidade: Int = -1,
    var data_validade: String = "",
    var preco: Double = -1.0
) : Parcelable {

    override fun toString(): String {
        return "EstoqueDataValidadeString(id='$id', id_estoque='$id_estoque', id_produto='$id_produto', quantidade='$quantidade',preco='$preco', data_validade='$data_validade')"
    }
}
