document.addEventListener('DOMContentLoaded', () => {
    const addButton = document.querySelector('header > nav > button');
    const containerBox = document.getElementById('container-box');

    addButton.addEventListener('click', () => {
        const newBox = document.createElement('button');
        newBox.className = 'box';
        
        newBox.innerHTML = `
            <div class="clickable-div">
                <h2>Stock Novo</h2>
                <p>Lorem ipsum dolor sit amet consectetur adipisicing elit. Alias culpa, quia iusto iure voluptates voluptas odio distinctio placeat voluptatem in sequi ex, veritatis repudiandae! Vel doloribus maiores fugiat necessitatibus exercitationem.</p>
                <img src="../IMG/Logo.webp" alt="">
            </div>
        `;

        newBox.addEventListener('click', () => {
            window.location.href = 'tela-parcial-camera.html';
        });

        containerBox.appendChild(newBox);
    });
});

// parte da câmera 

let currentStream = null;

function getDevices() {
    navigator.mediaDevices.enumerateDevices()
    .then(function(devices) {
        const videoSelect = document.getElementById('video-source');
        videoSelect.innerHTML = '';
        devices.forEach(function(device) {
            if (device.kind === 'videoinput') {
                const option = document.createElement('option');
                option.value = device.deviceId;
                option.text = device.label || `Camera ${videoSelect.length + 1}`;
                videoSelect.appendChild(option);
            }
        });
    })
    .catch(function(err) {
        console.log("Erro ao listar dispositivos: " + err);
    });
}

function startVideo(deviceId) {
    if (currentStream) {
        currentStream.getTracks().forEach(track => track.stop());
    }

    const constraints = {
        video: {
            deviceId: deviceId ? { exact: deviceId } : undefined
        }
    };

    navigator.mediaDevices.getUserMedia(constraints)
    .then(function(stream) {
        currentStream = stream;
        const video = document.getElementById('video');
        video.srcObject = stream;
        video.play();
    })
    .catch(function(err) {
        console.log("Erro ao acessar a câmera: " + err);
    });
}

document.getElementById('btn-produtos').addEventListener('click', function() {
    const videoSelect = document.getElementById('video-source');
    const deviceId = videoSelect.value;
    startVideo(deviceId);
});

document.getElementById('stop-video').addEventListener('click', function() {
    if (currentStream) {
        currentStream.getTracks().forEach(track => track.stop());
        currentStream = null;
    }
});

getDevices();
