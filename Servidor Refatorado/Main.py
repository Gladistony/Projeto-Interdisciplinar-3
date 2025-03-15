from Classes.GLOBAIS import BANCO_DE_DADOS, linkhost
from mangum import Mangum
import uvicorn
from fastapi import Depends, FastAPI
from Classes.BancoDeDados import BancoDeDados
from Classes.Manage_Conect import ManageConect
from Classes.Rotas.rota_inicial import router as rota_inicial
from Classes.Rotas.rota_usuario import router as rota_usuario
from Classes.Rotas.rota_admin import router as rota_admin
from Classes.Rotas.rota_camera import router as rota_camera
from Classes.GLOBAIS import linkhost, BANCO_DE_DADOS, PORT_LOCAL, PRODUCT_MODE
from fastapi.middleware.cors import CORSMiddleware


#Ativar API
app = FastAPI()
handler = Mangum(app)

# Configurar CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Permite todas as origens (altere para domínios específicos em produção)
    allow_credentials=True,
    allow_methods=["*"],  # Permite todos os métodos (GET, POST, OPTIONS, etc.)
    allow_headers=["*"],  # Permite todos os cabeçalhos
)

#Conectar ao banco de dados
Banco_de_dados = BancoDeDados()
Banco_de_dados.criar_banco(BANCO_DE_DADOS)
Manage_conect = ManageConect()
Manage_conect.criar_manage(Banco_de_dados)


#Criar Entrada da API
@app.get("/")
def read_root():
    #Manda pra os docs
    http = f"http://{linkhost}/docs"
    return {"message": "Bem vindo a API do projeto", "status": "Online", "docs": http}


app.include_router(rota_inicial)
app.include_router(rota_usuario)
app.include_router(rota_admin)
app.include_router(rota_camera)


if __name__ == "__main__":
    if PRODUCT_MODE:
        print("API em modo de produção, envio de email desativado")
    uvicorn.run(app, host="0.0.0.0", port=PORT_LOCAL)