from fastapi import APIRouter, Depends, Request
from Classes.BancoDeDados import BancoDeDados
from Classes.ClassesFastAPI import *
from Classes.Manage_Conect import ManageConect
from Classes.ModuloResposta import generate_response

router = APIRouter()
manage_conect = ManageConect()
database = BancoDeDados()

@router.post("/get_all_user/")
def get_all_user(item: GetAllUser):
    conect = manage_conect.get_conect(item.id)
    if conect is None:
        return generate_response(12)
    if not conect.ja_logado:
        return generate_response(15)
    usuario = conect.usuario
    return database.get_all_user(usuario)

@router.post("/get_user_data/")
def get_user_data(item: GetUserData):
    conect = manage_conect.get_conect(item.id)
    if conect is None:
        return generate_response(12)
    if not conect.ja_logado:
        return generate_response(15)
    usuario = conect.usuario
    return database.get_user_data(item.usuario, usuario)

@router.post("/set_user_data/")
def set_user_data(item: SetUserData):
    conect = manage_conect.get_conect(item.id)
    if conect is None:
        return generate_response(12)
    if not conect.ja_logado:
        return generate_response(15)
    usuario = conect.usuario
    return database.set_user_data(item.usuario, item.senha, usuario)

@router.post("/delete_user/")
def delete_user(item: GetUserData):
    conect = manage_conect.get_conect(item.id)
    if conect is None:
        return generate_response(12)
    if not conect.ja_logado:
        return generate_response(15)
    usuario = conect.usuario
    return database.delete_user(item.usuario, usuario)

@router.post("/get_user_estoque/")
def get_user_estoque(item: GetUserData):
    conect = manage_conect.get_conect(item.id)
    if conect is None:
        return generate_response(12)
    if not conect.ja_logado:
        return generate_response(15)
    usuario = item.usuario
    admin = conect.usuario
    return database.get_user_estoque(usuario, admin)
