import requests

def ler_chave_api(caminho_arquivo):
    """Lê a chave da API a partir de um arquivo de texto."""
    with open(caminho_arquivo, 'r') as file:
        return file.read().strip()

def consultar_gemini(api_key, pergunta):
    url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent"
    headers = {
        "Content-Type": "application/json"
    }
    data = {
        "contents": [
            {
                "parts": [
                    {
                        "text": pergunta
                    }
                ]
            }
        ]
    }
    params = {
        "key": api_key
    }
    response = requests.post(url, headers=headers, json=data, params=params)
    if response.status_code == 200:
        return response.json()
    else:
        return None

def salvar_resposta_em_arquivo(resposta, arquivo):
    with open(arquivo, 'w') as f:
        f.write(resposta)

def main():
    caminho_arquivo_api = "C:/API Gemi.txt"
    api_key = ler_chave_api(caminho_arquivo_api)
    pergunta = "Quais pratos eu posso fazer com os seguintes ingredientes: batata, cenoura, cebola, alho?"
    #pergunta = "Qual a receita de Sopa de Batata e Cenoura?"
    resposta = consultar_gemini(api_key, pergunta)
    if resposta:
        resposta_texto = resposta['candidates'][0]['content']['parts'][0]['text']
        #salvar_resposta_em_arquivo(resposta_texto, 'resposta_gemini.txt')
        print(resposta_texto)
    else:
        print("Erro ao consultar a API do Gemini")

if __name__ == "__main__":
    main()
