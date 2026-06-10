// Spring Whale Admin JS

document.addEventListener('DOMContentLoaded', function () {

    // ---- Sidebar toggle ----
    var toggleBtn = document.getElementById('sidebarToggle');
    if (toggleBtn) {
        toggleBtn.addEventListener('click', function () {
            document.body.classList.toggle('sidebar-collapsed');
        });
    }

    // ---- Sidebar group toggle ----
    var groupHeaders = document.querySelectorAll('.sidebar-menu-group-header');
    groupHeaders.forEach(function (header) {
        header.addEventListener('click', function () {
            var children = this.nextElementSibling;
            if (children && children.classList.contains('sidebar-menu-children')) {
                var isHidden = children.style.display === 'none';
                children.style.display = isHidden ? '' : 'none';
                var arrow = this.querySelector('.menu-arrow');
                if (arrow) {
                    arrow.style.transform = isHidden ? '' : 'rotate(-90deg)';
                }
            }
        });
    });

    // ---- Auto-expand active menu group ----
    var activeLink = document.querySelector('.sidebar-menu-item a.active');
    if (activeLink) {
        var childrenList = activeLink.closest('.sidebar-menu-children');
        if (childrenList) {
            childrenList.style.display = '';
            var groupHeader = childrenList.previousElementSibling;
            if (groupHeader) {
                var arrow = groupHeader.querySelector('.menu-arrow');
                if (arrow) {
                    arrow.style.transform = '';
                }
            }
        }
    }

    // ---- Confirm dialogs for delete links ----
    document.querySelectorAll('[data-confirm]').forEach(function (el) {
        el.addEventListener('click', function (e) {
            var message = this.getAttribute('data-confirm') || 'Are you sure?';
            if (!confirm(message)) {
                e.preventDefault();
            }
        });
    });

});
