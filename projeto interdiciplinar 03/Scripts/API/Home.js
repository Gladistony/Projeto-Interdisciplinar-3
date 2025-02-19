import { getDadosUsuario } from './apiConnection.js';

document.addEventListener('DOMContentLoaded', async function () {
    try {
        // Obtenha os dados do usuário
        const dadosUsuario = await getDadosUsuario();
        console.log('Dados do usuário:', dadosUsuario);

        if (dadosUsuario.status === "sucesso") {
            document.getElementById('nome-perfil').textContent = dadosUsuario.nome_completo;
            document.getElementById('email-perfil').textContent = dadosUsuario.email;

            // Se houver uma URL de foto, atualize a imagem de perfil
            if (dadosUsuario.url_foto) {
                document.querySelector('.conteiner-foto img').src = dadosUsuario.url_foto;
            }
        } else {
            throw new Error('Erro ao obter dados do usuário.');
        }
    } catch (error) {
        console.error('Erro ao carregar os dados do usuário:', error);
        alert(`Erro: ${error.message}`);
    }
});
