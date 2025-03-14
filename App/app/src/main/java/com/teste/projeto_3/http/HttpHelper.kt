package com.teste.projeto_3.http

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit


class HttpHelper {

    private val timeout: Long = 20

    fun post(method: String, json: String): String {
        // URL do servidor
        val url = "http://192.168.1.106:7300/$method/"

        // Tipo de mídia JSON
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()

        // Cliente HTTP
        val client = OkHttpClient.Builder()
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .writeTimeout(timeout, TimeUnit.SECONDS)
            .readTimeout(timeout, TimeUnit.SECONDS)
            .build()

        // Corpo da requisição com o JSON
        val body = json.toRequestBody(jsonMediaType)

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
            if (e.message?.contains("to connect") == true) {
                "Erro de conexão ao servidor."
            } else {
                "Erro na requisição: ${e.message}"
            }
        }
    }

    fun get(method: String, body: String): String {
        // URL do servidor
        val url = "http://192.168.1.106:7300/$method/$body"

        // Cliente HTTP
        val client = OkHttpClient.Builder()
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .writeTimeout(timeout, TimeUnit.SECONDS)
            .readTimeout(timeout, TimeUnit.SECONDS)
            .build()

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
            if (e.message?.contains("to connect") == true) {
                "Erro de conexão ao servidor."
            } else {
                "Erro na requisição: ${e.message}"
            }
        }
    }
    }
