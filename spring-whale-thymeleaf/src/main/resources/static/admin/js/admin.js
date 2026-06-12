// Spring Whale Admin JS

document.addEventListener('DOMContentLoaded', () => {
    initSidebarToggle();
    initSidebarGroups();
    initConfirmDialogs();
    initModalSystem();
});

/* ===== Sidebar ===== */

function initSidebarToggle() {
    const toggleBtn = document.getElementById('sidebarToggle');
    if (toggleBtn) {
        toggleBtn.addEventListener('click', () => document.body.classList.toggle('sidebar-collapsed'));
    }
}

function initSidebarGroups() {
    // Toggle group expand/collapse
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

    // Auto-expand active group
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

function initConfirmDialogs() {
    document.querySelectorAll('[data-confirm]').forEach(el => {
        el.addEventListener('click', function (e) {
            const message = this.getAttribute('data-confirm') || 'Are you sure?';
            if (!confirm(message)) e.preventDefault();
        });
    });
}

/* ===== Modal CRUD ===== */

function initModalSystem() {
    document.querySelectorAll('[data-modal]').forEach(btn => {
        btn.addEventListener('click', () => {
            const modalId = btn.getAttribute('data-modal');
            const modalEl = document.getElementById(modalId);
            if (!modalEl) return;

            const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
            const form = modalEl.querySelector('.dict-modal-form');
            if (!form) return;

            const isEdit = btn.hasAttribute('data-edit-id');
            const titleEl = modalEl.querySelector('.modal-title');

            loadGroupOptions(form);

            if (isEdit) {
                const id = btn.getAttribute('data-edit-id');
                const apiUrl = form.getAttribute('data-api-base');
                form.setAttribute('data-edit-id', id);
                if (titleEl) titleEl.textContent = 'Edit';
                form.action = apiUrl + '/' + id;
                form.method = 'put';
                apiCall(apiUrl + '/' + id)
                    .then(data => fillFormFields(form, data));
            } else {
                form.removeAttribute('data-edit-id');
                if (titleEl) titleEl.textContent = 'Create';
                form.action = form.getAttribute('data-api-base');
                form.method = 'post';
                form.reset();
            }

            modal.show();
        });
    });

    // Submit buttons
    document.querySelectorAll('.modal-submit').forEach(btn => {
        btn.addEventListener('click', () => {
            const modalEl = btn.closest('.modal');
            const form = modalEl.querySelector('.dict-modal-form');
            if (form) submitDictForm(form, modalEl);
        });
    });
}

function loadGroupOptions(form) {
    const groupSelects = form.querySelectorAll('select[name="groupId"]');
    groupSelects.forEach(select => {
        if (select.options.length > 1) return;
        apiCall('/api/rbac/groups?page=0&size=1000')
            .then(page => {
                const groups = page.content || page;
                groups.forEach(g => {
                    const option = document.createElement('option');
                    option.value = g.id;
                    option.textContent = g.name;
                    select.appendChild(option);
                });
            })
            .catch(() => {});
    });
}

function fillFormFields(form, data) {
    form.querySelectorAll('[name]').forEach(field => {
        const name = field.name;
        if (!data.hasOwnProperty(name)) return;
        const value = data[name];

        if (field.tagName === 'SELECT') {
            const setVal = () => {
                if (value != null) field.value = value;
            };
            if (field.options.length > 1) {
                setVal();
            } else {
                let retries = 0;
                const timer = setInterval(() => {
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
            field.value = value != null ? value : '';
        }
    });
}

function submitDictForm(form, modalEl) {
    const id = form.getAttribute('data-edit-id');
    const method = id ? 'PUT' : 'POST';
    const url = form.getAttribute('action');
    const submitBtn = modalEl.querySelector('.modal-submit');

    const body = {};
    form.querySelectorAll('[name]').forEach(field => {
        const value = field.value;
        if (field.name === 'password' && method === 'PUT' && !value) return;
        if (['status', 'groupId', 'sort', 'parentId', 'type', 'visible'].includes(field.name) && value !== '') {
            body[field.name] = parseInt(value, 10);
        } else {
            body[field.name] = value;
        }
    });

    // Clear errors
    form.querySelectorAll('.invalid-feedback').forEach(e => e.textContent = '');

    if (submitBtn) submitBtn.disabled = true;

    fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    })
        .then(r => r.json())
        .then(data => {
            if (data.code && data.code !== '200') {
                if (data.errors) throw data;
                throw new Error(data.message || 'Operation failed');
            }
            const modal = bootstrap.Modal.getInstance(modalEl);
            if (modal) modal.hide();
            showToast(method === 'PUT' ? 'Updated successfully' : 'Created successfully', 'success');
            setTimeout(() => location.reload(), 600);
        })
        .catch(err => {
            if (submitBtn) submitBtn.disabled = false;
            if (err?.errors) {
                for (const key in err.errors) {
                    const errorEl = form.querySelector(`.invalid-feedback[data-field="${key}"]`);
                    if (errorEl) errorEl.textContent = err.errors[key];
                }
            } else {
                showPageError(err?.message || 'Operation failed. Please try again.', 'error');
            }
        });
}

function showToast(message, type) {
    let toast = document.getElementById('dict-toast');
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
    toast._timer = setTimeout(() => toast.classList.remove('show'), 2500);
}

/* ===== Global Page Error Banner ===== */

function showPageError(message, type = 'error', duration = 4000) {
    let banner = document.getElementById('page-error-banner');
    if (!banner) {
        banner = document.createElement('div');
        banner.id = 'page-error-banner';
        banner.className = 'page-error-banner';
        banner.addEventListener('click', () => hidePageError());
        const main = document.querySelector('.admin-main');
        if (main) {
            main.insertBefore(banner, main.firstChild);
        } else {
            document.body.insertBefore(banner, document.body.firstChild);
        }
    }
    banner.textContent = message;
    banner.className = 'page-error-banner ' + type;
    banner.classList.add('show');
    clearTimeout(banner._timer);
    if (duration > 0) {
        banner._timer = setTimeout(() => hidePageError(), duration);
    }
}

function hidePageError() {
    const banner = document.getElementById('page-error-banner');
    if (banner) banner.classList.remove('show');
}

/* ===== Unified API Call ===== */

/**
 * Wraps fetch() and handles ApiResult response structured as:
 *   { "code": "200", "message": "success", "data": ... }
 *
 * - On success (code === "200"): resolves with data (unwrapped from ApiResult envelope)
 * - On error (code !== "200"): shows page error banner and rejects
 * - On network error: shows page error banner and rejects
 */
function apiCall(url, options = {}) {
    const showErr = (msg) => showPageError(msg, 'error');

    return fetch(url, options)
        .then(r => {
            if (!r.ok) {
                return r.json().then(data => {
                    const msg = data.message || ('HTTP error ' + r.status);
                    showErr(msg);
                    throw data;
                }).catch(err => {
                    if (err.message) showErr(err.message);
                    throw err;
                });
            }
            return r.json();
        })
        .then(data => {
            if (data.code && data.code !== '200') {
                showErr(data.message || 'Request failed');
                throw data;
            }
            return data.data !== undefined ? data.data : data;
        })
        .catch(err => {
            if (err instanceof TypeError && err.message === 'Failed to fetch') {
                showErr('Network error. Please check your connection.');
            }
            throw err;
        });
}

/* ===== Login Form ===== */

(function initLogin() {
    const form = document.getElementById('loginForm');
    if (!form) return;

    form.addEventListener('submit', function (e) {
        e.preventDefault();

        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;
        let errorDiv = document.querySelector('.login-error');

        document.querySelectorAll('.alert-warning, .alert-info').forEach(el => el.style.display = 'none');

        if (!username || !password) {
            showLoginError(errorDiv, 'Please enter both username and password.');
            return;
        }

        const btn = e.target.querySelector('button[type="submit"]');
        const originalText = btn.textContent;
        btn.disabled = true;
        btn.textContent = 'Signing in...';

        fetch('/api/rbac/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        })
            .then(response => {
                if (!response.ok) {
                    return response.text().then(text => {
                        try {
                            const json = JSON.parse(text);
                            throw new Error(json.message || text);
                        } catch (err) {
                            if (err.message !== text) throw err;
                            throw new Error(text || 'Invalid username or password');
                        }
                    });
                }
                return response.json();
            })
            .then(data => {
                const token = data.token || (data.data && data.data.token);
                if (!token) throw new Error('No token in login response');
                document.getElementById('tokenField').value = token;
                HTMLFormElement.prototype.submit.call(e.target);
            })
            .catch(err => {
                showLoginError(errorDiv, 'Login failed: ' + err.message);
                btn.disabled = false;
                btn.textContent = originalText;
            });
    });

    function showLoginError(container, message) {
        if (!container) {
            container = document.createElement('div');
            container.className = 'alert alert-danger login-error';
            const form = document.getElementById('loginForm');
            form.parentNode.insertBefore(container, form);
        }
        container.textContent = message;
        container.style.display = '';
    }

    ['username', 'password'].forEach(id => {
        document.getElementById(id).addEventListener('input', () => {
            const errorDiv = document.querySelector('.login-error');
            if (errorDiv) errorDiv.style.display = 'none';
        });
    });

    document.getElementById('username').focus();
})();
