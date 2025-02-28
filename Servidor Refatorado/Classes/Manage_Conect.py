import datetime
import time
import uuid
import threading
from Classes import BancoDeDados
from Classes.ModuloResposta import generate_response, generate_resposta_incluir_dict


class Conect:
    def __init__(self, banco_dados):
        self.id = str(uuid.uuid4())
        self._usuario = None
        self.user_ip = []
        self.lastconection = datetime.datetime.now()
        self.banco_dados = banco_dados
    
    @property
    def data(self):
        return {
            "id": self.id,
            "usuario": self.usuario,
            "user_ip": self.user_ip,
            "lastconection": self.lastconection
        }

    def att_conection(self):
        self.lastconection = datetime.datetime.now()
    
    @property
    def time_last_conection(self):
        dif = datetime.datetime.now() - self.lastconection
        return dif.total_seconds()
    
    @property
    def ja_logado(self):
        return self.usuario is not None

    @property
    def ip(self):
        return self.user_ip
    
    def verificar_ip(self, ip):
        return ip in self.user_ip
    
    def set_ip(self, ip):
        if ip not in self.user_ip:
            self.user_ip.append(ip)
    
    @property
    def usuario(self):
        return self._usuario
    
    @usuario.setter
    def usuario(self, usuario):
        self._usuario = usuario

    def remove_ip(self, ip):
        if ip in self.user_ip:
            self.user_ip.remove(ip)
    
    def remove_usuario(self):
        self.usuario = None

    def get_data(self):
        conta = self.banco_dados.give_data(self.usuario)    
        if conta:
            return generate_resposta_incluir_dict(0, conta)
        else:
            return generate_response(15)
    
    def set_new_id(self, new_id):
        self.id = new_id
        

class ManageConect:
    _instance = None

    def __new__(cls, *args, **kwargs):
        if not cls._instance:
            cls._instance = super().__new__(cls, *args, **kwargs)
        return cls._instance


    def criar_manage(self, banco_dados):
        self.conects = {}
        self.stop_event = threading.Event()
        self.thr = threading.Thread(target=self.clear_conections)
        self.thr.start()
        self.banco_dados = banco_dados
        
    def clear_conections(self):
        while not self.stop_event.is_set():
            print("Sistema para limpar conexões iniciado")
            time.sleep(86400)
            self.clear_database()
    
    def create_instance(self):
        con = Conect(self.banco_dados)
        self.conects[con.id] = con
        print(f"Número de conexões ativas: {len(self.conects)}")
        return con
    
    def get_conect(self, con_id):
        if con_id not in self.conects:
            return None
        return self.conects.get(con_id)
    
    def clear_database(self):
        to_remove = [con_id for con_id, con in self.conects.items() if con.time_last_conection > 432000] # 5 dias em segundos
        for con_id in to_remove:
            self.conects.pop(con_id)

    def destroy_instance(self):
        self.stop_event.set()
        self.thr.join()
    
    def save_user_ip(self, con_id, ip):
        con = self.conects.get(con_id)
        if con:
            con.set_ip(ip)
            con.att_conection()
            print(f"IP {ip} adicionado para a conexão {con_id}")

    def listar_conexoes_por_ip(self, ip):
        conexoes = [con.data for con in self.conects.values() if con.verificar_ip(ip)]
        return conexoes
    
    def apagar_conexao_mais_antiga(self, ip):
        conexoes = [con for con in self.conects.values() if con.verificar_ip(ip)]
        if conexoes:
            con = sorted(conexoes, key=lambda x: x.lastconection)[0]
            self.conects.pop(con.id)
            print(f"Conexão {con.id} removida")