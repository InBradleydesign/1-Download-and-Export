// ==================== CUIDARAPP - VERSÃO OFFLINE ====================
// Bloco de notas de cuidados para idoso e cuidador em um único aparelho.
// Todos os dados ficam no armazenamento local do WebView (nada vai para a internet).

const STORAGE_SETTINGS = 'cuidarapp_settings';
const STORAGE_DATA = 'cuidarapp_data';
const DEFAULT_PASSWORD = '1234';
const CUP_ML = 250;

const DEFAULT_SETTINGS = {
    elderlyName: 'Idoso',
    caregiverName: 'Cuidador',
    elderlyPhoto: '',
    caregiverPhoto: '',
    passwordHash: '',
    deviceOwner: 'elderly',
    fontSize: 100,
    animationsEnabled: true,
    highContrast: false,
    emergencyPhone: '192',
    caregiverPhone: '',
    weightKg: 0,
    frail: false,
    waterGoal: 8
};

const DEFAULT_DATA = {
    medications: [],
    exercises: [],
    visits: [],
    diet: {},
    log: {},
    alerts: []
};

const App = {
    settings: { ...DEFAULT_SETTINGS },
    data: JSON.parse(JSON.stringify(DEFAULT_DATA)),
    history: [],
    editStack: [],
    currentCategory: 'all'
};

// ==================== CONSTANTES DE DOMÍNIO ====================
const ICONS = {
    pill: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m10.5 20.5 10-10a4.95 4.95 0 1 0-7-7l-10 10a4.95 4.95 0 1 0 7 7Z"/><path d="m8.5 8.5 7 7"/></svg>',
    exercise: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="5" r="3"/><path d="M12 22V8"/><path d="m5 12 7 3 7-3"/></svg>',
    food: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 8h1a4 4 0 0 1 0 8h-1"/><path d="M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z"/></svg>',
    water: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2.69l5.66 5.66a8 8 0 1 1-11.31 0z"/></svg>',
    calendar: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>',
    clock: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12,6 12,12 16,14"/></svg>',
    check: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20,6 9,17 4,12"/></svg>',
    chevron: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>',
    info: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>',
    alert: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>',
    users: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>',
    user: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>',
    plus: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>'
};

const EXERCISE_TYPES = {
    strength: 'Força',
    balance: 'Equilíbrio',
    gait: 'Marcha',
    flexibility: 'Flexibilidade'
};

// Baseado no programa multicomponente Vivifrail (força, equilíbrio, marcha, flexibilidade)
const EXERCISE_PRESETS = [
    { name: 'Sentar e levantar da cadeira', type: 'strength', duration: 10, description: '3 séries de 8 a 12 repetições, com os braços cruzados no peito.' },
    { name: 'Ficar na ponta dos pés', type: 'strength', duration: 5, description: 'Segurando no encosto de uma cadeira, 2 séries de 10 repetições.' },
    { name: 'Equilíbrio em um pé só', type: 'balance', duration: 5, description: 'Apoiado na cadeira, 10 segundos em cada perna, 3 vezes.' },
    { name: 'Andar em linha reta', type: 'balance', duration: 5, description: 'Um pé na frente do outro, encostando o calcanhar na ponta do pé.' },
    { name: 'Caminhada leve', type: 'gait', duration: 20, description: 'Em ritmo confortável, em local plano, iluminado e seguro.' },
    { name: 'Alongamento de braços e pernas', type: 'flexibility', duration: 10, description: 'Segure cada posição por 20 a 30 segundos, sem sentir dor.' }
];

const MEALS = [
    { key: 'breakfast', label: 'Café da manhã', time: '07:30' },
    { key: 'lunch', label: 'Almoço', time: '12:00' },
    { key: 'snacks', label: 'Lanche', time: '15:30' },
    { key: 'dinner', label: 'Jantar', time: '19:00' }
];

const MOODS = [
    { value: 5, emoji: '😄', label: 'Muito bem' },
    { value: 4, emoji: '🙂', label: 'Bem' },
    { value: 3, emoji: '😐', label: 'Mais ou menos' },
    { value: 2, emoji: '😟', label: 'Triste' },
    { value: 1, emoji: '😣', label: 'Mal' }
];

const SYMPTOMS = ['Dor', 'Tontura', 'Cansaço', 'Falta de apetite', 'Dormi mal', 'Sede'];

const WEEKDAYS_SHORT = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];
const DIET_DAYS = ['Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado', 'Domingo'];
const WEEKLY_EXERCISE_GOAL = 3;

// ==================== INICIALIZAÇÃO ====================
document.addEventListener('DOMContentLoaded', async () => {
    loadFromStorage();
    await ensurePasswordHash();
    applySettings();
    initNavigation();
    initModals();
    initSettings();
    initCaregiverScreen();
    initEmergencyScreen();
    initPhotoPicker();

    document.addEventListener('visibilitychange', () => {
        if (!document.hidden) refreshCurrentScreen();
    });
});

// Chamado pelo MainActivity ao pressionar o botão voltar do Android
window.CuidarBack = function () {
    const openModal = document.querySelector('.modal-overlay.active');
    if (openModal) {
        closeModal(openModal.id);
        return true;
    }
    return goBack();
};

// ==================== ARMAZENAMENTO ====================
function loadFromStorage() {
    try {
        const savedSettings = localStorage.getItem(STORAGE_SETTINGS);
        if (savedSettings) {
            App.settings = { ...DEFAULT_SETTINGS, ...JSON.parse(savedSettings) };
        }
        const savedData = localStorage.getItem(STORAGE_DATA);
        if (savedData) {
            App.data = { ...JSON.parse(JSON.stringify(DEFAULT_DATA)), ...JSON.parse(savedData) };
        }
    } catch (e) {
        console.error('Erro ao carregar dados:', e);
    }
    migrateLegacyData();
}

function migrateLegacyData() {
    App.data.exercises.forEach(ex => {
        if (!ex.type) ex.type = 'strength';
        if (!Array.isArray(ex.days)) ex.days = [0, 1, 2, 3, 4, 5, 6];
    });

    // Versão 1.2 gravava marcações como chaves soltas (med_ID_DataString)
    const legacy = /^(med|ex)_(\d+)_(.+)$/;
    Object.keys(localStorage).forEach(key => {
        const match = key.match(legacy);
        if (!match) return;
        const date = new Date(match[3]);
        if (!isNaN(date)) {
            const log = getLog(dateKey(date));
            const list = match[1] === 'med' ? log.meds : log.exercises;
            const id = Number(match[2]);
            if (!list.includes(id)) list.push(id);
        }
        localStorage.removeItem(key);
    });
}

async function ensurePasswordHash() {
    if (App.settings.password) {
        App.settings.passwordHash = await hashPassword(App.settings.password);
        delete App.settings.password;
        saveToStorage();
    }
    if (!App.settings.passwordHash) {
        App.settings.passwordHash = await hashPassword(DEFAULT_PASSWORD);
        saveToStorage();
    }
}

function saveToStorage() {
    try {
        localStorage.setItem(STORAGE_SETTINGS, JSON.stringify(App.settings));
        localStorage.setItem(STORAGE_DATA, JSON.stringify(App.data));
    } catch (e) {
        console.error('Erro ao salvar dados:', e);
        showToast('Não foi possível salvar. Memória cheia?', 'error');
    }
}

async function hashPassword(value) {
    const text = 'cuidarapp:' + value;
    if (window.crypto && crypto.subtle) {
        const buffer = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(text));
        return Array.from(new Uint8Array(buffer)).map(b => b.toString(16).padStart(2, '0')).join('');
    }
    let hash = 0;
    for (let i = 0; i < text.length; i++) hash = ((hash << 5) - hash + text.charCodeAt(i)) | 0;
    return 'h' + hash;
}

// ==================== DATAS E REGISTROS DIÁRIOS ====================
function dateKey(date = new Date()) {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
}

function getLog(key = dateKey()) {
    if (!App.data.log[key]) {
        App.data.log[key] = { meds: [], exercises: [], meals: [], water: 0, mood: 0, symptoms: [], exerciseFeedback: {} };
    }
    const log = App.data.log[key];
    log.meds = log.meds || [];
    log.exercises = log.exercises || [];
    log.meals = log.meals || [];
    log.symptoms = log.symptoms || [];
    log.exerciseFeedback = log.exerciseFeedback || {};
    log.water = log.water || 0;
    return log;
}

function peekLog(key) {
    return App.data.log[key] || null;
}

function lastDays(count) {
    const days = [];
    for (let i = count - 1; i >= 0; i--) {
        const d = new Date();
        d.setHours(12, 0, 0, 0);
        d.setDate(d.getDate() - i);
        days.push(d);
    }
    return days;
}

function dietIndexFor(date) {
    return (date.getDay() + 6) % 7;
}

function isMedicationScheduled(med, date) {
    const day = date.getDay();
    if (med.frequency === 'weekdays') return day >= 1 && day <= 5;
    if (med.frequency === 'weekly') return day === 1;
    return true;
}

function isExerciseScheduled(ex, date) {
    return !Array.isArray(ex.days) || ex.days.includes(date.getDay());
}

function mealsFor(date) {
    const diet = App.data.diet[dietIndexFor(date)] || {};
    return MEALS.map(meal => ({ ...meal, menu: diet[meal.key] || '' }));
}

function dayStats(date) {
    const key = dateKey(date);
    const log = peekLog(key) || { meds: [], exercises: [], meals: [], water: 0, mood: 0 };
    const meds = App.data.medications.filter(m => isMedicationScheduled(m, date));
    const exercises = App.data.exercises.filter(e => isExerciseScheduled(e, date));
    const medsDone = meds.filter(m => log.meds.includes(m.id)).length;
    const exDone = exercises.filter(e => log.exercises.includes(e.id)).length;
    const mealsDone = MEALS.filter(m => log.meals.includes(m.key)).length;
    const waterDone = Math.min(log.water || 0, App.settings.waterGoal);
    const total = meds.length + exercises.length + MEALS.length + 1;
    const done = medsDone + exDone + mealsDone + (waterDone >= App.settings.waterGoal ? 1 : 0);
    return {
        meds: { done: medsDone, total: meds.length },
        exercises: { done: exDone, total: exercises.length },
        meals: { done: mealsDone, total: MEALS.length },
        water: { done: log.water || 0, total: App.settings.waterGoal },
        mood: log.mood || 0,
        hasActivity: !!peekLog(key),
        pct: Math.round((done / total) * 100)
    };
}

function weeklyExerciseSessions() {
    const days = lastDays(7);
    return days.filter(d => {
        const log = peekLog(dateKey(d));
        return log && log.exercises && log.exercises.length > 0;
    }).length;
}

function proteinGoal() {
    const kg = Number(App.settings.weightKg);
    if (!kg) return null;
    const [min, max] = App.settings.frail ? [1.2, 1.5] : [1.0, 1.2];
    return { min: Math.round(kg * min), max: Math.round(kg * max), perKg: App.settings.frail ? '1,2 a 1,5' : '1,0 a 1,2' };
}

// ==================== APARÊNCIA ====================
function applySettings() {
    document.documentElement.style.setProperty('--font-scale', App.settings.fontSize / 100);
    document.body.classList.toggle('no-animations', !App.settings.animationsEnabled);
    document.body.classList.toggle('high-contrast', !!App.settings.highContrast);
    updateDisplayNames();
}

function updateDisplayNames() {
    const s = App.settings;
    setText('elderly-name', s.elderlyName);
    setText('caregiver-name', s.caregiverName);
    setText('elderly-greeting-name', firstName(s.elderlyName));
    setText('caregiver-greeting-name', firstName(s.caregiverName));
    setText('caregiver-elderly-name', s.elderlyName);
    setText('display-elderly-name', s.elderlyName);
    setText('display-caregiver-name', s.caregiverName);
    setText('display-emergency-phone', s.emergencyPhone || 'Não informado');
    setText('display-caregiver-phone', s.caregiverPhone || 'Não informado');
    setText('emergency-number-label', s.emergencyPhone || '192');
    setText('emergency-caregiver-label', firstName(s.caregiverName));
    setText('font-size-value', s.fontSize + '%');
    setText('water-goal-value', s.waterGoal);
    setText('display-water-ml', `${s.waterGoal} copos de 250 ml (${formatLiters(s.waterGoal * CUP_ML)})`);

    const protein = proteinGoal();
    setText('display-weight', s.weightKg
        ? `${s.weightKg} kg • Proteína: ${protein.min}–${protein.max} g/dia`
        : 'Informe para calcular a meta de proteína');

    document.querySelectorAll('.device-option').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.device === s.deviceOwner);
    });
    setText('device-desc', s.deviceOwner === 'elderly'
        ? 'O idoso usa o app sozinho no dia a dia'
        : 'O cuidador registra as tarefas pelo idoso');

    setToggle('toggle-animations', s.animationsEnabled);
    setToggle('toggle-contrast', s.highContrast);
    setToggle('toggle-frail', s.frail);

    const caregiverCallBtn = document.getElementById('btn-call-caregiver');
    if (caregiverCallBtn) caregiverCallBtn.hidden = !s.caregiverPhone;

    renderAvatars();
    renderHomeOrder();
}

function renderAvatars() {
    const { elderlyPhoto, caregiverPhoto } = App.settings;
    setAvatar('elderly-avatar', elderlyPhoto, ICONS.user);
    setAvatar('caregiver-avatar', caregiverPhoto, ICONS.users);
    setAvatar('elderly-header-avatar', elderlyPhoto, ICONS.user);
    setAvatar('settings-elderly-avatar', elderlyPhoto, ICONS.user);
    setAvatar('settings-caregiver-avatar', caregiverPhoto, ICONS.users);
    document.getElementById('elderly-header-avatar').classList.add('elderly');
}

function setAvatar(id, photo, fallbackIcon) {
    const el = document.getElementById(id);
    if (!el) return;
    el.innerHTML = photo ? `<img src="${photo}" alt="">` : fallbackIcon;
}

function renderHomeOrder() {
    const onCaregiverPhone = App.settings.deviceOwner === 'caregiver';
    document.getElementById('profile-cards').classList.toggle('caregiver-first', onCaregiverPhone);
    document.getElementById('btn-enter-elderly').classList.toggle('primary-device', !onCaregiverPhone);
    setText('elderly-role', onCaregiverPhone
        ? `Registrar as tarefas de ${firstName(App.settings.elderlyName)}`
        : 'Ver minhas tarefas do dia');
}

// ==================== NAVEGAÇÃO ====================
function showScreen(screenId, options = {}) {
    const current = document.querySelector('.screen.active');
    const next = document.getElementById(screenId);
    if (!next) return;

    if (current && current.id !== screenId && !options.isBack) {
        App.history.push(current.id);
    }
    if (current && current.id !== screenId) {
        current.classList.remove('active', 'back');
    }

    next.classList.toggle('back', !!options.isBack);
    next.classList.remove('active');
    void next.offsetWidth;
    next.classList.add('active');

    const content = next.querySelector('.screen-content');
    if (content && !options.keepScroll) content.scrollTop = 0;

    renderScreen(screenId);
}

function goBack() {
    const current = document.querySelector('.screen.active');
    if (!current || current.id === 'screen-home') return false;

    if (current.id === 'screen-edit' && App.editStack.length > 1) {
        App.editStack.pop();
        const prev = App.editStack[App.editStack.length - 1];
        renderEdit(prev.type, prev.id);
        return true;
    }

    if (current.id === 'screen-edit') App.editStack = [];
    if (current.id === 'screen-elderly' || current.id === 'screen-caregiver') {
        App.history = [];
        showScreen('screen-home', { isBack: true });
        return true;
    }

    const prev = App.history.pop() || 'screen-home';
    showScreen(prev, { isBack: true });
    return true;
}

function goHome() {
    App.history = [];
    App.editStack = [];
    showScreen('screen-home', { isBack: true });
}

function renderScreen(screenId) {
    if (screenId === 'screen-elderly') renderElderly();
    else if (screenId === 'screen-caregiver') renderCaregiver();
    else if (screenId === 'screen-settings' || screenId === 'screen-emergency') updateDisplayNames();
}

function refreshCurrentScreen() {
    const current = document.querySelector('.screen.active');
    if (current) renderScreen(current.id);
}

function initNavigation() {
    document.getElementById('btn-enter-elderly').addEventListener('click', () => {
        setCategory('all');
        showScreen('screen-elderly');
        document.querySelector('.categories-scroll').scrollLeft = 0;
    });
    document.getElementById('btn-enter-caregiver').addEventListener('click', showPasswordModal);
    document.getElementById('btn-elderly-home').addEventListener('click', goHome);
    document.getElementById('btn-caregiver-home').addEventListener('click', goHome);
    document.getElementById('btn-caregiver-settings').addEventListener('click', () => showScreen('screen-settings'));
    document.getElementById('btn-emergency').addEventListener('click', () => showScreen('screen-emergency'));

    document.querySelectorAll('[data-back]').forEach(btn => btn.addEventListener('click', goBack));

    document.querySelectorAll('.category-pill, #elderly-nav .nav-item[data-category]').forEach(el => {
        el.addEventListener('click', () => {
            setCategory(el.dataset.category);
            document.getElementById('elderly-content').scrollTop = 0;
            renderElderly();
        });
    });

    document.querySelectorAll('#caregiver-nav .nav-item').forEach(item => {
        item.addEventListener('click', () => {
            const action = item.dataset.action;
            if (action === 'top') {
                document.getElementById('caregiver-content').scrollTo({ top: 0, behavior: App.settings.animationsEnabled ? 'smooth' : 'auto' });
            } else if (action === 'settings') {
                showScreen('screen-settings');
            } else {
                openEditScreen(action, null);
            }
        });
    });
}

function setCategory(category) {
    App.currentCategory = category;
    document.querySelectorAll('.category-pill').forEach(p => {
        const active = p.dataset.category === category;
        p.classList.toggle('active', active);
        p.setAttribute('aria-selected', active);
        if (active) p.scrollIntoView({ block: 'nearest', inline: 'nearest' });
    });
    document.querySelectorAll('#elderly-nav .nav-item[data-category]').forEach(n => {
        n.classList.toggle('active', n.dataset.category === category);
    });
}

// ==================== MODAIS ====================
function openModal(id) {
    document.getElementById(id).classList.add('active');
}

function closeModal(id) {
    const modal = document.getElementById(id);
    modal.classList.remove('active');
    if (modal._onClose) {
        const cb = modal._onClose;
        modal._onClose = null;
        cb();
    }
}

function initModals() {
    document.querySelectorAll('.modal-overlay').forEach(modal => {
        modal.addEventListener('click', e => {
            if (e.target === modal) closeModal(modal.id);
        });
    });

    const pwInput = document.getElementById('password-input');
    document.getElementById('password-cancel').addEventListener('click', () => closeModal('password-modal'));
    document.getElementById('password-confirm').addEventListener('click', validatePassword);
    pwInput.addEventListener('keyup', e => {
        if (e.key === 'Enter' || pwInput.value.length === 4) validatePassword();
    });

    document.getElementById('confirm-cancel').addEventListener('click', () => closeModal('confirm-modal'));
    document.getElementById('input-cancel').addEventListener('click', () => closeModal('input-modal'));
}

function showPasswordModal() {
    const input = document.getElementById('password-input');
    input.value = '';
    input.classList.remove('error');
    document.getElementById('password-error').classList.remove('show');
    openModal('password-modal');
    setTimeout(() => input.focus(), 100);
}

let validatingPassword = false;
async function validatePassword() {
    if (validatingPassword) return;
    const input = document.getElementById('password-input');
    const errorEl = document.getElementById('password-error');
    if (!input.value) return;

    validatingPassword = true;
    const ok = (await hashPassword(input.value)) === App.settings.passwordHash;
    validatingPassword = false;

    if (ok) {
        input.blur();
        closeModal('password-modal');
        showScreen('screen-caregiver');
    } else {
        input.classList.add('error');
        errorEl.classList.add('show');
        input.value = '';
        input.focus();
        setTimeout(() => input.classList.remove('error'), 500);
    }
}

function showConfirm(title, message, onConfirm, confirmText = 'Confirmar') {
    setText('confirm-title', title);
    setText('confirm-message', message);
    const ok = document.getElementById('confirm-ok');
    ok.textContent = confirmText;
    ok.onclick = () => {
        closeModal('confirm-modal');
        onConfirm();
    };
    openModal('confirm-modal');
}

// Substitui window.prompt, que não aparece no WebView do Android
function askInput({ title, hint = '', value = '', type = 'text', inputmode = '', placeholder = '', maxlength = 60, okText = 'Salvar', validate }) {
    return new Promise(resolve => {
        const modal = document.getElementById('input-modal');
        const field = document.getElementById('input-field');
        const errorEl = document.getElementById('input-error');
        const okBtn = document.getElementById('input-ok');

        setText('input-title', title);
        setText('input-hint', hint);
        field.type = type;
        field.value = value;
        field.placeholder = placeholder;
        field.maxLength = maxlength;
        field.classList.toggle('modal-input-text', type !== 'password');
        if (inputmode) field.setAttribute('inputmode', inputmode); else field.removeAttribute('inputmode');
        errorEl.classList.remove('show');
        okBtn.textContent = okText;

        let settled = false;
        const finish = result => {
            if (settled) return;
            settled = true;
            field.onkeyup = null;
            okBtn.onclick = null;
            resolve(result);
        };

        const submit = () => {
            const v = field.value.trim();
            const error = validate ? validate(v) : (v ? '' : 'Preencha o campo');
            if (error) {
                errorEl.textContent = error;
                errorEl.classList.add('show');
                field.classList.add('error');
                setTimeout(() => field.classList.remove('error'), 400);
                return;
            }
            field.blur();
            modal._onClose = null;
            modal.classList.remove('active');
            finish(v);
        };

        okBtn.onclick = submit;
        field.onkeyup = e => { if (e.key === 'Enter') submit(); };
        modal._onClose = () => finish(null);

        openModal('input-modal');
        setTimeout(() => { field.focus(); field.select(); }, 100);
    });
}

function askChoice(title, message, options) {
    return new Promise(resolve => {
        const modal = document.getElementById('choice-modal');
        setText('choice-title', title);
        setText('choice-message', message);
        const container = document.getElementById('choice-options');
        container.innerHTML = options.map((o, i) => `
            <button class="choice-btn ${o.style || ''}" data-index="${i}">
                ${o.emoji ? `<span class="choice-emoji">${o.emoji}</span>` : ''}
                <span>${escapeHtml(o.label)}</span>
            </button>
        `).join('');

        let settled = false;
        modal._onClose = () => { if (!settled) { settled = true; resolve(null); } };
        container.querySelectorAll('.choice-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                settled = true;
                modal._onClose = null;
                modal.classList.remove('active');
                resolve(options[Number(btn.dataset.index)].value);
            });
        });
        openModal('choice-modal');
    });
}

function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    container.appendChild(toast);
    setTimeout(() => {
        toast.style.transition = 'opacity 300ms, transform 300ms';
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(20px)';
        setTimeout(() => toast.remove(), 300);
    }, 2500);
}

// ==================== ALERTAS PARA O CUIDADOR ====================
function addAlert(type, message) {
    const now = new Date();
    App.data.alerts.unshift({
        id: Date.now(),
        type,
        message,
        date: dateKey(now),
        time: now.toTimeString().slice(0, 5),
        seen: false
    });
    App.data.alerts = App.data.alerts.slice(0, 50);
    saveToStorage();
}

// ==================== TELA DO IDOSO ====================
function renderElderly() {
    const now = new Date();
    const hour = now.getHours();
    setText('elderly-salutation', hour < 12 ? 'Bom dia' : hour < 18 ? 'Boa tarde' : 'Boa noite');
    const dateText = now.toLocaleDateString('pt-BR', { weekday: 'long', day: 'numeric', month: 'long' });
    setText('elderly-date', dateText.charAt(0).toUpperCase() + dateText.slice(1));

    const body = document.getElementById('elderly-body');
    const renderers = {
        all: renderElderlyToday,
        medication: renderElderlyMeds,
        exercise: renderElderlyExercises,
        food: renderElderlyFood,
        health: renderElderlyHealth
    };
    body.innerHTML = assistBanner() + (renderers[App.currentCategory] || renderElderlyToday)();
    bindElderlyEvents(body);
}

function assistBanner() {
    if (App.settings.deviceOwner !== 'caregiver') return '';
    return `<div class="assist-banner">${ICONS.users}<span>${escapeHtml(firstName(App.settings.caregiverName))} está registrando por ${escapeHtml(firstName(App.settings.elderlyName))}</span></div>`;
}

function renderElderlyToday() {
    const today = new Date();
    const stats = dayStats(today);
    const pending = (stats.meds.total - stats.meds.done) + (stats.exercises.total - stats.exercises.done) + (stats.meals.total - stats.meals.done);
    const log = getLog();

    let message;
    if (stats.pct >= 100) message = 'Parabéns! Você fez tudo hoje.';
    else if (pending === 0) message = 'Falta só beber água!';
    else message = `Faltam ${pending} ${pending === 1 ? 'tarefa' : 'tarefas'} para hoje.`;

    const tasks = [
        ...medicationTasks(today),
        ...exerciseTasks(today),
        ...mealTasks(today)
    ].sort((a, b) => (a.done - b.done) || (a.time || '99').localeCompare(b.time || '99'));

    return `
        ${summaryCard(stats.pct, message)}
        ${waterCard()}
        ${!log.mood ? `
            <button class="cta-card" data-goto="health">
                <span class="cta-emoji">🙂</span>
                <span class="task-info">
                    <span class="task-title">Como você está hoje?</span>
                    <span class="task-subtitle">Toque para contar ao seu cuidador</span>
                </span>
                <span class="profile-arrow">${ICONS.chevron}</span>
            </button>` : ''}
        <section class="section">
            <div class="section-header"><h2 class="section-title">Tarefas de Hoje</h2></div>
            ${tasks.length ? tasks.map(taskCard).join('') : emptyState('Tudo em dia!', 'Não há tarefas para hoje.')}
        </section>
        ${visitsSection()}
    `;
}

function renderElderlyMeds() {
    const today = new Date();
    const tasks = medicationTasks(today).sort((a, b) => (a.done - b.done) || (a.time || '99').localeCompare(b.time || '99'));
    const others = App.data.medications.filter(m => !isMedicationScheduled(m, today));
    return `
        <section class="section section-first">
            <div class="section-header"><h2 class="section-title">Remédios de Hoje</h2></div>
            ${tasks.length ? tasks.map(taskCard).join('') : emptyState('Nenhum remédio hoje', 'Seu cuidador pode cadastrar os remédios.')}
        </section>
        ${others.length ? `
        <section class="section">
            <div class="section-header"><h2 class="section-title">Em outros dias</h2></div>
            ${others.map(m => infoCard('medication', ICONS.pill, m.name, `${m.dosage} • ${frequencyLabel(m.frequency)}`)).join('')}
        </section>` : ''}
        <div class="tip-card">${ICONS.info}<div><strong>Dica</strong>Tome os remédios sempre no mesmo horário e marque aqui logo depois.</div></div>
    `;
}

function renderElderlyExercises() {
    const today = new Date();
    const sessions = weeklyExerciseSessions();
    const pct = Math.min(100, Math.round((sessions / WEEKLY_EXERCISE_GOAL) * 100));
    const tasks = exerciseTasks(today);
    const others = App.data.exercises.filter(e => !isExerciseScheduled(e, today));

    return `
        <div class="task-card">
            <div class="task-avatar" style="background: var(--color-exercise); color: var(--color-exercise-icon);">${ICONS.exercise}</div>
            <div class="task-info">
                <div class="task-title">${sessions} ${sessions === 1 ? 'dia ativo' : 'dias ativos'} na semana</div>
                <div class="task-subtitle">${sessions >= WEEKLY_EXERCISE_GOAL ? 'Meta alcançada! Continue assim.' : 'Meta: exercitar-se de 3 a 5 vezes por semana'}</div>
                <div class="bar"><div class="bar-fill" style="width:${pct}%; background: var(--color-exercise-icon);"></div></div>
            </div>
        </div>
        <section class="section">
            <div class="section-header"><h2 class="section-title">Exercícios de Hoje</h2></div>
            ${tasks.length ? tasks.map(taskCard).join('') : emptyState('Dia de descanso', 'Não há exercícios marcados para hoje.')}
        </section>
        ${others.length ? `
        <section class="section">
            <div class="section-header"><h2 class="section-title">Outros dias da semana</h2></div>
            ${others.map(e => infoCard('exercise', ICONS.exercise, e.name, `${EXERCISE_TYPES[e.type]} • ${daysLabel(e.days)}`)).join('')}
        </section>` : ''}
        <div class="tip-card exercise">${ICONS.alert}<div><strong>Segurança em primeiro lugar</strong>Faça perto de uma cadeira firme. Se sentir tontura ou dor, pare e use o botão SOS.</div></div>
    `;
}

function renderElderlyFood() {
    const today = new Date();
    const protein = proteinGoal();
    return `
        ${waterCard()}
        <section class="section section-first">
            <div class="section-header"><h2 class="section-title">Refeições de Hoje</h2></div>
            ${mealTasks(today).map(taskCard).join('')}
        </section>
        <div class="tip-card food">${ICONS.food}<div>
            <strong>Proteína em todas as refeições</strong>
            ${protein ? `Sua meta é de ${protein.min} a ${protein.max} g por dia. ` : ''}Ovos, feijão, lentilha, frango, peixe, leite e iogurte ajudam a manter a força dos músculos.
        </div></div>
        <div class="tip-card">${ICONS.water}<div><strong>Beba água mesmo sem sede</strong>Com a idade, a sede diminui. Deixe um copo sempre por perto.</div></div>
    `;
}

function renderElderlyHealth() {
    const log = getLog();
    const days = lastDays(7);
    return `
        <div class="mood-card">
            <div class="mood-question">Como você está se sentindo?</div>
            <div class="mood-grid">
                ${MOODS.map(m => `
                    <button class="mood-btn ${log.mood === m.value ? 'selected' : ''}" data-mood="${m.value}" aria-label="${m.label}">
                        <span class="mood-emoji">${m.emoji}</span>
                        <span class="mood-label">${m.label}</span>
                    </button>`).join('')}
            </div>
        </div>
        <section class="section">
            <div class="section-header"><h2 class="section-title">Está sentindo algo?</h2></div>
            <div class="chips">
                ${SYMPTOMS.map(s => `<button class="chip ${log.symptoms.includes(s) ? 'selected' : ''}" data-symptom="${s}">${s}</button>`).join('')}
            </div>
            <button class="fall-btn" data-fall>${ICONS.alert}Tive uma queda</button>
        </section>
        <section class="section">
            <div class="section-header"><h2 class="section-title">Minha semana</h2></div>
            <div class="mood-week">
                ${days.map(d => {
                    const l = peekLog(dateKey(d));
                    const mood = l && l.mood ? MOODS.find(m => m.value === l.mood).emoji : '·';
                    return `<div><div class="mood-week-day">${WEEKDAYS_SHORT[d.getDay()]}</div><div class="mood-week-emoji">${mood}</div></div>`;
                }).join('')}
            </div>
        </section>
    `;
}

function medicationTasks(date) {
    const log = getLog(dateKey(date));
    return App.data.medications.filter(m => isMedicationScheduled(m, date)).map(m => ({
        id: m.id,
        type: 'medication',
        title: m.name,
        subtitle: m.dosage,
        detail: m.instructions,
        time: m.time,
        done: log.meds.includes(m.id),
        icon: ICONS.pill,
        color: 'var(--color-medication)',
        iconColor: 'var(--color-medication-icon)'
    }));
}

function exerciseTasks(date) {
    const log = getLog(dateKey(date));
    return App.data.exercises.filter(e => isExerciseScheduled(e, date)).map(e => ({
        id: e.id,
        type: 'exercise',
        title: e.name,
        subtitle: `${e.duration} min`,
        tag: e.type,
        detail: e.description,
        time: e.time,
        done: log.exercises.includes(e.id),
        icon: ICONS.exercise,
        color: 'var(--color-exercise)',
        iconColor: 'var(--color-exercise-icon)'
    }));
}

function mealTasks(date) {
    const log = getLog(dateKey(date));
    return mealsFor(date).map(meal => ({
        id: meal.key,
        type: 'meal',
        title: meal.label,
        subtitle: meal.menu || 'Toque no círculo quando comer',
        time: meal.time,
        done: log.meals.includes(meal.key),
        icon: ICONS.food,
        color: 'var(--color-food)',
        iconColor: 'var(--color-food-icon)'
    }));
}

function taskCard(task) {
    return `
        <div class="task-card ${task.done ? 'done' : ''}">
            <div class="task-avatar" style="background: ${task.color}; color: ${task.iconColor};">${task.icon}</div>
            <div class="task-info">
                <div class="task-title">${escapeHtml(task.title)}</div>
                <div class="task-subtitle">${escapeHtml(task.subtitle)}</div>
                ${task.detail && !task.done ? `<div class="task-detail">${escapeHtml(task.detail)}</div>` : ''}
                ${task.time || task.tag ? `<div class="task-badges">
                    ${task.time ? `<span class="task-badge">${ICONS.clock}${task.time}</span>` : ''}
                    ${task.tag ? `<span class="type-tag type-${task.tag}">${EXERCISE_TYPES[task.tag]}</span>` : ''}
                </div>` : ''}
            </div>
            <button class="task-check ${task.done ? 'checked' : ''}" data-task-type="${task.type}" data-task-id="${task.id}"
                aria-label="${task.done ? 'Desmarcar' : 'Marcar como feito'}: ${escapeHtml(task.title)}" aria-pressed="${task.done}">
                ${ICONS.check}
            </button>
        </div>
    `;
}

function infoCard(kind, icon, title, subtitle, onclick = '') {
    return `
        <div class="task-card" ${onclick ? `onclick="${onclick}" role="button" tabindex="0"` : ''}>
            <div class="task-avatar" style="background: var(--color-${kind}); color: var(--color-${kind}-icon);">${icon}</div>
            <div class="task-info">
                <div class="task-title">${escapeHtml(title)}</div>
                <div class="task-subtitle">${escapeHtml(subtitle)}</div>
            </div>
            ${onclick ? `<div class="profile-arrow">${ICONS.chevron}</div>` : ''}
        </div>
    `;
}

function summaryCard(pct, message) {
    const circumference = 2 * Math.PI * 34;
    const offset = circumference * (1 - pct / 100);
    return `
        <div class="summary-card">
            <div class="summary-ring">
                <svg viewBox="0 0 80 80"><circle class="ring-bg" cx="40" cy="40" r="34" fill="none" stroke-width="8"/>
                <circle class="ring-fg" cx="40" cy="40" r="34" fill="none" stroke-width="8" stroke-dasharray="${circumference}" stroke-dashoffset="${offset}"/></svg>
                <div class="summary-ring-value">${pct}%</div>
            </div>
            <div>
                <div class="summary-title">Seu dia</div>
                <div class="summary-text">${escapeHtml(message)}</div>
            </div>
        </div>
    `;
}

function waterCard() {
    const log = getLog();
    const goal = App.settings.waterGoal;
    const cups = Array.from({ length: Math.max(goal, log.water) }, (_, i) =>
        `<span class="water-cup ${i < log.water ? 'filled' : ''}"></span>`).join('');
    const status = log.water >= goal ? 'Meta do dia alcançada!' : `Faltam ${goal - log.water} ${goal - log.water === 1 ? 'copo' : 'copos'}`;
    return `
        <div class="water-card">
            <div class="water-top">
                <div class="water-icon">${ICONS.water}</div>
                <div class="water-info">
                    <div class="task-title">Água: ${log.water} de ${goal} copos</div>
                    <div class="task-subtitle">${status}</div>
                </div>
                <div class="water-actions">
                    <button class="round-btn subtle" data-water="-1" aria-label="Tirar um copo" ${log.water ? '' : 'disabled'}>−</button>
                    <button class="round-btn primary" data-water="1" aria-label="Bebi um copo de água">+</button>
                </div>
            </div>
            <div class="water-cups" aria-hidden="true">${cups}</div>
        </div>
    `;
}

function visitsSection() {
    const visits = upcomingVisits().slice(0, 3);
    return `
        <section class="section">
            <div class="section-header"><h2 class="section-title">Próximas Visitas</h2></div>
            ${visits.length
                ? visits.map(v => infoCard('visit', ICONS.calendar, formatDate(v.date), `${v.description || 'Visita'}${v.time ? ' • ' + v.time : ''}`)).join('')
                : emptyState('', 'Nenhuma visita agendada.', true)}
        </section>
    `;
}

function bindElderlyEvents(root) {
    root.querySelectorAll('.task-check').forEach(btn => {
        btn.addEventListener('click', () => toggleTask(btn.dataset.taskType, btn.dataset.taskId));
    });
    root.querySelectorAll('[data-water]').forEach(btn => {
        btn.addEventListener('click', () => changeWater(Number(btn.dataset.water)));
    });
    root.querySelectorAll('[data-goto]').forEach(btn => {
        btn.addEventListener('click', () => { setCategory(btn.dataset.goto); renderElderly(); });
    });
    root.querySelectorAll('[data-mood]').forEach(btn => {
        btn.addEventListener('click', () => setMood(Number(btn.dataset.mood)));
    });
    root.querySelectorAll('[data-symptom]').forEach(btn => {
        btn.addEventListener('click', () => toggleSymptom(btn.dataset.symptom));
    });
    root.querySelectorAll('[data-fall]').forEach(btn => btn.addEventListener('click', registerFall));
}

function rerenderElderlyKeepingScroll() {
    const content = document.getElementById('elderly-content');
    const top = content.scrollTop;
    renderElderly();
    content.scrollTop = top;
}

async function toggleTask(type, rawId) {
    const log = getLog();
    const listName = { medication: 'meds', exercise: 'exercises', meal: 'meals' }[type];
    const id = type === 'meal' ? rawId : Number(rawId);
    const list = log[listName];
    const index = list.indexOf(id);

    if (index >= 0) {
        list.splice(index, 1);
        if (type === 'exercise') delete log.exerciseFeedback[id];
        saveToStorage();
        rerenderElderlyKeepingScroll();
        showToast('Marcação desfeita');
        return;
    }

    list.push(id);
    saveToStorage();
    rerenderElderlyKeepingScroll();

    if (type === 'medication') showToast('Remédio tomado! 💊', 'success');
    else if (type === 'meal') showToast('Bom apetite! 🍽️', 'success');
    else await askExerciseFeedback(id);
}

async function askExerciseFeedback(exerciseId) {
    const exercise = App.data.exercises.find(e => e.id === exerciseId);
    const feeling = await askChoice('Muito bem! 💪', 'Como você se sentiu depois do exercício?', [
        { value: 'good', label: 'Me senti bem', emoji: '😄' },
        { value: 'tired', label: 'Fiquei cansado', emoji: '😮‍💨' },
        { value: 'dizzy', label: 'Senti tontura ou dor', emoji: '😵', style: 'danger' }
    ]);
    if (!feeling) return;

    getLog().exerciseFeedback[exerciseId] = feeling;
    saveToStorage();

    if (feeling === 'dizzy') {
        addAlert('dizzy', `Sentiu tontura ou dor após "${exercise ? exercise.name : 'exercício'}"`);
        const help = await askChoice('Sente-se e descanse', 'Seu cuidador será avisado. Você precisa de ajuda agora?', [
            { value: 'sos', label: 'Sim, quero ligar', emoji: '📞', style: 'danger' },
            { value: 'ok', label: 'Não, já estou melhor', style: 'muted' }
        ]);
        if (help === 'sos') showScreen('screen-emergency');
    } else {
        showToast(feeling === 'good' ? 'Excelente! Continue assim.' : 'Descanse um pouco e beba água.', 'success');
    }
}

function changeWater(delta) {
    const log = getLog();
    log.water = Math.max(0, log.water + delta);
    saveToStorage();
    rerenderElderlyKeepingScroll();
    if (delta > 0) {
        showToast(log.water === App.settings.waterGoal ? 'Meta de água alcançada! 🎉' : 'Mais um copo! 💧', 'success');
    }
}

function setMood(value) {
    const log = getLog();
    const wasLow = log.mood && log.mood <= 2;
    log.mood = value;
    saveToStorage();
    if (value <= 2 && !wasLow) {
        addAlert('mood', `Disse que está se sentindo ${MOODS.find(m => m.value === value).label.toLowerCase()}`);
    }
    rerenderElderlyKeepingScroll();
    showToast(value >= 4 ? 'Que bom! 😊' : 'Obrigado por contar. Seu cuidador vai saber.', 'success');
}

function toggleSymptom(symptom) {
    const log = getLog();
    const i = log.symptoms.indexOf(symptom);
    if (i >= 0) log.symptoms.splice(i, 1);
    else log.symptoms.push(symptom);
    saveToStorage();
    rerenderElderlyKeepingScroll();
}

async function registerFall() {
    const answer = await askChoice('Você caiu?', 'Vamos avisar seu cuidador.', [
        { value: 'hurt', label: 'Caí e preciso de ajuda', emoji: '🚑', style: 'danger' },
        { value: 'ok', label: 'Caí, mas estou bem', emoji: '🙂' },
        { value: null, label: 'Cancelar', style: 'muted' }
    ]);
    if (!answer) return;
    getLog().fall = true;
    addAlert('fall', answer === 'hurt' ? 'Registrou uma queda e pediu ajuda' : 'Registrou uma queda (disse que está bem)');
    if (answer === 'hurt') showScreen('screen-emergency');
    else {
        showToast('Queda registrada. Seu cuidador vai saber.', 'success');
        if (document.getElementById('screen-elderly').classList.contains('active')) rerenderElderlyKeepingScroll();
    }
}

// ==================== TELA DO CUIDADOR ====================
function initCaregiverScreen() {
    document.getElementById('btn-add-medication').addEventListener('click', () => openEditScreen('medication', null));
    document.getElementById('btn-add-exercise').addEventListener('click', () => openEditScreen('exercise', null));
    document.getElementById('btn-add-visit').addEventListener('click', () => openEditScreen('visit', null));
    document.getElementById('btn-edit-diet').addEventListener('click', () => openEditScreen('diet', null));
    document.getElementById('btn-view-all-meds').addEventListener('click', () => openEditScreen('medication-list', null));
    document.getElementById('btn-view-all-visits').addEventListener('click', () => openEditScreen('visit-list', null));
}

function renderCaregiver() {
    renderCaregiverAlerts();
    renderCaregiverProgress();
    renderCaregiverWeek();
    renderCaregiverMeds();
    renderCaregiverVisits();
    document.querySelectorAll('#caregiver-nav .nav-item').forEach(n => n.classList.toggle('active', n.dataset.action === 'top'));
}

function renderCaregiverAlerts() {
    const container = document.getElementById('caregiver-alerts');
    const alerts = App.data.alerts.filter(a => !a.seen);
    const emoji = { fall: '🚨', dizzy: '😵', mood: '😟' };
    const title = { fall: 'Queda registrada', dizzy: 'Mal-estar no exercício', mood: 'Humor baixo' };

    container.innerHTML = alerts.map(a => `
        <div class="alert-card ${a.type === 'mood' ? 'warning' : ''}">
            <span class="alert-emoji">${emoji[a.type] || '⚠️'}</span>
            <div class="alert-body">
                <div class="alert-title">${title[a.type] || 'Alerta'}</div>
                <div class="task-subtitle">${escapeHtml(a.message)}</div>
                <div class="alert-time">${formatDate(a.date)} às ${a.time}</div>
            </div>
            <button class="alert-dismiss" data-alert="${a.id}">Visto</button>
        </div>
    `).join('');

    container.querySelectorAll('[data-alert]').forEach(btn => {
        btn.addEventListener('click', () => {
            const alert = App.data.alerts.find(a => a.id === Number(btn.dataset.alert));
            if (alert) alert.seen = true;
            saveToStorage();
            renderCaregiverAlerts();
        });
    });
}

function renderCaregiverProgress() {
    const stats = dayStats(new Date());
    const log = getLog();
    const mood = log.mood ? MOODS.find(m => m.value === log.mood) : null;

    const stat = (label, color, done, total, unit = '') => {
        const pct = total ? Math.min(100, Math.round((done / total) * 100)) : 0;
        return `
            <div class="stat-card">
                <div class="stat-label"><span class="stat-dot" style="background:${color}"></span>${label}</div>
                <div class="stat-value">${done}<small> / ${total}${unit}</small></div>
                <div class="bar"><div class="bar-fill" style="width:${pct}%; background:${color};"></div></div>
            </div>`;
    };

    document.getElementById('caregiver-progress').innerHTML = `
        <div class="stats-grid">
            ${stat('Remédios', 'var(--color-medication-icon)', stats.meds.done, stats.meds.total)}
            ${stat('Exercícios', 'var(--color-exercise-icon)', stats.exercises.done, stats.exercises.total)}
            ${stat('Refeições', 'var(--color-food-icon)', stats.meals.done, stats.meals.total)}
            ${stat('Água', 'var(--color-water-icon)', stats.water.done, stats.water.total, ' copos')}
        </div>
        <div class="task-card" style="margin-top: var(--spacing-md);">
            <div class="task-avatar" style="background: var(--color-health); font-size: calc(26px * var(--font-scale));">${mood ? mood.emoji : '❔'}</div>
            <div class="task-info">
                <div class="task-title">Humor: ${mood ? mood.label : 'ainda não informado'}</div>
                <div class="task-subtitle">${log.symptoms.length ? 'Sintomas: ' + escapeHtml(log.symptoms.join(', ')) : 'Nenhum sintoma relatado hoje'}</div>
            </div>
        </div>
    `;
}

function renderCaregiverWeek() {
    const days = lastDays(7);
    const stats = days.map(d => ({ date: d, ...dayStats(d) }));
    const active = stats.filter(s => s.hasActivity);
    const avg = active.length ? Math.round(active.reduce((sum, s) => sum + s.pct, 0) / active.length) : 0;
    const avgWater = active.length ? (active.reduce((sum, s) => sum + s.water.done, 0) / active.length) : 0;
    const sessions = weeklyExerciseSessions();
    const protein = proteinGoal();

    document.getElementById('caregiver-week').innerHTML = `
        <div class="week-card">
            <div class="task-title">Adesão à rotina</div>
            <div class="task-subtitle">Tarefas cumpridas por dia (remédios, exercícios, refeições e água)</div>
            <div class="week-chart">
                ${stats.map(s => {
                    const level = !s.hasActivity ? 'empty' : s.pct < 40 ? 'low' : s.pct < 70 ? 'mid' : '';
                    return `<div class="week-col"><span class="week-pct">${s.hasActivity ? s.pct + '%' : ''}</span><div class="week-bar ${level}" style="height:${s.hasActivity ? Math.max(4, s.pct) : 4}%"></div></div>`;
                }).join('')}
            </div>
            <div class="week-labels">
                ${stats.map(s => {
                    const mood = s.mood ? MOODS.find(m => m.value === s.mood).emoji : '·';
                    return `<div><div class="week-label">${WEEKDAYS_SHORT[s.date.getDay()]}</div><div class="week-mood">${mood}</div></div>`;
                }).join('')}
            </div>
            <div class="week-summary">
                <div><div class="week-summary-value">${avg}%</div><div class="week-summary-label">adesão média</div></div>
                <div><div class="week-summary-value">${sessions}</div><div class="week-summary-label">dias com exercício (meta 3 a 5)</div></div>
                <div><div class="week-summary-value">${avgWater.toFixed(1).replace('.', ',')}</div><div class="week-summary-label">copos de água/dia</div></div>
            </div>
        </div>
        ${sessions < WEEKLY_EXERCISE_GOAL ? `<div class="tip-card exercise" style="margin-top: var(--spacing-md);">${ICONS.exercise}<div><strong>Incentive o movimento</strong>Exercícios de força e equilíbrio de 3 a 5 vezes por semana reduzem quedas em até 34%.</div></div>` : ''}
        ${!protein ? `<button class="tip-card food" style="margin-top: var(--spacing-md); width:100%; border:none; text-align:left; cursor:pointer;" id="btn-tip-weight">${ICONS.food}<div><strong>Informe o peso do idoso</strong>Assim o app calcula a meta diária de proteína recomendada (ESPEN).</div></button>`
            : `<div class="tip-card food" style="margin-top: var(--spacing-md);">${ICONS.food}<div><strong>Meta de proteína: ${protein.min}–${protein.max} g/dia</strong>${protein.perKg} g por kg de peso. Inclua proteína em todas as refeições da dieta.</div></div>`}
    `;

    const weightBtn = document.getElementById('btn-tip-weight');
    if (weightBtn) weightBtn.addEventListener('click', editWeight);
}

function renderCaregiverMeds() {
    const container = document.getElementById('caregiver-meds-list');
    const meds = App.data.medications.slice(0, 3);
    container.innerHTML = meds.length
        ? meds.map(m => infoCard('medication', ICONS.pill, m.name, `${m.dosage}${m.time ? ' • ' + m.time : ''}`, `openEditScreen('medication', ${m.id})`)).join('')
        : emptyState('', 'Nenhum medicamento cadastrado', true);
}

function renderCaregiverVisits() {
    const container = document.getElementById('caregiver-visits-list');
    const visits = upcomingVisits().slice(0, 3);
    container.innerHTML = visits.length
        ? visits.map(v => infoCard('visit', ICONS.calendar, formatDate(v.date), `${v.description || 'Visita'}${v.time ? ' • ' + v.time : ''}`, `openEditScreen('visit', ${v.id})`)).join('')
        : emptyState('', 'Nenhuma visita agendada', true);
}

function upcomingVisits() {
    const today = dateKey();
    return App.data.visits
        .filter(v => v.date >= today)
        .sort((a, b) => (a.date + (a.time || '')).localeCompare(b.date + (b.time || '')));
}

// ==================== CONFIGURAÇÕES ====================
function initSettings() {
    document.getElementById('settings-elderly-name').addEventListener('click', () => editName('elderlyName', 'Nome do Idoso'));
    document.getElementById('settings-caregiver-name').addEventListener('click', () => editName('caregiverName', 'Nome do Cuidador'));

    document.querySelectorAll('.device-option').forEach(btn => {
        btn.addEventListener('click', () => {
            App.settings.deviceOwner = btn.dataset.device;
            saveSettings('Configuração salva!');
        });
    });

    document.getElementById('btn-font-decrease').addEventListener('click', () => changeFont(-10));
    document.getElementById('btn-font-increase').addEventListener('click', () => changeFont(10));
    document.getElementById('btn-water-decrease').addEventListener('click', () => changeWaterGoal(-1));
    document.getElementById('btn-water-increase').addEventListener('click', () => changeWaterGoal(1));

    bindToggle('toggle-animations', 'animationsEnabled', on => on ? 'Animações ativadas' : 'Animações desativadas');
    bindToggle('toggle-contrast', 'highContrast', on => on ? 'Alto contraste ativado' : 'Alto contraste desativado');
    bindToggle('toggle-frail', 'frail', () => 'Meta de proteína atualizada');

    document.getElementById('settings-weight').addEventListener('click', editWeight);
    document.getElementById('settings-emergency-phone').addEventListener('click', () => editPhone('emergencyPhone', 'Telefone de Emergência', 'SAMU: 192 • Bombeiros: 193'));
    document.getElementById('settings-caregiver-phone').addEventListener('click', () => editPhone('caregiverPhone', 'Telefone do Cuidador', 'Aparece no botão SOS do idoso'));
    document.getElementById('settings-change-password').addEventListener('click', changePassword);
    document.getElementById('settings-clear-data').addEventListener('click', clearAllData);
}

function saveSettings(message) {
    saveToStorage();
    applySettings();
    if (message) showToast(message, 'success');
}

function bindToggle(id, key, messageFn) {
    document.getElementById(id).addEventListener('click', () => {
        App.settings[key] = !App.settings[key];
        saveSettings(messageFn(App.settings[key]));
    });
}

function changeFont(delta) {
    const next = App.settings.fontSize + delta;
    if (next < 80 || next > 150) return;
    App.settings.fontSize = next;
    saveSettings();
}

function changeWaterGoal(delta) {
    const next = App.settings.waterGoal + delta;
    if (next < 4 || next > 14) return;
    App.settings.waterGoal = next;
    saveSettings();
}

async function editName(key, title) {
    const value = await askInput({ title, value: App.settings[key], maxlength: 40 });
    if (value === null) return;
    App.settings[key] = value;
    saveSettings('Nome atualizado!');
}

async function editWeight() {
    const value = await askInput({
        title: 'Peso do Idoso',
        hint: 'Usado para calcular a meta de proteína (ESPEN: 1,0 a 1,5 g por kg).',
        value: App.settings.weightKg ? String(App.settings.weightKg) : '',
        inputmode: 'decimal',
        placeholder: 'Ex: 65',
        maxlength: 5,
        validate: v => {
            const n = Number(v.replace(',', '.'));
            return n >= 25 && n <= 250 ? '' : 'Informe um peso entre 25 e 250 kg';
        }
    });
    if (value === null) return;
    App.settings.weightKg = Math.round(Number(value.replace(',', '.')) * 10) / 10;
    saveSettings('Peso salvo!');
    refreshCurrentScreen();
}

async function editPhone(key, title, hint) {
    const value = await askInput({
        title,
        hint,
        value: App.settings[key],
        type: 'tel',
        inputmode: 'tel',
        maxlength: 20,
        validate: v => v.replace(/\D/g, '').length >= 3 ? '' : 'Informe um número válido'
    });
    if (value === null) return;
    App.settings[key] = value.replace(/[^\d+]/g, '');
    saveSettings('Telefone atualizado!');
}

async function changePassword() {
    const pinRule = v => /^\d{4}$/.test(v) ? '' : 'A senha deve ter 4 números';
    const pinField = { type: 'password', inputmode: 'numeric', maxlength: 4, placeholder: '••••', okText: 'Continuar' };

    const current = await askInput({ ...pinField, title: 'Senha atual', validate: pinRule });
    if (current === null) return;
    if ((await hashPassword(current)) !== App.settings.passwordHash) {
        showToast('Senha atual incorreta', 'error');
        return;
    }
    const next = await askInput({ ...pinField, title: 'Nova senha', hint: 'Escolha 4 números fáceis de lembrar', validate: pinRule });
    if (next === null) return;
    const confirm = await askInput({ ...pinField, title: 'Repita a nova senha', okText: 'Salvar', validate: v => v === next ? '' : 'As senhas não são iguais' });
    if (confirm === null) return;

    App.settings.passwordHash = await hashPassword(next);
    saveSettings('Senha alterada!');
}

function clearAllData() {
    showConfirm('Limpar Dados', 'Todos os remédios, exercícios, registros e configurações serão apagados. Esta ação não pode ser desfeita.', async () => {
        localStorage.removeItem(STORAGE_SETTINGS);
        localStorage.removeItem(STORAGE_DATA);
        App.settings = { ...DEFAULT_SETTINGS };
        App.data = JSON.parse(JSON.stringify(DEFAULT_DATA));
        await ensurePasswordHash();
        applySettings();
        showToast('Dados apagados', 'success');
        goHome();
    }, 'Apagar tudo');
}

// ==================== FOTOS DE PERFIL ====================
let photoTarget = null;

function initPhotoPicker() {
    const input = document.getElementById('photo-input');
    document.querySelectorAll('[data-photo]').forEach(btn => {
        btn.addEventListener('click', async () => {
            const key = btn.dataset.photo === 'elderly' ? 'elderlyPhoto' : 'caregiverPhoto';
            if (App.settings[key]) {
                const action = await askChoice('Foto do perfil', '', [
                    { value: 'change', label: 'Escolher outra foto', emoji: '🖼️' },
                    { value: 'remove', label: 'Remover foto', emoji: '🗑️', style: 'danger' },
                    { value: null, label: 'Cancelar', style: 'muted' }
                ]);
                if (action === 'remove') {
                    App.settings[key] = '';
                    saveSettings('Foto removida');
                    return;
                }
                if (action !== 'change') return;
            }
            photoTarget = key;
            input.value = '';
            input.click();
        });
    });

    input.addEventListener('change', () => {
        const file = input.files && input.files[0];
        if (!file || !photoTarget) return;
        resizeImage(file, 256).then(dataUrl => {
            App.settings[photoTarget] = dataUrl;
            saveSettings('Foto atualizada!');
        }).catch(() => showToast('Não foi possível usar esta foto', 'error'));
    });
}

function resizeImage(file, size) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onerror = reject;
        reader.onload = () => {
            const img = new Image();
            img.onerror = reject;
            img.onload = () => {
                const canvas = document.createElement('canvas');
                canvas.width = size;
                canvas.height = size;
                const side = Math.min(img.width, img.height);
                const sx = (img.width - side) / 2;
                const sy = (img.height - side) / 2;
                canvas.getContext('2d').drawImage(img, sx, sy, side, side, 0, 0, size, size);
                resolve(canvas.toDataURL('image/jpeg', 0.85));
            };
            img.src = reader.result;
        };
        reader.readAsDataURL(file);
    });
}

// ==================== EMERGÊNCIA ====================
function initEmergencyScreen() {
    document.getElementById('btn-call-emergency').addEventListener('click', () => callNumber(App.settings.emergencyPhone || '192'));
    document.getElementById('btn-call-caregiver').addEventListener('click', () => callNumber(App.settings.caregiverPhone));
    document.getElementById('btn-register-fall').addEventListener('click', registerFall);
}

function callNumber(number) {
    if (!number) return;
    window.location.href = `tel:${number}`;
}

// ==================== TELA DE EDIÇÃO ====================
const EDIT_TITLES = {
    'medication': id => id ? 'Editar Medicamento' : 'Novo Medicamento',
    'exercise': id => id ? 'Editar Exercício' : 'Novo Exercício',
    'visit': id => id ? 'Editar Visita' : 'Nova Visita',
    'diet': () => 'Dieta da Semana',
    'medication-list': () => 'Medicamentos',
    'exercise-list': () => 'Exercícios',
    'visit-list': () => 'Visitas'
};

function openEditScreen(type, id) {
    const onEdit = document.getElementById('screen-edit').classList.contains('active');
    if (!onEdit) App.editStack = [];
    App.editStack.push({ type, id });
    renderEdit(type, id);
    if (!onEdit) showScreen('screen-edit');
    else document.querySelector('#screen-edit .screen-content').scrollTop = 0;
}

function renderEdit(type, id) {
    setText('edit-screen-title', EDIT_TITLES[type](id));
    const container = document.getElementById('edit-form-container');
    const renderers = {
        'medication': renderMedicationForm,
        'exercise': renderExerciseForm,
        'visit': renderVisitForm,
        'diet': renderDietForm,
        'medication-list': renderMedicationList,
        'exercise-list': renderExerciseList,
        'visit-list': renderVisitList
    };
    renderers[type](container, id);
}

function finishEdit(message) {
    saveToStorage();
    showToast(message, 'success');
    if (App.editStack.length > 1) {
        App.editStack.pop();
        const prev = App.editStack[App.editStack.length - 1];
        renderEdit(prev.type, prev.id);
    } else {
        goBack();
    }
}

function confirmDelete(title, message, collection, id, toast) {
    showConfirm(title, message, () => {
        App.data[collection] = App.data[collection].filter(item => item.id !== id);
        finishEdit(toast);
    }, 'Excluir');
}

function formActions(id, saveLabel, deleteLabel, deleteFn) {
    return `
        <div class="form-actions">
            <button type="submit" class="btn btn-primary btn-block">${id ? 'Salvar Alterações' : saveLabel}</button>
            ${id ? `<button type="button" class="btn btn-danger btn-block" onclick="${deleteFn}(${id})">${deleteLabel}</button>` : ''}
        </div>
    `;
}

function renderMedicationForm(container, id) {
    const med = id ? App.data.medications.find(m => m.id === id) : null;
    container.innerHTML = `
        <form id="form-medication" novalidate>
            <div class="form-group">
                <label class="form-label" for="med-name">Nome do Medicamento *</label>
                <input type="text" class="form-input" id="med-name" value="${med ? escapeHtml(med.name) : ''}" placeholder="Ex: Losartana">
            </div>
            <div class="form-group">
                <label class="form-label" for="med-dosage">Dosagem *</label>
                <input type="text" class="form-input" id="med-dosage" value="${med ? escapeHtml(med.dosage) : ''}" placeholder="Ex: 1 comprimido de 50 mg">
            </div>
            <div class="form-group">
                <label class="form-label" for="med-time">Horário</label>
                <input type="time" class="form-input" id="med-time" value="${med ? med.time || '' : ''}">
            </div>
            <div class="form-group">
                <label class="form-label" for="med-frequency">Frequência</label>
                <select class="form-input" id="med-frequency">
                    <option value="daily" ${!med || med.frequency === 'daily' ? 'selected' : ''}>Todos os dias</option>
                    <option value="weekdays" ${med && med.frequency === 'weekdays' ? 'selected' : ''}>Segunda a sexta</option>
                    <option value="weekly" ${med && med.frequency === 'weekly' ? 'selected' : ''}>Uma vez por semana (segunda)</option>
                </select>
            </div>
            <div class="form-group">
                <label class="form-label" for="med-instructions">Instruções</label>
                <textarea class="form-input form-textarea" id="med-instructions" placeholder="Ex: Tomar com água, depois do café">${med ? escapeHtml(med.instructions || '') : ''}</textarea>
                <p class="form-hint">Vitamina D e cálcio também podem ser cadastrados aqui, se prescritos pelo médico.</p>
            </div>
            ${formActions(id, 'Adicionar Medicamento', 'Excluir Medicamento', 'deleteMedication')}
        </form>
    `;
    document.getElementById('form-medication').addEventListener('submit', e => {
        e.preventDefault();
        const item = {
            id: id || Date.now(),
            name: val('med-name'),
            dosage: val('med-dosage'),
            time: val('med-time'),
            frequency: val('med-frequency'),
            instructions: val('med-instructions')
        };
        if (!item.name || !item.dosage) return showToast('Preencha nome e dosagem', 'error');
        upsert('medications', item);
        finishEdit(id ? 'Medicamento atualizado!' : 'Medicamento adicionado!');
    });
}

function deleteMedication(id) {
    confirmDelete('Excluir Medicamento', 'Tem certeza que deseja excluir este medicamento?', 'medications', id, 'Medicamento excluído');
}

function renderExerciseForm(container, id) {
    const ex = id ? App.data.exercises.find(e => e.id === id) : null;
    const days = ex ? ex.days : [1, 3, 5];
    container.innerHTML = `
        ${!ex ? `
            <div class="form-label">Sugestões (programa multicomponente)</div>
            <div class="preset-list">
                ${EXERCISE_PRESETS.map((p, i) => `
                    <button type="button" class="preset-chip" data-preset="${i}">
                        <strong>${p.name}</strong><span>${EXERCISE_TYPES[p.type]} • ${p.duration} min</span>
                    </button>`).join('')}
            </div>` : ''}
        <form id="form-exercise" novalidate>
            <div class="form-group">
                <label class="form-label" for="ex-name">Nome do Exercício *</label>
                <input type="text" class="form-input" id="ex-name" value="${ex ? escapeHtml(ex.name) : ''}" placeholder="Ex: Caminhada leve">
            </div>
            <div class="form-group">
                <label class="form-label" for="ex-type">Tipo</label>
                <select class="form-input" id="ex-type">
                    ${Object.entries(EXERCISE_TYPES).map(([k, v]) => `<option value="${k}" ${ex && ex.type === k ? 'selected' : ''}>${v}</option>`).join('')}
                </select>
                <p class="form-hint">Combine força, equilíbrio, marcha e flexibilidade para prevenir quedas.</p>
            </div>
            <div class="form-group">
                <label class="form-label">Dias da semana</label>
                <div class="day-picker" id="ex-days">
                    ${[1, 2, 3, 4, 5, 6, 0].map(d => `<button type="button" class="day-chip ${days.includes(d) ? 'selected' : ''}" data-day="${d}">${WEEKDAYS_SHORT[d]}</button>`).join('')}
                </div>
                <p class="form-hint">Recomendado: 3 a 5 vezes por semana.</p>
            </div>
            <div class="form-group">
                <label class="form-label" for="ex-duration">Duração (minutos) *</label>
                <input type="number" class="form-input" id="ex-duration" value="${ex ? ex.duration : 15}" min="1" max="180" inputmode="numeric">
            </div>
            <div class="form-group">
                <label class="form-label" for="ex-time">Horário sugerido</label>
                <input type="time" class="form-input" id="ex-time" value="${ex ? ex.time || '' : ''}">
            </div>
            <div class="form-group">
                <label class="form-label" for="ex-description">Como fazer</label>
                <textarea class="form-input form-textarea" id="ex-description" placeholder="Descreva o exercício">${ex ? escapeHtml(ex.description || '') : ''}</textarea>
            </div>
            ${formActions(id, 'Adicionar Exercício', 'Excluir Exercício', 'deleteExercise')}
        </form>
    `;

    container.querySelectorAll('.day-chip').forEach(chip => chip.addEventListener('click', () => chip.classList.toggle('selected')));
    container.querySelectorAll('[data-preset]').forEach(btn => {
        btn.addEventListener('click', () => {
            const p = EXERCISE_PRESETS[Number(btn.dataset.preset)];
            document.getElementById('ex-name').value = p.name;
            document.getElementById('ex-type').value = p.type;
            document.getElementById('ex-duration').value = p.duration;
            document.getElementById('ex-description').value = p.description;
        });
    });

    document.getElementById('form-exercise').addEventListener('submit', e => {
        e.preventDefault();
        const selectedDays = [...container.querySelectorAll('.day-chip.selected')].map(c => Number(c.dataset.day));
        const item = {
            id: id || Date.now(),
            name: val('ex-name'),
            type: val('ex-type'),
            duration: parseInt(val('ex-duration'), 10),
            days: selectedDays,
            time: val('ex-time'),
            description: val('ex-description')
        };
        if (!item.name || !item.duration) return showToast('Preencha nome e duração', 'error');
        if (!selectedDays.length) return showToast('Escolha pelo menos um dia', 'error');
        upsert('exercises', item);
        finishEdit(id ? 'Exercício atualizado!' : 'Exercício adicionado!');
    });
}

function deleteExercise(id) {
    confirmDelete('Excluir Exercício', 'Tem certeza que deseja excluir este exercício?', 'exercises', id, 'Exercício excluído');
}

function renderVisitForm(container, id) {
    const visit = id ? App.data.visits.find(v => v.id === id) : null;
    container.innerHTML = `
        <form id="form-visit" novalidate>
            <div class="form-group">
                <label class="form-label" for="visit-date">Data *</label>
                <input type="date" class="form-input" id="visit-date" value="${visit ? visit.date : dateKey()}">
            </div>
            <div class="form-group">
                <label class="form-label" for="visit-time">Horário</label>
                <input type="time" class="form-input" id="visit-time" value="${visit ? visit.time || '' : ''}">
            </div>
            <div class="form-group">
                <label class="form-label" for="visit-description">Descrição</label>
                <textarea class="form-input form-textarea" id="visit-description" placeholder="Ex: Consulta com geriatra, visita da filha">${visit ? escapeHtml(visit.description || '') : ''}</textarea>
            </div>
            ${formActions(id, 'Agendar Visita', 'Cancelar Visita', 'deleteVisit')}
        </form>
    `;
    document.getElementById('form-visit').addEventListener('submit', e => {
        e.preventDefault();
        const item = { id: id || Date.now(), date: val('visit-date'), time: val('visit-time'), description: val('visit-description') };
        if (!item.date) return showToast('Escolha uma data', 'error');
        upsert('visits', item);
        finishEdit(id ? 'Visita atualizada!' : 'Visita agendada!');
    });
}

function deleteVisit(id) {
    confirmDelete('Cancelar Visita', 'Tem certeza que deseja cancelar esta visita?', 'visits', id, 'Visita cancelada');
}

function renderDietForm(container) {
    let selectedDay = dietIndexFor(new Date());
    const protein = proteinGoal();

    container.innerHTML = `
        <div class="tip-card food">${ICONS.food}<div>
            <strong>Inclua proteína em todas as refeições</strong>
            ${protein ? `Meta: ${protein.min}–${protein.max} g/dia. ` : ''}Ex: ovos, leite, iogurte, queijo, feijão, frango, peixe.
        </div></div>
        <div class="preset-list" id="diet-days">
            ${DIET_DAYS.map((day, i) => `<button type="button" class="category-pill ${i === selectedDay ? 'active' : ''}" data-day="${i}"><span class="category-pill-text">${day.slice(0, 3)}</span></button>`).join('')}
        </div>
        <form id="form-diet" novalidate>
            <div id="diet-day-content"></div>
            <div class="form-actions">
                <button type="button" class="btn btn-outline btn-block" id="btn-diet-copy">Copiar este dia para a semana toda</button>
                <button type="submit" class="btn btn-primary btn-block">Salvar Dieta</button>
            </div>
        </form>
    `;

    const readDay = () => {
        const entry = {};
        MEALS.forEach(m => { entry[m.key] = val(`diet-${m.key}`); });
        App.data.diet[selectedDay] = entry;
    };

    const renderDay = () => {
        const diet = App.data.diet[selectedDay] || {};
        document.getElementById('diet-day-content').innerHTML = MEALS.map(m => `
            <div class="form-group">
                <label class="form-label" for="diet-${m.key}">${m.label}</label>
                <textarea class="form-input form-textarea" id="diet-${m.key}" placeholder="O que comer no ${m.label.toLowerCase()}">${escapeHtml(diet[m.key] || '')}</textarea>
            </div>`).join('');
    };

    renderDay();

    container.querySelectorAll('#diet-days .category-pill').forEach(pill => {
        pill.addEventListener('click', () => {
            readDay();
            container.querySelectorAll('#diet-days .category-pill').forEach(p => p.classList.remove('active'));
            pill.classList.add('active');
            selectedDay = Number(pill.dataset.day);
            renderDay();
        });
    });

    document.getElementById('btn-diet-copy').addEventListener('click', () => {
        readDay();
        const source = App.data.diet[selectedDay];
        showConfirm('Copiar para a semana', `O cardápio de ${DIET_DAYS[selectedDay].toLowerCase()} será usado em todos os dias.`, () => {
            DIET_DAYS.forEach((_, i) => { App.data.diet[i] = { ...source }; });
            saveToStorage();
            showToast('Cardápio copiado!', 'success');
        }, 'Copiar');
    });

    document.getElementById('form-diet').addEventListener('submit', e => {
        e.preventDefault();
        readDay();
        finishEdit('Dieta salva!');
    });
}

function listAddButton(label, type) {
    return `<button class="cta-card" onclick="openEditScreen('${type}', null)">
        <span class="quick-icon" style="background: var(--color-primary-light); color: var(--color-primary); width: 44px; height: 44px;">${ICONS.plus}</span>
        <span class="task-title">${label}</span>
    </button>`;
}

function renderMedicationList(container) {
    container.innerHTML = listAddButton('Adicionar medicamento', 'medication') + (App.data.medications.length
        ? [...App.data.medications].sort((a, b) => (a.time || '99').localeCompare(b.time || '99'))
            .map(m => infoCard('medication', ICONS.pill, m.name, `${m.dosage}${m.time ? ' • ' + m.time : ''} • ${frequencyLabel(m.frequency)}`, `openEditScreen('medication', ${m.id})`)).join('')
        : emptyState('Nenhum medicamento', 'Cadastre os remédios para o idoso acompanhar.'));
}

function renderExerciseList(container) {
    const sessions = weeklyExerciseSessions();
    const types = new Set(App.data.exercises.map(e => e.type));
    const missing = Object.keys(EXERCISE_TYPES).filter(t => !types.has(t)).map(t => EXERCISE_TYPES[t].toLowerCase());
    container.innerHTML = `
        <div class="tip-card exercise">${ICONS.exercise}<div>
            <strong>${sessions} ${sessions === 1 ? 'dia ativo' : 'dias ativos'} nos últimos 7 dias</strong>
            ${missing.length && App.data.exercises.length ? `Para um treino completo, inclua também: ${missing.join(', ')}.` : 'Treino multicomponente: força, equilíbrio, marcha e flexibilidade.'}
        </div></div>
        ${listAddButton('Adicionar exercício', 'exercise')}
        ${App.data.exercises.length
            ? App.data.exercises.map(e => infoCard('exercise', ICONS.exercise, e.name, `${EXERCISE_TYPES[e.type]} • ${e.duration} min • ${daysLabel(e.days)}`, `openEditScreen('exercise', ${e.id})`)).join('')
            : emptyState('Nenhum exercício', 'Use as sugestões prontas ao adicionar.')}
    `;
}

function renderVisitList(container) {
    const visits = [...App.data.visits].sort((a, b) => (a.date + (a.time || '')).localeCompare(b.date + (b.time || '')));
    const today = dateKey();
    container.innerHTML = listAddButton('Agendar visita', 'visit') + (visits.length
        ? visits.map(v => infoCard('visit', ICONS.calendar, formatDate(v.date) + (v.date < today ? ' (passada)' : ''), `${v.description || 'Visita'}${v.time ? ' • ' + v.time : ''}`, `openEditScreen('visit', ${v.id})`)).join('')
        : emptyState('Nenhuma visita', 'Agende consultas e visitas de acompanhamento.'));
}

// ==================== UTILITÁRIOS ====================
function upsert(collection, item) {
    const list = App.data[collection];
    const index = list.findIndex(i => i.id === item.id);
    if (index >= 0) list[index] = item;
    else list.push(item);
}

function val(id) {
    const el = document.getElementById(id);
    return el ? el.value.trim() : '';
}

function setText(id, text) {
    const el = document.getElementById(id);
    if (el) el.textContent = text;
}

function setToggle(id, on) {
    const el = document.getElementById(id);
    if (!el) return;
    el.classList.toggle('active', !!on);
    el.setAttribute('aria-checked', !!on);
}

function firstName(name) {
    return (name || '').trim().split(/\s+/)[0] || name;
}

function frequencyLabel(f) {
    return { weekdays: 'Seg a sex', weekly: 'Semanal' }[f] || 'Diário';
}

function daysLabel(days) {
    if (!Array.isArray(days) || days.length === 7) return 'Todos os dias';
    return [1, 2, 3, 4, 5, 6, 0].filter(d => days.includes(d)).map(d => WEEKDAYS_SHORT[d]).join(', ');
}

function formatLiters(ml) {
    return (ml / 1000).toLocaleString('pt-BR', { maximumFractionDigits: 2 }) + ' L';
}

function emptyState(title, text, compact = false) {
    return `
        <div class="empty-state" ${compact ? 'style="padding: var(--spacing-md);"' : ''}>
            ${!compact ? `<div class="empty-state-icon">${ICONS.check}</div>` : ''}
            ${title ? `<div class="empty-state-title">${escapeHtml(title)}</div>` : ''}
            <div class="empty-state-text">${escapeHtml(text)}</div>
        </div>
    `;
}

function escapeHtml(text) {
    if (text === null || text === undefined) return '';
    return String(text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr + 'T12:00:00');
    const today = dateKey();
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    if (dateStr === today) return 'Hoje';
    if (dateStr === dateKey(tomorrow)) return 'Amanhã';
    return date.toLocaleDateString('pt-BR', { weekday: 'short', day: 'numeric', month: 'short' });
}
