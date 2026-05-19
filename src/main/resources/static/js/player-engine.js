const audio = document.getElementById('audioEngine');
const mainPlayBtn = document.getElementById('mainPlayBtn');
const currentTimeText = document.getElementById('currentTimeText');
const totalTimeText = document.getElementById('totalTimeText');
const canvas = document.getElementById('scCanvas');

let isPlaying = false;

if (audio && audio.getAttribute('src') !== '') {
    audio.play().then(() => {
        isPlaying = true;
        updatePlayBtnIcon();
    }).catch(() => {
        isPlaying = false;
        updatePlayBtnIcon();
    });
}

if (audio) {

    audio.addEventListener('timeupdate', () => {

        if (currentTimeText) {
            currentTimeText.innerText = formatTime(audio.currentTime);
        }

        drawWaveform();
    });

    audio.addEventListener('loadedmetadata', () => {

        if (totalTimeText) {
            totalTimeText.innerText = formatTime(audio.duration);
        }

        drawWaveform();
    });

    audio.addEventListener('ended', () => {
        nextSong();
    });
}

function togglePlayPause(event) {

    if (event) event.preventDefault();

    if (!audio || audio.getAttribute('src') === '') return;

    if (isPlaying) {
        audio.pause();
        isPlaying = false;
    } else {
        audio.play();
        isPlaying = true;
    }

    updatePlayBtnIcon();
}

function updatePlayBtnIcon() {

    if (!mainPlayBtn) return;

    const icon = mainPlayBtn.querySelector('i');

    if (isPlaying) {
        icon.className = 'fa-solid fa-circle-pause';
    } else {
        icon.className = 'fa-solid fa-circle-play';
    }
}

function nextSong(event) {

    if (event) event.preventDefault();

    const mode = getUrlParam('mode') || 'sequential';
    const keyword = getUrlParam('keyword') || '';
    const type = getUrlParam('type') || '';
    const value = getUrlParam('value') || '';

    let basePath =
        (type === 'library' || window.location.pathname.includes('/library'))
            ? '/library'
            : '/';

    sessionStorage.setItem('scroll_position', window.scrollY);

    window.location.href =
        `${basePath}?action=next&mode=${mode}&keyword=${keyword}&type=${type}&value=${value}`;
}

function prevSong(event) {

    if (event) event.preventDefault();

    if (audio) {
        audio.currentTime = 0;
    }
}

function seekAudio(event) {

    if (!audio || !canvas) return;

    const rect = canvas.getBoundingClientRect();

    const clickX = event.clientX - rect.left;

    const width = rect.width;

    const percentage = clickX / width;

    audio.currentTime = percentage * audio.duration;
}

function toggleShuffle(event) {

    if (event) event.preventDefault();

    const currentMode = getUrlParam('mode') || 'sequential';

    const newMode =
        (currentMode === 'shuffle')
            ? 'sequential'
            : 'shuffle';

    updateModeUrl(newMode);
}

function toggleRepeat(event) {

    if (event) event.preventDefault();

    const currentMode = getUrlParam('mode') || 'sequential';

    const newMode =
        (currentMode === 'repeat')
            ? 'sequential'
            : 'repeat';

    updateModeUrl(newMode);
}

function updateModeUrl(newMode) {

    const urlParams = new URLSearchParams(window.location.search);

    urlParams.set('mode', newMode);

    if (window.currentSongData) {
        urlParams.set('id', window.currentSongData.id);
    }

    window.location.search = urlParams.toString();
}

function toggleVolumePopover(event) {

    if (event) event.stopPropagation();

    const popover = document.getElementById('volumePopover');

    if (popover) {

        popover.style.display =
            (popover.style.display === 'flex')
                ? 'none'
                : 'flex';
    }
}

function changeVolume(val) {

    if (!audio) return;

    audio.volume = val;

    const icon = document.getElementById('volumeIcon');

    if (!icon) return;

    if (val == 0) {

        icon.className = 'fa-solid fa-volume-xmark';

    } else if (val < 0.5) {

        icon.className = 'fa-solid fa-volume-low';

    } else {

        icon.className = 'fa-solid fa-volume-high';
    }
}

async function toggleFavorite(event) {

    if (event) event.preventDefault();

    if (
        !window.currentSongData ||
        !window.currentSongData.id ||
        window.currentSongData.id === '0'
    ) {
        return;
    }

    const songId = window.currentSongData.id;

    try {

        const response = await fetch('/library/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: `songId=${songId}`
        });

        if (!response.ok) {
            throw new Error("Database Error");
        }

        const favBtn = document.getElementById('playerFavBtn');

        const isFav = favBtn.classList.contains('fa-solid');

        if (isFav) {

            favBtn.className = 'fa-regular fa-heart sc-fav-btn';
            favBtn.style.color = '';

        } else {

            favBtn.className = 'fa-solid fa-heart sc-fav-btn';
            favBtn.style.color = '#1db954';
        }

        if (window.location.pathname.includes('/library')) {
            window.location.reload();
        }

    } catch (err) {

        console.error(err);
        alert("Không thể lưu thư viện");
    }
}

function formatTime(secs) {

    if (isNaN(secs)) return "00:00";

    const m =
        Math.floor(secs / 60)
            .toString()
            .padStart(2, '0');

    const s =
        Math.floor(secs % 60)
            .toString()
            .padStart(2, '0');

    return `${m}:${s}`;
}

function getUrlParam(param) {

    const params = new URLSearchParams(window.location.search);

    return params.get(param);
}

function drawWaveform() {

    if (!canvas || !audio) return;

    const ctx = canvas.getContext('2d');

    const width =
        canvas.width =
            canvas.parentElement.clientWidth;

    const height =
        canvas.height =
            canvas.parentElement.clientHeight;

    ctx.clearRect(0, 0, width, height);

    const progress =
        audio.currentTime / (audio.duration || 1);

    ctx.fillStyle = '#1db954';

    ctx.fillRect(
        0,
        height / 2 - 2,
        width * progress,
        4
    );

    ctx.fillStyle = '#535353';

    ctx.fillRect(
        width * progress,
        height / 2 - 2,
        width * (1 - progress),
        4
    );
}

function navigateToLibrary(event) {

    event.preventDefault();

    window.location.href = '/library';
}

document.addEventListener('click', () => {

    const popover = document.getElementById('volumePopover');

    if (popover) {
        popover.style.display = 'none';
    }
});

document.addEventListener("DOMContentLoaded", function() {

    if (window.currentSongData && window.currentSongData.id) {

        fetch(`/library/check?songId=${window.currentSongData.id}`)
            .then(res => res.json())
            .then(isFav => {

                const favBtn =
                    document.getElementById('playerFavBtn');

                if (!favBtn) return;

                if (isFav) {

                    favBtn.className =
                        'fa-solid fa-heart sc-fav-btn';

                    favBtn.style.color = '#1db954';

                } else {

                    favBtn.className =
                        'fa-regular fa-heart sc-fav-btn';

                    favBtn.style.color = '';
                }
            });
    }

    const mode = getUrlParam('mode');

    const shuffleBtn =
        document.getElementById('shuffleBtn');

    const repeatBtn =
        document.getElementById('repeatBtn');

    const repeatBadge =
        document.getElementById('repeatBadge');

    if (mode === 'shuffle' && shuffleBtn) {
        shuffleBtn.style.color = '#1db954';
    }

    if (mode === 'repeat' && repeatBtn) {

        repeatBtn.style.color = '#1db954';

        if (repeatBadge) {
            repeatBadge.style.display = 'inline-block';
        }
    }

    const scrollPos =
        sessionStorage.getItem('scroll_position');

    if (scrollPos) {

        window.scrollTo(0, parseInt(scrollPos));

        sessionStorage.removeItem('scroll_position');
    }

    drawWaveform();
});

window.addEventListener('resize', drawWaveform);