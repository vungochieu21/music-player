let isPlaying = false;
let isShuffle = false;
let repeatState = 0;
let savedVolume = 1;

const audioEngine = document.getElementById('audioEngine');
const canvas = document.getElementById('scCanvas');
const ctx = canvas.getContext('2d');

let audioCtx = null;
let analyser = null;
let source = null;
let dataArray = [];

function initAudioContext() {
    if (audioCtx) return;
    audioCtx = new (window.AudioContext || window.webkitAudioContext)();
    analyser = audioCtx.createAnalyser();
    analyser.fftSize = 256;
    source = audioCtx.createMediaElementSource(audioEngine);
    source.connect(analyser);
    analyser.connect(audioCtx.destination);
    dataArray = new Uint8Array(analyser.frequencyBinCount);
    drawWaveform();
}

function toggleVolumePopover(event) {
    event.stopPropagation();
    const popover = document.getElementById('volumePopover');
    popover.classList.toggle('show');
}

document.addEventListener('click', function(e) {
    const popover = document.getElementById('volumePopover');
    const wrapper = document.querySelector('.sc-volume-wrapper-vertical');
    if (popover && !wrapper.contains(e.target)) {
        popover.classList.remove('show');
    }
});

function changeVolume(val) {
    audioEngine.volume = val;
    localStorage.setItem('player_volume', val);
    const icon = document.getElementById('volumeIcon');
    if (val == 0) {
        icon.className = "fa-solid fa-volume-xmark sc-volume-icon-btn";
    } else if (val < 0.5) {
        icon.className = "fa-solid fa-volume-low sc-volume-icon-btn";
    } else {
        icon.className = "fa-solid fa-volume-high sc-volume-icon-btn";
    }
}

function toggleMute(event) {
    if(event) event.stopPropagation();
    const slider = document.getElementById('volumeSlider');
    if (audioEngine.volume > 0) {
        savedVolume = audioEngine.volume;
        slider.value = 0;
        changeVolume(0);
    } else {
        slider.value = savedVolume;
        changeVolume(savedVolume);
    }
}

function formatTime(seconds) {
    if (isNaN(seconds)) return "00:00";
    const min = Math.floor(seconds / 60);
    const sec = Math.floor(seconds % 60);
    return (min < 10 ? '0' : '') + min + ':' + (sec < 10 ? '0' : '') + sec;
}

audioEngine.ontimeupdate = function() {
    document.getElementById('currentTimeText').innerText = formatTime(audioEngine.currentTime);
};

function nextSong(event) {
    if(event) event.preventDefault();
    const tracks = Array.from(document.querySelectorAll('.hybrid-track'));
    if (tracks.length === 0) return;

    let currentTrackIdx = tracks.findIndex(t => t.classList.contains('is-playing'));
    let nextTrackIdx = 0; 

    if (isShuffle) {
        nextTrackIdx = Math.floor(Math.random() * tracks.length);
    } else if (currentTrackIdx !== -1 && currentTrackIdx < tracks.length - 1) {
        nextTrackIdx = currentTrackIdx + 1;
    } else if (repeatState === 0 && currentTrackIdx === tracks.length - 1) {
        audioEngine.pause();
        document.querySelector('#mainPlayBtn i').className = 'fa-solid fa-circle-play';
        isPlaying = false;
        audioEngine.currentTime = 0;
        return;
    }

    window.location.href = tracks[nextTrackIdx].getAttribute('href');
}

function prevSong(event) {
    if(event) event.preventDefault();
    const tracks = Array.from(document.querySelectorAll('.hybrid-track'));
    if (tracks.length === 0) return;

    let currentTrackIdx = tracks.findIndex(t => t.classList.contains('is-playing'));
    let prevTrackIdx = tracks.length - 1; 

    if (isShuffle) {
        prevTrackIdx = Math.floor(Math.random() * tracks.length);
    } else if (currentTrackIdx > 0) {
        prevTrackIdx = currentTrackIdx - 1;
    }

    window.location.href = tracks[prevTrackIdx].getAttribute('href');
}

audioEngine.onended = function() {
    if (repeatState === 2) {
        audioEngine.currentTime = 0;
        triggerPlay();
    } else {
        nextSong();
    }
};

function triggerPlay() {
    if (audioCtx && audioCtx.state === 'suspended') {
        audioCtx.resume();
    }
    audioEngine.play()
        .then(() => {
            document.querySelector('#mainPlayBtn i').className = 'fa-solid fa-circle-pause';
            isPlaying = true;
        })
        .catch(err => { console.log(err); });
}

function togglePlayPause(event) {
    if(event) event.preventDefault();
    initAudioContext();
    
    if (!audioEngine.src || audioEngine.src.endsWith('/audio/null.mp3') || audioEngine.src.endsWith('/audio/.mp3')) {
        return;
    }

    if (!isPlaying) {
        triggerPlay();
    } else {
        audioEngine.pause();
        document.querySelector('#mainPlayBtn i').className = 'fa-solid fa-circle-play';
        isPlaying = false;
    }
}

function seekAudio(event) {
    if (!audioEngine.duration) return;
    const rect = document.getElementById('canvasWrapper').getBoundingClientRect();
    const clickX = event.clientX - rect.left;
    const width = rect.width;
    const percentage = clickX / width;
    audioEngine.currentTime = percentage * audioEngine.duration;
}

function drawWaveform() {
    requestAnimationFrame(drawWaveform);
    if(!canvas.parentElement) return;
    const displayWidth = canvas.parentElement.clientWidth;
    const displayHeight = canvas.parentElement.clientHeight;
    if (canvas.width !== displayWidth || canvas.height !== displayHeight) {
        canvas.width = displayWidth;
        canvas.height = displayHeight;
    }

    if (isPlaying && analyser) {
        analyser.getByteFrequencyData(dataArray);
    } else {
        dataArray.fill(0);
    }

    ctx.clearRect(0, 0, canvas.width, canvas.height);
    const barWidth = 3;
    const barGap = 2;
    const totalBars = Math.floor(canvas.width / (barWidth + barGap));
    const progressPercent = audioEngine.duration ? (audioEngine.currentTime / audioEngine.duration) : 0;
    const activeBarLimit = Math.floor(totalBars * progressPercent);

    for (let i = 0; i < totalBars; i++) {
        let dataIndex = Math.floor((i / totalBars) * dataArray.length);
        let audioValue = dataArray[dataIndex];
        let baseHeight = 8 + Math.sin(i * 0.15) * 12 + Math.cos(i * 0.05) * 6;
        let dynamicHeight = isPlaying ? (audioValue / 255) * (canvas.height - 15) : 0;
        let finalHeight = Math.max(baseHeight, dynamicHeight);

        let x = i * (barWidth + barGap);
        let y = (canvas.height - finalHeight) / 2;

        if (i <= activeBarLimit) {
            ctx.fillStyle = '#ff5500';
        } else {
            ctx.fillStyle = '#444444';
        }
        ctx.beginPath();
        ctx.roundRect(x, y, barWidth, finalHeight, 2);
        ctx.fill();
    }
}

function toggleShuffle(event) { 
    event.preventDefault(); 
    isShuffle = !isShuffle;
    document.getElementById('shuffleBtn').classList.toggle('active', isShuffle); 
}

function toggleRepeat(event) {
    event.preventDefault(); 
    const repeatBtn = document.getElementById('repeatBtn'); 
    const repeatBadge = document.getElementById('repeatBadge');
    repeatState = (repeatState + 1) % 3;
    if (repeatState === 0) { 
        repeatBtn.classList.remove('active'); 
        repeatBadge.style.display = 'none'; 
    } else if (repeatState === 1) { 
        repeatBtn.classList.add('active'); 
        repeatBadge.style.display = 'none'; 
    } else if (repeatState === 2) { 
        repeatBtn.classList.add('active'); 
        repeatBadge.style.display = 'flex'; 
    }
}

document.addEventListener('click', function() {
    initAudioContext();
}, { once: true });

window.addEventListener('DOMContentLoaded', () => {
    const localVol = localStorage.getItem('player_volume');
    if (localVol !== null) {
        const volNum = parseFloat(localVol);
        audioEngine.volume = volNum;
        document.getElementById('volumeSlider').value = volNum;
        changeVolume(volNum);
    }
    
    if (audioEngine.src && !audioEngine.src.endsWith('/') && !audioEngine.src.endsWith('/null.mp3')) {
        setTimeout(() => {
            initAudioContext();
            triggerPlay();
        }, 300);
    }
});