import { ativarConta } from './apiConnection.js';

// Manipula o envio do formulário de ativação
document.getElementById('ativacaoForm').addEventListener('submit', async function (event) {
    event.preventDefault();

    try {
        const formData = new FormData(event.target);
        const id = localStorage.getItem('connectionId'); // Obtenha o ID de conexão armazenado
        const usuario = document.getElementById('nomeDeAtivacao').value;
        const codigo = document.getElementById('codigoDeAtivacao').value;

        // Envie a requisição GET para ativar a conta
        const ativacaoResult = await ativarConta(id, usuario, codigo);
        console.log('Resposta da ativação:', ativacaoResult);

        if (ativacaoResult.status === "sucesso") {
            alert('Conta ativada com sucesso!');

            // Redirecionar para a tela de home
            window.location.href = '../Paginas/telaDeHomel.html';
        } else {
            throw new Error('Erro na ativação da conta.');
        }
    } catch (error) {
        console.error('Erro durante o processo de ativação:', error);
        alert(`Erro: ${error.message}`);
    }
});
