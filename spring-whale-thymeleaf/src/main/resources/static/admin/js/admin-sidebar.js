/* ===== Sidebar ===== */

function initSidebarToggle() {
    const toggleBtn = document.getElementById('sidebarToggle');
    if (toggleBtn) {
        toggleBtn.addEventListener('click', () => document.body.classList.toggle('sidebar-collapsed'));
    }
}

function initSidebarGroups() {
    document.querySelectorAll('.sidebar-menu-group-header').forEach(header => {
        header.addEventListener('click', () => {
            const children = header.nextElementSibling;
            if (!children?.classList.contains('sidebar-menu-children')) return;
            const isHidden = children.style.display === 'none';
            children.style.display = isHidden ? '' : 'none';
            const arrow = header.querySelector('.menu-arrow');
            if (arrow) arrow.style.transform = isHidden ? '' : 'rotate(-90deg)';
        });
    });

    const activeLink = document.querySelector('.sidebar-menu-item a.active');
    if (activeLink) {
        const childrenList = activeLink.closest('.sidebar-menu-children');
        if (childrenList) {
            childrenList.style.display = '';
            const header = childrenList.previousElementSibling;
            const arrow = header?.querySelector('.menu-arrow');
            if (arrow) arrow.style.transform = '';
        }
    }
}