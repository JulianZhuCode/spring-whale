/* ===== Core Utilities ===== */

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