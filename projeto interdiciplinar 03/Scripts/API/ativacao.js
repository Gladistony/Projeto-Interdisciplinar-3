import { ativarConta, realizarLogin, getDadosUsuario } from './apiConnection.js';

// Manipula o envio do formulário de ativação
document.getElementById('ativacaoForm').addEventListener('submit', async function (event) {
    event.preventDefault();

    try {
        const formData = new FormData(event.target);
        const id = localStorage.getItem('connectionId'); // Obtenha o ID de conexão armazenado
        const usuario = formData.get('nomeDeAtivacao');
        const senha = formData.get('codigoDeAtivacao');

        // Envie a requisição POST para ativar a conta
        const ativacaoResult = await ativarConta(id, usuario, senha);
        console.log('Resposta da ativação:', ativacaoResult);

        if (ativacaoResult.status === "sucesso") {
            alert('Conta ativada com sucesso!');

            // Realize o login após a ativação para obter os dados do usuário
            const loginResult = await realizarLogin(id, usuario, senha);
            if (loginResult.status === "sucesso") {
                const dadosUsuario = await getDadosUsuario();
                localStorage.setItem('dadosUsuario', JSON.stringify(dadosUsuario));
                window.location.href = '../../Paginas/telaDeHome.html'; // Redirecionar para a tela de home
            } else {
                throw new Error('Erro ao realizar login após ativação.');
            }
        } else {
            throw new Error('Erro na ativação da conta.');
        }
    } catch (error) {
        console.error('Erro durante o processo de ativação:', error);
        alert(`Erro: ${error.message}`);
    }
});
