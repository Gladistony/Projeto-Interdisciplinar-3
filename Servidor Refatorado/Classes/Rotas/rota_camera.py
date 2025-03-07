import os
from fastapi import APIRouter, Request, Response
from fastapi.responses import JSONResponse, StreamingResponse
from Classes.BancoDeDados import BancoDeDados
from Classes.ClassesFastAPI import *
from Classes.ModuloResposta import generate_response
from Classes.Manage_Conect import ManageConect
import cv2
#import asyncio
import uvicorn
import aiohttp
import numpy as np
import time
from collections import deque
from ultralytics import YOLO
from datetime import datetime

router = APIRouter()

manage_conect = ManageConect()
database = BancoDeDados()

model = YOLO("yolov8n.pt")  # Modelo YOLO para detecção de objetos
last_frames = {}  # Armazena os últimos frames por câmera
last_frames_color = {}  # Armazena os últimos frames coloridos por câmera
recording = {}  # Controle de gravação por câmera
motion_last_detected = {}  # Última vez que houve movimento
video_writers = {}  # Gerenciadores de gravação
FRAME_INTERVAL = 0.03  # Intervalo entre frames (~30 FPS)
MOTION_THRESHOLD = 10000  # Sensibilidade da detecção de movimento
POST_MOTION_DELAY = 10  # Tempo extra de gravação após o movimento (segundos)
VIDEO_ROOT_FOLDER = "Recordings"  # Pasta raiz para salvar os vídeos
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

@router.post("/receive/{camera_id}")
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
    last_frames_color[camera_id] = frame
    
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

def autenticar_camera(id_conexao, camera_id):
    conect = manage_conect.get_conect(id_conexao)
    if conect is None:
        return generate_response(12)
    if not conect.ja_logado:
        return generate_response(15)
    response = database.verificar_posse(conect.usuario, camera_id)
    if response["code"] != 0:
        return response
    return None

@router.get("/videos/{camera_id}/{id_conexao}")
def list_videos(camera_id: str, id_conexao: str):
    response = autenticar_camera(id_conexao, camera_id)
    if response is not None:
        return response
    camera_folder = os.path.join(VIDEO_ROOT_FOLDER, camera_id)
    if not os.path.exists(camera_folder):
        return JSONResponse(content={"videos": []})
    files = os.listdir(camera_folder)
    return JSONResponse(content={"videos": files})

@router.get("/video/{camera_id}/{filename}/{id_conexao}")
def get_video(camera_id: str, filename: str, id_conexao: str):
    response = autenticar_camera(id_conexao, camera_id)
    if response is not None:
        return response
    file_path = os.path.join(VIDEO_ROOT_FOLDER, camera_id, filename)
    if os.path.exists(file_path):
        return Response(content=open(file_path, "rb").read(), media_type="video/x-msvideo")
    return Response(status_code=404, content="File not found")

@router.get("/list_all_videos/{camera_id}/{id_conexao}")
def list_all_videos(camera_id: str, id_conexao: str):
    response = autenticar_camera(id_conexao, camera_id)
    if response is not None:
        return response
    camera_folder = os.path.join(VIDEO_ROOT_FOLDER, camera_id)
    if not os.path.exists(camera_folder):
        return JSONResponse(content={"videos": []})
    files = sorted(os.listdir(camera_folder), reverse=True)  # Ordena por data decrescente
    return JSONResponse(content={"camera_id": camera_id, "videos": files})


def generate_frames(idcamera):
    while True:
        try:
            frame = last_frames_color[idcamera]
        except:
            break
        _, fra = cv2.imencode(".jpg", frame)
        yield (b"--frame\r\nContent-Type: image/jpeg\r\n\r\n" + fra.tobytes() + b"\r\n")

def classify_objects(frame):
    results = model(frame)  # Faz a detecção no frame

    for result in results:
        for box in result.boxes:
            class_id = int(box.cls[0])  # ID da classe detectada
            label = model.names[class_id]  # Nome da classe
            confidence = box.conf[0]  # Confiança da detecção

            # Obter coordenadas do bounding box
            x1, y1, x2, y2 = map(int, box.xyxy[0])

            # Desenhar o bounding box no frame
            cv2.rectangle(frame, (x1, y1), (x2, y2), (0, 255, 0), 2)
            cv2.putText(frame, f"{label} ({confidence:.2f})", (x1, y1 - 10),
                        cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 255, 0), 2)
    return frame

def generate_frames_classe(idcamera):
    while True:
        try:
            frame = last_frames_color[idcamera]
        except:
            break
        # Convertendo frame para blob (necessário para o modelo)
        frame = classify_objects(frame)
        _, fra = cv2.imencode(".jpg", frame)
        yield (b"--frame\r\nContent-Type: image/jpeg\r\n\r\n" + fra.tobytes() + b"\r\n")
   

@router.get("/ver_camera/{camera_id}/{id_conexao}")
async def ver_camera(camera_id: str, id_conexao: str):
    """Verifica o estado da câmera e transmite os frames ou retorna offline"""
    auth = autenticar_camera(id_conexao, camera_id)
    if auth is not None:
        return auth
    if camera_id not in camera_status:
        return JSONResponse(content={"message": "Câmera não encontrada"}, status_code=404)

    if camera_status[camera_id] == "online":        
        return StreamingResponse(generate_frames(camera_id), media_type="multipart/x-mixed-replace; boundary=frame")
    else:
        # Se a câmera não estiver online, retorna o status offline
        return JSONResponse(content={"message": "Câmera offline"}, status_code=404)
    
@router.get("/ver_camera_classe/{camera_id}/{id_conexao}")
async def ver_camera_classe(camera_id: str, id_conexao: str):
    """Verifica o estado da câmera e transmite os frames ou retorna offline"""
    auth = autenticar_camera(id_conexao, camera_id)
    if auth is not None:
        return auth
    if camera_id not in camera_status:
        return JSONResponse(content={"message": "Câmera não encontrada"}, status_code=404)

    if camera_status[camera_id] == "online":        
        return StreamingResponse(generate_frames_classe(camera_id), media_type="multipart/x-mixed-replace; boundary=frame")
    else:
        # Se a câmera não estiver online, retorna o status offline
        return JSONResponse(content={"message": "Câmera offline"}, status_code=404)