import mysql.connector
from mysql.connector import Error

class BancoDeDados:
    def __init__(self, host, port, database, user, password):
        self.host = host
        self.port = port
        self.database = database
        self.user = user
        self.password = password
        self.connection = None
    
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
                print(conta[5])
                if conta[5] == False:
                    #Verificar se o numero de tentativas de senha incorreta é menor que 3
                    if conta[6] < 3:
                        #Verificar se a senha está correta
                        if conta[1] == senha:
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
                        return {"status": "erro", "code":2, "message":"Conta bloqueada por excesso de tentativas de senha incorreta"}
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
                #Inserir a nova conta
                sql_insert_query = """INSERT INTO contas (nome_usuario, senha, data_criacao, email, codigo_ativacao, conta_bloqueada, tentativas_senha_incorreta, data_ultimo_login, nome_completo, anotacoes, numero_telefone) VALUES (%s, %s, NOW(), %s, %s, 1, 0, NOW(), %s, %s, %s)"""
                cursor.execute(sql_insert_query, (nome_usuario, senha, email, "123456", nome_completo, anotacoes, numero_telefone))
                self.connection.commit()
                return {"status": "sucesso", "code":0, "message":"Conta criada com sucesso"}
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
                if conta[5] == False:
                    #Verificar se o código de ativação está correto
                    if conta[4] == codigo_ativacao:
                        #Ativar a conta
                        sql_update_query = """UPDATE contas SET conta_bloqueada = 0 WHERE nome_usuario = %s"""
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
