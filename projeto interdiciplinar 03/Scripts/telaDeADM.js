
document.addEventListener('DOMContentLoaded', function() {
    const toggleButtons = document.querySelectorAll('.toggle-details');

    toggleButtons.forEach(button => {
        button.addEventListener('click', function() {
            const detalhesRow = this.closest('tr').nextElementSibling;
            if (detalhesRow.classList.contains('detalhes')) {
                detalhesRow.style.display = detalhesRow.style.display === 'table-row' ? 'none' : 'table-row';
                this.textContent = this.textContent === 'Mostrar Detalhes' ? 'Esconder Detalhes' : 'Mostrar Detalhes';
            }
        });
    });
});


    document.addEventListener('DOMContentLoaded', function() {
        const editButtons = document.querySelectorAll('.edit');
        const deleteButtons = document.querySelectorAll('.delete');

        editButtons.forEach(button => {
            button.addEventListener('click', function() {
                // a lógica de edição
                alert('Editar usuário');
            });
        });

        deleteButtons.forEach(button => {
            button.addEventListener('click', function() {
                // a lógica de exclusão
                alert('Apagar usuário');
            });
        });
    });