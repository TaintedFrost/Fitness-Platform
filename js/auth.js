const API_BASE = 'http://localhost:8080/api';

function storeSession(data) {
  localStorage.setItem('token',    data.token);
  localStorage.setItem('role',     data.role);
  localStorage.setItem('userId',   data.userId);
  localStorage.setItem('email',    data.email);
  localStorage.setItem('fullName', data.fullName || '');
}

function redirectToDashboard(role) {
  switch (role) {
    case 'ADMIN': window.location.href = 'dashboard-admin.html'; break;
    case 'COACH': window.location.href = 'dashboard-coach.html'; break;
    default:      window.location.href = 'dashboard-user.html';
  }
}

// ── REGISTER ─────────────────────────────────────────────
const registerForm = document.getElementById('registerForm');
if (registerForm) {
  registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const msg = document.getElementById('message');

    const payload = {
      fullName:           document.getElementById('fullName').value.trim(),
      email:              document.getElementById('email').value.trim(),
      password:           document.getElementById('password').value,
      fitnessGoals:       document.getElementById('fitnessGoals').value.trim(),
      experienceLevel:    document.getElementById('experienceLevel').value,
      schedulePreference: document.getElementById('schedulePreference').value.trim()
    };

    try {
      const res  = await fetch(`${API_BASE}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      const data = await res.json();

      if (res.ok) {
        storeSession(data);
        msg.className = 'msg success';
        msg.textContent = 'Registered! Redirecting...';
        setTimeout(() => redirectToDashboard(data.role), 900);
      } else {
        msg.className = 'msg error';
        msg.textContent = data.message || 'Registration failed.';
      }
    } catch (err) {
      msg.className = 'msg error';
      msg.textContent = 'Could not reach server.';
    }
    msg.style.display = 'block';
  });
}

// ── LOGIN ─────────────────────────────────────────────────
const loginForm = document.getElementById('loginForm');
if (loginForm) {
  loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const msg = document.getElementById('loginMessage');

    const payload = {
      email:    document.getElementById('loginEmail').value.trim(),
      password: document.getElementById('loginPassword').value
    };

    try {
      const res  = await fetch(`${API_BASE}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      const data = await res.json();

      if (res.ok) {
        storeSession(data);
        msg.className = 'msg success';
        msg.textContent = 'Welcome back! Redirecting...';
        setTimeout(() => redirectToDashboard(data.role), 900);
      } else {
        msg.className = 'msg error';
        msg.textContent = data.message || 'Invalid credentials.';
      }
    } catch (err) {
      msg.className = 'msg error';
      msg.textContent = 'Could not reach server.';
    }
    msg.style.display = 'block';
  });
}