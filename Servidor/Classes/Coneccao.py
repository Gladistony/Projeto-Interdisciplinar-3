import datetime
import time
import uuid
import threading

class conect:
    def __init__(self):
        self.id = str(uuid.uuid4())
        self.usuario = None
        self.senha = None
        self.data = None
        self.lastconection = datetime.datetime.now()
    
    def get_data(self):
        if self.data == None:
            return {"status": "Usuario nao logado"}
        jsonretorno = {}
        jsonretorno["usuario"] = self.data[1]
        jsonretorno["email"] = self.data[4]
        jsonretorno["nome_completo"] = self.data[9]
        jsonretorno["criacao"] = self.data[3]
        jsonretorno["ultimo_login"] = self.data[8]
        jsonretorno["url_foto"] = self.data[10]
        jsonretorno["status"] = "Usuario logado"
        return jsonretorno
        #sql_insert_query = """INSERT INTO contas (nome_usuario, senha, data_criacao, email, codigo_ativacao, conta_bloqueada, tentativas_senha_incorreta, data_ultimo_login, nome_completo, anotacoes, numero_telefone) VALUES (%s, %s, NOW(), %s, %s, 1, 0, NOW(), %s, %s, %s)"""


    def get_id(self):
        return self.id
    
    def att_conection(self):
        self.lastconection = datetime.datetime.now()
    
    def time_last_conection(self):
        dif = datetime.datetime.now() - self.lastconection
        return dif.total_seconds()
    
    def get_ja_logado(self):
        return self.data != None



class ManageConect:
    def __init__(self):
        self.conects = {}
        #Abrir uma Thead para limpar as coneccoes ao final de cada dia
        self.thr = threading.Thread(target=self.clear_conections)
        self.thr.start()
        
    def clear_conections(self):
        while True:
            print("Sistema para limpar conexões iniciado")
            time.sleep(86400)
            self.clear_database()
            if threading.Event.is_set():
                break
    
    def create_instance(self):
        con = conect()
        self.conects[con.get_id()] = con
        return con.get_id()
    
    def clear_database(self):
        for con in self.conects:
            if con.time_last_conection() > 31449600: #Limpa coneccoes depois de 14 dias
                self.conects.pop(con.get_id())

    def destroy_instance(self):
        #Cancelar a thead
        self.thr.set()