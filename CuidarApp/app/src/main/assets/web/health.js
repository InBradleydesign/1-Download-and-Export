// ==================== DIÁRIO DE SAÚDE ====================
// Medições, sintomas, doses, lembretes e relatório PDF. Os dados ficam no banco local do
// aparelho (Room), acessado pela ponte nativa window.CuidarHealth. Nada é enviado à internet.
// O app não diagnostica, não classifica leituras e não sugere mudanças de remédio.

// ---------- Catálogos (mantenha iguais aos de com.cuidarapp.health) ----------
const H_ICONS = {
    heart: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78L12 21.23l8.84-8.84a5.5 5.5 0 0 0 0-7.78z"/><path d="M3.5 12h4l2-3 3 6 2-3h6"/></svg>',
    drop: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2.69l5.66 5.66a8 8 0 1 1-11.31 0z"/><path d="M9 14h6M12 11v6"/></svg>',
    scale: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="4"/><path d="M8 9a5 5 0 0 1 8 0"/><path d="M12 9l1.5-2"/></svg>',
    thermo: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 14.76V3.5a2.5 2.5 0 0 0-5 0v11.26a4.5 4.5 0 1 0 5 0z"/></svg>',
    lungs: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 4v8"/><path d="M12 12c-1.5-1-3-1-3-1V6.5C9 5 7.5 4.5 6.5 5.5 4 8 3 13 3 17c0 2 1.5 3 3 3 2 0 3-1 3-3v-3"/><path d="M12 12c1.5-1 3-1 3-1V6.5c0-1.5 1.5-2 2.5-1C20 8 21 13 21 17c0 2-1.5 3-3 3-2 0-3-1-3-3v-3"/></svg>',
    pulse: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>',
    beaker: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 3h6"/><path d="M10 3v6l-5 9a2 2 0 0 0 1.7 3h10.6a2 2 0 0 0 1.7-3l-5-9V3"/><path d="M7.5 15h9"/></svg>',
    symptom: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M8 15s1.5-2 4-2 4 2 4 2"/><line x1="9" y1="9" x2="9.01" y2="9"/><line x1="15" y1="9" x2="15.01" y2="9"/></svg>',
    bell: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>',
    file: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="8" y1="13" x2="16" y2="13"/><line x1="8" y1="17" x2="13" y2="17"/></svg>',
    share: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/><line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/><line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/></svg>',
    chart: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 3v18h18"/><polyline points="7 14 11 10 14 13 20 7"/></svg>',
    shield: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>',
    x: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>'
};

const H_INDICATORS = {
    bp: { label: 'Pressão', long: 'Pressão arterial', unit: 'mmHg', icon: H_ICONS.heart },
    glucose: { label: 'Glicemia', long: 'Glicemia', unit: 'mg/dL', icon: H_ICONS.drop, min: 10, max: 800 },
    weight: { label: 'Peso', long: 'Peso', unit: 'kg', icon: H_ICONS.scale, min: 20, max: 350, decimals: 1 },
    temperature: { label: 'Temperatura', long: 'Temperatura', unit: '°C', icon: H_ICONS.thermo, min: 30, max: 45, decimals: 1 },
    spo2: { label: 'Saturação', long: 'Saturação de oxigênio', unit: '%', icon: H_ICONS.lungs, min: 50, max: 100 },
    heart_rate: { label: 'Batimentos', long: 'Frequência cardíaca', unit: 'bpm', icon: H_ICONS.pulse, min: 20, max: 250 },
    urine: { label: 'Urina', long: 'Volume de urina', unit: 'mL', icon: H_ICONS.beaker, min: 1, max: 5000 }
};
const H_BP = { sys: [50, 300], dia: [30, 200], pulse: [20, 250] };

const H_GLUCOSE_CONTEXTS = {
    fasting: 'Em jejum',
    before_meal: 'Antes de refeição',
    after_meal: 'Depois de refeição',
    bedtime: 'Ao deitar',
    other: 'Outro momento'
};

const H_SYMPTOMS = [
    { code: 'pain', label: 'Dor' },
    { code: 'headache', label: 'Dor de cabeça' },
    { code: 'dizziness', label: 'Tontura' },
    { code: 'fatigue', label: 'Cansaço' },
    { code: 'nausea', label: 'Enjoo ou vômito' },
    { code: 'fever', label: 'Febre ou calafrio' },
    { code: 'swelling', label: 'Inchaço' },
    { code: 'appetite', label: 'Falta de apetite' },
    { code: 'sleep', label: 'Dormiu mal' },
    { code: 'chest_pain', label: 'Dor ou aperto no peito', urgent: true },
    { code: 'breath', label: 'Falta de ar', urgent: true },
    { code: 'confusion', label: 'Confusão ou fala enrolada', urgent: true },
    { code: 'weakness_side', label: 'Fraqueza ou dormência de um lado do corpo', urgent: true },
    { code: 'fainting', label: 'Desmaio', urgent: true },
    { code: 'other', label: 'Outro' }
];
const H_INTENSITY = { 1: 'Leve', 2: 'Moderada', 3: 'Forte' };

const H_CONDITIONS = {
    hypertension: 'Hipertensão',
    diabetes: 'Diabetes',
    heart: 'Doença do coração',
    liver: 'Doença do fígado',
    respiratory: 'Doença respiratória',
    kidney: 'Doença dos rins',
    other: 'Outra'
};

const H_RULE_METRICS = {
    systolic: 'Pressão máxima (sistólica)',
    diastolic: 'Pressão mínima (diastólica)',
    pulse: 'Pulso medido com a pressão',
    glucose: 'Glicemia',
    weight: 'Peso',
    temperature: 'Temperatura',
    spo2: 'Saturação de oxigênio',
    heart_rate: 'Frequência cardíaca',
    urine: 'Volume de urina'
};
const H_RULE_UNITS = { systolic: 'mmHg', diastolic: 'mmHg', pulse: 'bpm' };

const H_DURATIONS = [
    ['', 'Não sei'], [15, '15 minutos'], [30, '30 minutos'], [60, '1 hora'], [120, '2 horas'],
    [360, '6 horas'], [720, '12 horas'], [1440, '1 dia'], [2880, '2 dias'], [10080, '1 semana ou mais']
];

const H_MIN_SAMPLES = 3;
const H_LATE_MINUTES = 60;
const H_ALL_DAYS = 127;

const Health = {
    profile: null,
    status: null,
    cgTab: 'history',
    view: { indicator: null, period: '7', customFrom: '', customTo: '' },
    reportPeriod: '30',
    reportCustom: { from: '', to: '' },
    report: null,
    cache: { measurements: {}, symptoms: {}, reminders: {} }
};

// ==================== ACESSO À PONTE NATIVA ====================
class HealthError extends Error {
    constructor(message, field) {
        super(message);
        this.field = field;
    }
}

function healthAvailable() {
    return !!(window.CuidarHealth && typeof window.CuidarHealth.call === 'function');
}

function hcall(method, args = {}) {
    if (!healthAvailable()) throw new HealthError('O Diário de Saúde funciona no aplicativo Android.');
    let response;
    try {
        response = JSON.parse(window.CuidarHealth.call(method, JSON.stringify(args)));
    } catch (e) {
        throw new HealthError('Não foi possível concluir. Tente novamente.');
    }
    if (!response || !response.ok) {
        throw new HealthError((response && response.error) || 'Não foi possível concluir.', response && response.field);
    }
    return response.data;
}

function hsafe(method, args, fallback) {
    try {
        return hcall(method, args);
    } catch (e) {
        return fallback;
    }
}

function healthProfile(force = false) {
    if (!healthAvailable()) return null;
    if (!Health.profile || force) Health.profile = hsafe('getProfile', {}, null);
    return Health.profile;
}

function healthHasConsent() {
    const p = healthProfile();
    return !!(p && p.consentAt);
}

function healthAuthor() {
    const role = App.actingRole || 'patient';
    return { role, name: role === 'caregiver' ? App.settings.caregiverName : App.settings.elderlyName };
}

function healthCanRecord() {
    const p = healthProfile();
    if (!p || !p.consentAt) return false;
    return healthAuthor().role !== 'caregiver' || !!p.caregiverMayRecord;
}

function healthIndicators() {
    const p = healthProfile();
    const list = p && p.indicators && p.indicators.length ? p.indicators : ['bp', 'glucose'];
    return list.filter(i => H_INDICATORS[i]);
}

// ==================== FORMATAÇÃO ====================
function hNumber(value, decimals = 0) {
    if (value === null || value === undefined || isNaN(value)) return '—';
    return Number(value).toLocaleString('pt-BR', { minimumFractionDigits: decimals, maximumFractionDigits: decimals });
}

function hTime(ms) {
    const d = new Date(ms);
    return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
}

function hDateShort(ms) {
    const d = new Date(ms);
    return `${String(d.getDate()).padStart(2, '0')}/${String(d.getMonth() + 1).padStart(2, '0')}`;
}

function hDateTime(ms) {
    return `${formatDate(dateKey(new Date(ms)))} às ${hTime(ms)}`;
}

function hParseNumber(text) {
    if (text === null || text === undefined) return NaN;
    const clean = String(text).trim().replace(/\s/g, '').replace(',', '.');
    return clean === '' ? NaN : Number(clean);
}

function hAuthorText(role, name) {
    const r = role === 'caregiver' ? 'cuidador' : role === 'notification' ? 'pela notificação' : 'idoso';
    return name ? `${name} (${r})` : r;
}

function hValueText(m) {
    const ind = H_INDICATORS[m.type];
    if (m.type === 'bp') {
        return `${hNumber(m.value1)} x ${hNumber(m.value2)} mmHg${m.value3 ? ` • pulso ${hNumber(m.value3)}` : ''}`;
    }
    return `${hNumber(m.value1, ind.decimals || 0)} ${ind.unit}`;
}

function hSymptomLabel(s) {
    if (s.symptom === 'other' && s.customLabel) return s.customLabel;
    const item = H_SYMPTOMS.find(x => x.code === s.symptom);
    return item ? item.label : s.symptom;
}

function hDurationLabel(minutes) {
    const found = H_DURATIONS.find(d => d[0] === minutes);
    if (found) return found[1];
    return minutes < 60 ? `${minutes} min` : `${Math.round(minutes / 60)} h`;
}

function hIntakeStatus(intake) {
    if (intake.status === 'missed') return 'missed';
    if (!intake.takenAt || !intake.scheduledTime) return 'taken';
    const scheduled = new Date(`${intake.scheduledDate}T${intake.scheduledTime}`).getTime();
    return intake.takenAt > scheduled + H_LATE_MINUTES * 60000 ? 'late' : 'taken';
}
const H_INTAKE_LABELS = { taken: 'Tomado', late: 'Tomado com atraso', missed: 'Não tomado' };

function hPeriodRange(period, customFrom, customTo) {
    const start = new Date();
    start.setHours(0, 0, 0, 0);
    let end = new Date();
    end.setHours(23, 59, 59, 999);
    if (period === 'custom' && customFrom && customTo) {
        const from = new Date(`${customFrom}T00:00:00`);
        const to = new Date(`${customTo}T23:59:59.999`);
        if (!isNaN(from) && !isNaN(to) && from <= to) return { from: from.getTime(), to: to.getTime() };
    }
    const days = { today: 1, '7': 7, '30': 30, '90': 90 }[period] || 7;
    start.setDate(start.getDate() - (days - 1));
    return { from: start.getTime(), to: end.getTime() };
}

function hStats(values) {
    const list = values.filter(v => v !== null && v !== undefined && !isNaN(v));
    if (list.length < H_MIN_SAMPLES) return { count: list.length };
    const sum = list.reduce((a, b) => a + b, 0);
    return { count: list.length, mean: sum / list.length, min: Math.min(...list), max: Math.max(...list) };
}

function hStatsLine(label, values, unit, decimals = 0) {
    const s = hStats(values);
    const count = `${s.count} ${s.count === 1 ? 'registro' : 'registros'}`;
    if (s.mean === undefined) {
        return `<div class="stats-line"><strong>${escapeHtml(label)}</strong><span>${count}. A média aparece a partir de ${H_MIN_SAMPLES} registros.</span></div>`;
    }
    return `<div class="stats-line"><strong>${escapeHtml(label)}</strong><span>${count} • média ${hNumber(s.mean, 1)} ${unit} • menor ${hNumber(s.min, decimals)} • maior ${hNumber(s.max, decimals)}</span></div>`;
}

// ==================== ENTRADA PELA NOTIFICAÇÃO E ATUALIZAÇÕES ====================
window.CuidarOpenHealth = function () {
    const current = document.querySelector('.screen.active');
    if (current && current.id === 'screen-caregiver') {
        Health.cgTab = 'reminders';
        setCaregiverTab('health');
    } else if (current && current.id === 'screen-elderly') {
        setCategory('health');
    } else {
        App.history = [];
        enterElderly('health');
    }
};

window.CuidarHealthChanged = function () {
    Health.status = null;
    const current = document.querySelector('.screen.active');
    if (!current) return;
    if (current.id === 'screen-elderly') rerenderElderlyKeepingScroll();
    else if (current.id === 'screen-caregiver' && ['panel', 'health'].includes(App.caregiverTab)) {
        const content = document.getElementById('caregiver-content');
        const top = content.scrollTop;
        renderCaregiver();
        content.scrollTop = top;
    } else if (current.id === 'screen-health-history') renderHealthScreen(current.id);
};

function renderHealthScreen(screenId) {
    if (screenId === 'screen-health-history') {
        const body = document.getElementById('health-history-body');
        body.innerHTML = renderHealthHistory();
        bindHealthEvents(body);
    } else if (screenId === 'screen-health-consent') {
        renderConsentScreen();
    } else if (screenId === 'screen-health-report') {
        renderReportPreview();
    }
}

// ==================== TELA DO IDOSO ====================
function renderElderlyHealthDiary() {
    if (!healthAvailable()) {
        return tip('', ICONS.info, 'Diário de Saúde', 'Disponível no aplicativo instalado no celular Android.');
    }
    const profile = healthProfile();
    if (!profile || !profile.consentAt) return consentCallToAction();

    const pending = healthPendingEvents();
    return `
        ${pending.length ? section('Lembretes de agora', pending.map(eventCard).join('')) : ''}
        ${section('Anotar agora', recordButtons(), '')}
        ${healthLatestSection()}
    `;
}

function consentCallToAction() {
    return `
        <div class="card health-cta">
            <div class="health-cta-icon">${H_ICONS.heart}</div>
            <div class="task-title">Diário de Saúde</div>
            <p class="task-subtitle">Anote pressão, glicemia, sintomas e remédios para mostrar na consulta. Os dados ficam só neste celular.</p>
            <button class="btn btn-primary btn-block" data-h-consent>Começar o diário</button>
        </div>`;
}

function recordButtons() {
    if (!healthCanRecord()) {
        return tip('', ICONS.info, 'Registro pelo cuidador não autorizado',
            'Para o cuidador anotar pelo idoso, ative a autorização em Cuidador > Saúde > Perfil, com a concordância do idoso.');
    }
    const items = healthIndicators().map(type => {
        const ind = H_INDICATORS[type];
        return `<button class="health-action" data-h-record="${type}"><span class="health-action-icon">${ind.icon}</span><span>${ind.label}</span></button>`;
    });
    items.push(`<button class="health-action" data-h-record="symptom"><span class="health-action-icon symptom">${H_ICONS.symptom}</span><span>Sintoma</span></button>`);
    return `<div class="health-grid">${items.join('')}</div>`;
}

function healthLatestSection() {
    const range = hPeriodRange('7');
    const measurements = hsafe('listMeasurements', { from: range.from, to: range.to }, []);
    const symptoms = hsafe('listSymptoms', { from: range.from, to: range.to }, []);
    measurements.forEach(m => { Health.cache.measurements[m.id] = m; });
    symptoms.forEach(s => { Health.cache.symptoms[s.id] = s; });
    const latest = [
        ...measurements.map(m => ({ at: m.measuredAt, html: measurementItem(m) })),
        ...symptoms.map(s => ({ at: s.occurredAt, html: symptomItem(s) }))
    ].sort((a, b) => b.at - a.at).slice(0, 3);
    return section('Últimos registros', `
        ${latest.length ? latest.map(i => i.html).join('') : emptyState('', 'Nenhum registro nos últimos 7 dias.')}
        <button class="btn btn-secondary btn-block" data-h-history>${H_ICONS.chart}Ver histórico e gráficos</button>
    `);
}

// ==================== LEMBRETES PENDENTES ====================
function healthPendingEvents() {
    const start = new Date();
    start.setHours(0, 0, 0, 0);
    const events = hsafe('listReminderEvents', { from: start.getTime(), to: Date.now() }, []);
    return events.filter(e => e.status === 'pending');
}

function eventCard(e) {
    const kindLabel = { medication: 'Remédio', measurement: 'Medição', appointment: 'Consulta' }[e.kind] || 'Lembrete';
    const doneLabel = e.kind === 'medication' ? 'Tomei' : e.kind === 'appointment' ? 'Ciente' : 'Feito';
    const ignoreLabel = e.kind === 'medication' ? 'Não tomei' : 'Ignorar';
    return `
        <div class="task-card reminder-card">
            <span class="task-avatar primary">${H_ICONS.bell}</span>
            <span class="task-info">
                <span class="task-title">${escapeHtml(e.title)}</span>
                <span class="task-subtitle">${kindLabel} • ${hTime(e.scheduledAt)}${e.detail ? ' • ' + escapeHtml(e.detail) : ''}</span>
                <span class="reminder-actions">
                    <button class="pill-btn primary" data-h-event="${e.id}" data-h-status="done">${doneLabel}</button>
                    <button class="pill-btn" data-h-event="${e.id}" data-h-status="ignored">${ignoreLabel}</button>
                </span>
            </span>
        </div>`;
}

function resolveHealthEvent(eventId, status) {
    const start = new Date();
    start.setHours(0, 0, 0, 0);
    const events = hsafe('listReminderEvents', { from: start.getTime() - 86400000 * 31, to: Date.now() }, []);
    const event = events.find(e => e.id === eventId);
    try {
        if (event && event.kind === 'medication' && event.refId) {
            const med = App.data.medications.find(m => String(m.id) === event.refId);
            const key = dateKey(new Date(event.scheduledAt));
            if (med && key === dateKey()) {
                // Mesma marcação da lista de remédios (resolve o lembrete pelo lado nativo).
                setMedicationStatus(med, status === 'done' ? 'taken' : 'missed');
                return;
            }
        }
        hcall('resolveReminderEvent', { id: eventId, status, author: healthAuthor() });
        showToast(status === 'done' ? 'Lembrete concluído' : 'Lembrete ignorado', 'success');
    } catch (e) {
        showToast(e.message, 'error');
    }
    window.CuidarHealthChanged();
}

// ==================== REMÉDIOS: TOMADO / ATRASADO / NÃO TOMADO ====================
function healthTodayIntakes() {
    if (!healthHasConsent()) return {};
    const today = dateKey();
    const list = hsafe('listIntakes', { fromDate: today, toDate: today }, []);
    const map = {};
    list.forEach(i => { map[i.medRef] = i; });
    return map;
}

// Doses confirmadas pela notificação ("Tomei") aparecem marcadas na lista do dia.
function syncIntakesToLog() {
    const intakes = healthTodayIntakes();
    const log = getLog();
    let changed = false;
    App.data.medications.forEach(m => {
        const intake = intakes[String(m.id)];
        if (intake && intake.status === 'taken' && !log.meds.includes(m.id)) {
            log.meds.push(m.id);
            changed = true;
        }
    });
    if (changed) saveToStorage();
    return intakes;
}

function recordMedicationIntake(med, status) {
    if (!healthHasConsent()) return null;
    try {
        return hcall('recordIntake', {
            record: {
                medRef: String(med.id), medName: med.name, dosage: med.dosage,
                scheduledDate: dateKey(), scheduledTime: med.time || '', status
            },
            author: healthAuthor()
        });
    } catch (e) {
        if (e.field !== 'consent') showToast(e.message, 'error');
        return null;
    }
}

function removeMedicationIntake(med) {
    if (!healthHasConsent()) return;
    hsafe('deleteIntake', { medRef: String(med.id), date: dateKey(), author: healthAuthor() });
}

/** status: 'taken' | 'missed' | null (desfazer). Mantém o registro do dia e o diário iguais. */
function setMedicationStatus(med, status) {
    const log = getLog();
    const index = log.meds.indexOf(med.id);
    if (status === 'taken' && index < 0) log.meds.push(med.id);
    if (status !== 'taken' && index >= 0) log.meds.splice(index, 1);
    saveToStorage();
    if (status) recordMedicationIntake(med, status);
    else removeMedicationIntake(med);
    if (status === 'taken') showToast('Remédio tomado! 💊', 'success');
    else if (status === 'missed') showToast('Anotado: remédio não tomado.', 'success');
    else showToast('Marcação desfeita');
    window.CuidarHealthChanged();
}

async function askMissedMedication(medId) {
    const med = App.data.medications.find(m => m.id === medId);
    if (!med) return;
    const answer = await askChoice('Não tomou este remédio?', `${med.name} • ${med.dosage}. Isso fica anotado no histórico. Em caso de dúvida sobre a dose, fale com o médico ou farmacêutico.`, [
        { value: 'missed', label: 'Não tomei', emoji: '✖️' },
        { value: null, label: 'Cancelar', style: 'muted' }
    ]);
    if (answer) setMedicationStatus(med, 'missed');
}

function medicationIntakeNote(med, intakes) {
    const intake = intakes[String(med.id)];
    if (!intake) return '';
    const st = hIntakeStatus(intake);
    if (st === 'missed') return 'Marcado como não tomado';
    return `${H_INTAKE_LABELS[st]} às ${hTime(intake.takenAt)}`;
}

// ==================== CUIDADOR: ABA SAÚDE ====================
function renderCaregiverHealth() {
    if (!healthAvailable()) {
        return emptyState('Diário de Saúde', 'Disponível no aplicativo instalado no celular Android.');
    }
    const profile = healthProfile(true);
    if (!profile || !profile.consentAt) return consentCallToAction();

    const tabs = { history: 'Histórico', reminders: 'Lembretes', report: 'Relatório', profile: 'Perfil' };
    const renderers = { history: caregiverHealthHistory, reminders: caregiverReminders, report: caregiverReport, profile: caregiverProfile };
    return `
        <div class="scroll-row" role="tablist">
            ${Object.entries(tabs).map(([k, v]) => `<button type="button" role="tab" aria-selected="${Health.cgTab === k}" class="day-tab ${Health.cgTab === k ? 'active' : ''}" data-h-cgtab="${k}">${v}</button>`).join('')}
        </div>
        ${(renderers[Health.cgTab] || caregiverHealthHistory)()}
    `;
}

function caregiverHealthHistory() {
    return `${section('Anotar pelo idoso', recordButtons())}${renderHealthHistory()}`;
}

function caregiverHealthSummary() {
    if (!healthHasConsent()) return '';
    const range = hPeriodRange('today');
    const measurements = hsafe('listMeasurements', { from: range.from, to: range.to }, []);
    const pending = healthPendingEvents();
    const last = {};
    measurements.forEach(m => { last[m.type] = m; });
    const parts = Object.values(last).map(m => `${H_INDICATORS[m.type].label}: ${hValueText(m)} (${hTime(m.measuredAt)})`);
    return `
        <button class="task-card stats-follow" data-h-goto-health>
            <span class="task-avatar health">${H_ICONS.heart}</span>
            <span class="task-info">
                <span class="task-title">Saúde hoje</span>
                <span class="task-subtitle">${parts.length ? escapeHtml(parts.join(' • ')) : 'Nenhuma medição hoje'}${pending.length ? ` • ${pending.length} ${pending.length === 1 ? 'lembrete pendente' : 'lembretes pendentes'}` : ''}</span>
            </span>
            <span class="chevron">${ICONS.chevron}</span>
        </button>`;
}

function healthTodaySymptomLabels() {
    if (!healthHasConsent()) return [];
    const range = hPeriodRange('today');
    return hsafe('listSymptoms', { from: range.from, to: range.to }, []).map(hSymptomLabel);
}

// ==================== HISTÓRICO E GRÁFICOS ====================
function renderHealthHistory() {
    if (!healthHasConsent()) return consentCallToAction();
    const indicators = healthIndicators();
    if (!Health.view.indicator || (!indicators.includes(Health.view.indicator) && !['symptoms', 'meds'].includes(Health.view.indicator))) {
        Health.view.indicator = indicators[0];
    }
    const v = Health.view;
    const range = hPeriodRange(v.period, v.customFrom, v.customTo);
    const tabs = [...indicators.map(i => [i, H_INDICATORS[i].label]), ['symptoms', 'Sintomas'], ['meds', 'Remédios']];
    const periods = [['today', 'Hoje'], ['7', '7 dias'], ['30', '30 dias'], ['custom', 'Período']];

    let content;
    try {
        if (v.indicator === 'symptoms') content = symptomHistory(range);
        else if (v.indicator === 'meds') content = medicationHistory(range);
        else content = measurementHistory(v.indicator, range);
    } catch (e) {
        content = emptyState('Não foi possível carregar', e.message);
    }

    return `
        <div class="scroll-row" aria-label="O que mostrar">
            ${tabs.map(([k, label]) => `<button type="button" class="day-tab ${v.indicator === k ? 'active' : ''}" aria-pressed="${v.indicator === k}" data-h-ind="${k}">${label}</button>`).join('')}
        </div>
        <div class="segmented period-picker" aria-label="Período">
            ${periods.map(([k, label]) => `<button type="button" class="segmented-option ${v.period === k ? 'active' : ''}" aria-pressed="${v.period === k}" data-h-period="${k}">${label}</button>`).join('')}
        </div>
        ${v.period === 'custom' ? customRangeForm('h-range', v.customFrom, v.customTo) : ''}
        <p class="period-caption">${hDateShort(range.from)} a ${hDateShort(range.to)}</p>
        ${content}
    `;
}

function customRangeForm(prefix, from, to) {
    const today = dateKey();
    return `
        <div class="range-row">
            <label class="form-label">De<input type="date" class="form-input" id="${prefix}-from" value="${from || today}" max="${today}"></label>
            <label class="form-label">Até<input type="date" class="form-input" id="${prefix}-to" value="${to || today}" max="${today}"></label>
            <button type="button" class="btn btn-secondary" data-h-apply-range="${prefix}">Aplicar</button>
        </div>`;
}

function measurementHistory(type, range) {
    const ind = H_INDICATORS[type];
    const list = hcall('listMeasurements', { type, from: range.from, to: range.to });
    list.forEach(m => { Health.cache.measurements[m.id] = m; });
    if (!list.length) {
        return emptyState(`Sem registros de ${ind.long.toLowerCase()}`, 'Nenhuma medição neste período. Os gráficos aparecem quando houver registros.');
    }

    let stats;
    let series;
    if (type === 'bp') {
        series = [
            { name: 'Máxima (sistólica)', points: list.map(m => ({ t: m.measuredAt, v: m.value1 })) },
            { name: 'Mínima (diastólica)', points: list.map(m => ({ t: m.measuredAt, v: m.value2 })) }
        ];
        const pulses = list.filter(m => m.value3).map(m => m.value3);
        stats = hStatsLine('Máxima (sistólica)', list.map(m => m.value1), 'mmHg')
            + hStatsLine('Mínima (diastólica)', list.map(m => m.value2), 'mmHg')
            + (pulses.length ? hStatsLine('Pulso', pulses, 'bpm') : '');
    } else {
        series = [{ name: ind.long, points: list.map(m => ({ t: m.measuredAt, v: m.value1 })) }];
        if (type === 'glucose') {
            // Médias só fazem sentido dentro do mesmo momento de medição.
            stats = Object.entries(H_GLUCOSE_CONTEXTS)
                .map(([ctx, label]) => [label, list.filter(m => m.context === ctx).map(m => m.value1)])
                .filter(([, values]) => values.length)
                .map(([label, values]) => hStatsLine(label, values, ind.unit))
                .join('');
        } else {
            stats = hStatsLine('Registros', list.map(m => m.value1), ind.unit, ind.decimals || 0);
        }
    }

    return `
        <div class="card chart-card">
            <div class="chart-title">${escapeHtml(ind.long)} <small>(${ind.unit})</small></div>
            ${chartLegend(series, ind.unit)}
            ${lineChart(series, range, ind)}
        </div>
        <div class="card stats-card">${stats}<p class="form-hint">Resumo dos números anotados. Não indica se os valores estão bons ou ruins: converse com o profissional de saúde.</p></div>
        ${section('Registros', [...list].reverse().map(measurementItem).join(''), `${list.length}`)}
    `;
}

function chartLegend(series, unit) {
    return `<div class="chart-legend">${series.map((s, i) => `<span><i class="legend-marker s${i}"></i>${escapeHtml(s.name)} (${unit})</span>`).join('')}</div>`;
}

function lineChart(series, range, ind) {
    const W = 320, H = 180, L = 40, R = 12, T = 10, B = 24;
    const points = series.flatMap(s => s.points).filter(p => p.v !== null && p.v !== undefined);
    if (!points.length) return '';
    let minV = Math.min(...points.map(p => p.v));
    let maxV = Math.max(...points.map(p => p.v));
    const pad = Math.max((maxV - minV) * 0.15, Math.max(1, Math.abs(maxV) * 0.05));
    minV = Math.max(0, minV - pad);
    maxV += pad;
    const t0 = range.from;
    const t1 = Math.max(range.to, t0 + 3600000);
    const x = t => L + (W - L - R) * (t - t0) / (t1 - t0);
    const y = v => H - B - (H - B - T) * (v - minV) / (maxV - minV);
    const decimals = maxV - minV < 10 ? 1 : 0;

    let grid = '';
    for (let i = 0; i <= 4; i++) {
        const v = minV + (maxV - minV) * i / 4;
        const gy = y(v).toFixed(1);
        grid += `<line class="chart-grid" x1="${L}" x2="${W - R}" y1="${gy}" y2="${gy}"/><text class="chart-axis" x="${L - 5}" y="${(+gy + 3).toFixed(1)}" text-anchor="end">${hNumber(v, decimals)}</text>`;
    }
    const sameDay = t1 - t0 <= 36 * 3600000;
    const ticks = [t0, t0 + (t1 - t0) / 2, t1];
    const xLabels = ticks.map((t, i) => {
        const anchor = i === 0 ? 'start' : i === 2 ? 'end' : 'middle';
        return `<text class="chart-axis" x="${x(t).toFixed(1)}" y="${H - 6}" text-anchor="${anchor}">${sameDay ? hTime(t) : hDateShort(t)}</text>`;
    }).join('');

    const lines = series.map((s, i) => {
        const pts = s.points.filter(p => p.v !== null && p.v !== undefined).sort((a, b) => a.t - b.t);
        const path = pts.map(p => `${x(p.t).toFixed(1)},${y(p.v).toFixed(1)}`).join(' ');
        const markers = pts.map(p => i % 2 === 0
            ? `<circle class="chart-marker s${i}" cx="${x(p.t).toFixed(1)}" cy="${y(p.v).toFixed(1)}" r="3.2"/>`
            : `<rect class="chart-marker s${i}" x="${(x(p.t) - 3).toFixed(1)}" y="${(y(p.v) - 3).toFixed(1)}" width="6" height="6"/>`).join('');
        return `<polyline class="chart-line s${i}" points="${path}"/>${markers}`;
    }).join('');

    const label = `Gráfico de ${ind.long.toLowerCase()} em ${ind.unit}, ${points.length / series.length} registros entre ${hDateShort(t0)} e ${hDateShort(t1)}`;
    return `<svg class="chart" viewBox="0 0 ${W} ${H}" role="img" aria-label="${escapeHtml(label)}">
        ${grid}
        <line class="chart-axis-line" x1="${L}" x2="${L}" y1="${T}" y2="${H - B}"/>
        <line class="chart-axis-line" x1="${L}" x2="${W - R}" y1="${H - B}" y2="${H - B}"/>
        ${xLabels}${lines}
    </svg>`;
}

function measurementItem(m) {
    const ind = H_INDICATORS[m.type];
    const context = m.context ? ` • ${H_GLUCOSE_CONTEXTS[m.context]}` : '';
    return `
        <button class="task-card record-item" data-h-edit="m" data-id="${m.id}">
            <span class="task-avatar health">${ind.icon}</span>
            <span class="task-info">
                <span class="task-title">${escapeHtml(hValueText(m))}</span>
                <span class="task-subtitle">${ind.label} • ${hDateTime(m.measuredAt)}${context}</span>
                <span class="record-meta">por ${escapeHtml(hAuthorText(m.createdByRole, m.createdByName))}${m.updatedAt ? ' • editado' : ''}${m.note ? ' • com observação' : ''}</span>
            </span>
            <span class="chevron">${ICONS.chevron}</span>
        </button>`;
}

function symptomItem(s) {
    const urgent = H_SYMPTOMS.find(x => x.code === s.symptom && x.urgent);
    return `
        <button class="task-card record-item" data-h-edit="s" data-id="${s.id}">
            <span class="task-avatar ${urgent ? 'danger' : 'health'}">${H_ICONS.symptom}</span>
            <span class="task-info">
                <span class="task-title">${escapeHtml(hSymptomLabel(s))}</span>
                <span class="task-subtitle">${H_INTENSITY[s.intensity]} • ${hDateTime(s.occurredAt)}${s.durationMinutes ? ' • ' + hDurationLabel(s.durationMinutes) : ''}</span>
                <span class="record-meta">por ${escapeHtml(hAuthorText(s.createdByRole, s.createdByName))}${s.updatedAt ? ' • editado' : ''}${s.note ? ' • com observação' : ''}</span>
            </span>
            <span class="chevron">${ICONS.chevron}</span>
        </button>`;
}

function symptomHistory(range) {
    const list = hcall('listSymptoms', { from: range.from, to: range.to });
    list.forEach(s => { Health.cache.symptoms[s.id] = s; });
    if (!list.length) return emptyState('Nenhum sintoma', 'Nenhum sintoma anotado neste período.');
    const counts = {};
    list.forEach(s => { const l = hSymptomLabel(s); counts[l] = (counts[l] || 0) + 1; });
    const summary = Object.entries(counts).sort((a, b) => b[1] - a[1])
        .map(([label, n]) => `<div class="stats-line"><strong>${escapeHtml(label)}</strong><span>${n} ${n === 1 ? 'vez' : 'vezes'}</span></div>`).join('');
    return `
        <div class="card stats-card">${summary}</div>
        ${section('Registros', [...list].reverse().map(symptomItem).join(''), `${list.length}`)}`;
}

function medicationHistory(range) {
    const list = hcall('listIntakes', { fromDate: dateKey(new Date(range.from)), toDate: dateKey(new Date(range.to)) });
    if (!list.length) {
        return emptyState('Nenhuma confirmação', 'Nenhum remédio foi marcado como tomado ou não tomado neste período.');
    }
    const counts = { taken: 0, late: 0, missed: 0 };
    list.forEach(i => { counts[hIntakeStatus(i)]++; });
    const items = [...list].reverse().map(i => {
        const st = hIntakeStatus(i);
        return `
            <div class="task-card">
                <span class="task-avatar medication">${ICONS.pill}</span>
                <span class="task-info">
                    <span class="task-title">${escapeHtml(i.medName)}</span>
                    <span class="task-subtitle">${formatDate(i.scheduledDate)}${i.scheduledTime ? ' • previsto ' + i.scheduledTime : ''}${i.dosage ? ' • ' + escapeHtml(i.dosage) : ''}</span>
                    <span class="record-meta"><span class="status-tag ${st}">${H_INTAKE_LABELS[st]}${i.takenAt ? ' às ' + hTime(i.takenAt) : ''}</span> por ${escapeHtml(hAuthorText(i.createdByRole, i.createdByName))}</span>
                </span>
            </div>`;
    }).join('');
    return `
        <div class="card stats-card">
            <div class="stats-line"><strong>Tomado</strong><span>${counts.taken}</span></div>
            <div class="stats-line"><strong>Tomado com atraso</strong><span>${counts.late} (confirmado mais de ${H_LATE_MINUTES} min depois do horário)</span></div>
            <div class="stats-line"><strong>Não tomado</strong><span>${counts.missed}</span></div>
            <p class="form-hint">Mostra só o que foi marcado. Dias sem marcação não significam que o remédio foi ou não foi tomado.</p>
        </div>
        ${section('Registros', items, `${list.length}`)}`;
}

// ==================== LEMBRETES (CUIDADOR) ====================
function caregiverReminders() {
    const status = Health.status || (Health.status = hsafe('getStatus', {}, null));
    const reminders = hsafe('listReminders', {}, []);
    Health.cache.reminders = {};
    reminders.forEach(r => { Health.cache.reminders[r.reminder.id] = r; });
    const start = new Date();
    start.setHours(0, 0, 0, 0);
    const events = hsafe('listReminderEvents', { from: start.getTime() - 6 * 86400000, to: Date.now() }, []);
    const pending = events.filter(e => e.status === 'pending');
    const resolved = events.filter(e => e.status !== 'pending').slice(0, 15);

    const kindIcon = { medication: ICONS.pill, measurement: H_ICONS.heart, appointment: ICONS.calendar };
    const kindClass = { medication: 'medication', measurement: 'health', appointment: 'visit' };
    const list = reminders.map(({ reminder: r, nextAt }) => {
        const when = r.kind === 'appointment' ? (r.onceAt ? hDateTime(r.onceAt) : '') : `${r.times.split(',').join(', ')} • ${daysLabelFromMask(r.daysMask)}`;
        const next = !r.enabled ? 'Desligado' : nextAt ? `Próximo: ${hDateTime(nextAt)}` : 'Sem próximos avisos';
        const attrs = r.kind === 'measurement' ? `data-h-reminder="${r.id}"` : r.kind === 'medication' ? `data-edit="medication" data-id="${r.refId}"` : `data-edit="visit" data-id="${r.refId}"`;
        return `
            <button class="task-card" ${attrs}>
                <span class="task-avatar ${kindClass[r.kind]}">${kindIcon[r.kind]}</span>
                <span class="task-info">
                    <span class="task-title">${escapeHtml(r.title)}</span>
                    <span class="task-subtitle">${escapeHtml(when)}</span>
                    <span class="record-meta">${escapeHtml(next)}</span>
                </span>
                <span class="chevron">${ICONS.chevron}</span>
            </button>`;
    }).join('');

    const statusLabel = { done: 'Feito', ignored: 'Ignorado' };
    return `
        ${reminderStatusCard(status)}
        ${pending.length ? section('Pendentes', pending.map(eventCard).join(''), `${pending.length}`) : ''}
        ${section('Lembretes cadastrados', `
            <button class="add-btn" data-h-reminder-new>${ICONS.plus}Lembrete de medição</button>
            ${list || emptyState('Nenhum lembrete', 'Cadastre lembretes com os horários orientados pelo profissional. Remédios e consultas ganham lembrete no próprio cadastro.')}
        `)}
        ${resolved.length ? section('Últimos 7 dias', resolved.map(e => `
            <div class="task-card">
                <span class="task-info">
                    <span class="task-title">${escapeHtml(e.title)}</span>
                    <span class="task-subtitle">${hDateTime(e.scheduledAt)}</span>
                    <span class="record-meta"><span class="status-tag ${e.status === 'done' ? 'taken' : 'missed'}">${statusLabel[e.status]}</span>${e.resolvedBy ? ' por ' + escapeHtml(e.resolvedBy) : ''}</span>
                </span>
            </div>`).join('')) : ''}
    `;
}

function reminderStatusCard(status) {
    if (!status) return '';
    const rows = [];
    rows.push(statusRow(status.notificationsAllowed, 'Notificações',
        status.notificationsAllowed ? 'Permitidas' : 'Bloqueadas: os lembretes não vão aparecer', 'notifications', 'Permitir'));
    if (status.exactAlarmsConfigurable) {
        rows.push(statusRow(status.exactAlarms, 'Horário exato',
            status.exactAlarms ? 'Os avisos saem no minuto certo' : 'Sem esta permissão o Android pode atrasar o aviso alguns minutos', 'exact', 'Permitir'));
    }
    rows.push(statusRow(status.batteryUnrestricted, 'Economia de bateria',
        status.batteryUnrestricted ? 'Sem restrição para o CuidarApp' : 'Alguns celulares atrasam ou bloqueiam avisos para economizar bateria', 'battery', 'Ajustar'));
    return `<div class="settings-card status-card">${rows.join('')}</div>`;
}

function statusRow(ok, title, desc, action, button) {
    return `
        <div class="settings-item">
            <span class="settings-item-icon ${ok ? 'success' : 'danger'}">${ok ? ICONS.check : ICONS.alert}</span>
            <span class="settings-item-left settings-item-body">
                <span class="settings-item-text">${title}</span>
                <span class="settings-item-desc">${desc}</span>
            </span>
            ${ok ? '' : `<button class="pill-btn primary" data-h-perm="${action}">${button}</button>`}
        </div>`;
}

function daysLabelFromMask(mask) {
    if ((mask & H_ALL_DAYS) === H_ALL_DAYS) return 'Todos os dias';
    return [1, 2, 3, 4, 5, 6, 0].filter(d => mask & (1 << d)).map(d => WEEKDAYS_SHORT[d]).join(', ');
}

function maskFromFrequency(frequency) {
    if (frequency === 'weekdays') return 0b0111110;
    if (frequency === 'weekly') return 0b0000010;
    return H_ALL_DAYS;
}

/** Lembrete vinculado ao remédio: usa exatamente o horário cadastrado, sem horário padrão. */
function syncMedicationReminder(med, enabled) {
    if (!healthHasConsent()) return;
    try {
        if (enabled && med.time) {
            hcall('saveReminder', {
                record: {
                    kind: 'medication', refId: String(med.id), title: med.name, detail: med.dosage,
                    times: med.time, daysMask: maskFromFrequency(med.frequency), enabled: true
                },
                author: healthAuthor()
            });
        } else {
            hcall('deleteLinkedReminder', { kind: 'medication', refId: String(med.id) });
        }
    } catch (e) {
        showToast(e.message, 'error');
    }
}

function syncVisitReminder(visit, offsetMinutes) {
    if (!healthHasConsent()) return;
    try {
        if (offsetMinutes !== '' && offsetMinutes !== null && visit.time) {
            const at = new Date(`${visit.date}T${visit.time}`).getTime() - Number(offsetMinutes) * 60000;
            hcall('saveReminder', {
                record: {
                    kind: 'appointment', refId: String(visit.id), title: visit.description || 'Consulta ou visita',
                    detail: `${formatDate(visit.date)} às ${visit.time}`, onceAt: at, daysMask: 0, enabled: true
                },
                author: healthAuthor()
            });
            if (at <= Date.now()) showToast('O horário do aviso já passou; nenhum lembrete será mostrado.', 'error');
        } else {
            hcall('deleteLinkedReminder', { kind: 'appointment', refId: String(visit.id) });
        }
    } catch (e) {
        showToast(e.message, 'error');
    }
}

function linkedReminder(kind, refId) {
    if (!healthHasConsent() || !refId) return null;
    const found = hsafe('listReminders', {}, []).find(r => r.reminder.kind === kind && r.reminder.refId === String(refId));
    return found ? found.reminder : null;
}

function medicationReminderField(med) {
    if (!healthAvailable()) return '';
    if (!healthHasConsent()) {
        return `<p class="form-hint">Para receber lembretes no horário, ative o Diário de Saúde na aba Saúde.</p>`;
    }
    const reminder = med ? linkedReminder('medication', med.id) : null;
    return `
        <div class="settings-card form-group">
            <div class="settings-item">
                <span class="settings-item-icon primary">${H_ICONS.bell}</span>
                <span class="settings-item-left settings-item-body">
                    <span class="settings-item-text">Avisar no horário</span>
                    <span class="settings-item-desc">Notificação no horário acima, nos dias da frequência</span>
                </span>
                <button type="button" class="toggle ${reminder && reminder.enabled ? 'active' : ''}" id="med-reminder" role="switch" aria-checked="${!!(reminder && reminder.enabled)}" aria-label="Avisar no horário"></button>
            </div>
        </div>`;
}

function visitReminderField(visit) {
    if (!healthAvailable() || !healthHasConsent()) return '';
    const reminder = visit ? linkedReminder('appointment', visit.id) : null;
    let current = '';
    if (reminder && visit && visit.time) {
        const diff = Math.round((new Date(`${visit.date}T${visit.time}`).getTime() - reminder.onceAt) / 60000);
        current = String(diff);
    }
    const options = [['', 'Sem lembrete'], ['0', 'Na hora'], ['60', '1 hora antes'], ['180', '3 horas antes'], ['1440', '1 dia antes']];
    return `
        <div class="form-group">
            <label class="form-label" for="visit-reminder">Lembrete</label>
            <select class="form-input" id="visit-reminder">
                ${options.map(([v, l]) => `<option value="${v}" ${current === v ? 'selected' : ''}>${l}</option>`).join('')}
            </select>
            <p class="form-hint">Precisa do horário preenchido.</p>
        </div>`;
}

// ==================== RELATÓRIO PDF ====================
function caregiverReport() {
    const periods = [['7', '7 dias'], ['30', '30 dias'], ['90', '90 dias'], ['custom', 'Período']];
    const range = hPeriodRange(Health.reportPeriod, Health.reportCustom.from, Health.reportCustom.to);
    return `
        ${section('Relatório para a consulta', `
            <div class="segmented period-picker">
                ${periods.map(([k, l]) => `<button type="button" class="segmented-option ${Health.reportPeriod === k ? 'active' : ''}" aria-pressed="${Health.reportPeriod === k}" data-h-report-period="${k}">${l}</button>`).join('')}
            </div>
            ${Health.reportPeriod === 'custom' ? customRangeForm('h-report', Health.reportCustom.from, Health.reportCustom.to) : ''}
            <p class="period-caption">${hDateShort(range.from)} a ${hDateShort(range.to)}</p>
            <div class="card">
                <div class="task-subtitle">O PDF traz: nome do idoso, período, quantidade de registros, tabelas com unidades, gráficos, sintomas, observações, histórico de remédios e a data de emissão.</div>
                <p class="form-hint">Inclui o aviso de que os dados foram anotados em casa e não substituem a avaliação clínica.</p>
            </div>
            <button class="btn btn-primary btn-block" id="h-report-generate" data-h-report-generate>${H_ICONS.file}Gerar relatório PDF</button>
            <p class="report-status" id="h-report-status" role="status" aria-live="polite"></p>
        `)}
        ${tip('', H_ICONS.shield, 'Você decide com quem compartilhar', 'O relatório fica guardado só no celular. Ele só sai daqui se você tocar em Compartilhar e escolher um aplicativo.')}
    `;
}

function generateHealthReport() {
    const btn = document.getElementById('h-report-generate');
    const status = document.getElementById('h-report-status');
    const range = hPeriodRange(Health.reportPeriod, Health.reportCustom.from, Health.reportCustom.to);
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner" aria-hidden="true"></span>Gerando relatório…';
    status.textContent = 'Gerando relatório, aguarde.';
    // Deixa a tela mostrar o "carregando" antes da chamada nativa, que é síncrona.
    setTimeout(() => {
        try {
            Health.report = hcall('generateReport', { patientName: App.settings.elderlyName, from: range.from, to: range.to });
            Health.report.range = range;
            status.textContent = '';
            showScreen('screen-health-report');
        } catch (e) {
            status.textContent = e.message;
            showToast(e.message, 'error');
        } finally {
            btn.disabled = false;
            btn.innerHTML = `${H_ICONS.file}Gerar relatório PDF`;
        }
    }, 60);
}

function renderReportPreview() {
    const body = document.getElementById('health-report-body');
    const r = Health.report;
    if (!r) {
        body.innerHTML = emptyState('Nenhum relatório', 'Gere o relatório na aba Saúde do cuidador.');
        return;
    }
    body.innerHTML = `
        <div class="card">
            <div class="task-title">${escapeHtml(r.fileName)}</div>
            <div class="task-subtitle">${r.pageCount} ${r.pageCount === 1 ? 'página' : 'páginas'} • ${r.totalRecords} registros • ${hDateShort(r.range.from)} a ${hDateShort(r.range.to)}</div>
        </div>
        <button class="btn btn-primary btn-block" id="h-report-share">${H_ICONS.share}Compartilhar PDF</button>
        <p class="form-hint center">Abre a lista de aplicativos do Android. Nada é enviado automaticamente.</p>
        <div class="report-pages">
            ${(r.pages || []).map((src, i) => `<img src="${src}" alt="Página ${i + 1} do relatório">`).join('')}
        </div>
        ${r.pageCount > (r.pages || []).length ? `<p class="form-hint center">A pré-visualização mostra as primeiras ${(r.pages || []).length} páginas. O PDF compartilhado tem todas.</p>` : ''}
    `;
    document.getElementById('h-report-share').addEventListener('click', () => {
        try {
            hcall('shareReport');
        } catch (e) {
            showToast(e.message, 'error');
        }
    });
}

// ==================== PERFIL, ORIENTAÇÕES E PRIVACIDADE ====================
function caregiverProfile() {
    const p = healthProfile(true);
    const conditions = (p.conditions || []).filter(c => c !== 'other').map(c => H_CONDITIONS[c]);
    if ((p.conditions || []).includes('other') && p.conditionsOther) conditions.push(p.conditionsOther);
    const rules = p.alertRules || [];
    const opLabel = { gte: 'igual ou acima de', lte: 'igual ou abaixo de' };

    return `
        ${section('Perfil de saúde', `
            <button class="settings-card profile-summary" data-h-profile-edit>
                <span class="settings-item"><span class="settings-item-left settings-item-body"><span class="settings-item-desc">Condições</span><span class="settings-item-text">${escapeHtml(conditions.join(', ') || 'Não informadas')}</span></span></span>
                <span class="settings-item"><span class="settings-item-left settings-item-body"><span class="settings-item-desc">Medições acompanhadas</span><span class="settings-item-text">${healthIndicators().map(i => H_INDICATORS[i].long).join(', ')}</span></span></span>
                <span class="settings-item"><span class="settings-item-left settings-item-body"><span class="settings-item-desc">Observações do profissional</span><span class="settings-item-text">${escapeHtml(p.doctorNotes || 'Nenhuma')}</span></span></span>
                <span class="settings-item"><span class="settings-item-left settings-item-body"><span class="settings-item-desc">Cuidador pode anotar pelo idoso</span><span class="settings-item-text">${p.caregiverMayRecord ? 'Sim' : 'Não'}</span></span><span class="chevron">${ICONS.chevron}</span></span>
            </button>`)}
        ${section('Orientações do profissional', `
            <p class="form-hint section-intro">Valores e orientações passados pelo médico ou equipe. Quando uma medição atingir o valor, o app mostra a orientação. O app não tem limites próprios.</p>
            <button class="add-btn" data-h-rule-new>${ICONS.plus}Adicionar orientação</button>
            ${rules.length ? rules.map((r, i) => `
                <button class="task-card" data-h-rule="${i}">
                    <span class="task-avatar primary">${ICONS.info}</span>
                    <span class="task-info">
                        <span class="task-title">${escapeHtml(H_RULE_METRICS[r.metric])} ${opLabel[r.op]} ${hNumber(r.threshold, r.threshold % 1 ? 1 : 0)} ${ruleUnit(r.metric)}${r.context ? ' (' + H_GLUCOSE_CONTEXTS[r.context].toLowerCase() + ')' : ''}</span>
                        <span class="task-subtitle">${escapeHtml(r.message)}</span>
                    </span>
                    <span class="chevron">${ICONS.chevron}</span>
                </button>`).join('') : emptyState('', 'Nenhuma orientação cadastrada.')}
        `)}
        ${section('Privacidade (LGPD)', `
            <div class="card">
                <div class="task-subtitle">Consentimento dado em ${p.consentAt ? hDateTime(p.consentAt) : '—'}${p.consentBy ? ' por ' + escapeHtml(p.consentBy) : ''}.</div>
                <p class="form-hint">Os dados ficam só neste celular, fora do backup na nuvem. Cada aparelho tem o seu diário: não há sincronização entre celulares.</p>
            </div>
            <button class="btn btn-danger btn-block" data-h-wipe>Apagar o Diário de Saúde</button>
        `)}
    `;
}

function ruleUnit(metric) {
    return H_RULE_UNITS[metric] || (H_INDICATORS[metric] ? H_INDICATORS[metric].unit : '');
}

function wipeHealthDiary() {
    showConfirm('Apagar o Diário de Saúde', 'Todas as medições, sintomas, confirmações de remédio, lembretes e o perfil de saúde serão apagados deste celular. Remédios, exercícios e dieta continuam. Esta ação não pode ser desfeita.', () => {
        try {
            hcall('wipe');
            Health.profile = null;
            Health.report = null;
            Health.cgTab = 'history';
            showToast('Diário de Saúde apagado', 'success');
            refreshCurrentScreen();
        } catch (e) {
            showToast(e.message, 'error');
        }
    }, 'Apagar diário');
}

// ==================== CONSENTIMENTO ====================
function openConsent() {
    showScreen('screen-health-consent');
}

function renderConsentScreen() {
    const body = document.getElementById('health-consent-body');
    const elderly = escapeHtml(firstName(App.settings.elderlyName));
    const caregiver = escapeHtml(firstName(App.settings.caregiverName));
    body.innerHTML = `
        <div class="consent">
            <div class="health-cta-icon">${H_ICONS.shield}</div>
            <h2 class="consent-title">Antes de começar</h2>
            <ul class="consent-list">
                <li>O Diário guarda medições (como pressão e glicemia), sintomas e confirmações de remédio de <strong>${elderly}</strong>.</li>
                <li>Os dados ficam <strong>só neste celular</strong>. Não vão para a internet nem para o backup na nuvem. Só saem daqui se você compartilhar o relatório.</li>
                <li>Quem usa este celular pode ver os registros na área do idoso. A área do cuidador continua protegida pela senha.</li>
                <li>O app <strong>não faz diagnóstico</strong> e não muda remédios. Dúvidas sobre os valores devem ser levadas ao profissional de saúde.</li>
                <li>Você pode apagar o diário quando quiser em Cuidador › Saúde › Perfil.</li>
            </ul>
            <div class="settings-card">
                <div class="settings-item">
                    <span class="settings-item-left settings-item-body">
                        <span class="settings-item-text">Autorizo ${caregiver} a anotar dados de saúde por mim</span>
                        <span class="settings-item-desc">Pode ser mudado depois no perfil de saúde</span>
                    </span>
                    <button type="button" class="toggle" id="consent-caregiver" role="switch" aria-checked="false" aria-label="Autorizar o cuidador a anotar"></button>
                </div>
            </div>
            <p class="form-hint">Este consentimento deve ser dado pela pessoa idosa ou pelo responsável legal.</p>
            <div class="form-actions">
                <button class="btn btn-primary btn-block" id="consent-accept">Concordo e quero usar</button>
                <button class="btn btn-secondary btn-block" data-consent-later>Agora não</button>
            </div>
        </div>`;
    const toggle = document.getElementById('consent-caregiver');
    toggle.addEventListener('click', () => {
        const on = !toggle.classList.contains('active');
        toggle.classList.toggle('active', on);
        toggle.setAttribute('aria-checked', on);
    });
    body.querySelector('[data-consent-later]').addEventListener('click', goBack);
    document.getElementById('consent-accept').addEventListener('click', () => {
        try {
            Health.profile = hcall('giveConsent', { caregiverMayRecord: toggle.classList.contains('active'), author: healthAuthor() });
            showToast('Diário de Saúde ativado!', 'success');
            goBack();
        } catch (e) {
            showToast(e.message, 'error');
        }
    });
}

// ==================== FORMULÁRIOS ====================
function openHealthForm(title, render) {
    setText('edit-screen-title', title);
    const container = document.getElementById('edit-form-container');
    render(container);
    showScreen('screen-edit');
}

function healthFinish(message) {
    showToast(message, 'success');
    goBack();
}

function fieldError(container, message, field) {
    showToast(message, 'error');
    const el = field && container.querySelector(`[data-field="${field}"]`);
    if (el) {
        el.classList.add('error');
        el.focus();
        setTimeout(() => el.classList.remove('error'), 1200);
    }
}

function dateTimeFields(ms) {
    const d = new Date(ms || Date.now());
    return `
        <div class="range-row two">
            <label class="form-label">Data<input type="date" class="form-input" id="h-date" data-field="measuredAt" value="${dateKey(d)}" max="${dateKey()}"></label>
            <label class="form-label">Horário<input type="time" class="form-input" id="h-time" value="${hTime(d.getTime())}"></label>
        </div>`;
}

function readDateTime() {
    const date = val('h-date');
    const time = val('h-time');
    if (!date || !time) return NaN;
    return new Date(`${date}T${time}`).getTime();
}

function auditInfo(r) {
    if (!r) return '';
    return `<div class="audit-info">Registrado por ${escapeHtml(hAuthorText(r.createdByRole, r.createdByName))} em ${hDateTime(r.createdAt)}${r.updatedAt ? `<br>Editado por ${escapeHtml(hAuthorText(r.updatedByRole, r.updatedByName))} em ${hDateTime(r.updatedAt)}` : ''}</div>`;
}

function readOnlyNotice(readOnly) {
    return readOnly ? tip('', ICONS.info, 'Somente leitura', 'O cuidador precisa da autorização do idoso para editar registros (Saúde › Perfil).') : '';
}

function healthFormActions(record, saveLabel, readOnly) {
    if (readOnly) return '';
    return `
        <div class="form-actions">
            <button type="submit" class="btn btn-primary btn-block">${record ? 'Salvar alterações' : saveLabel}</button>
            ${record ? '<button type="button" class="btn btn-danger btn-block" data-delete>Excluir registro</button>' : ''}
        </div>`;
}

function openMeasurementForm(type, id) {
    const record = id ? Health.cache.measurements[id] : null;
    if (id && !record) return showToast('Registro não encontrado', 'error');
    const ind = H_INDICATORS[type];
    const readOnly = !healthCanRecord();
    openHealthForm(record ? ind.long : `Anotar ${ind.long.toLowerCase()}`, container => {
        let fields;
        if (type === 'bp') {
            fields = `
                <div class="range-row two">
                    <label class="form-label">Máxima (sistólica) *<input type="text" inputmode="numeric" class="form-input big-number" id="h-v1" data-field="value1" maxlength="3" placeholder="Ex: 120" value="${record ? record.value1 : ''}"></label>
                    <label class="form-label">Mínima (diastólica) *<input type="text" inputmode="numeric" class="form-input big-number" id="h-v2" data-field="value2" maxlength="3" placeholder="Ex: 80" value="${record ? record.value2 : ''}"></label>
                </div>
                <p class="form-hint">Os dois números do aparelho, em mmHg.</p>
                <div class="form-group">
                    <label class="form-label" for="h-v3">Pulso (bpm) <span class="optional">opcional</span></label>
                    <input type="text" inputmode="numeric" class="form-input" id="h-v3" data-field="value3" maxlength="3" value="${record && record.value3 ? record.value3 : ''}">
                </div>`;
        } else {
            fields = `
                <div class="form-group">
                    <label class="form-label" for="h-v1">${ind.long} (${ind.unit}) *</label>
                    <input type="text" inputmode="decimal" class="form-input big-number" id="h-v1" data-field="value1" maxlength="6" value="${record ? String(record.value1).replace('.', ',') : ''}">
                </div>
                ${type === 'glucose' ? `
                <div class="form-group">
                    <span class="form-label">Quando foi medida? *</span>
                    <div class="choice-grid" id="h-context" data-field="context" role="radiogroup">
                        ${Object.entries(H_GLUCOSE_CONTEXTS).map(([k, l]) => `<button type="button" role="radio" class="day-chip wide ${record && record.context === k ? 'selected' : ''}" aria-checked="${!!(record && record.context === k)}" data-context="${k}">${l}</button>`).join('')}
                    </div>
                </div>` : ''}`;
        }
        container.innerHTML = `
            ${readOnlyNotice(readOnly)}
            ${auditInfo(record)}
            <form id="form-health" novalidate>
                <fieldset ${readOnly ? 'disabled' : ''}>
                ${fields}
                ${dateTimeFields(record ? record.measuredAt : null)}
                <div class="form-group">
                    <label class="form-label" for="h-note">Observação <span class="optional">opcional</span></label>
                    <textarea class="form-input form-textarea" id="h-note" data-field="note" maxlength="500" placeholder="Ex: medi depois de caminhar">${record ? escapeHtml(record.note || '') : ''}</textarea>
                </div>
                </fieldset>
                ${healthFormActions(record, 'Salvar', readOnly)}
            </form>`;

        container.querySelectorAll('[data-context]').forEach(btn => btn.addEventListener('click', () => {
            container.querySelectorAll('[data-context]').forEach(b => {
                b.classList.toggle('selected', b === btn);
                b.setAttribute('aria-checked', b === btn);
            });
        }));
        bindDelete(container, () => showConfirm('Excluir registro', 'Este registro será apagado do histórico.', () => {
            try {
                hcall('deleteMeasurement', { id, author: healthAuthor() });
                delete Health.cache.measurements[id];
                healthFinish('Registro excluído');
            } catch (e) {
                showToast(e.message, 'error');
            }
        }, 'Excluir'));

        document.getElementById('form-health').addEventListener('submit', e => {
            e.preventDefault();
            const v1 = hParseNumber(val('h-v1'));
            const v2 = type === 'bp' ? hParseNumber(val('h-v2')) : null;
            const v3raw = type === 'bp' ? val('h-v3') : '';
            const v3 = v3raw ? hParseNumber(v3raw) : null;
            const ctxBtn = container.querySelector('[data-context].selected');
            const measuredAt = readDateTime();

            const error = validateMeasurementInput(type, v1, v2, v3, ctxBtn, measuredAt);
            if (error) return fieldError(container, error[1], error[0]);

            const record2 = {
                id: id || 0, type, value1: v1, value2: v2, value3: v3,
                context: ctxBtn ? ctxBtn.dataset.context : null, measuredAt, note: val('h-note')
            };
            try {
                const result = hcall('saveMeasurement', { record: record2, author: healthAuthor() });
                Health.cache.measurements[result.record.id] = result.record;
                afterMeasurementSaved(result, !!id);
            } catch (err) {
                fieldError(container, err.message, err.field);
            }
        });
    });
}

function validateMeasurementInput(type, v1, v2, v3, ctxBtn, measuredAt) {
    const ind = H_INDICATORS[type];
    if (type === 'bp') {
        if (isNaN(v1) || v1 < H_BP.sys[0] || v1 > H_BP.sys[1]) return ['value1', `A pressão máxima deve estar entre ${H_BP.sys[0]} e ${H_BP.sys[1]} mmHg. Confira o número.`];
        if (isNaN(v2) || v2 < H_BP.dia[0] || v2 > H_BP.dia[1]) return ['value2', `A pressão mínima deve estar entre ${H_BP.dia[0]} e ${H_BP.dia[1]} mmHg. Confira o número.`];
        if (v2 >= v1) return ['value2', 'A mínima deve ser menor que a máxima. Confira se os números não foram trocados.'];
        if (v3 !== null && (isNaN(v3) || v3 < H_BP.pulse[0] || v3 > H_BP.pulse[1])) return ['value3', `O pulso deve estar entre ${H_BP.pulse[0]} e ${H_BP.pulse[1]} bpm.`];
    } else {
        if (isNaN(v1) || v1 < ind.min || v1 > ind.max) return ['value1', `O valor deve estar entre ${hNumber(ind.min)} e ${hNumber(ind.max)} ${ind.unit}. Confira o número.`];
        if (type === 'glucose' && !ctxBtn) return ['context', 'Escolha quando a glicemia foi medida.'];
    }
    if (isNaN(measuredAt)) return ['measuredAt', 'Informe a data e o horário.'];
    if (measuredAt > Date.now() + 5 * 60000) return ['measuredAt', 'A data e o horário não podem estar no futuro.'];
    return null;
}

async function afterMeasurementSaved(result, edited) {
    healthFinish(edited ? 'Registro atualizado' : 'Registro salvo');
    if (!result.guidance || !result.guidance.length) return;
    if (App.actingRole !== 'caregiver') {
        addAlert('health', `${hValueText(result.record)}: ${result.guidance.join(' ')}`);
    }
    const answer = await askChoice('Orientação do profissional', `Para este valor, o profissional de saúde orientou: ${result.guidance.join(' • ')}`, [
        { value: 'ok', label: 'Entendi' },
        { value: 'sos', label: 'Preciso de ajuda agora', emoji: '📞', style: 'danger' }
    ]);
    if (answer === 'sos') showScreen('screen-emergency');
}

function openSymptomForm(id, presetCode) {
    const record = id ? Health.cache.symptoms[id] : null;
    if (id && !record) return showToast('Registro não encontrado', 'error');
    const readOnly = !healthCanRecord();
    const selected = record ? record.symptom : presetCode || '';
    openHealthForm(record ? 'Sintoma' : 'Anotar sintoma', container => {
        container.innerHTML = `
            ${readOnlyNotice(readOnly)}
            ${auditInfo(record)}
            <form id="form-health" novalidate>
                <fieldset ${readOnly ? 'disabled' : ''}>
                <div class="form-group">
                    <span class="form-label">O que está sentindo? *</span>
                    <div class="choice-grid" id="h-symptom" data-field="symptom" role="radiogroup">
                        ${H_SYMPTOMS.map(s => `<button type="button" role="radio" class="day-chip wide ${s.urgent ? 'urgent' : ''} ${selected === s.code ? 'selected' : ''}" aria-checked="${selected === s.code}" data-symptom-code="${s.code}">${s.label}</button>`).join('')}
                    </div>
                </div>
                <div id="h-urgent-box"></div>
                <div class="form-group" id="h-custom-group" ${selected === 'other' ? '' : 'hidden'}>
                    <label class="form-label" for="h-custom">Qual sintoma? *</label>
                    <input type="text" class="form-input" id="h-custom" data-field="customLabel" maxlength="80" value="${record ? escapeHtml(record.customLabel || '') : ''}">
                </div>
                <div class="form-group">
                    <span class="form-label">Intensidade *</span>
                    <div class="segmented intensity" id="h-intensity" data-field="intensity" role="radiogroup">
                        ${[1, 2, 3].map(i => `<button type="button" role="radio" class="segmented-option ${record && record.intensity === i ? 'active' : ''}" aria-checked="${!!(record && record.intensity === i)}" data-intensity="${i}">${H_INTENSITY[i]}</button>`).join('')}
                    </div>
                </div>
                ${dateTimeFields(record ? record.occurredAt : null)}
                <div class="form-group">
                    <label class="form-label" for="h-duration">Há quanto tempo? <span class="optional">opcional</span></label>
                    <select class="form-input" id="h-duration">
                        ${H_DURATIONS.map(([v, l]) => `<option value="${v}" ${record && record.durationMinutes === v ? 'selected' : ''}>${l}</option>`).join('')}
                    </select>
                </div>
                <div class="form-group">
                    <label class="form-label" for="h-note">Observação <span class="optional">opcional</span></label>
                    <textarea class="form-input form-textarea" id="h-note" data-field="note" maxlength="500">${record ? escapeHtml(record.note || '') : ''}</textarea>
                </div>
                </fieldset>
                ${healthFormActions(record, 'Salvar sintoma', readOnly)}
            </form>`;

        const updateUrgent = code => {
            const s = H_SYMPTOMS.find(x => x.code === code);
            document.getElementById('h-urgent-box').innerHTML = s && s.urgent ? urgentCareBox() : '';
            const sos = container.querySelector('[data-h-sos]');
            if (sos) sos.addEventListener('click', () => showScreen('screen-emergency'));
            document.getElementById('h-custom-group').hidden = code !== 'other';
        };
        updateUrgent(selected);
        container.querySelectorAll('[data-symptom-code]').forEach(btn => btn.addEventListener('click', () => {
            container.querySelectorAll('[data-symptom-code]').forEach(b => {
                b.classList.toggle('selected', b === btn);
                b.setAttribute('aria-checked', b === btn);
            });
            updateUrgent(btn.dataset.symptomCode);
        }));
        container.querySelectorAll('[data-intensity]').forEach(btn => btn.addEventListener('click', () => {
            container.querySelectorAll('[data-intensity]').forEach(b => {
                b.classList.toggle('active', b === btn);
                b.setAttribute('aria-checked', b === btn);
            });
        }));
        bindDelete(container, () => showConfirm('Excluir registro', 'Este sintoma será apagado do histórico.', () => {
            try {
                hcall('deleteSymptom', { id, author: healthAuthor() });
                delete Health.cache.symptoms[id];
                healthFinish('Registro excluído');
            } catch (e) {
                showToast(e.message, 'error');
            }
        }, 'Excluir'));

        document.getElementById('form-health').addEventListener('submit', e => {
            e.preventDefault();
            const symptomBtn = container.querySelector('[data-symptom-code].selected');
            const intensityBtn = container.querySelector('[data-intensity].active');
            const occurredAt = readDateTime();
            if (!symptomBtn) return fieldError(container, 'Escolha o sintoma.', 'symptom');
            if (symptomBtn.dataset.symptomCode === 'other' && !val('h-custom')) return fieldError(container, 'Descreva o sintoma.', 'customLabel');
            if (!intensityBtn) return fieldError(container, 'Escolha a intensidade.', 'intensity');
            if (isNaN(occurredAt)) return fieldError(container, 'Informe a data e o horário.', 'measuredAt');
            if (occurredAt > Date.now() + 5 * 60000) return fieldError(container, 'A data e o horário não podem estar no futuro.', 'measuredAt');
            const duration = val('h-duration');
            const payload = {
                id: id || 0,
                symptom: symptomBtn.dataset.symptomCode,
                customLabel: val('h-custom'),
                intensity: Number(intensityBtn.dataset.intensity),
                durationMinutes: duration ? Number(duration) : null,
                occurredAt,
                note: val('h-note')
            };
            try {
                const result = hcall('saveSymptom', { record: payload, author: healthAuthor() });
                Health.cache.symptoms[result.record.id] = result.record;
                afterSymptomSaved(result, !!id);
            } catch (err) {
                fieldError(container, err.message, err.field);
            }
        });
    });
}

function urgentCareBox() {
    return `
        <div class="urgent-box" role="alert">
            <strong>Procure atendimento agora</strong>
            <span>Este sintoma pode ser sinal de algo grave. Não espere a próxima consulta: ligue para o SAMU (192) ou vá a um pronto-atendimento.</span>
            <button type="button" class="emergency-btn" data-h-sos>${ICONS.alert}Pedir ajuda agora</button>
        </div>`;
}

async function afterSymptomSaved(result, edited) {
    healthFinish(edited ? 'Registro atualizado' : 'Sintoma anotado');
    if (edited) return;
    const label = hSymptomLabel(result.record);
    if (result.seekCareNow) {
        if (App.actingRole !== 'caregiver') addAlert('symptom', `Relatou: ${label} (${H_INTENSITY[result.record.intensity].toLowerCase()})`);
        const answer = await askChoice('Procure atendimento agora', 'Este sintoma pode ser grave. Não espere a próxima consulta.', [
            { value: 'sos', label: 'Ligar para pedir ajuda', emoji: '📞', style: 'danger' },
            { value: null, label: 'Já estou sendo atendido', style: 'muted' }
        ]);
        if (answer === 'sos') showScreen('screen-emergency');
    } else if (result.record.intensity === 3 && App.actingRole !== 'caregiver') {
        addAlert('symptom', `Relatou: ${label} (forte)`);
    }
}

function openProfileForm() {
    const p = healthProfile(true);
    openHealthForm('Perfil de saúde', container => {
        const conditions = p.conditions || [];
        const indicators = healthIndicators();
        container.innerHTML = `
            <form id="form-health" novalidate>
                <div class="form-group">
                    <span class="form-label">Condições de saúde</span>
                    <div class="choice-grid">
                        ${Object.entries(H_CONDITIONS).map(([k, l]) => `<button type="button" class="day-chip wide ${conditions.includes(k) ? 'selected' : ''}" aria-pressed="${conditions.includes(k)}" data-condition="${k}">${l}</button>`).join('')}
                    </div>
                </div>
                <div class="form-group" id="h-other-condition" ${conditions.includes('other') ? '' : 'hidden'}>
                    <label class="form-label" for="h-condition-other">Qual outra condição?</label>
                    <input type="text" class="form-input" id="h-condition-other" maxlength="80" value="${escapeHtml(p.conditionsOther || '')}">
                </div>
                <div class="form-group">
                    <span class="form-label">O que acompanhar em casa *</span>
                    <div class="choice-grid" data-field="indicators">
                        ${Object.entries(H_INDICATORS).map(([k, ind]) => `<button type="button" class="day-chip wide ${indicators.includes(k) ? 'selected' : ''}" aria-pressed="${indicators.includes(k)}" data-indicator="${k}">${ind.long}</button>`).join('')}
                    </div>
                    <p class="form-hint">Escolha o que o profissional pediu para medir.</p>
                </div>
                <div class="form-group">
                    <label class="form-label" for="h-doctor-notes">Observações do profissional</label>
                    <textarea class="form-input form-textarea" id="h-doctor-notes" data-field="doctorNotes" maxlength="1000" placeholder="Ex: medir a pressão sentado, depois de 5 minutos de descanso">${escapeHtml(p.doctorNotes || '')}</textarea>
                </div>
                <div class="settings-card form-group">
                    <div class="settings-item">
                        <span class="settings-item-left settings-item-body">
                            <span class="settings-item-text">Cuidador pode anotar pelo idoso</span>
                            <span class="settings-item-desc">Somente com a concordância do idoso ou do responsável</span>
                        </span>
                        <button type="button" class="toggle ${p.caregiverMayRecord ? 'active' : ''}" id="h-may-record" role="switch" aria-checked="${!!p.caregiverMayRecord}" aria-label="Cuidador pode anotar pelo idoso"></button>
                    </div>
                </div>
                <div class="form-actions"><button type="submit" class="btn btn-primary btn-block">Salvar perfil</button></div>
            </form>`;

        container.querySelectorAll('[data-condition], [data-indicator]').forEach(btn => btn.addEventListener('click', () => {
            btn.classList.toggle('selected');
            btn.setAttribute('aria-pressed', btn.classList.contains('selected'));
            if (btn.dataset.condition === 'other') document.getElementById('h-other-condition').hidden = !btn.classList.contains('selected');
        }));
        const toggle = document.getElementById('h-may-record');
        toggle.addEventListener('click', () => {
            toggle.classList.toggle('active');
            toggle.setAttribute('aria-checked', toggle.classList.contains('active'));
        });
        document.getElementById('form-health').addEventListener('submit', e => {
            e.preventDefault();
            const profile = {
                conditions: [...container.querySelectorAll('[data-condition].selected')].map(b => b.dataset.condition),
                conditionsOther: val('h-condition-other'),
                indicators: [...container.querySelectorAll('[data-indicator].selected')].map(b => b.dataset.indicator),
                doctorNotes: val('h-doctor-notes'),
                caregiverMayRecord: toggle.classList.contains('active'),
                alertRules: p.alertRules || []
            };
            if (!profile.indicators.length) return fieldError(container, 'Escolha pelo menos uma medição para acompanhar.', 'indicators');
            try {
                Health.profile = hcall('saveProfile', { profile, author: healthAuthor() });
                healthFinish('Perfil de saúde salvo');
            } catch (err) {
                fieldError(container, err.message, err.field);
            }
        });
    });
}

function openRuleForm(index) {
    const p = healthProfile(true);
    const rules = [...(p.alertRules || [])];
    const rule = index !== null ? rules[index] : null;
    openHealthForm(rule ? 'Orientação' : 'Nova orientação', container => {
        container.innerHTML = `
            ${tip('', ICONS.info, 'Somente o que o profissional orientou', 'Cadastre o valor e a orientação exatamente como foram passados pelo médico ou pela equipe de saúde.')}
            <form id="form-health" novalidate>
                <div class="form-group">
                    <label class="form-label" for="h-rule-metric">O que observar *</label>
                    <select class="form-input" id="h-rule-metric">
                        ${Object.entries(H_RULE_METRICS).map(([k, l]) => `<option value="${k}" ${rule && rule.metric === k ? 'selected' : ''}>${l}</option>`).join('')}
                    </select>
                </div>
                <div class="range-row two">
                    <label class="form-label">Quando for *
                        <select class="form-input" id="h-rule-op">
                            <option value="gte" ${!rule || rule.op === 'gte' ? 'selected' : ''}>Igual ou acima de</option>
                            <option value="lte" ${rule && rule.op === 'lte' ? 'selected' : ''}>Igual ou abaixo de</option>
                        </select>
                    </label>
                    <label class="form-label">Valor (<span id="h-rule-unit"></span>) *
                        <input type="text" inputmode="decimal" class="form-input" id="h-rule-value" data-field="threshold" maxlength="6" value="${rule ? String(rule.threshold).replace('.', ',') : ''}">
                    </label>
                </div>
                <div class="form-group" id="h-rule-context-group">
                    <label class="form-label" for="h-rule-context">Momento da glicemia</label>
                    <select class="form-input" id="h-rule-context">
                        <option value="">Qualquer momento</option>
                        ${Object.entries(H_GLUCOSE_CONTEXTS).map(([k, l]) => `<option value="${k}" ${rule && rule.context === k ? 'selected' : ''}>${l}</option>`).join('')}
                    </select>
                </div>
                <div class="form-group">
                    <label class="form-label" for="h-rule-message">Orientação *</label>
                    <textarea class="form-input form-textarea" id="h-rule-message" data-field="message" maxlength="200" placeholder="Ex: medir de novo em 15 minutos e ligar para a UBS">${rule ? escapeHtml(rule.message) : ''}</textarea>
                </div>
                ${formActions(rule ? 1 : 0, 'Salvar orientação', 'Excluir orientação')}
            </form>`;
        const metric = document.getElementById('h-rule-metric');
        const sync = () => {
            setText('h-rule-unit', ruleUnit(metric.value));
            document.getElementById('h-rule-context-group').hidden = metric.value !== 'glucose';
        };
        metric.addEventListener('change', sync);
        sync();

        const save = list => {
            try {
                Health.profile = hcall('saveProfile', { profile: { ...healthProfile(true), alertRules: list }, author: healthAuthor() });
                return true;
            } catch (err) {
                fieldError(container, err.message, err.field);
                return false;
            }
        };
        bindDelete(container, () => showConfirm('Excluir orientação', 'Esta orientação deixará de aparecer.', () => {
            rules.splice(index, 1);
            if (save(rules)) healthFinish('Orientação excluída');
        }, 'Excluir'));
        document.getElementById('form-health').addEventListener('submit', e => {
            e.preventDefault();
            const threshold = hParseNumber(val('h-rule-value'));
            const message = val('h-rule-message');
            if (isNaN(threshold) || threshold <= 0) return fieldError(container, 'Informe o valor indicado pelo profissional.', 'threshold');
            if (!message) return fieldError(container, 'Escreva a orientação do profissional.', 'message');
            const item = {
                metric: metric.value, op: val('h-rule-op'), threshold,
                context: metric.value === 'glucose' ? val('h-rule-context') || null : null, message
            };
            if (rule) rules[index] = item; else rules.push(item);
            if (save(rules)) healthFinish('Orientação salva');
        });
    });
}

function openReminderForm(id) {
    const entry = id ? Health.cache.reminders[id] : null;
    const r = entry ? entry.reminder : null;
    const times = r ? r.times.split(',') : [];
    const mask = r ? r.daysMask : 0;
    openHealthForm(r ? 'Lembrete de medição' : 'Novo lembrete', container => {
        container.innerHTML = `
            ${tip('', ICONS.clock, 'Horários do profissional', 'Use os horários e dias que o profissional de saúde indicou. O app não sugere horários.')}
            <form id="form-health" novalidate>
                <div class="form-group">
                    <label class="form-label" for="h-rem-type">Medição *</label>
                    <select class="form-input" id="h-rem-type">
                        ${Object.entries(H_INDICATORS).map(([k, ind]) => `<option value="${k}" ${r && r.refId === k ? 'selected' : ''}>${ind.long}</option>`).join('')}
                    </select>
                </div>
                <div class="form-group">
                    <span class="form-label">Horários *</span>
                    <div class="time-list" id="h-rem-times" data-field="times"></div>
                    <div class="range-row two">
                        <input type="time" class="form-input" id="h-rem-time-input" aria-label="Novo horário">
                        <button type="button" class="btn btn-secondary" id="h-rem-add-time">${ICONS.plus}Adicionar</button>
                    </div>
                </div>
                <div class="form-group">
                    <span class="form-label">Dias da semana *</span>
                    <div class="day-picker" id="h-rem-days" data-field="daysMask">
                        ${[1, 2, 3, 4, 5, 6, 0].map(d => `<button type="button" class="day-chip ${mask & (1 << d) ? 'selected' : ''}" aria-pressed="${!!(mask & (1 << d))}" data-day="${d}">${WEEKDAYS_SHORT[d]}</button>`).join('')}
                    </div>
                </div>
                <div class="form-group">
                    <label class="form-label" for="h-rem-detail">Observação <span class="optional">opcional</span></label>
                    <input type="text" class="form-input" id="h-rem-detail" maxlength="200" value="${r ? escapeHtml(r.detail || '') : ''}">
                </div>
                ${r ? `
                <div class="settings-card form-group">
                    <div class="settings-item">
                        <span class="settings-item-left settings-item-text">Lembrete ligado</span>
                        <button type="button" class="toggle ${r.enabled ? 'active' : ''}" id="h-rem-enabled" role="switch" aria-checked="${r.enabled}" aria-label="Lembrete ligado"></button>
                    </div>
                </div>` : ''}
                ${formActions(r ? r.id : 0, 'Salvar lembrete', 'Excluir lembrete')}
            </form>`;

        const list = [...times];
        const renderTimes = () => {
            document.getElementById('h-rem-times').innerHTML = list.length
                ? list.sort().map(t => `<span class="time-chip">${t}<button type="button" aria-label="Remover ${t}" data-remove-time="${t}">${H_ICONS.x}</button></span>`).join('')
                : '<span class="form-hint">Nenhum horário ainda.</span>';
            container.querySelectorAll('[data-remove-time]').forEach(b => b.addEventListener('click', () => {
                list.splice(list.indexOf(b.dataset.removeTime), 1);
                renderTimes();
            }));
        };
        renderTimes();
        document.getElementById('h-rem-add-time').addEventListener('click', () => {
            const t = val('h-rem-time-input');
            if (!t) return showToast('Escolha um horário', 'error');
            if (!list.includes(t)) list.push(t);
            document.getElementById('h-rem-time-input').value = '';
            renderTimes();
        });
        container.querySelectorAll('#h-rem-days .day-chip').forEach(chip => chip.addEventListener('click', () => {
            chip.classList.toggle('selected');
            chip.setAttribute('aria-pressed', chip.classList.contains('selected'));
        }));
        const enabled = document.getElementById('h-rem-enabled');
        if (enabled) enabled.addEventListener('click', () => {
            enabled.classList.toggle('active');
            enabled.setAttribute('aria-checked', enabled.classList.contains('active'));
        });
        bindDelete(container, () => showConfirm('Excluir lembrete', 'Os avisos deste lembrete vão parar.', () => {
            hsafe('deleteReminder', { id });
            healthFinish('Lembrete excluído');
        }, 'Excluir'));

        document.getElementById('form-health').addEventListener('submit', e => {
            e.preventDefault();
            const pendingTime = val('h-rem-time-input');
            if (pendingTime && !list.includes(pendingTime)) list.push(pendingTime);
            const daysMask = [...container.querySelectorAll('#h-rem-days .day-chip.selected')]
                .reduce((m, c) => m | (1 << Number(c.dataset.day)), 0);
            if (!list.length) return fieldError(container, 'Adicione pelo menos um horário.', 'times');
            if (!daysMask) return fieldError(container, 'Escolha pelo menos um dia.', 'daysMask');
            const type = val('h-rem-type');
            try {
                hcall('saveReminder', {
                    record: {
                        id: id || 0, kind: 'measurement', refId: r ? r.refId : null, title: H_INDICATORS[type].long,
                        detail: val('h-rem-detail'), times: list.sort().join(','), daysMask,
                        enabled: enabled ? enabled.classList.contains('active') : true
                    },
                    author: healthAuthor()
                });
                Health.status = null;
                healthFinish('Lembrete salvo');
                const st = hsafe('getStatus', {}, null);
                if (st && !st.notificationsAllowed) showToast('Permita as notificações para ver os lembretes.', 'error');
            } catch (err) {
                fieldError(container, err.message, err.field);
            }
        });
    });
}

// ==================== EVENTOS ====================
function bindHealthEvents(root) {
    const on = (selector, handler) => root.querySelectorAll(selector).forEach(el => el.addEventListener('click', () => handler(el)));
    on('[data-h-consent]', openConsent);
    on('[data-h-history]', () => showScreen('screen-health-history'));
    on('[data-h-goto-health]', () => { Health.cgTab = 'history'; setCaregiverTab('health'); });
    on('[data-h-record]', el => {
        if (el.dataset.hRecord === 'symptom') openSymptomForm(null);
        else openMeasurementForm(el.dataset.hRecord, null);
    });
    on('[data-h-edit]', el => {
        const id = Number(el.dataset.id);
        if (el.dataset.hEdit === 's') openSymptomForm(id);
        else {
            const m = Health.cache.measurements[id];
            if (m) openMeasurementForm(m.type, id);
        }
    });
    on('[data-h-ind]', el => { Health.view.indicator = el.dataset.hInd; rerenderHealthArea(); });
    on('[data-h-period]', el => { Health.view.period = el.dataset.hPeriod; rerenderHealthArea(); });
    on('[data-h-report-period]', el => { Health.reportPeriod = el.dataset.hReportPeriod; rerenderHealthArea(); });
    on('[data-h-apply-range]', el => {
        const prefix = el.dataset.hApplyRange;
        const from = val(`${prefix}-from`);
        const to = val(`${prefix}-to`);
        if (!from || !to || from > to) return showToast('Escolha um período válido', 'error');
        if (prefix === 'h-report') Health.reportCustom = { from, to };
        else { Health.view.customFrom = from; Health.view.customTo = to; }
        rerenderHealthArea();
    });
    on('[data-h-cgtab]', el => { Health.cgTab = el.dataset.hCgtab; rerenderHealthArea(true); });
    on('[data-h-event]', el => resolveHealthEvent(Number(el.dataset.hEvent), el.dataset.hStatus));
    on('[data-h-reminder-new]', () => openReminderForm(null));
    on('[data-h-reminder]', el => openReminderForm(Number(el.dataset.hReminder)));
    on('[data-h-perm]', el => {
        const method = { notifications: 'requestNotifications', exact: 'openExactAlarmSettings', battery: 'openBatterySettings' }[el.dataset.hPerm];
        Health.status = null;
        hsafe(method, {});
    });
    on('[data-h-report-generate]', generateHealthReport);
    on('[data-h-profile-edit]', openProfileForm);
    on('[data-h-rule-new]', () => openRuleForm(null));
    on('[data-h-rule]', el => openRuleForm(Number(el.dataset.hRule)));
    on('[data-h-wipe]', wipeHealthDiary);
    on('[data-h-missed]', el => askMissedMedication(Number(el.dataset.hMissed)));
}

function rerenderHealthArea(resetScroll = false) {
    const current = document.querySelector('.screen.active');
    if (!current) return;
    const content = current.querySelector('.screen-content');
    const top = content ? content.scrollTop : 0;
    renderScreen(current.id);
    if (content) content.scrollTop = resetScroll ? 0 : top;
}
