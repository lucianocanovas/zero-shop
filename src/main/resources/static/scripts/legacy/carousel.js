const carousel = document.querySelector('.carousel');
const prevButton = document.querySelector('.carousel-prev');
const nextButton = document.querySelector('.carousel-next');
const track = document.querySelector('.carousel-track');
const slides = track ? track.querySelectorAll('.carousel-slide') : [];
let currentIndex = 0;
let autoplayTimer;

function getVisibleSlides() {
    if (!track || slides.length === 0) {
        return 1;
    }

    const slideWidth = slides[0].getBoundingClientRect().width;
    const gap = parseFloat(window.getComputedStyle(track).gap) || 0;

    return Math.max(1, Math.floor((track.parentElement.clientWidth + gap) / (slideWidth + gap)));
}

function updateCarousel() {
    const firstSlide = slides[0];
    const gap = parseFloat(window.getComputedStyle(track).gap) || 0;
    const slideOffset = firstSlide.getBoundingClientRect().width + gap;
    const maxIndex = Math.max(0, slides.length - getVisibleSlides());

    currentIndex = Math.min(currentIndex, maxIndex);
    track.style.transform = `translateX(-${currentIndex * slideOffset}px)`;
    prevButton.disabled = currentIndex === 0;
    nextButton.disabled = currentIndex === maxIndex;
}

function moveToSlide(step) {
    const maxIndex = Math.max(0, slides.length - getVisibleSlides());
    currentIndex = Math.max(0, Math.min(currentIndex + step, maxIndex));
    updateCarousel();
}

function startAutoplay() {
    clearInterval(autoplayTimer);
    autoplayTimer = setInterval(() => moveToSlide(1), 5000);
}

if (carousel && track && slides.length > 0) {
    updateCarousel();
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
    window.addEventListener('resize', updateCarousel);
}