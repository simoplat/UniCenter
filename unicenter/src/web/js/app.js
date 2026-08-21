/**
 * UNICENTER - Client-Side Single Page Application Logic
 */

const API = {
  async get(endpoint, params = {}) {
    const query = new URLSearchParams(params).toString();
    const url = query ? `${endpoint}?${query}` : endpoint;
    const res = await fetch(url);
    return res.json();
  },

  async post(endpoint, data = {}) {
    const res = await fetch(endpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    return res.json();
  }
};

// Global App State
const state = {
  user: null,
  activeTab: 'dashboard',
  demoUsers: []
};

// UI Helpers
const UI = {
  toast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `<span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateY(10px)';
      setTimeout(() => toast.remove(), 300);
    }, 4000);
  },

  modal(title, bodyHtml, footerHtml = '') {
    const backdrop = document.getElementById('app-modal');
    if (!backdrop) return;

    document.getElementById('modal-title').textContent = title;
    document.getElementById('modal-body').innerHTML = bodyHtml;
    document.getElementById('modal-footer').innerHTML = footerHtml;

    backdrop.classList.add('active');
  },

  closeModal() {
    const backdrop = document.getElementById('app-modal');
    if (backdrop) backdrop.classList.remove('active');
  },

  formatDate(isoStr) {
    if (!isoStr) return '-';
    try {
      const d = new Date(isoStr);
      return d.toLocaleDateString('it-IT', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
    } catch {
      return isoStr;
    }
  },

  formatShortDate(isoStr) {
    if (!isoStr) return '-';
    try {
      const d = new Date(isoStr);
      return d.toLocaleDateString('it-IT', { day: '2-digit', month: '2-digit', year: 'numeric' });
    } catch {
      return isoStr;
    }
  }
};

// Initialize Application
document.addEventListener('DOMContentLoaded', async () => {
  setupGlobalEvents();
  await loadDemoUsers();
  await checkAuth();
});

function setupGlobalEvents() {
  document.getElementById('modal-close-btn')?.addEventListener('click', UI.closeModal);
  document.getElementById('app-modal')?.addEventListener('click', (e) => {
    if (e.target.id === 'app-modal') UI.closeModal();
  });
}

async function loadDemoUsers() {
  try {
    const res = await API.get('/api/auth/demo-users');
    if (res.success) {
      state.demoUsers = res.data;
      renderDemoSwitcher();
    }
  } catch (err) {
    console.error('Error loading demo users', err);
  }
}

function renderDemoSwitcher() {
  const container = document.getElementById('demo-switcher-container');
  if (!container) return;

  container.innerHTML = `
    <div class="demo-switcher">
      <label>Demo:</label>
      <button class="demo-btn" onclick="quickLogin('mario.rossi@studenti.it', 'pass123')">Studente (Mario)</button>
      <button class="demo-btn" onclick="quickLogin('simo.plata@studenti.it', 'pass123')">Studente (Simo)</button>
      <button class="demo-btn" onclick="quickLogin('mario.rossi@unicenter.it', 'pass123')">Prof. Rossi</button>
      <button class="demo-btn" onclick="quickLogin('admin@unicenter.it', 'admin123')">Admin</button>
    </div>
  `;
}

async function quickLogin(email, password) {
  const res = await API.post('/api/auth/login', { email, password });
  if (res.success) {
    state.user = res.data;
    state.activeTab = 'dashboard';
    UI.toast(`Benvenuto, ${res.data.nome}!`, 'success');
    renderApp();
  } else {
    UI.toast(res.error || 'Login fallito', 'error');
  }
}

async function checkAuth() {
  try {
    const res = await API.get('/api/auth/current');
    if (res.success && res.data.authenticated) {
      state.user = res.data;
    } else {
      state.user = null;
    }
    renderApp();
  } catch (err) {
    state.user = null;
    renderApp();
  }
}

// Main Render Function
function renderApp() {
  renderTopbarUser();
  if (!state.user) {
    renderGuestView();
  } else {
    renderAuthenticatedView();
  }
}

function renderTopbarUser() {
  const container = document.getElementById('topbar-user-section');
  if (!container) return;

  if (!state.user) {
    container.innerHTML = `
      <button class="btn btn-secondary btn-sm" onclick="switchGuestTab('login')">Accedi</button>
      <button class="btn btn-primary btn-sm" onclick="switchGuestTab('immatricolazione')">Immatricolati</button>
    `;
  } else {
    const initials = (state.user.nome?.[0] || 'U') + (state.user.cognome?.[0] || '');
    container.innerHTML = `
      <div class="user-pill">
        <div class="user-avatar">${initials}</div>
        <div class="user-info">
          <span class="user-name">${state.user.nome} ${state.user.cognome}</span>
          <span class="user-role">${state.user.ruolo}</span>
        </div>
      </div>
      <button class="btn btn-secondary btn-sm" onclick="logout()" title="Disconnetti">Esci</button>
    `;
  }
}

async function logout() {
  await API.post('/api/auth/logout');
  state.user = null;
  state.activeTab = 'dashboard';
  UI.toast('Disconnessione effettuata', 'info');
  renderApp();
}

// ==========================================
// GUEST VIEW (HERO, LOGIN, IMMATRICOLAZIONE)
// ==========================================
let guestActiveTab = 'welcome';

function switchGuestTab(tab) {
  guestActiveTab = tab;
  renderGuestView();
}

async function renderGuestView() {
  const main = document.getElementById('main-content');
  if (!main) return;

  let contentHtml = '';

  if (guestActiveTab === 'welcome') {
    contentHtml = `
      <div class="auth-hero">
        <div class="auth-hero-badge">UniCenter v1.0 • Piattaforma di Gestione Universitaria</div>
        <h1 class="auth-hero-title">Semplice. Veloce. Completo.</h1>
        <p class="auth-hero-desc">Accedi al tuo portale studente, docente o amministratore per gestire appelli, esami, carriere e piani di studio in tempo reale.</p>
        
        <div class="auth-cards-grid">
          <div class="card-panel">
            <div class="card-panel-header">
              <h2 class="card-panel-title">Accedi a UniCenter</h2>
            </div>
            <form onsubmit="handleLoginForm(event)">
              <div class="form-group">
                <label class="form-label">Email Istituzionale</label>
                <input type="email" id="login-email" class="form-input" placeholder="es. mario.rossi@studenti.it" required />
              </div>
              <div class="form-group">
                <label class="form-label">Password</label>
                <input type="password" id="login-password" class="form-input" placeholder="••••••••" required />
              </div>
              <button type="submit" class="btn btn-primary" style="width: 100%;">Accedi al Portale</button>
            </form>
          </div>

          <div class="card-panel" style="display: flex; flex-direction: column; justify-content: space-between;">
            <div>
              <div class="card-panel-header">
                <h2 class="card-panel-title">Nuovo Studente?</h2>
              </div>
              <p style="color: var(--text-secondary); margin-bottom: 1.5rem; line-height: 1.6;">
                Immatricolati online per ottenere la tua matricola, il libretto universitario e il piano di studi per l'anno accademico in corso.
              </p>
            </div>
            <button class="btn btn-secondary" style="width: 100%;" onclick="switchGuestTab('immatricolazione')">
              Avvia Immatricolazione Online &rarr;
            </button>
          </div>
        </div>
      </div>
    `;
  } else if (guestActiveTab === 'login') {
    contentHtml = `
      <div class="auth-hero" style="max-width: 480px;">
        <h1 class="auth-hero-title" style="font-size: 2rem;">Accedi a UniCenter</h1>
        <p class="auth-hero-desc">Inserisci le tue credenziali accademiche</p>
        <div class="card-panel" style="width: 100%; text-align: left;">
          <form onsubmit="handleLoginForm(event)">
            <div class="form-group">
              <label class="form-label">Email Istituzionale</label>
              <input type="email" id="login-email" class="form-input" placeholder="es. mario.rossi@studenti.it" required />
            </div>
            <div class="form-group">
              <label class="form-label">Password</label>
              <input type="password" id="login-password" class="form-input" placeholder="••••••••" required />
            </div>
            <button type="submit" class="btn btn-primary" style="width: 100%; margin-bottom: 0.75rem;">Accedi</button>
            <button type="button" class="btn btn-secondary" style="width: 100%;" onclick="switchGuestTab('welcome')">Annulla</button>
          </form>
        </div>
      </div>
    `;
  } else if (guestActiveTab === 'immatricolazione') {
    const statusRes = await API.get('/api/immatricolazione/status');
    const corsiRes = await API.get('/api/immatricolazione/corsi');

    const isOpen = statusRes.data?.aperta;
    const msg = statusRes.data?.messaggio || '';
    const corsi = corsiRes.data || [];

    contentHtml = `
      <div class="auth-hero" style="max-width: 680px;">
        <h1 class="auth-hero-title" style="font-size: 2.2rem;">Immatricolazione Nuovo Studente</h1>
        <p class="auth-hero-desc">Registrati a UniCenter e ottieni la tua matricola accademica</p>
        
        <div class="card-panel" style="width: 100%; text-align: left;">
          <div style="margin-bottom: 1.5rem; padding: 0.75rem 1rem; border-radius: var(--radius-md); background: ${isOpen ? 'var(--accent-success-bg)' : 'var(--accent-danger-bg)'}; border: 1px solid ${isOpen ? 'rgba(16,185,129,0.3)' : 'rgba(239,68,68,0.3)'}; color: ${isOpen ? 'var(--accent-success)' : 'var(--accent-danger)'}; font-weight: 500;">
            ${msg}
          </div>

          ${isOpen ? `
            <form onsubmit="handleImmatricolazioneForm(event)">
              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">Nome</label>
                  <input type="text" id="imm-nome" class="form-input" placeholder="Mario" required />
                </div>
                <div class="form-group">
                  <label class="form-label">Cognome</label>
                  <input type="text" id="imm-cognome" class="form-input" placeholder="Rossi" required />
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">Email Personale</label>
                  <input type="email" id="imm-email" class="form-input" placeholder="mario.rossi@email.it" required />
                </div>
                <div class="form-group">
                  <label class="form-label">Password (min. 4 caratteri)</label>
                  <input type="password" id="imm-password" minlength="4" class="form-input" placeholder="••••••••" required />
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">Codice Fiscale</label>
                  <input type="text" id="imm-cf" class="form-input" placeholder="RSSMRA80A01H501U" required />
                </div>
                <div class="form-group">
                  <label class="form-label">Corso di Laurea</label>
                  <select id="imm-corso" class="form-select" required>
                    <option value="">Seleziona un corso attivo...</option>
                    ${corsi.map(c => `<option value="${c.nome}">${c.nome} (${c.tipologia})</option>`).join('')}
                  </select>
                </div>
              </div>

              <div style="display: flex; gap: 1rem; margin-top: 1.5rem;">
                <button type="submit" class="btn btn-primary" style="flex: 1;">Completa Immatricolazione</button>
                <button type="button" class="btn btn-secondary" onclick="switchGuestTab('welcome')">Annulla</button>
              </div>
            </form>
          ` : `
            <p style="color: var(--text-secondary); margin-bottom: 1.5rem;">Le immatricolazioni sono attualmente chiuse per la finestra temporale in corso.</p>
            <button type="button" class="btn btn-secondary" style="width: 100%;" onclick="switchGuestTab('welcome')">Torna alla Home</button>
          `}
        </div>
      </div>
    `;
  }

  main.innerHTML = contentHtml;
}

async function handleLoginForm(e) {
  e.preventDefault();
  const email = document.getElementById('login-email').value.trim();
  const password = document.getElementById('login-password').value;

  const res = await API.post('/api/auth/login', { email, password });
  if (res.success) {
    state.user = res.data;
    state.activeTab = 'dashboard';
    UI.toast(`Benvenuto, ${res.data.nome}!`, 'success');
    renderApp();
  } else {
    UI.toast(res.error || 'Credenziali non valide', 'error');
  }
}

async function handleImmatricolazioneForm(e) {
  e.preventDefault();
  const data = {
    nome: document.getElementById('imm-nome').value.trim(),
    cognome: document.getElementById('imm-cognome').value.trim(),
    email: document.getElementById('imm-email').value.trim(),
    password: document.getElementById('imm-password').value,
    codiceFiscale: document.getElementById('imm-cf').value.trim().toUpperCase(),
    corso: document.getElementById('imm-corso').value
  };

  const res = await API.post('/api/immatricolazione', data);
  if (res.success) {
    UI.modal(
      'Immatricolazione Completata!',
      `
        <div style="text-align: center; padding: 1rem 0;">
          <div style="font-size: 3rem; margin-bottom: 1rem;">🎉</div>
          <h3 style="color: #fff; margin-bottom: 0.5rem;">Benvenuto in UniCenter!</h3>
          <p style="color: var(--text-secondary); margin-bottom: 1.5rem;">La tua immatricolazione è avvenuta con successo.</p>
          <div style="background: var(--bg-surface); padding: 1rem; border-radius: var(--radius-md); border: 1px solid var(--border-subtle); text-align: left; font-size: 0.9rem;">
            <div style="margin-bottom: 0.4rem;"><strong>Matricola:</strong> <span style="color: var(--accent-primary); font-family: var(--font-mono);">${res.data.matricola}</span></div>
            <div style="margin-bottom: 0.4rem;"><strong>Email:</strong> ${res.data.email}</div>
            <div style="margin-bottom: 0.4rem;"><strong>Corso:</strong> ${res.data.corso}</div>
            <div style="margin-bottom: 0.4rem;"><strong>Tasse da Pagare:</strong> ${res.data.tasse} €</div>
            <div><strong>Codice Fiscale:</strong> ${res.data.codiceFiscale}</div>
          </div>
        </div>
      `,
      `<button class="btn btn-primary" onclick="UI.closeModal(); switchGuestTab('login')">Vai al Login</button>`
    );
  } else {
    UI.toast(res.error || 'Immatricolazione fallita', 'error');
  }
}

// ==========================================
// AUTHENTICATED MAIN VIEW (STUDENT, PROF, ADMIN)
// ==========================================
function renderAuthenticatedView() {
  const main = document.getElementById('main-content');
  if (!main) return;

  const role = state.user.ruolo;

  let navItemsHtml = '';
  if (role === 'studente') {
    navItemsHtml = `
      <div class="nav-section-title">Area Studente</div>
      <a class="nav-item ${state.activeTab === 'dashboard' ? 'active' : ''}" onclick="switchTab('dashboard')">
        <span>📊 Dashboard</span>
      </a>
      <a class="nav-item ${state.activeTab === 'appelli-disponibili' ? 'active' : ''}" onclick="switchTab('appelli-disponibili')">
        <span>📝 Prenota Appelli</span>
      </a>
      <a class="nav-item ${state.activeTab === 'appelli-prenotati' ? 'active' : ''}" onclick="switchTab('appelli-prenotati')">
        <span>📅 Appelli Prenotati</span>
      </a>
      <a class="nav-item ${state.activeTab === 'esiti' ? 'active' : ''}" onclick="switchTab('esiti')">
        <span>🎓 Esiti & Voti</span>
      </a>
      <a class="nav-item ${state.activeTab === 'libretto' ? 'active' : ''}" onclick="switchTab('libretto')">
        <span>📖 Libretto Universitario</span>
      </a>
      <a class="nav-item ${state.activeTab === 'piano-studi' ? 'active' : ''}" onclick="switchTab('piano-studi')">
        <span>📋 Piano di Studi</span>
      </a>
      <a class="nav-item ${state.activeTab === 'tasse' ? 'active' : ''}" onclick="switchTab('tasse')">
        <span>💳 Tasse Universitarie</span>
      </a>
      <a class="nav-item ${state.activeTab === 'notifiche' ? 'active' : ''}" onclick="switchTab('notifiche')">
        <span>🔔 Notifiche</span>
      </a>
    `;
  } else if (role === 'professore') {
    navItemsHtml = `
      <div class="nav-section-title">Area Docente</div>
      <a class="nav-item ${state.activeTab === 'dashboard' ? 'active' : ''}" onclick="switchTab('dashboard')">
        <span>📊 Dashboard</span>
      </a>
      <a class="nav-item ${state.activeTab === 'crea-appello' ? 'active' : ''}" onclick="switchTab('crea-appello')">
        <span>➕ Crea Nuovo Appello</span>
      </a>
      <a class="nav-item ${state.activeTab === 'gestione-appelli' ? 'active' : ''}" onclick="switchTab('gestione-appelli')">
        <span>📅 I Miei Appelli</span>
      </a>
      <a class="nav-item ${state.activeTab === 'pubblica-esito' ? 'active' : ''}" onclick="switchTab('pubblica-esito')">
        <span>✍️ Pubblica Esito Esame</span>
      </a>
      <a class="nav-item ${state.activeTab === 'esiti-pubblicati' ? 'active' : ''}" onclick="switchTab('esiti-pubblicati')">
        <span>📑 Esiti Pubblicati</span>
      </a>
      <a class="nav-item ${state.activeTab === 'comunicazioni' ? 'active' : ''}" onclick="switchTab('comunicazioni')">
        <span>📢 Avvisi & Comunicazioni</span>
      </a>
    `;
  } else if (role === 'amministratore') {
    navItemsHtml = `
      <div class="nav-section-title">Area Amministratore</div>
      <a class="nav-item ${state.activeTab === 'dashboard' ? 'active' : ''}" onclick="switchTab('dashboard')">
        <span>📊 Panoramica Sistema</span>
      </a>
      <a class="nav-item ${state.activeTab === 'corsi' ? 'active' : ''}" onclick="switchTab('corsi')">
        <span>🏛️ Corsi di Laurea</span>
      </a>
      <a class="nav-item ${state.activeTab === 'materie' ? 'active' : ''}" onclick="switchTab('materie')">
        <span>📚 Gestione Materie</span>
      </a>
      <a class="nav-item ${state.activeTab === 'preapprovate' ? 'active' : ''}" onclick="switchTab('preapprovate')">
        <span>⭐ Materie Pre-Approvate</span>
      </a>
      <a class="nav-item ${state.activeTab === 'piani-attesa' ? 'active' : ''}" onclick="switchTab('piani-attesa')">
        <span>⏳ Valutazione Piani di Studio</span>
      </a>
    `;
  }

  main.innerHTML = `
    <div class="main-wrapper">
      <aside class="sidebar">
        ${navItemsHtml}
      </aside>
      <section class="content-area" id="tab-content">
        <div style="text-align: center; padding: 3rem;">Caricamento sezione...</div>
      </section>
    </div>
  `;

  loadActiveTabContent();
}

function switchTab(tabName) {
  state.activeTab = tabName;
  document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
  renderAuthenticatedView();
}

async function loadActiveTabContent() {
  const container = document.getElementById('tab-content');
  if (!container) return;

  const role = state.user.ruolo;
  const tab = state.activeTab;

  try {
    if (role === 'studente') {
      if (tab === 'dashboard') await renderStudentDashboard(container);
      else if (tab === 'appelli-disponibili') await renderStudentAppelliDisponibili(container);
      else if (tab === 'appelli-prenotati') await renderStudentAppelliPrenotati(container);
      else if (tab === 'esiti') await renderStudentEsiti(container);
      else if (tab === 'libretto') await renderStudentLibretto(container);
      else if (tab === 'piano-studi') await renderStudentPianoStudi(container);
      else if (tab === 'tasse') await renderStudentTasse(container);
      else if (tab === 'notifiche') await renderStudentNotifiche(container);
    } else if (role === 'professore') {
      if (tab === 'dashboard') await renderProfessorDashboard(container);
      else if (tab === 'crea-appello') await renderProfessorCreaAppello(container);
      else if (tab === 'gestione-appelli') await renderProfessorGestioneAppelli(container);
      else if (tab === 'pubblica-esito') await renderProfessorPubblicaEsito(container);
      else if (tab === 'esiti-pubblicati') await renderProfessorEsitiPubblicati(container);
      else if (tab === 'comunicazioni') await renderProfessorComunicazioni(container);
    } else if (role === 'amministratore') {
      if (tab === 'dashboard') await renderAdminDashboard(container);
      else if (tab === 'corsi') await renderAdminCorsi(container);
      else if (tab === 'materie') await renderAdminMaterie(container);
      else if (tab === 'preapprovate') await renderAdminPreapprovate(container);
      else if (tab === 'piani-attesa') await renderAdminPianiAttesa(container);
    }
  } catch (err) {
    container.innerHTML = `<div class="card-panel"><p style="color: var(--accent-danger);">Errore nel caricamento dei dati: ${err.message}</p></div>`;
  }
}

// ==========================================
// STUDENT TABS IMPLEMENTATIONS
// ==========================================
async function renderStudentDashboard(container) {
  const res = await API.get('/api/student/dashboard');
  const d = res.data || {};

  container.innerHTML = `
    <div class="page-header">
      <div>
        <h1 class="page-title">Bentornato, ${state.user.nome}!</h1>
        <p class="page-subtitle">Matricola: <strong>${d.matricola}</strong> • Corso: <strong>${d.corso}</strong></p>
      </div>
    </div>

    <div class="grid-cards">
      <div class="stat-card">
        <div class="stat-icon-wrapper emerald">📖</div>
        <div class="stat-content">
          <span class="stat-label">CFU Acquisiti</span>
          <span class="stat-value">${d.cfu} CFU</span>
          <span class="stat-desc">${d.esamiSuperati} esami superati</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon-wrapper indigo">🎯</div>
        <div class="stat-content">
          <span class="stat-label">Media Ponderata</span>
          <span class="stat-value">${d.media ? d.media.toFixed(2) : '-'} / 30</span>
          <span class="stat-desc">Carriera universitaria</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon-wrapper ${d.tassePagate ? 'emerald' : 'amber'}">💳</div>
        <div class="stat-content">
          <span class="stat-label">Tasse Universitarie</span>
          <span class="stat-value">${d.tassePagate ? 'Saldate' : `${d.tasseImporto} €`}</span>
          <span class="stat-desc">${d.tassePagate ? 'Regolare' : 'Da pagare'}</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon-wrapper purple">⏳</div>
        <div class="stat-content">
          <span class="stat-label">Esiti in Attesa</span>
          <span class="stat-value">${d.esitiPendentiCount}</span>
          <span class="stat-desc">Da confermare / rifiutare</span>
        </div>
      </div>
    </div>

    <div class="card-panel">
      <div class="card-panel-header">
        <h3 class="card-panel-title">Azioni Rapide</h3>
      </div>
      <div style="display: flex; gap: 1rem; flex-wrap: wrap;">
        <button class="btn btn-primary" onclick="switchTab('appelli-disponibili')">Prenota un Appello d'Esame</button>
        <button class="btn btn-secondary" onclick="switchTab('esiti')">Gestisci Esiti (${d.esitiPendentiCount})</button>
        <button class="btn btn-secondary" onclick="switchTab('libretto')">Visualizza Libretto</button>
        <button class="btn btn-secondary" onclick="switchTab('piano-studi')">Compila Piano di Studi</button>
      </div>
    </div>
  `;
}

async function renderStudentAppelliDisponibili(container) {
  const res = await API.get('/api/student/appelli-disponibili');
  const list = res.data || [];

  container.innerHTML = `
    <div class="page-header">
      <div>
        <h1 class="page-title">Iscrizione Appelli d'Esame</h1>
        <p class="page-subtitle">Visualizza e prenota gli appelli disponibili per il tuo corso di laurea e piano di studi</p>
      </div>
    </div>

    <div class="card-panel">
      <div class="card-panel-header">
        <h3 class="card-panel-title">Appelli Prenotabili (${list.length})</h3>
      </div>
      ${list.length === 0 ? '<p style="color: var(--text-muted);">Nessun appello disponibile al momento (verifica tasse o piano di studi).</p>' : `
        <div class="table-responsive">
          <table class="data-table">
            <thead>
              <tr>
                <th>Codice</th>
                <th>Materia</th>
                <th>Data & Ora</th>
                <th>Aula</th>
                <th>Vincolo</th>
                <th>Posti Disponibili</th>
                <th>Termine Iscrizione</th>
                <th>Azione</th>
              </tr>
            </thead>
            <tbody>
              ${list.map(a => {
                const postiTot = a.posti || 0;
                const iscritti = a.iscrittiCount || 0;
                const postiDisp = Math.max(0, postiTot - iscritti);
                const esaurito = postiDisp === 0;

                return `
                  <tr>
                    <td><span style="font-family: var(--font-mono); color: var(--accent-primary); font-weight: 600;">${a.codiceAppello}</span></td>
                    <td><strong>${a.nomeMateria}</strong> (${a.cfu} CFU)</td>
                    <td>${UI.formatDate(a.dataOra)}</td>
                    <td>${a.aula}</td>
                    <td><span class="badge ${a.vincolo && a.vincolo !== 'A-Z' ? 'badge-warning' : 'badge-info'}">${a.vincolo || 'A-Z'}</span></td>
                    <td>
                      <span class="badge ${postiDisp > 0 ? 'badge-success' : 'badge-danger'}">
                        ${postiDisp} disponibili
                      </span>
                      <span style="font-size: 0.75rem; color: var(--text-muted); margin-left: 0.35rem;">(${postiTot} tot)</span>
                    </td>
                    <td>${UI.formatShortDate(a.termineIscrizione)}</td>
                    <td>
                      ${esaurito 
                        ? '<button class="btn btn-secondary btn-sm" disabled>Esaurito</button>' 
                        : `<button class="btn btn-primary btn-sm" onclick="prenotaAppello('${a.codiceAppello}')">Prenota</button>`}
                    </td>
                  </tr>
                `;
              }).join('')}
            </tbody>
          </table>
        </div>
      `}
    </div>
  `;
}

async function prenotaAppello(codiceAppello) {
  const res = await API.post('/api/student/prenota-appello', { codiceAppello });
  if (res.success) {
    UI.toast('Prenotazione effettuata con successo!', 'success');
    loadActiveTabContent();
  } else {
    UI.toast(res.error || 'Errore durante la prenotazione', 'error');
  }
}

async function renderStudentAppelliPrenotati(container) {
  const res = await API.get('/api/student/appelli-prenotati');
  const list = res.data || [];

  container.innerHTML = `
    <div class="page-header">
      <div>
        <h1 class="page-title">I Tuoi Appelli Prenotati</h1>
        <p class="page-subtitle">Gestisci le tue iscrizioni attive agli esami universitari</p>
      </div>
    </div>

    <div class="card-panel">
      <div class="card-panel-header">
        <h3 class="card-panel-title">Prenotazioni Attive (${list.length})</h3>
      </div>
      ${list.length === 0 ? '<p style="color: var(--text-muted);">Non sei iscritto a nessun appello.</p>' : `
        <div class="table-responsive">
          <table class="data-table">
            <thead>
              <tr>
                <th>Codice Appello</th>
                <th>Materia</th>
                <th>Data & Ora</th>
                <th>Aula</th>
                <th>Termine Iscrizione</th>
                <th>Azione</th>
              </tr>
            </thead>
            <tbody>
              ${list.map(a => `
                <tr>
                  <td><span style="font-family: var(--font-mono); color: var(--accent-primary); font-weight: 600;">${a.codiceAppello}</span></td>
                  <td><strong>${a.nomeMateria}</strong> (${a.cfu} CFU)</td>
                  <td>${UI.formatDate(a.dataOra)}</td>
                  <td>${a.aula}</td>
                  <td>${UI.formatShortDate(a.termineIscrizione)}</td>
                  <td>
                    <button class="btn btn-danger btn-sm" onclick="annullaPrenotazione('${a.codiceAppello}')">Annulla</button>
                  </td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
      `}
    </div>
  `;
}

async function annullaPrenotazione(codiceAppello) {
  if (!confirm(`Sei sicuro di voler annullare la prenotazione per l'appello ${codiceAppello}?`)) return;
  const res = await API.post('/api/student/disiscrivi-appello', { codiceAppello });
  if (res.success) {
    UI.toast('Prenotazione annullata', 'info');
    loadActiveTabContent();
  } else {
    UI.toast(res.error || 'Impossibile annullare', 'error');
  }
}

async function renderStudentEsiti(container) {
  const res = await API.get('/api/student/esiti');
  const d = res.data || { pendenti: [], storico: [] };

  container.innerHTML = `
    <div class="page-header">
      <div>
        <h1 class="page-title">Gestione Esiti Esami (UC3)</h1>
        <p class="page-subtitle">Accetta o rifiuta i voti pubblicati dai docenti entro la scadenza</p>
      </div>
    </div>

    <div class="card-panel">
      <div class="card-panel-header">
        <h3 class="card-panel-title">Esiti in Attesa di Scelta (${d.pendenti.length})</h3>
      </div>
      ${d.pendenti.length === 0 ? '<p style="color: var(--text-muted);">Non hai esiti in attesa di conferma.</p>' : `
        <div class="table-responsive">
          <table class="data-table">
            <thead>
              <tr>
                <th>ID Verbale</th>
                <th>Materia</th>
                <th>Voto</th>
                <th>Stato</th>
                <th>Scadenza Conferma</th>
                <th>Decisione</th>
              </tr>
            </thead>
            <tbody>
              ${d.pendenti.map(e => `
                <tr>
                  <td><span style="font-family: var(--font-mono); font-weight: 600;">${e.idVerbale}</span></td>
                  <td><strong>${e.nomeMateria}</strong></td>
                  <td><span style="font-size: 1.1rem; font-weight: 700; color: var(--accent-success);">${e.voto}${e.lode ? ' e Lode' : ''}</span>/30</td>
                  <td><span class="badge badge-warning">${e.stato}</span></td>
                  <td>${UI.formatDate(e.scadenza)}</td>
                  <td>
                    <div style="display: flex; gap: 0.5rem;">
                      <button class="btn btn-success btn-sm" onclick="accettaVoto('${e.idVerbale}')">Accetta</button>
                      <button class="btn btn-danger btn-sm" onclick="rifiutaVoto('${e.idVerbale}')">Rifiuta</button>
                    </div>
                  </td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
      `}
    </div>

    <div class="card-panel">
      <div class="card-panel-header">
        <h3 class="card-panel-title">Storico Completo Esiti (${d.storico.length})</h3>
      </div>
      ${d.storico.length === 0 ? '<p style="color: var(--text-muted);">Nessun esito presente nello storico.</p>' : `
        <div class="table-responsive">
          <table class="data-table">
            <thead>
              <tr>
                <th>ID Verbale</th>
                <th>Materia</th>
                <th>Voto</th>
                <th>Stato</th>
                <th>Data Registrazione</th>
              </tr>
            </thead>
            <tbody>
              ${d.storico.map(e => `
                <tr>
                  <td><span style="font-family: var(--font-mono);">${e.idVerbale}</span></td>
                  <td><strong>${e.nomeMateria}</strong></td>
                  <td>${e.voto}${e.lode ? ' e Lode' : ''}/30</td>
                  <td>
                    <span class="badge ${e.stato === 'Approvato' ? 'badge-success' : e.stato === 'Bocciato' || e.stato === 'Rifiutato' ? 'badge-danger' : 'badge-warning'}">
                      ${e.stato}
                    </span>
                  </td>
                  <td>${UI.formatDate(e.dataRegistrazione)}</td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
      `}
    </div>
  `;
}

async function accettaVoto(idVerbale) {
  const res = await API.post('/api/student/accetta-voto', { idVerbale });
  if (res.success) {
    UI.toast('Voto accettato e verbalizzato nel libretto!', 'success');
    loadActiveTabContent();
  } else {
    UI.toast(res.error || 'Errore accettazione voto', 'error');
  }
}

async function rifiutaVoto(idVerbale) {
  if (!confirm('Sei sicuro di voler rifiutare questo voto? Potrai ripetere l\'esame in un appello futuro.')) return;
  const res = await API.post('/api/student/rifiuta-voto', { idVerbale });
  if (res.success) {
    UI.toast('Voto rifiutato.', 'info');
    loadActiveTabContent();
  } else {
    UI.toast(res.error || 'Errore rifiuto voto', 'error');
  }
}

async function renderStudentLibretto(container) {
  const res = await API.get('/api/student/libretto');
  const d = res.data || { obbligatorie: [], aScelta: [] };

  // Raggruppa materie obbligatorie per anno di corso
  const anniMap = {};
  (d.obbligatorie || []).forEach(m => {
    const anno = m.anno || 1;
    if (!anniMap[anno]) anniMap[anno] = [];
    anniMap[anno].push(m);
  });
  const anniSorted = Object.keys(anniMap).map(Number).sort((a, b) => a - b);

  container.innerHTML = `
    <div class="page-header">
      <div>
        <h1 class="page-title">Il Tuo Libretto Universitario</h1>
        <p class="page-subtitle">Riepilogo carriera e piano degli esami suddivisi per anno accademico</p>
      </div>
    </div>

    <div class="grid-cards">
      <div class="stat-card">
        <div class="stat-icon-wrapper emerald">🎓</div>
        <div class="stat-content">
          <span class="stat-label">Esami Superati</span>
          <span class="stat-value">${d.esamiSuperati ?? 0} / ${d.esamiTotali ?? 0}</span>
          <span class="stat-desc">Avanzamento carriera</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon-wrapper indigo">📊</div>
        <div class="stat-content">
          <span class="stat-label">CFU Totali</span>
          <span class="stat-value">${d.cfuAcquisiti ?? 0} / ${d.cfuTotali ?? 0} CFU</span>
          <span class="stat-desc">Crediti formativi</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon-wrapper amber">⭐</div>
        <div class="stat-content">
          <span class="stat-label">Media Ponderata</span>
          <span class="stat-value">${d.mediaPonderata && d.mediaPonderata > 0 ? d.mediaPonderata.toFixed(2) : '-'} / 30</span>
          <span class="stat-desc">Media voti registrati</span>
        </div>
      </div>
    </div>

    ${anniSorted.map(anno => {
      const materieAnno = anniMap[anno] || [];
      const superatiAnno = materieAnno.filter(m => m.superato).length;
      const cfuAnno = materieAnno.reduce((sum, m) => sum + (m.cfu || 0), 0);
      const cfuAcquisitiAnno = materieAnno.filter(m => m.superato).reduce((sum, m) => sum + (m.cfu || 0), 0);

      return `
        <div class="card-panel" style="margin-bottom: 1.5rem;">
          <div class="card-panel-header" style="border-bottom: 1px solid var(--border-subtle); padding-bottom: 0.85rem; margin-bottom: 1rem;">
            <div style="display: flex; align-items: center; gap: 0.75rem;">
              <span class="badge badge-info" style="font-size: 0.9rem; font-weight: 700; padding: 0.35rem 0.85rem;">
                ${anno}° ANNO DI CORSO
              </span>
              <span style="color: var(--text-secondary); font-size: 0.9rem; font-weight: 600;">
                Materie Obbligatorie
              </span>
            </div>
            <div style="display: flex; gap: 1rem; align-items: center; font-size: 0.82rem; color: var(--text-muted);">
              <span>Esami: <strong style="color: var(--text-primary);">${superatiAnno} / ${materieAnno.length}</strong> superati</span>
              <span>CFU: <strong style="color: var(--accent-primary);">${cfuAcquisitiAnno} / ${cfuAnno}</strong></span>
            </div>
          </div>
          <div class="table-responsive">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Codice</th>
                  <th>Insegnamento</th>
                  <th>CFU</th>
                  <th>Stato Esame</th>
                  <th>Esito / Voto</th>
                  <th>Data Registrazione</th>
                </tr>
              </thead>
              <tbody>
                ${materieAnno.map(m => `
                  <tr>
                    <td><span style="font-family: var(--font-mono); color: var(--accent-primary); font-weight: 600;">${m.codice}</span></td>
                    <td><strong>${m.nome}</strong></td>
                    <td>${m.cfu} CFU</td>
                    <td>
                      ${m.superato 
                        ? '<span class="badge badge-success">SUPERATO</span>' 
                        : '<span class="badge badge-warning">DA SOSTENERE</span>'}
                    </td>
                    <td>
                      ${m.superato ? `<strong style="color: var(--accent-success); font-size: 1rem;">${m.voto}${m.lode ? ' e Lode' : ''}</strong> / 30` : '<span style="color: var(--text-muted);">-</span>'}
                    </td>
                    <td>${UI.formatShortDate(m.dataRegistrazione)}</td>
                  </tr>
                `).join('')}
              </tbody>
            </table>
          </div>
        </div>
      `;
    }).join('')}

    <div class="card-panel">
      <div class="card-panel-header" style="border-bottom: 1px solid var(--border-subtle); padding-bottom: 0.85rem; margin-bottom: 1rem;">
        <div style="display: flex; align-items: center; gap: 0.75rem;">
          <span class="badge badge-purple" style="font-size: 0.9rem; font-weight: 700; padding: 0.35rem 0.85rem;">
            MATERIE A SCELTA
          </span>
          <span style="color: var(--text-secondary); font-size: 0.9rem; font-weight: 600;">
            Insegnamenti del Piano di Studi Individuale
          </span>
        </div>
      </div>
      ${(d.aScelta || []).length === 0 ? '<p style="color: var(--text-muted); padding: 0.5rem 0;">Nessuna materia a scelta inserita nel piano di studi.</p>' : `
        <div class="table-responsive">
          <table class="data-table">
            <thead>
              <tr>
                <th>Codice</th>
                <th>Insegnamento</th>
                <th>CFU</th>
                <th>Stato Esame</th>
                <th>Esito / Voto</th>
                <th>Data Registrazione</th>
              </tr>
            </thead>
            <tbody>
              ${d.aScelta.map(m => `
                <tr>
                  <td><span style="font-family: var(--font-mono); color: var(--accent-primary); font-weight: 600;">${m.codice}</span></td>
                  <td><strong>${m.nome}</strong></td>
                  <td>${m.cfu} CFU</td>
                  <td>
                    ${m.superato 
                      ? '<span class="badge badge-success">SUPERATO</span>' 
                      : '<span class="badge badge-warning">DA SOSTENERE</span>'}
                  </td>
                  <td>
                    ${m.superato ? `<strong style="color: var(--accent-success); font-size: 1rem;">${m.voto}${m.lode ? ' e Lode' : ''}</strong> / 30` : '<span style="color: var(--text-muted);">-</span>'}
                  </td>
                  <td>${UI.formatShortDate(m.dataRegistrazione)}</td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
      `}
    </div>
  `;
}

async function renderStudentPianoStudi(container) {
  const res = await API.get('/api/student/piano-studi');
  const d = res.data || { obbligatorie: [], aSceltaAttuali: [], materieDisponibili: [], statoPiano: '' };

  const currentSelectedCodes = (d.aSceltaAttuali || []).map(m => m.codice);

  container.innerHTML = `
    <div class="page-header">
      <div>
        <h1 class="page-title">Compilazione Piano di Studi (UC9)</h1>
        <p class="page-subtitle">Seleziona le materie a scelta (almeno 12 CFU complessivi). Le materie pre-approvate godono di approvazione automatica.</p>
      </div>
      <div>
        <span class="badge ${d.statoPiano === 'Approvato' || d.statoPiano === 'Registrato' ? 'badge-success' : d.statoPiano === 'In Attesa' ? 'badge-warning' : 'badge-purple'}" style="font-size: 0.85rem; padding: 0.4rem 0.8rem;">
          Stato: ${d.statoPiano}
        </span>
      </div>
    </div>

    <div class="card-panel">
      <div class="card-panel-header">
        <h3 class="card-panel-title">1. Materie Obbligatorie del Manifesto (${d.obbligatorie.length})</h3>
      </div>
      <div style="display: flex; flex-wrap: wrap; gap: 0.5rem;">
        ${d.obbligatorie.map(m => `
          <div style="background: var(--bg-surface); border: 1px solid var(--border-subtle); padding: 0.4rem 0.75rem; border-radius: var(--radius-md); font-size: 0.85rem;">
            <strong>${m.codice}</strong> - ${m.nome} <span style="color: var(--text-muted);">(${m.cfu} CFU)</span>
          </div>
        `).join('')}
      </div>
    </div>

    <div class="card-panel">
      <div class="card-panel-header">
        <h3 class="card-panel-title">2. Seleziona le Materie a Scelta (Minimo 12 CFU)</h3>
      </div>
      <form onsubmit="handleSalvaPianoStudi(event)">
        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 0.75rem; margin-bottom: 1.5rem;">
          ${(d.materieDisponibili || []).map(m => {
            const isChecked = currentSelectedCodes.includes(m.codice);
            const isVerbalizzata = (d.verbalizzate || []).includes(m.codice);
            return `
              <label style="display: flex; align-items: flex-start; gap: 0.75rem; background: var(--bg-surface); padding: 0.75rem 1rem; border-radius: var(--radius-md); border: 1px solid var(--border-medium); cursor: ${isVerbalizzata ? 'not-allowed' : 'pointer'};">
                <input type="checkbox" name="piano_materia" value="${m.codice}" data-cfu="${m.cfu}" ${isChecked ? 'checked' : ''} ${isVerbalizzata ? 'disabled checked' : ''} onchange="updateCfuCounter()" style="margin-top: 0.2rem;" />
                <div>
                  <div style="font-weight: 600; color: #fff;">${m.nome}</div>
                  <div style="font-size: 0.8rem; color: var(--text-muted); display: flex; gap: 0.4rem; align-items: center; margin-top: 0.2rem;">
                    <span>${m.codice} • ${m.cfu} CFU</span>
                    ${m.preApprovata ? '<span class="badge badge-success" style="font-size: 0.65rem;">Pre-Approvata</span>' : '<span class="badge badge-warning" style="font-size: 0.65rem;">Valutazione Docente</span>'}
                    ${isVerbalizzata ? '<span class="badge badge-info" style="font-size: 0.65rem;">Verbalizzata</span>' : ''}
                  </div>
                </div>
              </label>
            `;
          }).join('')}
        </div>

        <div style="background: var(--bg-surface-elevated); padding: 1rem 1.25rem; border-radius: var(--radius-md); border: 1px solid var(--border-subtle); display: flex; justify-content: space-between; align-items: center;">
          <div>
            Totale CFU a Scelta Selezionati: <strong id="totale-cfu-label" style="color: var(--accent-primary); font-size: 1.1rem;">0</strong> / 12 CFU min
          </div>
          <button type="submit" class="btn btn-primary">Invia Piano di Studi</button>
        </div>
      </form>
    </div>
  `;

  updateCfuCounter();
}

function updateCfuCounter() {
  const checkboxes = document.querySelectorAll('input[name="piano_materia"]:checked');
  let tot = 0;
  checkboxes.forEach(cb => {
    tot += parseInt(cb.getAttribute('data-cfu') || '0', 10);
  });
  const label = document.getElementById('totale-cfu-label');
  if (label) {
    label.textContent = tot;
    label.style.color = tot >= 12 ? 'var(--accent-success)' : 'var(--accent-warning)';
  }
}

async function handleSalvaPianoStudi(e) {
  e.preventDefault();
  const checkboxes = document.querySelectorAll('input[name="piano_materia"]:checked');
  const codici = Array.from(checkboxes).map(cb => cb.value);

  const res = await API.post('/api/student/compila-piano', { codici });
  if (res.success) {
    UI.toast(res.data?.message || 'Piano salvato con successo!', 'success');
    loadActiveTabContent();
  } else {
    UI.toast(res.error || 'Errore compilazione piano', 'error');
  }
}

async function renderStudentTasse(container) {
  const res = await API.get('/api/student/tasse');
  const d = res.data || {};

  container.innerHTML = `
    <div class="page-header">
      <div>
        <h1 class="page-title">Gestione Tasse Universitarie</h1>
        <p class="page-subtitle">Verifica lo stato dei pagamenti universitari e salda le rate pendenti</p>
      </div>
    </div>

    <div class="card-panel" style="max-width: 600px;">
      <div class="card-panel-header">
        <h3 class="card-panel-title">Situazione Contabile</h3>
      </div>
      <div style="margin-bottom: 1.5rem;">
        <div style="font-size: 0.85rem; color: var(--text-muted); text-transform: uppercase;">Importo Totale Tasse</div>
        <div style="font-size: 2.2rem; font-weight: 800; color: #fff; margin: 0.2rem 0 1rem 0;">${d.importo ? d.importo.toFixed(2) : '0.00'} €</div>
        <div>
          Stato Pagamento: 
          <span class="badge ${d.pagate ? 'badge-success' : 'badge-danger'}" style="font-size: 0.85rem;">
            ${d.pagate ? 'REGOLARE (Saldate)' : 'IN SOSPESO (Non Saldate)'}
          </span>
        </div>
      </div>

      ${d.pagate ? `
        <div style="padding: 1rem; border-radius: var(--radius-md); background: var(--accent-success-bg); border: 1px solid rgba(16,185,129,0.3); color: var(--accent-success);">
          Tutte le tasse universitarie risultano regolarmente saldate. Puoi iscriverti a tutti gli appelli d'esame.
        </div>
      ` : `
        <button class="btn btn-primary" style="width: 100%;" onclick="simulaPagaTasse()">
          Simula Pagamento Tasse (${d.importo ? d.importo.toFixed(2) : '0.00'} €)
        </button>
      `}
    </div>
  `;
}

async function simulaPagaTasse() {
  const res = await API.post('/api/student/paga-tasse');
  if (res.success) {
    UI.toast('Pagamento completato con successo!', 'success');
    loadActiveTabContent();
  } else {
    UI.toast(res.error || 'Errore nel pagamento', 'error');
  }
}

const ALPHABET = ['A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'];
function renderLetterOptions(selectedLetter) {
  return ALPHABET.map(l => `<option value="${l}" ${l === selectedLetter ? 'selected' : ''}>${l}</option>`).join('');
}

function formatNotificationBody(messaggio) {
  if (!messaggio) return '';

  // Controlla se contiene il pattern Appello [ ... ]
  if (messaggio.includes('Appello [') && messaggio.includes(']')) {
    const startIdx = messaggio.indexOf('Appello [');
    const introText = messaggio.substring(0, startIdx).trim();
    const appelloPart = messaggio.substring(startIdx + 9, messaggio.lastIndexOf(']'));

    const props = {};
    appelloPart.split(',').forEach(pair => {
      const idx = pair.indexOf('=');
      if (idx !== -1) {
        const k = pair.substring(0, idx).trim();
        const v = pair.substring(idx + 1).trim();
        props[k] = v;
      }
    });

    return `
      ${introText ? `<div style="margin-bottom: 0.65rem; color: var(--text-primary); font-weight: 500;">${introText}</div>` : ''}
      <div style="background: var(--bg-surface-elevated); border: 1px solid var(--border-medium); border-radius: var(--radius-md); padding: 0.85rem 1.1rem; display: grid; grid-template-columns: repeat(auto-fit, minmax(130px, 1fr)); gap: 0.75rem;">
        ${props.codiceAppello ? `<div><span style="font-size: 0.72rem; color: var(--text-muted); text-transform: uppercase; display: block;">Codice Appello</span><strong style="color: var(--accent-primary); font-family: var(--font-mono);">${props.codiceAppello}</strong></div>` : ''}
        ${props.codiceMateria ? `<div><span style="font-size: 0.72rem; color: var(--text-muted); text-transform: uppercase; display: block;">Materia</span><strong style="color: #fff;">${props.codiceMateria}</strong></div>` : ''}
        ${props.dataOra ? `<div><span style="font-size: 0.72rem; color: var(--text-muted); text-transform: uppercase; display: block;">Data & Ora</span><strong style="color: #fff;">${UI.formatDate(props.dataOra)}</strong></div>` : ''}
        ${props.aula ? `<div><span style="font-size: 0.72rem; color: var(--text-muted); text-transform: uppercase; display: block;">Aula</span><strong style="color: #fff;">${props.aula}</strong></div>` : ''}
        ${props.postiDisponibili !== undefined ? `<div><span style="font-size: 0.72rem; color: var(--text-muted); text-transform: uppercase; display: block;">Posti Disp.</span><strong style="color: var(--accent-success);">${props.postiDisponibili}</strong></div>` : ''}
        ${props.vincoloLetteraCognome ? `<div><span style="font-size: 0.72rem; color: var(--text-muted); text-transform: uppercase; display: block;">Vincolo Cognome</span><span class="badge ${props.vincoloLetteraCognome !== 'A-Z' && props.vincoloLetteraCognome !== 'Nessuno' ? 'badge-warning' : 'badge-info'}">${props.vincoloLetteraCognome}</span></div>` : ''}
      </div>
    `;
  }

  return `<div class="notification-text">${messaggio}</div>`;
}

async function renderStudentNotifiche(container) {
  const res = await API.get('/api/student/notifiche');
  const list = (res.data || []).slice();

  // Ordina notifiche per data più recente prima
  list.sort((a, b) => {
    const timeA = a.data ? new Date(a.data).getTime() : 0;
    const timeB = b.data ? new Date(b.data).getTime() : 0;
    return timeB - timeA;
  });

  container.innerHTML = `
    <div class="page-header">
      <div>
        <h1 class="page-title">Le Tue Notifiche</h1>
        <p class="page-subtitle">Avvisi di corso, comunicazioni dei docenti e conferme di sistema in ordine cronologico recente</p>
      </div>
      <div style="display: flex; gap: 0.5rem; align-items: center;">
        <span class="badge badge-info" style="font-size: 0.85rem; padding: 0.4rem 0.8rem;">
          ${list.length} notifiche ricevute
        </span>
      </div>
    </div>

    <div class="card-panel">
      <div class="card-panel-header">
        <h3 class="card-panel-title">Centro Avvisi & Notifiche</h3>
      </div>
      ${list.length === 0 ? `
        <div style="text-align: center; padding: 3rem 1rem;">
          <div style="font-size: 3rem; margin-bottom: 0.75rem;">📭</div>
          <h3 style="color: var(--text-primary); margin-bottom: 0.25rem;">Nessuna notifica</h3>
          <p style="color: var(--text-muted); font-size: 0.9rem;">Non hai ancora ricevuto avvisi o comunicazioni dai tuoi docenti.</p>
        </div>
      ` : `
        <div class="notification-feed">
          ${list.map(n => {
            const isAvvisoDocente = n.titolo && (n.titolo.toLowerCase().includes('avviso') || n.titolo.toLowerCase().includes('comunicazione') || n.titolo.toLowerCase().includes('lezione'));
            const isEsito = n.titolo && (n.titolo.toLowerCase().includes('esito') || n.titolo.toLowerCase().includes('voto') || n.titolo.toLowerCase().includes('esame'));
            const isTasse = n.titolo && (n.titolo.toLowerCase().includes('tasse') || n.titolo.toLowerCase().includes('pagamento'));
            const isIscrizione = n.titolo && (n.titolo.toLowerCase().includes('iscrizione') || n.titolo.toLowerCase().includes('appello'));
            const icon = isIscrizione ? '📅' : isAvvisoDocente ? '📢' : isEsito ? '🎓' : isTasse ? '💳' : '🔔';

            return `
              <div class="notification-card">
                <div class="notification-icon-wrap">
                  ${icon}
                </div>
                <div class="notification-body">
                  <div class="notification-header">
                    <span class="notification-title">${n.titolo || 'Avviso di Sistema'}</span>
                    <span class="notification-time">
                      <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="12" cy="12" r="10"></circle>
                        <polyline points="12 6 12 12 16 14"></polyline>
                      </svg>
                      ${UI.formatDate(n.data)}
                    </span>
                  </div>
                  ${formatNotificationBody(n.messaggio)}
                </div>
              </div>
            `;
          }).join('')}
        </div>
      `}
    </div>
  `;
}

// ==========================================
// PROFESSOR TABS IMPLEMENTATIONS
// ==========================================
async function renderProfessorDashboard(container) {
  const materieRes = await API.get('/api/professor/materie');
  const appelliRes = await API.get('/api/professor/appelli');
  const esitiRes = await API.get('/api/professor/esiti-pubblicati');

  const materie = materieRes.data || [];
  const appelli = appelliRes.data || [];
  const esiti = esitiRes.data || [];

  container.innerHTML = `
    <div class="page-header">
      <div>
        <h1 class="page-title">Area Docente: Prof. ${state.user.nome} ${state.user.cognome}</h1>
        <p class="page-subtitle">ID Docente: <strong>${state.user.idProfessore}</strong></p>
      </div>
    </div>

    <div class="grid-cards">
      <div class="stat-card">
        <div class="stat-icon-wrapper indigo">📚</div>
        <div class="stat-content">
          <span class="stat-label">Materie di Insegnamento</span>
          <span class="stat-value">${materie.length}</span>
          <span class="stat-desc">Corsi abilitati</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon-wrapper emerald">📅</div>
        <div class="stat-content">
          <span class="stat-label">Appelli Gestiti</span>
          <span class="stat-value">${appelli.length}</span>
          <span class="stat-desc">Sessioni d'esame</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon-wrapper purple">✍️</div>
        <div class="stat-content">
          <span class="stat-label">Esiti Pubblicati</span>
          <span class="stat-value">${esiti.length}</span>
          <span class="stat-desc">Voti registrati</span>
        </div>
      </div>
    </div>

    <div class="card-panel">
      <div class="card-panel-header">
        <h3 class="card-panel-title">Le Tue Materie di Insegnamento</h3>
      </div>
      <div style="display: flex; flex-wrap: wrap; gap: 0.75rem;">
        ${materie.map(m => `
          <div style="background: var(--bg-surface); padding: 0.75rem 1.25rem; border-radius: var(--radius-md); border: 1px solid var(--border-medium);">
            <div style="font-weight: 700; color: #fff;">${m.nome}</div>
            <div style="color: var(--text-muted); font-size: 0.8rem; margin-top: 0.2rem;">Codice: ${m.codice} • ${m.cfu} CFU</div>
          </div>
        `).join('')}
      </div>
    </div>
  `;
}

async function renderProfessorCreaAppello(container) {
  const res = await API.get('/api/professor/materie');
  const materie = res.data || [];

  container.innerHTML = `
    <div class="page-header">
      <div>
        <h1 class="page-title">Crea Nuovo Appello d'Esame (UC1)</h1>
        <p class="page-subtitle">Pianifica una nuova sessione d'esame per le materie di cui sei responsabile</p>
      </div>
    </div>

    <div class="card-panel" style="max-width: 720px;">
      <form onsubmit="handleCreaAppello(event)">
        <div class="form-group">
          <label class="form-label">Materia di Insegnamento</label>
          <select id="app-materia" class="form-select" required>
            <option value="">Seleziona una materia...</option>
            ${materie.map(m => `<option value="${m.codice}">${m.nome} (${m.codice})</option>`).join('')}
          </select>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label">Data e Ora Appello</label>
            <input type="datetime-local" id="app-dataora" class="form-input" required />
          </div>
          <div class="form-group">
            <label class="form-label">Data Termine Iscrizione</label>
            <input type="date" id="app-termine" class="form-input" required />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label">Aula</label>
            <input type="text" id="app-aula" class="form-input" placeholder="es. Aula Magna / Lab Inf 1" required />
          </div>
          <div class="form-group">
            <label class="form-label">Posti Disponibili</label>
            <input type="number" id="app-posti" min="1" class="form-input" value="40" required />
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">Vincolo Cognome per Fascia Alfabetica</label>
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
            <div>
              <label style="font-size: 0.75rem; color: var(--text-muted); display: block; margin-bottom: 0.25rem;">Da Lettera Iniziale</label>
              <select id="app-vincolo-da" class="form-select">
                ${renderLetterOptions('A')}
              </select>
            </div>
            <div>
              <label style="font-size: 0.75rem; color: var(--text-muted); display: block; margin-bottom: 0.25rem;">A Lettera Finale</label>
              <select id="app-vincolo-a" class="form-select">
                ${renderLetterOptions('Z')}
              </select>
            </div>
          </div>
          <small style="color: var(--text-muted); font-size: 0.78rem; margin-top: 0.4rem; display: block;">
            Imposta <strong>A - Z</strong> per consentire l'iscrizione a tutti gli studenti senza alcun vincolo alfabetico.
          </small>
        </div>

        <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 0.5rem;">Crea Sessione d'Esame</button>
      </form>
    </div>
  `;
}

async function handleCreaAppello(e) {
  e.preventDefault();
  const vDa = document.getElementById('app-vincolo-da').value;
  const vA = document.getElementById('app-vincolo-a').value;
  const vincolo = (vDa === 'A' && vA === 'Z') ? '' : `${vDa}-${vA}`;

  const data = {
    codiceMateria: document.getElementById('app-materia').value,
    dataOra: document.getElementById('app-dataora').value,
    termineIscrizione: document.getElementById('app-termine').value,
    aula: document.getElementById('app-aula').value.trim(),
    posti: parseInt(document.getElementById('app-posti').value, 10),
    vincolo: vincolo
  };

  const res = await API.post('/api/professor/crea-appello', data);
  if (res.success) {
    UI.toast('Appello creato con successo!', 'success');
    switchTab('gestione-appelli');
  } else {
    UI.toast(res.error || 'Errore creazione appello', 'error');
  }
}

async function renderProfessorGestioneAppelli(container) {
  const res = await API.get('/api/professor/appelli');
  const list = res.data || [];

  container.innerHTML = `
    <div class="page-header">
      <div>
        <h1 class="page-title">I Miei Appelli d'Esame</h1>
        <p class="page-subtitle">Visualizza gli iscritti, modifica dettagli o elimina appelli</p>
      </div>
      <button class="btn btn-primary" onclick="switchTab('crea-appello')">+ Nuovo Appello</button>
    </div>

    <div class="card-panel">
      <div class="card-panel-header">
        <h3 class="card-panel-title">Elenco Sessioni Esame (${list.length})</h3>
      </div>
      ${list.length === 0 ? '<p style="color: var(--text-muted);">Non hai ancora creato nessun appello d\'esame.</p>' : `
        <div class="table-responsive">
          <table class="data-table">
            <thead>
              <tr>
                <th>Codice</th>
                <th>Materia</th>
                <th>Data & Ora</th>
                <th>Aula</th>
                <th>Vincolo</th>
                <th>Iscritti</th>
                <th>Termine</th>
                <th>Azioni</th>
              </tr>
            </thead>
            <tbody>
              ${list.map(a => `
                <tr>
                  <td><span style="font-family: var(--font-mono); color: var(--accent-primary); font-weight: 600;">${a.codiceAppello}</span></td>
                  <td><strong>${a.nomeMateria}</strong></td>
                  <td>${UI.formatDate(a.dataOra)}</td>
                  <td>${a.aula}</td>
                  <td><span class="badge ${a.vincolo && a.vincolo !== 'A-Z' ? 'badge-warning' : 'badge-info'}">${a.vincolo || 'A-Z (Tutti)'}</span></td>
                  <td><button class="btn btn-secondary btn-sm" onclick="visualizzaIscritti('${a.codiceAppello}')">${a.iscrittiCount} iscritti</button></td>
                  <td>${UI.formatShortDate(a.termineIscrizione)}</td>
                  <td>
                    <div style="display: flex; gap: 0.4rem;">
                      <button class="btn btn-secondary btn-sm" onclick="apriModaleModificaAppello('${a.codiceAppello}')">Modifica</button>
                      <button class="btn btn-danger btn-sm" onclick="eliminaAppelloDocente('${a.codiceAppello}')">Elimina</button>
                    </div>
                  </td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
      `}
    </div>
  `;
}

async function apriModaleModificaAppello(codiceAppello) {
  const res = await API.get('/api/professor/appelli');
  const appelli = res.data || [];
  const a = appelli.find(item => item.codiceAppello === codiceAppello);
  if (!a) {
    UI.toast('Appello non trovato', 'error');
    return;
  }

  let dataOraVal = '';
  if (a.dataOra) {
    dataOraVal = a.dataOra.length >= 16 ? a.dataOra.substring(0, 16) : a.dataOra;
  }
  let termineVal = '';
  if (a.termineIscrizione) {
    termineVal = a.termineIscrizione.length >= 10 ? a.termineIscrizione.substring(0, 10) : a.termineIscrizione;
  }

  let daLettera = 'A';
  let aLettera = 'Z';
  if (a.vincolo && a.vincolo.includes('-')) {
    const parts = a.vincolo.split('-');
    if (parts.length === 2) {
      daLettera = parts[0].trim().toUpperCase();
      aLettera = parts[1].trim().toUpperCase();
    }
  }

  UI.modal(
    `Modifica Appello: ${a.codiceAppello} (${a.nomeMateria})`,
    `
      <form id="form-modifica-appello" onsubmit="handleSalvaModificaAppello(event, '${a.codiceAppello}')">
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">Data e Ora Appello</label>
            <input type="datetime-local" id="edit-app-dataora" class="form-input" value="${dataOraVal}" required />
          </div>
          <div class="form-group">
            <label class="form-label">Data Termine Iscrizione</label>
            <input type="date" id="edit-app-termine" class="form-input" value="${termineVal}" required />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label">Aula</label>
            <input type="text" id="edit-app-aula" class="form-input" value="${a.aula || ''}" required />
          </div>
          <div class="form-group">
            <label class="form-label">Posti Disponibili</label>
            <input type="number" id="edit-app-posti" min="1" class="form-input" value="${a.posti || 40}" required />
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">Vincolo Cognome per Fascia Alfabetica</label>
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
            <div>
              <label style="font-size: 0.75rem; color: var(--text-muted); display: block; margin-bottom: 0.25rem;">Da Lettera Iniziale</label>
              <select id="edit-app-vincolo-da" class="form-select">
                ${renderLetterOptions(daLettera)}
              </select>
            </div>
            <div>
              <label style="font-size: 0.75rem; color: var(--text-muted); display: block; margin-bottom: 0.25rem;">A Lettera Finale</label>
              <select id="edit-app-vincolo-a" class="form-select">
                ${renderLetterOptions(aLettera)}
              </select>
            </div>
          </div>
        </div>
      </form>
    `,
    `
      <button class="btn btn-secondary" onclick="UI.closeModal()">Annulla</button>
      <button type="submit" form="form-modifica-appello" class="btn btn-primary">Salva Modifiche</button>
    `
  );
}

async function handleSalvaModificaAppello(e, codiceAppello) {
  e.preventDefault();
  const vDa = document.getElementById('edit-app-vincolo-da').value;
  const vA = document.getElementById('edit-app-vincolo-a').value;
  const vincolo = (vDa === 'A' && vA === 'Z') ? '' : `${vDa}-${vA}`;

  const payload = {
    codiceAppello: codiceAppello,
    dataOra: document.getElementById('edit-app-dataora').value,
    termineIscrizione: document.getElementById('edit-app-termine').value,
    aula: document.getElementById('edit-app-aula').value.trim(),
    posti: parseInt(document.getElementById('edit-app-posti').value, 10),
    vincolo: vincolo
  };

  const res = await API.post('/api/professor/modifica-appello', payload);
  if (res.success) {
    UI.toast('Appello modificato con successo!', 'success');
    UI.closeModal();
    loadActiveTabContent();
  } else {
    UI.toast(res.error || 'Errore modifica appello', 'error');
  }
}

async function visualizzaIscritti(codiceAppello) {
  const res = await API.get('/api/professor/iscritti-appello', { codiceAppello });
  const iscritti = res.data || [];

  UI.modal(
    `Studenti Iscritti - Appello ${codiceAppello}`,
    `
      ${iscritti.length === 0 ? '<p style="color: var(--text-muted);">Nessun studente iscritto a questo appello.</p>' : `
        <div class="table-responsive">
          <table class="data-table">
            <thead>
              <tr>
                <th>Matricola</th>
                <th>Nome e Cognome</th>
                <th>Email</th>
              </tr>
            </thead>
            <tbody>
              ${iscritti.map(s => `
                <tr>
                  <td><span style="font-family: var(--font-mono);">${s.matricola}</span></td>
                  <td><strong>${s.nome} ${s.cognome}</strong></td>
                  <td>${s.email}</td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
      `}
    `,
    `<button class="btn btn-secondary" onclick="UI.closeModal()">Chiudi</button>`
  );
}

async function eliminaAppelloDocente(codiceAppello) {
  if (!confirm(`Sei sicuro di voler eliminare l'appello ${codiceAppello}?`)) return;
  const res = await API.post('/api/professor/elimina-appello', { codiceAppello });
  if (res.success) {
    UI.toast('Appello eliminato con successo', 'info');
    loadActiveTabContent();
  } else {
    UI.toast(res.error || 'Errore eliminazione', 'error');
  }
}

async function renderProfessorPubblicaEsito(container) {
  const res = await API.get('/api/professor/appelli');
  const appelli = res.data || [];

  container.innerHTML = `
    <div class="page-header">
      <div>
        <h1 class="page-title">Pubblica Esito Esame (UC3)</h1>
        <p class="page-subtitle">Registra il voto d'esame per uno studente iscritto all'appello</p>
      </div>
    </div>

    <div class="card-panel" style="max-width: 700px;">
      <form onsubmit="handlePubblicaEsito(event)">
        <div class="form-group">
          <label class="form-label">Seleziona Appello d'Esame</label>
          <select id="esito-appello" class="form-select" onchange="caricaIscrittiPerEsito()" required>
            <option value="">Seleziona un appello...</option>
            ${appelli.map(a => `<option value="${a.codiceAppello}">${a.codiceAppello} - ${a.nomeMateria} (${UI.formatShortDate(a.dataOra)})</option>`).join('')}
          </select>
        </div>

        <div class="form-group">
          <label class="form-label">Studente Iscritto (Senza voto già verbalizzato o esito pendente)</label>
          <select id="esito-studente" class="form-select" required disabled>
            <option value="">Prima seleziona un appello...</option>
          </select>
        </div>

        <input type="hidden" id="esito-codicemateria" />

        <div class="form-row">
          <div class="form-group">
            <label class="form-label">Voto Numerico (0-30)</label>
            <input type="number" id="esito-voto" min="0" max="30" class="form-input" placeholder="30" oninput="toggleLodeCheckbox()" required />
          </div>
          <div class="form-group" style="display: flex; align-items: center; gap: 0.5rem; margin-top: 1.8rem;">
            <input type="checkbox" id="esito-lode" disabled />
            <label for="esito-lode" style="cursor: pointer; color: #fff;">Lode (solo per voto 30)</label>
          </div>
        </div>

        <button type="submit" class="btn btn-primary" style="width: 100%;">Pubblica Esito</button>
      </form>
    </div>
  `;
}

function toggleLodeCheckbox() {
  const voto = parseInt(document.getElementById('esito-voto')?.value || '0', 10);
  const lodeCb = document.getElementById('esito-lode');
  if (lodeCb) {
    lodeCb.disabled = voto !== 30;
    if (voto !== 30) lodeCb.checked = false;
  }
}

async function caricaIscrittiPerEsito() {
  const codAppello = document.getElementById('esito-appello').value;
  const selectStud = document.getElementById('esito-studente');
  const hiddenMat = document.getElementById('esito-codicemateria');
  if (!codAppello) {
    selectStud.disabled = true;
    selectStud.innerHTML = '<option value="">Prima seleziona un appello...</option>';
    return;
  }

  const res = await API.get('/api/professor/iscritti-per-esito', { codiceAppello: codAppello });
  if (res.success) {
    hiddenMat.value = res.data.codiceMateria;
    const list = res.data.studenti || [];
    if (list.length === 0) {
      selectStud.disabled = true;
      selectStud.innerHTML = '<option value="">Nessuno studente idoneo (tutti già verbalizzati o con esito pendente)</option>';
    } else {
      selectStud.disabled = false;
      selectStud.innerHTML = `
        <option value="">Seleziona studente...</option>
        ${list.map(s => `<option value="${s.matricola}">${s.nome} ${s.cognome} (${s.matricola})</option>`).join('')}
      `;
    }
  } else {
    UI.toast(res.error || 'Errore caricamento iscritti', 'error');
  }
}

async function handlePubblicaEsito(e) {
  e.preventDefault();
  const data = {
    codiceAppello: document.getElementById('esito-appello').value,
    matricola: document.getElementById('esito-studente').value,
    codiceMateria: document.getElementById('esito-codicemateria').value,
    voto: parseInt(document.getElementById('esito-voto').value, 10),
    lode: document.getElementById('esito-lode').checked,
    giorni: 7
  };

  const res = await API.post('/api/professor/pubblica-esito', data);
  if (res.success) {
    UI.toast('Esito pubblicato con successo!', 'success');
    switchTab('esiti-pubblicati');
  } else {
    UI.toast(res.error || 'Errore pubblicazione esito', 'error');
  }
}

async function renderProfessorEsitiPubblicati(container) {
  const res = await API.get('/api/professor/esiti-pubblicati');
  const list = res.data || [];

  container.innerHTML = `
    <div class="page-header">
      <div>
        <h1 class="page-title">Esiti Pubblicati</h1>
        <p class="page-subtitle">Visualizza tutti i verbali e i voti assegnati agli studenti</p>
      </div>
    </div>

    <div class="card-panel">
      <div class="card-panel-header">
        <h3 class="card-panel-title">Elenco Verbali (${list.length})</h3>
      </div>
      ${list.length === 0 ? '<p style="color: var(--text-muted);">Nessun esito pubblicato finora.</p>' : `
        <div class="table-responsive">
          <table class="data-table">
            <thead>
              <tr>
                <th>ID Verbale</th>
                <th>Materia</th>
                <th>Studente</th>
                <th>Voto</th>
                <th>Stato</th>
                <th>Scadenza</th>
              </tr>
            </thead>
            <tbody>
              ${list.map(e => `
                <tr>
                  <td><span style="font-family: var(--font-mono);">${e.idVerbale}</span></td>
                  <td><strong>${e.nomeMateria}</strong></td>
                  <td>${e.nomeStudente} (${e.matricola})</td>
                  <td><span style="font-weight: 700;">${e.voto}${e.lode ? ' e Lode' : ''}</span>/30</td>
                  <td><span class="badge ${e.stato === 'Approvato' ? 'badge-success' : e.stato === 'Bocciato' || e.stato === 'Rifiutato' ? 'badge-danger' : 'badge-warning'}">${e.stato}</span></td>
                  <td>${UI.formatDate(e.scadenza)}</td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
      `}
    </div>
  `;
}

async function renderProfessorComunicazioni(container) {
  const res = await API.get('/api/professor/materie');
  const materie = res.data || [];

  container.innerHTML = `
    <div class="page-header">
      <div>
        <h1 class="page-title">Invia Comunicazione di Corso (UC7 - Observer)</h1>
        <p class="page-subtitle">Pubblica un avviso per gli studenti iscritti alla tua materia</p>
      </div>
    </div>

    <div class="card-panel" style="max-width: 700px;">
      <form onsubmit="handleInviaComunicazione(event)">
        <div class="form-group">
          <label class="form-label">Materia</label>
          <select id="com-materia" class="form-select" onchange="aggiornaDestinatariCount()" required>
            <option value="">Seleziona una materia...</option>
            ${materie.map(m => `<option value="${m.codice}">${m.nome} (${m.codice})</option>`).join('')}
          </select>
        </div>

        <div id="com-destinatari-info" style="color: var(--text-muted); font-size: 0.85rem; margin-bottom: 1rem;">
          Seleziona una materia per visualizzare il numero di studenti destinatari.
        </div>

        <div class="form-group">
          <label class="form-label">Titolo / Oggetto dell'Avviso</label>
          <input type="text" id="com-titolo" class="form-input" placeholder="es. Variazione orario lezione" required />
        </div>

        <div class="form-group">
          <label class="form-label">Testo della Comunicazione</label>
          <textarea id="com-messaggio" class="form-textarea" placeholder="Inserisci il corpo del messaggio..." required></textarea>
        </div>

        <button type="submit" class="btn btn-primary" style="width: 100%;">Invia Comunicazione</button>
      </form>
    </div>
  `;
}

async function aggiornaDestinatariCount() {
  const cod = document.getElementById('com-materia').value;
  const info = document.getElementById('com-destinatari-info');
  if (!cod) {
    info.textContent = 'Seleziona una materia per visualizzare il numero di studenti destinatari.';
    return;
  }
  const res = await API.get('/api/professor/destinatari-comunicazione', { codiceMateria: cod });
  if (res.success) {
    info.innerHTML = `Studenti destinatari che riceveranno la notifica: <strong style="color: var(--accent-primary);">${res.data.conteggio}</strong>`;
  }
}

async function handleInviaComunicazione(e) {
  e.preventDefault();
  const data = {
    codiceMateria: document.getElementById('com-materia').value,
    titolo: document.getElementById('com-titolo').value.trim(),
    messaggio: document.getElementById('com-messaggio').value.trim()
  };

  const res = await API.post('/api/professor/invia-comunicazione', data);
  if (res.success) {
    UI.toast(res.data?.message || 'Comunicazione inviata con successo!', 'success');
    document.getElementById('com-titolo').value = '';
    document.getElementById('com-messaggio').value = '';
  } else {
    UI.toast(res.error || 'Errore invio comunicazione', 'error');
  }
}

// ==========================================
// ADMIN TABS IMPLEMENTATIONS
// ==========================================
async function renderAdminDashboard(container) {
  const res = await API.get('/api/admin/stats');
  const d = res.data || {};

  container.innerHTML = `
    <div class="page-header">
      <div>
        <h1 class="page-title">Pannello di Amministrazione UniCenter</h1>
        <p class="page-subtitle">Panoramica e monitoraggio globale dell'ateneo</p>
      </div>
    </div>

    <div class="grid-cards">
      <div class="stat-card">
        <div class="stat-icon-wrapper indigo">🏛️</div>
        <div class="stat-content">
          <span class="stat-label">Corsi di Laurea</span>
          <span class="stat-value">${d.corsiCount}</span>
          <span class="stat-desc">Attivi e configurati</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon-wrapper emerald">📚</div>
        <div class="stat-content">
          <span class="stat-label">Materie Totali</span>
          <span class="stat-value">${d.materieCount}</span>
          <span class="stat-desc">Catalogo insegnamenti</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon-wrapper amber">👨‍🎓</div>
        <div class="stat-content">
          <span class="stat-label">Studenti Iscritti</span>
          <span class="stat-value">${d.studentiCount}</span>
          <span class="stat-desc">Immatricolati</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon-wrapper purple">⏳</div>
        <div class="stat-content">
          <span class="stat-label">Piani in Attesa</span>
          <span class="stat-value">${d.pianiInAttesaCount}</span>
          <span class="stat-desc">Da valutare</span>
        </div>
      </div>
    </div>
  `;
}

async function renderAdminCorsi(container) {
  const res = await API.get('/api/admin/corsi');
  const list = res.data || [];

  container.innerHTML = `
    <div class="page-header">
      <div>
        <h1 class="page-title">Gestione Corsi di Laurea (UC4)</h1>
        <p class="page-subtitle">Configura, finalizza e gestisci l'offerta formativa dell'ateneo</p>
      </div>
      <button class="btn btn-primary" onclick="apriModalCreaCorso()">+ Nuovo Corso</button>
    </div>

    <div class="card-panel">
      <div class="card-panel-header">
        <h3 class="card-panel-title">Tutti i Corsi di Laurea (${list.length})</h3>
      </div>
      <div class="table-responsive">
        <table class="data-table">
          <thead>
            <tr>
              <th>Codice</th>
              <th>Nome Corso</th>
              <th>Tipologia</th>
              <th>Anni</th>
              <th>Stato</th>
              <th>Azioni</th>
            </tr>
          </thead>
          <tbody>
            ${list.map(c => `
              <tr>
                <td><span style="font-family: var(--font-mono); font-weight: 600;">${c.id}</span></td>
                <td><strong>${c.nome}</strong></td>
                <td>${c.tipologia}</td>
                <td>${c.anni} anni</td>
                <td>
                  ${c.obsoleto ? '<span class="badge badge-danger">Obsoleto</span>' : c.finalizzato ? '<span class="badge badge-success">Finalizzato</span>' : '<span class="badge badge-warning">Bozza</span>'}
                </td>
                <td>
                  <div style="display: flex; gap: 0.3rem;">
                    ${!c.finalizzato && !c.obsoleto ? `
                      <button class="btn btn-success btn-sm" onclick="finalizzaCorso('${c.id}')">Finalizza</button>
                      <button class="btn btn-secondary btn-sm" onclick="apriModalAssociaMateria('${c.id}')">+ Materia</button>
                    ` : ''}
                    ${!c.obsoleto ? `<button class="btn btn-secondary btn-sm" onclick="rendiObsoletoCorso('${c.id}')">Obsoleto</button>` : ''}
                    <button class="btn btn-danger btn-sm" onclick="eliminaCorso('${c.id}')">Elimina</button>
                  </div>
                </td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    </div>
  `;
}

function apriModalCreaCorso() {
  UI.modal(
    'Crea Nuovo Corso di Laurea',
    `
      <form id="form-crea-corso" onsubmit="handleCreaCorso(event)">
        <div class="form-group">
          <label class="form-label">Nome Corso</label>
          <input type="text" id="nc-nome" class="form-input" placeholder="es. Ingegneria Meccatronica" required />
        </div>
        <div class="form-group">
          <label class="form-label">Tipologia</label>
          <select id="nc-tipo" class="form-select" required>
            <option value="Laurea Triennale">Laurea Triennale</option>
            <option value="Laurea Magistrale">Laurea Magistrale</option>
            <option value="Laurea Magistrale a Ciclo Unico">Laurea Magistrale a Ciclo Unico</option>
          </select>
        </div>
        <div class="form-group">
          <label class="form-label">Anni Accademici</label>
          <input type="number" id="nc-anni" min="1" max="6" class="form-input" value="3" required />
        </div>
        <button type="submit" class="btn btn-primary" style="width: 100%;">Salva Corso</button>
      </form>
    `
  );
}

async function handleCreaCorso(e) {
  e.preventDefault();
  const data = {
    nome: document.getElementById('nc-nome').value.trim(),
    tipologia: document.getElementById('nc-tipo').value,
    anni: parseInt(document.getElementById('nc-anni').value, 10)
  };

  const res = await API.post('/api/admin/crea-corso', data);
  if (res.success) {
    UI.closeModal();
    UI.toast('Corso creato con successo!', 'success');
    loadActiveTabContent();
  } else {
    UI.toast(res.error || 'Errore creazione corso', 'error');
  }
}

async function finalizzaCorso(codice) {
  if (!confirm(`Sei sicuro di voler finalizzare il corso ${codice}? Non sarà più possibile modificarne le materie obbligatorie.`)) return;
  const res = await API.post('/api/admin/finalizza-corso', { codice });
  if (res.success) {
    UI.toast('Corso finalizzato con successo!', 'success');
    loadActiveTabContent();
  } else {
    UI.toast(res.error || 'Errore finalizzazione', 'error');
  }
}

async function rendiObsoletoCorso(codice) {
  if (!confirm(`Rendere obsoleto il corso ${codice}? Non accetterà più nuove immatricolazioni.`)) return;
  const res = await API.post('/api/admin/rendi-obsoleto-corso', { codice });
  if (res.success) {
    UI.toast('Corso reso obsoleto', 'info');
    loadActiveTabContent();
  } else {
    UI.toast(res.error || 'Errore', 'error');
  }
}

async function eliminaCorso(codice) {
  if (!confirm(`Sei sicuro di voler eliminare il corso ${codice}?`)) return;
  const res = await API.post('/api/admin/elimina-corso', { codice });
  if (res.success) {
    UI.toast('Corso eliminato', 'info');
    loadActiveTabContent();
  } else {
    UI.toast(res.error || 'Errore eliminazione', 'error');
  }
}

async function apriModalAssociaMateria(codiceCorso) {
  const matRes = await API.get('/api/admin/materie');
  const materie = matRes.data || [];

  UI.modal(
    `Associa Materia al Corso ${codiceCorso}`,
    `
      <form onsubmit="handleAssociaMateria(event, '${codiceCorso}')">
        <div class="form-group">
          <label class="form-label">Anno di Corso</label>
          <input type="number" id="am-anno" min="1" max="6" class="form-input" value="1" required />
        </div>
        <div class="form-group">
          <label class="form-label">Materia da Associare</label>
          <select id="am-materia" class="form-select" required>
            ${materie.map(m => `<option value="${m.codice}">${m.nome} (${m.codice} - ${m.cfu} CFU)</option>`).join('')}
          </select>
        </div>
        <button type="submit" class="btn btn-primary" style="width: 100%;">Associa Materia</button>
      </form>
    `
  );
}

async function handleAssociaMateria(e, codiceCorso) {
  e.preventDefault();
  const data = {
    codiceCorso,
    anno: parseInt(document.getElementById('am-anno').value, 10),
    codiceMateria: document.getElementById('am-materia').value
  };

  const res = await API.post('/api/admin/associa-materia-corso', data);
  if (res.success) {
    UI.closeModal();
    UI.toast('Materia associata al corso con successo!', 'success');
    loadActiveTabContent();
  } else {
    UI.toast(res.error || 'Errore associazione', 'error');
  }
}

async function renderAdminMaterie(container) {
  const res = await API.get('/api/admin/materie');
  const list = res.data || [];

  container.innerHTML = `
    <div class="page-header">
      <div>
        <h1 class="page-title">Gestione Materie (UC5)</h1>
        <p class="page-subtitle">Crea nuove materie accademiche e associa docenti responsabili</p>
      </div>
      <button class="btn btn-primary" onclick="apriModalCreaMateria()">+ Nuova Materia</button>
    </div>

    <div class="card-panel">
      <div class="card-panel-header">
        <h3 class="card-panel-title">Elenco Materie (${list.length})</h3>
      </div>
      <div class="table-responsive">
        <table class="data-table">
          <thead>
            <tr>
              <th>Codice</th>
              <th>Nome Materia</th>
              <th>CFU</th>
              <th>Docenti Associati</th>
              <th>Azioni</th>
            </tr>
          </thead>
          <tbody>
            ${list.map(m => `
              <tr>
                <td><span style="font-family: var(--font-mono); font-weight: 600;">${m.codice}</span></td>
                <td><strong>${m.nome}</strong></td>
                <td>${m.cfu} CFU</td>
                <td>
                  ${(m.professori || []).length === 0 ? '<span style="color: var(--text-muted);">(Nessun docente)</span>' : (m.professori || []).join(', ')}
                </td>
                <td>
                  <button class="btn btn-secondary btn-sm" onclick="apriModalAssociaProf('${m.codice}', '${m.nome}')">+ Docente</button>
                </td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    </div>
  `;
}

function apriModalCreaMateria() {
  UI.modal(
    'Crea Nuova Materia',
    `
      <form onsubmit="handleCreaMateria(event)">
        <div class="form-group">
          <label class="form-label">Nome Materia</label>
          <input type="text" id="nm-nome" class="form-input" placeholder="es. Sistemi Distribuiti" required />
        </div>
        <div class="form-group">
          <label class="form-label">Numero CFU</label>
          <input type="number" id="nm-cfu" min="1" max="18" class="form-input" value="6" required />
        </div>
        <button type="submit" class="btn btn-primary" style="width: 100%;">Salva Materia</button>
      </form>
    `
  );
}

async function handleCreaMateria(e) {
  e.preventDefault();
  const data = {
    nome: document.getElementById('nm-nome').value.trim(),
    cfu: parseInt(document.getElementById('nm-cfu').value, 10)
  };

  const res = await API.post('/api/admin/crea-materia', data);
  if (res.success) {
    UI.closeModal();
    UI.toast('Materia creata con successo!', 'success');
    loadActiveTabContent();
  } else {
    UI.toast(res.error || 'Errore creazione materia', 'error');
  }
}

async function apriModalAssociaProf(codiceMateria, nomeMateria) {
  const profRes = await API.get('/api/admin/professori-non-associati', { codiceMateria });
  const docenti = profRes.data || [];

  UI.modal(
    `Associa Docente a ${nomeMateria}`,
    `
      ${docenti.length === 0 ? '<p style="color: var(--text-muted);">Tutti i docenti sono già associati a questa materia.</p>' : `
        <form onsubmit="handleAssociaProf(event, '${codiceMateria}')">
          <div class="form-group">
            <label class="form-label">Seleziona Docente</label>
            <select id="ap-prof" class="form-select" required>
              ${docenti.map(p => `<option value="${p.id}">${p.nome} ${p.cognome} (ID: ${p.id})</option>`).join('')}
            </select>
          </div>
          <button type="submit" class="btn btn-primary" style="width: 100%;">Associa Docente</button>
        </form>
      `}
    `
  );
}

async function handleAssociaProf(e, codiceMateria) {
  e.preventDefault();
  const idProfessore = document.getElementById('ap-prof').value;
  const res = await API.post('/api/admin/associa-professore', { idProfessore, codiceMateria });
  if (res.success) {
    UI.closeModal();
    UI.toast('Docente associato con successo!', 'success');
    loadActiveTabContent();
  } else {
    UI.toast(res.error || 'Errore associazione docente', 'error');
  }
}

async function renderAdminPreapprovate(container) {
  const corsiRes = await API.get('/api/admin/corsi');
  const corsi = corsiRes.data || [];

  container.innerHTML = `
    <div class="page-header">
      <div>
        <h1 class="page-title">Gestione Materie Pre-Approvate (UC9)</h1>
        <p class="page-subtitle">Imposta le materie a scelta con approvazione automatica per ciascun corso di laurea</p>
      </div>
    </div>

    <div class="card-panel">
      <div class="form-group" style="max-width: 400px;">
        <label class="form-label">Seleziona Corso di Laurea</label>
        <select id="pa-corso" class="form-select" onchange="caricaMateriePreapprovate()">
          <option value="">Seleziona un corso...</option>
          ${corsi.map(c => `<option value="${c.id}">${c.nome} (${c.id})</option>`).join('')}
        </select>
      </div>

      <div id="pa-content-area" style="margin-top: 1.5rem;">
        <p style="color: var(--text-muted);">Seleziona un corso per visualizzare e modificare le materie pre-approvate.</p>
      </div>
    </div>
  `;
}

async function caricaMateriePreapprovate() {
  const codCorso = document.getElementById('pa-corso').value;
  const container = document.getElementById('pa-content-area');
  if (!codCorso) {
    container.innerHTML = '<p style="color: var(--text-muted);">Seleziona un corso per visualizzare le materie pre-approvate.</p>';
    return;
  }

  const res = await API.get('/api/admin/materie-preapprovate', { codiceCorso: codCorso });
  const pre = res.data || [];

  const matRes = await API.get('/api/admin/materie');
  const tutte = matRes.data || [];

  const preCodici = pre.map(m => m.codice);
  const disponibiliDaAggiungere = tutte.filter(m => !preCodici.includes(m.codice));

  container.innerHTML = `
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
      <h4>Materie Attualmente Pre-Approvate (${pre.length})</h4>
    </div>

    ${pre.length === 0 ? '<p style="color: var(--text-muted); margin-bottom: 1.5rem;">Nessuna materia pre-approvata configurata per questo corso.</p>' : `
      <div class="table-responsive" style="margin-bottom: 1.5rem;">
        <table class="data-table">
          <thead>
            <tr>
              <th>Codice</th>
              <th>Nome Materia</th>
              <th>CFU</th>
              <th>Azione</th>
            </tr>
          </thead>
          <tbody>
            ${pre.map(m => `
              <tr>
                <td><span style="font-family: var(--font-mono);">${m.codice}</span></td>
                <td><strong>${m.nome}</strong></td>
                <td>${m.cfu} CFU</td>
                <td>
                  <button class="btn btn-danger btn-sm" onclick="rimuoviMateriaPreapprovata('${codCorso}', '${m.codice}')">Rimuovi</button>
                </td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    `}

    <div class="card-panel" style="background: var(--bg-surface);">
      <h4 style="margin-bottom: 1rem;">Aggiungi Materia alle Pre-Approvate</h4>
      <div style="display: flex; gap: 0.75rem; max-width: 500px;">
        <select id="nuova-pre-materia" class="form-select">
          ${disponibiliDaAggiungere.map(m => `<option value="${m.codice}">${m.nome} (${m.codice} - ${m.cfu} CFU)</option>`).join('')}
        </select>
        <button class="btn btn-primary" onclick="aggiungiMateriaPreapprovata('${codCorso}')">Aggiungi</button>
      </div>
    </div>
  `;
}

async function aggiungiMateriaPreapprovata(codiceCorso) {
  const codMateria = document.getElementById('nuova-pre-materia').value;
  if (!codMateria) return;
  const res = await API.post('/api/admin/aggiungi-preapprovata', { codiceCorso, codiceMateria: codMateria });
  if (res.success) {
    UI.toast('Materia aggiunta alle pre-approvate', 'success');
    caricaMateriePreapprovate();
  } else {
    UI.toast(res.error || 'Errore', 'error');
  }
}

async function rimuoviMateriaPreapprovata(codiceCorso, codiceMateria) {
  const res = await API.post('/api/admin/rimuovi-preapprovata', { codiceCorso, codiceMateria });
  if (res.success) {
    UI.toast('Materia rimossa dalle pre-approvate', 'info');
    caricaMateriePreapprovate();
  } else {
    UI.toast(res.error || 'Errore', 'error');
  }
}

async function renderAdminPianiAttesa(container) {
  const res = await API.get('/api/admin/piani-in-attesa');
  const list = res.data || [];

  container.innerHTML = `
    <div class="page-header">
      <div>
        <h1 class="page-title">Valutazione Piani di Studio in Attesa (UC9)</h1>
        <p class="page-subtitle">Approva o rifiuta i piani di studio con materie a scelta non pre-approvate</p>
      </div>
    </div>

    <div class="card-panel">
      <div class="card-panel-header">
        <h3 class="card-panel-title">Piani di Studio da Valutare (${list.length})</h3>
      </div>
      ${list.length === 0 ? '<p style="color: var(--text-muted);">Nessun piano di studi in attesa di approvazione.</p>' : `
        <div style="display: flex; flex-direction: column; gap: 1.25rem;">
          ${list.map(p => `
            <div style="background: var(--bg-surface); border: 1px solid var(--border-medium); border-radius: var(--radius-lg); padding: 1.25rem;">
              <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1rem;">
                <div>
                  <h4 style="color: #fff; font-size: 1.05rem;">Studente: ${p.studenteNome}</h4>
                  <div style="color: var(--text-muted); font-size: 0.82rem; margin-top: 0.2rem;">
                    Matricola: <span style="font-family: var(--font-mono);">${p.matricola}</span> • Corso: ${p.corso}
                  </div>
                </div>
                <div style="display: flex; gap: 0.5rem;">
                  <button class="btn btn-success btn-sm" onclick="approvaPianoAdmin('${p.matricola}')">Approva Piano</button>
                  <button class="btn btn-danger btn-sm" onclick="rifiutaPianoAdmin('${p.matricola}')">Rifiuta Piano</button>
                </div>
              </div>

              <div style="font-size: 0.85rem; font-weight: 600; color: var(--text-secondary); margin-bottom: 0.5rem;">
                Materie a Scelta Richieste dallo Studente:
              </div>
              <div style="display: flex; flex-wrap: wrap; gap: 0.5rem;">
                ${p.materieScelta.map(m => `
                  <div style="background: var(--bg-surface-elevated); padding: 0.4rem 0.75rem; border-radius: var(--radius-md); border: 1px solid var(--border-subtle); display: flex; align-items: center; gap: 0.5rem;">
                    <span><strong>${m.nome}</strong> (${m.codice} - ${m.cfu} CFU)</span>
                    ${m.preApprovata ? '<span class="badge badge-success" style="font-size: 0.65rem;">Pre-Approvata</span>' : '<span class="badge badge-warning" style="font-size: 0.65rem;">Da Valutare</span>'}
                  </div>
                `).join('')}
              </div>
            </div>
          `).join('')}
        </div>
      `}
    </div>
  `;
}

async function approvaPianoAdmin(matricola) {
  const res = await API.post('/api/admin/approva-piano', { matricola });
  if (res.success) {
    UI.toast(`Piano per la matricola ${matricola} APPROVATO!`, 'success');
    loadActiveTabContent();
  } else {
    UI.toast(res.error || 'Errore approvazione', 'error');
  }
}

async function rifiutaPianoAdmin(matricola) {
  if (!confirm(`Sei sicuro di voler rifiutare il piano di studi per la matricola ${matricola}?`)) return;
  const res = await API.post('/api/admin/rifiuta-piano', { matricola });
  if (res.success) {
    UI.toast(`Piano per la matricola ${matricola} RIFIUTATO`, 'info');
    loadActiveTabContent();
  } else {
    UI.toast(res.error || 'Errore rifiuto', 'error');
  }
}
