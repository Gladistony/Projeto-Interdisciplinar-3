#Configuração do cliente de câmera
DEFAULT_CAMERA_ID = "6e48a36d-8e28-4560-a524-d5fdb89e2f68"



# NÃO ALTERAR ABAIXO


#------------------------------------------------------------
import cv2
import asyncio
import aiohttp
import numpy as np

#Verificar se o arquivo de configuração existe
import os
if os.path.exists('c:/config.json'):
    import json
    with open('c:/config.json') as f:
        config = json.load(f)
    PORT_LOCAL = config['PORT_LOCAL']
    PRODUCT_MODE = config['PRODUCT_MODE']
    HOST_URL = config['HOST_URL']
    START_QUESTION_CAMERA_ID = config['START_QUESTION_CAMERA_ID']
    START_QUESTION_CAMERA = config['START_QUESTION_CAMERA']
    DEFAULT_CAMERA = config['DEFAULT_CAMERA']
else:
    PORT_LOCAL = 8000
    PRODUCT_MODE = True
    HOST_URL = '127.0.0.1' #'44.203.201.20'
    START_QUESTION_CAMERA_ID = False
    START_QUESTION_CAMERA = False
    DEFAULT_CAMERA = 0



if START_QUESTION_CAMERA_ID:
    CAMERA_ID = input("Digite o ID da câmera: ")
else:
    CAMERA_ID = DEFAULT_CAMERA_ID
    

def listar_webcams():
    index = 0
    webcams = []
    while True:
        cap = cv2.VideoCapture(index)
        if not cap.read()[0]:
            break
        else:
            webcams.append(index)
        cap.release()
        index += 1
    return webcams

if START_QUESTION_CAMERA_ID:
    webcams_disponiveis = listar_webcams()
    print("Webcams disponíveis:", webcams_disponiveis)
    escolha = int(input("Escolha a webcam: "))
    if escolha not in webcams_disponiveis:
        print("Webcam inválida")
        exit()
else:
    escolha = DEFAULT_CAMERA

def capture_and_send():
    cap = cv2.VideoCapture(escolha)
    #url = f"http://44.203.201.20/receive/{CAMERA_ID}"
    #url = f"http://127.0.0.1:3000/receive/{CAMERA_ID}"
    url = f"http://{HOST_URL}:{PORT_LOCAL}/receive/{CAMERA_ID}"
    print("Sending frames to", url)
    while True:
        ret, frame = cap.read()
        if not ret:
            break
        _, buffer = cv2.imencode(".jpg", frame)
        frame_bytes = buffer.tobytes()
        
        asyncio.run(send_frame(url, frame_bytes))


async def send_frame(url, frame_bytes):
    async with aiohttp.ClientSession() as session:
        await session.post(url, data=frame_bytes, headers={"Content-Type": "image/jpeg"})

capture_and_send()