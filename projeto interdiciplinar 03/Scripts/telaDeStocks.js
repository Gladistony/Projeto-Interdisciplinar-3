import { criar_estoque, upload_imgeral } from './API/apiConnection.js';

document.addEventListener('DOMContentLoaded', () => {
    const addButton = document.getElementById('add-stock');
    const containerBox = document.getElementById('container-box');
    const modal = document.getElementById('modal-stock');
    const addStockButton = document.getElementById('btn-add-stock');
    const closeModalButton = document.getElementById('btn-close-modal');
    const stockNameInput = document.getElementById('stock-name');
    const stockDescriptionInput = document.getElementById('stock-description');
    const stockImageInput = document.getElementById('stock-image-file');

    const imagemPadrao = "../IMG/Logo.webp"; // Caminho da imagem padrão

    addButton.addEventListener('click', () => {
        modal.style.display = 'block';
    });

    closeModalButton.addEventListener('click', () => {
        modal.style.display = 'none';
    });

    addStockButton.addEventListener('click', async () => {
        const name = stockNameInput.value.trim();
        const description = stockDescriptionInput.value.trim();

        if (!name || !description) {
            alert('Nome e descrição são obrigatórios!');
            return;
        }

        try {
            let imageUrl = imagemPadrao; // Define imagem padrão por segurança

            if (stockImageInput.files.length > 0) {
                const file = stockImageInput.files[0];

                try {
                    console.log("📸 Imagem recebida:", file.name);
                    const compressedImage = await resizeImage(file, 800, 800);
                    console.log("✅ Imagem comprimida com sucesso!");

                    const uploadResult = await upload_imgeral(compressedImage, "estoque");
                    console.log("🌐 Resposta do servidor:", uploadResult);

                    if (uploadResult.url) {
                        imageUrl = uploadResult.url;
                        console.log("🔗 URL da imagem final:", imageUrl);
                    } else {
                        console.warn("⚠️ Servidor não retornou URL. Usando imagem padrão.");
                    }
                } catch (error) {
                    console.error("❌ Erro no upload da imagem:", error);
                    alert("Erro no upload da imagem. O estoque será criado sem imagem.");
                }
            }

            console.log("📦 Criando estoque com imagem:", imageUrl);
            await adicionarEstoque(name, description, imageUrl);
        } catch (error) {
            console.error("❌ Erro ao adicionar estoque:", error);
            alert("Erro ao adicionar estoque. Verifique o console.");
        }
    });

    async function adicionarEstoque(name, description, imageUrl) {
        try {
            await criar_estoque(name, description, imageUrl);

            const newBox = document.createElement('button');
            newBox.className = 'box';
            newBox.innerHTML = `
                <div class="clickable-div">
                    <h2>${name}</h2>
                    <p>${description}</p>
                    <img src="${imageUrl}" alt="Imagem do Estoque">
                </div>
            `;

            newBox.addEventListener('click', () => {
                window.location.href = 'tela-parcial-camera.html';
            });

            containerBox.appendChild(newBox);
            modal.style.display = 'none';
            stockNameInput.value = '';
            stockDescriptionInput.value = '';
            stockImageInput.value = ''; // Limpa o input de arquivo
        } catch (error) {
            console.error("❌ Erro ao criar estoque:", error);
            alert("Erro ao criar estoque. Verifique o console.");
        }
    }

    // Função para redimensionar a imagem antes do upload
    function resizeImage(file, maxWidth, maxHeight) {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.readAsDataURL(file);

            reader.onload = (event) => {
                const img = new Image();
                img.src = event.target.result;

                img.onload = () => {
                    const canvas = document.createElement("canvas");
                    let width = img.width;
                    let height = img.height;

                    if (width > maxWidth || height > maxHeight) {
                        if (width > height) {
                            height *= maxWidth / width;
                            width = maxWidth;
                        } else {
                            width *= maxHeight / height;
                            height = maxHeight;
                        }
                    }

                    canvas.width = width;
                    canvas.height = height;

                    const ctx = canvas.getContext("2d");
                    ctx.drawImage(img, 0, 0, width, height);

                    console.log("🎨 Imagem redimensionada:", width, "x", height);
                    resolve(canvas.toDataURL("image/jpeg", 0.7));
                };
            };

            reader.onerror = (error) => reject(error);
        });
    }
});