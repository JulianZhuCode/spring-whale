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

        var loginApi = form.getAttribute('data-login-api') || '/api/rbac/auth/login';
        fetch(loginApi, {
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