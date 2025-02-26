import { getDadosUsuario } from './apiConnection.js';

document.addEventListener('DOMContentLoaded', async function () {
    try {
        // Obtenha os dados do usuário do armazenamento local
        const dadosUsuario = JSON.parse(localStorage.getItem('dadosUsuario'));
        if (!dadosUsuario) {
            throw new Error('Dados do usuário não encontrados no armazenamento local.');
        }

        console.log('Dados do usuário:', dadosUsuario);

        document.getElementById('nome-perfil').textContent = dadosUsuario.nome_completo;
        document.getElementById('email-perfil').textContent = dadosUsuario.email;

        // Se houver uma URL de foto, atualize a imagem de perfil
        if (dadosUsuario.url_foto) {
            document.querySelector('.conteiner-foto img').src = dadosUsuario.url_foto;
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

document.addEventListener('DOMContentLoaded', function () {
    const configIcon = document.getElementById('config-icon');
    const configMenu = document.getElementById('config-menu');

    // Mostra/esconde o menu ao clicar no ícone de engrenagem
    configIcon.addEventListener('click', function () {
        configMenu.style.display = (configMenu.style.display === 'block') ? 'none' : 'block';
    });

    // Esconde o menu ao clicar fora dele
    document.addEventListener('click', function (event) {
        if (!configIcon.contains(event.target) && !configMenu.contains(event.target)) {
            configMenu.style.display = 'none';
        }
    });

    // Ação do botão "Alterar Senha"
    document.getElementById('btn-alterar-senha').addEventListener('click', function () {
        window.location.href = "telaDeAlterarSenha.html"; // Redireciona para a página de alteração de senha
    });
});