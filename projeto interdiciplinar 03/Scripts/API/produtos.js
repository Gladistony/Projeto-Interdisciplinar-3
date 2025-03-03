document.addEventListener('DOMContentLoaded', () => {
    const estoqueSelecionado = JSON.parse(localStorage.getItem('estoqueSelecionado'));

    if (estoqueSelecionado) {
        document.getElementById('h1').textContent = estoqueSelecionado.name;

        // Aqui você pode preencher outros elementos conforme necessário
        // Exemplo:
        const descriptionElement = document.getElementById('descricao');
        descriptionElement.textContent = estoqueSelecionado.description;
        document.getElementById('descricao').appendChild(descriptionElement);
    } else {
        alert('Nenhum estoque selecionado.');
        window.location.href = 'telaDeStocks.html';
    }
});