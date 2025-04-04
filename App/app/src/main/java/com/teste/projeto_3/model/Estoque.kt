package com.teste.projeto_3.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Estoque(
    var code: Int = -1, // Resultado da requisição
    var id: String = "",
    var nome: String = "",
    var descricao: String = "",
    var imagem: String = "",
    var cameras: MutableList<Camera> = mutableListOf(),
    var produtos: MutableList<Produto> = mutableListOf(),

    var url_foto: String = "", // Para requisição de trocar a imagem do estoque
    var id_estoque: String = "",
    var id_produto: String = "",
    var quantidade: Int = -1,
    var data_validade: MutableList<String> = mutableListOf(),
    var preco: Double = -1.0,

    var status: String = "",
    var message: String = ""
) : Parcelable {

    override fun toString(): String {
        return "Estoque(id='$id', nome='$nome', descricao='$descricao', imagem='$imagem', cameras='$cameras', produtos='$produtos', url_foto='$url_foto', id_estoque='$id_estoque', id_produto='$id_produto', quantidade='$quantidade',preco='$preco', data_validade='$data_validade', status='$status', message='$message')"
    }
}
