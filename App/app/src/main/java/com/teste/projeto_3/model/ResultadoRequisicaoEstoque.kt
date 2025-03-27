package com.teste.projeto_3.model

class ResultadoRequisicaoEstoque(
    var nomeEstoque: String = "",           // Nome do estoque
    var descricaoEstoque: String = "",
    var tituloResultado: String = "",       // Resultado da requisição para o RecyclerView
    var iconeResultado: Int = 0,            // 0 = sucesso, 1 = falha, 2 = enviandoRequisicao
    var status: String = "",                // Status da requisição ("erro" ou "sucesso")
    var descricaoStatus: String = "",       // Descrição do status da operação
    var metodoDeEnvio: String = "",         // Nome da função que executou a requisição (ex.: apagar_estoque, criar_estoque...)
    var imagemUrl: String = "",             // URL da imagem, caso esta já tenha sido enviada com sucesso ao servidor
    var imagemBase64: String = "",          // Imagem em Base64, caso ocorra falha ao fazer o upload
    var idEstoque: String = "",
    var positionEstoqueLista: Int = 0
) {
    override fun toString(): String {
        return "ResultadoRequisicaoEstoque(nomeEstoque='$nomeEstoque', descricaoEstoque='$descricaoEstoque', textoResultado='$tituloResultado', iconeResultado=$iconeResultado, status='$status', descricaoStatus='$descricaoStatus', metodoDeEnvio='$metodoDeEnvio', imagemUrl='$imagemUrl', imagemBase64='$imagemBase64', idEstoque='$idEstoque', positionEstoqueLista='$positionEstoqueLista')"
    }
}
