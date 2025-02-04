import os
from Classes.BancoDeDados import BancoDeDados
from Classes.Coneccao import ManageConect
from fastapi import FastAPI, File, UploadFile
from pydantic import BaseModel
from typing import Optional
from mangum import Mangum
from fastapi.responses import HTMLResponse

# Dados da conexão com o banco de dados
host = '127.0.0.1'  # Substitua pelo endereço IP do WSL
linkhost = '44.203.201.20'
port = 3306
database = 'Projeto3'
user = 'servidor'
password = '159753'
# API Gemini
caminho_arquivo_api = "C:/API Gemi.txt"
EMAIL_PADRAO = f"Esse é seu codigo de ativacao: CODIGO <br> Você pode ativar sua conta em: http://{linkhost}/ativar/USUARIO/CODIGO"



# Conectar com o banco de dados
database = BancoDeDados(host, port, database, user, password, EMAIL_PADRAO)
database.create_connection()
# Criar o gerenciador de conexões
manage_conect = ManageConect()

# Ativar API
app = FastAPI()
handler = Mangum(app)

@app.get("/")
def read_root():
    html = open("Html_Template/index.html", "r")
    return HTMLResponse(content=html.read(), status_code=200)

class Item(BaseModel):
    id: str

class Cadastro(BaseModel):
    id: str
    usuario: str
    senha: str
    nome_completo: str
    email: str

class Login(BaseModel):
    id: str
    usuario: str
    senha: str

class Ativar(BaseModel):
    id: str
    usuario: str
    senha: str

class Logout(BaseModel):
    id: str

class GetDados(BaseModel):
    id: str

class SetImgUrl(BaseModel):
    id: str
    url_foto: str

class Recover(BaseModel):
    id: str
    usuario: str

class GetEmail(BaseModel):
    id: str
    usuario: str

class UploadImg(BaseModel):
    id: str
    file: UploadFile = File(...)
    destino: str

@app.post("/give/")
def create_item(item: Item):
    if item.id == "null":
        id = manage_conect.create_instance()
        resposta = {"id": id, "status": "Conexao criada"}
        return resposta
    else:
        if len(item.id) == 36:
            if item.id in manage_conect.conects:
                manage_conect.conects[item.id].att_conection()
                return {"message": "Conexao atualizada", "status": "sucesso"}
            else:
                return {"message": "Conexao nao encontrada", "code": 12, "status": "erro"}
        else:
            return {"message": "Id invalido", "code": 12, "status": "erro"}

@app.post("/cadastro/")
def cadastro(item: Cadastro):
    try:
        conec = manage_conect.conects[item.id]
    except:
        return {"message": "Conexao nao encontrada", "status": "erro", "code": 12}
    if conec.get_ja_logado():
        return {"message": "Usuario ja logado", "status": "erro", "code": 14}
    nome_usuario = item.usuario
    senha = item.senha
    email = item.email
    nome_completo = item.nome_completo
    anotacoes = ""
    numero_telefone = ""
    return database.criar_conta(nome_usuario, senha, email, nome_completo, anotacoes, numero_telefone)

@app.post("/login/")
def login(item: Login):
    try:
        conec = manage_conect.conects[item.id]
    except:
        return {"message": "Conexao nao encontrada", "status": "erro", "code": 12}
    if conec.get_ja_logado():
        return {"message": "Usuario ja logado", "status": "erro", "code": 14}
    dados = database.tentativa_login(item.usuario, item.senha)
    if dados["code"] == 0:
        conec.data = dados["data"]
        dados.pop("data")
        info = conec.get_data()
        dados.update(info)
    return dados

@app.post("/ativar/")
def ativar(item: Ativar):
    return database.ativar_conta(item.usuario, item.senha)

@app.post("/logout/")
def logout(item: Logout):
    try:
        conec = manage_conect.conects[item.id]
    except:
        return {"message": "Conexao nao encontrada", "status": "erro", "code": 12}
    if conec.get_ja_logado():
        conec.data = None
        return {"message": "Usuario deslogado", "status": "sucesso", "code": 0}
    else:
        return {"message": "Usuario nao logado", "status": "erro", "code": 15}

@app.post("/get_dados/")
def get_dados(item: GetDados):
    try:
        conec = manage_conect.conects[item.id]
    except:
        return {"message": "Conexao nao encontrada", "status": "erro", "code": 12}
    if conec.get_ja_logado():
        return conec.get_data()
    else:
        return {"message": "Usuario nao logado", "status": "erro", "code": 15}

@app.post("/set_img_url/")
def set_img_url(item: SetImgUrl):
    try:
        conec = manage_conect.conects[item.id]
    except:
        return {"message": "Conexao nao encontrada", "status": "erro", "code": 12}
    if conec.get_ja_logado():
        status = database.set_img(conec.data[0], item.url_foto)
        if status["status"] == "sucesso":
            conec.data = database.get_data(conec.data[0])
        return status
    else:
        return {"message": "Usuario nao logado", "status": "erro", "code": 15}

@app.get("/get_img_url/")
def get_img_url(link: str):
    if not os.path.isdir("imagens"):
        os.mkdir("imagens")
    if os.path.isfile(f"imagens/{link}_perfil.jpg"):
        file = open(f"imagens/{link}_perfil.jpg", "rb")
        return {"status": "sucesso", "file": file}
    else:
        return {"status": "erro", "message": "Imagem nao encontrada", "code": 18}

@app.post("/upload-image/")
async def upload_image(item: UploadImg):
    try:
        contents = await item.file.read()
    except:
        return {"message": "Erro ao ler arquivo", "status": "erro", "code": 16}    
    try:
        conec = manage_conect.conects[item.id]
    except:
        return {"message": "Conexao nao encontrada", "status": "erro", "code": 12}
    if conec.get_ja_logado():
        #Verificar se a pasta imagens existe
        if not os.path.isdir("imagens"):
            os.mkdir("imagens")
        #Verficar o tipo de envio
        if item.destino == "perfil":
            destino = f"imagens/{item.id}_perfil.jpg"
        elif item.destino == "post":
            destino = f"imagens/{item.id}_post.jpg"
        else:
            return {"message": "Destino invalido", "status": "erro", "code": 17}
        #Salvar o arquivo
        with open(destino, "wb") as file:
            file.write(contents)
        #Pegar url
        url = f"http://{linkhost}/get_img_url/?link={item.id}"
        return {"message": "Arquivo salvo", "status": "sucesso", "url": url}
    else:
        return {"message": "Usuario nao logado", "status": "erro", "code": 15}

@app.post("/recover/")
def recover(item: Recover):
    return database.recover(item.usuario)

@app.post("/get_email/")
def get_email(item: GetEmail):
    return database.get_email(item.usuario)