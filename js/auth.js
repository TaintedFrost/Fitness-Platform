const API_BASE = 'http://localhost:8080/api';

// ─── REGISTER ───────────────────────────────────────────────────────────────
const registerForm = document.getElementById('registerForm');
if (registerForm) {
    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const message = document.getElementById('message');

        const payload = {
            fullName:          document.getElementById('fullName').value.trim(),
            email:             document.getElementById('email').value.trim(),
            password:          document.getElementById('password').value,
            fitnessGoals:      document.getElementById('fitnessGoals').value.trim(),
            experienceLevel:   document.getElementById('experienceLevel').value,
            schedulePreference: document.getElementById('schedulePreference').value.trim()
        };

        try {
            const res = await fetch(`${API_BASE}/auth/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            const data = await res.json();

            if (res.ok) {
                localStorage.setItem('token', data.token);
                localStorage.setItem('role', data.role);
                message.style.color = 'green';
                message.textContent = 'Registration successful! Redirecting...';
                setTimeout(() => redirectToDashboard(data.role), 1000);
            } else {
                message.style.color = 'red';
                message.textContent = data.message || 'Registration failed.';
            }
        } catch (err) {
            document.getElementById('message').style.color = 'red';
            document.getElementById('message').textContent = 'Could not reach server.';
            console.error(err);
        }
    });
}

// ─── LOGIN ───────────────────────────────────────────────────────────────────
const loginForm = document.getElementById('loginForm');
if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const message = document.getElementById('loginMessage');

        const payload = {
            email:    document.getElementById('loginEmail').value.trim(),
            password: document.getElementById('loginPassword').value
        };

        try {
            const res = await fetch(`${API_BASE}/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            const data = await res.json();

            if (res.ok) {
                localStorage.setItem('token', data.token);
                localStorage.setItem('role', data.role);
                message.style.color = 'green';
                message.textContent = 'Login successful! Redirecting...';
                setTimeout(() => redirectToDashboard(data.role), 1000);
            } else {
                message.style.color = 'red';
                message.textContent = data.message || 'Invalid credentials.';
            }
        } catch (err) {
            message.style.color = 'red';
            message.textContent = 'Could not reach server.';
            console.error(err);
        }
    });
}

// ─── HELPERS ─────────────────────────────────────────────────────────────────
function redirectToDashboard(role) {
    switch (role) {
        case 'ADMIN': window.location.href = 'dashboard-admin.html'; break;
        case 'COACH': window.location.href = 'dashboard-coach.html'; break;
        default:      window.location.href = 'dashboard-user.html';
    }
}

// Call this on any protected page to get the stored token
function getAuthHeaders() {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = 'login.html';
        return null;
    }
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
}

// Call this on any protected page load to guard against unauthenticated access
function requireAuth() {
    const token = localStorage.getItem('token');
    if (!token) window.location.href = 'login.html';
}