package com.teste.projeto_3.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class User(
    var id: String = "",
    var status: String = "",
    var code: Int = -1,
    var message: String = "",
    var usuario: String = "",
    var senha: String = "",
    var nova_senha: String = "",
    var email: String = "",
    var nome_completo: String = "",
    var criacao: String = "",
    var ultimo_login: String = "",
    var url_foto: String? = null, // URL da foto já existente no servidor
    var restante: Double = -1.00,
    var file: String = "",
    var destino: String = "",
    var data: Data? = null,
    var estoque: MutableList<Estoque> = mutableListOf(),
    var ip: String = "",
    var url: String = "" // URL após envio de imagem
) : Parcelable {

    override fun toString(): String {
        return "User(id='$id', status='$status', code='$code', message='$message', usuario='$usuario', senha='$senha', nova_senha='$nova_senha', email='$email', nome_completo='$nome_completo', criacao='$criacao', ultimo_login='$ultimo_login', url_foto='$url_foto', restante='$restante', file='$file', destino='$destino', data='$data', estoque='$estoque', ip='$ip', url='$url')"
    }
}
