package com.teste.projeto_3.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users") // Define a classe como uma tabela do Room
class User {
    @PrimaryKey // Define o ID como chave primária, mas sem auto geração
    var id: String = ""             // ID fornecido pelo servidor
    var request: String = ""        // Request é configurado no uso
    var usuario: String = ""        // Nome de usuário
    var nome_completo: String = ""  // Nome completo do usuário
    var email: String = ""          // E-mail do usuário
    var senha: String = ""          // Senha do usuário

    override fun toString(): String {
        return "User(id='$id', request='$request', usuario='$usuario', nome_completo='$nome_completo', email='$email', senha='$senha')"
    }
}

