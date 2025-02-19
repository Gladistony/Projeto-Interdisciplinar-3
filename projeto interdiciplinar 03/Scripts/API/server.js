const express = require('express');
const cors = require('cors');
const fetch = (...args) => import('node-fetch').then(({default: fetch}) => fetch(...args)); // Corrige a importação do node-fetch
const app = express();

app.use(cors());
app.use(express.json());

// Rota básica para verificar se o servidor está funcionando
app.get('/', (req, res) => {
    res.send('Servidor funcionando corretamente!');
});

// Endpoint para obter o ID de conexão
app.post('/give', async (req, res) => {
    try {
        console.log('Recebendo solicitação para /give');
        const response = await fetch('http://44.203.201.20/give', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id: "null" }) // Envia o valor "null" como string
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
    const { id, usuario, senha, nome_completo, email } = req.body; // Ordem ajustada dos campos

    try {
        console.log('Recebendo dados para cadastro:', req.body);
        const response = await fetch('http://44.203.201.20/cadastro', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, usuario, senha, nome_completo, email }) // Ordem ajustada dos campos
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

// Endpoint para realizar o login
app.post('/login', async (req, res) => {
    const { id, usuario, senha } = req.body;

    try {
        console.log('Recebendo dados para login:', req.body);
        const response = await fetch('http://44.203.201.20/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, usuario, senha })
        });

        const responseBody = await response.text(); // Lê a resposta como texto
        console.log('Resposta completa do servidor /login:', response.status, responseBody);

        if (!response.ok) {
            console.error('Erro no login:', response.status, response.statusText);
            return res.status(response.status).json({ error: 'Erro ao realizar login' });
        }

        const data = JSON.parse(responseBody); // Faz o parse da resposta como JSON
        res.json(data);
    } catch (error) {
        console.error('Erro no servidor ao realizar login:', error);
        res.status(500).json({ error: 'Erro ao realizar login' });
    }
});

app.post('/get_all_user', async (req, res) => {
    const { id, usuario, senha } = req.body;

    try {
        console.log('Recebendo dados para login:', req.body);
        const response = await fetch('http://44.203.201.20/get_all_user', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, usuario, senha })
        });

        const responseBody = await response.text(); // Lê a resposta como texto
        console.log('Resposta completa do servidor /login:', response.status, responseBody);

        if (!response.ok) {
            console.error('Erro no login:', response.status, response.statusText);
            return res.status(response.status).json({ error: 'Erro ao realizar login' });
        }

        const data = JSON.parse(responseBody); // Faz o parse da resposta como JSON
        res.json(data);
    } catch (error) {
        console.error('Erro no servidor ao realizar login:', error);
        res.status(500).json({ error: 'Erro ao realizar login' });
    }
});


app.listen(3000, () => {
    console.log('Servidor rodando na porta 3000');
});