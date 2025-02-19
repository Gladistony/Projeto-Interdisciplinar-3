import { getConnectionId, realizarCadastro } from './apiConnection.js';

// Manipula o envio do formulário de cadastro
document.getElementById('cadastroForm').addEventListener('submit', async function (event) {
    event.preventDefault();

    try {
        // Obtenha o ID de conexão primeiro
        const connectionId = await getConnectionId();
        console.log('ID de conexão recebido:', connectionId);

        // Armazenar o ID de conexão no armazenamento local
        localStorage.setItem('connectionId', connectionId);

        const formData = new FormData(event.target);
        const usuario = formData.get('usuario');
        const senha = formData.get('senha');
        const nome_completo = formData.get('nome_completo'); // Ordem ajustada dos campos
        const email = formData.get('email');

        // Verificação de usuário e senha
        if (usuario.includes(' ') || senha.includes(' ')) {
            throw new Error('Usuário e senha não podem conter espaços!');
        }

        // Em seguida, realize o cadastro usando o ID obtido
        const cadastroResult = await realizarCadastro(connectionId, usuario, senha, nome_completo, email); // Ordem ajustada dos campos
        console.log('Resposta do cadastro:', cadastroResult);

        // Armazenar o novo ID no armazenamento local
        if (cadastroResult.data && cadastroResult.data.length > 0) {
            localStorage.setItem('novoID', cadastroResult.data[0]);
        }

        window.location.href = 'index.html'; // Redirecionar para a tela de login
    } catch (error) {
        console.error('Erro durante o processo de cadastro:', error);
        alert(`Erro: ${error.message}`);
    }
});
