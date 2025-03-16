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

    // Adiciona evento de expandir e recolher lista de produtos
    const listaProdutos = document.getElementById('lista-produtos');
    let isExpanded = false;
    listaProdutos.style.marginRight = '565px';

    listaProdutos.addEventListener('click', () => {
        if (!isExpanded) {
            listaProdutos.style.width = '150%';
            listaProdutos.style.marginLeft = '180px';
            listaProdutos.style.marginTop = '-80px';
            listaProdutos.style.height = '105%';
            listaProdutos.style.maxHeight = '110%';
            listaProdutos.style.marginRight = "0"

            document.getElementById('camera-opcao').style.display = 'none';
            document.getElementById('product-form').style.display = 'none';
            document.getElementById('codigo-camera').style.display = 'none';
        } else {
            listaProdutos.style.width = '';
            listaProdutos.style.marginLeft = '';
            listaProdutos.style.marginTop = '';
            listaProdutos.style.marginRight = '565px';
            listaProdutos.style.height = '40%';

            document.getElementById('product-form').style.display = '';
            document.getElementById('camera-opcao').style.display = '';
            document.getElementById('codigo-camera').style.display = '';
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

// Obter os elementos
const select_Box = document.getElementById("cameras-estoque");
const cameraTipoDropdown = document.getElementById("camera-tipo");
const iframe = document.getElementById("camera");

// Função para atualizar a câmera
async function atualizarCamera() {
    const codigoCamera = select_Box.value;
    const cameraTipo = cameraTipoDropdown.value || "padrao";

    // Verificar se todos os valores necessários foram selecionados
    if (!codigoCamera) {
        console.error("Nenhuma câmera selecionada.");
        return;
    }

    const codigoP = document.getElementById("codigo-camera")
    codigoP.textContent = `Codigo da câmera: ${codigoCamera}`;
    const link = `http://127.0.0.1:3000` //link do server padrão

    // Construir a URL com base no tipo de câmera selecionado
    let url = cameraTipo === "ia" 
        ? `${link}/ver_camera_classe/${codigoCamera}/${connectionId}`
        : `${link}/ver_camera/${codigoCamera}/${connectionId}`;
    
    // Atualizar o src do iframe, se ele existir
    if (iframe) {
        iframe.src = url;
    } else {
        console.error("Erro: O iframe não foi encontrado no DOM.");
    }
}

// Adicionar event listeners para os dois campos
selectBox.addEventListener("change", atualizarCamera);
cameraTipoDropdown.addEventListener("change", atualizarCamera);

    
    botaoCamera.addEventListener('click', async () => {
        const nome = nomeInput.value.trim();
        const descricao = descricaoInput.value.trim();

        if (!nome || !descricao) {
            return;
        }

        try {
            await cadastrar_camera(nome, descricao, String(estoqueSelecionado.id));
            alert('✅ Câmera cadastrada com sucesso!');
            nomeInput.value = '';
            descricaoInput.value = '';
            camera_inputs.style.display = 'none';
        } catch (error) {
            console.error('Erro ao cadastrar câmera:', error);
        }
    });

    const closeproduto = document.querySelector('#closeproduto');
    const camerainputs2 = document.querySelector('#camera-inputs');

    closeproduto.addEventListener("click", () => {
        camerainputs2.style.display = "none";
    });   

    const pegar_video = document.getElementById("pegar_video");
    const videoListContainer = document.getElementById("video-list");
    const videoItems = document.getElementById("video-items");
    const closeList = document.getElementById("close-list");
    
    pegar_video.addEventListener('click', async () => {
        const link1 = `http://127.0.0.1:3000` //link do server padrão
        try {
            const select_Box = document.getElementById("cameras-estoque");
            const camera_id = select_Box.value;
            const connectionId2 = localStorage.getItem('connectionId');
    
            // Verificar se o camera_id foi selecionado
            if (!camera_id) {
                alert("Por favor, selecione uma câmera antes de continuar.");
                return; // Interrompe a execução
            }
    
            // Fazer o GET para obter a lista de vídeos
            const response = await fetch(`${link1}/videos/${camera_id}/${connectionId2}`);
            if (!response.ok) {
                throw new Error("Erro ao buscar listagem de vídeos.");
            }
    
            const data = await response.json();
            const videos = data.videos;
            videoItems.innerHTML = "";

            // Adicionar cada vídeo à lista
            videos.forEach(videoFilename => {
                const videoItem = document.createElement("button");
                videoItem.textContent = videoFilename;
                videoItem.style.cursor = "pointer";
    
                // Adicionar evento de clique para baixar o vídeo
                videoItem.addEventListener("click", async () => {
                    const link1 = `http://127.0.0.1:3000` //link do server padrão
                    try {
                        const downloadResponse = await fetch(`${link1}/video/${camera_id}/${videoFilename}/${connectionId2}`);
                        if (downloadResponse.ok) {
                            const blob = await downloadResponse.blob();
                            const url = window.URL.createObjectURL(blob);
                            const a = document.createElement("a");
                            a.href = url;
                            a.download = videoFilename;
                            document.body.appendChild(a);
                            a.click();
                            a.remove();
                            window.URL.revokeObjectURL(url);
                        } else {
                            throw new Error("Erro ao tentar baixar o vídeo.");
                        }
                    } catch (error) {
                        console.error("Erro no download do vídeo:", error);
                    }
                });
    
                videoItems.appendChild(videoItem);
            });
    
            // Exibir a lista no centro da tela
            videoListContainer.style.display = "flex";
        } catch (error) {
            console.error("Erro ao buscar ou exibir a listagem de vídeos:", error);
        }
    });    
    
    // Evento para fechar a lista
    closeList.addEventListener("click", () => {
        videoListContainer.style.display = "none"; // Esconde a lista
    });
});
