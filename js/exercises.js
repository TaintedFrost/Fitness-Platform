const DAYS = ['', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];

//coach side manage exercises on a plan
let coachPlans = [];

async function loadCoachPlansWithExercises() {
  const el = document.getElementById('coachPlansSection');
  if (!el) return;
  const u = getUser();
  el.innerHTML = `<div class="loading"><div class="spinner"></div></div>`;
  try {
    coachPlans = await api('/workoutplans/coach/' + u.userId);
    if (!coachPlans.length) {
      el.innerHTML = `<div class="empty"><div class="empty-icon">📋</div><p>No plans created yet. Create one using the form above.</p></div>`;
      return;
    }
    el.innerHTML = coachPlans.map(p => renderCoachPlanCard(p)).join('');
  } catch (e) {
    el.innerHTML = `<div class="empty"><p style="color:var(--danger)">Failed to load plans.</p></div>`;
  }
}

function renderCoachPlanCard(p) {
  const exCount = p.exercises?.length ?? 0;
  return `
    <div class="card" style="margin-bottom:16px">
      <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:12px">
        <div>
          <div style="font-family:'Syne',sans-serif;font-weight:700;font-size:1.05rem">${p.name}</div>
          <div style="font-size:.82rem;color:var(--muted);margin-top:2px">
            For: ${p.userName || '—'} · ${p.difficultyLevel || '—'} · ${p.durationWeeks} weeks
            · <span class="badge ${p.isActive ? 'badge-green' : 'badge-gray'}" style="font-size:.7rem">${p.isActive ? 'Active' : 'Inactive'}</span>
          </div>
        </div>
        <button class="btn btn-secondary btn-sm" onclick="toggleExercisePanel(${p.id})">
          ➕ Add Exercise
        </button>
      </div>

      <!-- Exercise add form (hidden by default) -->
      <div id="exForm-${p.id}" style="display:none;background:var(--surface);border-radius:8px;padding:16px;margin-bottom:16px">
        <div style="font-weight:600;font-size:.9rem;margin-bottom:12px">Add Exercise to "${p.name}"</div>
        <div class="form-grid">
          <div class="field full"><label>Exercise Name</label><input type="text" id="exName-${p.id}" placeholder="e.g. Bench Press"></div>
          <div class="field full"><label>Instructions</label><textarea id="exInstructions-${p.id}" rows="2" placeholder="Step-by-step instructions..."></textarea></div>
          <div class="field"><label>Muscle Group</label>
            <select id="exMuscle-${p.id}">
              <option value="">Select...</option>
              <option>Chest</option><option>Back</option><option>Shoulders</option>
              <option>Arms</option><option>Legs</option><option>Core</option><option>Full Body</option><option>Cardio</option>
            </select>
          </div>
          <div class="field"><label>Type</label>
            <select id="exType-${p.id}">
              <option value="STRENGTH">Strength</option>
              <option value="CARDIO">Cardio</option>
              <option value="FLEXIBILITY">Flexibility</option>
              <option value="BALANCE">Balance</option>
              <option value="SPORTS">Sports</option>
            </select>
          </div>
          <div class="field"><label>Day of Week</label>
            <select id="exDay-${p.id}">
              <option value="1">Monday</option><option value="2">Tuesday</option>
              <option value="3">Wednesday</option><option value="4">Thursday</option>
              <option value="5">Friday</option><option value="6">Saturday</option><option value="7">Sunday</option>
            </select>
          </div>
          <div class="field"><label>Sets</label><input type="number" id="exSets-${p.id}" placeholder="e.g. 3" min="1"></div>
          <div class="field"><label>Reps</label><input type="number" id="exReps-${p.id}" placeholder="e.g. 12" min="1"></div>
          <div class="field"><label>Rest (seconds)</label><input type="number" id="exRest-${p.id}" placeholder="e.g. 60" min="0"></div>
          <div class="field"><label>Duration (min, for cardio)</label><input type="number" id="exDur-${p.id}" placeholder="e.g. 20" min="0"></div>
          <div class="field"><label>Video URL (optional)</label><input type="text" id="exVideo-${p.id}" placeholder="https://youtube.com/..."></div>
        </div>
        <div style="display:flex;gap:10px;margin-top:4px">
          <button class="btn btn-primary" style="width:auto;padding:9px 20px" onclick="addExercise(${p.id})">Add Exercise</button>
          <button class="btn btn-secondary" onclick="toggleExercisePanel(${p.id})">Cancel</button>
        </div>
        <p class="msg" id="exMsg-${p.id}" style="display:none;margin-top:8px"></p>
      </div>

      <!-- Exercises list -->
      <div id="exList-${p.id}">
        ${renderExerciseList(p.exercises, true)}
      </div>
    </div>`;
}

function toggleExercisePanel(planId) {
  const el = document.getElementById(`exForm-${planId}`);
  if (el) el.style.display = el.style.display === 'none' ? 'block' : 'none';
}

function renderExerciseList(exercises, canDelete) {
  if (!exercises?.length)
    return `<div class="empty" style="padding:12px 0"><p style="font-size:.85rem">No exercises yet. Add some using the button above.</p></div>`;

  //group by day
  const byDay = {};
  exercises.forEach(e => {
    const day = e.dayOfWeek || 0;
    if (!byDay[day]) byDay[day] = [];
    byDay[day].push(e);
  });

  return Object.keys(byDay).sort((a,b) => a-b).map(day => `
    <div style="margin-bottom:16px">
      <div style="font-size:.78rem;text-transform:uppercase;letter-spacing:.08em;color:var(--accent);font-weight:700;margin-bottom:8px">
        ${DAYS[day] || 'Unscheduled'}
      </div>
      ${byDay[day].map(e => `
        <div style="background:var(--surface);border:1px solid var(--border);border-radius:8px;padding:14px;margin-bottom:8px">
          <div style="display:flex;justify-content:space-between;align-items:flex-start;gap:8px">
            <div style="flex:1">
              <div style="font-weight:600;font-size:.95rem;margin-bottom:4px">
                ${e.name}
                ${e.muscleGroup ? `<span class="badge badge-gray" style="font-size:.7rem;margin-left:6px">${e.muscleGroup}</span>` : ''}
                ${e.exerciseType ? `<span class="badge badge-blue" style="font-size:.7rem;margin-left:4px">${e.exerciseType}</span>` : ''}
              </div>
              <div style="display:flex;gap:16px;font-size:.82rem;color:var(--muted);flex-wrap:wrap;margin-bottom:6px">
                ${e.sets  ? `<span>💪 ${e.sets} sets</span>` : ''}
                ${e.reps  ? `<span>🔁 ${e.reps} reps</span>` : ''}
                ${e.durationMinutes ? `<span>⏱ ${e.durationMinutes} min</span>` : ''}
                ${e.restSeconds ? `<span>😴 ${e.restSeconds}s rest</span>` : ''}
              </div>
              ${e.instructions ? `
                <div style="font-size:.82rem;color:var(--muted);line-height:1.5;margin-top:4px">
                  <strong style="color:var(--text)">Instructions:</strong> ${e.instructions}
                </div>` : ''}
              ${e.videoUrl ? `
                <a href="${e.videoUrl}" target="_blank" style="font-size:.8rem;color:var(--accent);display:inline-block;margin-top:6px">▶ Watch Video</a>` : ''}
            </div>
            ${canDelete ? `<button onclick="deleteExercise(${e.id}, this)" style="background:none;border:none;color:var(--muted);cursor:pointer;font-size:1rem;padding:0;flex-shrink:0">🗑</button>` : ''}
          </div>
        </div>`).join('')}
    </div>`).join('');
}

async function addExercise(planId) {
  const name         = document.getElementById(`exName-${planId}`)?.value.trim();
  const instructions = document.getElementById(`exInstructions-${planId}`)?.value.trim();
  const muscle       = document.getElementById(`exMuscle-${planId}`)?.value;
  const type         = document.getElementById(`exType-${planId}`)?.value;
  const day          = document.getElementById(`exDay-${planId}`)?.value;
  const sets         = document.getElementById(`exSets-${planId}`)?.value;
  const reps         = document.getElementById(`exReps-${planId}`)?.value;
  const rest         = document.getElementById(`exRest-${planId}`)?.value;
  const dur          = document.getElementById(`exDur-${planId}`)?.value;
  const video        = document.getElementById(`exVideo-${planId}`)?.value.trim();

  if (!name) { showMsg(`exMsg-${planId}`, 'Exercise name is required.', true); return; }

  try {
    await api(`/workoutplans/${planId}/exercises`, 'POST', {
      name, instructions: instructions || '',
      muscleGroup: muscle || '', exerciseType: type || 'STRENGTH',
      dayOfWeek: parseInt(day) || 1,
      sets: parseInt(sets) || 0, reps: parseInt(reps) || 0,
      restSeconds: parseInt(rest) || 0,
      durationMinutes: parseInt(dur) || 0,
      videoUrl: video || ''
    });

    showMsg(`exMsg-${planId}`, 'Exercise added! ✅');
    document.getElementById(`exName-${planId}`).value         = '';
    document.getElementById(`exInstructions-${planId}`).value = '';

    // Reload this plan's exercises
    const updated = await api(`/workoutplans/${planId}`);
    document.getElementById(`exList-${planId}`).innerHTML = renderExerciseList(updated.exercises, true);
  } catch (e) {
    showMsg(`exMsg-${planId}`, 'Failed: ' + e.message, true);
  }
}

async function deleteExercise(exerciseId, btn) {
  if (!confirm('Remove this exercise?')) return;
  try {
    await api(`/workoutplans/exercises/${exerciseId}`, 'DELETE');
    btn.closest('[style*="background:var(--surface)"]').remove();
  } catch (e) { alert('Failed: ' + e.message); }
}

//user side — view plan with full exercise schedule
async function loadUserPlansWithExercises() {
  const el = document.getElementById('plansSection');
  if (!el) return;
  const u = getUser();
  el.innerHTML = `<div class="loading"><div class="spinner"></div></div>`;
  try {
    const plans = await api('/workoutplans/user/' + u.userId);
    if (!plans.length) {
      el.innerHTML = `<div class="empty"><div class="empty-icon">📋</div><p>No workout plans assigned yet. Ask your coach to create one.</p></div>`;
      return;
    }

    //load full plan details (with exercises) for each
    const detailed = await Promise.all(plans.map(p => api('/workoutplans/' + p.id)));

    el.innerHTML = detailed.map(p => `
      <div class="card" style="margin-bottom:20px">
        <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:16px">
          <div>
            <div style="font-family:'Syne',sans-serif;font-weight:800;font-size:1.2rem">${p.name}</div>
            <div style="font-size:.82rem;color:var(--muted);margin-top:4px">
              ${p.description || ''} · ${p.difficultyLevel || ''} · ${p.durationWeeks} weeks
              ${p.coachName ? ` · Coach: ${p.coachName}` : ''}
            </div>
          </div>
          <span class="badge ${p.isActive ? 'badge-green' : 'badge-gray'}">${p.isActive ? 'Active' : 'Inactive'}</span>
        </div>
        ${p.exercises?.length
          ? `<div style="border-top:1px solid var(--border);padding-top:16px">
               <div style="font-size:.75rem;text-transform:uppercase;letter-spacing:.08em;color:var(--muted);margin-bottom:12px">Weekly Schedule</div>
               ${renderExerciseList(p.exercises, false)}
             </div>`
          : `<div style="color:var(--muted);font-size:.88rem;border-top:1px solid var(--border);padding-top:12px">No exercises added to this plan yet.</div>`
        }
      </div>`).join('');
  } catch (e) {
    el.innerHTML = `<div class="empty"><p style="color:var(--danger)">Failed to load plans.</p></div>`;
  }
}