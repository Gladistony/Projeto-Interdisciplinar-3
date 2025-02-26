import { charge, getConnectionId } from './apiConnection.js';

document.getElementById('nova_senha_Form').addEventListener('submit', async function (event) {
    event.preventDefault();

    const usuarioInput = document.querySelector('#usuario');
    const senha = document.querySelector('#senha').value.trim();
    const nova_senha = document.querySelector('#novasenha').value.trim();

    const usuario = usuarioInput.value.trim();
    if (!usuario) {
        mensagem.textContent = 'Por favor, insira o nome de usuário.';
        return;
    }

    try {
        console.log("Tentando mudar senha para o usuário:", usuario);

        const result = await charge(usuario, senha, nova_senha);

        console.log("Resposta da API:", result);

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