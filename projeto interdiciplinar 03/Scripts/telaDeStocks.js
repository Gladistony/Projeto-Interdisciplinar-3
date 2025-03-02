import { criar_estoque } from './API/apiConnection.js';

document.addEventListener('DOMContentLoaded', () => {
    const addButton = document.getElementById('add-stock');
    const containerBox = document.getElementById('container-box');
    const modal = document.getElementById('modal-stock');
    const addStockButton = document.getElementById('btn-add-stock');
    const closeModalButton = document.getElementById('btn-close-modal');
    const stockNameInput = document.getElementById('stock-name');
    const stockDescriptionInput = document.getElementById('stock-description');
    const stockImageInput = document.getElementById('stock-image-file'); // Input de arquivo

    addButton.addEventListener('click', () => {
        modal.style.display = 'block';
    });

    closeModalButton.addEventListener('click', () => {
        modal.style.display = 'none';
    });

    addStockButton.addEventListener('click', async () => {
        const name = stockNameInput.value.trim();
        const description = stockDescriptionInput.value.trim();

        if (name && description) {
            try {
                // Se o usuário não enviar uma imagem, usa uma string vazia
                if (stockImageInput.files.length > 0) {
                    const file = stockImageInput.files[0];
                    const reader = new FileReader();
                    reader.readAsDataURL(file);

                    reader.onload = async () => {
                        const imageUrl = reader.result; // Usa a imagem convertida para Base64
                        await adicionarEstoque(name, description, imageUrl);
                    };

                    reader.onerror = () => {
                        console.error("Erro ao ler o arquivo");
                        alert("Erro ao ler o arquivo. Tente novamente.");
                    };
                } else {
                    await adicionarEstoque(name, description, "");
                }
            } catch (error) {
                console.error("Erro ao adicionar estoque:", error);
                alert("Erro ao adicionar estoque. Verifique o console para mais detalhes.");
            }
        } else {
            alert('Nome e descrição são obrigatórios!');
        }
    });

    async function adicionarEstoque(name, description, imageUrl) {
        try {
            // Criar estoque sem a necessidade de upload
            await criar_estoque(name, description, imageUrl);

            // Atualiza a interface
            const newBox = document.createElement('button');
            newBox.className = 'box';
            newBox.innerHTML = `
                <div class="clickable-div">
                    <h2>${name}</h2>
                    <p>${description}</p>
                    <img src="${imageUrl || '../IMG/Logo.webp'}" alt="Imagem do Estoque">
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
            console.error("Erro ao criar estoque:", error);
            alert("Erro ao criar estoque. Verifique o console.");
        }
    }
});
