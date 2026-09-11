const setupOverlay = ({ trigger, overlay, closeButton }) => {
    if (!trigger || !overlay) return;

    trigger.addEventListener('click', () => {
        overlay.classList.toggle('active');
    });

    if (closeButton) {
        closeButton.addEventListener('click', () => {
            overlay.classList.remove('active');
        });
    }

    overlay.addEventListener('click', (event) => {
        if (event.target === overlay) {
            overlay.classList.remove('active');
        }
    });
};

// Nav Overlay
setupOverlay({
    trigger: document.querySelector('.header-nav-button'),
    overlay: document.querySelector('.nav-overlay'),
    closeButton: document.querySelector('.nav-close-button')
});

// Not logged in Overlay
setupOverlay({
    trigger: document.querySelector('.not-logged-in'),
    overlay: document.querySelector('.auth-overlay'),
    closeButton: document.querySelector('.auth-close-button')
});

// Logged in Overlay
setupOverlay({
    trigger: document.querySelector('.logged-in'),
    overlay: document.querySelector('.account-overlay'),
    closeButton: document.querySelector('.account-close-button')
});