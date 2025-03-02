import { getAllUsers, excluirUsuario, getUserData, set_user_data } from './apiConnection.js';

document.addEventListener('DOMContentLoaded', async function () {
    try {
        let usuarios = await getAllUsers();
        console.log('Usuários:', usuarios);

        const tbody = document.querySelector('table tbody');
        tbody.innerHTML = '';

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
                        <div class="detalhe-estoque-container">
                            ${dadosUser.data.estoque.map(estoque => `
                                <div class="detalhe-estoque">
                                    <h3>Nome do Estoque:</h3>
                                    <p><strong>${estoque.nome}</strong></p>
                                    <h3>Fotos do Estoque</h3>
                                    <img src="${estoque.url_foto}" alt="Foto do Estoque" class="estoque">
                                    <h3>Descrição do Estoque</h3>
                                    <p>${estoque.descricao}</p>
                                    <h3>ID de Câmeras</h3>
                                    <p>${estoque.id_camera}</p>
                                    <h3>ID do Estoque</h3>
                                    <p>${estoque.id}</p>
                                    <h3>Produtos no Estoque</h3>
                                    ${estoque.produtos ? estoque.produtos.map(produto => `
                                        <div class="detalhe-produto">
                                            <p>Produto: ${produto.nome} - Quantidade: ${produto.quantidade}</p>
                                            <button class="edit-produto">Editar</button>
                                            <button class="delete-produto">Apagar</button>
                                        </div>
                                    `).join('') : '<p>Sem produtos</p>'}
                                    <button class="edit-estoque">Editar Estoque</button>
                                    <button class="delete-estoque">Apagar Estoque</button>
                                </div>
                            `).join('')}
                        </div>
                    </td>
                `;
                tbody.appendChild(trDetalhes);                                                          

                tr.querySelector('.toggle-details').addEventListener('click', () => {
                    const isHidden = trDetalhes.style.display === 'none';
                    trDetalhes.style.display = isHidden ? 'table-row' : 'none';
                    tr.querySelector('.toggle-details').textContent = isHidden ? 'Esconder Detalhes' : 'Mostrar Detalhes';
                });
            }
        };

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

                const confirmacao = confirm(`Tem certeza de que deseja excluir o usuário ${usuario}?`);
                if (!confirmacao) return;

                try {
                    await excluirUsuario(idAdmin, usuario);
                    alert(`Usuário ${usuario} excluído com sucesso!`);
                    location.reload();
                } catch (error) {
                    console.error('Erro ao excluir usuário:', error);
                    alert(`Erro ao excluir usuário: ${error.message}`);
                }
            }

            if (target.classList.contains('edit')) {
                const tr = target.closest('tr');
                const usuario = target.dataset.usuario;
                const senhaContainer = tr.querySelector('.senha-container');

                if (target.textContent === 'Editar') {
                    senhaContainer.innerHTML = `<input type="password" placeholder="Nova senha" class="senha-input">`;
                    target.textContent = 'Confirmar';
                } else if (target.textContent === 'Confirmar') {
                    const novaSenha = senhaContainer.querySelector('.senha-input').value;
                    const idAdmin = localStorage.getItem('connectionId');

                    if (!novaSenha) {
                        alert('Digite uma nova senha!');
                        return;
                    }

                    try {
                        await set_user_data(idAdmin, usuario, novaSenha);
                        alert(`Senha do usuário ${usuario} atualizada com sucesso!`);
                        senhaContainer.innerHTML = '*******';
                        target.textContent = 'Editar';
                    } catch (error) {
                        console.error('Erro ao atualizar senha:', error);
                        alert(`Erro ao atualizar senha: ${error.message}`);
                    }
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