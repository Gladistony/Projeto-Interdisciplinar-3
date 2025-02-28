from datetime import datetime
from typing import Optional
from pydantic import BaseModel
from sqlalchemy import Boolean, Column, DateTime, Float, ForeignKey, Integer, MetaData, String, Table, Text


class Conta(BaseModel):
    id: int
    nome_usuario: str
    senha: str
    data_criacao: datetime
    email: str
    codigo_ativacao: str
    conta_bloqueada: bool
    tentativas_senha_incorreta: int
    data_ultimo_login: datetime
    nome_completo: str
    anotacoes: str
    numero_telefone: str
    url_foto: Optional[str]
    tipo_conta: str

class Camera(BaseModel):
    id: int
    nome: str
    descricao: Optional[str]
    id_conta: int
    codigo_camera: str

class Estoque(BaseModel):
    id: int
    id_conta: int
    nome: str
    descricao: Optional[str]
    imagem: Optional[str]

class Produto(BaseModel):
    id: int
    nome: str
    descricao: Optional[str]
    foto: Optional[str]

class RegistroProdutoEstoque(BaseModel):
    id: int
    id_estoque: int
    id_produto: int
    quantidade: int
    data_validade: Optional[datetime]
    preco: float

def criar_tabelas(engime):
    meta = MetaData()

    # Tabela contas
    contas = Table(
        'contas', meta,
        Column('id', Integer, primary_key=True, autoincrement=True),
        Column('nome_usuario', String(255), nullable=False),
        Column('senha', String(255), nullable=False),
        Column('data_criacao', DateTime, default=datetime.now),
        Column('email', String(255), nullable=False),
        Column('codigo_ativacao', String(255), nullable=False),
        Column('conta_bloqueada', Boolean, default=True),
        Column('tentativas_senha_incorreta', Integer, default=0),
        Column('data_ultimo_login', DateTime, default=datetime.now),
        Column('url_foto', String(255)),
        Column('tipo_conta', String(255), default="usuario"),
        Column('nome_completo', String(255)),
        Column('anotacoes', Text),
        Column('numero_telefone', String(20))
    )
    
    # Tabela cameras
    cameras = Table(
        'cameras', meta,
        Column('id', Integer, primary_key=True, autoincrement=True),
        Column('nome', String, nullable=False),
        Column('descricao', String),
        Column('id_conta', Integer, ForeignKey('contas.id'), nullable=False),
        Column('codigo_camera', String, nullable=False)
    )

    # Tabela estoque
    estoque = Table(
        'estoque', meta,
        Column('id', Integer, primary_key=True, autoincrement=True),
        Column('id_conta', Integer, ForeignKey('contas.id'), nullable=False),
        Column('nome', String, nullable=False),
        Column('descricao', String),
        Column('imagem', String)
    )

    # Tabela produto
    produto = Table(
        'produto', meta,
        Column('id', Integer, primary_key=True, autoincrement=True),
        Column('nome', String, nullable=False),
        Column('descricao', String),
        Column('foto', String)
    )

    # Tabela registro_produto_estoque
    registro_produto_estoque = Table(
        'registro_produto_estoque', meta,
        Column('id', Integer, primary_key=True, autoincrement=True),
        Column('id_estoque', Integer, ForeignKey('estoque.id'), nullable=False),
        Column('id_produto', Integer, ForeignKey('produto.id'), nullable=False),
        Column('quantidade', Integer, nullable=False),
        Column('data_validade', DateTime),
        Column('preco', Float, nullable=False)
    )

    # Criar as tabelas se não existirem
    meta.create_all(engime)
    #Se a tabela contas existir, verificar se a coluna url_foto de todos os elementos é menor que 250
    #Se não for apagar todo valor 


    print("Tabelas verificadas/criadas com sucesso")