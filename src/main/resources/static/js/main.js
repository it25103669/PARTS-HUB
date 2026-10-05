/**
 * PartsHub Pro – main.js
 * Shared UI behaviour: sidebar toggle, profile dropdown, active menu.
 */

document.addEventListener('DOMContentLoaded', function () {

    // ── Sidebar mobile toggle ──────────────────────────────
    const menuToggle  = document.getElementById('menuToggle');
    const sidebar     = document.getElementById('phSidebar');
    const overlay     = document.getElementById('sidebarOverlay');

    if (menuToggle && sidebar) {
        menuToggle.addEventListener('click', function () {
            sidebar.classList.toggle('open');
            if (overlay) overlay.classList.toggle('open');
        });
    }
    if (overlay) {
        overlay.addEventListener('click', function () {
            sidebar.classList.remove('open');
            overlay.classList.remove('open');
        });
    }

    // ── Profile dropdown ───────────────────────────────────
    const profileBtn = document.getElementById('profileDropdownBtn');
    const dropdown   = document.getElementById('profileDropdown');

    if (profileBtn && dropdown) {
        profileBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            dropdown.classList.toggle('open');
        });
        document.addEventListener('click', function () {
            dropdown.classList.remove('open');
        });
    }

    // ── Notification panel ─────────────────────────────────
    const notifBtn = document.getElementById('notifBtn');
    if (notifBtn) {
        notifBtn.addEventListener('click', function (e) {
            e.preventDefault();
            if (window.PartsHub && typeof window.PartsHub.toggleNotifications === 'function') {
                window.PartsHub.toggleNotifications();
            }
        });
    }

    // ── Auto-close alert messages after 4 s ───────────────
    const alerts = document.querySelectorAll('.ph-alert-auto');
    alerts.forEach(function (el) {
        setTimeout(function () {
            el.style.opacity = '0';
            el.style.transition = 'opacity 0.4s';
            setTimeout(function () { el.remove(); }, 400);
        }, 4000);
    });
});
