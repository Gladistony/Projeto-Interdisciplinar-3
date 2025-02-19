import { verificarAtivacao } from './apiConnection.js';

document.addEventListener('DOMContentLoaded', async function () {
    try {
        const usuarioAtivo = await verificarAtivacao();

        if (!usuarioAtivo) {
            // Conta não está ativa, redirecionar para a tela de ativação
            alert('Você precisa ativar a sua conta antes de acessar esta página.');
            window.location.href = '../Paginas/telaDeAtivacao.html'; // Redirecionar para a tela de ativação
        }
    } catch (error) {
        console.error('Erro durante a verificação da ativação:', error);
    }
});
