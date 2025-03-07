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
    file: UploadFile = File(...)
    destino: str