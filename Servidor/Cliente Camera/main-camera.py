import cv2
import asyncio
import aiohttp
import numpy as np

CAMERA_ID = "123456789"


def capture_and_send():
    cap = cv2.VideoCapture(0)  # Captura da webcam
    url = f"http://127.0.0.1:8000/receive/{CAMERA_ID}"
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