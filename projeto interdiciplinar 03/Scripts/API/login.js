import { getConnectionId, realizarLogin, getDadosUsuario } from './apiConnection.js';

document.getElementById('loginForm').addEventListener('submit', async function (event) {
    event.preventDefault();

    try {
        let connectionId = localStorage.getItem('connectionId');
        if (!connectionId) {
            connectionId = await getConnectionId();
            localStorage.setItem('connectionId', connectionId);
            console.log('Novo ID de conexão obtido:', connectionId);
        }

        const formData = new FormData(event.target);
        const usuario = formData.get('usuario');
        const senha = formData.get('senha');

        const loginResult = await realizarLogin(connectionId, usuario, senha);
        console.log('Resposta do login:', loginResult);

        if (loginResult.status === "sucesso" || loginResult.code === 14) {
            localStorage.setItem('logado', true);
            const dadosUsuario = loginResult.data;
            localStorage.setItem('dadosUsuario', JSON.stringify(dadosUsuario));
            redirecionarUsuario(dadosUsuario);
        } else if (loginResult.code === 3) {
            window.location.replace('../Paginas/telaDeAtivacao.html');
        } else {
            localStorage.setItem('logado', false);
            throw new Error(loginResult.message);
        }
    } catch (error) {
        console.error('Erro durante o processo de login:', error);
        alert(`Erro: ${error.message}`);
    }
});

function redirecionarUsuario(dadosUsuario) {
    if (dadosUsuario.tipo_conta === "admin") {
        window.location.replace('../Paginas/ADM.html');
    } else {
        window.location.replace('../Paginas/telaDeHomel.html');
    }
}
