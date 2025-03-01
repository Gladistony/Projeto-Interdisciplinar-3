import { getDadosUsuario } from './apiConnection.js';

document.addEventListener('DOMContentLoaded', async function () {
    try {
        // Obtenha os dados do usuário a partir da função getDadosUsuario
        const dadosUsuario = await getDadosUsuario();
        if (!dadosUsuario) {
            throw new Error('Dados do usuário não encontrados.');
        }

        console.log('Dados do usuário:', dadosUsuario);

        document.getElementById('nome-perfil').textContent = dadosUsuario.nome_completo;
        document.getElementById('email-perfil').textContent = dadosUsuario.email;
        document.getElementById('user-name').textContent = dadosUsuario.usuario;

        // Se houver uma URL de foto, atualize a imagem de perfil
        if (dadosUsuario.url_foto) {
            document.querySelector('.conteiner-foto img').src = dadosUsuario.url_foto;
        }

        // Verificar o tipo de conta e adicionar botão se for administrador
        if (dadosUsuario.tipo_conta === 'admin') {
            const adminButton = document.createElement('button');
            adminButton.textContent = 'ADM';
            adminButton.addEventListener('click', () => {
                window.location.href = 'ADM.html';
            });
            document.body.appendChild(adminButton);
        }
    } catch (error) {
        console.error('Erro ao carregar os dados do usuário:', error);
        alert(`Erro: ${error.message}`);
    }

    // Função para deslogar o usuário
    function deslogarUsuario() {
        localStorage.removeItem('usuarioAtivo');
        localStorage.removeItem('connectionId');
        localStorage.removeItem('dadosUsuario');
        window.location.replace('../Paginas/index.html'); // Redirecionar para a página de login
    }

    // Adiciona o evento de clique ao botão de deslogar
    document.getElementById('deslogar').addEventListener('click', deslogarUsuario);
});
