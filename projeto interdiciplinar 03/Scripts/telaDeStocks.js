import { criar_estoque, upload_img, set_img_url } from './apiConnection.js';

document.addEventListener('DOMContentLoaded', () => {
    const addButton = document.getElementById('add-stock');
    const containerBox = document.getElementById('container-box');
    const modal = document.getElementById('modal-stock');
    const addStockButton = document.getElementById('btn-add-stock');
    const closeModalButton = document.getElementById('btn-close-modal');
    const stockNameInput = document.getElementById('stock-name');
    const stockDescriptionInput = document.getElementById('stock-description');
    const stockImageInput = document.getElementById('stock-image-file'); // Novo input de arquivo

    addButton.addEventListener('click', () => {
        modal.style.display = 'block';
    });

    closeModalButton.addEventListener('click', () => {
        modal.style.display = 'none';
    });

    addStockButton.addEventListener('click', async () => {
        const name = stockNameInput.value.trim();
        const description = stockDescriptionInput.value.trim();
        const file = stockImageInput.files[0];

        if (name && description && file) {
            try {
                // Converte imagem para base64
                const reader = new FileReader();
                reader.readAsDataURL(file);
                reader.onload = async () => {
                    const base64Image = reader.result.split(',')[1]; // Remove o prefixo base64

                    // Envia a imagem para o servidor e obtém a URL
                    const uploadResponse = await upload_img(base64Image, "estoque");
                    if (!uploadResponse.url) {
                        alert("Erro ao fazer upload da imagem!");
                        return;
                    }

                    // Ativa a URL da imagem no sistema
                    await set_img_url(uploadResponse.url);

                    // Cria o estoque no servidor
                    await criar_estoque(name, description, uploadResponse.url);

                    // Atualiza a interface
                    const newBox = document.createElement('button');
                    newBox.className = 'box';
                    newBox.innerHTML = `
                        <div class="clickable-div">
                            <h2>${name}</h2>
                            <p>${description}</p>
                            <img src="${uploadResponse.url}" alt="">
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
                };
            } catch (error) {
                console.error("Erro ao adicionar estoque:", error);
                alert("Erro ao adicionar estoque. Verifique o console para mais detalhes.");
            }
        } else {
            alert('Todos os campos são obrigatórios!');
        }
    });
});