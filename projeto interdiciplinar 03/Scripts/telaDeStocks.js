document.addEventListener('DOMContentLoaded', () => {
    const addButton = document.querySelector('header > nav > button');
    const containerBox = document.getElementById('container-box');

    addButton.addEventListener('click', () => {
        const newBox = document.createElement('button');
        newBox.className = 'box';
        
        newBox.innerHTML = `
            <div>
                <h2>Stock Novo</h2>
                <p>Lorem ipsum dolor sit amet consectetur adipisicing elit. Alias culpa, quia iusto iure voluptates voluptas odio distinctio placeat voluptatem in sequi ex, veritatis repudiandae! Vel doloribus maiores fugiat necessitatibus exercitationem.</p>
                <img src="../IMG/Logo.webp" alt="">
            </div>
        `;

        containerBox.appendChild(newBox);
    });
});
