const API = 'http://localhost:8080/api';

// ── AUTH HELPERS ──────────────────────────────────────────
function getUser() {
  return {
    token:    localStorage.getItem('token'),
    role:     localStorage.getItem('role'),
    userId:   localStorage.getItem('userId'),
    email:    localStorage.getItem('email'),
    fullName: localStorage.getItem('fullName'),
  };
}

function authHeaders() {
  const token = localStorage.getItem('token');
  return { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` };
}

function logout() {
  localStorage.clear();
  window.location.href = 'login.html';
}

function requireAuth(expectedRole) {
  const u = getUser();
  if (!u.token) { window.location.href = 'login.html'; return false; }
  if (expectedRole && u.role !== expectedRole) {
    window.location.href = u.role === 'COACH' ? 'dashboard-coach.html'
                         : u.role === 'ADMIN' ? 'dashboard-admin.html'
                         : 'dashboard-user.html';
    return false;
  }
  return true;
}

// ── NAVIGATION ────────────────────────────────────────────
function showPage(name) {
  document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
  document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
  const page = document.getElementById('page-' + name);
  if (page) page.classList.add('active');
  event.currentTarget.classList.add('active');
}

function initSidebar(fullName) {
  const el = document.getElementById('sidebarName');
  const av = document.getElementById('sidebarAvatar');
  if (el) el.textContent = fullName || 'User';
  if (av) av.textContent = (fullName || 'U')[0].toUpperCase();
}

// ── API HELPERS ───────────────────────────────────────────
async function api(path, method = 'GET', body = null) {
  const opts = { method, headers: authHeaders() };
  if (body) opts.body = JSON.stringify(body);
  const res = await fetch(API + path, opts);
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message || 'Request failed');
  }
  return res.json();
}

function showMsg(id, text, isError = false) {
  const el = document.getElementById(id);
  if (!el) return;
  el.style.display = 'block';
  el.className = 'msg ' + (isError ? 'error' : 'success');
  el.textContent = text;
  setTimeout(() => { el.style.display = 'none'; }, 4000);
}

// ─────────────────────────────────────────────────────────
// USER DASHBOARD
// ─────────────────────────────────────────────────────────
let selectedCoachId = null;
let dashboardData = null;

async function initUserDashboard() {
  if (!requireAuth('USER')) return;
  const u = getUser();
  initSidebar(u.fullName);

  const nameEl = document.getElementById('welcomeName');
  if (nameEl) nameEl.textContent = u.fullName ? u.fullName.split(' ')[0] : 'there';

  // set default log date to today
  const logDate = document.getElementById('logDate');
  if (logDate) logDate.value = new Date().toISOString().split('T')[0];

  await loadUserDashboard();
}

async function loadUserDashboard() {
  const u = getUser();
  try {
    dashboardData = await api('/dashboard/user/' + u.userId);
    renderUserStats(dashboardData);
    renderOverviewCoach(dashboardData.coach);
    renderOverviewLogs(dashboardData.recentLogs);
    renderPlansPage(dashboardData.workoutPlans);
    renderCoachPage(dashboardData.coach);
    renderLogHistory(dashboardData.recentLogs);
    renderProgress(dashboardData);
  } catch (e) {
    console.error('Dashboard load failed:', e);
  }
}

function renderUserStats(data) {
  document.getElementById('statWorkouts').textContent = data.totalWorkouts ?? 0;
  document.getElementById('statPlans').textContent = data.workoutPlans?.filter(p => p.isActive).length ?? 0;
  document.getElementById('statCoach').textContent = data.coach ? '✅ Assigned' : 'None';
  const goals = data.user?.fitnessGoals;
  document.getElementById('statGoal').textContent = goals
    ? goals.split(',')[0].trim().slice(0, 10)
    : 'Not set';
}

function renderOverviewCoach(coach) {
  const el = document.getElementById('overviewCoach');
  if (!el) return;
  if (!coach) {
    el.innerHTML = `<p style="color:var(--muted);font-size:.9rem">No coach assigned yet. <a href="#" onclick="showCoachPage()" style="color:var(--accent)">Find one →</a></p>`;
    return;
  }
  el.innerHTML = `
    <div style="display:flex;gap:14px;align-items:center">
      <div class="coach-avatar">${coach.fullName?.[0] ?? 'C'}</div>
      <div>
        <div style="font-weight:600">${coach.fullName}</div>
        <div style="font-size:.82rem;color:var(--muted)">${coach.specializations ?? 'General Fitness'}</div>
        <div style="font-size:.82rem;color:var(--accent);margin-top:4px">⭐ ${coach.rating?.toFixed(1) ?? 'N/A'}</div>
      </div>
    </div>`;
}

function renderOverviewLogs(logs) {
  const el = document.getElementById('overviewLogs');
  if (!el) return;
  if (!logs?.length) { el.innerHTML = `<div class="empty"><div class="empty-icon">📝</div><p>No workouts logged yet.</p></div>`; return; }
  el.innerHTML = logs.slice(0, 4).map(l => `
    <div class="log-item" style="margin-bottom:8px">
      <div>
        <div style="font-size:.85rem;font-weight:500">${new Date(l.workoutDate).toLocaleDateString()}</div>
        <div class="log-date">${l.durationMinutes} min · ${l.caloriesBurned} kcal</div>
      </div>
      ${l.notes ? `<span style="font-size:.8rem;color:var(--muted);max-width:120px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">${l.notes}</span>` : ''}
    </div>`).join('');
}

function renderCoachPage(coach) {
  const el = document.getElementById('coachSection');
  const card = document.getElementById('availableCoachesCard');
  if (!el) return;

  if (coach) {
    el.innerHTML = `
      <div class="coach-card">
        <div class="coach-avatar">${coach.fullName?.[0] ?? 'C'}</div>
        <div class="coach-info" style="flex:1">
          <h3>${coach.fullName}</h3>
          <div class="spec">${coach.specializations ?? 'General Fitness'}</div>
          <div class="rating">⭐ ${coach.rating?.toFixed(1) ?? 'N/A'}</div>
          ${coach.bio ? `<p style="font-size:.85rem;color:var(--muted);margin-top:8px">${coach.bio}</p>` : ''}
        </div>
        <button class="btn btn-danger btn-sm" onclick="unassignCoach()">Unassign</button>
      </div>`;
    if (card) card.style.display = 'none';
  } else {
    el.innerHTML = `
      <div class="no-coach">
        <div style="font-size:2rem;margin-bottom:12px">🧑‍💼</div>
        <p>You don't have a coach yet. Browse available coaches below and assign one.</p>
      </div>`;
    if (card) card.style.display = 'block';
    loadAvailableCoaches();
  }
}

async function loadAvailableCoaches() {
  const el = document.getElementById('coachesList');
  if (!el) return;
  el.innerHTML = `<div class="loading"><div class="spinner"></div> Loading coaches...</div>`;
  try {
    const coaches = await api('/coaches/available');
    if (!coaches.length) {
      el.innerHTML = `<div class="empty"><div class="empty-icon">🧑‍💼</div><p>No coaches available right now.</p></div>`;
      return;
    }
    el.innerHTML = coaches.map(c => `
      <div style="display:flex;align-items:center;justify-content:space-between;padding:14px 0;border-bottom:1px solid var(--border)">
        <div style="display:flex;gap:14px;align-items:center">
          <div class="coach-avatar" style="width:40px;height:40px;font-size:1rem">${c.user?.fullName?.[0] ?? 'C'}</div>
          <div>
            <div style="font-weight:600;font-size:.95rem">${c.user?.fullName ?? 'Coach'}</div>
            <div style="font-size:.8rem;color:var(--muted)">${c.specializations ?? 'General'} · ⭐ ${c.rating?.toFixed(1) ?? 'N/A'}</div>
          </div>
        </div>
        <button class="btn btn-secondary btn-sm" onclick="openAssignModal(${c.user?.id}, '${c.user?.fullName ?? 'Coach'}', '${c.specializations ?? ''}', ${c.rating ?? 0})">Assign</button>
      </div>`).join('');
  } catch (e) {
    el.innerHTML = `<div class="empty"><p style="color:var(--danger)">Failed to load coaches.</p></div>`;
  }
}

function openAssignModal(coachId, name, spec, rating) {
  selectedCoachId = coachId;
  document.getElementById('modalCoachInfo').innerHTML = `
    <div class="coach-card" style="margin-bottom:0">
      <div class="coach-avatar">${name[0]}</div>
      <div class="coach-info">
        <h3>${name}</h3>
        <div class="spec">${spec || 'General Fitness'}</div>
        <div class="rating">⭐ ${rating.toFixed ? rating.toFixed(1) : rating}</div>
      </div>
    </div>`;
  document.getElementById('coachModal').classList.add('open');
}

function closeModal() {
  document.getElementById('coachModal').classList.remove('open');
  selectedCoachId = null;
}

async function confirmAssign() {
  if (!selectedCoachId) return;
  const u = getUser();
  try {
    await api(`/coaches/assign?userId=${u.userId}&coachId=${selectedCoachId}`, 'POST');
    closeModal();
    await loadUserDashboard();
  } catch (e) {
    alert('Failed to assign coach: ' + e.message);
  }
}

async function unassignCoach() {
  if (!confirm('Remove your coach assignment?')) return;
  const u = getUser();
  try {
    await api(`/coaches/unassign?userId=${u.userId}`, 'POST');
    await loadUserDashboard();
  } catch (e) {
    alert('Failed: ' + e.message);
  }
}

function showCoachPage() {
  showPage('coach');
}

function renderPlansPage(plans) {
  const el = document.getElementById('plansSection');
  if (!el) return;
  if (!plans?.length) {
    el.innerHTML = `<div class="empty"><div class="empty-icon">📋</div><p>No workout plans assigned yet. Ask your coach to create one.</p></div>`;
    return;
  }
  el.innerHTML = `<div class="plan-list">` + plans.map(p => `
    <div class="plan-item">
      <div>
        <h4>${p.name}</h4>
        <p>${p.description || ''} · ${p.durationWeeks} weeks · ${p.difficultyLevel}</p>
      </div>
      <span class="badge ${p.isActive ? 'badge-green' : 'badge-gray'}">${p.isActive ? 'Active' : 'Inactive'}</span>
    </div>`).join('') + `</div>`;
}

function renderLogHistory(logs) {
  const el = document.getElementById('logHistory');
  if (!el) return;
  if (!logs?.length) { el.innerHTML = `<div class="empty"><div class="empty-icon">📝</div><p>No workouts logged yet.</p></div>`; return; }
  el.innerHTML = `<div class="log-list">` + logs.map(l => `
    <div class="log-item">
      <div>
        <div style="font-weight:500;font-size:.9rem">${new Date(l.workoutDate).toLocaleDateString('en-US', {weekday:'short',month:'short',day:'numeric'})}</div>
        <div class="log-date">${l.notes || 'No notes'}</div>
      </div>
      <div class="log-stats">
        <span>⏱ <strong>${l.durationMinutes}m</strong></span>
        <span>🔥 <strong>${l.caloriesBurned}</strong></span>
      </div>
    </div>`).join('') + `</div>`;
}

function renderProgress(data) {
  const el = document.getElementById('progressSection');
  if (!el) return;
  const logs = data.recentLogs ?? [];
  const totalCal = logs.reduce((s, l) => s + (l.caloriesBurned ?? 0), 0);
  const totalMin = logs.reduce((s, l) => s + (l.durationMinutes ?? 0), 0);
  el.innerHTML = `
    <div class="stats-row" style="margin-bottom:0">
      <div class="stat-card"><div class="stat-value">${logs.length}</div><div class="stat-label">Recent Sessions</div></div>
      <div class="stat-card"><div class="stat-value">${totalMin}</div><div class="stat-label">Total Minutes</div></div>
      <div class="stat-card"><div class="stat-value">${totalCal}</div><div class="stat-label">Calories Burned</div></div>
    </div>`;
}

async function submitLog() {
  const u = getUser();
  const date = document.getElementById('logDate').value;
  const duration = document.getElementById('logDuration').value;
  const calories = document.getElementById('logCalories').value;
  const difficulty = document.getElementById('logDifficulty').value;
  const notes = document.getElementById('logNotes').value;

  if (!date || !duration) { showMsg('logMsg', 'Date and duration are required.', true); return; }

  try {
    await api('/workoutlogs', 'POST', {
      userId: parseInt(u.userId),
      workoutDate: date + 'T00:00:00',
      durationMinutes: parseInt(duration),
      caloriesBurned: parseInt(calories) || 0,
      difficultyRating: difficulty,
      notes
    });
    showMsg('logMsg', 'Workout logged successfully! 💪');
    document.getElementById('logDuration').value = '';
    document.getElementById('logCalories').value = '';
    document.getElementById('logNotes').value = '';
    await loadUserDashboard();
  } catch (e) {
    showMsg('logMsg', 'Failed to log workout: ' + e.message, true);
  }
}

// ─────────────────────────────────────────────────────────
// COACH DASHBOARD
// ─────────────────────────────────────────────────────────
async function initCoachDashboard() {
  if (!requireAuth('COACH')) return;
  const u = getUser();
  initSidebar(u.fullName);
  await loadCoachDashboard();
}

async function loadCoachDashboard() {
  const u = getUser();
  try {
    const data = await api('/dashboard/coach/' + u.userId);
    renderCoachStats(data);
    renderCoachProfile(data.profile);
    renderClientsList(data.clients, 'recentClients', 4);
    renderClientsList(data.clients, 'allClients');
    populateClientSelect(data.clients);
    await loadReviews();
  } catch (e) {
    console.error('Coach dashboard failed:', e);
  }
}

function renderCoachStats(data) {
  document.getElementById('statClients').textContent = data.clientCount ?? 0;
  document.getElementById('statRating').textContent = data.profile?.rating?.toFixed(1) ?? 'N/A';
  document.getElementById('statExperience').textContent = (data.profile?.yearsExperience ?? '—') + ' yrs';
  document.getElementById('statAvailable').textContent = data.profile?.isAvailable ? 'Open' : 'Closed';
}

function renderCoachProfile(profile) {
  const el = document.getElementById('coachProfileView');
  if (!el) return;
  if (!profile) { el.innerHTML = `<p style="color:var(--muted)">No coach profile set up yet.</p>`; return; }
  el.innerHTML = `
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px">
      <div><div style="font-size:.75rem;color:var(--muted);text-transform:uppercase;letter-spacing:.05em;margin-bottom:4px">Specializations</div><div>${profile.specializations || '—'}</div></div>
      <div><div style="font-size:.75rem;color:var(--muted);text-transform:uppercase;letter-spacing:.05em;margin-bottom:4px">Experience</div><div>${profile.yearsExperience ?? 0} years</div></div>
      <div class="full" style="grid-column:1/-1"><div style="font-size:.75rem;color:var(--muted);text-transform:uppercase;letter-spacing:.05em;margin-bottom:4px">Bio</div><div style="color:var(--muted);font-size:.9rem">${profile.bio || 'No bio added.'}</div></div>
    </div>`;
}

function renderClientsList(clients, elId, limit) {
  const el = document.getElementById(elId);
  if (!el) return;
  const list = limit ? clients.slice(0, limit) : clients;
  if (!list.length) { el.innerHTML = `<div class="empty"><div class="empty-icon">👥</div><p>No clients yet.</p></div>`; return; }
  el.innerHTML = `
    <table style="width:100%;border-collapse:collapse">
      <thead><tr>
        <th style="text-align:left;padding:8px 12px;font-size:.75rem;color:var(--muted);text-transform:uppercase;border-bottom:1px solid var(--border)">Name</th>
        <th style="text-align:left;padding:8px 12px;font-size:.75rem;color:var(--muted);text-transform:uppercase;border-bottom:1px solid var(--border)">Email</th>
        <th style="text-align:left;padding:8px 12px;font-size:.75rem;color:var(--muted);text-transform:uppercase;border-bottom:1px solid var(--border)">Level</th>
        <th style="text-align:left;padding:8px 12px;font-size:.75rem;color:var(--muted);text-transform:uppercase;border-bottom:1px solid var(--border)">Goal</th>
      </tr></thead>
      <tbody>` + list.map(c => `
        <tr>
          <td style="padding:12px;border-bottom:1px solid rgba(255,255,255,.04)">
            <div style="display:flex;align-items:center;gap:10px">
              <div class="avatar" style="width:28px;height:28px;font-size:.75rem">${(c.fullName||'U')[0]}</div>
              ${c.fullName || 'User'}
            </div>
          </td>
          <td style="padding:12px;border-bottom:1px solid rgba(255,255,255,.04);color:var(--muted);font-size:.88rem">${c.email}</td>
          <td style="padding:12px;border-bottom:1px solid rgba(255,255,255,.04)"><span class="badge badge-blue">${c.experienceLevel || '—'}</span></td>
          <td style="padding:12px;border-bottom:1px solid rgba(255,255,255,.04);color:var(--muted);font-size:.88rem">${c.fitnessGoals || '—'}</td>
        </tr>`).join('') + `</tbody></table>`;
}

function populateClientSelect(clients) {
  const sel = document.getElementById('planClient');
  if (!sel) return;
  sel.innerHTML = '<option value="">Select client...</option>' +
    clients.map(c => `<option value="${c.id}">${c.fullName || c.email}</option>`).join('');
}

async function submitPlan() {
  const u = getUser();
  const clientId = document.getElementById('planClient').value;
  const name = document.getElementById('planName').value;
  const description = document.getElementById('planDescription').value;
  const difficulty = document.getElementById('planDifficulty').value;
  const duration = document.getElementById('planDuration').value;
  const start = document.getElementById('planStart').value;
  const end = document.getElementById('planEnd').value;

  if (!clientId || !name) { showMsg('planMsg', 'Client and plan name are required.', true); return; }

  try {
    await api('/workoutplans', 'POST', {
      userId: parseInt(clientId),
      coachId: parseInt(u.userId),
      name, description,
      difficultyLevel: difficulty,
      durationWeeks: parseInt(duration) || 4,
      startDate: start || null,
      endDate: end || null,
      isActive: true
    });
    showMsg('planMsg', 'Plan created successfully! ✅');
    document.getElementById('planName').value = '';
    document.getElementById('planDescription').value = '';
  } catch (e) {
    showMsg('planMsg', 'Failed: ' + e.message, true);
  }
}

async function loadReviews() {
  const el = document.getElementById('reviewsList');
  if (!el) return;
  const u = getUser();
  try {
    const reviews = await api('/reviews/coach/' + u.userId);
    if (!reviews.length) { el.innerHTML = `<div class="empty"><div class="empty-icon">⭐</div><p>No reviews yet.</p></div>`; return; }
    el.innerHTML = reviews.map(r => `
      <div style="padding:16px 0;border-bottom:1px solid var(--border)">
        <div style="display:flex;justify-content:space-between;margin-bottom:6px">
          <div style="font-weight:500">${r.user?.fullName ?? 'Anonymous'}</div>
          <div style="color:var(--accent)">${'⭐'.repeat(r.rating)}</div>
        </div>
        <div style="color:var(--muted);font-size:.88rem">${r.comment || 'No comment.'}</div>
      </div>`).join('');
  } catch (e) {
    el.innerHTML = `<div class="empty"><p style="color:var(--danger)">Failed to load reviews.</p></div>`;
  }
}

// ─────────────────────────────────────────────────────────
// ADMIN DASHBOARD
// ─────────────────────────────────────────────────────────
let allUsersData = [];

async function initAdminDashboard() {
  if (!requireAuth('ADMIN')) return;
  await loadAdminDashboard();
}

async function loadAdminDashboard() {
  try {
    const data = await api('/dashboard/admin');
    allUsersData = data.users ?? [];

    document.getElementById('statTotalUsers').textContent = data.totalUsers ?? 0;
    document.getElementById('statTotalCoaches').textContent = data.totalCoaches ?? 0;
    document.getElementById('statTotalAccounts').textContent = data.totalAccounts ?? 0;

    renderAdminUsersTable(allUsersData.slice(0, 5), 'recentUsers');
    renderAdminUsersTable(allUsersData, 'usersTableBody');
    renderAdminCoaches(allUsersData.filter(u => u.role === 'COACH'));
  } catch (e) {
    console.error('Admin dashboard failed:', e);
  }
}

function renderAdminUsersTable(users, elId) {
  const el = document.getElementById(elId);
  if (!el) return;
  if (!users.length) { el.innerHTML = `<div class="empty"><p>No users found.</p></div>`; return; }

  // recentUsers gets a simpler card layout
  if (elId === 'recentUsers') {
    el.innerHTML = `<div class="table-wrap"><table><thead><tr><th>Name</th><th>Email</th><th>Role</th><th>Coach</th></tr></thead><tbody>` +
      users.map(u => `<tr>
        <td>${u.fullName || '—'}</td>
        <td style="color:var(--muted);font-size:.88rem">${u.email}</td>
        <td><span class="badge ${u.role === 'COACH' ? 'badge-blue' : u.role === 'ADMIN' ? 'badge-red' : 'badge-gray'}">${u.role}</span></td>
        <td><span class="badge ${u.hasCoach ? 'badge-green' : 'badge-gray'}">${u.hasCoach ? 'Assigned' : 'None'}</span></td>
      </tr>`).join('') + `</tbody></table></div>`;
    return;
  }

  el.innerHTML = users.map(u => `<tr>
    <td>${u.fullName || '—'}</td>
    <td style="color:var(--muted);font-size:.88rem">${u.email}</td>
    <td><span class="badge ${u.role === 'COACH' ? 'badge-blue' : u.role === 'ADMIN' ? 'badge-red' : 'badge-gray'}">${u.role}</span></td>
    <td><span class="badge ${u.hasCoach ? 'badge-green' : 'badge-gray'}">${u.hasCoach ? 'Assigned' : 'None'}</span></td>
  </tr>`).join('');
}

function filterUsers() {
  const q = document.getElementById('userSearch').value.toLowerCase();
  const filtered = allUsersData.filter(u =>
    (u.fullName || '').toLowerCase().includes(q) ||
    (u.email || '').toLowerCase().includes(q)
  );
  renderAdminUsersTable(filtered, 'usersTableBody');
}

function renderAdminCoaches(coaches) {
  const el = document.getElementById('coachesAdminList');
  if (!el) return;
  if (!coaches.length) { el.innerHTML = `<div class="empty"><div class="empty-icon">🧑‍💼</div><p>No coaches registered.</p></div>`; return; }
  el.innerHTML = `<div class="table-wrap"><table><thead><tr><th>Name</th><th>Email</th></tr></thead><tbody>` +
    coaches.map(c => `<tr>
      <td>${c.fullName || '—'}</td>
      <td style="color:var(--muted);font-size:.88rem">${c.email}</td>
    </tr>`).join('') + `</tbody></table></div>`;
}