package com.teste.projeto_3.http
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class HttpHelper {

    fun post(method: String, json: String): String {
        // URL do servidor
        val url = "http://44.203.201.20/$method/"

        // Tipo de mídia JSON
        val JSON = "application/json; charset=utf-8".toMediaType()

        // Cliente HTTP
        val client = OkHttpClient()

        // Corpo da requisição com o JSON
        val body = json.toRequestBody(JSON)

        // Construção da requisição POST
        val request = Request.Builder()
            .url(url)
            .post(body) // Define o método como POST
            .build() // Sem cabeçalhos adicionais

        return try {
            // Executa a requisição e processa a resposta
            val response: Response = client.newCall(request).execute()
            response.body?.string() ?: "Erro: Resposta vazia ou nula"
        } catch (e: IOException) {
            // Retorna o erro caso ocorra uma falha
            "Erro na requisição: ${e.message}"
        }
    }

    fun get(method: String, json: String): String {
        // URL do servidor
        val url = "http://44.203.201.20/$method?data=$json" /// Verificar se aceita o corpo do JSON aqui no método GET depois

        // Cliente HTTP
        val client = OkHttpClient()

        // Construção da requisição POST
        val request = Request.Builder()
            .url(url)
            .get() // Define o método como GET
            .build() // Sem cabeçalhos adicionais

        return try {
            // Executa a requisição e processa a resposta
            val response: Response = client.newCall(request).execute()
            response.body?.string() ?: "Erro: Resposta vazia ou nula"
        } catch (e: IOException) {
            // Retorna o erro caso ocorra uma falha
            "Erro na requisição: ${e.message}"
        }
    }
}
