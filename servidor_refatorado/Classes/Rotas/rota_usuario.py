import os
from fastapi import APIRouter, Depends, Request
from Classes.BancoDeDados import BancoDeDados
from Classes.ClassesFastAPI import *
from Classes.Manage_Conect import ManageConect
from Classes.ModuloResposta import generate_response
import uuid
from Classes.GLOBAIS import linkhost, PORT_LOCAL
import base64

router = APIRouter()
manage_conect = ManageConect()
database = BancoDeDados()


@router.post("/logout/")
def logout(item: Logout, request: Request):
    conect = manage_conect.get_conect(item.id)
    meu_ip = request.client.host
    meu_ip = request.headers.get('X-Real-IP')
    if conect is None:
        return generate_response(12, ip=meu_ip)
    if conect.ja_logado:
        conect.remove_usuario()
        return generate_response(0, ip=meu_ip)
    else:
        return generate_response(15, ip=meu_ip)

@router.post("/get_dados/")
def get_dados(item: GetDados):
    conect = manage_conect.get_conect(item.id)
    if conect is None:
        return generate_response(12)
    if conect.ja_logado:
        return conect.get_data()
    else:
        return generate_response(15)

@router.post("/set_img_url/")
def set_img_url(item: SetImgUrl):
    conec = manage_conect.get_conect(item.id)
    if conec is None:
        return generate_response(12)
    if not conec.ja_logado:
        return generate_response(15)
    #Verificar se a url é válida
    if not item.url_foto.startswith("http://") and not item.url_foto.startswith("https://") and len(item.url_foto) > 250:
        return generate_response(25)
    return database.set_img_url(conec.usuario, item.url_foto)

@router.post("/charge/")
def charge(item: ChargePassword):
    conect = manage_conect.get_conect(item.id)
    if conect is None:
        return generate_response(12)
    if not conect.ja_logado:
        return generate_response(15)
    return database.charge_password(conect.usuario, item.senha, item.nova_senha)

@router.post("/get_email/")
def get_email(item: GetEmail):
    return database.get_email(item.usuario)

@router.post("/upload_img/")
async def upload_image(item: UploadImg):
    try:
        file_content = base64.b64decode(item.file)
    except:
        return generate_response(23) 
    conect = manage_conect.get_conect(item.id)
    if conect is None:
        return generate_response(12)
    if not conect.ja_logado:
        return generate_response(15)

    #Verificar se a pasta imagens existe
    if not os.path.isdir("imagens"):
        os.mkdir("imagens")
    #Verficar o tipo de envio
    gerar_nome_foto = uuid.uuid4()
    destino = f"imagens/{gerar_nome_foto}.jpg"
    url_foto = f"http://{linkhost}/get_img_url/{gerar_nome_foto}.jpg"

    if item.destino == "perfil":
        if not os.path.isdir("imagens/perfil"):
            os.mkdir("imagens/perfil")
        destino = f"imagens/perfil/{gerar_nome_foto}.jpg"
        url_foto = f"http://{linkhost}/get_img_url/perfil/{gerar_nome_foto}.jpg"
        #Atualizar foto de perfil
        database.set_img_url(conect.usuario, url_foto)
    #Salvar o arquivo
    with open(destino, "wb") as file:
        file.write(file_content)
    #Pegar url
    return generate_response(0, url=url_foto)

@router.post("/get_estoque/")
def get_estoque(item: GetEstoque):
    conect = manage_conect.get_conect(item.id)
    if conect is None:
        return generate_response(12)
    if not conect.ja_logado:
        return generate_response(15)
    return database.get_estoque(conect.usuario)

@router.post("/get_produto/")
def get_produto(item: GetProduto):
    conect = manage_conect.get_conect(item.id)
    if conect is None:
        return generate_response(12)
    if not conect.ja_logado:
        return generate_response(15)
    return database.get_produto(conect.usuario, item.id_produto)

@router.post("/registro_produto/")
def registro_produto(item: RegistroProduto):
    conect = manage_conect.get_conect(item.id)
    if conect is None:
        return generate_response(12)
    if not conect.ja_logado:
        return generate_response(15)
    return database.registro_produto(conect.usuario, item.nome, item.descricao, item.imagem)

@router.post("/registro_produto_estoque/")
def registro_produto_estoque(item: RegistroProdutoEstoque):
    conect = manage_conect.get_conect(item.id)
    if conect is None:
        return generate_response(12)
    if not conect.ja_logado:
        return generate_response(15)
    return database.registro_produto_estoque(conect.usuario, item.id_estoque, item.id_produto, item.quantidade, item.data_validade, item.preco)

@router.post("/criar_estoque/")
def criar_estoque(item: CriarEstoque):
    conect = manage_conect.get_conect(item.id)
    if conect is None:
        return generate_response(12)
    if not conect.ja_logado:
        return generate_response(15)
    return database.criar_estoque(conect.usuario, item.nome, item.descricao, item.imagem)

@router.post("/charge_estoque_url/")
def charge_estoque_url(item: ChargeEstoqueUrl):
    conect = manage_conect.get_conect(item.id)
    if conect is None:
        return generate_response(12)
    if not conect.ja_logado:
        return generate_response(15)
    estoque_id = item.id_estoque
    url_foto = item.url_foto
    return database.charge_estoque_image(conect.usuario, estoque_id, url_foto)

@router.post("/cadastrar_camera/")
def cadastrar_camera(item: CadastrarCamera):
    conect = manage_conect.get_conect(item.id)
    if conect is None:
        return generate_response(12)
    if not conect.ja_logado:
        return generate_response(15)
    codigo_camera = f"{uuid.uuid4()}"
    id_estoque = item.id_estoque
    return database.cadastrar_camera(conect.usuario, item.nome, item.descricao, codigo_camera, id_estoque)

@router.post("/apagar_estoque/")
def apagar_estoque(item: DelEstoque):
    conect = manage_conect.get_conect(item.id)
    if conect is None:
        return generate_response(12)
    if not conect.ja_logado:
        return generate_response(15)
    id_estoque = item.id_estoque
    return database.deletar_estoque(id_estoque, conect.usuario)

@router.post("/mudar_produto/")
def mudar_produto(item: MudarProdutoQt):
    conect = manage_conect.get_conect(item.id)
    if conect is None:
        return generate_response(12)
    if not conect.ja_logado:
        return generate_response(15)
    id_produto = item.id_produto
    quantidade = item.quantidade
    id_estoque = item.id_estoque
    return database.mudar_produto(id_produto, quantidade, id_estoque, conect.usuario)

@router.post("/lista_produtos/")
def lista_produtos(item: GetProduto):
    conect = manage_conect.get_conect(item.id)
    if conect is None:
        return generate_response(12)
    if not conect.ja_logado:
        return generate_response(15)
    id_produto = item.id_produto
    return database.lista_produtos(id_produto)
    