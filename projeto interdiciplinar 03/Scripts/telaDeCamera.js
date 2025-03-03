
document.addEventListener('DOMContentLoaded', () => {
    const estoqueSelecionado = JSON.parse(localStorage.getItem('estoqueSelecionado'));

    if (estoqueSelecionado) {
        document.getElementById('h1').textContent = estoqueSelecionado.name;
        document.getElementById('descricao').textContent = estoqueSelecionado.description;
    } else {
        alert('Nenhum estoque selecionado.');
        window.location.href = '../telaDeStocks.html'; // Redireciona de volta para a tela de estoques se nenhum estoque foi selecionado
    }

    // Adiciona evento de clique ao #lista-produtos
    const listaProdutos = document.getElementById('lista-produtos');
    let isExpanded = false;

    listaProdutos.addEventListener('click', () => {
        if (!isExpanded) {
            listaProdutos.style.width = '150%';
            listaProdutos.style.marginLeft = '180px';
            listaProdutos.style.marginTop = '-110px';
            listaProdutos.style.height = '110%';

            document.getElementById('camera-opcao').style.display = 'none';
            document.getElementById('product-form').style.display = 'none';
        } else {
            listaProdutos.style.width = '';
            listaProdutos.style.marginLeft = '';
            listaProdutos.style.marginTop = '';
            listaProdutos.style.height = '';

            document.getElementById('product-form').style.display = '';
            document.getElementById('camera-opcao').style.display = '';
        }
        isExpanded = !isExpanded;
    });
});