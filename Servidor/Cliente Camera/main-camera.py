import cv2
import asyncio
import aiohttp
import numpy as np

CAMERA_ID = "c2039538-91c2-4cfe-9afd-3703296fdcb4"

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

webcams_disponiveis = listar_webcams()
print("Webcams disponíveis:", webcams_disponiveis)
escolha = int(input("Escolha a webcam: "))
if escolha not in webcams_disponiveis:
    print("Webcam inválida")
    exit()


def capture_and_send():
    cap = cv2.VideoCapture(escolha)
    #url = f"http://44.203.201.20/receive/{CAMERA_ID}"
    url = f"http://127.0.0.1:7300/receive/{CAMERA_ID}"
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