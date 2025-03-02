const API_URL = 'http://localhost:3000';

// Função para obter o ID de conexão
async function getConnectionId() {
    try {
        const response = await fetch(`${API_URL}/give`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id: "null" }) // Solicita o ID inicial
        });

        if (!response.ok) {
            throw new Error(`Erro ao obter ID: ${response.statusText}`);
        }

        const data = await response.json();
        localStorage.setItem('connectionId', data.id); // Salva o ID localmente
        return data.id;
    } catch (error) {
        console.error('Erro ao obter o ID de conexão:', error);
        throw error;
    }
}


// Função para realizar o cadastro
async function realizarCadastro(id, usuario, senha, nome_completo, email) {
    const cadastroData = {
        id: id,
        usuario: usuario,
        senha: senha,
        nome_completo: nome_completo,
        email: email
    };

    try {
        const response = await fetch(`${API_URL}/cadastro`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(cadastroData)
        });

        if (!response.ok) {
            throw new Error(`Erro no cadastro: ${response.statusText}`);
        }

        const result = await response.json();
        return result;
    } catch (error) {
        console.error('Erro durante o processo de cadastro:', error);
        throw error;
    }
}

// Função para realizar o login
async function realizarLogin(id, usuario, senha) {
    const loginData = { id, usuario, senha };

    try {
        const response = await fetch(`${API_URL}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(loginData)
        });

        if (!response.ok) {
            throw new Error(`Erro no login: ${response.statusText}`);
        }

        const result = await response.json();

        // Verifica o status do usuário e armazena se a conta está ativa
        if (result.status === "sucesso" || result.code === 14) {
            localStorage.setItem('usuarioAtivo', true);
            localStorage.setItem('connectionId', id); // Armazena o ID de conexão
        } else {
            localStorage.removeItem('usuarioAtivo');
            localStorage.removeItem('connectionId');
        }

        return result;
    } catch (error) {
        console.error('Erro durante o processo de login:', error);
        throw error;
    }
}

// Função para ativar a conta
async function ativarConta(id, usuario, codigo) {
    try {
        const url = `${API_URL}/ativar/${usuario}/${codigo}`;
        console.log("URL de requisição:", url); // Log para verificar a URL

        const response = await fetch(url, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        });

        if (!response.ok) {
            throw new Error(`Erro na ativação da conta: ${response.statusText}`);
        }

        const result = await response.json();

        // Armazena o status do usuário após a ativação
        if (result.status === "sucesso") {
            localStorage.setItem('usuarioAtivo', true);
            localStorage.setItem('connectionId', id); // Armazena o ID de conexão
        } else {
            localStorage.removeItem('usuarioAtivo');
            localStorage.removeItem('connectionId');
            throw new Error('Erro na ativação da conta.');
        }

        return result;
    } catch (error) {
        console.error('Erro durante o processo de ativação da conta:', error);
        throw error;
    }
}


// Função para obter os dados do usuário
async function getDadosUsuario() {
    const id = localStorage.getItem('connectionId'); // Obtenha o ID de conexão armazenado

    try {
        const response = await fetch(`${API_URL}/get_dados`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id })
        });

        if (!response.ok) {
            throw new Error(`Erro ao obter dados do usuário: ${response.statusText}`);
        }

        const result = await response.json();
        return result;
    } catch (error) {
        console.error('Erro ao obter dados do usuário:', error);
        throw error;
    }
}

// Função para verificar a ativação do usuário
async function verificarAtivacao() {
    const usuarioAtivo = localStorage.getItem('usuarioAtivo');
    return usuarioAtivo === 'true';
}

// Função para obter todos os usuários (apenas para administradores)
async function getAllUsers() {
    const id = localStorage.getItem('connectionId'); // Obtenha o ID de conexão armazenado

    try {
        const response = await fetch(`${API_URL}/get_all_user/`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id })
        });
        const result1 = await response;
        console.log(result1)

        if (!response.ok) {
            throw new Error(`Erro ao obter todos os usuários: ${response.statusText}`);
        }

        const result = await response.json();
        return result;
    } catch (error) {
        console.error('Erro ao obter todos os usuários:', error);
        throw error;
    }
}

// Função para excluir usuário
async function excluirUsuario(id, usuario) {
    const idadm = localStorage.getItem('connectionId'); // ID do administrador

    if (!idadm) {
        throw new Error('ID do administrador não encontrado.');
    }

    try {
        const response = await fetch(`${API_URL}/delete_user/`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, usuario }) // Enviando o ID do admin e o nome do usuário a ser deletado
        });

        if (!response.ok) {
            throw new Error(`Erro ao excluir usuário: ${response.statusText}`);
        }

        return await response.json();
    } catch (error) {
        console.error('Erro ao excluir usuário:', error);
        throw error;
    }
}

async function iniciarRecuperacao(usuario) {
    let idUsuario = localStorage.getItem('connectionId');

    if (!idUsuario) {
        idUsuario = await getConnectionId(); // Obtém o ID se ainda não estiver salvo
    }

    try {
        const result = await recoverSenha(idUsuario, usuario);
        console.log('Recuperação de senha:', result);
        return result;
    } catch (error) {
        console.error('Erro na recuperação de senha:', error);
        throw error;
    }
}


async function recoverSenha(usuario) {
    let id = localStorage.getItem('connectionId');

    if (!id) {
        id = await getConnectionId(); // Obtém o ID se ainda não estiver salvo
        localStorage.setItem('connectionId', id); // Salva para uso futuro
    }

    try {
        const response = await fetch(`${API_URL}/recover`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, usuario }) // Enviando o ID e o usuário corretamente
        });

        if (!response.ok) {
            throw new Error(`Erro ao recuperar senha: ${response.statusText}`);
        }

        return await response.json();
    } catch (error) {
        console.error('Erro ao recuperar senha:', error);
        throw error;
    }
}

async function charge(usuario, senha, nova_senha) {
    const id = localStorage.getItem('connectionId'); // Obtenha o ID de conexão armazenado

    try {
        const response = await fetch(`${API_URL}/charge/`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, usuario, senha, nova_senha })
        });

        if (!response.ok) {
            throw new Error(`Erro ao mudar senha: ${response.statusText}`);
        }

        const result = await response.json();
        return result;
    } catch (error) {
        console.error('Erro ao mudar senha:', error);
        throw error;
    }
}

async function set_img_url(url_foto) {
    let id = localStorage.getItem('connectionId');

    if (!id) {
        id = await getConnectionId(); // Obtém o ID se ainda não estiver salvo
        localStorage.setItem('connectionId', id); // Salva para uso futuro
    }

    try {
        const response = await fetch(`${API_URL}/set_img_url`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, url_foto }) // Enviando o ID e o url corretamente
        });

        if (!response.ok) {
            throw new Error(`Erro ao mandar foto: ${response.statusText}`);
        }

        return await response.json();
    } catch (error) {
        console.error('Erro ao mandar foto:', error);
        throw error;
    }
}

async function upload_img(base64String, destino) {
    const id = localStorage.getItem('connectionId'); // Obtém o ID armazenado

    const payload = {
        id: id,
        destino: destino,
        file: base64String
    };

    try {
        const response = await fetch(`${API_URL}/upload_img/`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            throw new Error(`Erro ao fazer upload: ${response.statusText}`);
        }

        const result = await response.json();

        if (result.url) {
            console.log("Imagem enviada com sucesso:", result.url);

            // Envia a URL da imagem para ativação no sistema
            const setUrlResponse = await set_img_url(result.url);
            console.log("Resposta do set_img_url:", setUrlResponse);
        } else {
            console.error("Erro: Resposta do upload sem URL.");
        }

        return result;
    } catch (error) {
        console.error("Erro ao enviar imagem:", error);
        throw error;
    }
}

async function getUserData(usuario) {
    const id = localStorage.getItem('connectionId'); // Obtenha o ID de conexão armazenado
    if (!id) {
        console.error("ID de conexão não encontrado no localStorage.");
        return;
    }

    try {
        const response = await fetch(`${API_URL}/get_user_data/`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, usuario })
        });

        if (!response.ok) {
            throw new Error(`Erro ao obter os dados do usuário: ${response.statusText}`);
        }

        const result = await response.json();
        console.log("Dados do usuário recebidos:", result);
        return result;
    } catch (error) {
        console.error('Erro ao obter os dados do usuário:', error);
        throw error;
    }
}

async function set_user_data(id, usuario, senha) {
    const idadm = localStorage.getItem('connectionId'); // ID do administrador

    if (!idadm) {
        throw new Error('ID do administrador não encontrado.');
    }

    try {
        const response = await fetch(`${API_URL}/set_user_data/`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, usuario, senha }) // Enviando o ID do admin e o nome do usuário
        });

        if (!response.ok) {
            throw new Error(`Erro ao editar usuário: ${response.statusText}`);
        }

        return await response.json();
    } catch (error) {
        console.error('Erro ao editar usuário:', error);
        throw error;
    }
}

async function upload_imgeral(base64String, destino) {
    const id = localStorage.getItem('connectionId'); // Obtém o ID armazenado

    const payload = {
        id: id,
        destino: destino,
        file: base64String
    };

    try {
        const response = await fetch(`${API_URL}/upload_img/`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            throw new Error(`Erro ao fazer upload: ${response.statusText}`);
        }

        const result = await response.json();
        console.log("Resposta completa do upload:", result);

        if (result.url) {
            console.log("Imagem enviada com sucesso:", result.url);
        } else {
            console.error("Erro: Resposta do upload sem URL.");
        }

        return result;
    } catch (error) {
        console.error("Erro ao enviar imagem:", error);
        throw error;
    }
}

async function criar_estoque(nome, descricao, imagem) {
    const id = localStorage.getItem('connectionId'); // Obtém o ID armazenado

    if (!id) {
        console.error("Erro: ID do usuário não encontrado.");
        throw new Error("ID do usuário não encontrado.");
    }

    try {
        // Criar o estoque diretamente com a URL da imagem ou uma string vazia
        const payload = {
            id: id,
            nome: nome,
            descricao: descricao,
            imagem: imagem // Usando a URL da imagem
        };

        console.log("Payload enviado:", JSON.stringify(payload, null, 2));

        const response = await fetch(`${API_URL}/criar_estoque/`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const errorDetails = await response.json();
            console.error("Detalhes do erro:", errorDetails);
            throw new Error(`Erro ao criar estoque: ${response.statusText}`);
        }

        return await response.json();
    } catch (error) {
        console.error("Erro ao criar estoque:", error);
        throw error;
    }
}

async function getEstoque() {
    const id = localStorage.getItem('connectionId'); // Obtenha o ID de conexão armazenado

    try {
        const response = await fetch(`${API_URL}/get_estoque`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id })
        });

        if (!response.ok) {
            throw new Error(`Erro ao obter dados do estoque: ${response.statusText}`);
        }

        const result = await response.json();
        return result;
    } catch (error) {
        console.error('Erro ao obter dados do estoque:', error);
        throw error;
    }
}

// Exporta as funções para uso em outros arquivos
export { getConnectionId, realizarCadastro, realizarLogin, ativarConta, getDadosUsuario, getAllUsers, excluirUsuario, recoverSenha, iniciarRecuperacao, charge, set_img_url, verificarAtivacao, upload_img, getUserData, set_user_data, criar_estoque, upload_imgeral, getEstoque };