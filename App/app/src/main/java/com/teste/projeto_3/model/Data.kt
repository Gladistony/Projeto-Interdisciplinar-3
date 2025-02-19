package com.teste.projeto_3.model

class Data {
    var usuario: String = ""        // Nome de usuário
    var email: String = ""
    var nome_completo: String = ""
    var criacao: String = ""
    var ultimo_login: String = ""
    var url_foto: String? = null // Pode ser nulo
    var tipo_conta: String = ""

    override fun toString(): String {
        return "UserData(usuario='$usuario', email='$email', nome_completo='$nome_completo', criacao='$criacao', ultimo_login='$ultimo_login', url_foto='$url_foto', tipo_conta='$tipo_conta')"
    }
}