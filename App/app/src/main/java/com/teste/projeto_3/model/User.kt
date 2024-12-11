package com.teste.projeto_3.model

class User {
    var id: String = ""             // ID
    var code: Int = -1             // code
    var request: String = ""             // Request é configurado no uso
    var usuario: String = ""             // Nome de usuário
    var nome_completo: String = ""       // Nome completo do usuário
    var email: String = ""               // E-mail do usuário
    var senha: String = ""               // Senha do usuário
    var message: String = ""               // message

    override fun toString(): String {
        return "User(id='$id', code='$code', request='$request', usuario='$usuario', nome_completo='$nome_completo', email='$email', senha='$senha', message='$message')"
    }
}
