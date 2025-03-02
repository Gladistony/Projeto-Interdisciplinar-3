package com.teste.projeto_3.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Estoque(
    var id: Int = -1,
    var nome: String = "",
    var descricao: String = "",
    var imagem: String = "",
    var cameras: String = "",
    var produtos: String = ""
) : Parcelable {

    override fun toString(): String {
        return "Estoque(id='$id', nome='$nome', descricao='$descricao', imagem='$imagem', cameras='$cameras', produtos='$produtos')"
    }
}
