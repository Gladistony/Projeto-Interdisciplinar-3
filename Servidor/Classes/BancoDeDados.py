import random
from datetime import datetime
import mysql.connector
from mysql.connector import Error
import Classes.emailGmail as emailGmail
from validate_email_address import validate_email
import dns.resolver

class BancoDeDados:
    def __init__(self, host, port, database, user, password, msgpadrao):
        self.host = host
        self.port = port
        self.database = database
        self.user = user
        self.password = password
        self.connection = None
        self.msgpadrao = msgpadrao
    
    def create_connection(self):
        """Cria uma conexão com o banco de dados MySQL."""
        try:
            self.connection = mysql.connector.connect(
                host=self.host,
                port=self.port,
                database=self.database,
                user=self.user,
                password=self.password
            )
            if self.connection.is_connected():
                print("Conexão ao banco de dados MySQL foi bem-sucedida")
        except Error as e:
            print(f"Erro ao conectar ao MySQL: {e}")

    def execute_query(self, query, params):
        """Executa uma query no banco de dados."""
        try:
            cursor = self.connection.cursor()
            cursor.execute(query, params)
            return cursor
        except Error as e:
            if not self.connection.is_connected():
                self.create_connection()
                cursor = self.connection.cursor()
                cursor.execute(query, params)
                return cursor

    def generate_response(self, code, **kwargs):
        messages = {
            0: ("sucesso", "Operação realizada com sucesso"),
            1: ("erro", "Senha incorreta"),
            2: ("erro", "Conta bloqueada por excesso de tentativas de senha incorreta, ativação novamente necessária"),
            3: ("erro", "Conta não está ativa"),
            4: ("erro", "Conta não encontrada"),
            5: ("erro", "Conexão com o banco de dados não foi estabelecida"),
            6: ("erro", "Conta já existe"),
            7: ("erro", "Nome de usuário deve ter entre 8 e 20 caracteres e conter apenas letras e números, sem espaços"),
            8: ("erro", "Senha deve ter entre 8 e 20 caracteres"),
            9: ("erro", "Email inválido"),
            10: ("erro", "Código de ativação incorreto"),
            11: ("erro", "Conta já está ativa"),
            12: ("erro", "Id inválido"),
            13: ("erro", "Requisição vazia"),
            14: ("erro", "Usuário já logado"),
            15: ("erro", "Usuário não logado")
        }
        status, message = messages.get(code, ("erro", "Código desconhecido"))
        response = {"status": status, "code": code, "message": message}
        response.update(kwargs)
        return response

    def get_data(self, id_conta):
        #Retorna os dados de uma conta
        query = """SELECT * FROM contas WHERE id = %s"""
        cursor = self.execute_query(query, (id_conta,))
        return cursor.fetchone()

    def get_email(self, usuario):
        query = """SELECT * FROM contas WHERE nome_usuario = %s"""
        cursor = self.execute_query(query, (usuario,))
        conta = cursor.fetchone()
        if not conta:
            return self.generate_response(4)
        email = conta[4]
        endereco, dominio = email.split('@')
        if len(endereco) > 3:
            endereco = endereco[:3] + '*' * (len(endereco) - 3)
        email_modificado = endereco + "@" + dominio
        return self.generate_response(0, email=email_modificado)

    def recover(self, usuario):
        query = """SELECT * FROM contas WHERE nome_usuario = %s"""
        cursor = self.execute_query(query, (usuario,))
        conta = cursor.fetchone()
        if not conta:
            return self.generate_response(4)
        if conta[6]:  # Conta bloqueada
            return self.generate_response(3)
        codigo_ativacao = self.bloquear_conta(cursor, usuario)
        sql_update_query = """UPDATE contas SET senha = %s WHERE nome_usuario = %s"""
        self.execute_query(sql_update_query, (codigo_ativacao, usuario))
        self.connection.commit()
        self.enviar_email_ativacao(emailGmail, self.msgpadrao, usuario, codigo_ativacao, conta[4])

    def set_img(self, id_conta, url):
        query = """UPDATE contas SET anotacoes = %s WHERE id = %s"""
        self.execute_query(query, (url, id_conta))
        self.connection.commit()
        return self.generate_response(0)

    def is_valid_email(self, email):
        if not validate_email(email):
            return False
        domain = email.split('@')[1]
        try:
            records = dns.resolver.resolve(domain, 'MX')
            return True if records else False
        except (dns.resolver.NoAnswer, dns.resolver.NXDOMAIN, dns.resolver.Timeout, dns.exception.DNSException):
            return False

    def selecionar_conta(self, cursor, nome_usuario):
        query = """SELECT * FROM contas WHERE nome_usuario = %s"""
        cursor = self.execute_query(query, (nome_usuario,))
        return cursor.fetchone()

    def atualizar_login(self, cursor, nome_usuario):
        query = """UPDATE contas SET data_ultimo_login = NOW(), tentativas_senha_incorreta = 0 WHERE nome_usuario = %s"""
        self.execute_query(query, (nome_usuario,))

    def incrementar_tentativas(self, cursor, nome_usuario):
        query = """UPDATE contas SET tentativas_senha_incorreta = tentativas_senha_incorreta + 1 WHERE nome_usuario = %s"""
        self.execute_query(query, (nome_usuario,))

    def bloquear_conta(self, cursor, nome_usuario):
        codigo_ativacao = "".join([str(random.randint(0, 9)) for _ in range(6)])
        query = """UPDATE contas SET conta_bloqueada = 1, codigo_ativacao = %s WHERE nome_usuario = %s"""
        self.execute_query(query, (codigo_ativacao, nome_usuario))
        return codigo_ativacao

    def enviar_email_ativacao(self, emailGmail, msgpadrao, nome_usuario, codigo_ativacao, email):
        string_cod = msgpadrao.replace("CODIGO", codigo_ativacao)
        string_final = string_cod.replace("USUARIO", nome_usuario)
        print(string_final)
        emailGmail.codigoAtivacao(string_final, email)

    def tentativa_login(self, nome_usuario, senha):
        if not self.connection:
            return self.generate_response(5)

        cursor = self.connection.cursor()
        conta = self.selecionar_conta(cursor, nome_usuario)

        if not conta:
            return self.generate_response(4)

        if conta[6]:  # Conta bloqueada
            return self.generate_response(3)

        if conta[7] < 3:  # Menos de 3 tentativas incorretas
            if conta[2] == senha:
                self.atualizar_login(cursor, nome_usuario)
                self.connection.commit()
                return self.generate_response(0, data=conta)
            else:
                self.incrementar_tentativas(cursor, nome_usuario)
                self.connection.commit()
                return self.generate_response(1)

        ultima_tentativa = conta[8]
        tempo_atual = datetime.now()
        diferenca_em_segundos = (tempo_atual - ultima_tentativa).total_seconds()

        if diferenca_em_segundos < 300:
            return self.generate_response(2, restante=300 - diferenca_em_segundos)

        if conta[7] < 6:  # Menos de 6 tentativas incorretas
            if conta[2] == senha:
                self.atualizar_login(cursor, nome_usuario)
                self.connection.commit()
                return self.generate_response(0, data=conta)
            else:
                self.incrementar_tentativas(cursor, nome_usuario)
                self.connection.commit()
                return self.generate_response(1)

        # Bloquear a conta e enviar código de ativação
        codigo_ativacao = self.bloquear_conta(cursor, nome_usuario)
        self.connection.commit()
        self.enviar_email_ativacao(emailGmail, self.msgpadrao, nome_usuario, codigo_ativacao, conta[4])
        return self.generate_response(2)

    def criar_conta(self, nome_usuario, senha, email, nome_completo, anotacoes, numero_telefone):
        """Cria uma nova conta."""
        if not self.connection:
            return self.generate_response(5)

        cursor = self.connection.cursor()
        conta = self.selecionar_conta(cursor, nome_usuario)

        if conta:
            return self.generate_response(6)

        if len(nome_usuario) < 8 or len(nome_usuario) > 20 or not nome_usuario.isalnum() or " " in nome_usuario:
            return self.generate_response(7)

        if len(senha) < 8 or len(senha) > 20:
            return self.generate_response(8)

        if not self.is_valid_email(email):
            return self.generate_response(9)

        codigo_ativacao = "".join([str(random.randint(0, 9)) for _ in range(6)])
        sql_insert_query = """INSERT INTO contas (nome_usuario, senha, data_criacao, email, codigo_ativacao, conta_bloqueada, tentativas_senha_incorreta, data_ultimo_login, nome_completo, anotacoes, numero_telefone) VALUES (%s, %s, NOW(), %s, %s, 1, 0, NOW(), %s, %s, %s)"""
        try:
            cursor.execute(sql_insert_query, (nome_usuario, senha, email, codigo_ativacao, nome_completo, anotacoes, numero_telefone))
            self.connection.commit()
        except Error as e:
            if not self.connection.is_connected():
                self.create_connection()
                cursor = self.connection.cursor()
                cursor.execute(sql_insert_query, (nome_usuario, senha, email, codigo_ativacao, nome_completo, anotacoes, numero_telefone))

        self.enviar_email_ativacao(emailGmail, self.msgpadrao, nome_usuario, codigo_ativacao, email)
        conta = self.selecionar_conta(cursor, nome_usuario)
        return self.generate_response(0, data=conta)

    def ativar_conta(self, nome_usuario, codigo_ativacao):
        if not self.connection:
            return self.generate_response(5)

        cursor = self.connection.cursor()
        conta = self.selecionar_conta(cursor, nome_usuario)

        if not conta:
            return self.generate_response(4)

        if not conta[6]:  # Conta já está ativa
            return self.generate_response(11)

        if conta[5] == codigo_ativacao:
            sql_update_query = """UPDATE contas SET conta_bloqueada = 0, tentativas_senha_incorreta = 0 WHERE nome_usuario = %s"""
            try:
                cursor.execute(sql_update_query, (nome_usuario,))
                self.connection.commit()
            except Error as e:
                if not self.connection.is_connected():
                    self.create_connection()
                    cursor = self.connection.cursor()
                    cursor.execute(sql_update_query, (nome_usuario,))
                    self.connection.commit()
            return self.generate_response(0)
        else:
            return self.generate_response(10)

# Teste de conexão com o banco de dados
if __name__ == "__main__":
    bd = BancoDeDados(
        host='localhost',
        port=3306,
        database='Projeto3',
        user='servidor',
        password='159753'
    )
    bd.create_connection()
