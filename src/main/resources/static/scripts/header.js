/**
 * scripts/header.js - Interactividad del Header y Menú Hamburguesa
 */
document.addEventListener('DOMContentLoaded', () => {
    const hamburgerBtn = document.getElementById('navbarMenuToggle') || document.querySelector('.header-navbar-button');
    const offcanvasEl = document.getElementById('sidebarNav');

    if (offcanvasEl && hamburgerBtn) {
        offcanvasEl.addEventListener('show.bs.offcanvas', () => {
            hamburgerBtn.classList.add('is-active');
            hamburgerBtn.setAttribute('aria-expanded', 'true');
        });

        offcanvasEl.addEventListener('hide.bs.offcanvas', () => {
            hamburgerBtn.classList.remove('is-active');
            hamburgerBtn.setAttribute('aria-expanded', 'false');
        });
    }
});

