// cadastro.js
import { getConnectionId, realizarCadastro } from '../API/apiConnection.js';

// Manipula o envio do formulário de cadastro
document.getElementById('cadastroForm').addEventListener('submit', async function (event) {
    event.preventDefault();

    try {
        // Obtenha o ID de conexão primeiro
        const connectionId = await getConnectionId();
        console.log('ID de conexão recebido:', connectionId);

        const formData = new FormData(event.target);
        const usuario = formData.get('usuario');
        const senha = formData.get('senha');
        const email = formData.get('email');
        const nome_completo = formData.get('nome_completo');

        // Verificação de usuário e senha
        if (usuario.includes(' ') || senha.includes(' ')) {
            throw new Error('Usuário e senha não podem conter espaços!');
        }

        // Em seguida, realize o cadastro usando o ID obtido
        const cadastroResult = await realizarCadastro(connectionId, usuario, senha, email, nome_completo);
        console.log('Resposta do cadastro:', cadastroResult);
        alert('Cadastro realizado com sucesso!');
        window.location.href = 'index.html'; // Redirecionar para a tela de login
    } catch (error) {
        console.error('Erro durante o processo de cadastro:', error);
        alert(`Erro: ${error.message}`);
    }
});
