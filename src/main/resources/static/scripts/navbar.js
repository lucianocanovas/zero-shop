const navButton = document.querySelector('.nav-button');
const closeButton = document.querySelector('.close-button');
const navOverlay = document.querySelector('.nav-overlay');

navButton.addEventListener('click', () => {
    navOverlay.classList.toggle('active');
});

closeButton.addEventListener('click', () => {
    navOverlay.classList.remove('active');
});