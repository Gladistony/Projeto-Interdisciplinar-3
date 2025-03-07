import os
from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.sql import text
from Classes.TabelasBancoDeDados import *
from Classes.ModuloResposta import generate_response
from Classes.ModuloEmail import enviar_email_ativacao, enviar_email_recuperacao
from validate_email_address import validate_email
import dns.resolver
import random

Base = declarative_base()

class BancoDeDados:
    _instance = None

    def __new__(cls, *args, **kwargs):
        if not cls._instance:
            cls._instance = super().__new__(cls, *args, **kwargs)
        return cls._instance


    def criar_banco(self, URL):
        try:
            absolute_path = os.path.abspath(URL)
            self.engine = create_engine(f"sqlite:///{absolute_path}", pool_pre_ping=True)
            criar_tabelas(self.engine)
        except Exception as e:
            print("Erro ao criar banco de dados", e)
    
    def consultaSql(self, sql, params=None):
        try:
            with self.engine.connect() as connection:
                if params:
                    result = connection.execute(text(sql), params)
                else:
                    result = connection.execute(text(sql))
            return result
        except Exception as e:
            print("Erro ao fazer consulta SQL", e)
            return None
    
    def operacaoSql(self, sql, params=None):
        try:
            with self.engine.connect() as connection:
                if params:
                    connection.execute(text(sql), params)
                else:
                    connection.execute(text(sql))
                connection.commit()
            return True
        except Exception as e:
            print("Erro ao fazer operacao SQL", e)
            return False
    
    def selecionar_conta(self, nome_usuario):
        if not self.engine:
            return generate_response(5)
        sql = "SELECT * FROM contas WHERE nome_usuario = :nome_usuario"
        params = {"nome_usuario": nome_usuario}
        result = self.consultaSql(sql, params)
        if result:
            conta = result.fetchone()
            if conta:
                return conta
        return None
    
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
        
    def criar_conta(self, nome_usuario, senha, email, nome_completo, meu_ip):
        #Verificação inicial
        if not self.engine:
            return generate_response(5, ip=meu_ip)
        
        if self.selecionar_conta(nome_usuario):
            return generate_response(6, ip=meu_ip)
        
        #Verificação de parametros
        if len(nome_usuario) < 8 or len(nome_usuario) > 20 or not nome_usuario.isalnum() or " " in nome_usuario:
            return generate_response(7, ip=meu_ip)

        if len(senha) < 8 or len(senha) > 20:
            return generate_response(8, ip=meu_ip)

        if not self.is_valid_email(email):
            return generate_response(9, ip=meu_ip)
        
        if " " not in nome_completo or len(nome_completo) < 5:
            return generate_response(18, ip=meu_ip)

        #Criar conta
        codigo_ativacao = "".join([str(random.randint(0, 9)) for _ in range(6)])
        data_atual = datetime.now()
        tipo_conta = "usuario"
        if email == "gladistony.silva@ufrpe.br":
            tipo_conta = "admin"

        sql = "INSERT INTO contas (nome_usuario, senha, email, nome_completo, codigo_ativacao, data_criacao, tipo_conta) VALUES (:nome_usuario, :senha, :email, :nome_completo, :codigo_ativacao, :now, :tipo_conta)"
        params = {"nome_usuario": nome_usuario, "senha": senha, "email": email, "nome_completo": nome_completo, "codigo_ativacao": codigo_ativacao, "now": data_atual, "tipo_conta": tipo_conta}
        if self.operacaoSql(sql, params):
            enviar_email_ativacao(nome_usuario, codigo_ativacao, email)
            return generate_response(19, ip=meu_ip)
        return generate_response(5, ip=meu_ip)
        
    def atualizar_login(self, nome_usuario):
        # Atualiza a data de login de uma conta.
        query = """UPDATE contas SET data_ultimo_login = :now, tentativas_senha_incorreta = 0 WHERE nome_usuario = :nome_usuario"""
        self.operacaoSql(query, {"now": datetime.now(), "nome_usuario": nome_usuario})

    def incrementar_tentativas(self, nome_usuario):
        # Incrementa o número de tentativas de senha incorreta de uma conta.
        agora = datetime.now()
        query = """UPDATE contas SET tentativas_senha_incorreta = tentativas_senha_incorreta + 1 WHERE nome_usuario = :nome_usuario"""
        self.operacaoSql(query, {"nome_usuario": nome_usuario})
        query = """UPDATE contas SET data_ultimo_login = :agora WHERE nome_usuario = :nome_usuario"""
        self.operacaoSql(query, {"agora": agora, "nome_usuario": nome_usuario})

    def bloquear_conta(self, nome_usuario):
        # Bloqueia uma conta e gera um código de ativação.
        codigo_ativacao = "".join([str(random.randint(0, 9)) for _ in range(6)])
        query = """UPDATE contas SET conta_bloqueada = 0, codigo_ativacao = :codigo_ativacao WHERE nome_usuario = :nome_usuario"""
        self.operacaoSql(query, {"codigo_ativacao": codigo_ativacao, "nome_usuario": nome_usuario})
        return codigo_ativacao

        #Tentativa de login
    def tentativa_login(self, nome_usuario, senha, meu_ip):
        if not self.engine:
            return generate_response(5, ip=meu_ip)

        conta = self.selecionar_conta(nome_usuario)

        if not conta:
            return generate_response(4, ip=meu_ip)

        if not conta.conta_bloqueada:  # Conta bloqueada
            return generate_response(3, ip=meu_ip)
        
        if conta.tentativas_senha_incorreta is None:
            sql_update_query = """UPDATE contas SET tentativas_senha_incorreta = 0 WHERE nome_usuario = :nome_usuario"""
            self.operacaoSql(sql_update_query, {"nome_usuario": nome_usuario})
            conta = self.selecionar_conta(nome_usuario)

        if conta.tentativas_senha_incorreta < 3:  # Menos de 3 tentativas incorretas
            if conta.senha == senha:
                self.atualizar_login(nome_usuario)
                return generate_response(0, data=self.data_conta_organizada(conta), ip=meu_ip)
            else:
                self.incrementar_tentativas(nome_usuario)
                return generate_response(1, ip=meu_ip)

        if type(conta.data_ultimo_login) == str:
            ultima_tentativa = datetime.strptime(conta.data_ultimo_login, "%Y-%m-%d %H:%M:%S.%f")
        else:
            ultima_tentativa = conta.data_ultimo_login
        tempo_atual = datetime.now()
        diferenca_em_segundos = (tempo_atual - ultima_tentativa).total_seconds()

        if diferenca_em_segundos < 300:
            return generate_response(2, restante=300 - diferenca_em_segundos)

        if conta.tentativas_senha_incorreta < 6:  # Menos de 6 tentativas incorretas
            if conta.senha == senha:
                self.atualizar_login(nome_usuario)
                return generate_response(0, data=self.data_conta_organizada(conta), ip=meu_ip)
            else:
                self.incrementar_tentativas(nome_usuario)
                return generate_response(1, ip=meu_ip)

        # Bloquear a conta e enviar código de ativação
        codigo_ativacao = self.bloquear_conta(nome_usuario)
        email = conta.email
        enviar_email_ativacao(nome_usuario, codigo_ativacao, email)
        return generate_response(2, ip=meu_ip)

    def ativar_conta(self, nome_usuario, codigo_ativacao):
        conta = self.selecionar_conta(nome_usuario)

        if not conta:
            return generate_response(4)

        if conta.conta_bloqueada:  # Conta já está ativa
            return generate_response(11)

        if conta.codigo_ativacao == codigo_ativacao:
            sql_update_query = """UPDATE contas SET conta_bloqueada = 1, tentativas_senha_incorreta = 0 WHERE nome_usuario = :nome_usuario"""
            self.operacaoSql(sql_update_query, {"nome_usuario": nome_usuario})
            return generate_response(0)
        else:
            return generate_response(10)
    
    def otimizar_estoque(self, id_estoque):
        #apagar todas as entradas de produtos com quantidade 0
        query = """DELETE FROM registro_produto_estoque WHERE quantidade = 0 AND id_estoque = :id_estoque"""
        self.operacaoSql(query, {"id_estoque": id_estoque})
        return generate_response(0)

    def receber_produtos(self, id_estoque):
        produtos = []
        self.otimizar_estoque(id_estoque)
        query = """SELECT * FROM registro_produto_estoque WHERE id_estoque = :id_estoque"""
        params = {"id_estoque": id_estoque}
        result = self.consultaSql(query, params)
        if result:
            estoque_temp = {}
            for row in result:
                id_produto = row.id_produto
                quantidade = row.quantidade
                data_validade = row.data_validade
                preco = row.preco
                if id_produto not in estoque_temp:
                    query = """SELECT * FROM produto WHERE id = :id_produto"""
                    params = {"id_produto": id_produto}
                    result = self.consultaSql(query, params).fetchone()
                    if result:
                        estoque_temp[id_produto] = {"nome": result.nome, "descricao": result.descricao, "foto": result.foto, "quantidade": [quantidade], "data_validade": [data_validade], "preco": [preco]}
                else:
                    estoque_temp[id_produto]["quantidade"].append(quantidade)
                    estoque_temp[id_produto]["data_validade"].append(data_validade)
                    estoque_temp[id_produto]["preco"].append(preco)
            for key in estoque_temp:
                quantidade_total = sum(estoque_temp[key]["quantidade"])
                data_validade = estoque_temp[key]["data_validade"]
                preco_medio = sum(estoque_temp[key]["preco"]) / len(estoque_temp[key]["preco"])
                produtos.append({"id": key, "nome": estoque_temp[key]["nome"], "descricao": estoque_temp[key]["descricao"], "foto": estoque_temp[key]["foto"], "quantidade": quantidade_total, "data_validade": data_validade, "preco_medio": preco_medio, "lista_precos": estoque_temp[key]["preco"], "lista_quantidades": estoque_temp[key]["quantidade"]})
        return produtos
    
    def mudar_produto(self, id_produto, quantidade, id_estoque, usuario):
        #verificar se o usuario é dono do estoque
        conta = self.selecionar_conta(usuario)
        if not conta:
            return generate_response(4)
        query = """SELECT * FROM estoque WHERE id = :id_estoque"""
        params = {"id_estoque": id_estoque}
        result = self.consultaSql(query, params).fetchone()
        if not result:
            return generate_response(26)
        if result.id_conta != conta.id and conta.tipo_conta != "admin":
            return generate_response(27)
        return self.mudar_quantiade_produto(id_produto, id_estoque, quantidade)

    def mudar_quantiade_produto(self, id_produto, id_estoque, quantidade):
        #Pegar todos os produtos com o id_produto no estoque
        query = """SELECT * FROM registro_produto_estoque WHERE id_produto = :id_produto AND id_estoque = :id_estoque"""
        params = {"id_produto": id_produto, "id_estoque": id_estoque}
        result = self.consultaSql(query, params)
        result = result.fetchall()
        if len(result) == 0:
                return generate_response(24)
        if result:
            if quantidade > 0:
                #Adicionar quantidade ao ultimo produto
                query = """UPDATE registro_produto_estoque SET quantidade = quantidade + :quantidade WHERE id = :id_produto"""
                params = {"quantidade": quantidade, "id_produto": result[-1].id}
                self.operacaoSql(query, params)
                return generate_response(0)
            elif quantidade < 0:
                #Iniciar loop de remoção de produtos
                quantidade = abs(quantidade)
                for row in result:
                    if row.quantidade >= quantidade:
                        query = """UPDATE registro_produto_estoque SET quantidade = quantidade - :quantidade WHERE id = :id_produto"""
                        params = {"quantidade": quantidade, "id_produto": row.id}
                        self.operacaoSql(query, params)
                        return generate_response(0)
                    else:
                        query = """UPDATE registro_produto_estoque SET quantidade = 0 WHERE id = :id_produto"""
                        self.operacaoSql(query, {"id_produto": row.id})
                        quantidade -= row.quantidade
                return generate_response(28, faltaram=quantidade)
        else:
            return generate_response(23)
    
    def data_conta_organizada(self, conta):
        #Verificar se existem estoques para a conta
        query = """SELECT * FROM estoque WHERE id_conta = :id_conta"""
        params = {"id_conta": conta.id}
        result = self.consultaSql(query, params)
        estoque = []
        if result:
            for row in result:
                #Buscar Listagens das cameras
                query = """SELECT * FROM cameras WHERE id_conta = :id_estoque"""
                params = {"id_estoque": row.id}
                result = self.consultaSql(query, params)
                cameras = []
                if result:
                    for rowcamera in result:
                        cameras.append({"id": rowcamera.id, "nome": rowcamera.nome, "descricao": rowcamera.descricao, "codigo_camera": rowcamera.codigo_camera})
                #Buscar Listagens dos produtos
                produtos = self.receber_produtos(row.id)
                estoque.append({"id": row.id, "nome": row.nome, "descricao": row.descricao, "imagem": row.imagem, "cameras": cameras, "produtos": produtos})

        return {
            "usuario": conta.nome_usuario,
            "email": conta.email,
            "nome_completo": conta.nome_completo,
            "criacao": conta.data_criacao,
            "ultimo_login": conta.data_ultimo_login,
            "url_foto": conta.url_foto,
            "tipo_conta": conta.tipo_conta,
            "estoque": estoque
        }

    def give_data(self, nome_usuario):
        conta = self.selecionar_conta(nome_usuario)
        if conta:
            return self.data_conta_organizada(conta)
        return None

    def recover(self, usuario):
        # Recupera a conta de um usuário.
        query = """SELECT * FROM contas WHERE nome_usuario = :usuario"""
        conta = self.consultaSql(query, {"usuario": usuario}).fetchone()
        if not conta:
            return generate_response(4)
        if not conta.conta_bloqueada:  # Conta bloqueada
            return generate_response(3)
        codigo_ativacao = self.bloquear_conta(usuario)
        #definir senha da conta como um codigo de 8 digitos
        senha = "".join([str(random.randint(0, 9)) for _ in range(8)])
        query = """UPDATE contas SET senha = :senha WHERE nome_usuario = :usuario"""
        self.operacaoSql(query, {"senha": senha, "usuario": usuario})
        enviar_email_recuperacao(usuario, codigo_ativacao, conta.email, senha)
        return generate_response(21)
    
    def set_img_url(self, nome_usuario, url_foto):
        query = """UPDATE contas SET url_foto = :url_foto WHERE nome_usuario = :nome_usuario"""
        self.operacaoSql(query, {"url_foto": url_foto, "nome_usuario": nome_usuario})
        return generate_response(0)
    
    def charge_password(self, nome_usuario, senha, nova_senha):
        conta = self.selecionar_conta(nome_usuario)
        if conta:
            if conta.senha == senha:
                query = """UPDATE contas SET senha = :nova_senha WHERE nome_usuario = :nome_usuario"""
                self.operacaoSql(query, {"nova_senha": nova_senha, "nome_usuario": nome_usuario})
                return generate_response(0)
            else:
                return generate_response(1)
        return generate_response(4)
    
    def get_email(self, usuario):
        # Retorna o email de um usuário, ofuscando parte do endereço de email.
        query = """SELECT * FROM contas WHERE nome_usuario = :usuario"""
        conta = self.consultaSql(query, {"usuario": usuario}).fetchone()
        if not conta:
            return generate_response(4)
        email = conta.email
        endereco, dominio = email.split('@')
        if len(endereco) > 3:
            endereco = endereco[:3] + '*' * (len(endereco) - 3)
        email_modificado = endereco + "@" + dominio
        return generate_response(0, email=email_modificado)
    
    def get_all_user(self, admin):
        conta = self.selecionar_conta(admin)
        if not conta:
            return generate_response(4)
        if conta.tipo_conta != "admin":
            return generate_response(22)
        query = """SELECT * FROM contas"""
        result = self.consultaSql(query)
        contas = []
        for row in result:
            contas.append({"usuario": row.nome_usuario, "email": row.email })
        return generate_response(0, contas=contas)
    
    def get_user_data(self, usuario, admin):
        conta = self.selecionar_conta(admin)
        if not conta:
            return generate_response(4)
        if conta.tipo_conta != "admin":
            return generate_response(22)
        query = """SELECT * FROM contas WHERE nome_usuario = :usuario"""
        result = self.consultaSql(query, {"usuario": usuario}).fetchone()
        if not result:
            return generate_response(4)
        return generate_response(0, data=self.data_conta_organizada(result))
    
    def set_user_data(self, usuario, senha, admin):
        conta = self.selecionar_conta(admin)
        if not conta:
            return generate_response(4)
        if conta.tipo_conta != "admin":
            return generate_response(22)
        query = """UPDATE contas SET senha = :senha WHERE nome_usuario = :usuario"""
        self.operacaoSql(query, {"senha": senha, "usuario": usuario})
        return generate_response(0)
    
    def get_estoque(self, admin):
        conta = self.selecionar_conta(admin)
        if not conta:
            return generate_response(4)
        id_conta = conta.id
        query = """SELECT * FROM estoque WHERE id_conta = :id_conta"""
        params = {"id_conta": id_conta}
        result = self.consultaSql(query, params)
        estoque = []
        for row in result:
            data_estoque = {"id": row.id, "nome": row.nome, "descricao": row.descricao, "imagem": row.imagem}
            #Buscar Listagens das cameras
            query = """SELECT * FROM cameras WHERE id_conta = :id_estoque"""
            params = {"id_estoque": row.id}
            result = self.consultaSql(query, params)
            cameras = []
            if result:
                for rowcamera in result:
                    cameras.append({"id": rowcamera.id, "nome": rowcamera.nome, "descricao": rowcamera.descricao, "codigo_camera": rowcamera.codigo_camera})
            data_estoque["cameras"] = cameras
            #Buscar Listagens dos produtos
            produtos = self.receber_produtos(row.id)
            data_estoque["produtos"] = produtos
            estoque.append(data_estoque)
        return generate_response(0, estoque=estoque)
    
    def charge_estoque_image(self, usuario, id_estoque, imagem):
        #Verificar propriedade do estoque
        conta = self.selecionar_conta(usuario)
        if not conta:
            return generate_response(4)
        query = """SELECT * FROM estoque WHERE id = :id_estoque"""
        params = {"id_estoque": id_estoque}
        result = self.consultaSql(query, params).fetchone()
        if not result:
            return generate_response(26)
        if result.id_conta != conta.id and conta.tipo_conta != "admin":
            return generate_response(27)
        query = """UPDATE estoque SET imagem = :imagem WHERE id = :id_estoque"""
        self.operacaoSql(query, {"imagem": imagem, "id_estoque": id_estoque})
        return generate_response(0)
    
    def get_produto(self, admin, id_produto):
        conta = self.selecionar_conta(admin)
        if not conta:
            return generate_response(4)
        query = """SELECT * FROM produto WHERE id = :id_produto"""
        params = {"id_produto": id_produto}
        result = self.consultaSql(query, params).fetchone()
        if not result:
            return generate_response(23)
        return generate_response(0, produto={"id": result.id, "nome": result.nome, "descricao": result.descricao, "foto": result.foto})
    
    def registro_produto(self, admin, nome, descricao, imagem):
        conta = self.selecionar_conta(admin)
        if not conta:
            return generate_response(4)
        query = """INSERT INTO produto (nome, descricao, foto) VALUES (:nome, :descricao, :imagem)"""
        params = {"nome": nome, "descricao": descricao, "imagem": imagem}
        self.operacaoSql(query, params)
        id_produto = self.consultaSql("SELECT last_insert_rowid()").fetchone()[0]
        return generate_response(0, id_produto=id_produto)
    
    def registro_produto_estoque(self, admin, id_estoque, id_produto, quantidade, data_validade, preco):
        conta = self.selecionar_conta(admin)
        if not conta:
            return generate_response(4)
        query = """INSERT INTO registro_produto_estoque (id_estoque, id_produto, quantidade, data_validade, preco) VALUES (:id_estoque, :id_produto, :quantidade, :data_validade, :preco)"""
        params = {"id_estoque": id_estoque, "id_produto": id_produto, "quantidade": quantidade, "data_validade": data_validade, "preco": preco}
        self.operacaoSql(query, params)
        return generate_response(0)
    
    def criar_estoque(self, admin, nome, descricao, imagem):
        conta = self.selecionar_conta(admin)
        if not conta:
            return generate_response(4)
        query = """INSERT INTO estoque (id_conta, nome, descricao, imagem) VALUES (:id_conta, :nome, :descricao, :imagem)"""
        params = {"id_conta": conta.id, "nome": nome, "descricao": descricao, "imagem": imagem}
        self.operacaoSql(query, params)
        id_estoque = self.consultaSql("SELECT last_insert_rowid()").fetchone()[0]
        return generate_response(0, id_estoque=id_estoque)
    
    def cadastrar_camera(self, admin, nome, descricao, codigo_camera, id_estoque):
        conta = self.selecionar_conta(admin)
        if not conta:
            return generate_response(4)
        #Verificar se o estoque pertence ao dono
        query = """SELECT * FROM estoque WHERE id = :id_estoque"""
        params = {"id_estoque": id_estoque}
        result = self.consultaSql(query, params).fetchone()
        if not result:
            return generate_response(26)
        if result.id_conta != conta.id and conta.tipo_conta != "admin":
            return generate_response(27)
        query = """INSERT INTO cameras (id_conta, nome, descricao, codigo_camera) VALUES (:id_estoque, :nome, :descricao, :codigo_camera)"""
        params = {"id_estoque": id_estoque, "nome": nome, "descricao": descricao, "codigo_camera": codigo_camera}
        self.operacaoSql(query, params)
        return generate_response(0, id_camera=codigo_camera)
    
    def delete_user(self, usuario, admin):
        conta = self.selecionar_conta(admin)
        if not conta:
            return generate_response(4)
        if conta.tipo_conta != "admin":
            return generate_response(22)
        query = """DELETE FROM contas WHERE nome_usuario = :usuario"""
        self.operacaoSql(query, {"usuario": usuario})
        return generate_response(0)
    
    def get_user_estoque(self, usuario, admin):
        conta = self.selecionar_conta(admin)
        if not conta:
            return generate_response(4)
        if conta.tipo_conta != "admin":
            return generate_response(22)
        return self.get_estoque(usuario)

    def deletar_estoque(self, id_estoque, dono):
        conta = self.selecionar_conta(dono)
        if not conta:
            return generate_response(4)
        #Verificar se o estoque pertence ao dono
        query = """SELECT * FROM estoque WHERE id = :id_estoque"""
        params = {"id_estoque": id_estoque}
        result = self.consultaSql(query, params).fetchone()
        if not result:
            return generate_response(26)
        if result.id_conta != conta.id and conta.tipo_conta != "admin":
            return generate_response(27)
        query = """DELETE FROM estoque WHERE id = :id_estoque"""
        self.operacaoSql(query, params)
        return generate_response(0)
    
    def lista_produtos(self, id_produto):
        if id_produto == "all":
            query = """SELECT * FROM produto"""
            result = self.consultaSql(query)
            produtos = []
            for row in result:
                produtos.append({"id": row.id, "nome": row.nome, "descricao": row.descricao, "foto": row.foto})
            return generate_response(0, produtos=produtos)
        else:
            query = """SELECT * FROM produto WHERE id = :id_produto"""
            params = {"id_produto": id_produto}
            result = self.consultaSql(query, params).fetchone()
            if not result:
                return generate_response(23)
            return generate_response(0, produto={"id": result.id, "nome": result.nome, "descricao": result.descricao, "foto": result.foto})
        


        
