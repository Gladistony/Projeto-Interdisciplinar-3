import { upload_img } from './API/apiConnection.js';

document.addEventListener("DOMContentLoaded", function () {
    let imageFile = null;

    const modal = document.getElementById("modal-camera");
    const btnAbrirModal = document.getElementById("btn-abrir-modal");
    const btnFecharModal = document.getElementById("btn-fechar-modal");
    const btnEnviar = document.getElementById("btn-enviar-modal");
    const fileInput = document.getElementById("file-input");
    const fotoPerfil = document.getElementById("foto-perfil");
    const produtosbtn = document.getElementById("btn-produtos");

    // Abrir modal ao clicar na foto de perfil
    btnAbrirModal.addEventListener("click", function () {
        modal.style.display = "flex";
    });

    produtosbtn.addEventListener("click", function () {
        window.location.href = '../Paginas/telaDeStocks.html';
    });

    // Escolher foto do arquivo
    fileInput.addEventListener("change", function (event) {
        const file = event.target.files[0];
        if (file) {
            imageFile = file;
            const reader = new FileReader();
            reader.onload = function (e) {
                fotoPerfil.src = e.target.result;
            };
            reader.readAsDataURL(file);
        }
    });

    // Botão "Enviar" - Faz o upload da imagem convertendo para Base64
    btnEnviar.addEventListener("click", async function () {
        if (imageFile) {
            const reader = new FileReader();
            reader.onload = async function (event) {
                const base64String = event.target.result.split(",")[1]; // Remove o prefixo "data:image/..."

                try {
                    const uploadedImage = await upload_img(base64String, "perfil");

                    if (uploadedImage && uploadedImage.url) {
                        fotoPerfil.src = uploadedImage.url;
                        console.log("Imagem enviada com sucesso:", uploadedImage.url);
                    } else {
                        console.error("Erro: Resposta do upload sem URL.");
                    }
                } catch (error) {
                    console.error("Erro ao enviar imagem:", error);
                }
                closeModal();
            };

            reader.readAsDataURL(imageFile); // Converte a imagem para Base64
        } else {
            console.error("Nenhuma imagem foi selecionada.");
        }
    });

    // Fechar modal ao clicar no botão "Fechar"
    btnFecharModal.addEventListener("click", closeModal);

    // Função para fechar o modal
    function closeModal() {
        modal.style.display = "none";
        imageFile = null;
    }
});