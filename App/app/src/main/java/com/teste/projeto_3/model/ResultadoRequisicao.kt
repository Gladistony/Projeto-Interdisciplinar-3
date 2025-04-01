package com.teste.projeto_3.model

class ResultadoRequisicao(
    // Variáveis para exibir a resultado da requisição
    var tituloResultado: String = "",       // Resultado da requisição para o RecyclerView
    var status: String = "",                // Status da requisição ("erro" ou "sucesso")
    var descricaoStatus: String = "",       // Descrição do status da operação
    var metodoDeEnvio: String = "",         // Nome da função que executou a requisição (ex.: apagar_estoque, criar_estoque...)
    var iconeResultado: Int = 0,            // 0 = sucesso, 1 = falha, 2 = enviandoRequisicao

    // Variáveis usadas para estoques e produtos
    var imagemUrl: String = "",             // URL da imagem, caso esta já tenha sido enviada com sucesso ao servidor
    var imagemBase64: String = "",          // Imagem em Base64, caso ocorra falha ao fazer o upload

    // Variáveis com informações sobre o estoque a ser enviado
    var nomeEstoque: String = "",
    var descricaoEstoque: String = "",
    var idEstoque: String = "",
    var positionEstoqueLista: Int = 0,

    // Variáveis com informações sobre o produto a ser enviado
    var idProduto: String? = null,
    var positionProdutoLista: Int = 0,
    var nomeProduto: String = "",
    var descricaoProduto: String = "",
    var dataValidadeProduto: String = "",
    var quantidadeProduto: String = "",
    var precoProduto: Double = 0.0,
    var subtracao: Int = 0
) {
    override fun toString(): String {
        return "ResultadoRequisicaoEstoque(nomeEstoque='$nomeEstoque', descricaoEstoque='$descricaoEstoque', textoResultado='$tituloResultado', iconeResultado=$iconeResultado, status='$status', descricaoStatus='$descricaoStatus', metodoDeEnvio='$metodoDeEnvio', imagemUrl='$imagemUrl', imagemBase64='$imagemBase64', idEstoque='$idEstoque', positionEstoqueLista='$positionEstoqueLista', idProduto='$idProduto', nomeProduto='$nomeProduto', descricaoProduto='$descricaoProduto', dataValidadeProduto='$dataValidadeProduto', quantidadeProduto='$quantidadeProduto', precoProduto='$precoProduto', positionProdutoLista='$positionProdutoLista')"
    }
}
