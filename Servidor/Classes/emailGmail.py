
from email.message import EmailMessage
import smtplib
from datetime import datetime
import threading

#Carregar credenciais do arquivo login_gmail.txt
caminho_arquivo = "C:/login_gmail.txt"
with open(caminho_arquivo) as arquivo:
    LOGIN = arquivo.readline().strip()
    SENHA = arquivo.readline().strip()
    print(f"Email de envio: {LOGIN}")


def enviarEmail(msg,assunto, destino):
    email = EmailMessage()
    email["From"] = LOGIN
    email["To"] = destino
    email["Subject"] = assunto
    email.set_content(msg)
    smtp = smtplib.SMTP_SSL("smtp.gmail.com",465)
    try:
        smtp.login(LOGIN, SENHA)
        smtp.sendmail(LOGIN, destino, email.as_string())
        #print(f"\nEnvio de email exitoso para {destino} às {datetime.now()}")
    except:
        print(f"\nErro ao enviar email para {destino} às {datetime.now()}")
    smtp.quit()
    
def codigoAtivacao(codigo, email):
    #conteudo = "Seja muito bem vindo ao Dragon World \n\n Seu codigo de ativação é: " + codigo + "\n\n Obrigado por se cadastrar em nosso jogo, esperamos que se divirta muito!"
    thr = threading.Thread(target=enviarEmail, args=(codigo, "Codigo de Ativação",email))
    thr.start()
    #enviarEmail(conteudo, "Codigo de Ativação",email)

if __name__ == "__main__":
    enviarEmail("Esse aqui é seu codigo:", "Codigo de Ativação","bistone3000@hotmail.com")