import { initializeOrUpdateConnectionId } from './apiConnection.js';

// Inicializa ou atualiza o ID de conexão ao carregar a página
document.addEventListener('DOMContentLoaded', async () => {
    await initializeOrUpdateConnectionId();
});

document.addEventListener('DOMContentLoaded', function() {
    const form = document.querySelector('form');
    form.addEventListener('submit', async function(event) {
        event.preventDefault();

        const usuario = document.querySelector('input[type="text"]').value;
        const id = localStorage.getItem('connectionId'); // Obtenha o ID de conexão armazenado

        if (!id) {
            console.error('ID de conexão não encontrado.');
            form.innerHTML = `<p>Erro: ID de conexão não encontrado.</p>`;
            return;
        }

        try {
            const response = await fetch('http://localhost:3000/recover', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ id, usuario })
            });

            if (!response.ok) {
                throw new Error('Erro ao recuperar a senha.');
            }

            const result = await response.json();
            if (result.status === 'sucesso' && result.code === 21) {
                // Limpar o formulário e exibir a mensagem de sucesso
                form.innerHTML = `<p>${result.message}</p>`;
            } else {
                throw new Error(result.message);
            }
        } catch (error) {
            console.error('Erro durante a recuperação de senha:', error);
            form.innerHTML = `<p>Erro: ${error.message}</p>`;
        }
    });
});
