// ==================== CUIDARAPP - VERSÃO OFFLINE ====================
// Aplicativo funciona como bloco de notas personalizado e interativo
// para uso em um único dispositivo (idoso ou cuidador)

const App = {
    settings: {
        elderlyName: 'Idoso',
        caregiverName: 'Cuidador',
        password: '1234',
        deviceOwner: 'elderly',
        fontSize: 100,
        animationsEnabled: true,
        emergencyPhone: '192'
    },
    data: {
        medications: [],
        exercises: [],
        visits: [],
        diet: {},
        tasks: [],
        moodHistory: []
    },
    currentScreen: 'screen-home',
    previousScreen: null,
    currentCategory: 'all',
    editingItem: null,
    editingType: null
};

// ==================== INICIALIZAÇÃO ====================
document.addEventListener('DOMContentLoaded', () => {
    loadFromStorage();
    applySettings();
    initNavigation();
    initPasswordModal();
    initConfirmModal();
    initSettings();
    initElderlyScreen();
    initCaregiverScreen();
    initEmergencyScreen();
    initEditScreen();
    renderHomeScreen();
    
    showScreen('screen-home');
});

// ==================== STORAGE ====================
function loadFromStorage() {
    try {
        const savedSettings = localStorage.getItem('cuidarapp_settings');
        if (savedSettings) {
            App.settings = { ...App.settings, ...JSON.parse(savedSettings) };
        }
        
        const savedData = localStorage.getItem('cuidarapp_data');
        if (savedData) {
            App.data = { ...App.data, ...JSON.parse(savedData) };
        }
    } catch (e) {
        console.error('Erro ao carregar dados:', e);
    }
}

function saveToStorage() {
    try {
        localStorage.setItem('cuidarapp_settings', JSON.stringify(App.settings));
        localStorage.setItem('cuidarapp_data', JSON.stringify(App.data));
    } catch (e) {
        console.error('Erro ao salvar dados:', e);
    }
}

function applySettings() {
    document.documentElement.style.setProperty('--font-scale', App.settings.fontSize / 100);
    
    if (!App.settings.animationsEnabled) {
        document.body.classList.add('no-animations');
    } else {
        document.body.classList.remove('no-animations');
    }
    
    updateDisplayNames();
}

function updateDisplayNames() {
    document.getElementById('elderly-name').textContent = App.settings.elderlyName;
    document.getElementById('caregiver-name').textContent = App.settings.caregiverName;
    document.getElementById('elderly-greeting-name').textContent = App.settings.elderlyName;
    document.getElementById('caregiver-greeting-name').textContent = App.settings.caregiverName;
    document.getElementById('display-elderly-name').textContent = App.settings.elderlyName;
    document.getElementById('display-caregiver-name').textContent = App.settings.caregiverName;
    document.getElementById('display-emergency-phone').textContent = App.settings.emergencyPhone;
    document.getElementById('font-size-value').textContent = App.settings.fontSize + '%';
    
    const deviceButtons = document.querySelectorAll('.device-option');
    deviceButtons.forEach(btn => {
        btn.classList.toggle('active', btn.dataset.device === App.settings.deviceOwner);
    });
    
    const animToggle = document.getElementById('toggle-animations');
    if (animToggle) {
        animToggle.classList.toggle('active', App.settings.animationsEnabled);
    }
}

// ==================== NAVEGAÇÃO ====================
function showScreen(screenId, direction = 'forward') {
    const currentScreen = document.querySelector('.screen.active');
    const newScreen = document.getElementById(screenId);
    
    if (!newScreen || (currentScreen && currentScreen.id === screenId)) return;
    
    App.previousScreen = App.currentScreen;
    App.currentScreen = screenId;
    
    if (currentScreen) {
        currentScreen.classList.remove('active');
    }
    
    newScreen.classList.add('active');
    
    if (screenId === 'screen-elderly') {
        renderElderlyTasks();
    } else if (screenId === 'screen-caregiver') {
        renderCaregiverScreen();
    } else if (screenId === 'screen-settings') {
        updateDisplayNames();
    }
}

function initNavigation() {
    document.getElementById('btn-enter-elderly').addEventListener('click', () => {
        showScreen('screen-elderly');
    });
    
    document.getElementById('btn-enter-caregiver').addEventListener('click', () => {
        showPasswordModal();
    });
    
    document.getElementById('btn-elderly-home').addEventListener('click', () => {
        showScreen('screen-home');
    });
    
    document.getElementById('btn-caregiver-home').addEventListener('click', () => {
        showScreen('screen-home');
    });
    
    document.getElementById('btn-caregiver-settings').addEventListener('click', () => {
        showScreen('screen-settings');
    });
    
    document.getElementById('btn-back-settings').addEventListener('click', () => {
        showScreen('screen-caregiver');
    });
    
    document.getElementById('btn-back-emergency').addEventListener('click', () => {
        showScreen(App.previousScreen || 'screen-elderly');
    });
    
    document.getElementById('btn-back-edit').addEventListener('click', () => {
        showScreen(App.previousScreen || 'screen-caregiver');
    });
    
    document.getElementById('btn-emergency').addEventListener('click', () => {
        showScreen('screen-emergency');
    });
    
    initCategoryPills();
    initBottomNav();
}

function initCategoryPills() {
    document.querySelectorAll('.category-pill').forEach(pill => {
        pill.addEventListener('click', () => {
            document.querySelectorAll('.category-pill').forEach(p => p.classList.remove('active'));
            pill.classList.add('active');
            App.currentCategory = pill.dataset.category;
            renderElderlyTasks();
        });
    });
}

function initBottomNav() {
    document.querySelectorAll('.bottom-nav .nav-item').forEach(item => {
        item.addEventListener('click', () => {
            const screen = item.dataset.screen;
            if (!screen) return;
            
            document.querySelectorAll('.bottom-nav .nav-item').forEach(i => i.classList.remove('active'));
            item.classList.add('active');
        });
    });
}

// ==================== MODAL DE SENHA ====================
function initPasswordModal() {
    const modal = document.getElementById('password-modal');
    const input = document.getElementById('password-input');
    const errorEl = document.getElementById('password-error');
    const cancelBtn = document.getElementById('password-cancel');
    const confirmBtn = document.getElementById('password-confirm');
    
    cancelBtn.addEventListener('click', () => {
        closePasswordModal();
    });
    
    confirmBtn.addEventListener('click', () => {
        validatePassword();
    });
    
    input.addEventListener('keyup', (e) => {
        if (e.key === 'Enter') {
            validatePassword();
        }
    });
    
    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            closePasswordModal();
        }
    });
}

function showPasswordModal() {
    const modal = document.getElementById('password-modal');
    const input = document.getElementById('password-input');
    const errorEl = document.getElementById('password-error');
    
    input.value = '';
    errorEl.classList.remove('show');
    input.classList.remove('error');
    modal.classList.add('active');
    
    setTimeout(() => input.focus(), 100);
}

function closePasswordModal() {
    const modal = document.getElementById('password-modal');
    modal.classList.remove('active');
}

function validatePassword() {
    const input = document.getElementById('password-input');
    const errorEl = document.getElementById('password-error');
    
    if (input.value === App.settings.password) {
        closePasswordModal();
        showScreen('screen-caregiver');
    } else {
        input.classList.add('error');
        errorEl.classList.add('show');
        input.value = '';
        input.focus();
        
        setTimeout(() => {
            input.classList.remove('error');
        }, 500);
    }
}

// ==================== MODAL DE CONFIRMAÇÃO ====================
let confirmCallback = null;

function initConfirmModal() {
    const modal = document.getElementById('confirm-modal');
    const cancelBtn = document.getElementById('confirm-cancel');
    const okBtn = document.getElementById('confirm-ok');
    
    cancelBtn.addEventListener('click', () => {
        modal.classList.remove('active');
        confirmCallback = null;
    });
    
    okBtn.addEventListener('click', () => {
        modal.classList.remove('active');
        if (confirmCallback) {
            confirmCallback();
            confirmCallback = null;
        }
    });
    
    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            modal.classList.remove('active');
            confirmCallback = null;
        }
    });
}

function showConfirm(title, message, callback) {
    const modal = document.getElementById('confirm-modal');
    document.getElementById('confirm-title').textContent = title;
    document.getElementById('confirm-message').textContent = message;
    confirmCallback = callback;
    modal.classList.add('active');
}

// ==================== TOAST ====================
function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    container.appendChild(toast);
    
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(20px)';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// ==================== TELA HOME ====================
function renderHomeScreen() {
    updateDisplayNames();
}

// ==================== TELA DO IDOSO ====================
function initElderlyScreen() {
}

function renderElderlyTasks() {
    const container = document.getElementById('elderly-tasks');
    const visitsContainer = document.getElementById('elderly-visits');
    
    const today = new Date();
    const dayOfWeek = today.getDay();
    
    let tasks = [];
    
    App.data.medications.forEach(med => {
        if (shouldShowToday(med.frequency, dayOfWeek)) {
            tasks.push({
                id: med.id,
                type: 'medication',
                title: med.name,
                subtitle: med.dosage,
                time: med.time,
                done: isMedicationDoneToday(med.id),
                icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"/></svg>`,
                color: 'var(--color-medication)',
                iconColor: 'var(--color-medication-icon)'
            });
        }
    });
    
    App.data.exercises.forEach(ex => {
        tasks.push({
            id: ex.id,
            type: 'exercise',
            title: ex.name,
            subtitle: `${ex.duration} minutos`,
            time: ex.time || '',
            done: isExerciseDoneToday(ex.id),
            icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="5" r="3"/><path d="M12 22V8"/><path d="m5 12 7 3 7-3"/></svg>`,
            color: 'var(--color-exercise)',
            iconColor: 'var(--color-exercise-icon)'
        });
    });
    
    if (App.currentCategory !== 'all') {
        tasks = tasks.filter(t => t.type === App.currentCategory);
    }
    
    tasks.sort((a, b) => {
        if (a.done !== b.done) return a.done ? 1 : -1;
        return (a.time || '').localeCompare(b.time || '');
    });
    
    if (tasks.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M8 14s1.5 2 4 2 4-2 4-2"/><line x1="9" y1="9" x2="9.01" y2="9"/><line x1="15" y1="9" x2="15.01" y2="9"/></svg>
                </div>
                <div class="empty-state-title">Tudo em dia!</div>
                <div class="empty-state-text">Não há tarefas pendentes para hoje.</div>
            </div>
        `;
    } else {
        container.innerHTML = tasks.map(task => `
            <div class="task-card ${task.done ? 'featured' : ''}" data-id="${task.id}" data-type="${task.type}">
                <div class="task-avatar" style="background: ${task.color};">
                    <span style="color: ${task.iconColor};">${task.icon}</span>
                </div>
                <div class="task-info">
                    <div class="task-title">${escapeHtml(task.title)}</div>
                    <div class="task-subtitle">${escapeHtml(task.subtitle)}</div>
                    ${task.time ? `<div class="task-badges"><span class="task-badge"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12,6 12,12 16,14"/></svg>${task.time}</span></div>` : ''}
                </div>
                <div class="task-check ${task.done ? 'checked' : ''}" data-id="${task.id}" data-type="${task.type}">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20,6 9,17 4,12"/></svg>
                </div>
            </div>
        `).join('');
        
        container.querySelectorAll('.task-check').forEach(check => {
            check.addEventListener('click', (e) => {
                e.stopPropagation();
                const id = parseInt(check.dataset.id);
                const type = check.dataset.type;
                
                if (type === 'medication') {
                    toggleMedicationDone(id);
                } else if (type === 'exercise') {
                    toggleExerciseDone(id);
                }
                
                renderElderlyTasks();
            });
        });
    }
    
    const upcomingVisits = App.data.visits
        .filter(v => new Date(v.date) >= today)
        .sort((a, b) => new Date(a.date) - new Date(b.date))
        .slice(0, 3);
    
    if (upcomingVisits.length === 0) {
        visitsContainer.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-text">Nenhuma visita agendada.</div>
            </div>
        `;
    } else {
        visitsContainer.innerHTML = upcomingVisits.map(visit => `
            <div class="task-card">
                <div class="task-avatar" style="background: var(--color-visit);">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="color: var(--color-visit-icon);"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                </div>
                <div class="task-info">
                    <div class="task-title">${formatDate(visit.date)}</div>
                    <div class="task-subtitle">${escapeHtml(visit.description || 'Visita agendada')}</div>
                    ${visit.time ? `<div class="task-badges"><span class="task-badge"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12,6 12,12 16,14"/></svg>${visit.time}</span></div>` : ''}
                </div>
            </div>
        `).join('');
    }
}

function shouldShowToday(frequency, dayOfWeek) {
    if (!frequency || frequency === 'daily') return true;
    if (frequency === 'weekly') return dayOfWeek === 1;
    if (frequency === 'weekdays') return dayOfWeek >= 1 && dayOfWeek <= 5;
    return true;
}

function isMedicationDoneToday(id) {
    const today = new Date().toDateString();
    const key = `med_${id}_${today}`;
    return localStorage.getItem(key) === 'done';
}

function toggleMedicationDone(id) {
    const today = new Date().toDateString();
    const key = `med_${id}_${today}`;
    if (localStorage.getItem(key) === 'done') {
        localStorage.removeItem(key);
        showToast('Medicamento desmarcado');
    } else {
        localStorage.setItem(key, 'done');
        showToast('Medicamento tomado!', 'success');
    }
}

function isExerciseDoneToday(id) {
    const today = new Date().toDateString();
    const key = `ex_${id}_${today}`;
    return localStorage.getItem(key) === 'done';
}

function toggleExerciseDone(id) {
    const today = new Date().toDateString();
    const key = `ex_${id}_${today}`;
    if (localStorage.getItem(key) === 'done') {
        localStorage.removeItem(key);
        showToast('Exercício desmarcado');
    } else {
        localStorage.setItem(key, 'done');
        showToast('Exercício concluído!', 'success');
    }
}

// ==================== TELA DO CUIDADOR ====================
function initCaregiverScreen() {
    document.getElementById('btn-add-medication').addEventListener('click', () => {
        openEditScreen('medication', null);
    });
    
    document.getElementById('btn-add-exercise').addEventListener('click', () => {
        openEditScreen('exercise', null);
    });
    
    document.getElementById('btn-add-visit').addEventListener('click', () => {
        openEditScreen('visit', null);
    });
    
    document.getElementById('btn-edit-diet').addEventListener('click', () => {
        openEditScreen('diet', null);
    });
    
    document.getElementById('btn-view-all-meds').addEventListener('click', () => {
        openEditScreen('medication-list', null);
    });
    
    document.getElementById('btn-view-all-visits').addEventListener('click', () => {
        openEditScreen('visit-list', null);
    });
}

function renderCaregiverScreen() {
    renderCaregiverMeds();
    renderCaregiverVisits();
    renderCaregiverProgress();
}

function renderCaregiverMeds() {
    const container = document.getElementById('caregiver-meds-list');
    const meds = App.data.medications.slice(0, 3);
    
    if (meds.length === 0) {
        container.innerHTML = `
            <div class="empty-state" style="padding: var(--spacing-md);">
                <div class="empty-state-text">Nenhum medicamento cadastrado</div>
            </div>
        `;
    } else {
        container.innerHTML = meds.map(med => `
            <div class="task-card" onclick="openEditScreen('medication', ${med.id})">
                <div class="task-avatar" style="background: var(--color-medication);">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="color: var(--color-medication-icon);"><path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"/></svg>
                </div>
                <div class="task-info">
                    <div class="task-title">${escapeHtml(med.name)}</div>
                    <div class="task-subtitle">${escapeHtml(med.dosage)}</div>
                </div>
                <div class="profile-arrow">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
                </div>
            </div>
        `).join('');
    }
}

function renderCaregiverVisits() {
    const container = document.getElementById('caregiver-visits-list');
    const today = new Date();
    const visits = App.data.visits
        .filter(v => new Date(v.date) >= today)
        .sort((a, b) => new Date(a.date) - new Date(b.date))
        .slice(0, 3);
    
    if (visits.length === 0) {
        container.innerHTML = `
            <div class="empty-state" style="padding: var(--spacing-md);">
                <div class="empty-state-text">Nenhuma visita agendada</div>
            </div>
        `;
    } else {
        container.innerHTML = visits.map(visit => `
            <div class="task-card" onclick="openEditScreen('visit', ${visit.id})">
                <div class="task-avatar" style="background: var(--color-visit);">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="color: var(--color-visit-icon);"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                </div>
                <div class="task-info">
                    <div class="task-title">${formatDate(visit.date)}</div>
                    <div class="task-subtitle">${escapeHtml(visit.description || 'Visita')}</div>
                </div>
                <div class="profile-arrow">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
                </div>
            </div>
        `).join('');
    }
}

function renderCaregiverProgress() {
    const container = document.getElementById('caregiver-progress');
    
    const totalMeds = App.data.medications.length;
    const totalExercises = App.data.exercises.length;
    
    let medsDone = 0;
    let exDone = 0;
    const today = new Date().toDateString();
    
    App.data.medications.forEach(med => {
        if (localStorage.getItem(`med_${med.id}_${today}`) === 'done') medsDone++;
    });
    
    App.data.exercises.forEach(ex => {
        if (localStorage.getItem(`ex_${ex.id}_${today}`) === 'done') exDone++;
    });
    
    const medsPct = totalMeds > 0 ? Math.round((medsDone / totalMeds) * 100) : 0;
    const exPct = totalExercises > 0 ? Math.round((exDone / totalExercises) * 100) : 0;
    
    container.innerHTML = `
        <div class="task-card">
            <div class="task-info" style="flex: 1;">
                <div class="task-title">Medicamentos</div>
                <div class="task-subtitle">${medsDone} de ${totalMeds} tomados hoje</div>
                <div style="background: var(--color-border); height: 8px; border-radius: 4px; margin-top: 8px;">
                    <div style="background: var(--color-medication-icon); height: 100%; border-radius: 4px; width: ${medsPct}%; transition: width 0.3s;"></div>
                </div>
            </div>
        </div>
        <div class="task-card">
            <div class="task-info" style="flex: 1;">
                <div class="task-title">Exercícios</div>
                <div class="task-subtitle">${exDone} de ${totalExercises} concluídos hoje</div>
                <div style="background: var(--color-border); height: 8px; border-radius: 4px; margin-top: 8px;">
                    <div style="background: var(--color-exercise-icon); height: 100%; border-radius: 4px; width: ${exPct}%; transition: width 0.3s;"></div>
                </div>
            </div>
        </div>
    `;
}

// ==================== TELA DE CONFIGURAÇÕES ====================
function initSettings() {
    document.getElementById('settings-elderly-name').addEventListener('click', () => {
        const newName = prompt('Nome do Idoso:', App.settings.elderlyName);
        if (newName && newName.trim()) {
            App.settings.elderlyName = newName.trim();
            saveToStorage();
            updateDisplayNames();
            showToast('Nome atualizado!', 'success');
        }
    });
    
    document.getElementById('settings-caregiver-name').addEventListener('click', () => {
        const newName = prompt('Nome do Cuidador:', App.settings.caregiverName);
        if (newName && newName.trim()) {
            App.settings.caregiverName = newName.trim();
            saveToStorage();
            updateDisplayNames();
            showToast('Nome atualizado!', 'success');
        }
    });
    
    document.querySelectorAll('.device-option').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.device-option').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            App.settings.deviceOwner = btn.dataset.device;
            saveToStorage();
            showToast('Configuração salva!', 'success');
        });
    });
    
    document.getElementById('btn-font-decrease').addEventListener('click', () => {
        if (App.settings.fontSize > 80) {
            App.settings.fontSize -= 10;
            saveToStorage();
            applySettings();
            showToast(`Fonte: ${App.settings.fontSize}%`);
        }
    });
    
    document.getElementById('btn-font-increase').addEventListener('click', () => {
        if (App.settings.fontSize < 150) {
            App.settings.fontSize += 10;
            saveToStorage();
            applySettings();
            showToast(`Fonte: ${App.settings.fontSize}%`);
        }
    });
    
    document.getElementById('toggle-animations').addEventListener('click', () => {
        App.settings.animationsEnabled = !App.settings.animationsEnabled;
        saveToStorage();
        applySettings();
        showToast(App.settings.animationsEnabled ? 'Animações ativadas' : 'Animações desativadas');
    });
    
    document.getElementById('settings-change-password').addEventListener('click', () => {
        const currentPassword = prompt('Digite a senha atual:');
        if (currentPassword !== App.settings.password) {
            showToast('Senha incorreta!', 'error');
            return;
        }
        
        const newPassword = prompt('Digite a nova senha (4 dígitos):');
        if (!newPassword || !/^\d{4}$/.test(newPassword)) {
            showToast('A senha deve ter 4 dígitos!', 'error');
            return;
        }
        
        App.settings.password = newPassword;
        saveToStorage();
        showToast('Senha alterada!', 'success');
    });
    
    document.getElementById('settings-emergency-phone').addEventListener('click', () => {
        const newPhone = prompt('Telefone de Emergência:', App.settings.emergencyPhone);
        if (newPhone && newPhone.trim()) {
            App.settings.emergencyPhone = newPhone.trim().replace(/\D/g, '');
            saveToStorage();
            updateDisplayNames();
            showToast('Telefone atualizado!', 'success');
        }
    });
    
    document.getElementById('settings-clear-data').addEventListener('click', () => {
        showConfirm('Limpar Dados', 'Tem certeza que deseja apagar TODOS os dados? Esta ação não pode ser desfeita.', () => {
            localStorage.clear();
            App.settings = {
                elderlyName: 'Idoso',
                caregiverName: 'Cuidador',
                password: '1234',
                deviceOwner: 'elderly',
                fontSize: 100,
                animationsEnabled: true,
                emergencyPhone: '192'
            };
            App.data = {
                medications: [],
                exercises: [],
                visits: [],
                diet: {},
                tasks: [],
                moodHistory: []
            };
            saveToStorage();
            applySettings();
            showToast('Dados apagados!', 'success');
            showScreen('screen-home');
        });
    });
}

// ==================== TELA DE EMERGÊNCIA ====================
function initEmergencyScreen() {
    document.getElementById('btn-call-emergency').addEventListener('click', () => {
        window.location.href = `tel:${App.settings.emergencyPhone}`;
    });
}

// ==================== TELA DE EDIÇÃO ====================
function initEditScreen() {
}

function openEditScreen(type, id) {
    App.editingType = type;
    App.editingItem = id;
    
    const title = document.getElementById('edit-screen-title');
    const container = document.getElementById('edit-form-container');
    
    if (type === 'medication') {
        title.textContent = id ? 'Editar Medicamento' : 'Novo Medicamento';
        renderMedicationForm(container, id);
    } else if (type === 'exercise') {
        title.textContent = id ? 'Editar Exercício' : 'Novo Exercício';
        renderExerciseForm(container, id);
    } else if (type === 'visit') {
        title.textContent = id ? 'Editar Visita' : 'Nova Visita';
        renderVisitForm(container, id);
    } else if (type === 'diet') {
        title.textContent = 'Dieta da Semana';
        renderDietForm(container);
    } else if (type === 'medication-list') {
        title.textContent = 'Todos os Medicamentos';
        renderMedicationList(container);
    } else if (type === 'visit-list') {
        title.textContent = 'Todas as Visitas';
        renderVisitList(container);
    }
    
    showScreen('screen-edit');
}

function renderMedicationForm(container, id) {
    const med = id ? App.data.medications.find(m => m.id === id) : null;
    
    container.innerHTML = `
        <form id="form-medication" class="edit-form">
            <div class="form-group">
                <label class="form-label">Nome do Medicamento *</label>
                <input type="text" class="form-input" id="med-name" value="${med ? escapeHtml(med.name) : ''}" required>
            </div>
            <div class="form-group">
                <label class="form-label">Dosagem *</label>
                <input type="text" class="form-input" id="med-dosage" value="${med ? escapeHtml(med.dosage) : ''}" placeholder="Ex: 1 comprimido, 10ml" required>
            </div>
            <div class="form-group">
                <label class="form-label">Horário</label>
                <input type="time" class="form-input" id="med-time" value="${med ? med.time || '' : ''}">
            </div>
            <div class="form-group">
                <label class="form-label">Frequência</label>
                <select class="form-input" id="med-frequency">
                    <option value="daily" ${!med || med.frequency === 'daily' ? 'selected' : ''}>Diário</option>
                    <option value="weekdays" ${med && med.frequency === 'weekdays' ? 'selected' : ''}>Dias úteis</option>
                    <option value="weekly" ${med && med.frequency === 'weekly' ? 'selected' : ''}>Semanal</option>
                </select>
            </div>
            <div class="form-group">
                <label class="form-label">Instruções</label>
                <textarea class="form-input form-textarea" id="med-instructions" placeholder="Ex: Tomar com água, antes das refeições">${med ? escapeHtml(med.instructions || '') : ''}</textarea>
            </div>
            <button type="submit" class="btn btn-primary btn-block">
                ${id ? 'Salvar Alterações' : 'Adicionar Medicamento'}
            </button>
            ${id ? `<button type="button" class="btn btn-danger btn-block" style="margin-top: 8px;" onclick="deleteMedication(${id})">Excluir Medicamento</button>` : ''}
        </form>
    `;
    
    document.getElementById('form-medication').addEventListener('submit', (e) => {
        e.preventDefault();
        saveMedication(id);
    });
}

function saveMedication(id) {
    const name = document.getElementById('med-name').value.trim();
    const dosage = document.getElementById('med-dosage').value.trim();
    const time = document.getElementById('med-time').value;
    const frequency = document.getElementById('med-frequency').value;
    const instructions = document.getElementById('med-instructions').value.trim();
    
    if (!name || !dosage) {
        showToast('Preencha os campos obrigatórios!', 'error');
        return;
    }
    
    if (id) {
        const index = App.data.medications.findIndex(m => m.id === id);
        if (index !== -1) {
            App.data.medications[index] = { id, name, dosage, time, frequency, instructions };
        }
    } else {
        const newId = Date.now();
        App.data.medications.push({ id: newId, name, dosage, time, frequency, instructions });
    }
    
    saveToStorage();
    showToast(id ? 'Medicamento atualizado!' : 'Medicamento adicionado!', 'success');
    showScreen('screen-caregiver');
    renderCaregiverScreen();
}

function deleteMedication(id) {
    showConfirm('Excluir Medicamento', 'Tem certeza que deseja excluir este medicamento?', () => {
        App.data.medications = App.data.medications.filter(m => m.id !== id);
        saveToStorage();
        showToast('Medicamento excluído!', 'success');
        showScreen('screen-caregiver');
        renderCaregiverScreen();
    });
}

function renderExerciseForm(container, id) {
    const ex = id ? App.data.exercises.find(e => e.id === id) : null;
    
    container.innerHTML = `
        <form id="form-exercise" class="edit-form">
            <div class="form-group">
                <label class="form-label">Nome do Exercício *</label>
                <input type="text" class="form-input" id="ex-name" value="${ex ? escapeHtml(ex.name) : ''}" required>
            </div>
            <div class="form-group">
                <label class="form-label">Duração (minutos) *</label>
                <input type="number" class="form-input" id="ex-duration" value="${ex ? ex.duration : '15'}" min="1" required>
            </div>
            <div class="form-group">
                <label class="form-label">Horário Sugerido</label>
                <input type="time" class="form-input" id="ex-time" value="${ex ? ex.time || '' : ''}">
            </div>
            <div class="form-group">
                <label class="form-label">Descrição</label>
                <textarea class="form-input form-textarea" id="ex-description" placeholder="Descreva o exercício">${ex ? escapeHtml(ex.description || '') : ''}</textarea>
            </div>
            <button type="submit" class="btn btn-primary btn-block">
                ${id ? 'Salvar Alterações' : 'Adicionar Exercício'}
            </button>
            ${id ? `<button type="button" class="btn btn-danger btn-block" style="margin-top: 8px;" onclick="deleteExercise(${id})">Excluir Exercício</button>` : ''}
        </form>
    `;
    
    document.getElementById('form-exercise').addEventListener('submit', (e) => {
        e.preventDefault();
        saveExercise(id);
    });
}

function saveExercise(id) {
    const name = document.getElementById('ex-name').value.trim();
    const duration = parseInt(document.getElementById('ex-duration').value);
    const time = document.getElementById('ex-time').value;
    const description = document.getElementById('ex-description').value.trim();
    
    if (!name || !duration) {
        showToast('Preencha os campos obrigatórios!', 'error');
        return;
    }
    
    if (id) {
        const index = App.data.exercises.findIndex(e => e.id === id);
        if (index !== -1) {
            App.data.exercises[index] = { id, name, duration, time, description };
        }
    } else {
        const newId = Date.now();
        App.data.exercises.push({ id: newId, name, duration, time, description });
    }
    
    saveToStorage();
    showToast(id ? 'Exercício atualizado!' : 'Exercício adicionado!', 'success');
    showScreen('screen-caregiver');
    renderCaregiverScreen();
}

function deleteExercise(id) {
    showConfirm('Excluir Exercício', 'Tem certeza que deseja excluir este exercício?', () => {
        App.data.exercises = App.data.exercises.filter(e => e.id !== id);
        saveToStorage();
        showToast('Exercício excluído!', 'success');
        showScreen('screen-caregiver');
        renderCaregiverScreen();
    });
}

function renderVisitForm(container, id) {
    const visit = id ? App.data.visits.find(v => v.id === id) : null;
    const today = new Date().toISOString().split('T')[0];
    
    container.innerHTML = `
        <form id="form-visit" class="edit-form">
            <div class="form-group">
                <label class="form-label">Data *</label>
                <input type="date" class="form-input" id="visit-date" value="${visit ? visit.date : today}" required>
            </div>
            <div class="form-group">
                <label class="form-label">Horário</label>
                <input type="time" class="form-input" id="visit-time" value="${visit ? visit.time || '' : ''}">
            </div>
            <div class="form-group">
                <label class="form-label">Descrição</label>
                <textarea class="form-input form-textarea" id="visit-description" placeholder="Ex: Consulta médica, visita do cuidador">${visit ? escapeHtml(visit.description || '') : ''}</textarea>
            </div>
            <button type="submit" class="btn btn-primary btn-block">
                ${id ? 'Salvar Alterações' : 'Agendar Visita'}
            </button>
            ${id ? `<button type="button" class="btn btn-danger btn-block" style="margin-top: 8px;" onclick="deleteVisit(${id})">Cancelar Visita</button>` : ''}
        </form>
    `;
    
    document.getElementById('form-visit').addEventListener('submit', (e) => {
        e.preventDefault();
        saveVisit(id);
    });
}

function saveVisit(id) {
    const date = document.getElementById('visit-date').value;
    const time = document.getElementById('visit-time').value;
    const description = document.getElementById('visit-description').value.trim();
    
    if (!date) {
        showToast('Selecione uma data!', 'error');
        return;
    }
    
    if (id) {
        const index = App.data.visits.findIndex(v => v.id === id);
        if (index !== -1) {
            App.data.visits[index] = { id, date, time, description };
        }
    } else {
        const newId = Date.now();
        App.data.visits.push({ id: newId, date, time, description });
    }
    
    saveToStorage();
    showToast(id ? 'Visita atualizada!' : 'Visita agendada!', 'success');
    showScreen('screen-caregiver');
    renderCaregiverScreen();
}

function deleteVisit(id) {
    showConfirm('Cancelar Visita', 'Tem certeza que deseja cancelar esta visita?', () => {
        App.data.visits = App.data.visits.filter(v => v.id !== id);
        saveToStorage();
        showToast('Visita cancelada!', 'success');
        showScreen('screen-caregiver');
        renderCaregiverScreen();
    });
}

function renderDietForm(container) {
    const days = ['Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado', 'Domingo'];
    
    container.innerHTML = `
        <form id="form-diet" class="edit-form">
            <div style="display: flex; gap: 8px; overflow-x: auto; margin-bottom: 16px; padding-bottom: 8px;">
                ${days.map((day, i) => `
                    <button type="button" class="category-pill ${i === 0 ? 'active' : ''}" data-day="${i}" style="flex-shrink: 0;">
                        <span class="category-pill-text">${day.substring(0, 3)}</span>
                    </button>
                `).join('')}
            </div>
            <div id="diet-day-content"></div>
            <button type="submit" class="btn btn-primary btn-block" style="margin-top: 16px;">
                Salvar Dieta
            </button>
        </form>
    `;
    
    let selectedDay = 0;
    
    function renderDayContent() {
        const diet = App.data.diet[selectedDay] || {};
        document.getElementById('diet-day-content').innerHTML = `
            <div class="form-group">
                <label class="form-label">Café da Manhã</label>
                <textarea class="form-input form-textarea" id="diet-breakfast" placeholder="O que comer no café da manhã">${escapeHtml(diet.breakfast || '')}</textarea>
            </div>
            <div class="form-group">
                <label class="form-label">Almoço</label>
                <textarea class="form-input form-textarea" id="diet-lunch" placeholder="O que comer no almoço">${escapeHtml(diet.lunch || '')}</textarea>
            </div>
            <div class="form-group">
                <label class="form-label">Jantar</label>
                <textarea class="form-input form-textarea" id="diet-dinner" placeholder="O que comer no jantar">${escapeHtml(diet.dinner || '')}</textarea>
            </div>
            <div class="form-group">
                <label class="form-label">Lanches</label>
                <textarea class="form-input form-textarea" id="diet-snacks" placeholder="Lanches entre refeições">${escapeHtml(diet.snacks || '')}</textarea>
            </div>
        `;
    }
    
    renderDayContent();
    
    container.querySelectorAll('.category-pill').forEach(pill => {
        pill.addEventListener('click', () => {
            saveDayContent();
            container.querySelectorAll('.category-pill').forEach(p => p.classList.remove('active'));
            pill.classList.add('active');
            selectedDay = parseInt(pill.dataset.day);
            renderDayContent();
        });
    });
    
    function saveDayContent() {
        App.data.diet[selectedDay] = {
            breakfast: document.getElementById('diet-breakfast')?.value.trim() || '',
            lunch: document.getElementById('diet-lunch')?.value.trim() || '',
            dinner: document.getElementById('diet-dinner')?.value.trim() || '',
            snacks: document.getElementById('diet-snacks')?.value.trim() || ''
        };
    }
    
    document.getElementById('form-diet').addEventListener('submit', (e) => {
        e.preventDefault();
        saveDayContent();
        saveToStorage();
        showToast('Dieta salva!', 'success');
        showScreen('screen-caregiver');
    });
}

function renderMedicationList(container) {
    if (App.data.medications.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"/></svg>
                </div>
                <div class="empty-state-title">Nenhum medicamento</div>
                <div class="empty-state-text">Adicione medicamentos para acompanhar.</div>
            </div>
        `;
    } else {
        container.innerHTML = App.data.medications.map(med => `
            <div class="task-card" onclick="openEditScreen('medication', ${med.id})">
                <div class="task-avatar" style="background: var(--color-medication);">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="color: var(--color-medication-icon);"><path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"/></svg>
                </div>
                <div class="task-info">
                    <div class="task-title">${escapeHtml(med.name)}</div>
                    <div class="task-subtitle">${escapeHtml(med.dosage)} ${med.time ? '• ' + med.time : ''}</div>
                </div>
                <div class="profile-arrow">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
                </div>
            </div>
        `).join('');
    }
}

function renderVisitList(container) {
    const visits = App.data.visits.sort((a, b) => new Date(a.date) - new Date(b.date));
    
    if (visits.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                </div>
                <div class="empty-state-title">Nenhuma visita</div>
                <div class="empty-state-text">Agende visitas para acompanhamento.</div>
            </div>
        `;
    } else {
        container.innerHTML = visits.map(visit => `
            <div class="task-card" onclick="openEditScreen('visit', ${visit.id})">
                <div class="task-avatar" style="background: var(--color-visit);">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="color: var(--color-visit-icon);"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                </div>
                <div class="task-info">
                    <div class="task-title">${formatDate(visit.date)}</div>
                    <div class="task-subtitle">${escapeHtml(visit.description || 'Visita')} ${visit.time ? '• ' + visit.time : ''}</div>
                </div>
                <div class="profile-arrow">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
                </div>
            </div>
        `).join('');
    }
}

// ==================== UTILITÁRIOS ====================
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr + 'T12:00:00');
    const options = { weekday: 'short', day: 'numeric', month: 'short' };
    return date.toLocaleDateString('pt-BR', options);
}

function generateId() {
    return Date.now();
}
