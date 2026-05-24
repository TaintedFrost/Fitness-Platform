// ─────────────────────────────────────────────────────────
// COMMUNITY — add this to dashboard.js or include separately
// ─────────────────────────────────────────────────────────

let currentPostId = null;

// ── Load all posts ────────────────────────────────────────
async function loadCommunity() {
  const el = document.getElementById('communityFeed');
  if (!el) return;
  el.innerHTML = `<div class="loading"><div class="spinner"></div> Loading posts...</div>`;
  try {
    const posts = await api('/forum');
    renderPosts(posts, el);
  } catch (e) {
    el.innerHTML = `<div class="empty"><p style="color:var(--danger)">Failed to load community.</p></div>`;
  }
}

function renderPosts(posts, el) {
  if (!posts.length) {
    el.innerHTML = `<div class="empty"><div class="empty-icon">💬</div><p>No posts yet. Be the first to start a discussion!</p></div>`;
    return;
  }
  el.innerHTML = posts.map(p => `
    <div class="card" style="margin-bottom:16px;cursor:pointer" onclick="openPost(${p.id})">
      <div style="display:flex;justify-content:space-between;align-items:flex-start;gap:12px">
        <div style="flex:1;min-width:0">
          <div style="display:flex;align-items:center;gap:10px;margin-bottom:8px">
            <div class="avatar" style="width:32px;height:32px;font-size:.8rem;flex-shrink:0">${(p.author?.fullName||'?')[0]}</div>
            <div>
              <span style="font-weight:600;font-size:.9rem">${p.author?.fullName || 'Anonymous'}</span>
              <span class="badge ${p.author?.role === 'COACH' ? 'badge-blue' : 'badge-gray'}" style="margin-left:6px;font-size:.7rem">${p.author?.role || 'USER'}</span>
            </div>
            <span style="color:var(--muted);font-size:.78rem;margin-left:auto">${formatDate(p.createdAt)}</span>
          </div>
          <h3 style="font-family:'Syne',sans-serif;font-weight:700;font-size:1.05rem;margin-bottom:6px">${p.title}</h3>
          <p style="color:var(--muted);font-size:.88rem;overflow:hidden;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical">${p.content}</p>
        </div>
      </div>
      <div style="margin-top:12px;padding-top:12px;border-top:1px solid var(--border);display:flex;align-items:center;gap:16px">
        <span style="font-size:.82rem;color:var(--muted)">💬 ${p.commentCount} comment${p.commentCount !== 1 ? 's' : ''}</span>
        <span style="font-size:.82rem;color:var(--accent);margin-left:auto">Read more →</span>
      </div>
    </div>`).join('');
}

// ── Open single post ──────────────────────────────────────
async function openPost(postId) {
  currentPostId = postId;
  const overlay = document.getElementById('postModal');
  if (!overlay) return;
  overlay.classList.add('open');
  document.getElementById('postModalBody').innerHTML =
    `<div class="loading"><div class="spinner"></div></div>`;

  try {
    const post = await api('/forum/' + postId);
    renderPostDetail(post);
  } catch (e) {
    document.getElementById('postModalBody').innerHTML =
      `<p style="color:var(--danger)">Failed to load post.</p>`;
  }
}

function renderPostDetail(post) {
  const u = getUser();
  const canDelete = u.userId == post.author?.id || u.role === 'ADMIN';

  document.getElementById('postModalBody').innerHTML = `
    <div style="margin-bottom:20px">
      <div style="display:flex;align-items:center;gap:10px;margin-bottom:12px">
        <div class="avatar" style="width:36px;height:36px">${(post.author?.fullName||'?')[0]}</div>
        <div>
          <div style="font-weight:600">${post.author?.fullName || 'Anonymous'}
            <span class="badge ${post.author?.role === 'COACH' ? 'badge-blue' : 'badge-gray'}" style="margin-left:6px;font-size:.7rem">${post.author?.role}</span>
          </div>
          <div style="font-size:.78rem;color:var(--muted)">${formatDate(post.createdAt)}</div>
        </div>
        ${canDelete ? `<button class="btn btn-danger btn-sm" style="margin-left:auto" onclick="deletePost(${post.id})">Delete</button>` : ''}
      </div>
      <h2 style="font-family:'Syne',sans-serif;font-weight:800;font-size:1.3rem;margin-bottom:10px">${post.title}</h2>
      <p style="color:var(--muted);line-height:1.7;white-space:pre-wrap">${post.content}</p>
    </div>

    <div style="border-top:1px solid var(--border);padding-top:20px">
      <div style="font-family:'Syne',sans-serif;font-weight:700;margin-bottom:14px">
        💬 ${post.comments?.length || 0} Comment${post.comments?.length !== 1 ? 's' : ''}
      </div>
      <div id="commentsList">
        ${(post.comments || []).map(c => renderComment(c, u)).join('') || 
          '<p style="color:var(--muted);font-size:.88rem">No comments yet. Be the first!</p>'}
      </div>

      <div style="margin-top:20px;display:flex;gap:10px;align-items:flex-start">
        <div class="avatar" style="width:32px;height:32px;flex-shrink:0">${(u.fullName||'?')[0]}</div>
        <div style="flex:1">
          <textarea id="commentInput" rows="2" placeholder="Write a comment..." style="resize:none"></textarea>
          <button class="btn btn-primary" style="margin-top:8px;width:auto;padding:8px 20px" onclick="submitComment()">Post Comment</button>
        </div>
      </div>
      <p class="msg" id="commentMsg" style="display:none;margin-top:8px"></p>
    </div>`;
}

function renderComment(c, u) {
  const canDelete = u && (u.userId == c.author?.id || u.role === 'ADMIN');
  return `
    <div style="display:flex;gap:10px;margin-bottom:14px;padding-bottom:14px;border-bottom:1px solid rgba(255,255,255,.05)">
      <div class="avatar" style="width:28px;height:28px;font-size:.75rem;flex-shrink:0">${(c.author?.fullName||'?')[0]}</div>
      <div style="flex:1">
        <div style="display:flex;align-items:center;gap:8px;margin-bottom:4px">
          <span style="font-weight:600;font-size:.88rem">${c.author?.fullName || 'Anonymous'}</span>
          <span class="badge ${c.author?.role === 'COACH' ? 'badge-blue' : 'badge-gray'}" style="font-size:.68rem">${c.author?.role}</span>
          <span style="color:var(--muted);font-size:.75rem;margin-left:auto">${formatDate(c.createdAt)}</span>
          ${canDelete ? `<button onclick="deleteComment(${c.id})" style="background:none;border:none;color:var(--muted);cursor:pointer;font-size:.8rem;padding:0">✕</button>` : ''}
        </div>
        <p style="font-size:.88rem;color:var(--muted)">${c.content}</p>
      </div>
    </div>`;
}

function closePostModal() {
  document.getElementById('postModal')?.classList.remove('open');
  currentPostId = null;
}

// ── Submit new post ───────────────────────────────────────
async function submitPost() {
  const u = getUser();
  const title   = document.getElementById('postTitle')?.value.trim();
  const content = document.getElementById('postContent')?.value.trim();

  if (!title || !content) { showMsg('postMsg', 'Title and content are required.', true); return; }

  try {
    await api('/forum', 'POST', {
      userId: parseInt(u.userId), title, content
    });
    document.getElementById('postTitle').value   = '';
    document.getElementById('postContent').value = '';
    document.getElementById('newPostForm').style.display = 'none';
    showMsg('postMsg', 'Post published! ✅');
    await loadCommunity();
  } catch (e) {
    showMsg('postMsg', e.message || 'Failed to post.', true);
  }
}

// ── Submit comment ────────────────────────────────────────
async function submitComment() {
  const u = getUser();
  const content = document.getElementById('commentInput')?.value.trim();
  if (!content) return;

  try {
    const comment = await api(`/forum/${currentPostId}/comments`, 'POST', {
      userId: parseInt(u.userId), content
    });

    // Append comment to list
    const list = document.getElementById('commentsList');
    const noComments = list.querySelector('p');
    if (noComments) noComments.remove();
    list.insertAdjacentHTML('beforeend', renderComment(comment, u));
    document.getElementById('commentInput').value = '';
  } catch (e) {
    showMsg('commentMsg', 'Failed to comment.', true);
  }
}

// ── Delete post ───────────────────────────────────────────
async function deletePost(postId) {
  if (!confirm('Delete this post and all its comments?')) return;
  try {
    await api('/forum/' + postId, 'DELETE');
    closePostModal();
    await loadCommunity();
  } catch (e) {
    alert('Failed to delete: ' + e.message);
  }
}

// ── Delete comment ────────────────────────────────────────
async function deleteComment(commentId) {
  try {
    await api('/forum/comments/' + commentId, 'DELETE');
    // Remove from DOM
    event.target.closest('[style*="margin-bottom:14px"]').remove();
  } catch (e) {
    alert('Failed to delete comment.');
  }
}

// ── Admin moderation ──────────────────────────────────────
async function loadAdminCommunity() {
  const el = document.getElementById('adminCommunityList');
  if (!el) return;
  el.innerHTML = `<div class="loading"><div class="spinner"></div></div>`;
  try {
    const posts = await api('/forum/admin/all');
    if (!posts.length) { el.innerHTML = `<div class="empty"><div class="empty-icon">💬</div><p>No posts yet.</p></div>`; return; }
    el.innerHTML = posts.map(p => `
      <div style="display:flex;align-items:flex-start;justify-content:space-between;gap:12px;padding:16px 0;border-bottom:1px solid var(--border)">
        <div style="flex:1;min-width:0">
          <div style="display:flex;align-items:center;gap:8px;margin-bottom:4px">
            ${p.isHidden ? '<span class="badge badge-red">Hidden</span>' : '<span class="badge badge-green">Visible</span>'}
            <span style="font-weight:600">${p.title}</span>
          </div>
          <div style="font-size:.82rem;color:var(--muted)">
            by ${p.author?.fullName || 'Unknown'} · ${formatDate(p.createdAt)} · 💬 ${p.commentCount}
          </div>
          <div style="font-size:.85rem;color:var(--muted);margin-top:4px;overflow:hidden;white-space:nowrap;text-overflow:ellipsis">${p.content}</div>
        </div>
        <div style="display:flex;gap:6px;flex-shrink:0">
          ${p.isHidden
            ? `<button class="btn btn-secondary btn-sm" onclick="moderatePost(${p.id},'unhide')">Restore</button>`
            : `<button class="btn btn-secondary btn-sm" onclick="moderatePost(${p.id},'hide')">Hide</button>`}
          <button class="btn btn-danger btn-sm" onclick="adminDeletePost(${p.id})">Delete</button>
        </div>
      </div>`).join('');
  } catch (e) {
    el.innerHTML = `<div class="empty"><p style="color:var(--danger)">Failed to load posts.</p></div>`;
  }
}

async function moderatePost(postId, action) {
  try {
    await api(`/forum/${postId}/${action}`, 'POST');
    await loadAdminCommunity();
  } catch (e) { alert('Failed: ' + e.message); }
}

async function adminDeletePost(postId) {
  if (!confirm('Permanently delete this post?')) return;
  try {
    await api('/forum/' + postId, 'DELETE');
    await loadAdminCommunity();
  } catch (e) { alert('Failed: ' + e.message); }
}

// ── Utility ───────────────────────────────────────────────
function formatDate(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  const now = new Date();
  const diff = Math.floor((now - d) / 1000);
  if (diff < 60)    return 'just now';
  if (diff < 3600)  return Math.floor(diff/60) + 'm ago';
  if (diff < 86400) return Math.floor(diff/3600) + 'h ago';
  return d.toLocaleDateString('en-US', {month:'short', day:'numeric'});
}

// ── Wire into showPage ────────────────────────────────────
// Add these cases to the window.showPage override in dashboard.js:
//   if (name === 'community' && getUser().role !== 'ADMIN') loadCommunity();
//   if (name === 'community' && getUser().role === 'ADMIN')  loadAdminCommunity();