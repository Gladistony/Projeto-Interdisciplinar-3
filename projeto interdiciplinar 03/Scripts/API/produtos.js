import { registro_produto, registro_produto_estoque } from './apiConnection.js';

document.addEventListener('DOMContentLoaded', () => {
    const productForm = document.getElementById('product-form');
    const estoqueSelecionado = JSON.parse(localStorage.getItem('estoqueSelecionado'));

    if (estoqueSelecionado) {
        document.getElementById('h1').textContent = estoqueSelecionado.name;
        document.getElementById('descricao').textContent = estoqueSelecionado.description;
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