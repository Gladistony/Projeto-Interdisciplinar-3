const express = require('express');
const { createProxyMiddleware } = require('http-proxy-middleware');

const app = express();

const targetUrl = 'http://44.203.201.20/give';

app.options('/api/give', (req, res) => {
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
    res.status(200).end(); // Finaliza a requisição com status HTTP 200
});


// Middleware para lidar com CORS
app.use((req, res, next) => {
    res.setHeader('Access-Control-Allow-Origin', '*'); // Permitir todas as origens
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
    next();
});

// Proxy para redirecionar as requisições
app.use('/api/give', createProxyMiddleware({
    target: targetUrl,
    changeOrigin: true,
    pathRewrite: {
        '^/api/give': '', // Remove `/api/give` da URL ao redirecionar
    },
}));

// Inicia o servidor proxy
app.listen(3000, () => {
    console.log('Proxy rodando em http://localhost:3000');
});
