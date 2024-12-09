package com.teste.projeto_3.model

class ServerStatus {
    var status: String = ""             // Status
    var code: Int = -1                  // Código do status
    var data: String = ""             // Informações adicionais
    var message: String = ""             // Mensagem do resultado da conexão

    override fun toString(): String {
        return "User(status='$status', code='$code', data='$data', message='$message')"
    }
}