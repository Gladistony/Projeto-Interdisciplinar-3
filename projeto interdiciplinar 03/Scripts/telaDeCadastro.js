document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById('FormDeCadastro');
    const nomeInput = document.getElementById('nome');
    const emailInput = document.getElementById('email');
    const senhaInput = document.getElementById('senha');
    const confirmeASenhaInput = document.getElementById('confirmeASenha');
    const errorMessages = document.querySelectorAll('.mensagemDeErro');
    const loadingSpinner = document.getElementById('loading');

    form.addEventListener('submit', function (e) {
        e.preventDefault();
        clearErrorMessages();

        const nome = nomeInput.value.trim();
        const email = emailInput.value.trim();
        const senha = senhaInput.value.trim();
        const confirmeASenha = confirmeASenhaInput.value.trim();
        nomeInput.addEventListener("input", function(){
            var valor = nome.value;
            var palavaras = valor.split(/\s+/);
            if (palavaras.length>2) {
                this.value = [palavras[0], palavras[palavras.length - 1]].join(" ");
            }
        })

        let isValid = true;

        if (!validateNome(nome)) {
            showError(nomeInput, 'O nome é obrigatório.');
            isValid = false;
        }

        if (!validateEmail(email)) {
            showError(emailInput, 'O e-mail é inválido.');
            isValid = false;
        }

        if (!validateSenha(senha)) {
            showError(senhaInput, 'A senha deve ter entre 8 e 20 caracteres.');
            isValid = false;
        }

        if (senha !== confirmeASenha) {
            showError(confirmeASenhaInput, 'As senhas não coincidem.');
            isValid = false;
        }

        if (!validateRequiredFields()) {
            isValid = false;
        }

        if (isValid) {
            loadingSpinner.style.display = 'block';
        }
    });

    function validateNome(nome) {
        return nome.length > 0;
    }

    function validateEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(String(email).toLowerCase());
    }

    function validateSenha(senha) {
        return senha.length >= 8 && senha.length <= 20;
    }

    function validateRequiredFields() {
        let valid = true;
        const requiredFields = document.querySelectorAll("input");

        requiredFields.forEach(field => {
            if (field.value.trim() === "") {
                showError(field, "Este campo é obrigatório");
                valid = false;
            }
        });

        return valid;
    }

    function clearErrorMessages() {
        errorMessages.forEach(message => {
            message.textContent = '';
            message.style.display = 'none';
        });
    }

    function showError(input, message) {
        if (input) {
            const errorSpan = input.parentElement.querySelector('.mensagemDeErro');
            if (errorSpan) {
                errorSpan.textContent = message;
                errorSpan.style.display = 'inline-block';
            }
        } else {
            const mainError = document.getElementById('mensagemDeErro');
            if (mainError) {
                mainError.textContent = message;
            }
        }
    }

});
