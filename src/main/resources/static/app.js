document.addEventListener('DOMContentLoaded', () => {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('overlay');
    const toggle  = document.getElementById('menuToggle');

    // Mobile menu
    const closeMenu = () => { sidebar.classList.remove('open'); overlay.classList.remove('show'); };
    toggle?.addEventListener('click', () => {
        sidebar.classList.toggle('open');
        overlay.classList.toggle('show');
    });
    overlay?.addEventListener('click', closeMenu);

    // Close alerts (and auto-hide after 5 seconds)
    document.querySelectorAll('.alert').forEach(alert => {
        alert.querySelector('.alert-close')?.addEventListener('click', () => alert.remove());
        setTimeout(() => alert.remove(), 5000);
    });

    // Confirm before delete: <form data-confirm="Delete this item?">
    document.querySelectorAll('form[data-confirm]').forEach(form => {
        form.addEventListener('submit', e => {
            if (!confirm(form.dataset.confirm)) e.preventDefault();
        });
    });
});