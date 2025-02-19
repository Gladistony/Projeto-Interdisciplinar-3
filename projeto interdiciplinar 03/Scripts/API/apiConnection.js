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
async function realizarCadastro(id, usuario, senha, email, nome_completo) {
    const cadastroData = {
        id: id,
        usuario: usuario,
        senha: senha,
        email: email,
        nome_completo: nome_completo
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

// Exporta as funções para uso em outros arquivos
export { getConnectionId, realizarCadastro };
