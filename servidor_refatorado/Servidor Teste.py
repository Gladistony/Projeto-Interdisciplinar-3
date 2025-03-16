import os
import requests
import time

# URL da API
url = "http://127.0.0.1:8000/"

# Procedimento de autenticação de id inicial
def get_initial_id():
    if os.path.isfile("id.txt"):
        with open("id.txt", "r") as arq:
            return arq.read()
    return "null"

def save_id(id):
    with open("id.txt", "w") as arq:
        arq.write(id)

# Enviar requisição e retornar a resposta em JSON
def post_request(endpoint, data):
    response = requests.post(url + endpoint, json=data)
    time.sleep(2)  # Aguarda 2 segundos (ajuste conforme necessário)
    return response.json()

# Iniciar o processo
id = get_initial_id()
data1 = {"id": id}
response1 = post_request("give/", data1)
print("Resposta 1:", response1)

code = response1["code"]
if code in [0, 16]:
    save_id(response1["id"])
elif code == 12:
    data1["id"] = "null"
    response1 = post_request("give/", data1)
    save_id(response1["id"])
    id = response1["id"]

# Verificar se o usuário está logado ou não usando o /get_dados/
data2 = {"id": id}
response2 = post_request("/get_dados/", data2)
print("Resposta 2:", response2)

data = response2
if data["code"] == 15:
    login_data = {"id": id, "usuario": "gladistony", "senha": "12345678"}
    response2 = post_request("/login/", login_data)
    print("Resposta 2:", response2)
    data = response2["data"]

print("Dados do usuário:", data)
