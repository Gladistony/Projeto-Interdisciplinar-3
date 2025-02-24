import { getAllUsers, excluirUsuario } from './apiConnection.js';

document.addEventListener('DOMContentLoaded', async function () {
    try {
        // Obtenha a lista de usuários
        let usuarios = await getAllUsers();
        console.log('Usuários:', usuarios);

        const tbody = document.querySelector('table tbody');
        tbody.innerHTML = ''; // Limpa o conteúdo existente

        const renderTable = (userList) => {
            tbody.innerHTML = ''; // Limpa o conteúdo existente

            userList.forEach((usuario, index) => {
                // Cria a linha principal do usuário
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${index + 1}</td>
                    <td><img src="../IMG/perfil_generico.png" alt="Foto de Perfil" class="perfil"></td>
                    <td>${usuario.usuario}</td>
                    <td>${usuario.nome_completo || 'Nome Completo'}</td>
                    <td>${usuario.email}</td>
                    <td>*******</td>
                    <td>
                        <button class="edit">Editar</button>
                        <button class="delete" data-usuario="${usuario.usuario}">Apagar</button>
                        <button class="toggle-details">Mostrar Detalhes</button>
                    </td>
                `;
                tbody.appendChild(tr);

                // Cria a linha de detalhes do usuário (inicialmente oculta)
                const trDetalhes = document.createElement('tr');
                trDetalhes.classList.add('detalhes');
                trDetalhes.style.display = 'none'; // Oculta os detalhes inicialmente
                trDetalhes.innerHTML = `
                    <td colspan="7">
                        <div class="detalhe-estoque">
                            <h3>Fotos do Estoque</h3>
                            <img src="../IMG/estoque1.jpg" alt="Foto do Estoque" class="estoque">
                            <img src="../IMG/estoque2.jpg" alt="Foto do Estoque" class="estoque">
                        </div>
                        <div class="detalhe-estoque">
                            <h3>Descrição do Estoque</h3>
                            <p>Descrição do Estoque 1</p>
                            <p>Descrição do Estoque 2</p>
                        </div>
                        <div class="detalhe-estoque">
                            <h3>ID de Câmeras</h3>
                            <p>ID Câmera 1</p>
                            <p>ID Câmera 2</p>
                        </div>
                        <div class="detalhe-estoque">
                            <h3>ID de Cada Estoque</h3>
                            <p>ID Estoque 1</p>
                            <p>ID Estoque 2</p>
                        </div>
                    </td>
                `;
                tbody.appendChild(trDetalhes);

                // Adiciona evento de clique ao botão de detalhes
                tr.querySelector('.toggle-details').addEventListener('click', () => {
                    const isHidden = trDetalhes.style.display === 'none';
                    trDetalhes.style.display = isHidden ? 'table-row' : 'none';
                    tr.querySelector('.toggle-details').textContent = isHidden ? 'Esconder Detalhes' : 'Mostrar Detalhes';
                });

                // Evento para excluir usuário
                tr.querySelector('.delete').addEventListener('click', async function () {
                    const usuarioNome = this.getAttribute('data-usuario');

                    if (!usuarioNome) {
                        alert('Erro: Nome do usuário não encontrado.');
                        return;
                    }

                    const confirmacao = confirm(`Tem certeza de que deseja excluir o usuário ${usuarioNome}?`);
                    if (!confirmacao) return;

                    try {
                        await excluirUsuario(usuarioNome);
                        renderTable(userList.filter(u => u.usuario !== usuarioNome)); // Atualiza a tabela
                    } catch (error) {
                        console.error('Erro ao excluir usuário:', error);
                        alert(`Erro ao excluir usuário: ${error.message}`);
                    }
                });
            });
        };

        // Renderiza a tabela inicialmente com todos os usuários
        renderTable(usuarios.contas);

        // Adiciona evento de input ao campo de pesquisa
        document.getElementById('search-input').addEventListener('input', (event) => {
            const searchText = event.target.value.toLowerCase();
            const filteredUsers = usuarios.contas.filter(usuario =>
                usuario.usuario.toLowerCase().includes(searchText)
            );
            renderTable(filteredUsers);
        });
    } catch (error) {
        console.error('Erro ao carregar os usuários:', error);
        alert(`Erro: ${error.message}`);
    }

    // Adiciona evento de clique ao botão de redirecionamento para home
    document.getElementById('btn-home').addEventListener('click', () => {
        window.location.href = 'telaDeHomel.html';
    });
});
