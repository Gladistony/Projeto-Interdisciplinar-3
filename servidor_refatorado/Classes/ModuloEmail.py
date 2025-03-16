from Classes.emailGmail import codigoAtivacao 
from Classes.GLOBAIS import EMAIL_PADRAO as msgpadrao
from Classes.GLOBAIS import EMAIL_RECOV as msgrecov


def enviar_email_ativacao(nome_usuario, codigo_ativacao, email):
        string_cod = msgpadrao.replace("CODIGO", codigo_ativacao)
        string_final = string_cod.replace("USUARIO", nome_usuario)
        codigoAtivacao(string_final, email)

def enviar_email_recuperacao(usuario, codigo_ativacao, email, senha):
        string_cod = msgrecov.replace("CODIGO", codigo_ativacao)
        string_final = string_cod.replace("USUARIO", usuario)
        string_final = string_final.replace("SENHA", senha)
        codigoAtivacao(string_final, email)