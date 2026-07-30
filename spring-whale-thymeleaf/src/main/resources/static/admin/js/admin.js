// Spring Whale Admin JS

document.addEventListener('DOMContentLoaded', () => {
    initSidebarToggle();
    initSidebarGroups();
    initConfirmDialogs();
    initModalSystem();
    initDeleteButtons();
    initTableSearch();
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

/* ===== Global Confirm Dialog ===== */

/**
 * 公共确认对话框组件。
 *
 * @param {Object|string} opts - 选项对象或直接传消息文本
 * @param {string} opts.message   - 确认消息（必填）
 * @param {string} [opts.title]   - 标题，默认"确认操作"
 * @param {string} [opts.type]    - 类型：warning(默认)|danger|success|info
 * @param {string} [opts.okText]  - 确认按钮文字，默认"确定"
 * @param {string} [opts.cancelText] - 取消按钮文字，默认"取消"
 * @returns {Promise<boolean>}    - 用户点击确认返回 true，取消返回 false
 *
 * 示例：
 *   if (await showConfirm('确定要删除吗？')) { ... }
 *   if (await showConfirm({message:'危险操作', type:'danger', okText:'确认删除'})) { ... }
 */
function showConfirm(opts) {
    if (typeof opts === 'string') opts = {message: opts};
    const {
        message,
        title = '确认操作',
        type = 'warning',
        okText = '确定',
        cancelText = '取消'
    } = opts || {};

    const modalEl = document.getElementById('global-confirm-modal');
    if (!modalEl) return Promise.resolve(confirm(message || '确定要执行此操作吗？'));

    const titleEl = modalEl.querySelector('#gc-title');
    const msgEl = modalEl.querySelector('#gc-message');
    const iconWrap = modalEl.querySelector('#gc-icon');
    const btnOk = modalEl.querySelector('#gc-btn-ok');
    const btnCancel = modalEl.querySelector('#gc-btn-cancel');

    titleEl.textContent = title || '确认操作';
    msgEl.textContent = message || '确定要执行此操作吗？';
    btnOk.textContent = okText || '确定';
    btnCancel.textContent = cancelText || '取消';

    // 图标 & 确认按钮样式
    const iconMap = {
        warning: {icon: 'bi-exclamation-triangle-fill', color: '#ffc107', bg: '#fff8e1', btn: 'btn-warning'},
        danger:  {icon: 'bi-x-circle-fill',         color: '#dc3545', bg: '#fde8ea', btn: 'btn-danger'},
        success: {icon: 'bi-check-circle-fill',     color: '#198754', bg: '#e8f5ee', btn: 'btn-success'},
        info:    {icon: 'bi-info-circle-fill',      color: '#0dcaf0', bg: '#e6faff', btn: 'btn-info'},
    };
    const theme = iconMap[type] || iconMap.warning;
    iconWrap.innerHTML = '<i class="bi ' + theme.icon + ' fs-4"></i>';
    iconWrap.style.background = theme.bg;
    iconWrap.style.color = theme.color;
    // 重置按钮 class
    btnOk.className = 'btn rounded-3 px-4 ' + theme.btn;

    const modal = bootstrap.Modal.getOrCreateInstance(modalEl);

    return new Promise(resolve => {
        let settled = false;
        const cleanup = () => {
            btnOk.removeEventListener('click', onOk);
            btnCancel.removeEventListener('click', onCancel);
            modalEl.removeEventListener('hidden.bs.modal', onHide);
            if (!settled) {
                settled = true;
                resolve(false);
            }
        };
        const onOk = () => {
            if (settled) return;
            settled = true;
            modal.hide();
            setTimeout(() => {
                cleanup();
                resolve(true);
            }, 150);
        };
        const onCancel = () => {
            if (settled) return;
            settled = true;
            modal.hide();
            setTimeout(() => {
                cleanup();
                resolve(false);
            }, 150);
        };
        const onHide = () => {
            cleanup();
        };
        btnOk.addEventListener('click', onOk);
        btnCancel.addEventListener('click', onCancel);
        modalEl.addEventListener('hidden.bs.modal', onHide);
        modal.show();
    });
}

function initConfirmDialogs() {
    document.querySelectorAll('[data-confirm]').forEach(el => {
        el.addEventListener('click', async function (e) {
            const message = this.getAttribute('data-confirm') || 'Are you sure?';
            const title = this.getAttribute('data-confirm-title') || null;
            const type = this.getAttribute('data-confirm-type') || 'warning';
            const okText = this.getAttribute('data-confirm-ok') || null;
            e.preventDefault();
            const ok = await showConfirm({message, title, type, okText});
            if (ok) {
                // 通过原生提交或触发原行为
                if (this.tagName === 'FORM') this.submit();
                else this.click();
            }
        });
    });
}

/* ===== Delete Buttons ===== */

function initDeleteButtons() {
    document.querySelectorAll('[data-delete-api]').forEach(btn => {
        btn.addEventListener('click', async function () {
            const id = this.getAttribute('data-delete-id');
            const api = this.getAttribute('data-delete-api');
            const name = this.getAttribute('data-delete-name') || '';
            const message = name
                ? '确认删除「' + name + '」？此操作不可撤销。'
                : '确认删除？此操作不可撤销。';

            const ok = await showConfirm({
                message,
                type: 'danger',
                title: '删除确认',
                okText: '确认删除'
            });
            if (!ok) return;

            var origHtml = this.innerHTML;
            this.disabled = true;
            this.innerHTML = '<span class="spinner-border spinner-border-sm"></span>';

            fetch(api + '/' + id, { method: 'DELETE' })
                .then(function (r) { return r.json(); })
                .then(function (data) {
                    if (data.code && data.code !== '200') {
                        throw new Error(data.message || '删除失败');
                    }
                    showToast('删除成功', 'success');
                    setTimeout(function () { location.reload(); }, 500);
                })
                .catch(function (err) {
                    btn.disabled = false;
                    btn.innerHTML = origHtml;
                    showPageError(err.message || '删除失败', 'error');
                });
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
            .catch(() => {
            });
    });
}

function fillFormFields(form, data) {
    // Ensure any tag selectors in this form are initialized before populating
    const modalEl = form.closest('.modal');
    if (modalEl) {
        modalEl.querySelectorAll('.tag-selector').forEach(function (ts) {
            if (!ts._inited && typeof window.initTagSelectors === 'function') {
                window.initTagSelectors();
            }
        });
    }

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
        } else if (name === 'relatedWords' || name === 'relatedGrammars') {
            // Tag selector: populate via component API
            const tagSelector = form.querySelector('.tag-selector[data-field="' + name + '"]');
            const itemsKey = name === 'relatedWords' ? 'relatedWordItems' : 'relatedGrammarItems';
            if (tagSelector && typeof tagSelector.setTagData === 'function') {
                tagSelector.setTagData(data[itemsKey]);
                field.value = value ? value.join(',') : '';
            } else if (Array.isArray(value)) {
                field.value = value.join(', ');
            } else {
                field.value = value != null ? value : '';
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

    // Fields that should be sent as string arrays (comma-separated input)
    const stringArrayFields = ['meaning', 'notes'];
    // Fields that should be sent as integer arrays (comma-separated IDs)
    const intArrayFields = ['relatedWords', 'relatedGrammars'];

    const body = {};
    form.querySelectorAll('[name]').forEach(field => {
        const value = field.value;
        if (field.name === 'password' && method === 'PUT' && !value) return;
        if (['status', 'groupId', 'sort', 'parentId', 'type', 'visible'].includes(field.name) && value !== '') {
            body[field.name] = parseInt(value, 10);
        } else if (stringArrayFields.includes(field.name)) {
            body[field.name] = value ? value.split(',').map(s => s.trim()).filter(s => s) : [];
        } else if (intArrayFields.includes(field.name)) {
            body[field.name] = value ? value.split(',').map(s => parseInt(s.trim(), 10)).filter(n => !isNaN(n)) : [];
        } else {
            body[field.name] = value;
        }
    });

    // Clear errors
    form.querySelectorAll('.invalid-feedback').forEach(e => e.textContent = '');

    if (submitBtn) submitBtn.disabled = true;

    fetch(url, {
        method,
        headers: {'Content-Type': 'application/json'},
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
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({username, password})
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

/* ===== Table Search (universal filter bar) ===== */

/**
 * Initializes all table search bars on the page.
 *
 * A search bar is a container marked with `data-table-search` that holds:
 *   - a text input  (input[data-search-field="keyword"])
 *   - a clear button (button[data-search-clear])
 *   - a search button (button[data-search-submit])
 *   - any number of filter selects (select[data-search-field])
 *
 * Attributes on the container:
 *   data-table-search  : base URL for the search (e.g. "/admin/rbac/users")
 *   data-debounce-ms   : debounce delay in ms for text input (default: 800)
 *
 * Attributes on fields:
 *   data-search-field  : query param name (e.g. "keyword", "status", "type")
 *   data-search-clear  : marks the clear button
 *   data-search-submit : marks the search button
 *
 * Usage example:
 *   <div class="row g-2" data-table-search="/admin/dict/words" data-debounce-ms="800">
 *     <div class="col-md-5">
 *       <div class="input-group">
 *         <span class="input-group-text"><i class="bi bi-search"></i></span>
 *         <input class="form-control" data-search-field="keyword" type="text"
 *                placeholder="Search..." th:value="${keyword}">
 *         <button class="btn btn-outline-secondary" data-search-clear type="button">
 *           <i class="bi bi-x-lg"></i>
 *         </button>
 *         <button class="btn btn-primary" data-search-submit type="button">
 *           <i class="bi bi-search me-1"></i>Search
 *         </button>
 *       </div>
 *     </div>
 *     <div class="col-md-3">
 *       <select class="form-select" data-search-field="pos">
 *         <option value="">All</option>
 *         ...
 *       </select>
 *     </div>
 *   </div>
 */
function initTableSearch() {
    const containers = document.querySelectorAll('[data-table-search]');
    containers.forEach(container => {
        if (container._searchInited) return;
        container._searchInited = true;

        const baseUrl = container.getAttribute('data-table-search');
        const debounceMs = parseInt(container.getAttribute('data-debounce-ms') || '800', 10);

        const keywordInput = container.querySelector('input[data-search-field="keyword"]');
        const clearBtn = container.querySelector('[data-search-clear]');
        const submitBtn = container.querySelector('[data-search-submit]');
        const filterFields = container.querySelectorAll('[data-search-field]');

        let searchTimer = null;

        function collectParams() {
            const params = new URLSearchParams(window.location.search);
            filterFields.forEach(field => {
                const paramName = field.getAttribute('data-search-field');
                const value = field.value.trim();
                if (value) {
                    params.set(paramName, value);
                } else {
                    params.delete(paramName);
                }
            });
            params.delete('page');
            return params;
        }

        function doSearch() {
            const params = collectParams();
            const sep = baseUrl.includes('?') ? '&' : '?';
            const paramStr = params.toString();
            window.location.href = baseUrl + (paramStr ? sep + paramStr : '');
        }

        function updateClearBtn() {
            if (!clearBtn || !keywordInput) return;
            clearBtn.style.display = keywordInput.value ? '' : 'none';
        }

        // Keyword input: debounced search
        if (keywordInput) {
            updateClearBtn();
            keywordInput.addEventListener('input', () => {
                updateClearBtn();
                clearTimeout(searchTimer);
                searchTimer = setTimeout(doSearch, debounceMs);
            });
            keywordInput.addEventListener('keydown', (e) => {
                if (e.key === 'Enter') {
                    clearTimeout(searchTimer);
                    doSearch();
                }
            });
        }

        // Clear button
        if (clearBtn) {
            clearBtn.addEventListener('click', () => {
                if (keywordInput) {
                    keywordInput.value = '';
                    keywordInput.dispatchEvent(new Event('input'));
                }
                clearTimeout(searchTimer);
                doSearch();
            });
        }

        // Submit button
        if (submitBtn) {
            submitBtn.addEventListener('click', () => {
                clearTimeout(searchTimer);
                doSearch();
            });
        }

        // Filter selects: immediate search on change
        filterFields.forEach(field => {
            if (field.tagName === 'SELECT') {
                field.addEventListener('change', () => {
                    clearTimeout(searchTimer);
                    doSearch();
                });
            }
        });
    });
}

// Expose for manual re-init (e.g. after dynamic content load)
window.initTableSearch = initTableSearch;
