import { getAllUsers } from './apiConnection.js';

document.addEventListener('DOMContentLoaded', async function () {
    try {
        // Obtenha a lista de todos os usuários
        const allUsers = await getAllUsers();
        console.log('Todos os usuários:', allUsers);

        if (allUsers.status === "sucesso") {
            const userList = document.getElementById('user-list');

            // Preencher a lista de usuários no HTML
            allUsers.contas.forEach(user => {
                const listItem = document.createElement('li');
                listItem.textContent = `${user.usuario} - ${user.email}`;
                userList.appendChild(listItem);
            });
        } else {
            throw new Error('Erro ao obter a lista de usuários.');
        }
    } catch (error) {
        console.error('Erro ao carregar a lista de usuários:', error);
        alert(`Erro: ${error.message}`);
    }
});
