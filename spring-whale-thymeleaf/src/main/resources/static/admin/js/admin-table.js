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

/* ===== Table Search ===== */

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

        if (submitBtn) {
            submitBtn.addEventListener('click', () => {
                clearTimeout(searchTimer);
                doSearch();
            });
        }

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

window.initTableSearch = initTableSearch;