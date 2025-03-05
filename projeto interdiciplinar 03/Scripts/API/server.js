const express = require('express');
const cors = require('cors');
const fetch = (...args) => import('node-fetch').then(({ default: fetch }) => fetch(...args)); // Corrige a importação do node-fetch
const app = express();
const path = require('path'); 

const baseUrl = 'http://44.203.201.20';

app.use(cors());
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ limit: '10mb', extended: true }));

app.use(express.static(path.join(__dirname, 'public')));

// Rota básica para verificar se o servidor está funcionando
app.get('/', (req, res) => {
    res.send('Servidor funcionando corretamente!');
});

// Endpoint para obter o ID de conexão
app.post('/give', async (req, res) => {
    try {
        console.log('Recebendo solicitação para /give');
        const response = await fetch(`${baseUrl}/give`, {
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
        const response = await fetch(`${baseUrl}/cadastro`, {
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
        const response = await fetch(`${baseUrl}/login`, {
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

// Endpoint para obter todos os usuários
app.post('/get_all_user', async (req, res) => {
    const { id, usuario, senha } = req.body;

    try {
        console.log('Recebendo solicitação para /get_all_user:', req.body);
        const response = await fetch(`${baseUrl}/get_all_user`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, usuario, senha })
        });

        const responseBody = await response.text(); // Lê a resposta como texto
        console.log('Resposta completa do servidor /get_all_user:', response.status, responseBody);

        if (!response.ok) {
            console.error('Erro no get_all_user:', response.status, response.statusText);
            return res.status(response.status).json({ error: 'Erro ao obter usuários' });
        }

        const data = JSON.parse(responseBody); // Faz o parse da resposta como JSON
        res.json(data);
    } catch (error) {
        console.error('Erro no servidor ao obter usuários:', error);
        res.status(500).json({ error: 'Erro ao obter usuários' });
    }
});

// Endpoint para excluir um usuário
app.post('/delete_user', async (req, res) => {
    const { id, usuario } = req.body;

    try {
        console.log('Recebendo solicitação para /delete_user:', req.body);
        
        const response = await fetch(`${baseUrl}/delete_user/`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, usuario })
        });

        if (!response.ok) {
            console.error('Erro ao excluir usuário:', response.status, response.statusText);
            return res.status(response.status).json({ error: 'Erro ao excluir usuário' });
        }

        const data = await response.json(); // Lê a resposta diretamente como JSON
        console.log('Resposta completa do servidor /delete_user:', response.status, data);

        res.json(data);
    } catch (error) {
        console.error('Erro no servidor ao excluir usuário:', error);
        res.status(500).json({ error: 'Erro ao excluir usuário' });
    }
});

// Endpoint para esqueci a senha
app.post('/recover/', async (req, res) => {
    const { id, usuario } = req.body;

    try {
        console.log('Recebendo solicitação para /recover:', req.body);
        
        const response = await fetch(`${baseUrl}/recover/`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, usuario })
        });

        if (!response.ok) {
            console.error('Erro ao redefinir senha usuário:', response.status, response.statusText);
            return res.status(response.status).json({ error: 'Erro ao redefinir senha' });
        }

        const data = await response.json(); // Lê a resposta diretamente como JSON
        console.log('Resposta completa do servidor /recover:', response.status, data);

        res.json(data);
    } catch (error) {
        console.error('Erro no servidor ao redefinir senha:', error);
        res.status(500).json({ error: 'Erro ao redefinir senha' });
    }
});

app.post('/charge/', async (req, res) => {
    const { id, usuario, senha, nova_senha } = req.body;

    try {
        console.log('Recebendo solicitação para /recover:', req.body);
        
        const response = await fetch(`${baseUrl}/charge/`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, usuario, senha, nova_senha })
        });

        if (!response.ok) {
            console.error('Erro ao mudar senha:', response.status, response.statusText);
            return res.status(response.status).json({ error: 'Erro ao mudar senha' });
        }

        const data = await response.json(); // Lê a resposta diretamente como JSON
        console.log('Resposta completa do servidor /charge:', response.status, data);

        res.json(data);
    } catch (error) {
        console.error('Erro no servidor ao mudar senha:', error);
        res.status(500).json({ error: 'Erro ao mudar senha' });
    }
});

app.post('/set_img_url/', async (req, res) => {
    const { id, url_foto } = req.body;

    try {
        console.log('Recebendo solicitação para /set_img_url:', req.body);
        
        const response = await fetch(`${baseUrl}/set_img_url/`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, url_foto })
        });

        if (!response.ok) {
            console.error('Erro ao mandar foto:', response.status, response.statusText);
            return res.status(response.status).json({ error: 'Erro ao mandar foto' });
        }

        const data = await response.json(); // Lê a resposta diretamente como JSON
        console.log('Resposta completa do servidor /set_img_url:', response.status, data);

        res.json(data);
    } catch (error) {
        console.error('Erro no servidor ao mandar foto:', error);
        res.status(500).json({ error: 'Erro ao mandar foto' });
    }
});

app.post('/upload_img', async (req, res) => {
    const { id, file, destino } = req.body;

    try {
        console.log('Recebendo solicitação para /upload_img:', req.body);
        const response = await fetch(`${baseUrl}/upload_img`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, file, destino })
        });

        const responseBody = await response.text(); // Lê a resposta como texto
        console.log('Resposta completa do servidor /upload_img:', response.status, responseBody);

        if (!response.ok) {
            console.error('Erro no upload_img:', response.status, response.statusText);
            return res.status(response.status).json({ error: 'Erro ao obter usuários' });
        }

        const data = JSON.parse(responseBody); // Faz o parse da resposta como JSON
        res.json(data);
    } catch (error) {
        console.error('Erro no servidor ao obter upload:', error);
        res.status(500).json({ error: 'Erro ao obter upload' });
    }
});


app.get('/ativar/:usuario/:codigo', async (req, res) => {
    const { usuario, codigo } = req.params;

    try {
        console.log('Recebendo solicitação para /ativar:', req.params);

        const response = await fetch(`${baseUrl}/ativar/${usuario}/${codigo}`, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        });

        if (!response.ok) {
            console.error('Erro ao ativar conta:', response.status, response.statusText);
            return res.status(response.status).json({ error: 'Erro ao ativar conta' });
        }

        const contentType = response.headers.get('content-type');
        if (!contentType || !contentType.includes('application/json')) {
            // Se a resposta não for JSON, devolva o HTML como está
            const text = await response.text();
            console.log('Resposta do servidor é HTML:', text);

            // Verifique se o HTML contém a mensagem de sucesso
            if (text.includes('Operação realizada com sucesso')) {
                return res.status(200).json({ status: 'sucesso' });
            } else {
                return res.status(200).json({ status: 'erro', mensagem: 'Erro na ativação' });
            }
        }

        const data = await response.json();
        console.log('Resposta completa do servidor /ativar:', response.status, data);

        res.json(data);
    } catch (error) {
        console.error('Erro no servidor ao ativar conta:', error);
        res.status(500).json({ error: 'Erro ao ativar conta' });
    }
});

app.post('/delete_user', async (req, res) => {
    const { id, usuario } = req.body;

    try {
        console.log('Recebendo solicitação para /delete_user:', req.body);
        
        const response = await fetch(`${baseUrl}/delete_user/`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, usuario })
        });

        if (!response.ok) {
            console.error('Erro ao excluir usuário:', response.status, response.statusText);
            return res.status(response.status).json({ error: 'Erro ao excluir usuário' });
        }

        const data = await response.json(); // Lê a resposta diretamente como JSON
        console.log('Resposta completa do servidor /delete_user:', response.status, data);

        res.json(data);
    } catch (error) {
        console.error('Erro no servidor ao excluir usuário:', error);
        res.status(500).json({ error: 'Erro ao excluir usuário' });
    }
});

app.post('/get_user_data', async (req, res) => {
    const { id, usuario } = req.body;

    try {
        console.log('Recebendo solicitação para /get_user_data:', req.body);
        
        const response = await fetch(`${baseUrl}/get_user_data/`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, usuario })
        });

        if (!response.ok) {
            console.error('Erro ao obter todos os dados de usuários:', response.status, response.statusText);
            return res.status(response.status).json({ error: 'Erro ao obter todos os dados de usuários:' });
        }

        const data = await response.json(); // Lê a resposta diretamente como JSON
        console.log('Resposta completa do servidor /delete_user:', response.status, data);

        res.json(data);
    } catch (error) {
        console.error('Erro no servidor ao obter todos os dados de usuários:', error);
        res.status(500).json({ error: 'Erro ao obter todos os dados de usuários:' });
    }
});

app.post('/get_dados', async (req, res) => {
    const { id } = req.body;

    try {
        console.log('Recebendo solicitação para /get_dados:', req.body);
        
        const response = await fetch(`${baseUrl}/get_dados/`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id })
        });

        if (!response.ok) {
            console.error('Erro ao obter todos os dados de usuário:', response.status, response.statusText);
            return res.status(response.status).json({ error: 'Erro ao obter todos os dados de usuário:' });
        }

        const data = await response.json(); // Lê a resposta diretamente como JSON
        console.log('Resposta completa do servidor /get_dados:', response.status, data);

        res.json(data);
    } catch (error) {
        console.error('Erro no servidor ao obter todos os dados de usuário:', error);
        res.status(500).json({ error: 'Erro ao obter todos os dados de usuário:' });
    }
});

app.post('/set_user_data', async (req, res) => {
    const { id, usuario, senha } = req.body;

    try {
        console.log('Recebendo solicitação para /set_user_data:', req.body);
        
        const response = await fetch(`${baseUrl}/set_user_data/`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, usuario, senha })
        });

        if (!response.ok) {
            console.error('Erro ao editar usuário:', response.status, response.statusText);
            return res.status(response.status).json({ error: 'Erro ao editar usuário' });
        }

        const data = await response.json(); // Lê a resposta diretamente como JSON
        console.log('Resposta completa do servidor /set_user_data:', response.status, data);

        res.json(data);
    } catch (error) {
        console.error('Erro no servidor ao editar usuário:', error);
        res.status(500).json({ error: 'Erro ao editar usuário' });
    }
});

app.post('/criar_estoque', async (req, res) => {
    const { id, nome, descricao, imagem } = req.body;

    // Verificação de dados obrigatórios
    if (!id || !nome || !descricao || !imagem) {
        return res.status(400).json({ error: 'Todos os campos são obrigatórios!' });
    }

    try {
        console.log('Recebendo solicitação para /criar_estoque:', req.body);

        const response = await fetch(`${baseUrl}/criar_estoque/`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, nome, descricao, imagem }) // Enviando os dados corretamente
        });

        // Verificar o tipo da resposta antes de processar
        const contentType = response.headers.get('content-type');
        let responseBody;
        
        if (contentType && contentType.includes('application/json')) {
            responseBody = await response.json(); // Se for JSON, processa como JSON
        } else {
            responseBody = await response.text(); // Se for texto, processa como texto
        }

        console.log('Resposta completa do servidor /criar_estoque:', response.status, responseBody);

        if (!response.ok) {
            console.error('Erro no criar_estoque:', response.status, response.statusText);
            return res.status(response.status).json({ error: 'Erro ao criar estoque' });
        }

        res.json(responseBody);
    } catch (error) {
        console.error('Erro no servidor ao criar estoque:', error);
        res.status(500).json({ error: 'Erro interno ao criar estoque' });
    }
});

app.post('/get_estoque', async (req, res) => {
    const { id } = req.body;

    try {
        console.log('Recebendo solicitação para /get_estoque:', req.body);
        
        const response = await fetch(`${baseUrl}/get_estoque/`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id })
        });

        if (!response.ok) {
            console.error('Erro ao obter todos os dados do estoque:', response.status, response.statusText);
            return res.status(response.status).json({ error: 'Erro ao obter todos os dados  do estoque:' });
        }

        const data = await response.json(); // Lê a resposta diretamente como JSON
        console.log('Resposta completa do servidor /get_estoque:', response.status, data);

        res.json(data);
    } catch (error) {
        console.error('Erro no servidor ao obter todos os dados  do estoque:', error);
        res.status(500).json({ error: 'Erro ao obter todos os dados  do estoque:' });
    }
});

app.post('/registro_produto', async (req, res) => {
    const { id, nome, descricao, imagem } = req.body;

    try {
        console.log('Recebendo solicitação para /registro_produto:', req.body);

        const response = await fetch(`${baseUrl}/registro_produto/`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, nome, descricao, imagem }) // Enviando os dados corretamente
        });

        // Verificar o tipo da resposta antes de processar
        const contentType = response.headers.get('content-type');
        let responseBody;
        
        if (contentType && contentType.includes('application/json')) {
            responseBody = await response.json();
        } else {
            responseBody = await response.text();
        }

        console.log('Resposta completa do servidor /registro_produto:', response.status, responseBody);

        if (!response.ok) {
            console.error('Erro no registro_produto:', response.status, response.statusText);
            return res.status(response.status).json({ error: 'Erro ao criar produto' });
        }

        res.json(responseBody);
    } catch (error) {
        console.error('Erro no servidor ao criar produto:', error);
        res.status(500).json({ error: 'Erro interno ao criar produto' });
    }
});

app.post('/registro_produto_estoque', async (req, res) => {
    const { id, id_estoque, id_produto, quantidade, data_validade, preco } = req.body;

    try {
        console.log('Recebendo solicitação para /registro_produto_estoque:', req.body);

        const response = await fetch(`${baseUrl}/registro_produto_estoque/`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, id_estoque, id_produto, quantidade, data_validade, preco }) // Enviando os dados corretamente
        });

        // Verificar o tipo da resposta antes de processar
        const contentType = response.headers.get('content-type');
        let responseBody;
        
        if (contentType && contentType.includes('application/json')) {
            responseBody = await response.json();
        } else {
            responseBody = await response.text();
        }

        console.log('Resposta completa do servidor /registro_produto_estoque:', response.status, responseBody);

        if (!response.ok) {
            console.error('Erro no registro_produto_estoque:', response.status, response.statusText);
            return res.status(response.status).json({ error: 'Erro ao inserir produto' });
        }

        res.json(responseBody);
    } catch (error) {
        console.error('Erro no servidor ao inserir produto:', error);
        res.status(500).json({ error: 'Erro interno ao inserir produto' });
    }
});

app.post('/apagar_estoque', async (req, res) => {
    const { id, id_estoque } = req.body;

    try {
        console.log('Recebendo solicitação para /apagar_estoque:', req.body);
        
        const response = await fetch(`${baseUrl}/apagar_estoque/`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, id_estoque })
        });

        if (!response.ok) {
            console.error('Erro ao excluir estoque:', response.status, response.statusText);
            return res.status(response.status).json({ error: 'Erro ao excluir estoque' });
        }

        const data = await response.json(); // Lê a resposta diretamente como JSON
        console.log('Resposta completa do servidor /apagar_estoque:', response.status, data);

        res.json(data);
    } catch (error) {
        console.error('Erro no servidor ao excluir estoque:', error);
        res.status(500).json({ error: 'Erro ao excluir estoque' });
    }
});

app.post('/mudar_produto', async (req, res) => {
    const { id, id_produto, id_estoque, quantidade } = req.body;

    try {
        console.log('Recebendo solicitação para /mudar_produto:', req.body);

        const response = await fetch(`${baseUrl}/mudar_produto/`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, id_produto, id_estoque, quantidade }) // Enviando os dados corretamente
        });

        // Verificar o tipo da resposta antes de processar
        const contentType = response.headers.get('content-type');
        let responseBody;
        
        if (contentType && contentType.includes('application/json')) {
            responseBody = await response.json();
        } else {
            responseBody = await response.text();
        }

        console.log('Resposta completa do servidor /mudar_produto:', response.status, responseBody);

        if (!response.ok) {
            console.error('Erro no mudar_produto:', response.status, response.statusText);
            return res.status(response.status).json({ error: 'Erro ao mudar produto' });
        }

        res.json(responseBody);
    } catch (error) {
        console.error('Erro no servidor ao mudar produto:', error);
        res.status(500).json({ error: 'Erro interno ao mudar produto' });
    }
});

app.post('/charge_estoque_url', async (req, res) => {
    const { id, url_foto, id_estoque } = req.body;

    try {
        console.log('Recebendo solicitação para /charge_estoque_url:', req.body);
        
        const response = await fetch(`${baseUrl}/charge_estoque_url/`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, url_foto, id_estoque })
        });

        if (!response.ok) {
            console.error('Erro ao mudar url de estoque:', response.status, response.statusText);
            return res.status(response.status).json({ error: 'Erro ao  mudar url de estoque' });
        }

        const data = await response.json(); // Lê a resposta diretamente como JSON
        console.log('Resposta completa do servidor /charge_estoque_url:', response.status, data);

        res.json(data);
    } catch (error) {
        console.error('Erro no servidor ao  mudar url de estoque:', error);
        res.status(500).json({ error: 'Erro ao  mudar url de estoque' });
    }
});

app.post('/cadastrar_camera', async (req, res) => {
    const { id, nome, descricao, id_estoque } = req.body;

    try {
        console.log('Recebendo solicitação para /cadastrar_camera:', req.body);

        const response = await fetch(`${baseUrl}/cadastrar_camera/`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, nome, descricao, id_estoque }) // Enviando os dados corretamente
        });

        // Verificar o tipo da resposta antes de processar
        const contentType = response.headers.get('content-type');
        let responseBody;
        
        if (contentType && contentType.includes('application/json')) {
            responseBody = await response.json();
        } else {
            responseBody = await response.text();
        }

        console.log('Resposta completa do servidor /cadastrar_camera:', response.status, responseBody);

        if (!response.ok) {
            console.error('Erro no cadastrar_camera:', response.status, response.statusText);
            return res.status(response.status).json({ error: 'Erro ao criar camera' });
        }

        res.json(responseBody);
    } catch (error) {
        console.error('Erro no servidor ao criar camera:', error);
        res.status(500).json({ error: 'Erro interno ao criar camera' });
    }
});

app.post('/logout', async (req, res) => {
    const { id } = req.body;

    try {
        console.log('Recebendo solicitação para /logout:', req.body);
        
        const response = await fetch(`${baseUrl}/logout/`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id })
        });

        if (!response.ok) {
            console.error('Erro ao fazer logout:', response.status, response.statusText);
            return res.status(response.status).json({ error: 'Erro ao fazer logout:' });
        }

        const data = await response.json(); // Lê a resposta diretamente como JSON
        console.log('Resposta completa do servidor /logout:', response.status, data);

        res.json(data);
    } catch (error) {
        console.error('Erro no servidor ao fazer logout:', error);
        res.status(500).json({ error: 'Erro ao fazer logout:' });
    }
});

// Inicia o servidor na porta 3000
app.listen(3000, () => {
    console.log('Servidor rodando na porta 3000');
});