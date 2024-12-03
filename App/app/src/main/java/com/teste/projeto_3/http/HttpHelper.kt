package com.teste.projeto_3.http

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class HttpHelper {

    fun post(json: String): String {
        // URL do servidor
        val url = "http://44.203.201.20:80/give"

        // Cabeçalho (MediaType atualizado com extensão toMediaType)
        val headerHttp = "application/json; charset=utf-8".toMediaType()

        // Cliente HTTP
        val client = OkHttpClient()

        // Body da requisição (usando toRequestBody para conteúdo)
        val body = json.toRequestBody(headerHttp)

        // Construção da requisição POST
        val request = Request.Builder().url(url).post(body).build()

        return try {
            // Enviar a requisição e processar a resposta
            val response: Response = client.newCall(request).execute()
            response.body?.string() ?: "Erro: Resposta vazia ou nula"
        } catch (e: IOException) {
            "Erro na requisição: ${e.message}"
        }
    }
}
