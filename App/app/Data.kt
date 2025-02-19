package com.teste.projeto_3.model

class Data {
    var usuario: String = ""        // Nome de usuário
    var senha: String = ""          // Senha do usuário
    var email: String = ""          // E-mail do usuário
    var nome_completo: String = ""  // Nome completo do usuário
    var criacao: String = ""        // Data da criação da conta
    var ultimo_login: String = ""   // Data do último login
    var url_foto: String = ""       // Link URL da foto de perfil do usuário
}

override fun toString(): String {
    return "Data(usuario='$usuario', senha='$senha', email='$email', nome_completo='$nome_completo', criacao='$criacao', ultimo_login='$ultimo_login', url_foto='$url_foto')"
}