import { recoverSenha, getConnectionId } from './apiConnection.js';

document.addEventListener('DOMContentLoaded', function () {
    const form = document.querySelector('form');
    const mensagem = document.createElement('p');
    mensagem.style.color = 'red';
    form.appendChild(mensagem); // Adiciona a mensagem ao formulário

    form.addEventListener('submit', async function (event) {
        event.preventDefault();

        const usuarioInput = document.querySelector('#usuario');
        const botaoSubmit = form.querySelector('button[type="submit"]');

        const usuario = usuarioInput.value.trim();
        if (!usuario) {
            mensagem.textContent = 'Por favor, insira o nome de usuário.';
            return;
        }

        try {
            console.log("Tentando recuperar senha para o usuário:", usuario);

            const result = await recoverSenha(usuario);

            console.log("Resposta da API:", result);

            if (result.status === 'sucesso' && result.code === 21) {
                mensagem.style.color = 'green';
                mensagem.textContent = result.message; // Exibir mensagem de sucesso

                // Esconder o campo de usuário e o botão
                usuarioInput.style.display = 'none';
                botaoSubmit.style.display = 'none';
            } else {
                throw new Error(result.message);
            }
        } catch (error) {
            console.error('Erro durante a recuperação de senha:', error);
            mensagem.textContent = `Erro: ${error.message}`;
        }
    });
});
