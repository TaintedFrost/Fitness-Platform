//User: load nutrition page
async function loadNutritionPage() {
  const el = document.getElementById('nutritionSection');
  if (!el) return;
  const u = getUser();
  el.innerHTML = `<div class="loading"><div class="spinner"></div></div>`;

  try {
    const plans = await api('/nutrition/user/' + u.userId);

    let html = `
      <!-- Auto-generate button -->
      <div class="card" style="max-width:580px;margin-bottom:24px;background:linear-gradient(135deg,rgba(200,245,66,.06),rgba(66,245,200,.03));border-color:rgba(200,245,66,.2)">
        <div style="display:flex;align-items:center;gap:16px">
          <div style="font-size:2rem">🥗</div>
          <div style="flex:1">
            <div style="font-family:'Syne',sans-serif;font-weight:700;margin-bottom:4px">Get a Personalised Nutrition Plan</div>
            <div style="font-size:.85rem;color:var(--muted)">We'll generate a plan aligned with your fitness goal: <strong style="color:var(--text)">${localStorage.getItem('fitnessGoals') || 'General Fitness'}</strong></div>
          </div>
          <button class="btn btn-primary" style="width:auto;padding:10px 20px;flex-shrink:0" onclick="generateNutritionPlan()">Generate →</button>
        </div>
        <p class="msg" id="nutritionGenMsg" style="display:none;margin-top:12px"></p>
      </div>`;

    if (!plans.length) {
      html += `<div class="empty"><div class="empty-icon">🥗</div><p>No nutrition plans yet. Generate one above or ask your coach to create one.</p></div>`;
    } else {
      html += plans.map(p => renderNutritionCard(p, true)).join('');
    }

    el.innerHTML = html;
  } catch (e) {
    el.innerHTML = `<div class="empty"><p style="color:var(--danger)">Failed to load nutrition plans.</p></div>`;
  }
}

async function generateNutritionPlan() {
  const u = getUser();
  showMsg('nutritionGenMsg', 'Generating your personalised plan...');
  try {
    await api('/nutrition/generate/' + u.userId, 'POST');
    showMsg('nutritionGenMsg', 'Plan generated! ✅');
    setTimeout(() => loadNutritionPage(), 1000);
  } catch (e) {
    showMsg('nutritionGenMsg', 'Failed: ' + e.message, true);
  }
}

async function deleteNutritionPlan(planId) {
  if (!confirm('Delete this nutrition plan?')) return;
  try {
    await api('/nutrition/' + planId, 'DELETE');
    loadNutritionPage();
  } catch (e) { alert('Failed: ' + e.message); }
}

function renderNutritionCard(p, canDelete) {
  const macroTotal = (p.proteinGrams * 4) + (p.carbsGrams * 4) + (p.fatGrams * 9);
  const proteinPct = macroTotal ? Math.round((p.proteinGrams * 4) / macroTotal * 100) : 33;
  const carbsPct   = macroTotal ? Math.round((p.carbsGrams   * 4) / macroTotal * 100) : 33;
  const fatPct     = macroTotal ? Math.round((p.fatGrams     * 9) / macroTotal * 100) : 34;

  return `
    <div class="card" style="margin-bottom:20px">
      <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:16px">
        <div>
          <div style="font-family:'Syne',sans-serif;font-weight:800;font-size:1.15rem">${p.title}</div>
          <div style="font-size:.82rem;color:var(--muted);margin-top:3px">
            🎯 Goal: ${p.fitnessGoalTarget || 'General Fitness'}
            ${p.coachName ? ` · Coach: ${p.coachName}` : ' · Auto-generated'}
            ${p.createdAt ? ` · ${new Date(p.createdAt).toLocaleDateString()}` : ''}
          </div>
        </div>
        <div style="display:flex;gap:8px;align-items:center">
          <span class="badge ${p.isActive ? 'badge-green' : 'badge-gray'}">${p.isActive ? 'Active' : 'Inactive'}</span>
          ${canDelete ? `<button onclick="deleteNutritionPlan(${p.id})" style="background:none;border:none;color:var(--muted);cursor:pointer;font-size:1rem">🗑</button>` : ''}
        </div>
      </div>

      ${p.description ? `<p style="color:var(--muted);font-size:.88rem;margin-bottom:16px;line-height:1.6">${p.description}</p>` : ''}

      <!-- Daily targets -->
      <div class="stats-row" style="margin-bottom:20px">
        <div class="stat-card"><div class="stat-value" style="font-size:1.6rem">${p.dailyCalories}</div><div class="stat-label">Daily Calories</div></div>
        <div class="stat-card"><div class="stat-value" style="font-size:1.6rem;color:#42f5c8">${p.proteinGrams}g</div><div class="stat-label">Protein</div></div>
        <div class="stat-card"><div class="stat-value" style="font-size:1.6rem;color:#c8f542">${p.carbsGrams}g</div><div class="stat-label">Carbs</div></div>
        <div class="stat-card"><div class="stat-value" style="font-size:1.6rem;color:#ffa500">${p.fatGrams}g</div><div class="stat-label">Fat</div></div>
        <div class="stat-card"><div class="stat-value" style="font-size:1.6rem;color:#4fc3f7">${p.waterLiters}L</div><div class="stat-label">Water / Day</div></div>
      </div>

      <!-- Macro bar -->
      <div style="margin-bottom:20px">
        <div style="display:flex;justify-content:space-between;font-size:.78rem;color:var(--muted);margin-bottom:6px">
          <span>Macro Split</span>
          <span style="color:#42f5c8">Protein ${proteinPct}%</span>
          <span style="color:#c8f542">Carbs ${carbsPct}%</span>
          <span style="color:#ffa500">Fat ${fatPct}%</span>
        </div>
        <div style="height:10px;border-radius:5px;overflow:hidden;display:flex">
          <div style="background:#42f5c8;width:${proteinPct}%"></div>
          <div style="background:#c8f542;width:${carbsPct}%"></div>
          <div style="background:#ffa500;width:${fatPct}%"></div>
        </div>
      </div>

      <!-- Meal plan -->
      <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;margin-bottom:16px">
        ${mealBox('🌅', 'Breakfast', p.breakfast)}
        ${mealBox('☀️', 'Lunch',     p.lunch)}
        ${mealBox('🌙', 'Dinner',    p.dinner)}
        ${mealBox('🍎', 'Snacks',    p.snacks)}
      </div>

      ${p.supplements ? `
        <div style="background:rgba(200,245,66,.05);border:1px solid rgba(200,245,66,.15);border-radius:8px;padding:14px;margin-bottom:12px">
          <div style="font-size:.75rem;text-transform:uppercase;letter-spacing:.05em;color:var(--accent);margin-bottom:6px">💊 Supplements</div>
          <p style="font-size:.88rem;color:var(--muted)">${p.supplements}</p>
        </div>` : ''}

      ${p.restrictions ? `
        <div style="background:rgba(255,77,77,.05);border:1px solid rgba(255,77,77,.15);border-radius:8px;padding:14px">
          <div style="font-size:.75rem;text-transform:uppercase;letter-spacing:.05em;color:var(--danger);margin-bottom:6px">⚠️ Dietary Restrictions</div>
          <p style="font-size:.88rem;color:var(--muted)">${p.restrictions}</p>
        </div>` : ''}
    </div>`;
}

function mealBox(icon, label, content) {
  return `
    <div style="background:var(--surface);border:1px solid var(--border);border-radius:8px;padding:14px">
      <div style="font-size:.75rem;text-transform:uppercase;letter-spacing:.05em;color:var(--muted);margin-bottom:6px">${icon} ${label}</div>
      <p style="font-size:.85rem;line-height:1.6;color:${content ? 'var(--text)' : 'var(--muted)'}">${content || 'Not specified'}</p>
    </div>`;
}

//coach: create nutrition plan for client
async function loadCoachNutritionPage() {
  const u = getUser();
  const el = document.getElementById('coachNutritionSection');
  if (!el) return;

  //get all clients then their plans
  try {
    const dashData = await api('/dashboard/coach/' + u.userId);
    const clients  = dashData.clients || [];

    el.innerHTML = `
      <div class="card" style="max-width:600px;margin-bottom:24px">
        <div class="card-header"><span class="card-title">➕ Create Nutrition Plan for Client</span></div>
        <div class="form-grid">
          <div class="field full"><label>Client</label>
            <select id="nutritionClient">
              <option value="">Select client...</option>
              ${clients.map(c => `<option value="${c.id}">${c.fullName || c.email}</option>`).join('')}
            </select>
          </div>
          <div class="field full"><label>Plan Title</label><input type="text" id="nutritionTitle" placeholder="e.g. Weight Loss Meal Plan"></div>
          <div class="field full"><label>Fitness Goal Target</label><input type="text" id="nutritionGoal" placeholder="e.g. Weight Loss, Muscle Building"></div>
          <div class="field full"><label>Description</label><textarea id="nutritionDesc" rows="2" placeholder="Overview of this nutrition plan..."></textarea></div>
          <div class="field"><label>Daily Calories</label><input type="number" id="nutritionCal" placeholder="e.g. 2000"></div>
          <div class="field"><label>Protein (g)</label><input type="number" id="nutritionProtein" placeholder="e.g. 150"></div>
          <div class="field"><label>Carbs (g)</label><input type="number" id="nutritionCarbs" placeholder="e.g. 250"></div>
          <div class="field"><label>Fat (g)</label><input type="number" id="nutritionFat" placeholder="e.g. 70"></div>
          <div class="field"><label>Water (L/day)</label><input type="number" id="nutritionWater" placeholder="e.g. 3" step="0.5"></div>
          <div class="field full"><label>Breakfast</label><textarea id="nutritionBreakfast" rows="2" placeholder="Breakfast recommendations..."></textarea></div>
          <div class="field full"><label>Lunch</label><textarea id="nutritionLunch" rows="2" placeholder="Lunch recommendations..."></textarea></div>
          <div class="field full"><label>Dinner</label><textarea id="nutritionDinner" rows="2" placeholder="Dinner recommendations..."></textarea></div>
          <div class="field full"><label>Snacks</label><textarea id="nutritionSnacks" rows="2" placeholder="Snack options..."></textarea></div>
          <div class="field full"><label>Supplements</label><input type="text" id="nutritionSupplements" placeholder="e.g. Whey protein, Creatine, Multivitamin"></div>
          <div class="field full"><label>Dietary Restrictions / Allergies</label><input type="text" id="nutritionRestrictions" placeholder="e.g. Lactose intolerant, No nuts"></div>
        </div>
        <button class="btn btn-primary" onclick="submitCoachNutritionPlan()">Create Plan</button>
        <p class="msg" id="coachNutritionMsg" style="display:none"></p>
      </div>`;
  } catch (e) {
    el.innerHTML = `<div class="empty"><p style="color:var(--danger)">Failed to load.</p></div>`;
  }
}

async function submitCoachNutritionPlan() {
  const u        = getUser();
  const clientId = document.getElementById('nutritionClient')?.value;
  const title    = document.getElementById('nutritionTitle')?.value.trim();
  if (!clientId || !title) { showMsg('coachNutritionMsg', 'Client and title are required.', true); return; }

  try {
    await api('/nutrition', 'POST', {
      userId:            parseInt(clientId),
      coachId:           parseInt(u.userId),
      title,
      fitnessGoalTarget: document.getElementById('nutritionGoal')?.value.trim()        || '',
      description:       document.getElementById('nutritionDesc')?.value.trim()        || '',
      dailyCalories:     document.getElementById('nutritionCal')?.value                || 0,
      proteinGrams:      document.getElementById('nutritionProtein')?.value            || 0,
      carbsGrams:        document.getElementById('nutritionCarbs')?.value              || 0,
      fatGrams:          document.getElementById('nutritionFat')?.value                || 0,
      waterLiters:       document.getElementById('nutritionWater')?.value              || 0,
      breakfast:         document.getElementById('nutritionBreakfast')?.value.trim()   || '',
      lunch:             document.getElementById('nutritionLunch')?.value.trim()       || '',
      dinner:            document.getElementById('nutritionDinner')?.value.trim()      || '',
      snacks:            document.getElementById('nutritionSnacks')?.value.trim()      || '',
      supplements:       document.getElementById('nutritionSupplements')?.value.trim() || '',
      restrictions:      document.getElementById('nutritionRestrictions')?.value.trim()|| ''
    });
    showMsg('coachNutritionMsg', 'Nutrition plan created! ✅');
  } catch (e) {
    showMsg('coachNutritionMsg', 'Failed: ' + e.message, true);
  }
}