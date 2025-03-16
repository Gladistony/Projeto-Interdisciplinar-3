# StockView

**Disciplina**: Projeto Interdisciplinar 3 - Engenharia da Computação, UFRPE, Campus UABJ

Este projeto é dedicado à disciplina de Projeto 3 do curso de Engenharia da Computação da UFRPE, Campus UABJ.

## 🎯 Objetivo do Projeto

O projeto StockView tem como objetivo criar uma plataforma integrada (web e aplicativo) para gerenciar estoques de produtos. A plataforma permitirá que usuários acompanhem, atualizem e monitorem o estoque em tempo real, garantindo eficiência e controle no gerenciamento de inventário.

## 👥 Equipe

- **[Gladistony Silva](https://github.com/Gladistony)**
- **[Gustavo França](https://github.com/gustavof0411)**
- **[José Miguel](https://github.com/JMiguelsilva2003)**
- **[Pedro Emmanuel](https://github.com/Pedro-Emmanuel-G-C-Machado)**

## Orientação

- **Professor**: [Waldemar Ferreira](https://github.com/)

## 🚀 Tecnologias Utilizadas

O projeto está sendo desenvolvido utilizando as seguintes tecnologias:

- **HTML**: Para a estruturação do conteúdo do site.
- **CSS**: Para estilização e layout das páginas.
- **JavaScript**: Para a lógica de interação no frontend.
- **Python**: Para o desenvolvimento do backend e integração com o banco de dados.
- **Java**: Para o desenvolvimento do aplicativo mobile (Android).
- **Kotlin**: Para o desenvolvimento do aplicativo mobile (Android).

## 🛠️ Instruções de Instalação

Para instalar e configurar o StockView corretamente, siga os passos abaixo:

### Servidor da API

1. **Criar um ambiente virtual** (opcional, mas recomendado):
   ```sh
   python -m venv venv
   source venv/bin/activate  # Linux/Mac
   venv\Scripts\activate  # Windows
   ```

2. **Acessar a pasta do servidor:**
   ```sh
   cd servidor_refatorado
   ```

3. **Instalar os requisitos do projeto:**
   ```sh
   pip install -r requirements.txt
   ```

4. **Copiar e editar o arquivo de configuração:**
   ```sh
   copy config.json C:/  # Windows
   cp config.json /  # Linux/Mac
   ```
   Edite o arquivo `C:/config.json` com as seguintes variáveis:
   ```json
   {
       "PORT_LOCAL": 3000,
       "PRODUCT_MODE": true,
       "HOST_URL": "127.0.0.1",
       "START_QUESTION_CAMERA_ID": true,
       "START_QUESTION_CAMERA": true,
       "DEFAULT_CAMERA": 0
   }
   ```
   - **PORT_LOCAL**: Porta onde o servidor está rodando.
   - **PRODUCT_MODE**: Quando ativado, desativa o serviço de envio de e-mail pelo Gmail. Em vez disso, os e-mails aparecem no console do VS Code.
   - **HOST_URL**: IP do servidor. Para testes locais, mantenha `127.0.0.1`.
   - **START_QUESTION_CAMERA_ID**: Faz com que o aplicativo da câmera pergunte automaticamente qual ID usar.
   - **START_QUESTION_CAMERA**: Lista todas as câmeras e permite escolher uma. Se desativado, a câmera padrão (`DEFAULT_CAMERA`) será usada.

5. **Executar o servidor da API:**
   ```sh
   python main.py
   ```

Agora o servidor estará rodando e pronto para ser utilizado!

### Inicialização da Câmera

Para utilizar a funcionalidade de câmera, siga os passos abaixo:

1. **Abrir a pasta `Cliente Camera` em outro terminal:**
   ```sh
   cd "Cliente Camera"
   ```

2. **Instalar as bibliotecas necessárias:**
   ```sh
   pip install opencv-python asyncio aiohttp numpy
   ```

3. **Executar o script da câmera:**
   ```sh
   python main-camera.py
   ```

Agora a câmera estará operando corretamente e integrada ao sistema!
