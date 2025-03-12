package com.teste.projeto_3.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Data(
    var usuario: String = "",
    var email: String = "",
    var nome_completo: String = "",
    var criacao: String = "",
    var ultimo_login: String? = null,
    var url_foto: String? = null,
    var tipo_conta: String = "",
    var estoque: MutableList<Estoque> = mutableListOf(),
) : Parcelable {

    override fun toString(): String {
        return "UserData(usuario='$usuario', email='$email', nome_completo='$nome_completo', criacao='$criacao', ultimo_login='$ultimo_login', url_foto='$url_foto', tipo_conta='$tipo_conta', estoque='$estoque')"
    }
}
