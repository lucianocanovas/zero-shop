const toast = document.querySelector('.toast');
const toastMessage = document.querySelector('.toast-message');
const toastCloseButton = document.querySelector('.toast-close-button');

const showToast = (message, duration = 5000) => {
    if (!toast || !toastMessage) return;

    toastMessage.textContent = message;
    toast.classList.add('show');

    setTimeout(() => {
        toast.classList.remove('show');
    }, duration);
};

if (toastCloseButton && toast) {
    toastCloseButton.addEventListener('click', () => {
        toast.classList.remove('show');
    });
}

const testToastButton = document.querySelector('.test-toast');

if (testToastButton) {
    testToastButton.addEventListener('click', () => {
        showToast('¡Este es un mensaje de prueba!');
    });
}