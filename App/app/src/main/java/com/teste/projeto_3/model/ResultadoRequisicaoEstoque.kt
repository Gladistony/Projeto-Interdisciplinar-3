package com.teste.projeto_3.model

class ResultadoRequisicaoEstoque(
    var textoResultado: String = "",
    // 0 = sucesso, 1 = falha, 2 = enviandoRequisicao
    var iconeResultado: Int = 0
) {
    override fun toString(): String {
        return "ResultadoRequisicaoEstoque(textoResultado='$textoResultado', iconeResultado=$iconeResultado)"
    }
}
