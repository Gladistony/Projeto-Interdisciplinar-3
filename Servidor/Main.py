from Classes.BancoDeDados import BancoDeDados
from Classes.Coneccao import ManageConect
from fastapi import FastAPI
from pydantic import BaseModel
from typing import Optional
from mangum import Mangum

#Dados da conexão com o banco de dados
host=  '127.0.0.1'  # Substitua pelo endereço IP do WSL
linkhost = '44.203.201.20'
port=3306
database='Projeto3'
user='servidor'
password='159753'
#API Gemini
caminho_arquivo_api = "C:/API Gemi.txt"
EMAIL_PADRAO = f"Esse é seu codigo de ativacao: CODIGO \\n Você pode ativar sua conta em: http://{linkhost}/ativar/USUARIO/CODIGO"


#Conectar com o banco de dados
database = BancoDeDados(host, port, database, user, password, EMAIL_PADRAO)
database.create_connection()
#Criar o gerenciador de conexões
manage_conect = ManageConect()

#Ativar API

app = FastAPI()
handler = Mangum(app)

@app.get("/")
def read_root():
    return {"message": "Olá, bem vindo ao API do Projeto 3!"}

@app.get("/items/{item_id}")
def read_item(item_id: int, q: str = None):
    return {"item_id": item_id, "q": q}

class Item(BaseModel):
    id: str
    request: Optional[str] = None
    usuario: Optional[str] = None
    senha: Optional[str] = None
    nome_completo : Optional[str] = None
    email: Optional[str] = None
    url_foto: Optional[str] = None


@app.post("/give/")
def create_item(item: Item):
    if item.id == "null":
        id = manage_conect.create_instance()
        resposta = {}
        resposta["id"] = id
        resposta["status"] = "Conexao criada"
        return resposta
    else:
        #Verificar se o id corresponde ao formato do uuid
        if len(item.id) == 36:
            #Verificar se o id existe
            if item.id in manage_conect.conects:
                #Atualizar a conexão
                manage_conect.conects[item.id].att_conection()
                #Verificar se a requisição é vazia
                return processarrequerimento(item)
            else:
                return {"status": "Conexao nao encontrada"}
        else:
            return {"status": "Id invalido"}

@app.get("/ativar/{usuario}/{codigo}")
def ativar_conta(usuario: str, codigo: str):
    return database.ativar_conta(usuario, codigo)

def processarrequerimento(item):
    if item.request == "null" or item.request == None:
        return {"status": "Requisicao vazia"}
    elif item.request == "cadastro" and item.usuario != None and item.senha != None and item.email != None and item.nome_completo != None:
        conec = manage_conect.conects[item.id]
        if conec.get_ja_logado():
            return {"status": "Usuario ja logado"}
        nome_usuario = item.usuario
        senha = item.senha
        email = item.email
        nome_completo = item.nome_completo
        anotacoes = ""
        numero_telefone = ""
        return database.criar_conta(nome_usuario, senha, email, nome_completo, anotacoes, numero_telefone)
    elif item.request == "login" and item.usuario != None and item.senha != None:
        conec = manage_conect.conects[item.id]
        if conec.get_ja_logado():
            return {"status": "Usuario ja logado"}
        dados = database.tentativa_login(item.usuario, item.senha)
        if dados["code"] == 0:
            conec.data = dados["data"]
        #removar o campo data
        dados.pop("data")
        info = conec.get_data()
        dados.update(info)
        return dados
    elif item.request == "ativar" and item.usuario != None and item.senha != None:
        return database.ativar_conta(item.usuario, item.senha)
    elif item.request == "logout":
        conec = manage_conect.conects[item.id]
        if conec.get_ja_logado():
            conec.data = None
            return {"status": "Usuario deslogado"}
        else:
            return {"status": "Usuario nao logado"}
    elif item.request == "get_dados":
        conec = manage_conect.conects[item.id]
        if conec.get_ja_logado():
            return conec.get_data()
        else:
            return {"status": "Usuario nao logado"}
    elif item.request == "set_img" and item.url_foto != None:
        conec = manage_conect.conects[item.id]
        if conec.get_ja_logado():
            status = database.set_img(conec.data[0], item.url_foto)
            if status["status"] == "sucesso":
                conec.data = database.get_data(conec.data[0])
            return status
        else:
            return {"status": "Usuario nao logado"}
    elif item.request == "recover" and item.usuario != None:
        return database.recover(item.usuario)
    else:
        return {"status": "Requisicao invalida"}

if __name__ == '__main__':
    import uvicorn
    uvicorn.run(app, host="127.0.0.1", port=8000, log_level="info")
