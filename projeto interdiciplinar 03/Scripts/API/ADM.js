import { getAllUsers, excluirUsuario } from './apiConnection.js';

document.addEventListener('DOMContentLoaded', async function () {
    try {
        let usuarios = await getAllUsers();
        console.log('Usuários:', usuarios);

        const tbody = document.querySelector('table tbody');
        tbody.innerHTML = '';

        const renderTable = (userList) => {
            tbody.innerHTML = '';
            userList.forEach((usuario, index) => {
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

                const trDetalhes = document.createElement('tr');
                trDetalhes.classList.add('detalhes');
                trDetalhes.style.display = 'none';
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

                tr.querySelector('.toggle-details').addEventListener('click', () => {
                    const isHidden = trDetalhes.style.display === 'none';
                    trDetalhes.style.display = isHidden ? 'table-row' : 'none';
                    tr.querySelector('.toggle-details').textContent = isHidden ? 'Esconder Detalhes' : 'Mostrar Detalhes';
                });
            });
        };

        renderTable(usuarios.contas);

        document.getElementById('search-input').addEventListener('input', (event) => {
            const searchText = event.target.value.toLowerCase();
            const filteredUsers = usuarios.contas.filter(usuario =>
                usuario.usuario.toLowerCase().includes(searchText)
            );
            renderTable(filteredUsers);
        });

        document.querySelectorAll('.delete').forEach(button => {
            button.addEventListener('click', async function () {
                const usuario = this.dataset.usuario;
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
            });
        });

        document.getElementById('btn-home').addEventListener('click', () => {
            window.location.href = 'telaDeHomel.html';
        });

    } catch (error) {
        console.error('Erro ao carregar os usuários:', error);
        alert(`Erro: ${error.message}`);
    }
});
