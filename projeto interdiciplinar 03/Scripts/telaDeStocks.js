import { criar_estoque, upload_imgeral, getEstoque, apagar_estoque, charge_estoque_url } from './API/apiConnection.js';

document.addEventListener('DOMContentLoaded', async () => {
    const addButton = document.getElementById('add-stock');
    const containerBox = document.getElementById('container-box');
    const modal = document.getElementById('modal-stock');
    const addStockButton = document.getElementById('btn-add-stock');
    const closeModalButton = document.getElementById('btn-close-modal');
    const stockNameInput = document.getElementById('stock-name');
    const stockDescriptionInput = document.getElementById('stock-description');
    const stockImageInput = document.getElementById('stock-image-file');

    const imagemPadrao = "../IMG/Logo.webp";

    // === LISTAR ESTOQUES EXISTENTES ===
    try {
        const resposta = await getEstoque();
        const estoques = resposta.estoque;

        estoques.forEach(estoque => {
            adicionarBoxEstoqueNaTela(estoque.id, estoque.nome, estoque.descricao, estoque.imagem || imagemPadrao);
        });
    } catch (error) {
        console.error('❌ Erro ao carregar estoques existentes:', error);
        alert('Erro ao carregar estoques existentes.');
    }

    // === EVENTOS PARA ABRIR E FECHAR MODAL ===
    addButton.addEventListener('click', () => {
        modal.style.display = 'block';
    });

    closeModalButton.addEventListener('click', () => {
        modal.style.display = 'none';
    });

    // === ADICIONAR NOVO ESTOQUE ===
    addStockButton.addEventListener('click', async () => {
        const name = stockNameInput.value.trim();
        const description = stockDescriptionInput.value.trim();

        if (!name || !description) {
            alert('Nome e descrição são obrigatórios!');
            return;
        }

        try {
            let imageUrl = imagemPadrao;

            if (stockImageInput.files.length > 0) {
                const file = stockImageInput.files[0];

                try {
                    const compressedImage = await resizeImage(file, 800, 800);
                    const uploadResult = await upload_imgeral(compressedImage, "estoque");

                    if (uploadResult.url) {
                        imageUrl = uploadResult.url;
                    }
                } catch (error) {
                    console.error("❌ Erro no upload da imagem:", error);
                    alert("Erro no upload da imagem. O estoque será criado sem imagem.");
                }
            }

            const novoEstoque = await criar_estoque(name, description, imageUrl);

            adicionarBoxEstoqueNaTela(novoEstoque.id, name, description, imageUrl);

            modal.style.display = 'none';
            stockNameInput.value = '';
            stockDescriptionInput.value = '';
            stockImageInput.value = ''; 
        } catch (error) {
            console.error("❌ Erro ao criar estoque:", error);
            alert("Erro ao criar estoque. Verifique o console.");
        }
    });

    // === FUNÇÃO PARA ADICIONAR BOX DE ESTOQUE NA TELA ===
    function adicionarBoxEstoqueNaTela(id, name, description, imageUrl) {
        const newBox = document.createElement('button');
        newBox.className = 'box';
        newBox.innerHTML = `
            <div class="clickable-div">
                <h2>${name}</h2>
                <p>${description}</p>
                <img src="${imageUrl}" alt="Imagem do Estoque" class="estoque-img">
                <div class="actions">
                    <button class="edit-btn">Editar</button>
                    <button class="delete-btn">Deletar</button>
                </div>
            </div>
        `;

        // Redirecionamento ao clicar no box
        newBox.addEventListener('click', () => {
            const estoqueSelecionado = {
                id: id,
                name: name,
                description: description,
                imageUrl: imageUrl
            };
            localStorage.setItem('estoqueSelecionado', JSON.stringify(estoqueSelecionado));
            window.location.href = 'tela-parcial-camera.html';
        });

        // === EVENTO PARA ALTERAR A IMAGEM DO ESTOQUE ===
        newBox.querySelector('.edit-btn').addEventListener('click', async (event) => {
            event.stopPropagation(); // Impede o clique do box ao editar

            // Criar input para selecionar a nova imagem
            const inputFile = document.createElement('input');
            inputFile.type = 'file';
            inputFile.accept = 'image/*';
            inputFile.click();

            inputFile.addEventListener('change', async () => {
                if (inputFile.files.length > 0) {
                    const file = inputFile.files[0];

                    try {
                        const compressedImage = await resizeImage(file, 800, 800);
                        const uploadResult = await upload_imgeral(compressedImage, "estoque");

                        if (uploadResult.url) {
                            const novaUrl = uploadResult.url;

                            await charge_estoque_url(novaUrl, id);
                            console.log(`Imagem do estoque ${name} atualizada com sucesso.`);

                            // Atualizar a imagem na tela
                            newBox.querySelector('.estoque-img').src = novaUrl;
                        }
                    } catch (error) {
                        console.error("❌ Erro ao atualizar a imagem:", error);
                        alert("Erro ao atualizar a imagem do estoque.");
                    }
                }
            });
        });

        // Evento para deletar estoque
        newBox.querySelector('.delete-btn').addEventListener('click', async (event) => {
            event.stopPropagation();
            
            const confirmar = confirm(`Tem certeza que deseja deletar o estoque ${name}?`);
            if (confirmar) {
                try {
                    await apagar_estoque(id.toString());
                    newBox.remove();
                    console.log(`Estoque ${name} deletado com sucesso.`);
                } catch (error) {
                    console.error(`❌ Erro ao deletar o estoque ${name}:`, error);
                    alert(`Erro ao deletar o estoque ${name}. Verifique o console.`);
                }
            }
        });

        containerBox.appendChild(newBox);
    }    

    // === FUNÇÃO PARA REDIMENSIONAR IMAGEM ===
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

                    resolve(canvas.toDataURL("image/jpeg", 0.7).replace(/^data:image\/jpeg;base64,/, ""));
                };
            };

            reader.onerror = (error) => reject(error);
        });
    }
});