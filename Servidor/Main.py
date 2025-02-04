import os
from Classes.BancoDeDados import BancoDeDados
from Classes.Coneccao import ManageConect
from fastapi import FastAPI, File, UploadFile, Request, Response
from fastapi.responses import StreamingResponse, JSONResponse
from pydantic import BaseModel
from typing import Optional
from mangum import Mangum
from fastapi.responses import HTMLResponse
import cv2
import asyncio
import uvicorn
import aiohttp
import numpy as np
import time
from collections import deque
from datetime import datetime

# Dados da conexão com o banco de dados
host = '127.0.0.1'  # Substitua pelo endereço IP do WSL
linkhost = '44.203.201.20'
port = 3306
database = 'Projeto3'
user = 'servidor'
password = '159753'
# API Gemini
caminho_arquivo_api = "C:/API Gemi.txt"
EMAIL_PADRAO = f"Esse é seu codigo de ativacao: CODIGO <br> Você pode ativar sua conta em: http://{linkhost}/ativar/USUARIO/CODIGO"



# Conectar com o banco de dados
database = BancoDeDados(host, port, database, user, password, EMAIL_PADRAO)
database.create_connection()
# Criar o gerenciador de conexões
manage_conect = ManageConect()

# Ativar API
app = FastAPI()
handler = Mangum(app)

@app.get("/")
def read_root():
    html = open("Html_Template/index.html", "r")
    return HTMLResponse(content=html.read(), status_code=200)

class Item(BaseModel):
    id: str

class Cadastro(BaseModel):
    id: str
    usuario: str
    senha: str
    nome_completo: str
    email: str

class Login(BaseModel):
    id: str
    usuario: str
    senha: str

class Ativar(BaseModel):
    id: str
    usuario: str
    senha: str

class Logout(BaseModel):
    id: str

class GetDados(BaseModel):
    id: str

class SetImgUrl(BaseModel):
    id: str
    url_foto: str

class Recover(BaseModel):
    id: str
    usuario: str

class GetEmail(BaseModel):
    id: str
    usuario: str

class UploadImg(BaseModel):
    id: str
    file: UploadFile = File(...)
    destino: str

@app.post("/give/")
def create_item(item: Item):
    if item.id == "null":
        id = manage_conect.create_instance()
        resposta = {"id": id, "status": "Conexao criada"}
        return resposta
    else:
        if len(item.id) == 36:
            if item.id in manage_conect.conects:
                manage_conect.conects[item.id].att_conection()
                return {"message": "Conexao atualizada", "status": "sucesso"}
            else:
                return {"message": "Conexao nao encontrada", "code": 12, "status": "erro"}
        else:
            return {"message": "Id invalido", "code": 12, "status": "erro"}

@app.post("/cadastro/")
def cadastro(item: Cadastro):
    try:
        conec = manage_conect.conects[item.id]
    except:
        return {"message": "Conexao nao encontrada", "status": "erro", "code": 12}
    if conec.get_ja_logado():
        return {"message": "Usuario ja logado", "status": "erro", "code": 14}
    nome_usuario = item.usuario
    senha = item.senha
    email = item.email
    nome_completo = item.nome_completo
    anotacoes = ""
    numero_telefone = ""
    return database.criar_conta(nome_usuario, senha, email, nome_completo, anotacoes, numero_telefone)

@app.post("/login/")
def login(item: Login):
    try:
        conec = manage_conect.conects[item.id]
    except:
        return {"message": "Conexao nao encontrada", "status": "erro", "code": 12}
    if conec.get_ja_logado():
        return {"message": "Usuario ja logado", "status": "erro", "code": 14}
    dados = database.tentativa_login(item.usuario, item.senha)
    if dados["code"] == 0:
        conec.data = dados["data"]
        dados.pop("data")
        info = conec.get_data()
        dados.update(info)
    return dados

@app.post("/ativar/")
def ativar(item: Ativar):
    return database.ativar_conta(item.usuario, item.senha)

@app.post("/logout/")
def logout(item: Logout):
    try:
        conec = manage_conect.conects[item.id]
    except:
        return {"message": "Conexao nao encontrada", "status": "erro", "code": 12}
    if conec.get_ja_logado():
        conec.data = None
        return {"message": "Usuario deslogado", "status": "sucesso", "code": 0}
    else:
        return {"message": "Usuario nao logado", "status": "erro", "code": 15}

@app.post("/get_dados/")
def get_dados(item: GetDados):
    try:
        conec = manage_conect.conects[item.id]
    except:
        return {"message": "Conexao nao encontrada", "status": "erro", "code": 12}
    if conec.get_ja_logado():
        return conec.get_data()
    else:
        return {"message": "Usuario nao logado", "status": "erro", "code": 15}

@app.post("/set_img_url/")
def set_img_url(item: SetImgUrl):
    try:
        conec = manage_conect.conects[item.id]
    except:
        return {"message": "Conexao nao encontrada", "status": "erro", "code": 12}
    if conec.get_ja_logado():
        status = database.set_img(conec.data[0], item.url_foto)
        if status["status"] == "sucesso":
            conec.data = database.get_data(conec.data[0])
        return status
    else:
        return {"message": "Usuario nao logado", "status": "erro", "code": 15}

@app.get("/get_img_url/")
def get_img_url(link: str):
    if not os.path.isdir("imagens"):
        os.mkdir("imagens")
    if os.path.isfile(f"imagens/{link}_perfil.jpg"):
        file = open(f"imagens/{link}_perfil.jpg", "rb")
        return {"status": "sucesso", "file": file}
    else:
        return {"status": "erro", "message": "Imagem nao encontrada", "code": 18}

@app.post("/upload-image/")
async def upload_image(item: UploadImg):
    try:
        contents = await item.file.read()
    except:
        return {"message": "Erro ao ler arquivo", "status": "erro", "code": 16}    
    try:
        conec = manage_conect.conects[item.id]
    except:
        return {"message": "Conexao nao encontrada", "status": "erro", "code": 12}
    if conec.get_ja_logado():
        #Verificar se a pasta imagens existe
        if not os.path.isdir("imagens"):
            os.mkdir("imagens")
        #Verficar o tipo de envio
        if item.destino == "perfil":
            destino = f"imagens/{item.id}_perfil.jpg"
        elif item.destino == "post":
            destino = f"imagens/{item.id}_post.jpg"
        else:
            return {"message": "Destino invalido", "status": "erro", "code": 17}
        #Salvar o arquivo
        with open(destino, "wb") as file:
            file.write(contents)
        #Pegar url
        url = f"http://{linkhost}/get_img_url/?link={item.id}"
        return {"message": "Arquivo salvo", "status": "sucesso", "url": url}
    else:
        return {"message": "Usuario nao logado", "status": "erro", "code": 15}

@app.post("/recover/")
def recover(item: Recover):
    return database.recover(item.usuario)

@app.post("/get_email/")
def get_email(item: GetEmail):
    return database.get_email(item.usuario)

last_frames = {}  # Armazena os últimos frames por câmera
recording = {}  # Controle de gravação por câmera
motion_last_detected = {}  # Última vez que houve movimento
video_writers = {}  # Gerenciadores de gravação
FRAME_INTERVAL = 0.03  # Intervalo entre frames (~30 FPS)
MOTION_THRESHOLD = 10000  # Sensibilidade da detecção de movimento
POST_MOTION_DELAY = 10  # Tempo extra de gravação após o movimento (segundos)
VIDEO_ROOT_FOLDER = "recordings"  # Pasta raiz para salvar os vídeos
camera_status = {}

def detect_motion(frame, last_frame):
    if last_frame is None:
        return False
    
    diff = cv2.absdiff(cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY), last_frame)
    _, thresh = cv2.threshold(diff, 25, 255, cv2.THRESH_BINARY)
    motion = np.sum(thresh)
    return motion > MOTION_THRESHOLD

async def send_frame(url, frame_bytes):
    async with aiohttp.ClientSession() as session:
        await session.post(url, data=frame_bytes, headers={"Content-Type": "image/jpeg"})

@app.post("/receive/{camera_id}")
async def receive_stream(camera_id: str, request: Request):
    global last_frames, recording, motion_last_detected, video_writers, camera_status
    # Se a câmera ainda não foi registrada, adiciona à lista de câmeras
    if camera_id not in camera_status:
        camera_status[camera_id] = "online"  # Marca a câmera como online
    
    frame_bytes = await request.body()
    np_frame = np.frombuffer(frame_bytes, np.uint8)
    frame = cv2.imdecode(np_frame, cv2.IMREAD_COLOR)
    gray_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    
    if camera_id not in last_frames:
        last_frames[camera_id] = None
    
    motion_detected = detect_motion(frame, last_frames[camera_id])
    last_frames[camera_id] = gray_frame
    
    camera_folder = os.path.join(VIDEO_ROOT_FOLDER, camera_id)
    if not os.path.exists(camera_folder):
        os.makedirs(camera_folder)
    
    if motion_detected:
        motion_last_detected[camera_id] = time.time()
        if camera_id not in recording or not recording[camera_id]:
            filename = os.path.join(camera_folder, f"{datetime.now().strftime('%Y%m%d_%H%M%S')}.avi")
            fourcc = cv2.VideoWriter_fourcc(*"XVID")
            video_writers[camera_id] = cv2.VideoWriter(filename, fourcc, 30.0, (frame.shape[1], frame.shape[0]))
            recording[camera_id] = True
    
    if camera_id in recording and recording[camera_id]:
        video_writers[camera_id].write(frame)
        if time.time() - motion_last_detected[camera_id] > POST_MOTION_DELAY:
            video_writers[camera_id].release()
            del video_writers[camera_id]
            recording[camera_id] = False
    
    return Response(status_code=200)

@app.get("/videos/{camera_id}")
def list_videos(camera_id: str):
    camera_folder = os.path.join(VIDEO_ROOT_FOLDER, camera_id)
    if not os.path.exists(camera_folder):
        return JSONResponse(content={"videos": []})
    files = os.listdir(camera_folder)
    return JSONResponse(content={"videos": files})

@app.get("/video/{camera_id}/{filename}")
def get_video(camera_id: str, filename: str):
    file_path = os.path.join(VIDEO_ROOT_FOLDER, camera_id, filename)
    if os.path.exists(file_path):
        return Response(content=open(file_path, "rb").read(), media_type="video/x-msvideo")
    return Response(status_code=404, content="File not found")

@app.get("/list_all_videos/{camera_id}")
def list_all_videos(camera_id: str):
    camera_folder = os.path.join(VIDEO_ROOT_FOLDER, camera_id)
    if not os.path.exists(camera_folder):
        return JSONResponse(content={"videos": []})
    files = sorted(os.listdir(camera_folder), reverse=True)  # Ordena por data decrescente
    return JSONResponse(content={"camera_id": camera_id, "videos": files})

@app.get("/ver_camera/{camera_id}")
async def ver_camera(camera_id: str):
    """Verifica o estado da câmera e transmite os frames ou retorna offline"""
    if camera_id not in camera_status:
        return JSONResponse(content={"message": "Câmera não encontrada"}, status_code=404)

    if camera_status[camera_id] == "online":
        # Se a câmera estiver online, fornecemos a transmissão de vídeo
        def video_stream():
            async def generate():
                while True:
                    frame = last_frames.get(camera_id)
                    if frame is None:
                        continue  # Nenhum frame disponível ainda
                    _, buffer = cv2.imencode(".jpg", frame)  # Codifica o frame como JPEG
                    frame_bytes = buffer.tobytes()
                    yield frame_bytes
            return StreamingResponse(generate(), media_type="multipart/x-mixed-replace; boundary=frame")
        
        return video_stream()
    else:
        # Se a câmera não estiver online, retorna o status offline
        return JSONResponse(content={"message": "Câmera offline"}, status_code=404)

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)