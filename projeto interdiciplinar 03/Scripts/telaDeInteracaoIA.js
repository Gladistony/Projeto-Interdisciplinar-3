const caixatexto = document.getElementById("caixa-texto");
caixatexto.addEventListener("keydown", function (e) {
    if (e.code === "Enter") {
        criarTexto()
    }
});

function criarTexto(){
    let digitado = document.getElementById("caixa-texto").value;
    if (digitado != "") {
        document.getElementById("digitado").innerText = digitado;
    }
}