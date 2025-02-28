package com.teste.projeto_3.http
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.io.File
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
            if (e.message?.startsWith("Failed to connect") == true) {
                "Erro de conexão ao servidor."
            } else {
                "Erro na requisição: ${e.message}"
            }
        }
    }
    /*
    fun post(method: String, json: String, fileSend: File): String {
        // URL do servidor
        val url = "http://44.203.201.20/$method/"

        // Cliente HTTP
        val client = OkHttpClient()

        val fileCompressed = compressImage(fileSend)

        // Define explicitamente o tipo MIME como imagem JPEG
        val fileBody = fileCompressed.asRequestBody("image/jpeg".toMediaType())

        // Criação do corpo da requisição multipart
        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", fileSend.name, fileBody) // Arquivo de imagem
            .addFormDataPart("json", json) // JSON como string
            .build()

        // Construção da requisição POST
        val request = Request.Builder()
            .url(url)
            .post(multipartBody) // Define o método como POST e inclui o multipartBody
            .build()

        return try {
            // Executa a requisição e processa a resposta
            val response: Response = client.newCall(request).execute()
            response.body?.string() ?: "Erro: Resposta vazia ou nula"
        } catch (e: IOException) {
            // Retorna o erro caso ocorra uma falha
            if (e.message?.startsWith("Failed to connect") == true) {
                "Erro de conexão ao servidor."
            } else {
                "Erro na requisição: ${e.message}"
            }
        }
    }
     */

    fun ativarConta(usuario: String, senha: String): String {
        // URL do servidor
        val url = "http://44.203.201.20/ativar/$usuario/$senha"

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
            if (e.message?.startsWith("Failed to connect") == true) {
                "Erro de conexão ao servidor."
            } else {
                "Erro na requisição: ${e.message}"
            }
        }
    }

    fun get(method: String, json: String): String { // testar se funciona ainda
        val url = "http://44.203.201.20/$method/"

        // Tipo de mídia JSON
        val JSON = "application/json; charset=utf-8".toMediaType()

        // Cliente HTTP
        val client = OkHttpClient()

        // Corpo da requisição
        val body = json.toRequestBody(JSON)

        // Construção da requisição GET (verificar se funciona)
        val request = Request.Builder()
            .url(url)
            .method("GET", body)
            .build()

        return try {
            val response: Response = client.newCall(request).execute()
            response.body?.string() ?: "Erro: Resposta vazia ou nula"
        } catch (e: IOException) {
            // Retorna o erro caso ocorra uma falha
            if (e.message?.startsWith("Failed to connect") == true) {
                "Erro de conexão ao servidor."
            } else {
                "Erro na requisição: ${e.message}"
            }
        }
        }

    fun compressImage(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        val outputStream = ByteArrayOutputStream()

        // Compacta a imagem para 80% da qualidade original
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)

        val compressedFile = File(file.parent, "compressed_${file.name}")
        compressedFile.outputStream().use {
            it.write(outputStream.toByteArray())
        }
        return compressedFile
    }
    }
