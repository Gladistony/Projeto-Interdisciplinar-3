import { getConnectionId, realizarLogin, getDadosUsuario } from './apiConnection.js';

// Manipula o envio do formulário de login
document.getElementById('loginForm').addEventListener('submit', async function (event) {
    event.preventDefault();

    try {
        // Obtenha o ID de conexão armazenado
        let connectionId = localStorage.getItem('connectionId');
        if (!connectionId) {
            // Solicitar um novo ID de conexão se não houver um armazenado
            connectionId = await getConnectionId();
            localStorage.setItem('connectionId', connectionId);
            console.log('Novo ID de conexão obtido:', connectionId);
        }

        const formData = new FormData(event.target);
        const usuario = formData.get('usuario');
        const senha = formData.get('senha');

        // Realize o login usando o ID obtido
        const loginResult = await realizarLogin(connectionId, usuario, senha);
        console.log('Resposta do login:', loginResult);

        if (loginResult.status === "sucesso" || loginResult.code === 14) { // Verifica sucesso ou usuário já logado
            // Armazenar os dados do usuário no armazenamento local
            const dadosUsuario = loginResult.data; // Utilize os dados retornados diretamente
            localStorage.setItem('dadosUsuario', JSON.stringify(dadosUsuario));
            
            if (loginResult.status === "sucesso") {
                // Verificar se há um novo ID armazenado e exibir a mensagem
                const novoID = localStorage.getItem('novoID');
                if (novoID) {
                    alert(`Login realizado com sucesso! Seu novo ID é: ${novoID}`);
                    localStorage.removeItem('novoID'); // Limpar o ID armazenado após exibir a mensagem
                } else {
                    alert('Login realizado com sucesso!');
                }
            } else if (loginResult.code === 14) {
                alert('Usuário já está logado. Redirecionando para a tela de home.');
            }

            window.location.replace('../Paginas/telaDeHomel.html'); // Redirecionar para a tela de home saindo de duas pastas
        } else if (loginResult.code === 3) {
            alert('Conta não está ativa. Por favor, ative sua conta.');
            window.location.replace('../Paginas/telaDeAtivacao.html'); // Redirecionar para a tela de ativação
        } else {
            throw new Error(loginResult.message);
        }
    } catch (error) {
        console.error('Erro durante o processo de login:', error);
        alert(`Erro: ${error.message}`);
    }
});
