/* ===== Global Confirm Dialog ===== */

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
    if (!modalEl) {
        console.warn('showConfirm: global-confirm-modal not found, skipping confirmation');
        return Promise.resolve(true);
    }

    const titleEl = modalEl.querySelector('#gc-title');
    const msgEl = modalEl.querySelector('#gc-message');
    const iconWrap = modalEl.querySelector('#gc-icon');
    const btnOk = modalEl.querySelector('#gc-btn-ok');
    const btnCancel = modalEl.querySelector('#gc-btn-cancel');

    titleEl.textContent = title || '确认操作';
    msgEl.textContent = message || '确定要执行此操作吗？';
    btnOk.textContent = okText || '确定';
    btnCancel.textContent = cancelText || '取消';

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
                if (this.tagName === 'FORM') this.submit();
                else this.click();
            }
        });
    });
}