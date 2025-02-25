import { recoverSenha, getConnectionId } from './apiConnection.js'; 

document.addEventListener('DOMContentLoaded', function () {
    const form = document.querySelector('form');
    const mensagem = document.createElement('p');
    mensagem.style.color = 'red';
    form.appendChild(mensagem); // Adiciona a mensagem ao formulário

    form.addEventListener('submit', async function (event) {
        event.preventDefault();

        const usuario = document.querySelector('#usuario').value.trim();
        if (!usuario) {
            mensagem.textContent = 'Por favor, insira o nome de usuário.';
            return;
        }

        let id = localStorage.getItem('connectionId'); // Pegando o ID do usuário logado

        if (!id) {
            try {
                id = await getConnectionId(); // Obtém o ID se ainda não estiver salvo
                localStorage.setItem('connectionId', id); // Salva o ID para uso futuro
            } catch (error) {
                mensagem.textContent = 'Erro ao obter ID do usuário.';
                console.error(error);
                return;
            }
        }

        try {
            // Chamar a função para recuperar a senha
            const result = await recoverSenha(id, usuario);

            if (result.status === 'sucesso' && result.code === 21) {
                mensagem.style.color = 'green';
                mensagem.textContent = result.message; // Exibir mensagem de sucesso
            } else {
                throw new Error(result.message);
            }
        } catch (error) {
            console.error('Erro durante a recuperação de senha:', error);
            mensagem.textContent = `Erro: ${error.message}`;
        }
    });
});
