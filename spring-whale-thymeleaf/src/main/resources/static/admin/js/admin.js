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

    // ---- Dict Modal ----
    initDictModal();

});

// ---- Dict Modal System ----
function initDictModal() {
    var overlays = document.querySelectorAll('.dict-modal-overlay');
    overlays.forEach(function (overlay) {
        // Close on overlay click
        overlay.addEventListener('click', function (e) {
            if (e.target === overlay) {
                closeDictModal(overlay);
            }
        });

        // Close button
        var closeBtn = overlay.querySelector('.modal-close');
        if (closeBtn) {
            closeBtn.addEventListener('click', function () {
                closeDictModal(overlay);
            });
        }

        // Cancel button
        var cancelBtn = overlay.querySelector('.modal-cancel');
        if (cancelBtn) {
            cancelBtn.addEventListener('click', function () {
                closeDictModal(overlay);
            });
        }

        // Submit button
        var form = overlay.querySelector('.dict-modal-form');
        var submitBtn = overlay.querySelector('.modal-submit');
        if (form && submitBtn) {
            submitBtn.addEventListener('click', function () {
                submitDictForm(form, overlay);
            });
        }
    });

    // Open buttons
    document.querySelectorAll('[data-modal]').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var modalId = this.getAttribute('data-modal');
            var overlay = document.getElementById(modalId);
            if (overlay) {
                openDictModal(overlay, this);
            }
        });
    });
}

function openDictModal(overlay, triggerBtn) {
    var form = overlay.querySelector('.dict-modal-form');
    if (!form) return;

    var isEdit = triggerBtn.hasAttribute('data-edit-id');
    var titleEl = overlay.querySelector('.modal-title');

    // Load groups if select exists
    loadGroupOptions(form);

    if (isEdit) {
        var id = triggerBtn.getAttribute('data-edit-id');
        var apiUrl = form.getAttribute('data-api-base');
        form.setAttribute('data-edit-id', id);

        if (titleEl) titleEl.textContent = 'Edit';
        form.action = apiUrl + '/' + id;
        form.method = 'put';

        // Fetch existing data and fill form
        fetch(apiUrl + '/' + id)
            .then(function (r) { return r.json(); })
            .then(function (data) {
                fillFormFields(form, data);
            });
    } else {
        form.removeAttribute('data-edit-id');
        if (titleEl) titleEl.textContent = 'Create';
        form.action = form.getAttribute('data-api-base');
        form.method = 'post';
        form.reset();
    }

    overlay.classList.remove('hidden');
}

function closeDictModal(overlay) {
    overlay.classList.add('hidden');
    var form = overlay.querySelector('.dict-modal-form');
    if (form) {
        form.reset();
        form.removeAttribute('data-edit-id');
        var errors = form.querySelectorAll('.form-error');
        errors.forEach(function (e) { e.textContent = ''; });
    }
}

function loadGroupOptions(form) {
    var groupSelects = form.querySelectorAll('select[name="groupId"]');
    groupSelects.forEach(function (select) {
        // Already loaded
        if (select.options.length > 1) return;
        fetch('/api/rbac/groups?page=0&size=1000')
            .then(function (r) { return r.json(); })
            .then(function (page) {
                var groups = page.content || page;
                groups.forEach(function (g) {
                    var option = document.createElement('option');
                    option.value = g.id;
                    option.textContent = g.name;
                    select.appendChild(option);
                });
            })
            .catch(function () {
                // Groups API not available, leave default options
            });
    });
}

function fillFormFields(form, data) {
    var fields = form.querySelectorAll('[name]');
    fields.forEach(function (field) {
        var name = field.name;
        if (data.hasOwnProperty(name)) {
            var value = data[name];
            if (field.tagName === 'SELECT') {
                // Wait for options to load, then set value
                var setVal = function () {
                    if (value !== null && value !== undefined) {
                        field.value = value;
                    }
                };
                if (field.options.length > 1) {
                    setVal();
                } else {
                    // Retry after options load
                    var retries = 0;
                    var timer = setInterval(function () {
                        retries++;
                        if (field.options.length > 1 || retries > 20) {
                            clearInterval(timer);
                            setVal();
                        }
                    }, 100);
                }
            } else if (Array.isArray(value)) {
                field.value = value.join(', ');
            } else {
                field.value = value !== null && value !== undefined ? value : '';
            }
        }
    });
}

function submitDictForm(form, overlay) {
    var id = form.getAttribute('data-edit-id');
    var method = id ? 'PUT' : 'POST';
    var url = form.getAttribute('action');
    var submitBtn = overlay.querySelector('.modal-submit');

    // Build JSON body
    var body = {};
    var fields = form.querySelectorAll('[name]');
    fields.forEach(function (field) {
        var value = field.value;
        // Skip empty password in edit mode
        if (field.name === 'password' && method === 'PUT' && !value) return;
        // Convert number fields
        if ((field.name === 'status' || field.name === 'groupId' || field.name === 'sort'
                || field.name === 'parentId' || field.name === 'type' || field.name === 'visible') && value !== '') {
            body[field.name] = parseInt(value, 10);
        } else {
            body[field.name] = value;
        }
    });

    // Clear errors
    var errors = form.querySelectorAll('.form-error');
    errors.forEach(function (e) { e.textContent = ''; });

    // Disable button
    if (submitBtn) submitBtn.disabled = true;

    fetch(url, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    })
    .then(function (r) {
        if (!r.ok) {
            return r.json().then(function (err) {
                throw err;
            });
        }
        return r.json();
    })
    .then(function () {
        showToast(method === 'PUT' ? 'Updated successfully' : 'Created successfully', 'success');
        closeDictModal(overlay);
        // Reload the page to show new data
        setTimeout(function () { location.reload(); }, 600);
    })
    .catch(function (err) {
        if (submitBtn) submitBtn.disabled = false;
        if (err && err.errors) {
            for (var key in err.errors) {
                var errorEl = form.querySelector('.form-error[data-field="' + key + '"]');
                if (errorEl) {
                    errorEl.textContent = err.errors[key];
                }
            }
        } else {
            showToast('Operation failed. Please try again.', 'error');
        }
    });
}

function showToast(message, type) {
    var toast = document.getElementById('dict-toast');
    if (!toast) {
        toast = document.createElement('div');
        toast.id = 'dict-toast';
        toast.className = 'modal-toast';
        document.body.appendChild(toast);
    }
    toast.className = 'modal-toast ' + type;
    toast.textContent = message;
    toast.classList.add('show');
    clearTimeout(toast._timer);
    toast._timer = setTimeout(function () {
        toast.classList.remove('show');
    }, 2500);
}
