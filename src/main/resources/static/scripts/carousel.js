const carousel = document.querySelector('.carousel');
const prevButton = document.querySelector('.carousel-prev');
const nextButton = document.querySelector('.carousel-next');
const slides = document.querySelectorAll('.carousel-slide');
let currentIndex = 0;
let autoplayTimer;

function showSlide(index) {
    slides.forEach((slide, i) => {
        slide.classList.toggle('active', i === index);
    });
    currentIndex = index;
}

function moveToSlide(step) {
    const newIndex = (currentIndex + step + slides.length) % slides.length;
    showSlide(newIndex);
}

function startAutoplay() {
    clearInterval(autoplayTimer);
    autoplayTimer = setInterval(() => moveToSlide(1), 5000);
}

if (carousel && slides.length > 0) {
    showSlide(0);
    startAutoplay();

    prevButton.addEventListener('click', () => {
        moveToSlide(-1);
        startAutoplay();
    });

    nextButton.addEventListener('click', () => {
        moveToSlide(1);
        startAutoplay();
    });

    carousel.addEventListener('mouseenter', () => clearInterval(autoplayTimer));
    carousel.addEventListener('mouseleave', startAutoplay);
}