import { set_img_url } from './API/apiConnection.js';

document.getElementById('btn-produtos').addEventListener('click', function() {
    location.href = 'telaDeStocks.html';
});

document.addEventListener("DOMContentLoaded", function () {
    let currentStream = null;
    let imageDataURL = null; // Armazena a imagem atual selecionada ou capturada

    const modal = document.getElementById("modal-camera");
    const btnAbrirModal = document.getElementById("btn-abrir-modal");
    const btnFecharModal = document.getElementById("btn-fechar-modal");
    const btnAbrirCamera = document.getElementById("btn-abrir-camera");
    const btnCapturar = document.getElementById("btn-capturar");
    const btnEnviar = document.getElementById("btn-enviar-modal");
    const fileInput = document.getElementById("file-input");
    const videoSelect = document.getElementById("video-source-modal");
    const video = document.getElementById("video");
    const canvas = document.getElementById("canvas");
    const fotoPerfil = document.getElementById("foto-perfil");

    // Função para listar as câmeras disponíveis
    function getDevices() {
        navigator.mediaDevices.enumerateDevices()
        .then(function (devices) {
            videoSelect.innerHTML = ''; // Limpa opções anteriores
            devices.forEach(function (device) {
                if (device.kind === 'videoinput') {
                    const option = document.createElement('option');
                    option.value = device.deviceId;
                    option.text = device.label || `Câmera ${videoSelect.length + 1}`;
                    videoSelect.appendChild(option);
                }
            });
        })
        .catch(err => console.error("Erro ao listar dispositivos: " + err));
    }

    // Função para iniciar a câmera
    function startVideo(deviceId) {
        if (currentStream) {
            currentStream.getTracks().forEach(track => track.stop()); // Para a stream anterior
        }

        const constraints = {
            video: {
                deviceId: deviceId ? { exact: deviceId } : undefined
            }
        };

        navigator.mediaDevices.getUserMedia(constraints)
        .then(stream => {
            currentStream = stream;
            video.srcObject = stream;
        })
        .catch(err => console.error("Erro ao acessar a câmera: " + err));
    }

    // Abrir o modal ao clicar na foto de perfil
    btnAbrirModal.addEventListener("click", function () {
        modal.style.display = "flex";
        getDevices(); // Atualiza lista de câmeras
    });

    // Iniciar a câmera ao clicar no botão "Abrir Câmera"
    btnAbrirCamera.addEventListener("click", function () {
        startVideo(videoSelect.value);
    });

    // Capturar foto da câmera
    btnCapturar.addEventListener("click", function () {
        const context = canvas.getContext("2d");
        canvas.width = video.videoWidth;
        canvas.height = video.videoHeight;
        context.drawImage(video, 0, 0, canvas.width, canvas.height);

        // Converter para imagem e armazenar
        imageDataURL = canvas.toDataURL("image/png");
        fotoPerfil.src = imageDataURL;
    });

    // Escolher foto do arquivo
    fileInput.addEventListener("change", function (event) {
        const file = event.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function (e) {
                imageDataURL = e.target.result;
                fotoPerfil.src = imageDataURL;
            };
            reader.readAsDataURL(file);
        }
    });

    // Botão "Enviar" - Envia a imagem e fecha o modal
    btnEnviar.addEventListener("click", async function () {
        if (imageDataURL) {
            await enviarImagem(imageDataURL);
            closeModal();
        } else {
            console.error("Nenhuma imagem foi selecionada ou capturada.");
        }
    });

    // Função para enviar a imagem para o servidor
    async function enviarImagem(imageURL) {
        try {
            console.log("Enviando imagem para o servidor...");
            const response = await set_img_url(imageURL);
            console.log("Resposta do servidor:", response);
        } catch (error) {
            console.error("Erro ao enviar imagem:", error);
        }
    }

    // Fechar modal ao clicar no botão "Fechar"
    btnFecharModal.addEventListener("click", closeModal);

    // Função para fechar o modal e desligar a câmera
    function closeModal() {
        if (currentStream) {
            currentStream.getTracks().forEach(track => track.stop()); // Desliga a câmera
            currentStream = null;
        }
        modal.style.display = "none";
        imageDataURL = null; // Reseta a imagem armazenada
    }
});