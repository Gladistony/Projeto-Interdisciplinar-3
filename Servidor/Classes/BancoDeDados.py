import random
from datetime import datetime
from sqlalchemy import create_engine, Column, Integer, String, MetaData, Table, Boolean, Text, DateTime, ForeignKey, Float
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from sqlalchemy.sql import text
import Classes.emailGmail as emailGmail
from validate_email_address import validate_email
import dns.resolver
from Classes.Gerenciador_Respostas import generate_response
from pydantic import BaseModel
from typing import Optional

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


Base = declarative_base()

class BancoDeDados:
    def __init__(self, database_file, msgpadrao):
        self.database_file = database_file
        self.engine = None
        self.Session = None
        self.msgpadrao = msgpadrao

    def create_connection(self):
        # Cria uma conexão com o banco de dados SQLite.
        try:
            self.engine = create_engine(f"sqlite:///{self.database_file}", pool_pre_ping=True)
            self.Session = sessionmaker(bind=self.engine)
            print("Conexão ao banco de dados SQLite foi bem-sucedida")
            self.verificar_tabelas()
        except Exception as e:
            print(f"Erro ao conectar ao SQLite: {e}")

    def verificar_tabelas(self):
        # Verifica e cria as tabelas necessárias no banco de dados.
        meta = MetaData()
        
        # Tabela contas
        contas = Table(
            'contas', meta,
            Column('id', Integer, primary_key=True, autoincrement=True),
            Column('nome_usuario', String(255), nullable=False),
            Column('senha', String(255), nullable=False),
            Column('data_criacao', DateTime, default=datetime.utcnow),
            Column('email', String(255), nullable=False),
            Column('codigo_ativacao', String(255), nullable=False),
            Column('conta_bloqueada', Boolean, default=True),
            Column('tentativas_senha_incorreta', Integer, default=0),
            Column('data_ultimo_login', DateTime, default=datetime.utcnow),
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
        meta.create_all(self.engine)
        print("Tabelas verificadas/criadas com sucesso")

    def execute_query(self, query, params=None):
        # Executa uma query no banco de dados.
        query = text(query)
        session = self.Session()
        try:
            if params:
                result = session.execute(query, params)
            else:
                result = session.execute(query)
            session.commit()
            return result
        except Exception as e:
            session.rollback()
            print(f"Erro ao executar a query: {e}")
        finally:
            session.close()

    def execute_query_data(self, query, params=None):
        # Executa uma query no banco de dados e retorna uma única linha.
        query = text(query)
        session = self.Session()
        try:
            if params:
                result = session.execute(query, params)
            else:
                result = session.execute(query)
            return result.fetchone()
        except Exception as e:
            session.rollback()
            print(f"Erro ao executar a query: {e}")
        finally:
            session.close()

    def execute_query_data_all(self, query, params=None):
        # Executa uma query no banco de dados e retorna todas as linhas.
        query = text(query)
        session = self.Session()
        try:
            if params:
                result = session.execute(query, params)
            else:
                result = session.execute(query)
            return result.fetchall()
        except Exception as e:
            session.rollback()
            print(f"Erro ao executar a query: {e}")
        finally:
            session.close()

    def get_data(self, id_conta):
        # Retorna os dados de uma conta.
        query = """SELECT * FROM contas WHERE id = :id_conta"""
        return self.execute_query_data(query, {"id_conta": id_conta})
        
    def get_email(self, usuario):
        # Retorna o email de um usuário, ofuscando parte do endereço de email.
        query = """SELECT * FROM contas WHERE nome_usuario = :usuario"""
        conta = self.execute_query_data(query, {"usuario": usuario})
        if not conta:
            return generate_response(4)
        email = conta.email
        endereco, dominio = email.split('@')
        if len(endereco) > 3:
            endereco = endereco[:3] + '*' * (len(endereco) - 3)
        email_modificado = endereco + "@" + dominio
        return generate_response(0, email=email_modificado)
    
    def recover(self, usuario):
        # Recupera a conta de um usuário.
        query = """SELECT * FROM contas WHERE nome_usuario = :usuario"""
        conta = self.execute_query_data(query, {"usuario": usuario})
        if not conta:
            return generate_response(4)
        if conta.conta_bloqueada:  # Conta bloqueada
            return generate_response(3)
        codigo_ativacao = self.bloquear_conta(usuario)
        sql_update_query = """UPDATE contas SET senha = :codigo_ativacao WHERE nome_usuario = :usuario"""
        self.execute_query(sql_update_query, {"codigo_ativacao": codigo_ativacao, "usuario": usuario})
        self.enviar_email_ativacao(emailGmail, self.msgpadrao, usuario, codigo_ativacao, conta.email)

    def set_img(self, id_conta, url):
        # Define a imagem de uma conta.
        query = """UPDATE contas SET anotacoes = :url WHERE id = :id_conta"""
        self.execute_query(query, {"url": url, "id_conta": id_conta})
        return generate_response(0)

    def is_valid_email(self, email):
        # Verifica se um email é válido.
        if not validate_email(email):
            return False
        domain = email.split('@')[1]
        try:
            records = dns.resolver.resolve(domain, 'MX')
            return True if records else False
        except (dns.resolver.NoAnswer, dns.resolver.NXDOMAIN, dns.resolver.Timeout, dns.exception.DNSException):
            return False

    def selecionar_conta(self, nome_usuario):
        # Seleciona uma conta pelo nome de usuário.
        query = """SELECT * FROM contas WHERE nome_usuario = :nome_usuario"""
        return self.execute_query_data(query, {"nome_usuario": nome_usuario})

    def atualizar_login(self, nome_usuario):
        # Atualiza a data de login de uma conta.
        query = """UPDATE contas SET data_ultimo_login = :now, tentativas_senha_incorreta = 0 WHERE nome_usuario = :nome_usuario"""
        self.execute_query(query, {"now": datetime.utcnow(), "nome_usuario": nome_usuario})

    def incrementar_tentativas(self, nome_usuario):
        # Incrementa o número de tentativas incorretas de senha de uma conta.
        query = """UPDATE contas SET tentativas_senha_incorreta = tentativas_senha_incorreta + 1 WHERE nome_usuario = :nome_usuario"""
        self.execute_query(query, {"nome_usuario": nome_usuario})

    def bloquear_conta(self, nome_usuario):
        # Bloqueia uma conta e gera um código de ativação.
        codigo_ativacao = "".join([str(random.randint(0, 9)) for _ in range(6)])
        query = """UPDATE contas SET conta_bloqueada = 1, codigo_ativacao = :codigo_ativacao WHERE nome_usuario = :nome_usuario"""
        self.execute_query(query, {"codigo_ativacao": codigo_ativacao, "nome_usuario": nome_usuario})
        return codigo_ativacao

    def enviar_email_ativacao(self, emailGmail, msgpadrao, nome_usuario, codigo_ativacao, email):
        # Envia um email de ativação.
        string_cod = msgpadrao.replace("CODIGO", codigo_ativacao)
        string_final = string_cod.replace("USUARIO", nome_usuario)
        print(string_final)
        emailGmail.codigoAtivacao(string_final, email)

    def tentativa_login(self, nome_usuario, senha):
        if not self.engine:
            return generate_response(5)

        conta = self.selecionar_conta(nome_usuario)

        if not conta:
            return generate_response(4)

        if conta.conta_bloqueada:  # Conta bloqueada
            return generate_response(3)

        if conta.tentativas_senha_incorreta < 3:  # Menos de 3 tentativas incorretas
            if conta.senha == senha:
                self.atualizar_login(nome_usuario)
                return generate_response(0, data=conta)
            else:
                self.incrementar_tentativas(nome_usuario)
                return generate_response(1)

        ultima_tentativa = conta.data_ultimo_login
        tempo_atual = datetime.now()
        diferenca_em_segundos = (tempo_atual - ultima_tentativa).total_seconds()

        if diferenca_em_segundos < 300:
            return generate_response(2, restante=300 - diferenca_em_segundos)

        if conta.tentativas_senha_incorreta < 6:  # Menos de 6 tentativas incorretas
            if conta.senha == senha:
                self.atualizar_login(nome_usuario)
                return generate_response(0, data=conta)
            else:
                self.incrementar_tentativas(nome_usuario)
                return generate_response(1)

        # Bloquear a conta e enviar código de ativação
        codigo_ativacao = self.bloquear_conta(nome_usuario)
        self.enviar_email_ativacao(emailGmail, self.msgpadrao, nome_usuario, codigo_ativacao, conta.email)
        return generate_response(2)

    def criar_conta(self, nome_usuario, senha, email, nome_completo, anotacoes, numero_telefone):
        # Cria uma nova conta.
        conta = self.selecionar_conta(nome_usuario)

        if conta:
            return generate_response(6)

        if len(nome_usuario) < 8 or len(nome_usuario) > 20 or not nome_usuario.isalnum() or " " in nome_usuario:
            return generate_response(7)

        if len(senha) < 8 or len(senha) > 20:
            return generate_response(8)

        if not self.is_valid_email(email):
            return generate_response(9)

        codigo_ativacao = "".join([str(random.randint(0, 9)) for _ in range(6)])
        sql_insert_query = """INSERT INTO contas (nome_usuario, senha, data_criacao, email, codigo_ativacao, conta_bloqueada, tentativas_senha_incorreta, data_ultimo_login, nome_completo, anotacoes, numero_telefone) 
                            VALUES (:nome_usuario, :senha, :now, :email, :codigo_ativacao, 1, 0, :now, :nome_completo, :anotacoes, :numero_telefone)"""
        try:
            self.execute_query(sql_insert_query, {"nome_usuario": nome_usuario, "senha": senha, "now": datetime.utcnow(), "email": email, "codigo_ativacao": codigo_ativacao, "nome_completo": nome_completo, "anotacoes": anotacoes, "numero_telefone": numero_telefone})
        except Exception as e:
            print(f"Erro ao criar conta: {e}")

        self.enviar_email_ativacao(emailGmail, self.msgpadrao, nome_usuario, codigo_ativacao, email)
        conta = self.selecionar_conta(nome_usuario)
        return generate_response(0, data=conta)

    def ativar_conta(self, nome_usuario, codigo_ativacao):
        conta = self.selecionar_conta(nome_usuario)

        if not conta:
            return generate_response(4)

        if not conta.conta_bloqueada:  # Conta já está ativa
            return generate_response(11)

        if conta.codigo_ativacao == codigo_ativacao:
            sql_update_query = """UPDATE contas SET conta_bloqueada = 0, tentativas_senha_incorreta = 0 WHERE nome_usuario = :nome_usuario"""
            try:
                self.execute_query(sql_update_query, {"nome_usuario": nome_usuario})
            except Exception as e:
                print(f"Erro ao ativar conta: {e}")
            return generate_response(0)
        else:
            return generate_response(10)
