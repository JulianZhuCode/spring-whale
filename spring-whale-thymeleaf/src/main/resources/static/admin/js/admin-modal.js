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
                    .then(data => {
                        fillFormFields(form, data);
                        modal.show();
                    });
            } else {
                form.removeAttribute('data-edit-id');
                if (titleEl) titleEl.textContent = 'Create';
                form.action = form.getAttribute('data-api-base');
                form.method = 'post';
                form.reset();
                form.querySelectorAll('.tag-selector').forEach(ts => {
                    if (typeof ts.clearTags === 'function') ts.clearTags();
                });
                modal.show();
            }
        });
    });

    document.querySelectorAll('.modal').forEach(modalEl => {
        modalEl.addEventListener('hidden.bs.modal', () => {
            const form = modalEl.querySelector('.dict-modal-form');
            if (form) {
                form.querySelectorAll('.tag-selector').forEach(ts => {
                    if (typeof ts.clearTags === 'function') ts.clearTags();
                });
            }
        });
    });

    document.querySelectorAll('.modal-submit').forEach(btn => {
        btn.addEventListener('click', () => {
            const modalEl = btn.closest('.modal');
            const form = modalEl.querySelector('.dict-modal-form');
            if (form) submitDictForm(form, modalEl);
        });
    });
}

function loadGroupOptions(form) {
    var groupApi = form.getAttribute('data-group-api');
    if (!groupApi) return;
    var groupSelects = form.querySelectorAll('select[name="groupId"]');
    groupSelects.forEach(select => {
        if (select.options.length > 1) return;
        apiCall(groupApi)
            .then(page => {
                var groups = page.content || page;
                groups.forEach(g => {
                    var option = document.createElement('option');
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
    var modalEl = form.closest('.modal');
    if (modalEl) {
        modalEl.querySelectorAll('.tag-selector').forEach(function (ts) {
            if (!ts._inited && typeof window.initTagSelectors === 'function') {
                window.initTagSelectors();
            }
        });
    }

    form.querySelectorAll('[name]').forEach(field => {
        var name = field.name;
        if (!data.hasOwnProperty(name)) return;
        var value = data[name];

        if (field.tagName === 'SELECT') {
            var setVal = function () {
                if (value != null) {
                    if (typeof value === 'object') {
                        field.value = (value.name !== undefined) ? value.name : (value.id !== undefined) ? value.id : value;
                    } else {
                        field.value = value;
                    }
                }
            };
            if (field.options.length > 1) {
                setVal();
            } else {
                var retries = 0;
                var timer = setInterval(function () {
                    retries++;
                    if (field.options.length > 1 || retries > 20) {
                        clearInterval(timer);
                        setVal();
                    }
                }, 100);
            }
        } else if (field.hasAttribute('data-tag-field')) {
            var tagField = field.getAttribute('data-tag-field');
            var itemsKey = field.getAttribute('data-tag-items-key');
            var tagSelector = form.querySelector('.tag-selector[data-field="' + tagField + '"]');
            if (tagSelector && typeof tagSelector.setTagData === 'function' && itemsKey) {
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
    var id = form.getAttribute('data-edit-id');
    var method = id ? 'PUT' : 'POST';
    var url = form.getAttribute('action');
    var submitBtn = modalEl.querySelector('.modal-submit');

    var body = {};
    form.querySelectorAll('[name]').forEach(field => {
        var value = field.value;
        if (field.name === 'password' && method === 'PUT' && !value) return;
        var arrayType = field.getAttribute('data-array-type');
        if (field.hasAttribute('data-int-field') && value !== '') {
            body[field.name] = parseInt(value, 10);
        } else if (arrayType === 'string') {
            body[field.name] = value ? value.split(',').map(function (s) { return s.trim(); }).filter(function (s) { return s; }) : [];
        } else if (arrayType === 'int') {
            body[field.name] = value ? value.split(',').map(function (s) { return parseInt(s.trim(), 10); }).filter(function (n) { return !isNaN(n); }) : [];
        } else {
            body[field.name] = value;
        }
    });

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