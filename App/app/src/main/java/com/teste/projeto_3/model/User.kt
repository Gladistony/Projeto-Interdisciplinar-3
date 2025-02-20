package com.teste.projeto_3.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/*@Entity(tableName = "users") // Define a classe como uma tabela do Room*/
class User constructor(){
    /*@PrimaryKey // Define o ID como chave primária, mas sem auto geração*/
    var id: String = ""             // ID fornecido pelo servidor

    var status: String = ""         // Status da conexão
    var code: Int = -1              // Código do resultado da requisição
    var message: String = ""        // Mensagem adicional do resultado da requisição
    var usuario: String = ""        // Nome de usuário
    var senha: String = ""          // Senha do usuário
    var email: String = ""          // E-mail do usuário
    var nome_completo: String = ""  // Nome completo do usuário
    var criacao: String = ""        // Data da criação da conta
    var ultimo_login: String = ""   // Data do último login
    var url_foto: String = ""       // Link URL da foto de perfil do usuário
    var restante: Double = -1.00    // Tempo (em segundos) restante para tentar novamente após errar a senha
    var file: String = ""           // Imagem
    var destino: String = ""        // Objetivo da imagem ("perfil" ou "post")
    var data: Data? = null // Agora temos um objeto para armazenar os dados internos
    var ip: String = ""

    override fun toString(): String {
        return "User(id='$id', status='$status', code='$code', message='$message', usuario='$usuario', senha='$senha', email='$email', nome_completo='$nome_completo', criacao='$criacao', ultimo_login='$ultimo_login', url_foto='$url_foto', restante='$restante', file='$file', destino='$destino', data='$data')"
    }
}
