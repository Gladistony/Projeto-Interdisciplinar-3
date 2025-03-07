import os
from fastapi import APIRouter, Depends, Request
from fastapi.responses import HTMLResponse, StreamingResponse
from Classes.BancoDeDados import BancoDeDados
from Classes.ClassesFastAPI import *
from Classes.ModuloResposta import generate_response
from Classes.Manage_Conect import ManageConect

router = APIRouter()

manage_conect = ManageConect()
database = BancoDeDados()

@router.post("/give/" )
def create_item(item: Item, request: Request):
    if item.id == "null":
        conect = manage_conect.create_instance()
        id = conect.id
        meu_ip = request.client.host
        meu_ip = request.headers.get('X-Real-IP')
        conect.set_ip(meu_ip)
        numero_conexoes = len(manage_conect.listar_conexoes_por_ip(meu_ip))
        if numero_conexoes > 10:
            manage_conect.apagar_conexao_mais_antiga(meu_ip)
            resposta = generate_response(16, id=id, ip=meu_ip, numero_conexoes=numero_conexoes, aviso="Numero de conexoes excedido, a conexao mais antiga foi apagada")
        else:
            resposta = generate_response(16, id=id, ip=meu_ip, numero_conexoes=numero_conexoes, aviso="")
        
        return resposta
    else:
        if len(item.id) == 36:
            if item.id in manage_conect.conects:
                conect = manage_conect.get_conect(item.id)
                conect.att_conection()
                meu_ip = request.client.host
                meu_ip = request.headers.get('X-Real-IP')
                id = conect.id
                conect.set_ip(meu_ip)
                numero_conexoes = len(manage_conect.listar_conexoes_por_ip(meu_ip))
                return generate_response(17, id=id, ip=meu_ip, numero_conexoes=numero_conexoes, aviso="")
            else:
                meu_ip = request.client.host
                meu_ip = request.headers.get('X-Real-IP')
                return generate_response(12, ip=meu_ip)
        else:
            meu_ip = request.client.host
            meu_ip = request.headers.get('X-Real-IP')
            return generate_response(12, ip=meu_ip)

@router.post("/cadastro/")
def cadastro(item: Cadastro, request: Request):
    conect = manage_conect.get_conect(item.id)
    meu_ip = request.client.host
    meu_ip = request.headers.get('X-Real-IP')
    if conect is None:
        return generate_response(12, ip=meu_ip)
    if conect.ja_logado:
        return generate_response(14, ip=meu_ip)
    nome_usuario = item.usuario
    senha = item.senha
    email = item.email
    nome_completo = item.nome_completo
    return database.criar_conta(nome_usuario, senha, email, nome_completo, meu_ip)

@router.post("/login/")
def login(item: Login, request: Request):
    conect = manage_conect.get_conect(item.id)
    meu_ip = request.client.host
    meu_ip = request.headers.get('X-Real-IP')
    if conect is None:
        return generate_response(12, ip=meu_ip)
    if conect.ja_logado:
        return generate_response(14, ip=meu_ip)
    tentativa = database.tentativa_login(item.usuario, item.senha, meu_ip)
    if tentativa["code"] == 0:
        conect.usuario = item.usuario
    return tentativa

@router.get("/ativar/{usuario}/{codigo}")
def ativar_conta(usuario: str, codigo: str):
    resposta = database.ativar_conta(usuario, codigo)
    arqhtml = open("Html_Template/ativar.html", "r")
    html = arqhtml.read()
    #substituir o valor STATUSCODE pelo status da ativação
    html = html.replace("STATUSCODE", resposta["message"])
    return HTMLResponse(content=html, status_code=200)
    
@router.post("/recover/")
def recover(item: Recover):
    return database.recover(item.usuario)

@router.get("/get_img_url/{codigo}")
def get_img_url(codigo: str):
    if not os.path.isdir("imagens"):
        os.mkdir("imagens")
    if os.path.isfile(f"imagens/{codigo}"):
        file = open(f"imagens/{codigo}", "rb")
        return StreamingResponse(file, media_type="image/jpeg")
    else:
        return generate_response(20)

@router.get("/get_img_url/perfil/{codigo}")
def get_img_url_perfil(codigo: str):
    if not os.path.isdir("imagens"):
        os.mkdir("imagens")
    if not os.path.isdir(f"imagens/perfil"):
        os.mkdir("imagens/perfil")
    if os.path.isfile(f"imagens/perfil/{codigo}"):
        file = open(f"imagens/perfil/{codigo}", "rb")
        return StreamingResponse(file, media_type="image/jpeg")
    else:
        return generate_response(20)
        