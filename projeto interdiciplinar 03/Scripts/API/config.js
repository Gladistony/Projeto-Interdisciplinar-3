import { charge, getConnectionId } from './apiConnection.js';

document.getElementById('nova_senha_Form').addEventListener('submit', async function (event) {
    event.preventDefault();

    const usuarioInput = document.querySelector('#usuario');
    const senhaInput = document.querySelector('#senha');
    const novaSenhaInput = document.querySelector('#novasenha');
    const submitBtn = document.querySelector('#mudarSenha');

    // Criando e estilizando a mensagem
    let mensagem = document.getElementById('mensagem');
    if (!mensagem) {
        mensagem = document.createElement('p');
        mensagem.id = 'mensagem';
        mensagem.style.color = 'red';
        document.getElementById('nova_senha_Form').appendChild(mensagem);
    }

    const usuario = usuarioInput?.value.trim();
    const senha = senhaInput?.value.trim();
    const nova_senha = novaSenhaInput?.value.trim();

    if (!usuario) {
        mensagem.textContent = 'Por favor, insira o nome de usuário.';
        return;
    }

    try {
        console.log("Tentando mudar senha para o usuário:", usuario);

        const result = await charge(usuario, senha, nova_senha);

        console.log("Resposta da API:", result);

        // Verifica se a resposta foi bem-sucedida
        if (result.status === 'sucesso' && result.code === 0) {
            mensagem.style.color = 'green';
            mensagem.textContent = result.message; // Exibir mensagem de sucesso

            // Esconde os elementos apenas se eles existirem
            if (usuarioInput) usuarioInput.style.display = 'none';
            if (senhaInput) senhaInput.style.display = 'none';
            if (novaSenhaInput) novaSenhaInput.style.display = 'none';
            if (submitBtn) submitBtn.style.display = 'none';

        } else {
            throw new Error(result.message);
        }
    } catch (error) {
        console.error('Erro durante a recuperação de senha:', error);
        mensagem.textContent = `Erro: ${error.message}`;
    }
});
