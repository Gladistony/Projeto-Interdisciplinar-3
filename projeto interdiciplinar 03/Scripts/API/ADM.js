import { getAllUsers, excluirUsuario, getUserData, set_user_data, apagar_estoque, mudar_produto, charge_estoque_url, upload_imgeral } from './apiConnection.js';

document.addEventListener('DOMContentLoaded', async function () {
    try {
        let usuarios = await getAllUsers();
        console.log('Usuários:', usuarios);

        const tbody = document.querySelector('table tbody');
        tbody.innerHTML = '';

        const estoquesPorPagina = 5;
        let paginasEstoques = {}; // Armazena a página atual de cada usuário

        const renderTable = async (userList) => {
            tbody.innerHTML = '';

            for (const usuario of userList) {
                let dadosUser = await getUserData(usuario.usuario);

                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${userList.indexOf(usuario) + 1}</td>
                    <td><img src="${dadosUser.data.url_foto || '../IMG/perfil_generico.png'}" alt="Foto de Perfil" class="perfil"></td>
                    <td>${usuario.usuario}</td>
                    <td>${dadosUser.data.nome_completo || 'Não informado'}</td>
                    <td>${dadosUser.data.email}</td>
                    <td class="senha-container">*******</td>
                    <td>
                        <button class="edit" data-usuario="${usuario.usuario}">Editar</button>
                        <button class="delete" data-usuario="${usuario.usuario}">Apagar</button>
                        <button class="toggle-details">Mostrar Detalhes</button>
                    </td>
                `;
                tbody.appendChild(tr);

                const trDetalhes = document.createElement('tr');
                trDetalhes.classList.add('detalhes');
                trDetalhes.style.display = 'none';
                trDetalhes.innerHTML = `
                    <td colspan="7">
                        <div class="detalhe-usuario">
                            <h3>Detalhes do Usuário</h3>
                            <p><strong>Data de Criação:</strong> ${new Date(dadosUser.data.criacao).toLocaleString()}</p>
                            <p><strong>Último Login:</strong> ${new Date(dadosUser.data.ultimo_login).toLocaleString()}</p>
                            <p><strong>Tipo de Conta:</strong> ${dadosUser.data.tipo_conta}</p>
                        </div>
                        <div class="detalhe-estoque-container"></div>
                    </td>
                `;
                tbody.appendChild(trDetalhes);

                paginasEstoques[usuario.usuario] = 0; // Inicia página 0 para cada usuário

                tr.querySelector('.toggle-details').addEventListener('click', () => {
                    const isHidden = trDetalhes.style.display === 'none';
                    trDetalhes.style.display = isHidden ? 'table-row' : 'none';
                    tr.querySelector('.toggle-details').textContent = isHidden ? 'Esconder Detalhes' : 'Mostrar Detalhes';

                    if (isHidden) {
                        const containerEstoques = trDetalhes.querySelector('.detalhe-estoque-container');
                        renderizarEstoques(dadosUser, containerEstoques, usuario.usuario);
                    }
                });
            }
        };

        function renderizarEstoques(dadosUser, container, usuario) {
            container.innerHTML = '';

            const paginaAtual = paginasEstoques[usuario];
            const inicio = paginaAtual * estoquesPorPagina;
            const fim = inicio + estoquesPorPagina;
            const estoquesPagina = dadosUser.data.estoque.slice(inicio, fim);

            estoquesPagina.forEach(estoque => {
                const divEstoque = document.createElement('div');
                divEstoque.classList.add('detalhe-estoque');
                divEstoque.innerHTML = `
                    <h3>Nome do Estoque:</h3>
                    <p><strong>${estoque.nome}</strong></p>
                    <h3>Fotos do Estoque</h3>
                    <img src="${estoque.imagem}" alt="Foto do Estoque" class="estoque">
                    <h3>Descrição do Estoque</h3>
                    <p>${estoque.descricao}</p>
                    <h3>ID de Câmeras</h3>
                    <p>${estoque.id_camera}</p>
                    <h3>ID do Estoque</h3>
                    <p>${estoque.id}</p>
                    <h3>Produtos no Estoque</h3>
                    ${estoque.produtos ? estoque.produtos.map(produto => `
                        <div class="detalhe-produto">
                            <p>Produto: ${produto.nome} - Quantidade: <span class="produto-quantidade">${produto.quantidade} - Validade: ${produto.data_validade} </span></p>
                            <img src="${produto.foto}" alt="Foto do produto" class="produto-img">
                            <p class="descricao">${produto.descricao}</p>
                            <button class="edit-produto" data-produto="${produto.id}">Editar</button>
                            <button class="delete-produto" data-produto="${produto.id}">Apagar</button>
                        </div>
                    `).join('') : '<p>Sem produtos</p>'}
                    <button class="edit-estoque">Editar Estoque</button>
                    <button class="delete-estoque">Apagar Estoque</button>
                `;
                container.appendChild(divEstoque);

                divEstoque.querySelector('.edit-estoque').addEventListener('click', async () => {
                    const form = document.createElement('form');
                    form.classList.add('form-editar-estoque');
                    form.innerHTML = `
                        <label>Foto (nova):</label>
                        <input type="file" class="input-foto">
                        <button type="submit">Salvar Imagem</button>
                        <button type="button" class="cancelar-edicao">Cancelar</button>
                    `;
                
                    divEstoque.innerHTML = '';
                    divEstoque.appendChild(form);
                
                    // Botão cancelar
                    form.addEventListener('submit', async (event) => {
                        event.preventDefault();
                    
                        const arquivoFoto = form.querySelector('.input-foto').files[0];
                        if (!arquivoFoto) {
                            alert('Selecione uma nova imagem para salvar.');
                            return;
                        }
                    
                        try {
                            const base64Reduzido = await resizeImage(arquivoFoto, 800, 800);
                    
                            // Faz o upload direto do base64 puro
                            const uploadResult = await upload_imgeral(base64Reduzido, `estoque/${usuario}/${estoque.id}`);
                            const novaUrlFoto = uploadResult.url;
                    
                            await charge_estoque_url(novaUrlFoto, estoque.id);
                    
                            alert('Imagem atualizada com sucesso!');
                            renderizarEstoques(dadosUser, container, usuario);
                        } catch (error) {
                            console.error('Erro ao atualizar imagem:', error);
                            alert('Erro ao atualizar a imagem. Verifique o console.');
                        }
                    });
                });
                
                // === FUNÇÃO PARA REDIMENSIONAR IMAGEM ===
                function resizeImage(file, maxWidth, maxHeight) {
                    return new Promise((resolve, reject) => {
                        const reader = new FileReader();
                        reader.readAsDataURL(file);
                
                        reader.onload = (event) => {
                            const img = new Image();
                            img.src = event.target.result;
                
                            img.onload = () => {
                                const canvas = document.createElement("canvas");
                                let width = img.width;
                                let height = img.height;
                
                                if (width > maxWidth || height > maxHeight) {
                                    if (width > height) {
                                        height *= maxWidth / width;
                                        width = maxWidth;
                                    } else {
                                        width *= maxHeight / height;
                                        height = maxHeight;
                                    }
                                }
                
                                canvas.width = width;
                                canvas.height = height;
                
                                const ctx = canvas.getContext("2d");
                                ctx.drawImage(img, 0, 0, width, height);
                
                                const base64 = canvas.toDataURL("image/jpeg", 0.7);
                                resolve(base64.replace(/^data:image\/jpeg;base64,/, "")); // só a string base64 pura
                            };
                        };
                
                        reader.onerror = (error) => reject(error);
                    });
                }

                // Evento para o botão de deletar estoque
                divEstoque.querySelector('.delete-estoque').addEventListener('click', async (event) => {
                    event.stopPropagation(); // Impede o clique do box ao deletar
                    const confirmar = confirm(`Tem certeza que deseja deletar o estoque ${estoque.nome}?`);
                    if (confirmar) {
                        try {
                            const payload = { id_estoque: estoque.id.toString() };
                            console.log("Tentando deletar o estoque com payload:", payload);
            
                            await apagar_estoque(estoque.id.toString()); // Chama a função apagar_estoque passando o id_estoque como string
                            divEstoque.remove(); // Remove o box da tela
                            console.log(`Estoque ${estoque.nome} deletado com sucesso.`);
                        } catch (error) {
                            console.error(`❌ Erro ao deletar o estoque ${estoque.nome}:`, error);
                            alert(`Erro ao deletar o estoque ${estoque.nome}. Verifique o console.`);
                        }
                    }
                });
            
                // Evento para o botão de editar produto
                divEstoque.querySelectorAll('.edit-produto').forEach(button => {
                    button.addEventListener('click', (event) => {
                        const produtoDiv = event.target.closest('.detalhe-produto');
                        const quantidadeSpan = produtoDiv.querySelector('.produto-quantidade');
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
                                await mudar_produto(idProduto, estoque.id.toString(), novaQuantidade);
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
            
                // Evento para o botão de deletar produto
                divEstoque.querySelectorAll('.delete-produto').forEach(button => {
                    button.addEventListener('click', async (event) => {
                        const confirmar = confirm('Tem certeza que deseja apagar este produto?');
                        if (confirmar) {
                            const idProduto = button.getAttribute('data-produto');
                            try {
                                const resposta = await mudar_produto(idProduto, estoque.id.toString(), '-99999999');
                                if (resposta.status === 'erro' && resposta.code === 28) {
                                    button.closest('.detalhe-produto').remove();
                                    alert('Produto deletado com sucesso!');
                                } else {
                                    alert('Erro ao deletar produto. Verifique o console.');
                                    console.error('Erro ao deletar produto:', resposta);
                                }
                            } catch (error) {
                                console.error('Erro ao deletar produto:', error);
                                alert('Erro ao deletar produto. Verifique o console.');
                            }
                        }
                    });
                });
            });                   

            const totalPaginas = Math.ceil(dadosUser.data.estoque.length / estoquesPorPagina);
            const navContainer = document.createElement('div');
            navContainer.classList.add('paginacao-estoques');

            if (paginaAtual > 0) {
                const btnAnterior = document.createElement('button');
                btnAnterior.innerHTML = '⬅️';
                btnAnterior.onclick = () => {
                    paginasEstoques[usuario]--;
                    renderizarEstoques(dadosUser, container, usuario);
                };
                navContainer.appendChild(btnAnterior);
            }

            if (paginaAtual < totalPaginas - 1) {
                const btnProximo = document.createElement('button');
                btnProximo.innerHTML = '➡️';
                btnProximo.onclick = () => {
                    paginasEstoques[usuario]++;
                    renderizarEstoques(dadosUser, container, usuario);
                };
                navContainer.appendChild(btnProximo);
            }

            container.appendChild(navContainer);
        }

        renderTable(usuarios.contas);

        document.getElementById('search-input').addEventListener('input', (event) => {
            const searchText = event.target.value.toLowerCase();
            const filteredUsers = usuarios.contas.filter(usuario =>
                usuario.usuario.toLowerCase().includes(searchText)
            );
            renderTable(filteredUsers);
        });

        document.addEventListener('click', async (event) => {
            const target = event.target;

            if (target.classList.contains('delete')) {
                const usuario = target.dataset.usuario;
                const idAdmin = localStorage.getItem('connectionId');

                if (!idAdmin) {
                    alert('Erro: ID do administrador não encontrado.');
                    return;
                }

                if (confirm(`Tem certeza de que deseja excluir o usuário ${usuario}?`)) {
                    await excluirUsuario(idAdmin, usuario);
                    alert(`Usuário ${usuario} excluído com sucesso!`);
                    location.reload();
                }
            }

            if (target.classList.contains('edit')) {
                const tr = target.closest('tr');
                const usuario = target.dataset.usuario;
                const senhaContainer = tr.querySelector('.senha-container');

                if (target.textContent === 'Editar') {
                    senhaContainer.innerHTML = `<input type="password" placeholder="Nova senha" class="senha-input">`;
                    target.textContent = 'Confirmar';
                } else {
                    const novaSenha = senhaContainer.querySelector('.senha-input').value;
                    const idAdmin = localStorage.getItem('connectionId');

                    if (!novaSenha) {
                        alert('Digite uma nova senha!');
                        return;
                    }

                    await set_user_data(idAdmin, usuario, novaSenha);
                    alert(`Senha do usuário ${usuario} atualizada com sucesso!`);
                    senhaContainer.innerHTML = '*******';
                    target.textContent = 'Editar';
                }
            }
        });

        document.getElementById('btn-home').addEventListener('click', () => {
            window.location.href = 'telaDeHomel.html';
        });

    } catch (error) {
        console.error('Erro ao carregar os usuários:', error);
        alert(`Erro: ${error.message}`);
    }
});