const API_URL = 'http://localhost:3000';

// Função para obter o ID de conexão
async function getConnectionId() {
    try {
        const response = await fetch(`${API_URL}/give`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id: "null" }) // Solicita o ID inicial com a string "null"
        });

        if (!response.ok) {
            throw new Error(`Erro ao obter ID: ${response.statusText}`);
        }

        const data = await response.json();
        return data.id;
    } catch (error) {
        console.error('Erro ao obter o ID de conexão:', error);
        throw error;
    }
}

// Função para realizar o cadastro
async function realizarCadastro(id, usuario, senha, nome_completo, email) { // Ordem ajustada dos campos
    const cadastroData = {
        id: id,
        usuario: usuario,
        senha: senha,
        nome_completo: nome_completo, // Ordem ajustada dos campos
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
async function ativarConta(id, usuario, senha) {
    const ativacaoData = { id, usuario, senha };

    try {
        const response = await fetch(`${API_URL}/ativar`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(ativacaoData)
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

// Exporta as funções para uso em outros arquivos
export { getConnectionId, realizarCadastro, realizarLogin, ativarConta, getDadosUsuario, verificarAtivacao };
