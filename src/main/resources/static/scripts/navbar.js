const navButton = document.querySelector('.header-nav-button');
const closeButton = document.querySelector('.nav-close-button');
const navOverlay = document.querySelector('.nav-overlay');

navButton.addEventListener("click", () => {
    navOverlay.classList.toggle("active");
});

closeButton.addEventListener("click", () => {
    navOverlay.classList.remove("active");
});

navOverlay.addEventListener("click", () => {
    navOverlay.classList.remove("active");
});