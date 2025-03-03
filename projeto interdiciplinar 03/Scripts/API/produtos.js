import { getEstoque, registro_produto, registro_produto_estoque } from './apiConnection.js';

document.addEventListener('DOMContentLoaded', async () => {
    const productForm = document.getElementById('product-form');
    const estoqueSelecionado = JSON.parse(localStorage.getItem('estoqueSelecionado'));

    if (estoqueSelecionado) {
        document.getElementById('h1').textContent = estoqueSelecionado.name;
        document.getElementById('descricao').textContent = estoqueSelecionado.description;

        try {
            const resposta = await getEstoque(); // Aqui pega a resposta completa da API
            const estoques = resposta.estoque; // Extrai apenas o array "estoque"

            const estoqueAtual = estoques.find(estoque => estoque.id === estoqueSelecionado.id);

            if (estoqueAtual && estoqueAtual.produtos) {
                console.log('Produtos no estoque atual:', estoqueAtual.produtos); // Adiciona um log para verificar os produtos

                const listaProdutos = document.getElementById('lista-produtos').querySelector('ul');
                listaProdutos.innerHTML = ''; // Limpa a lista existente

                estoqueAtual.produtos.forEach(produto => {
                    const li = document.createElement('li');
                    li.innerHTML = `
                        <strong><h3 id="id">${produto.id}</h3>Nome:</strong> <span>${produto.nome}</span>, 
                        <strong>Quantidade:</strong> <span>${produto.quantidade}</span>, 
                        <strong>Data de Vencimento:</strong> <span>${produto.data_validade}</span>, 
                        <strong>Preço:</strong> <span>R$: ${produto.preco_medio}</span>
                    `;
                    listaProdutos.appendChild(li);
                });
            }
        } catch (error) {
            console.error('Erro ao carregar produtos do estoque:', error);
            alert('Erro ao carregar produtos do estoque.');
        }
    } else {
        alert('Nenhum estoque selecionado.');
        window.location.href = '../telaDeStocks.html';
    }
    
    productForm.addEventListener('submit', async (event) => {
        event.preventDefault(); // Impede o envio padrão do formulário
        
        const productName = document.getElementById('product-name').value.trim();
        const productDescription = document.getElementById('product-description').value.trim();
        const productQuantity = document.getElementById('product-quantity').value;
        const productDate = document.getElementById('product-date').value;
        const productPrice = document.getElementById('product-price').value;

        try {
            const novoProduto = await registro_produto(productName, productDescription, "../IMG/Logo.webp");
            const produtoInserido = await registro_produto_estoque(
                estoqueSelecionado.id.toString(), 
                novoProduto.id_produto.toString(), 
                productQuantity.toString(), 
                productDate, 
                productPrice
            );

            console.log('Produto inserido:', produtoInserido);
            alert('Produto adicionado com sucesso!');
        } catch (error) {
            console.error('Erro ao adicionar produto:', error);
            alert('Erro ao adicionar produto. Verifique o console.');
        }
    });
});