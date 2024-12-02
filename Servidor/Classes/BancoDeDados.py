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
    
    def tentativa_login(self, nome_usuario, senha):
        """Tenta fazer login com um usuário e senha."""
        if self.connection:
            cursor = self.connection.cursor()
            #Tentar selecionar a conta
            sql_select_query = """SELECT * FROM contas WHERE nome_usuario = %s"""
            cursor.execute(sql_select_query, (nome_usuario,))
            conta = cursor.fetchone()
            if conta:
                #Verificar se a conta não está bloqueada
                if conta[6] == False:
                    #Verificar se o numero de tentativas de senha incorreta é menor que 3
                    if conta[7] < 3:
                        #Verificar se a senha está correta
                        if conta[2] == senha:
                            #Atualizar a data do ultimo login e limpar o numero de tentativas de senha incorreta
                            sql_update_query = """UPDATE contas SET data_ultimo_login = NOW(), tentativas_senha_incorreta = 0 WHERE nome_usuario = %s"""
                            cursor.execute(sql_update_query, (nome_usuario,))
                            self.connection.commit()
                            return {"status": "sucesso", "code":0, "data":conta, "message":"Login bem-sucedido"}
                        else:
                            #Incrementar o numero de tentativas de senha incorreta
                            sql_update_query = """UPDATE contas SET tentativas_senha_incorreta = tentativas_senha_incorreta + 1 WHERE nome_usuario = %s"""
                            cursor.execute(sql_update_query, (nome_usuario,))
                            self.connection.commit()
                            return {"status": "erro", "code":1, "message":"Senha incorreta"}
                    else:
                        ultima_tentativa = conta[8]
                        tempo_atual = datetime.now()
                        diferenca = tempo_atual - ultima_tentativa
                        diferenca_em_segundos = diferenca.total_seconds()
                        if diferenca_em_segundos < 300:
                            return {"status": "erro", "code":2, "message":"Conta bloqueada por excesso de tentativas de senha incorreta, aguarde 5 minutos", "restante": 300 - diferenca_em_segundos}
                        else:
                            #Verificar se o numero de tentativas de senha incorreta é menor que 6
                            if conta[7] < 6:
                                if conta[2] == senha:
                                    #Atualizar a data do ultimo login e limpar o numero de tentativas de senha incorreta
                                    sql_update_query = """UPDATE contas SET data_ultimo_login = NOW(), tentativas_senha_incorreta = 0 WHERE nome_usuario = %s"""
                                    cursor.execute(sql_update_query, (nome_usuario,))
                                    self.connection.commit()
                                    return {"status": "sucesso", "code":0, "data":conta, "message":"Login bem-sucedido"}
                                else:
                                    #Incrementar o numero de tentativas de senha incorreta
                                    sql_update_query = """UPDATE contas SET tentativas_senha_incorreta = tentativas_senha_incorreta + 1 WHERE nome_usuario = %s"""
                                    cursor.execute(sql_update_query, (nome_usuario,))
                                    self.connection.commit()
                                    return {"status": "erro", "code":1, "message":"Senha incorreta"}
                            else:
                                #Bloquear a conta e mandar novo código de ativação
                                codigo_ativacao = ""
                                for i in range(6):
                                    codigo_ativacao += str(random.randint(0, 9))
                                sql_update_query = """UPDATE contas SET conta_bloqueada = 1, codigo_ativacao = %s WHERE nome_usuario = %s"""
                                cursor.execute(sql_update_query, (codigo_ativacao, nome_usuario))
                                self.connection.commit()
                                #Enviar email com o código de ativação
                                string_cod = self.msgpadrao.replace("CODIGO", codigo_ativacao)
                                string_final = string_cod.replace("USUARIO", nome_usuario)
                                print(string_final)
                                emailGmail.codigoAtivacao(string_final , conta[4])
                                return {"status": "erro", "code":2, "message":"Conta bloqueada por excesso de tentativas de senha incorreta, ativação novamente necessária"}
                else:
                    return {"status": "erro", "code":3, "message":"Conta não está ativa"}
            else:
                return {"status": "erro", "code":4, "message":"Conta não encontrada"}
        else:
            return {"status": "erro", "code":5, "message":"Conexão com o banco de dados não foi estabelecida"}
    
    def criar_conta(self, nome_usuario, senha, email, nome_completo, anotacoes, numero_telefone):
        """Cria uma nova conta."""
        if self.connection:
            cursor = self.connection.cursor()
            #Verificar se a conta já existe
            sql_select_query = """SELECT * FROM contas WHERE nome_usuario = %s"""
            cursor.execute(sql_select_query, (nome_usuario,))
            conta = cursor.fetchone()
            if conta:
                return {"status": "erro", "code":6, "message":"Conta já existe"}
            else:
                #Verificar se as informaçoes são válidas
                if len(nome_usuario) < 8 or len(nome_usuario) > 20:
                    return {"status": "erro", "code":7, "message":"Nome de usuário deve ter entre 8 e 20 caracteres"}
                #Verificar se o nome de usuário só contém letras e números e não espaços
                if not nome_usuario.isalnum():
                    return {"status": "erro", "code":8, "message":"Nome de usuário deve conter apenas letras e números"}
                if " " in nome_usuario:
                    return {"status": "erro", "code":9, "message":"Nome de usuário não pode conter espaços"}
                if len(senha) < 8 or len(senha) > 20:
                    return {"status": "erro", "code":8, "message":"Senha deve ter entre 8 e 20 caracteres"}
                #Consultar se o provedor de email é valido
                if self.is_valid_email(email) == False:
                    return {"status": "erro", "code":9, "message":"Email inválido"}
                #Gerar codigo de 6 digitos para ativação da conta
                codigo_ativacao = ""
                for i in range(6):
                    codigo_ativacao += str(random.randint(0, 9))
                #Inserir a nova conta
                sql_insert_query = """INSERT INTO contas (nome_usuario, senha, data_criacao, email, codigo_ativacao, conta_bloqueada, tentativas_senha_incorreta, data_ultimo_login, nome_completo, anotacoes, numero_telefone) VALUES (%s, %s, NOW(), %s, %s, 1, 0, NOW(), %s, %s, %s)"""
                cursor.execute(sql_insert_query, (nome_usuario, senha, email, codigo_ativacao, nome_completo, anotacoes, numero_telefone))
                self.connection.commit()
                #Enviar email com o código de ativação
                string_cod = self.msgpadrao.replace("CODIGO", codigo_ativacao)
                string_cod = string_cod.replace("USUARIO", nome_usuario)
                emailGmail.codigoAtivacao(string_cod, email)
                sql_select_query = """SELECT * FROM contas WHERE nome_usuario = %s"""
                cursor.execute(sql_select_query, (nome_usuario,))
                conta = cursor.fetchone()
                return {"status": "sucesso", "code":0, "message":"Conta criada com sucesso", "data":conta}
        else:
            return {"status": "erro", "code":5, "message":"Conexão com o banco de dados não foi estabelecida"}
    
    def ativar_conta(self, nome_usuario, codigo_ativacao):
        """Ativa uma conta."""
        if self.connection:
            cursor = self.connection.cursor()
            #Verificar se a conta existe
            sql_select_query = """SELECT * FROM contas WHERE nome_usuario = %s"""
            cursor.execute(sql_select_query, (nome_usuario,))
            conta = cursor.fetchone()
            if conta:
                #Verificar se a conta já está ativa
                if conta[6] == True:
                    #Verificar se o código de ativação está correto
                    if conta[5] == codigo_ativacao:
                        #Ativar a conta e limpa numero de tentativas de senha incorreta
                        sql_update_query = """UPDATE contas SET conta_bloqueada = 0, tentativas_senha_incorreta = 0 WHERE nome_usuario = %s"""
                        cursor.execute(sql_update_query, (nome_usuario,))
                        self.connection.commit()
                        return {"status": "sucesso", "code":0, "message":"Conta ativada com sucesso"}
                    else:
                        return {"status": "erro", "code":7, "message":"Código de ativação incorreto"}
                else:
                    return {"status": "erro", "code":8, "message":"Conta já está ativa"}
            else:
                return {"status": "erro", "code":4, "message":"Conta não encontrada"}
        else:
            return {"status": "erro", "code":5, "message":"Conexão com o banco de dados não foi estabelecida"}


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
