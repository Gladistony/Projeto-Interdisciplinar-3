const express = require('express');
const cors = require('cors');
const fetch = require('node-fetch'); // Adiciona o node-fetch para fazer requisições HTTP
const app = express();

app.use(cors());
app.use(express.json());

// Endpoint para obter o ID de conexão
app.post('/give', async (req, res) => {
    try {
        console.log('Recebendo solicitação para /give');
        const response = await fetch('http://44.203.201.20/give', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id: "null" }) // Envia o valor null corretamente como JSON
        });

        const responseBody = await response.text(); // Lê a resposta como texto
        console.log('Resposta completa do servidor /give:', response.status, responseBody);

        if (!response.ok) {
            console.error('Erro ao obter ID:', response.status, response.statusText);
            return res.status(response.status).json({ error: 'Erro ao obter ID de conexão' });
        }

        const data = JSON.parse(responseBody); // Faz o parse da resposta como JSON
        res.json(data);
    } catch (error) {
        console.error('Erro no servidor ao obter ID:', error);
        res.status(500).json({ error: 'Erro ao obter ID de conexão' });
    }
});

// Endpoint para realizar o cadastro
app.post('/cadastro', async (req, res) => {
    const { id, usuario, senha, email, nome_completo } = req.body;

    try {
        console.log('Recebendo dados para cadastro:', req.body);
        const response = await fetch('http://44.203.201.20/cadastro', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, usuario, senha, email, nome_completo })
        });

        const responseBody = await response.text(); // Lê a resposta como texto
        console.log('Resposta completa do servidor /cadastro:', response.status, responseBody);

        if (!response.ok) {
            console.error('Erro no cadastro:', response.status, response.statusText);
            return res.status(response.status).json({ error: 'Erro ao realizar cadastro' });
        }

        const data = JSON.parse(responseBody); // Faz o parse da resposta como JSON
        res.json(data);
    } catch (error) {
        console.error('Erro no servidor ao realizar cadastro:', error);
        res.status(500).json({ error: 'Erro ao realizar cadastro' });
    }
});

app.listen(3000, () => {
    console.log('Servidor rodando na porta 3000');
});