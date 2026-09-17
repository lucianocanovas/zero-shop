/**
 * TEST.js - Lógica interactiva para la barra de navegación (Navbar)
 */

document.addEventListener('DOMContentLoaded', () => {
    const hamburgerBtn = document.getElementById('navbarMenuToggle');
    const offcanvasEl = document.getElementById('sidebarNav');
    const userLoggedInContainer = document.getElementById('userLoggedIn');
    const userLoggedOutContainer = document.getElementById('userLoggedOut');
    const toggleUserStateBtn = document.getElementById('toggleUserStateBtn');
    const userStateStatusText = document.getElementById('userStateStatusText');

    // 1. Sincronización de la animación del botón hamburguesa con el Offcanvas de Bootstrap
    if (offcanvasEl && hamburgerBtn) {
        // Cuando el menú comienza a abrirse
        offcanvasEl.addEventListener('show.bs.offcanvas', () => {
            hamburgerBtn.classList.add('is-active');
            hamburgerBtn.setAttribute('aria-expanded', 'true');
        });

        // Cuando el menú comienza a cerrarse
        offcanvasEl.addEventListener('hide.bs.offcanvas', () => {
            hamburgerBtn.classList.remove('is-active');
            hamburgerBtn.setAttribute('aria-expanded', 'false');
        });
    }

    // 2. Control de prueba: Alternar entre usuario logueado y no logueado
    if (toggleUserStateBtn) {
        toggleUserStateBtn.addEventListener('click', () => {
            const isCurrentlyLoggedIn = !userLoggedInContainer.classList.contains('d-none');

            if (isCurrentlyLoggedIn) {
                // Cambiar a No Logueado
                userLoggedInContainer.classList.add('d-none');
                userLoggedOutContainer.classList.remove('d-none');
                userStateStatusText.textContent = 'No logueado (Visitante)';
                userStateStatusText.className = 'badge bg-secondary';
                toggleUserStateBtn.innerHTML = '<i class="bi bi-person-check me-1"></i> Simular: Usuario Logueado';
            } else {
                // Cambiar a Logueado
                userLoggedOutContainer.classList.add('d-none');
                userLoggedInContainer.classList.remove('d-none');
                userStateStatusText.textContent = 'Logueado (Con cuenta)';
                userStateStatusText.className = 'badge bg-success';
                toggleUserStateBtn.innerHTML = '<i class="bi bi-person-x me-1"></i> Simular: Usuario No Logueado';
            }
        });
    }
});
