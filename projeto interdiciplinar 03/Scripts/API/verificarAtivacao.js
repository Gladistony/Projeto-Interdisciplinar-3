import { verificarAtivacao } from './apiConnection.js';

document.addEventListener('DOMContentLoaded', async function () {
    try {
        const usuarioAtivo = await verificarAtivacao();

        // Recupera o status do login armazenado no localStorage
        const logado = localStorage.getItem('logado') === 'true';

        if (!usuarioAtivo && logado) {
            window.location.href = '../Paginas/telaDeAtivacao.html'; // Redirecionar para a tela de ativação
        } else {
            console.log("Usuário está ativo ou não está logado.");
        }
    } catch (error) {
        console.error('Erro durante a verificação da ativação:', error);
    }
});