import os

#Verificar se o arquivo de configuração existe
if os.path.exists('c:/config.json'):
    import json
    with open('c:/config.json') as f:
        config = json.load(f)
    print("Arquivo de configuração encontrado, usando configuração personalizada ...")
    PORT_LOCAL = config['PORT_LOCAL']
    PRODUCT_MODE = config['PRODUCT_MODE']
    HOST_URL = config['HOST_URL']
else:
    PORT_LOCAL = 8000
    PRODUCT_MODE = True
    HOST_URL = '127.0.0.1' #'44.203.201.20'

if HOST_URL == '127.0.0.1':
    linkhost = f'{HOST_URL}:{PORT_LOCAL}'
else:
    linkhost = f'{HOST_URL}'
EMAIL_PADRAO = f"Esse é seu codigo de ativacao: CODIGO <br> Você pode ativar sua conta em: http://{linkhost}/ativar/USUARIO/CODIGO"
EMAIL_RECOV = f"Esse é seu novo codigo de ativacao: CODIGO <br> Você pode ativar sua conta em: http://{linkhost}/ativar/USUARIO/CODIGO <br> Sua nova senha é: SENHA"
BANCO_DE_DADOS = "Banco de Dados/projetoEstoque.db"
