document.addEventListener('DOMContentLoaded', function () {
    const configMenu = document.getElementById('open1');
    const configForm = document.getElementById('nova_senha_Form');

    configMenu.addEventListener('click', function () {
        configForm.style.display = (configForm.style.display === 'block') ? 'none' : 'block';
    });
});