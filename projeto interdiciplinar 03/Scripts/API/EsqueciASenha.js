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

        try {
            // Agora chamamos recoverSenha apenas com 'usuario', pois a nova versão obtém o ID automaticamente
            const result = await recoverSenha(usuario);

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
