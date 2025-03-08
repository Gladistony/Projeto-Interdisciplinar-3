import { cadastrar_camera, getEstoque } from './API/apiConnection.js';

document.addEventListener('DOMContentLoaded', async () => {
    const estoqueSelecionado = JSON.parse(localStorage.getItem('estoqueSelecionado'));
    let connectionId = localStorage.getItem('connectionId');
    const seletor_camera = document.getElementById("camera-select");
    const camera_inputs = document.getElementById("camera-inputs");
    const nomeInput = document.getElementById("product-name1");
    const descricaoInput = document.getElementById("product-description2");
    const botaoCamera = document.getElementById("botao-camera");

    if (!estoqueSelecionado) {
        alert('Nenhum estoque selecionado.');
        window.location.href = '../telaDeStocks.html';
        return;
    }

    document.getElementById('h1').textContent = estoqueSelecionado.name;
    document.getElementById('descricao').textContent = estoqueSelecionado.description;

    const selectBox = document.getElementById("cameras-estoque");
    selectBox.addEventListener("change", atualizarCamera);

    try {
        const resposta = await getEstoque();
        const estoques = resposta.estoque;
        const estoqueAtual = estoques.find(e => e.id === estoqueSelecionado.id);
    
        if (!estoqueAtual || !estoqueAtual.cameras) {
            console.error('Erro: Estoque não encontrado ou sem câmeras.');
            return;
        }
    
        selectBox.innerHTML = '<option value="">Selecione câmera</option>';
    
        estoqueAtual.cameras.forEach(camera => {
            const option = document.createElement('option');
            option.value = camera.codigo_camera;
            option.textContent = camera.nome;
            selectBox.appendChild(option);
        });
    } catch (error) {
        console.error('Erro ao obter estoque:', error);
    }

    async function atualizarCamera() {
        const codigoCamera = selectBox.value;
        if (!codigoCamera) {
            console.error("Nenhuma câmera selecionada.");
            return;
        }
    
        const cameraTipo = document.getElementById("camera-tipo")?.value || "padrao";
        let url = cameraTipo === "ia" 
            ? `http://127.0.0.1:3000/ver_camera_classe/${codigoCamera}/${connectionId}`
            : `http://127.0.0.1:3000/ver_camera/${codigoCamera}/${connectionId}`;
    
        const iframe = document.getElementById("camera");
    
        if (iframe) {
            iframe.src = url;
        } else {
            console.error("Erro: O iframe não foi encontrado no DOM.");
        }
    }
    
    botaoCamera.addEventListener('click', async () => {
        const nome = nomeInput.value.trim();
        const descricao = descricaoInput.value.trim();

        if (!nome || !descricao) {
            alert('Preencha o nome e a descrição da câmera antes de cadastrar.');
            return;
        }

        try {
            await cadastrar_camera(nome, descricao, String(estoqueSelecionado.id));
            alert('✅ Câmera cadastrada com sucesso!');
            nomeInput.value = '';
            descricaoInput.value = '';
            camera_inputs.style.display = 'none';
        } catch (error) {
            alert('❌ Erro ao cadastrar a câmera. Verifique os dados e tente novamente.');
            console.error('Erro ao cadastrar câmera:', error);
        }
    });
});
