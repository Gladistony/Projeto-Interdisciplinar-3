import { getEstoque, registro_produto, registro_produto_estoque, mudar_produto } from './apiConnection.js';

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
                        <strong>ID:</strong> <span class="produto-id">${produto.id}</span>,
                        <strong>Nome:</strong> <span>${produto.nome}</span>, 
                        <strong>Quantidade:</strong> <span class="produto-quantidade">${produto.quantidade}</span>,
                        <strong>Data de Vencimento:</strong> <span>${produto.data_validade}</span>, 
                        <strong>Preço cada:</strong> <span>R$: ${produto.preco_medio}</span>
                        <button class="edit" data-produto="${produto.id}">Editar</button>
                    `;
                    listaProdutos.appendChild(li);
                });

                document.querySelectorAll('.edit').forEach(button => {
                    button.addEventListener('click', (event) => {
                        const li = event.target.closest('li');
                        const quantidadeSpan = li.querySelector('.produto-quantidade');
                        const quantidadeAtual = quantidadeSpan.textContent;
                        const input = document.createElement('input');
                        input.type = 'number';
                        input.value = quantidadeAtual;
                        quantidadeSpan.replaceWith(input);
                        input.focus();

                        const confirmButton = document.createElement('button');
                        confirmButton.className = 'confirm';
                        confirmButton.textContent = 'Confirmar';
                        button.replaceWith(confirmButton);

                        confirmButton.addEventListener('click', async () => {
                            const novaQuantidade = input.value;
                            const idProduto = button.getAttribute('data-produto');

                            try {
                                await mudar_produto(idProduto, estoqueSelecionado.id.toString(), novaQuantidade);
                                input.replaceWith(quantidadeSpan);
                                quantidadeSpan.textContent = novaQuantidade;
                                confirmButton.replaceWith(button);
                                alert('Quantidade atualizada com sucesso!');
                            } catch (error) {
                                console.error('Erro ao mudar produto:', error);
                                alert('Erro ao mudar produto. Verifique o console.');
                            }
                        });

                        input.addEventListener('keydown', (event) => {
                            if (event.key === 'Enter') {
                                confirmButton.click();
                            }
                        });
                    });
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
        const productDateFabricacao = document.getElementById('product-date-fabricacao').value;
        const productDateValidade = document.getElementById('product-date').value;
        const productPrice = document.getElementById('product-price').value;

        // Junta as datas no formato que você precisar, por exemplo: "fabricação - validade"
        const dataCompleta = `${productDateFabricacao} até ${productDateValidade}`;

        try {
            const novoProduto = await registro_produto(productName, productDescription, "../IMG/Logo.webp");

            const produtoInserido = await registro_produto_estoque(
                estoqueSelecionado.id.toString(),
                novoProduto.id_produto.toString(),
                productQuantity.toString(),
                dataCompleta, // Envia a string já formatada com as duas datas
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