import { verificarAtivacao } from './apiConnection.js';

document.addEventListener('DOMContentLoaded', async function () {
    try {
        const usuarioAtivo = await verificarAtivacao();

        if (usuarioAtivo) {
            window.location.href = '../Paginas/telaDeHomel.html'; // Redirecionar para a tela de ativação
        }
    } catch (error) {
        console.error('Erro durante a verificação da ativação:', error);
    }
});
