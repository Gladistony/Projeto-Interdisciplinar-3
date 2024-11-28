
from Classes.BancoDeDados import BancoDeDados
from flask import Flask, request, jsonify
import uuid

#Dados da conexão com o banco de dados
host='127.0.0.1'  # Substitua pelo endereço IP do WSL
port=3306
database='Projeto3'
user='servidor'
password='159753'
#API Gemini
caminho_arquivo_api = "C:/API Gemi.txt"
EMAIL_PADRAO = "Esse é seu codigo de ativacao: CODIGO"


#Conectar com o banco de dados
database = BancoDeDados(host, port, database, user, password, EMAIL_PADRAO)
database.create_connection()


#Configurações do flash
app = Flask(__name__)

#Rotina de processamento
@app.route('/data_request', methods=['POST'])
def data_request():
    #Informação recebida
    data = request.json
    ID = uuid.uuid4()
    #Resposta
    response = {}
    response['status'] = 'ok'
    response['data'] = data
    return jsonify(response)

#Loop de opções do usuario
while True:
    print("1 - Criar conta")
    print("2 - Fazer login")
    print("3 - Ativar conta")
    print("4 - Sair")
    opcao = input("Digite a opção desejada: ")
    if opcao == '1':
        #Criar conta
        nome_usuario = input("Digite o nome de usuário: ")
        senha = input("Digite a senha: ")
        email = input("Digite o email: ")
        nome_completo = input("Digite o nome completo: ")
        anotacoes = input("Digite anotações: ")
        numero_telefone = input("Digite o número de telefone: ")
        print(database.criar_conta(nome_usuario, senha, email, nome_completo, anotacoes, numero_telefone))
    elif opcao == '2':
        #Fazer login
        nome_usuario = input("Digite o nome de usuário: ")
        senha = input("Digite a senha: ")
        resultado = database.tentativa_login(nome_usuario, senha)
        print(resultado)
    elif opcao == '3':
        #Ativar conta
        nome_usuario = input("Digite o nome de usuário: ")
        codigo_ativacao = input("Digite o código de ativação: ")
        print(database.ativar_conta(nome_usuario, codigo_ativacao))
    elif opcao == '4':
        #Sair
        break
    else:
        print("Opção inválida")