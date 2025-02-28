def generate_response(code, **kwargs):
        messages = {
            0: ("sucesso", "Operação realizada com sucesso"),
            1: ("erro", "Senha incorreta"),
            2: ("erro", "Conta bloqueada por excesso de tentativas de senha incorreta, nova ativação pode necessária"),
            3: ("erro", "Conta não está ativa"),
            4: ("erro", "Conta não encontrada"),
            5: ("erro", "Conexão com o banco de dados não foi estabelecida"),
            6: ("erro", "Conta já existe"),
            7: ("erro", "Nome de usuário deve ter entre 8 e 20 caracteres e conter apenas letras e números, sem espaços"),
            8: ("erro", "Senha deve ter entre 8 e 20 caracteres"),
            9: ("erro", "Email inválido"),
            10: ("erro", "Código de ativação incorreto"),
            11: ("erro", "Conta já está ativa"),
            12: ("erro", "Id inválido"),
            13: ("erro", "Requisição vazia"),
            14: ("erro", "Usuário já logado"),
            15: ("erro", "Usuário não logado"),
            16: ("sucesso", "Conexão estabelecida com sucesso"),
            17: ("sucesso", "Conexão autenticada com sucesso"),
            18: ("erro", "Você deve fornecer um nome completo"),
            19: ("sucesso", "Conta criada com sucesso e código de ativação enviado para o email"),
            20: ("erro", "Imagem não encontrada"),
            21: ("sucesso", "Senha recuperada com sucesso, verifique seu email"),
            22: ("erro", "Acesso administrativo necessário"),
            23: ("erro", "Erro ao carregar imagem"),
            24: ("erro", "Produto não encontrado"),
            25: ("erro", "Link da imagem inválido")
        }
        status, message = messages.get(code, ("erro", "Código desconhecido"))
        response = {"status": status, "code": code, "message": message}
        response.update(kwargs)
        return response

def generate_resposta_incluir_dict(code, dict):
    response = generate_response(code)
    response.update(dict)
    return response