import { getAllUsers } from './apiConnection.js';

document.addEventListener('DOMContentLoaded', async function () {
    try {
        // Obtenha a lista de usuários
        const usuarios = await getAllUsers();
        console.log('Usuários:', usuarios);

        const tbody = document.querySelector('table tbody');
        tbody.innerHTML = ''; // Limpa o conteúdo existente

        usuarios.contas.forEach((usuario, index) => {
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
                    <button class="delete">Apagar</button>
                    <button class="toggle-details">Mostrar Detalhes</button>
                </td>
            `;
            tbody.appendChild(tr);

            // Cria a linha de detalhes do usuário (inicialmente oculta)
            const trDetalhes = document.createElement('tr');
            trDetalhes.classList.add('detalhes');
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
                        <h3>Título do Estoque</h3>
                        <p>Título do Estoque 1</p>
                        <p>Título do Estoque 2</p>
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
                trDetalhes.style.display = trDetalhes.style.display === 'table-row' ? 'none' : 'table-row'; // Alterna a visibilidade dos detalhes
                const button = tr.querySelector('.toggle-details');
                button.textContent = button.textContent === 'Mostrar Detalhes' ? 'Esconder Detalhes' : 'Mostrar Detalhes';
            });
        });
    } catch (error) {
        console.error('Erro ao carregar os usuários:', error);
        alert(`Erro: ${error.message}`);
    }
});
