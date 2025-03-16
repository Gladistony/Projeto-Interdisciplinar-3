from fastapi import File, UploadFile
from pydantic import BaseModel

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
    file: str
    destino: str

class ChargePassword(BaseModel):
    id: str
    usuario: str
    senha: str
    nova_senha: str

class GetAllUser(BaseModel):
    id: str

class GetUserData(BaseModel):
    id: str
    usuario: str

class SetUserData(BaseModel):
    id: str
    usuario: str
    senha: str

class GetEstoque(BaseModel):
    id: str

class GetProduto(BaseModel):
    id: str
    id_produto: str

class RegistroProduto(BaseModel):
    id: str
    nome: str
    descricao: str
    imagem: str

class RegistroProdutoEstoque(BaseModel):
    id: str
    id_estoque: str
    id_produto: str
    quantidade: int
    data_validade: str
    preco: float

class CriarEstoque(BaseModel):
    id: str
    nome: str
    descricao: str
    imagem: str #Optional

class CadastrarCamera(BaseModel):
    id: str
    nome: str
    descricao: str
    id_estoque: str

class ChargeEstoqueUrl(BaseModel): 
    id: str
    url_foto: str
    id_estoque: str

class MudarProdutoQt(BaseModel):
    id: str
    id_produto: str
    id_estoque: str
    quantidade: int

class DelEstoque(BaseModel):
    id: str
    id_estoque: str