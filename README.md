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

## 📱 Instalação e utilização do aplicativo Android

### Instalando através do arquivo APK

Para instalar uma versão já compilada do APK, você pode baixá-la nas **release notes** do repositório ou [**clicando aqui**](https://github.com/Gladistony/Projeto-Interdisciplinar-3/releases/download/first-release/StockView.apk).

### Instalando através do Android Studio

1. **Baixe o repositório do GitHub do projeto**

**a) Opção 1: através do navegador**

Na página do GitHub do projeto, baixe o repositório da branch **main** no formato **.zip**. 

Para evitar problemas do Android Studio com caracteres que não são do formado **ASCII** (tais como õ, é, á, ç), crie uma nova pasta em seu disco local **C:/** e extraia o repositório dentro desta pasta. O nome da pasta deverá conter apenas caracteres ASCII.


**b) Opção 2: através do aplicativo desktop GitHub**

No aplicativo do GitHub, crie um clone do repositório utilizando a URL

```sh
https://github.com/Gladistony/Projeto-Interdisciplinar-3.git
```
Para evitar problemas do Android Studio com caracteres que não são do formado **ASCII** (tais como õ, é, á, ç), crie uma nova pasta em seu disco local **C:/** e extraia o repositório dentro desta pasta. O nome da pasta deverá conter apenas caracteres ASCII.

2. **Instalando o Android Studio**

Acesse o website do [Android Studio](https://developer.android.com/studio/archive?hl=en) e baixe a versão **Android Studio Ladybug | 2024.2.1 Patch 2 October 31, 2024**.

❌ Evite utilizar a página do Android Studio no idioma *Português - Brasil*, pois poderá encontrar um erro ao obter as informações da página. Dê preferência a versão em inglês da página.

Siga os passos indicados pelo instalador do Android Studio para completar a instalação do programa.

3. **Abrindo o repositório no Android Studio**

Na tela inicial do Android Studio, importe o projeto ao selecionar a opção **Open**, navegue até a pasta onde se encontra o repositório baixado anteriormente e selecione a pasta no seguinte formato

```sh
C:\pasta-criada-para-exportacao\Projeto-Interdisciplinar-3\App
```
Aguarde até que o Android Studio complete a importação do projeto.

4. **Instalando o aplicativo no dispositivo Android**

Após completar a importação, conecte o dispositivo Android em seu computador através do cabo USB e clique no botão verde **Run** localizado no centro do topo superior ou pressione as teclas **Shift + F10** em seu teclado. Aguarde a instalação no dispositivo Android.

Agora você poderá utilizar o aplicativo do StockView em seu dispositivo Android!

# 🌐 Utilizando o site do projeto

## Pré-requisitos

- **Node.js**: Certifique-se de que o Node.js está instalado no sistema.
- **Configuração de endereços e portas**: Ajuste os arquivos necessários antes de iniciar o servidor.

---

### 1. Instalando o Node.js

1. Acesse o site oficial do Node.js: [https://nodejs.org](https://nodejs.org).
2. Baixe a versão recomendada para seu sistema operacional.
3. Siga as instruções do instalador para concluir a instalação.
4. Após a instalação, verifique se o Node.js foi instalado corretamente, executando o comando no terminal:

   ```sh
   node -v
   ```

   Se tudo estiver certo, ele exibirá a versão instalada do Node.js.

---

### 2. Configurando os endereços e portas do servidor

Antes de iniciar, edite os seguintes arquivos com os endereços e portas que desejar mantendo os mesmo endereço para todos:

- **Arquivo**: `Scripts/API/server.js`
  - **Linha 7**:
    ```js
    const baseUrl = 'http://127.0.0.1:4000';
    ```
  - **Linha 723**:
  vc tem que escolher a mesma porta do endereço
    ```js
    app.listen(4000, () => { 
    ```

- **Arquivo**: `Scripts/API/apiConnection.js`
  - **Linha 1**:
    ```js
    const API_URL = 'http://127.0.0.1:4000';
    ```

- **Arquivo**: `Scripts/telaDeCamera.js`
  - **Linha 104**:
    ```js
    const link = `http://127.0.0.1:4000`; //link do server padrão
    ```
  - **Linha 156**:
    ```js
    const link1 = `http://127.0.0.1:4000`; //link do server padrão
    ```
  - **Linha 186**:
    ```js
    const link1 = `http://127.0.0.1:4000`; //link do server padrão
    ```

- **Arquivo**: `Scripts/API/ADM.js`
  - **Linha 74**:
    ```js
    const link = `http://127.0.0.1:4000`;
    ```
   esses são os lugares para você colocar o seu endereço 
---

### 3. Iniciando o servidor Node.js

1. Abra o terminal e navegue até o diretório do arquivo `server.js`:

   ```sh
   cd C:seu-caminho\projeto interdiciplinar 03\Scripts\API
   ```

2. Execute o comando para iniciar o servidor:

   ```sh
   node server.js
   ```

3. Certifique-se de que o servidor iniciou corretamente e está ouvindo na porta configurada.

---

### 4. Acessando o site

Com o servidor rodando, abra o navegador e acesse o endereço configurado. Para acessar o **index**, é importante incluir a `/` no final do link. Por exemplo:

```
http://127.0.0.1:4000/
```

Agora o site estará disponível e pronto para uso.

---

## 🛠 Problemas ou dúvidas?

Se surgir qualquer problema ou se precisar de ajuda adicional, fique à vontade para entrar em contato! 😊
