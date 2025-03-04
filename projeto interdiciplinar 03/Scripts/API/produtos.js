import { getEstoque, registro_produto, registro_produto_estoque, mudar_produto, upload_imgeral } from './apiConnection.js';

document.addEventListener('DOMContentLoaded', async () => {
    const productForm = document.getElementById('product-form');
    const estoqueSelecionado = JSON.parse(localStorage.getItem('estoqueSelecionado'));

    if (!estoqueSelecionado) {
        alert('Nenhum estoque selecionado.');
        window.location.href = '../telaDeStocks.html';
        return;
    }

    document.getElementById('h1').textContent = estoqueSelecionado.name;
    document.getElementById('descricao').textContent = estoqueSelecionado.description;

    await carregarProdutos(estoqueSelecionado.id);

    productForm.addEventListener('submit', async (event) => {
        event.preventDefault();

        const productName = document.getElementById('product-name').value.trim();
        const productDescription = document.getElementById('product-description').value.trim();
        const productQuantity = document.getElementById('product-quantity').value;
        const productDateFabricacao = document.getElementById('product-date-fabricacao').value;
        const productDateValidade = document.getElementById('product-date').value;
        const productPrice = document.getElementById('product-price').value;
        const inputFile = document.querySelector('.img-produto');

        if (inputFile.files.length === 0) {
            alert('Selecione uma imagem do produto!');
            return;
        }

        const imagemBase64 = await converterImagemParaBase64(inputFile.files[0]);

        try {
            const respostaUpload = await upload_imgeral(imagemBase64, "produto");
            const imageUrl = respostaUpload.url;

            const novoProduto = await registro_produto(productName, productDescription, imageUrl);

            const dataCompleta = `${productDateFabricacao} até ${productDateValidade}`;
            await registro_produto_estoque(
                estoqueSelecionado.id.toString(),
                novoProduto.id_produto.toString(),
                productQuantity.toString(),
                dataCompleta,
                productPrice
            );

            alert('Produto cadastrado com sucesso!');
            await carregarProdutos(estoqueSelecionado.id);
            productForm.reset();
        } catch (error) {
            console.error('Erro ao cadastrar produto:', error);
            alert('Erro ao cadastrar produto. Verifique o console.');
        }
    });

    async function carregarProdutos(estoqueId) {
        try {
            const resposta = await getEstoque();
            const estoques = resposta.estoque;
            const estoqueAtual = estoques.find(e => e.id === estoqueId);

            const listaProdutos = document.getElementById('lista-produtos').querySelector('ul');
            listaProdutos.innerHTML = '';

            if (estoqueAtual && estoqueAtual.produtos) {
                estoqueAtual.produtos.forEach(produto => {
                    const li = document.createElement('li');
                    li.innerHTML = `
                        <img class="img-dtl" src="${produto.foto}" alt="">
                        <strong>ID:</strong> <span class="produto-id">${produto.id}</span>,
                        <strong>Nome:</strong> <span>${produto.nome}</span>,
                        <strong>Quantidade:</strong> <span class="produto-quantidade">${produto.quantidade}</span>,
                        <strong>Data de Vencimento:</strong> <span>${produto.data_validade}</span>,
                        <strong>Preço cada:</strong> <span>R$: ${produto.preco_medio}</span>
                        <button class="edit" data-produto="${produto.id}">Editar</button>
                        <button class="delete" data-produto="${produto.id}">Apagar</button>
                    `;
                    listaProdutos.appendChild(li);
                });

                adicionarEventosEditar(estoqueId);
                adicionarEventosDeletar(estoqueId);
            }
        } catch (error) {
            console.error('Erro ao carregar produtos:', error);
            alert('Erro ao carregar produtos.');
        }
    }

    function adicionarEventosEditar(estoqueId) {
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
                    const idProduto = confirmButton.getAttribute('data-produto');

                    try {
                        await mudar_produto(idProduto, estoqueId.toString(), novaQuantidade);
                        input.replaceWith(quantidadeSpan);
                        quantidadeSpan.textContent = novaQuantidade;
                        confirmButton.replaceWith(button);
                        alert('Quantidade atualizada com sucesso!');
                    } catch (error) {
                        console.error('Erro ao atualizar quantidade:', error);
                        alert('Erro ao atualizar quantidade.');
                    }
                });
            });
        });
    }

    function adicionarEventosDeletar(estoqueId) {
        document.querySelectorAll('.delete').forEach(button => {
            button.addEventListener('click', async () => {
                const confirmar = confirm('Deseja realmente apagar este produto?');
                if (!confirmar) return;

                const idProduto = button.getAttribute('data-produto');
                try {
                    const resposta = await mudar_produto(idProduto, estoqueId.toString(), '-99999999');
                    if (resposta.status === 'erro' && resposta.code === 28) {
                        button.closest('li').remove();
                        alert('Produto removido com sucesso!');
                    } else {
                        alert('Erro ao remover produto.');
                        console.error('Erro:', resposta);
                    }
                } catch (error) {
                    console.error('Erro ao remover produto:', error);
                    alert('Erro ao remover produto.');
                }
            });
        });
    }

    // === FUNÇÃO PARA CONVERTER IMAGEM EM BASE64 ===
    function converterImagemParaBase64(file) {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onload = (event) => resolve(event.target.result.split(',')[1]);
            reader.onerror = (error) => reject(error);
            reader.readAsDataURL(file);
        });
    }
});
