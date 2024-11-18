import mysql.connector
from mysql.connector import Error

def create_connection():
    """Cria uma conexão com o banco de dados MySQL usando o IP do WSL."""
    try:
        connection = mysql.connector.connect(
            host='127.0.0.1',  # Substitua pelo endereço IP do WSL
            port=3306,
            database='Projeto3',
            user='servidor',
            password='159753'
        )
        if connection.is_connected():
            print("Conexão ao banco de dados MySQL foi bem-sucedida")
            return connection
    except Error as e:
        print(f"Erro ao conectar ao MySQL: {e}")
        return None

def insert_into_contas(connection, dados):
    """Insere uma nova entrada na tabela 'contas'."""
    try:
        cursor = connection.cursor()
        sql_insert_query = """ INSERT INTO contas (nome_usuario, senha, data_criacao, email, codigo_ativacao, conta_bloqueada, tentativas_senha_incorreta, data_ultimo_login, nome_completo, anotacoes, numero_telefone) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"""
        cursor.execute(sql_insert_query, dados)
        connection.commit()
        print("Registro inserido com sucesso na tabela 'contas'")
    except mysql.connector.Error as e:
        print(f"Erro ao inserir dados na tabela 'contas': {e}")

def main():
    # Criar conexão com o banco de dados
    connection = create_connection()
    if connection:
        # Dados a serem inseridos na tabela 'contas'
        dados = (
            'novo_usuario',           # nome_usuario
            'senha_segura',           # senha
            '2024-11-11 12:00:00',    # data_criacao
            'usuario@exemplo.com',    # email
            '123456',                 # codigo_ativacao
            False,                    # conta_bloqueada
            0,                        # tentativas_senha_incorreta
            '2024-11-11 12:00:00',    # data_ultimo_login
            'Nome Completo',          # nome_completo
            'Anotações diversas',     # anotacoes
            '1234567890'              # numero_telefone
        )
        # Inserir os dados na tabela
        insert_into_contas(connection, dados)
        # Fechar conexão
        connection.close()

if __name__ == '__main__':
    main()
