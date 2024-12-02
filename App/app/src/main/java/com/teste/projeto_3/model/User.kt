package com.teste.projeto_3.model

class User {

    var id = ""
    var request = ""
    var usuario = ""
    var nome_completo = ""
    var email = ""
    var senha = ""

    override fun toString(): String {
        return "User(id='$id', request='$request', usuario='$usuario', senha='$senha', email='$email', nome_completo='$nome_completo')"
    }


}