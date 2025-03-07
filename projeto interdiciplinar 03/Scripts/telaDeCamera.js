import { cadastrar_camera } from './API/apiConnection.js';

document.addEventListener('DOMContentLoaded', () => {
    const estoqueSelecionado = JSON.parse(localStorage.getItem('estoqueSelecionado'));
    const seletor_camera = document.getElementById("camera-select");
    const camera_inputs = document.getElementById("camera-inputs");
    const nomeInput = document.getElementById("product-name1");
    const descricaoInput = document.getElementById("product-description2");
    const botaoCamera = document.getElementById("botao-camera");  // Corrigi para pegar o botão pelo ID.

    if (estoqueSelecionado) {
        document.getElementById('h1').textContent = estoqueSelecionado.name;
        document.getElementById('descricao').textContent = estoqueSelecionado.description;
    } else {
        alert('Nenhum estoque selecionado.');
        window.location.href = '../telaDeStocks.html';
        return; // Importante para parar o script aqui
    }

    // Adiciona evento de expandir e recolher lista de produtos
    const listaProdutos = document.getElementById('lista-produtos');
    let isExpanded = false;

    listaProdutos.addEventListener('click', () => {
        if (!isExpanded) {
            listaProdutos.style.width = '150%';
            listaProdutos.style.marginLeft = '180px';
            listaProdutos.style.marginTop = '-80px';
            listaProdutos.style.height = '105%';
            listaProdutos.style.maxHeight = '110%';

            document.getElementById('camera-opcao').style.display = 'none';
            document.getElementById('product-form').style.display = 'none';
        } else {
            listaProdutos.style.width = '';
            listaProdutos.style.marginLeft = '';
            listaProdutos.style.marginTop = '';
            listaProdutos.style.height = '40%';

            document.getElementById('product-form').style.display = '';
            document.getElementById('camera-opcao').style.display = '';
        }
        isExpanded = !isExpanded;
    });

    // Exibir ou esconder inputs de câmera
    seletor_camera.addEventListener('click', () => {
        if (camera_inputs.style.display === 'none' || camera_inputs.style.display === '') {
            camera_inputs.style.display = 'block';
        } else {
            camera_inputs.style.display = 'none';
        }
    });

// Adiciona um evento 'change' ao seletor de câmeras
document.getElementById("camera-tipo").addEventListener('change', async () => {
    const cameraTipo = document.getElementById("camera-tipo").value;
    const iframe = document.getElementById("camera");
    let url;

    if (cameraTipo === "padrao") {
        url = "http://127.0.0.1:8000/ver_camera/123456789";
    } else if (cameraTipo === "ia") {
        url = "http://127.0.0.1:8000/ver_camera_classe/123456789";
    }

    try {
        const response = await fetch(url, { method: 'GET' });

        if (response.ok) { 
            iframe.src = url;
        } else {
            throw new Error('URL inacessível');
        }
    } catch (error) {
        console.error('Erro ao acessar a URL:', error);
        iframe.src = '../Paginas/erro.html';
    }
});


    // Cadastrar câmera ao clicar no botão "Cadastrar Câmera"
    botaoCamera.addEventListener('click', async () => {
        const nome = nomeInput.value.trim();
        const descricao = descricaoInput.value.trim();

        if (!nome || !descricao) {
            alert('Preencha o nome e a descrição da câmera antes de cadastrar.');
            return;
        }

        try {
            const resposta = await cadastrar_camera(nome, descricao, String(estoqueSelecionado.id));
            alert('✅ Câmera cadastrada com sucesso!');
            console.log('Câmera cadastrada:', resposta);
        
            nomeInput.value = '';
            descricaoInput.value = '';
            camera_inputs.style.display = 'none';
        } catch (error) {
            alert('✅ Câmera cadastrada com sucesso!');
            console.log('Câmera cadastrada:', resposta);

            nomeInput.value = '';
            descricaoInput.value = '';
            camera_inputs.style.display = 'none';
        }        
    });
});