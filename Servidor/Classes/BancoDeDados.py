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
    
    def get_data(self, id_conta):
        """Retorna os dados de uma conta."""
        try:
            cursor = self.connection.cursor()
            sql_select_query = """SELECT * FROM contas WHERE id = %s"""
            cursor.execute(sql_select_query, (id_conta,))
            return cursor.fetchone()
        except Error as e:
            #Verificar se a conexao foi fechada e reabrir ela
            if self.connection.is_connected() == False:
                self.create_connection()
                cursor = self.connection.cursor()
                cursor.execute(sql_select_query, (id_conta,))
                return cursor.fetchone()

    def recover(self, usuario):
        cursor = self.connection.cursor()
        conta = self.selecionar_conta(cursor, usuario)
        if not conta:
            return {"status": "erro", "code":4, "message":"Conta não encontrada"}
        if conta[6]:  # Conta bloqueada
            return {"status": "erro", "code":3, "message":"Conta não está ativa"}
        codigo_ativacao = self.bloquear_conta(cursor, usuario)
        #mudar a senha para o codigo de ativação
        sql_update_query = """UPDATE contas SET senha = %s WHERE nome_usuario = %s"""
        cursor = self.connection.cursor()
        cursor.execute(sql_update_query, (codigo_ativacao, usuario))
        self.connection.commit()
        #enviar email com o codigo de ativação
        self.enviar_email_ativacao(emailGmail, self.msgpadrao, usuario, codigo_ativacao, conta[4])

            

    def set_img(self, id_conta, url):
        """Define a URL da imagem de perfil de uma conta."""
        #if not self.connection:
        #    return {"status": "erro", "code":5, "message":"Conexão com o banco de dados não foi estabelecida"}

        sql_update_query = """UPDATE contas SET anotacoes = %s WHERE id = %s"""
        try:
            cursor = self.connection.cursor()
            cursor.execute(sql_update_query, (url, id_conta))
            self.connection.commit()
        except Error as e:
            #Verificar se a conexao foi fechada e reabrir ela
            if self.connection.is_connected() == False:
                self.create_connection()
                cursor = self.connection.cursor()
                cursor.execute(sql_update_query, (url, id_conta))
                self.connection.commit()
        return {"status": "sucesso", "code":0, "message":"URL da imagem de perfil definida com sucesso"}

    def is_valid_email(self, email):
        # Verifica se o formato do e-mail é válido
        if not validate_email(email):
            return False

        # Extrai o domínio do e-mail
        domain = email.split('@')[1]

        try:
            # Verifica se o domínio possui registros MX
            records = dns.resolver.resolve(domain, 'MX')
            return True if records else False
        except dns.resolver.NoAnswer:
            return False
        except dns.resolver.NXDOMAIN:
            return False
        except dns.resolver.Timeout:
            return False
        except dns.exception.DNSException:
            return False
    
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

    def selecionar_conta(self, cursor, nome_usuario):
        try:
            sql_select_query = """SELECT * FROM contas WHERE nome_usuario = %s"""
            cursor.execute(sql_select_query, (nome_usuario,))
            return cursor.fetchone()
        except Error as e:
            #Verificar se a conexao foi fechada e reabrir ela
            if self.connection.is_connected() == False:
                self.create_connection()
                cursor = self.connection.cursor()
                return self.selecionar_conta(cursor, nome_usuario)

    def atualizar_login(self, cursor, nome_usuario):
        try:
            sql_update_query = """UPDATE contas SET data_ultimo_login = NOW(), tentativas_senha_incorreta = 0 WHERE nome_usuario = %s"""
            cursor.execute(sql_update_query, (nome_usuario,))
        except Error as e:
            #Verificar se a conexao foi fechada e reabrir ela
            if self.connection.is_connected() == False:
                self.create_connection()
                cursor = self.connection.cursor()
                return self.atualizar_login(cursor, nome_usuario)

    def incrementar_tentativas(self, cursor, nome_usuario):
        try:
            sql_update_query = """UPDATE contas SET tentativas_senha_incorreta = tentativas_senha_incorreta + 1 WHERE nome_usuario = %s"""
            cursor.execute(sql_update_query, (nome_usuario,))
        except Error as e:
            #Verificar se a conexao foi fechada e reabrir ela
            if self.connection.is_connected() == False:
                self.create_connection()
                cursor = self.connection.cursor()
                return self.incrementar_tentativas(cursor, nome_usuario)

    def bloquear_conta(self, cursor, nome_usuario):
        try:
            codigo_ativacao = "".join([str(random.randint(0, 9)) for _ in range(6)])
            sql_update_query = """UPDATE contas SET conta_bloqueada = 1, codigo_ativacao = %s WHERE nome_usuario = %s"""
            cursor.execute(sql_update_query, (codigo_ativacao, nome_usuario))
            return codigo_ativacao
        except Error as e:
            #Verificar se a conexao foi fechada e reabrir ela
            if self.connection.is_connected() == False:
                self.create_connection()
                cursor = self.connection.cursor()
                return self.bloquear_conta(cursor, nome_usuario)

    def enviar_email_ativacao(self, emailGmail, msgpadrao, nome_usuario, codigo_ativacao, email):
        string_cod = msgpadrao.replace("CODIGO", codigo_ativacao)
        string_final = string_cod.replace("USUARIO", nome_usuario)
        print(string_final)
        emailGmail.codigoAtivacao(string_final, email)

    def tentativa_login(self, nome_usuario, senha):
        """Tenta fazer login com um usuário e senha."""
        if not self.connection:
            return {"status": "erro", "code":5, "message":"Conexão com o banco de dados não foi estabelecida"}

        cursor = self.connection.cursor()
        conta = self.selecionar_conta(cursor, nome_usuario)

        if not conta:
            return {"status": "erro", "code":4, "message":"Conta não encontrada"}

        if conta[6]:  # Conta bloqueada
            return {"status": "erro", "code":3, "message":"Conta não está ativa"}

        if conta[7] < 3:  # Menos de 3 tentativas incorretas
            if conta[2] == senha:
                self.atualizar_login(cursor, nome_usuario)
                self.connection.commit()
                return {"status": "sucesso", "code":0, "data":conta, "message":"Login bem-sucedido"}
            else:
                self.incrementar_tentativas(cursor, nome_usuario)
                self.connection.commit()
                return {"status": "erro", "code":1, "message":"Senha incorreta"}

        ultima_tentativa = conta[8]
        tempo_atual = datetime.now()
        diferenca_em_segundos = (tempo_atual - ultima_tentativa).total_seconds()

        if diferenca_em_segundos < 300:
            return {"status": "erro", "code":2, "message":"Conta bloqueada por excesso de tentativas de senha incorreta, aguarde 5 minutos", "restante": 300 - diferenca_em_segundos}

        if conta[7] < 6:  # Menos de 6 tentativas incorretas
            if conta[2] == senha:
                self.atualizar_login(cursor, nome_usuario)
                self.connection.commit()
                return {"status": "sucesso", "code":0, "data":conta, "message":"Login bem-sucedido"}
            else:
                self.incrementar_tentativas(cursor, nome_usuario)
                self.connection.commit()
                return {"status": "erro", "code":1, "message":"Senha incorreta"}

        # Bloquear a conta e enviar código de ativação
        codigo_ativacao = self.bloquear_conta(cursor, nome_usuario)
        self.connection.commit()
        self.enviar_email_ativacao(emailGmail, self.msgpadrao, nome_usuario, codigo_ativacao, conta[4])
        return {"status": "erro", "code":2, "message":"Conta bloqueada por excesso de tentativas de senha incorreta, ativação novamente necessária"}

    def criar_conta(self, nome_usuario, senha, email, nome_completo, anotacoes, numero_telefone):
        """Cria uma nova conta."""
        if not self.connection:
            return {"status": "erro", "code":5, "message":"Conexão com o banco de dados não foi estabelecida"}

        cursor = self.connection.cursor()
        conta = self.selecionar_conta(cursor, nome_usuario)

        if conta:
            return {"status": "erro", "code":6, "message":"Conta já existe"}

        if len(nome_usuario) < 8 or len(nome_usuario) > 20:
            return {"status": "erro", "code":7, "message":"Nome de usuário deve ter entre 8 e 20 caracteres"}

        if not nome_usuario.isalnum() or " " in nome_usuario:
            return {"status": "erro", "code":7, "message":"Nome de usuário deve conter apenas letras e números e não pode conter espaços"}

        if len(senha) < 8 or len(senha) > 20:
            return {"status": "erro", "code":8, "message":"Senha deve ter entre 8 e 20 caracteres"}

        if not self.is_valid_email(email):
            return {"status": "erro", "code":9, "message":"Email inválido"}

        codigo_ativacao = "".join([str(random.randint(0, 9)) for _ in range(6)])
        sql_insert_query = """INSERT INTO contas (nome_usuario, senha, data_criacao, email, codigo_ativacao, conta_bloqueada, tentativas_senha_incorreta, data_ultimo_login, nome_completo, anotacoes, numero_telefone) VALUES (%s, %s, NOW(), %s, %s, 1, 0, NOW(), %s, %s, %s)"""
        try:
            cursor.execute(sql_insert_query, (nome_usuario, senha, email, codigo_ativacao, nome_completo, anotacoes, numero_telefone))
            self.connection.commit()
        except Error as e:
            #Testar se a conexao foi fechada e reabrir ela
            if self.connection.is_connected() == False:
                self.create_connection()
                cursor = self.connection.cursor()
                cursor.execute(sql_insert_query, (nome_usuario, senha, email, codigo_ativacao, nome_completo, anotacoes, numero_telefone))

        self.enviar_email_ativacao(emailGmail, self.msgpadrao, nome_usuario, codigo_ativacao, email)
        conta = self.selecionar_conta(cursor, nome_usuario)
        return {"status": "sucesso", "code":0, "message":"Conta criada com sucesso", "data":conta}

    def ativar_conta(self, nome_usuario, codigo_ativacao):
        """Ativa uma conta."""
        if not self.connection:
            return {"status": "erro", "code":5, "message":"Conexão com o banco de dados não foi estabelecida"}

        cursor = self.connection.cursor()
        conta = self.selecionar_conta(cursor, nome_usuario)

        if not conta:
            return {"status": "erro", "code":4, "message":"Conta não encontrada"}

        if not conta[6]:  # Conta já está ativa
            return {"status": "erro", "code":11, "message":"Conta já está ativa"}

        if conta[5] == codigo_ativacao:
            sql_update_query = """UPDATE contas SET conta_bloqueada = 0, tentativas_senha_incorreta = 0 WHERE nome_usuario = %s"""
            try:
                cursor.execute(sql_update_query, (nome_usuario,))
                self.connection.commit()
            except Error as e:
                #Verificar se a conexao foi fechada e reabrir ela
                if self.connection.is_connected() == False:
                    self.create_connection()
                    cursor = self.connection.cursor()
                    cursor.execute(sql_update_query, (nome_usuario,))
                    self.connection.commit()
            return {"status": "sucesso", "code":0, "message":"Conta ativada com sucesso"}
        else:
            return {"status": "erro", "code":10, "message":"Código de ativação incorreto"}



# Exemplo de uso da classe BancoDeDados
if __name__ == "__main__":
    bd = BancoDeDados(
        host='localhost',
        port=3306,
        database='Projeto3',
        user='servidor',
        password='159753'
    )
    bd.create_connection()
