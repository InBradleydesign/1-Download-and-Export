// ==================== ESTADO DA APLICAÇÃO ====================
const App = {
    usuario: null,
    idosoSelecionado: null,
    diaSelecionado: 1,
    fontScale: 1,
    isProcessing: false,
    
    diasSemana: ['', 'Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado', 'Domingo']
};

// ==================== UTILITÁRIOS ====================
function callBridge(method, ...args) {
    if (typeof AndroidBridge === 'undefined') {
        console.warn('AndroidBridge não disponível');
        return { success: false, error: 'Bridge não disponível' };
    }
    try {
        const result = AndroidBridge[method](...args);
        return JSON.parse(result);
    } catch (e) {
        console.error('Erro ao chamar bridge:', e);
        return { success: false, error: 'Erro interno' };
    }
}

function showScreen(screenId) {
    document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
    const screen = document.getElementById(screenId);
    if (screen) {
        screen.classList.add('active');
        const firstFocusable = screen.querySelector('button, input, select, textarea, [tabindex]:not([tabindex="-1"])');
        if (firstFocusable) firstFocusable.focus();
    }
}

function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    toast.setAttribute('role', 'alert');
    container.appendChild(toast);
    
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

function showModal(title, body, onConfirm, confirmText = 'Confirmar') {
    const overlay = document.getElementById('modal-overlay');
    document.getElementById('modal-title').textContent = title;
    document.getElementById('modal-body').innerHTML = body;
    
    const confirmBtn = document.getElementById('modal-confirm');
    confirmBtn.textContent = confirmText;
    confirmBtn.onclick = () => {
        overlay.classList.remove('active');
        if (onConfirm) onConfirm();
    };
    
    document.getElementById('modal-cancel').onclick = () => {
        overlay.classList.remove('active');
    };
    
    overlay.classList.add('active');
}

function preventDoubleClick(btn, callback) {
    if (App.isProcessing) return;
    App.isProcessing = true;
    btn.disabled = true;
    
    callback().finally(() => {
        App.isProcessing = false;
        btn.disabled = false;
    });
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr + 'T00:00:00');
    return date.toLocaleDateString('pt-BR');
}

function formatTime(timeStr) {
    if (!timeStr) return '';
    return timeStr.substring(0, 5);
}

// ==================== FONTE ====================
function updateFontScale(scale) {
    App.fontScale = Math.max(0.8, Math.min(2.0, scale));
    document.documentElement.style.setProperty('--font-scale', App.fontScale);
    
    const displayPercent = Math.round(App.fontScale * 100) + '%';
    const display1 = document.getElementById('font-scale-display');
    const display2 = document.getElementById('font-scale-display-idoso');
    if (display1) display1.textContent = displayPercent;
    if (display2) display2.textContent = displayPercent;
    
    if (App.usuario) {
        callBridge('atualizarTamanhoFonte', App.fontScale);
    }
}

// ==================== INICIALIZAÇÃO ====================
document.addEventListener('DOMContentLoaded', () => {
    initLogin();
    initCuidador();
    initIdoso();
    initFontControls();
    
    const result = callBridge('getUsuarioLogado');
    if (result.success && result.data) {
        App.usuario = result.data;
        App.fontScale = result.data.tamanhoFonte || 1;
        updateFontScale(App.fontScale);
        
        if (result.data.tipoPerfil === 'CUIDADOR') {
            showScreen('screen-cuidador-home');
            loadIdososCuidador();
        } else {
            document.body.classList.add('idoso-view');
            document.getElementById('idoso-nome-header').textContent = result.data.nome.split(' ')[0];
            showScreen('screen-idoso-home');
        }
    }
});

function initFontControls() {
    document.getElementById('btn-font-decrease')?.addEventListener('click', () => updateFontScale(App.fontScale - 0.1));
    document.getElementById('btn-font-increase')?.addEventListener('click', () => updateFontScale(App.fontScale + 0.1));
    document.getElementById('btn-font-decrease-idoso')?.addEventListener('click', () => updateFontScale(App.fontScale - 0.1));
    document.getElementById('btn-font-increase-idoso')?.addEventListener('click', () => updateFontScale(App.fontScale + 0.1));
}

// ==================== LOGIN ====================
function initLogin() {
    const profileBtns = document.querySelectorAll('.profile-btn');
    let selectedProfile = null;
    
    profileBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            profileBtns.forEach(b => {
                b.classList.remove('selected');
                b.setAttribute('aria-pressed', 'false');
            });
            btn.classList.add('selected');
            btn.setAttribute('aria-pressed', 'true');
            selectedProfile = btn.dataset.profile;
            document.getElementById('login-form').style.display = 'block';
        });
    });
    
    document.getElementById('login-form').addEventListener('submit', (e) => {
        e.preventDefault();
        const btn = document.getElementById('btn-login');
        
        preventDoubleClick(btn, async () => {
            const email = document.getElementById('login-email').value;
            const senha = document.getElementById('login-senha').value;
            
            if (!selectedProfile) {
                showToast('Selecione um perfil', 'error');
                return;
            }
            
            const result = callBridge('login', email, senha, selectedProfile);
            
            if (result.success) {
                App.usuario = result.data;
                App.fontScale = result.data.tamanhoFonte || 1;
                updateFontScale(App.fontScale);
                
                if (result.data.tipoPerfil === 'CUIDADOR') {
                    showScreen('screen-cuidador-home');
                    loadIdososCuidador();
                } else {
                    document.body.classList.add('idoso-view');
                    document.getElementById('idoso-nome-header').textContent = result.data.nome.split(' ')[0];
                    showScreen('screen-idoso-home');
                }
            } else {
                showToast(result.error || 'Erro ao fazer login', 'error');
            }
        });
    });
}

function logout() {
    callBridge('logout');
    App.usuario = null;
    App.idosoSelecionado = null;
    document.body.classList.remove('idoso-view');
    document.getElementById('login-form').style.display = 'none';
    document.getElementById('login-email').value = '';
    document.getElementById('login-senha').value = '';
    document.querySelectorAll('.profile-btn').forEach(b => {
        b.classList.remove('selected');
        b.setAttribute('aria-pressed', 'false');
    });
    showScreen('screen-login');
}

// ==================== CUIDADOR ====================
function initCuidador() {
    document.getElementById('btn-logout-cuidador')?.addEventListener('click', logout);
    document.getElementById('btn-cadastrar-idoso')?.addEventListener('click', () => showScreen('screen-cadastrar-idoso'));
    document.getElementById('btn-back-cadastrar-idoso')?.addEventListener('click', () => showScreen('screen-cuidador-home'));
    document.getElementById('btn-back-gestao')?.addEventListener('click', () => showScreen('screen-cuidador-home'));
    
    initFormCadastrarIdoso();
    initGestaoIdoso();
}

function loadIdososCuidador() {
    const result = callBridge('listarIdosos');
    const container = document.getElementById('lista-idosos-cuidador');
    
    if (!result.success || !result.data || result.data.length === 0) {
        container.innerHTML = '<div class="empty-state"><p>Nenhum idoso cadastrado ainda.</p></div>';
        return;
    }
    
    container.innerHTML = result.data.map(idoso => `
        <button type="button" class="idoso-select-card" data-id="${idoso.id}" aria-label="Selecionar ${escapeHtml(idoso.nome)}">
            <div class="idoso-avatar">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            </div>
            <div class="idoso-info">
                <div class="idoso-nome">${escapeHtml(idoso.nome)}</div>
                <div class="idoso-email">${escapeHtml(idoso.email)}</div>
            </div>
        </button>
    `).join('');
    
    container.querySelectorAll('.idoso-select-card').forEach(card => {
        card.addEventListener('click', () => {
            App.idosoSelecionado = result.data.find(i => i.id == card.dataset.id);
            document.getElementById('gestao-titulo').textContent = App.idosoSelecionado.nome;
            showScreen('screen-gestao-idoso');
        });
    });
}

function initFormCadastrarIdoso() {
    document.getElementById('form-cadastrar-idoso').addEventListener('submit', (e) => {
        e.preventDefault();
        const btn = e.target.querySelector('button[type="submit"]');
        
        preventDoubleClick(btn, async () => {
            const nome = document.getElementById('idoso-nome').value;
            const email = document.getElementById('idoso-email').value;
            const senha = document.getElementById('idoso-senha').value;
            const telefone = document.getElementById('idoso-telefone').value;
            const obs = document.getElementById('idoso-obs').value;
            
            const result = callBridge('cadastrarIdoso', nome, email, senha, telefone, obs);
            
            if (result.success) {
                showToast('Idoso cadastrado com sucesso!', 'success');
                e.target.reset();
                showScreen('screen-cuidador-home');
                loadIdososCuidador();
            } else {
                showToast(result.error || 'Erro ao cadastrar', 'error');
            }
        });
    });
}

function initGestaoIdoso() {
    document.getElementById('btn-progresso')?.addEventListener('click', () => {
        showScreen('screen-progresso');
        loadProgresso('mes');
    });
    document.getElementById('btn-back-progresso')?.addEventListener('click', () => showScreen('screen-gestao-idoso'));
    document.getElementById('btn-progresso-semana')?.addEventListener('click', () => loadProgresso('semana'));
    document.getElementById('btn-progresso-mes')?.addEventListener('click', () => loadProgresso('mes'));
    
    document.getElementById('btn-dieta-cuidador')?.addEventListener('click', () => {
        showScreen('screen-dieta-cuidador');
        initDiasTabs('dias-tabs-cuidador', loadDietaCuidador);
    });
    document.getElementById('btn-back-dieta-cuidador')?.addEventListener('click', () => showScreen('screen-gestao-idoso'));
    initFormDieta();
    
    document.getElementById('btn-medicamentos-cuidador')?.addEventListener('click', () => {
        showScreen('screen-medicamentos-cuidador');
        loadMedicamentosCuidador();
    });
    document.getElementById('btn-back-med-cuidador')?.addEventListener('click', () => showScreen('screen-gestao-idoso'));
    document.getElementById('btn-add-medicamento')?.addEventListener('click', () => {
        document.getElementById('form-med-titulo').textContent = 'Novo Medicamento';
        document.getElementById('form-medicamento').reset();
        document.getElementById('med-id').value = '';
        showScreen('screen-form-medicamento');
    });
    document.getElementById('btn-back-form-med')?.addEventListener('click', () => showScreen('screen-medicamentos-cuidador'));
    initFormMedicamento();
    
    document.getElementById('btn-exercicios-cuidador')?.addEventListener('click', () => {
        showScreen('screen-exercicios-cuidador');
        loadExerciciosCuidador();
    });
    document.getElementById('btn-back-ex-cuidador')?.addEventListener('click', () => showScreen('screen-gestao-idoso'));
    document.getElementById('btn-add-exercicio')?.addEventListener('click', () => {
        document.getElementById('form-ex-titulo').textContent = 'Novo Exercício';
        document.getElementById('form-exercicio').reset();
        document.getElementById('ex-id').value = '';
        showScreen('screen-form-exercicio');
    });
    document.getElementById('btn-back-form-ex')?.addEventListener('click', () => showScreen('screen-exercicios-cuidador'));
    initFormExercicio();
    
    document.getElementById('btn-visitas-cuidador')?.addEventListener('click', () => {
        showScreen('screen-visitas-cuidador');
        loadVisitasCuidador();
    });
    document.getElementById('btn-back-visitas-cuidador')?.addEventListener('click', () => showScreen('screen-gestao-idoso'));
    document.getElementById('btn-add-visita')?.addEventListener('click', () => {
        document.getElementById('form-visita-titulo').textContent = 'Nova Visita';
        document.getElementById('form-visita').reset();
        document.getElementById('visita-id').value = '';
        showScreen('screen-form-visita');
    });
    document.getElementById('btn-back-form-visita')?.addEventListener('click', () => showScreen('screen-visitas-cuidador'));
    initFormVisita();
    
    document.getElementById('btn-editar-idoso')?.addEventListener('click', () => {
        showScreen('screen-editar-idoso');
        loadEditarIdoso();
    });
    document.getElementById('btn-back-editar-idoso')?.addEventListener('click', () => showScreen('screen-gestao-idoso'));
    initFormEditarIdoso();
}

function loadProgresso(periodo) {
    if (!App.idosoSelecionado) return;
    
    document.getElementById('btn-progresso-semana').classList.toggle('btn-primary', periodo === 'semana');
    document.getElementById('btn-progresso-semana').classList.toggle('btn-secondary', periodo !== 'semana');
    document.getElementById('btn-progresso-mes').classList.toggle('btn-primary', periodo === 'mes');
    document.getElementById('btn-progresso-mes').classList.toggle('btn-secondary', periodo !== 'mes');
    
    const result = periodo === 'semana' 
        ? callBridge('getProgressoSemana', App.idosoSelecionado.id)
        : callBridge('getProgressoMes', App.idosoSelecionado.id);
    
    const container = document.getElementById('progresso-container');
    
    if (!result.success) {
        container.innerHTML = '<p>Erro ao carregar progresso.</p>';
        return;
    }
    
    const data = result.data;
    container.innerHTML = `
        <p style="margin-bottom: 16px; color: var(--color-text-secondary);">
            Período: ${formatDate(data.dataInicio)} a ${formatDate(data.dataFim)}
        </p>
        
        <div class="progress-container">
            <div class="progress-label">
                <span>Refeições Registradas</span>
                <span>${data.refeicoesRegistradas}/${data.refeicoesEsperadas}</span>
            </div>
            <div class="progress-bar">
                <div class="progress-fill ${data.percentualRefeicoes >= 70 ? 'success' : ''}" style="width: ${data.percentualRefeicoes}%"></div>
            </div>
        </div>
        
        <div class="progress-container">
            <div class="progress-label">
                <span>Medicamentos Confirmados</span>
                <span>${data.medicamentosConfirmados}/${data.medicamentosEsperados}</span>
            </div>
            <div class="progress-bar">
                <div class="progress-fill ${data.percentualMedicamentos >= 70 ? 'success' : ''}" style="width: ${data.percentualMedicamentos}%"></div>
            </div>
        </div>
        
        <div class="progress-container">
            <div class="progress-label">
                <span>Exercícios Concluídos</span>
                <span>${data.exerciciosConcluidos}/${data.exerciciosEsperados}</span>
            </div>
            <div class="progress-bar">
                <div class="progress-fill ${data.percentualExercicios >= 70 ? 'success' : ''}" style="width: ${data.percentualExercicios}%"></div>
            </div>
        </div>
        
        <div class="progress-container">
            <div class="progress-label">
                <span>Check-ins Diários</span>
                <span>${data.checkInsRealizados}/${data.checkInsEsperados}</span>
            </div>
            <div class="progress-bar">
                <div class="progress-fill ${data.percentualCheckIns >= 70 ? 'success' : ''}" style="width: ${data.percentualCheckIns}%"></div>
            </div>
        </div>
    `;
}

function initDiasTabs(containerId, onSelect) {
    const container = document.getElementById(containerId);
    const hoje = new Date().getDay() || 7;
    App.diaSelecionado = hoje;
    
    container.innerHTML = App.diasSemana.slice(1).map((dia, i) => `
        <button type="button" class="dia-tab ${i + 1 === hoje ? 'active' : ''}" data-dia="${i + 1}" role="tab" aria-selected="${i + 1 === hoje}">
            ${dia}
        </button>
    `).join('');
    
    container.querySelectorAll('.dia-tab').forEach(tab => {
        tab.addEventListener('click', () => {
            container.querySelectorAll('.dia-tab').forEach(t => {
                t.classList.remove('active');
                t.setAttribute('aria-selected', 'false');
            });
            tab.classList.add('active');
            tab.setAttribute('aria-selected', 'true');
            App.diaSelecionado = parseInt(tab.dataset.dia);
            onSelect();
        });
    });
    
    onSelect();
}

function loadDietaCuidador() {
    if (!App.idosoSelecionado) return;
    
    const result = callBridge('getDietaSemanal', App.idosoSelecionado.id);
    const dieta = result.success && result.data ? result.data.find(d => d.diaSemana === App.diaSelecionado) : null;
    
    document.getElementById('dieta-cafe').value = dieta?.cafeManha || '';
    document.getElementById('dieta-lanche-m').value = dieta?.lancheManha || '';
    document.getElementById('dieta-almoco').value = dieta?.almoco || '';
    document.getElementById('dieta-lanche-t').value = dieta?.lancheTarde || '';
    document.getElementById('dieta-jantar').value = dieta?.jantar || '';
    document.getElementById('dieta-ceia').value = dieta?.ceia || '';
    document.getElementById('dieta-obs').value = dieta?.observacoes || '';
}

function initFormDieta() {
    document.getElementById('form-dieta').addEventListener('submit', (e) => {
        e.preventDefault();
        const btn = e.target.querySelector('button[type="submit"]');
        
        preventDoubleClick(btn, async () => {
            const result = callBridge('salvarDieta', 
                App.idosoSelecionado.id,
                App.diaSelecionado,
                document.getElementById('dieta-cafe').value,
                document.getElementById('dieta-lanche-m').value,
                document.getElementById('dieta-almoco').value,
                document.getElementById('dieta-lanche-t').value,
                document.getElementById('dieta-jantar').value,
                document.getElementById('dieta-ceia').value,
                document.getElementById('dieta-obs').value
            );
            
            if (result.success) {
                showToast('Dieta salva com sucesso!', 'success');
            } else {
                showToast(result.error || 'Erro ao salvar', 'error');
            }
        });
    });
}

function loadMedicamentosCuidador() {
    if (!App.idosoSelecionado) return;
    
    const result = callBridge('getMedicamentos', App.idosoSelecionado.id);
    const container = document.getElementById('lista-medicamentos-cuidador');
    
    if (!result.success || !result.data || result.data.length === 0) {
        container.innerHTML = '<div class="empty-state"><p>Nenhum medicamento cadastrado.</p></div>';
        return;
    }
    
    container.innerHTML = result.data.map(med => `
        <div class="card" style="margin-bottom: 8px;">
            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                <div>
                    <strong>${escapeHtml(med.nome)}</strong>
                    <p style="color: var(--color-text-secondary); margin: 4px 0;">
                        ${escapeHtml(med.dosagem)} ${med.horario ? '• ' + formatTime(med.horario) : ''}
                    </p>
                    ${med.instrucoes ? `<p style="font-size: var(--font-small);">${escapeHtml(med.instrucoes)}</p>` : ''}
                </div>
                <div style="display: flex; gap: 8px;">
                    <button type="button" class="btn btn-secondary" onclick="editarMedicamento(${med.id})" aria-label="Editar ${escapeHtml(med.nome)}">Editar</button>
                    <button type="button" class="btn btn-danger" onclick="excluirMedicamento(${med.id})" aria-label="Excluir ${escapeHtml(med.nome)}">Excluir</button>
                </div>
            </div>
        </div>
    `).join('');
}

function editarMedicamento(id) {
    const result = callBridge('getMedicamentos', App.idosoSelecionado.id);
    const med = result.data?.find(m => m.id === id);
    if (!med) return;
    
    document.getElementById('form-med-titulo').textContent = 'Editar Medicamento';
    document.getElementById('med-id').value = med.id;
    document.getElementById('med-nome').value = med.nome;
    document.getElementById('med-dosagem').value = med.dosagem;
    document.getElementById('med-horario').value = med.horario || '';
    document.getElementById('med-frequencia').value = med.frequencia || 'diario';
    document.getElementById('med-instrucoes').value = med.instrucoes || '';
    document.getElementById('med-data-fim').value = med.dataFim || '';
    
    showScreen('screen-form-medicamento');
}

function excluirMedicamento(id) {
    showModal('Excluir Medicamento', 'Tem certeza que deseja excluir este medicamento?', () => {
        const result = callBridge('excluirMedicamento', id);
        if (result.success) {
            showToast('Medicamento excluído!', 'success');
            loadMedicamentosCuidador();
        } else {
            showToast(result.error || 'Erro ao excluir', 'error');
        }
    }, 'Excluir');
}

function initFormMedicamento() {
    document.getElementById('form-medicamento').addEventListener('submit', (e) => {
        e.preventDefault();
        const btn = e.target.querySelector('button[type="submit"]');
        
        preventDoubleClick(btn, async () => {
            const id = document.getElementById('med-id').value;
            const nome = document.getElementById('med-nome').value;
            const dosagem = document.getElementById('med-dosagem').value;
            const horario = document.getElementById('med-horario').value;
            const frequencia = document.getElementById('med-frequencia').value;
            const instrucoes = document.getElementById('med-instrucoes').value;
            const dataFim = document.getElementById('med-data-fim').value;
            
            let result;
            if (id) {
                result = callBridge('atualizarMedicamento', parseInt(id), nome, dosagem, horario, frequencia, instrucoes, dataFim, true);
            } else {
                result = callBridge('salvarMedicamento', App.idosoSelecionado.id, nome, dosagem, horario, frequencia, instrucoes, dataFim);
            }
            
            if (result.success) {
                showToast('Medicamento salvo!', 'success');
                showScreen('screen-medicamentos-cuidador');
                loadMedicamentosCuidador();
            } else {
                showToast(result.error || 'Erro ao salvar', 'error');
            }
        });
    });
}

function loadExerciciosCuidador() {
    if (!App.idosoSelecionado) return;
    
    const result = callBridge('getExercicios', App.idosoSelecionado.id);
    const container = document.getElementById('lista-exercicios-cuidador');
    
    if (!result.success || !result.data || result.data.length === 0) {
        container.innerHTML = '<div class="empty-state"><p>Nenhum exercício cadastrado.</p></div>';
        return;
    }
    
    container.innerHTML = result.data.map(ex => `
        <div class="card" style="margin-bottom: 8px;">
            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                <div>
                    <strong>${escapeHtml(ex.nome)}</strong>
                    <p style="color: var(--color-text-secondary); margin: 4px 0;">
                        ${ex.duracaoMinutos} min • ${ex.frequenciaSemanal}x por semana
                    </p>
                    ${ex.descricao ? `<p style="font-size: var(--font-small);">${escapeHtml(ex.descricao)}</p>` : ''}
                </div>
                <div style="display: flex; gap: 8px;">
                    <button type="button" class="btn btn-secondary" onclick="editarExercicio(${ex.id})" aria-label="Editar ${escapeHtml(ex.nome)}">Editar</button>
                    <button type="button" class="btn btn-danger" onclick="excluirExercicio(${ex.id})" aria-label="Excluir ${escapeHtml(ex.nome)}">Excluir</button>
                </div>
            </div>
        </div>
    `).join('');
}

function editarExercicio(id) {
    const result = callBridge('getExercicios', App.idosoSelecionado.id);
    const ex = result.data?.find(e => e.id === id);
    if (!ex) return;
    
    document.getElementById('form-ex-titulo').textContent = 'Editar Exercício';
    document.getElementById('ex-id').value = ex.id;
    document.getElementById('ex-nome').value = ex.nome;
    document.getElementById('ex-descricao').value = ex.descricao || '';
    document.getElementById('ex-duracao').value = ex.duracaoMinutos;
    document.getElementById('ex-frequencia').value = ex.frequenciaSemanal;
    document.getElementById('ex-instrucoes').value = ex.instrucoes || '';
    
    showScreen('screen-form-exercicio');
}

function excluirExercicio(id) {
    showModal('Excluir Exercício', 'Tem certeza que deseja excluir este exercício?', () => {
        const result = callBridge('excluirExercicio', id);
        if (result.success) {
            showToast('Exercício excluído!', 'success');
            loadExerciciosCuidador();
        } else {
            showToast(result.error || 'Erro ao excluir', 'error');
        }
    }, 'Excluir');
}

function initFormExercicio() {
    document.getElementById('form-exercicio').addEventListener('submit', (e) => {
        e.preventDefault();
        const btn = e.target.querySelector('button[type="submit"]');
        
        preventDoubleClick(btn, async () => {
            const id = document.getElementById('ex-id').value;
            const nome = document.getElementById('ex-nome').value;
            const descricao = document.getElementById('ex-descricao').value;
            const duracao = parseInt(document.getElementById('ex-duracao').value);
            const frequencia = parseInt(document.getElementById('ex-frequencia').value);
            const instrucoes = document.getElementById('ex-instrucoes').value;
            
            let result;
            if (id) {
                result = callBridge('atualizarExercicio', parseInt(id), nome, descricao, duracao, frequencia, '', instrucoes, true);
            } else {
                result = callBridge('salvarExercicio', App.idosoSelecionado.id, nome, descricao, duracao, frequencia, '', instrucoes);
            }
            
            if (result.success) {
                showToast('Exercício salvo!', 'success');
                showScreen('screen-exercicios-cuidador');
                loadExerciciosCuidador();
            } else {
                showToast(result.error || 'Erro ao salvar', 'error');
            }
        });
    });
}

function loadVisitasCuidador() {
    if (!App.idosoSelecionado) return;
    
    const result = callBridge('getVisitas', App.idosoSelecionado.id);
    const container = document.getElementById('lista-visitas-cuidador');
    
    if (!result.success || !result.data || result.data.length === 0) {
        container.innerHTML = '<div class="empty-state"><p>Nenhuma visita agendada.</p></div>';
        return;
    }
    
    container.innerHTML = result.data.map(v => `
        <div class="card" style="margin-bottom: 8px;">
            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                <div>
                    <strong>${formatDate(v.data)}</strong>
                    <span class="badge badge-${getStatusClass(v.status)}">${getStatusText(v.status)}</span>
                    <p style="color: var(--color-text-secondary); margin: 4px 0;">
                        ${v.horaInicio ? formatTime(v.horaInicio) : ''} ${v.horaFim ? '- ' + formatTime(v.horaFim) : ''}
                    </p>
                    ${v.descricao ? `<p>${escapeHtml(v.descricao)}</p>` : ''}
                </div>
                <div style="display: flex; gap: 8px;">
                    <button type="button" class="btn btn-danger" onclick="excluirVisita(${v.id})" aria-label="Excluir visita">Excluir</button>
                </div>
            </div>
        </div>
    `).join('');
}

function getStatusClass(status) {
    switch(status) {
        case 'AGENDADA': return 'info';
        case 'CONFIRMADA': return 'success';
        case 'REALIZADA': return 'success';
        case 'CANCELADA': return 'error';
        default: return 'info';
    }
}

function getStatusText(status) {
    switch(status) {
        case 'AGENDADA': return 'Agendada';
        case 'CONFIRMADA': return 'Confirmada';
        case 'REALIZADA': return 'Realizada';
        case 'CANCELADA': return 'Cancelada';
        default: return status;
    }
}

function excluirVisita(id) {
    showModal('Excluir Visita', 'Tem certeza que deseja excluir esta visita?', () => {
        const result = callBridge('excluirVisita', id);
        if (result.success) {
            showToast('Visita excluída!', 'success');
            loadVisitasCuidador();
        } else {
            showToast(result.error || 'Erro ao excluir', 'error');
        }
    }, 'Excluir');
}

function initFormVisita() {
    document.getElementById('form-visita').addEventListener('submit', (e) => {
        e.preventDefault();
        const btn = e.target.querySelector('button[type="submit"]');
        
        preventDoubleClick(btn, async () => {
            const data = document.getElementById('visita-data').value;
            const horaInicio = document.getElementById('visita-hora-inicio').value;
            const horaFim = document.getElementById('visita-hora-fim').value;
            const descricao = document.getElementById('visita-descricao').value;
            
            const result = callBridge('agendarVisita', App.idosoSelecionado.id, data, horaInicio, horaFim, descricao);
            
            if (result.success) {
                showToast('Visita agendada!', 'success');
                showScreen('screen-visitas-cuidador');
                loadVisitasCuidador();
            } else {
                showToast(result.error || 'Erro ao agendar', 'error');
            }
        });
    });
}

function loadEditarIdoso() {
    if (!App.idosoSelecionado) return;
    
    document.getElementById('edit-idoso-nome').value = App.idosoSelecionado.nome;
    document.getElementById('edit-idoso-telefone').value = App.idosoSelecionado.telefoneEmergencia || '';
    document.getElementById('edit-idoso-obs').value = App.idosoSelecionado.observacoes || '';
}

function initFormEditarIdoso() {
    document.getElementById('form-editar-idoso').addEventListener('submit', (e) => {
        e.preventDefault();
        const btn = e.target.querySelector('button[type="submit"]');
        
        preventDoubleClick(btn, async () => {
            const nome = document.getElementById('edit-idoso-nome').value;
            const telefone = document.getElementById('edit-idoso-telefone').value;
            const obs = document.getElementById('edit-idoso-obs').value;
            
            const result = callBridge('atualizarIdoso', App.idosoSelecionado.id, nome, telefone, obs);
            
            if (result.success) {
                showToast('Dados atualizados!', 'success');
                App.idosoSelecionado.nome = nome;
                App.idosoSelecionado.telefoneEmergencia = telefone;
                App.idosoSelecionado.observacoes = obs;
                document.getElementById('gestao-titulo').textContent = nome;
                showScreen('screen-gestao-idoso');
            } else {
                showToast(result.error || 'Erro ao atualizar', 'error');
            }
        });
    });
    
    document.getElementById('btn-excluir-idoso')?.addEventListener('click', () => {
        showModal('Excluir Idoso', 
            `<p><strong>ATENÇÃO:</strong> Esta ação não pode ser desfeita.</p>
             <p>Todos os dados de ${escapeHtml(App.idosoSelecionado.nome)} serão excluídos permanentemente.</p>`,
            () => {
                const result = callBridge('excluirIdoso', App.idosoSelecionado.id);
                if (result.success) {
                    showToast('Idoso excluído!', 'success');
                    App.idosoSelecionado = null;
                    showScreen('screen-cuidador-home');
                    loadIdososCuidador();
                } else {
                    showToast(result.error || 'Erro ao excluir', 'error');
                }
            }, 'Excluir');
    });
}

// ==================== IDOSO ====================
function initIdoso() {
    document.getElementById('btn-logout-idoso')?.addEventListener('click', logout);
    
    document.getElementById('btn-dieta-idoso')?.addEventListener('click', () => {
        showScreen('screen-dieta-idoso');
        initDiasTabs('dias-tabs-idoso', loadDietaIdoso);
    });
    document.getElementById('btn-back-dieta-idoso')?.addEventListener('click', () => showScreen('screen-idoso-home'));
    
    document.getElementById('btn-refeicao-idoso')?.addEventListener('click', () => {
        showScreen('screen-refeicao-idoso');
        loadRegistrosHoje();
    });
    document.getElementById('btn-back-refeicao-idoso')?.addEventListener('click', () => showScreen('screen-idoso-home'));
    initFormRefeicao();
    
    document.getElementById('btn-estado-idoso')?.addEventListener('click', () => {
        showScreen('screen-estado-idoso');
        initEstadoForm();
    });
    document.getElementById('btn-back-estado-idoso')?.addEventListener('click', () => showScreen('screen-idoso-home'));
    
    document.getElementById('btn-medicamentos-idoso')?.addEventListener('click', () => {
        showScreen('screen-medicamentos-idoso');
        loadMedicamentosIdoso();
    });
    document.getElementById('btn-back-med-idoso')?.addEventListener('click', () => showScreen('screen-idoso-home'));
    
    document.getElementById('btn-exercicios-idoso')?.addEventListener('click', () => {
        showScreen('screen-exercicios-idoso');
        loadExerciciosIdoso();
    });
    document.getElementById('btn-back-ex-idoso')?.addEventListener('click', () => showScreen('screen-idoso-home'));
    
    document.getElementById('btn-visitas-idoso')?.addEventListener('click', () => {
        showScreen('screen-visitas-idoso');
        loadVisitasIdoso();
    });
    document.getElementById('btn-back-visitas-idoso')?.addEventListener('click', () => showScreen('screen-idoso-home'));
    
    document.getElementById('btn-emergencia')?.addEventListener('click', () => {
        showScreen('screen-emergencia');
    });
    document.getElementById('btn-back-emergencia')?.addEventListener('click', () => showScreen('screen-idoso-home'));
    initEmergencia();
}

function loadDietaIdoso() {
    if (!App.usuario) return;
    
    const result = callBridge('getDietaSemanal', App.usuario.id);
    const container = document.getElementById('dieta-conteudo-idoso');
    
    const dieta = result.success && result.data ? result.data.find(d => d.diaSemana === App.diaSelecionado) : null;
    
    if (!dieta) {
        container.innerHTML = '<div class="empty-state"><p>Nenhuma dieta definida para este dia.</p></div>';
        return;
    }
    
    const refeicoes = [
        { nome: 'Café da Manhã', valor: dieta.cafeManha },
        { nome: 'Lanche da Manhã', valor: dieta.lancheManha },
        { nome: 'Almoço', valor: dieta.almoco },
        { nome: 'Lanche da Tarde', valor: dieta.lancheTarde },
        { nome: 'Jantar', valor: dieta.jantar },
        { nome: 'Ceia', valor: dieta.ceia }
    ].filter(r => r.valor);
    
    if (refeicoes.length === 0) {
        container.innerHTML = '<div class="empty-state"><p>Nenhuma refeição definida para este dia.</p></div>';
        return;
    }
    
    container.innerHTML = refeicoes.map(r => `
        <div class="refeicao-item">
            <div class="refeicao-titulo">${r.nome}</div>
            <div class="refeicao-descricao">${escapeHtml(r.valor)}</div>
        </div>
    `).join('') + (dieta.observacoes ? `
        <div class="refeicao-item" style="background-color: #FFF3E0;">
            <div class="refeicao-titulo">Observações</div>
            <div class="refeicao-descricao">${escapeHtml(dieta.observacoes)}</div>
        </div>
    ` : '');
}

function loadRegistrosHoje() {
    if (!App.usuario) return;
    
    const result = callBridge('getRegistrosRefeicaoHoje', App.usuario.id);
    const container = document.getElementById('registros-hoje');
    
    if (!result.success || !result.data || result.data.length === 0) {
        container.innerHTML = '<p style="color: var(--color-text-secondary);">Nenhum registro hoje.</p>';
        return;
    }
    
    const tiposRefeicao = {
        'cafe_manha': 'Café da Manhã',
        'lanche_manha': 'Lanche da Manhã',
        'almoco': 'Almoço',
        'lanche_tarde': 'Lanche da Tarde',
        'jantar': 'Jantar',
        'ceia': 'Ceia'
    };
    
    container.innerHTML = result.data.map(r => `
        <div class="refeicao-item">
            <div class="refeicao-titulo">${tiposRefeicao[r.tipoRefeicao] || 'Refeição'} - ${formatTime(r.hora)}</div>
            <div class="refeicao-descricao">${escapeHtml(r.descricao)}</div>
        </div>
    `).join('');
}

function initFormRefeicao() {
    document.getElementById('form-refeicao').addEventListener('submit', (e) => {
        e.preventDefault();
        const btn = e.target.querySelector('button[type="submit"]');
        
        preventDoubleClick(btn, async () => {
            const tipo = document.getElementById('refeicao-tipo').value;
            const descricao = document.getElementById('refeicao-descricao').value;
            const obs = document.getElementById('refeicao-obs').value;
            
            const result = callBridge('registrarRefeicao', App.usuario.id, tipo, descricao, obs);
            
            if (result.success) {
                showToast('Refeição registrada!', 'success');
                e.target.reset();
                loadRegistrosHoje();
            } else {
                showToast(result.error || 'Erro ao registrar', 'error');
            }
        });
    });
}

function initEstadoForm() {
    let estadoHumor = null;
    let estadoFisico = null;
    
    document.querySelectorAll('#estado-humor-grid .estado-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('#estado-humor-grid .estado-btn').forEach(b => {
                b.classList.remove('selected');
                b.setAttribute('aria-pressed', 'false');
            });
            btn.classList.add('selected');
            btn.setAttribute('aria-pressed', 'true');
            estadoHumor = btn.dataset.valor;
        });
    });
    
    document.querySelectorAll('#estado-fisico-grid .estado-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('#estado-fisico-grid .estado-btn').forEach(b => {
                b.classList.remove('selected');
                b.setAttribute('aria-pressed', 'false');
            });
            btn.classList.add('selected');
            btn.setAttribute('aria-pressed', 'true');
            estadoFisico = btn.dataset.valor;
        });
    });
    
    document.getElementById('form-estado').addEventListener('submit', (e) => {
        e.preventDefault();
        
        if (!estadoHumor) {
            showToast('Selecione como está seu humor', 'error');
            return;
        }
        
        if (!estadoFisico) {
            showToast('Selecione como está fisicamente', 'error');
            return;
        }
        
        const btn = e.target.querySelector('button[type="submit"]');
        
        preventDoubleClick(btn, async () => {
            const obs = document.getElementById('estado-obs').value;
            const result = callBridge('registrarEstado', App.usuario.id, estadoHumor, estadoFisico, obs);
            
            if (result.success) {
                showToast('Estado registrado!', 'success');
                showScreen('screen-idoso-home');
            } else {
                showToast(result.error || 'Erro ao registrar', 'error');
            }
        });
    });
}

function loadMedicamentosIdoso() {
    if (!App.usuario) return;
    
    const result = callBridge('getMedicamentos', App.usuario.id);
    const container = document.getElementById('lista-medicamentos-idoso');
    
    if (!result.success || !result.data || result.data.length === 0) {
        container.innerHTML = '<div class="empty-state"><p>Nenhum medicamento cadastrado.</p></div>';
        return;
    }
    
    container.innerHTML = result.data.map(med => {
        const confirmadoResult = callBridge('isMedicamentoConfirmadoHoje', App.usuario.id, med.id);
        const confirmado = confirmadoResult.success && confirmadoResult.data;
        
        return `
            <div class="check-item ${confirmado ? 'confirmed' : ''}">
                <button type="button" class="check-btn ${confirmado ? 'checked' : ''}" 
                        data-id="${med.id}" 
                        ${confirmado ? 'disabled' : ''}
                        aria-label="${confirmado ? 'Já tomado' : 'Marcar como tomado'} ${escapeHtml(med.nome)}">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20,6 9,17 4,12"/></svg>
                </button>
                <div class="check-content">
                    <div class="check-title">${escapeHtml(med.nome)}</div>
                    <div class="check-subtitle">
                        ${escapeHtml(med.dosagem)} ${med.horario ? '• ' + formatTime(med.horario) : ''}
                    </div>
                    ${med.instrucoes ? `<div class="check-subtitle">${escapeHtml(med.instrucoes)}</div>` : ''}
                </div>
            </div>
        `;
    }).join('');
    
    container.querySelectorAll('.check-btn:not(.checked)').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = parseInt(btn.dataset.id);
            const result = callBridge('confirmarMedicamento', App.usuario.id, id);
            
            if (result.success) {
                showToast('Medicamento confirmado!', 'success');
                loadMedicamentosIdoso();
            } else {
                showToast(result.error || 'Erro ao confirmar', 'error');
            }
        });
    });
}

function loadExerciciosIdoso() {
    if (!App.usuario) return;
    
    const result = callBridge('getExercicios', App.usuario.id);
    const container = document.getElementById('lista-exercicios-idoso');
    
    if (!result.success || !result.data || result.data.length === 0) {
        container.innerHTML = '<div class="empty-state"><p>Nenhum exercício cadastrado.</p></div>';
        return;
    }
    
    container.innerHTML = result.data.map(ex => {
        const concluidoResult = callBridge('isExercicioConcluidoHoje', App.usuario.id, ex.id);
        const concluido = concluidoResult.success && concluidoResult.data;
        
        return `
            <div class="check-item ${concluido ? 'confirmed' : ''}">
                <button type="button" class="check-btn ${concluido ? 'checked' : ''}" 
                        data-id="${ex.id}" 
                        ${concluido ? 'disabled' : ''}
                        aria-label="${concluido ? 'Já feito' : 'Marcar como feito'} ${escapeHtml(ex.nome)}">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20,6 9,17 4,12"/></svg>
                </button>
                <div class="check-content">
                    <div class="check-title">${escapeHtml(ex.nome)}</div>
                    <div class="check-subtitle">${ex.duracaoMinutos} minutos</div>
                    ${ex.descricao ? `<div class="check-subtitle">${escapeHtml(ex.descricao)}</div>` : ''}
                </div>
            </div>
        `;
    }).join('');
    
    container.querySelectorAll('.check-btn:not(.checked)').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = parseInt(btn.dataset.id);
            const result = callBridge('confirmarExercicio', App.usuario.id, id, '');
            
            if (result.success) {
                showToast('Exercício concluído!', 'success');
                loadExerciciosIdoso();
            } else {
                showToast(result.error || 'Erro ao confirmar', 'error');
            }
        });
    });
}

function loadVisitasIdoso() {
    if (!App.usuario) return;
    
    const result = callBridge('getProximasVisitas', App.usuario.id);
    const container = document.getElementById('lista-visitas-idoso');
    
    if (!result.success || !result.data || result.data.length === 0) {
        container.innerHTML = '<div class="empty-state"><p>Nenhuma visita agendada.</p></div>';
        return;
    }
    
    container.innerHTML = result.data.map(v => `
        <div class="card" style="margin-bottom: 8px;">
            <strong>${formatDate(v.data)}</strong>
            <span class="badge badge-${getStatusClass(v.status)}">${getStatusText(v.status)}</span>
            <p style="color: var(--color-text-secondary); margin: 4px 0;">
                ${v.horaInicio ? formatTime(v.horaInicio) : ''} ${v.horaFim ? '- ' + formatTime(v.horaFim) : ''}
            </p>
            ${v.descricao ? `<p>${escapeHtml(v.descricao)}</p>` : ''}
        </div>
    `).join('');
}

function initEmergencia() {
    document.getElementById('btn-confirmar-emergencia')?.addEventListener('click', () => {
        if (!App.usuario) return;
        
        const telefoneResult = callBridge('getTelefoneEmergencia', App.usuario.id);
        const telefone = telefoneResult.success ? telefoneResult.data : '192';
        
        showModal('Confirmar Emergência', 
            `<p>Você está prestes a registrar um alerta de emergência e será direcionado para ligar para:</p>
             <p style="font-size: 1.5em; font-weight: bold; text-align: center; margin: 16px 0;">${telefone}</p>
             <p><strong>Deseja continuar?</strong></p>`,
            () => {
                const alertaResult = callBridge('criarAlertaEmergencia', App.usuario.id, 'Emergência acionada pelo usuário');
                
                if (alertaResult.success) {
                    window.location.href = `tel:${telefone}`;
                } else {
                    showToast('Alerta registrado. Abra o discador manualmente.', 'info');
                }
            }, 'Ligar Agora');
    });
}
