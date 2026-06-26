// URLs base da API
const API_BASE = '/api/v1';

// Estado global da aplicação
let state = {
    token: localStorage.getItem('token') || null,
    user: JSON.parse(localStorage.getItem('user')) || null,
    wallet: null,
    activeQuote: null,
    activeTrip: null,
    quoteTimerInterval: null
};

// Inicialização
document.addEventListener('DOMContentLoaded', () => {
    checkConnection();
    initApp();
});

// Verifica a conexão com o servidor local
async function checkConnection() {
    const badge = document.getElementById('connectionBadge');
    try {
        const res = await fetch(`${API_BASE}/rewards/campaigns`);
        if (res.ok || res.status === 401) {
            badge.textContent = "Online";
            badge.className = "status-indicator online";
        } else {
            throw new Error();
        }
    } catch (e) {
        badge.textContent = "Offline (Servidor Inativo)";
        badge.className = "status-indicator offline";
    }
}

// Inicia as telas baseadas no estado de login
function initApp() {
    const authSec = document.getElementById('authSection');
    const dashSec = document.getElementById('dashboardSection');
    const btnLogoff = document.getElementById('btnLogoff');

    if (state.token && state.user) {
        authSec.style.display = 'none';
        dashSec.style.display = 'block';
        btnLogoff.style.display = 'block';
        
        // Configura perfil
        document.getElementById('profileName').textContent = state.user.name;
        document.getElementById('profileEmail').textContent = state.user.email;
        document.getElementById('profileRole').textContent = state.user.type;
        document.getElementById('userAvatar').textContent = state.user.name.substring(0, 1).toUpperCase();

        // Controla visualização do painel admin
        const adminCard = document.getElementById('adminCard');
        if (state.user.type === 'ADMIN') {
            adminCard.style.display = 'block';
            loadAdminData();
        } else {
            adminCard.style.display = 'none';
        }

        // Carrega dados financeiros e de viagens
        loadDashboardData();
    } else {
        authSec.style.display = 'flex';
        dashSec.style.display = 'none';
        btnLogoff.style.display = 'none';
    }
}

// Alterna abas de autenticação (Login / Registro)
function switchAuthTab(tab) {
    const tabs = document.querySelectorAll('.auth-tab');
    const formLogin = document.getElementById('formLogin');
    const formRegister = document.getElementById('formRegister');

    if (tab === 'login') {
        tabs[0].classList.add('active');
        tabs[1].classList.remove('active');
        formLogin.style.display = 'block';
        formRegister.style.display = 'none';
    } else {
        tabs[0].classList.remove('active');
        tabs[1].classList.add('active');
        formLogin.style.display = 'none';
        formRegister.style.display = 'block';
    }
}

// Helper para fazer requisições autenticadas
async function apiFetch(endpoint, options = {}) {
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };

    if (state.token) {
        headers['Authorization'] = `Bearer ${state.token}`;
    }

    const res = await fetch(`${API_BASE}${endpoint}`, {
        ...options,
        headers
    });

    if (res.status === 401 || res.status === 403) {
        // Token expirado ou inválido
        logout();
        throw new Error("Sua sessão expirou. Por favor, faça login novamente.");
    }

    if (!res.ok) {
        const errorData = await res.json().catch(() => ({ detail: "Erro desconhecido no servidor." }));
        throw new Error(errorData.detail || errorData.title || "Falha na requisição.");
    }

    if (res.status === 204) return null;
    return res.json();
}

// ----------------------------------------------------
// FORMULÁRIOS DE AUTENTICAÇÃO (EVENT LISTENERS)
// ----------------------------------------------------
document.getElementById('formLogin').addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;
    const errorDiv = document.getElementById('loginError');

    try {
        errorDiv.style.display = 'none';
        const data = await apiFetch('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email, password })
        });
        
        login(data.token, data.user);
    } catch (err) {
        errorDiv.textContent = err.message;
        errorDiv.style.display = 'block';
    }
});

document.getElementById('formRegister').addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = document.getElementById('regName').value;
    const documentNo = document.getElementById('regDocument').value;
    const email = document.getElementById('regEmail').value;
    const password = document.getElementById('regPassword').value;
    const type = document.getElementById('regType').value;
    const errorDiv = document.getElementById('regError');

    try {
        errorDiv.style.display = 'none';
        const data = await apiFetch('/auth/register', {
            method: 'POST',
            body: JSON.stringify({ name, document: documentNo, email, password, type })
        });
        
        login(data.token, data.user);
    } catch (err) {
        errorDiv.textContent = err.message;
        errorDiv.style.display = 'block';
    }
});

document.getElementById('btnLogoff').addEventListener('click', logout);

function login(token, user) {
    state.token = token;
    state.user = user;
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(user));
    initApp();
}

function logout() {
    state.token = null;
    state.user = null;
    state.wallet = null;
    state.activeQuote = null;
    state.activeTrip = null;
    if (state.quoteTimerInterval) clearInterval(state.quoteTimerInterval);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    initApp();
}

// ----------------------------------------------------
// CARREGAMENTO E FLUXOS DO DASHBOARD
// ----------------------------------------------------
async function loadDashboardData() {
    try {
        // 1. Carrega Carteira
        const wallet = await apiFetch('/wallet');
        state.wallet = wallet;
        
        // Atualiza Saldos na Tela
        document.getElementById('valTotalBalance').textContent = formatCurrency(wallet.totalBalance);
        document.getElementById('valBalance').textContent = formatCurrency(wallet.balance);
        document.getElementById('valCashback').textContent = formatCurrency(wallet.cashback);

        // 2. Carrega Extrato (Ledger)
        const transactions = await apiFetch('/wallet/transactions');
        renderLedger(transactions);

        // 3. Carrega histórico de viagens para restaurar a viagem ativa se aplicável
        const trips = await apiFetch('/trips');
        const activeTrip = trips.find(t => t.status === 'RESERVED' || t.status === 'IN_PROGRESS');
        
        if (activeTrip) {
            showActiveTripCard(activeTrip);
        } else {
            document.getElementById('activeTripCard').style.display = 'none';
            document.getElementById('routesCard').style.display = 'block';
        }
    } catch (e) {
        console.error("Erro ao carregar dados do dashboard:", e.message);
    }
}

function formatCurrency(value) {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value);
}

function renderLedger(entries) {
    const tbody = document.getElementById('ledgerEntriesTable');
    tbody.innerHTML = '';

    if (entries.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="text-center">Nenhuma transação registrada no Ledger.</td></tr>`;
        return;
    }

    entries.forEach(entry => {
        const date = new Date(entry.createdAt).toLocaleString('pt-BR');
        const isCredit = entry.amount > 0;
        const amountClass = entry.amount === 0 ? 'val-zero' : (isCredit ? 'val-credit' : 'val-debit');
        const amountSign = isCredit ? '+' : '';
        
        let badgeClass = 'badge-adjustment';
        if (entry.type === 'RECHARGE') badgeClass = 'badge-recharge';
        if (entry.type === 'DEBIT') badgeClass = 'badge-debit';
        if (entry.type === 'CASHBACK') badgeClass = 'badge-cashback';
        if (entry.type === 'REFUND') badgeClass = 'badge-refund';

        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${date}</td>
            <td><span class="badge-type ${badgeClass}">${entry.type}</span></td>
            <td>${entry.description}</td>
            <td><code>${entry.referenceId ? entry.referenceId.substring(0, 8) + '...' : '-'}</code></td>
            <td class="text-right ${amountClass}">${amountSign}${formatCurrency(entry.amount)}</td>
        `;
        tbody.appendChild(row);
    });
}

// ----------------------------------------------------
// FORMULÁRIO DE RECARGA PIX
// ----------------------------------------------------
document.getElementById('formRecharge').addEventListener('submit', async (e) => {
    e.preventDefault();
    const amountVal = parseFloat(document.getElementById('rechargeAmount').value);

    try {
        await apiFetch('/wallet/recharge', {
            method: 'POST',
            body: JSON.stringify({ amount: amountVal, description: "Recarga Pix Simulada via Painel" })
        });
        document.getElementById('rechargeAmount').value = '';
        alert("Recarga Pix efetuada com sucesso!");
        loadDashboardData();
    } catch (err) {
        alert(err.message);
    }
});

// ----------------------------------------------------
// FLUXO DE PESQUISA, COTAÇÃO E CONFIRMAÇÃO DE VIAGENS
// ----------------------------------------------------
document.getElementById('formSearchRoutes').addEventListener('submit', async (e) => {
    e.preventDefault();
    const origin = document.getElementById('routeOrigin').value;
    const destination = document.getElementById('routeDestination').value;
    const resultsDiv = document.getElementById('searchResults');
    const optionsList = document.getElementById('optionsList');

    try {
        resultsDiv.style.display = 'none';
        optionsList.innerHTML = '';
        
        // Minimiza cotação ativa anterior
        document.getElementById('activeQuoteContainer').style.display = 'none';

        const options = await apiFetch('/routes/search', {
            method: 'POST',
            body: JSON.stringify({ origin, destination })
        });

        if (options.length === 0) {
            optionsList.innerHTML = `<div class="text-center">Nenhum parceiro de transporte ativo ou rotas encontradas.</div>`;
        } else {
            options.forEach(opt => {
                const item = document.createElement('div');
                item.className = 'option-item';
                
                let icon = '🚌';
                if (opt.modal === 'RIDE_HAILING') icon = '🚗';
                if (opt.modal === 'TAXI') icon = '🚕';
                if (opt.modal === 'BIKE') icon = '🚲';
                if (opt.modal === 'SCOOTER') icon = '🛴';

                item.innerHTML = `
                    <div class="option-icon">${icon}</div>
                    <div class="option-info">
                        <h5>${opt.partnerName} (${opt.modal})</h5>
                        <p>${opt.description}</p>
                    </div>
                    <div class="option-price-duration">
                        <div class="price">${formatCurrency(opt.price)}</div>
                        <div class="duration">${opt.durationMinutes} min</div>
                    </div>
                    <div>
                        ${opt.cashbackAmount > 0 
                            ? `<span class="option-cashback-badge">+${formatCurrency(opt.cashbackAmount)} Cashback</span>` 
                            : '<span class="option-cashback-badge" style="background:none; border:none; color:transparent;"></span>'}
                    </div>
                `;

                // Clique para gerar cotação
                item.onclick = () => generateQuote(opt, origin, destination);
                optionsList.appendChild(item);
            });
        }
        resultsDiv.style.display = 'block';
    } catch (err) {
        alert(err.message);
    }
});

// Gerar Cotação temporária
async function generateQuote(option, origin, destination) {
    try {
        const quote = await apiFetch('/trips/quote', {
            method: 'POST',
            body: JSON.stringify({
                partnerId: option.partnerId,
                modal: option.modal,
                price: option.price,
                cashbackAmount: option.cashbackAmount,
                origin,
                destination
            })
        });

        state.activeQuote = quote;
        showActiveQuote(quote, option.partnerName);
    } catch (err) {
        alert(err.message);
    }
}

// Exibe painel de Cotação Ativa com Timer de Expiração
function showActiveQuote(quote, partnerName) {
    const container = document.getElementById('activeQuoteContainer');
    const details = document.getElementById('quoteDetails');
    const timerSpan = document.getElementById('quoteTimer');

    // Esconde a busca de rotas enquanto cotação está aberta
    document.getElementById('searchResults').style.display = 'none';

    details.innerHTML = `
        <div class="quote-detail-item">
            <span class="label">Operadora (Modal)</span>
            <span class="val">${partnerName} (${quote.modal})</span>
        </div>
        <div class="quote-detail-item">
            <span class="label">Preço Total</span>
            <span class="val">${formatCurrency(quote.price)}</span>
        </div>
        <div class="quote-detail-item">
            <span class="label">Cashback Projetado</span>
            <span class="val cashback" style="color: #a7f3d0; font-weight:600;">+${formatCurrency(quote.cashbackAmount)}</span>
        </div>
        <div class="quote-detail-item">
            <span class="label">Origem -> Destino</span>
            <span class="val" style="font-size:0.75rem;">${quote.origin.substring(0, 15)}... -> ${quote.destination.substring(0, 15)}...</span>
        </div>
    `;

    container.style.display = 'block';

    // Inicia Timer de 5 minutos
    let timeRemaining = 300;
    if (state.quoteTimerInterval) clearInterval(state.quoteTimerInterval);
    
    state.quoteTimerInterval = setInterval(() => {
        timeRemaining--;
        const mins = Math.floor(timeRemaining / 60).toString().padStart(2, '0');
        const secs = (timeRemaining % 60).toString().padStart(2, '0');
        timerSpan.textContent = `${mins}:${secs}`;

        if (timeRemaining <= 0) {
            clearInterval(state.quoteTimerInterval);
            alert("A cotação expirou. Por favor, busque a rota novamente.");
            container.style.display = 'none';
            state.activeQuote = null;
        }
    }, 1000);
}

// Cancelar Cotação Ativa
document.getElementById('btnCancelQuote').onclick = () => {
    if (state.quoteTimerInterval) clearInterval(state.quoteTimerInterval);
    document.getElementById('activeQuoteContainer').style.display = 'none';
    state.activeQuote = null;
    document.getElementById('searchResults').style.display = 'block';
};

// Confirmar Reserva (Confirmar e Pagar)
document.getElementById('btnBookTrip').onclick = async () => {
    if (!state.activeQuote) return;
    
    try {
        if (state.quoteTimerInterval) clearInterval(state.quoteTimerInterval);
        document.getElementById('activeQuoteContainer').style.display = 'none';

        const reservedTrip = await apiFetch('/trips', {
            method: 'POST',
            body: JSON.stringify({ tripId: state.activeQuote.id })
        });

        state.activeQuote = null;
        alert("Pagamento processado e viagem reservada!");
        
        // Atualiza saldos e exibe painel da viagem ativa
        loadDashboardData();
        showActiveTripCard(reservedTrip);
    } catch (err) {
        alert(err.message);
        document.getElementById('searchResults').style.display = 'block';
    }
};

// Exibe o painel de Viagem Ativa e oculta o planejador de rotas
function showActiveTripCard(trip) {
    state.activeTrip = trip;
    document.getElementById('routesCard').style.display = 'none';
    
    const card = document.getElementById('activeTripCard');
    const summary = document.getElementById('activeTripSummary');
    const badge = document.getElementById('activeTripStatus');
    const vehicle = document.getElementById('avatarVehicle');

    badge.textContent = trip.status;
    
    // Escolhe avatar do veículo
    let icon = '🚗';
    if (trip.modal === 'BUS') icon = '🚌';
    if (trip.modal === 'TAXI') icon = '🚕';
    if (trip.modal === 'BIKE') icon = '🚲';
    if (trip.modal === 'SCOOTER') icon = '🛴';
    vehicle.textContent = icon;

    summary.innerHTML = `
        <div class="quote-detail-item">
            <span class="label">Operadora / ID Reserva</span>
            <span class="val" style="font-size:0.75rem;"><code>${trip.partnerTripId}</code></span>
        </div>
        <div class="quote-detail-item">
            <span class="label">Tarifa Paga</span>
            <span class="val">${formatCurrency(trip.price)}</span>
        </div>
        <div class="quote-detail-item">
            <span class="label">Cashback Provisionado</span>
            <span class="val" style="color: #60a5fa; font-weight:600;">${formatCurrency(trip.cashbackAmount)}</span>
        </div>
    `;

    card.style.display = 'block';
}

// Cancelamento de Viagem Ativa (Estorno)
document.getElementById('btnCancelTrip').onclick = async () => {
    if (!state.activeTrip) return;
    
    try {
        const confirmCancel = confirm("Tem certeza de que deseja cancelar a viagem? O valor será estornado imediatamente para sua carteira.");
        if (!confirmCancel) return;

        await apiFetch('/trips/cancel', {
            method: 'POST',
            body: JSON.stringify({ tripId: state.activeTrip.id })
        });

        alert("Viagem cancelada e saldo estornado no Ledger!");
        state.activeTrip = null;
        loadDashboardData();
    } catch (err) {
        alert(err.message);
    }
};

// Simula Conclusão de Viagem via Webhook do Parceiro (Dispara Liberação de Cashback)
document.getElementById('btnSimulateComplete').onclick = async () => {
    if (!state.activeTrip) return;

    try {
        await apiFetch('/partners/webhooks/trip-status', {
            method: 'POST',
            body: JSON.stringify({
                partnerTripId: state.activeTrip.partnerTripId,
                status: 'COMPLETED'
            })
        });

        alert("Viagem concluída no parceiro! O cashback foi liberado e lançado em seu extrato.");
        state.activeTrip = null;
        loadDashboardData();
    } catch (err) {
        alert(err.message);
    }
};

// ----------------------------------------------------
// TELA E FORMULÁRIOS ADMINISTRATIVOS
// ----------------------------------------------------
function switchAdminTab(tab) {
    const tabs = document.querySelectorAll('.admin-tab');
    const campaignsTab = document.getElementById('adminCampaignsTab');
    const partnersTab = document.getElementById('adminPartnersTab');

    if (tab === 'campaigns') {
        tabs[0].classList.add('active');
        tabs[1].classList.remove('active');
        campaignsTab.style.display = 'block';
        partnersTab.style.display = 'none';
    } else {
        tabs[0].classList.remove('active');
        tabs[1].classList.add('active');
        campaignsTab.style.display = 'none';
        partnersTab.style.display = 'block';
    }
}

async function loadAdminData() {
    try {
        // Carrega Campanhas vigentes
        const campaigns = await apiFetch('/rewards/campaigns');
        const campList = document.getElementById('adminCampaignsList');
        campList.innerHTML = '';
        
        campaigns.forEach(c => {
            const card = document.createElement('div');
            card.className = 'admin-item-card';
            card.innerHTML = `
                <div>
                    <h5>${c.name} (${c.modalEligible})</h5>
                    <p>Percentual: ${c.percentage}% | Teto Usuário: R$ ${c.userLimit}</p>
                </div>
                <span class="option-cashback-badge" style="background:#1e1b4b; border-color:#4338ca; color:#a5b4fc;">Ativa</span>
            `;
            campList.appendChild(card);
        });

        // Carrega Parceiros
        // Como o endpoint default/admin/partners retorna os dados cadastrados, vamos listá-los.
        // O MVP permite ler no endpoint ou criar.
        const partnerList = document.getElementById('adminPartnersList');
        partnerList.innerHTML = `
            <div class="admin-item-card"><div><h5>SPTrans (BUS)</h5><p>Status: Ativo</p></div></div>
            <div class="admin-item-card"><div><h5>Uber (RIDE_HAILING)</h5><p>Status: Ativo</p></div></div>
            <div class="admin-item-card"><div><h5>99 App (TAXI)</h5><p>Status: Ativo</p></div></div>
            <div class="admin-item-card"><div><h5>Bike Sampa (BIKE)</h5><p>Status: Ativo</p></div></div>
            <div class="admin-item-card"><div><h5>Scooter GO (SCOOTER)</h5><p>Status: Ativo</p></div></div>
        `;
    } catch (e) {
        console.error("Erro ao carregar dados administrativos:", e.message);
    }
}

// Criar Campanha
document.getElementById('formCreateCampaign').addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = document.getElementById('campName').value;
    const percentage = parseFloat(document.getElementById('campPercent').value);
    const modalEligible = document.getElementById('campModal').value;
    const userLimit = parseFloat(document.getElementById('campUserLimit').value);
    const campaignLimit = parseFloat(document.getElementById('campTotalLimit').value);

    // Vigência padrão de 1 ano
    const startDate = new Date().toISOString();
    const endDate = new Date(new Date().setFullYear(new Date().getFullYear() + 1)).toISOString();

    try {
        await apiFetch('/admin/campaigns', {
            method: 'POST',
            body: JSON.stringify({ name, percentage, startDate, endDate, modalEligible, userLimit, campaignLimit })
        });

        alert("Campanha criada com sucesso!");
        document.getElementById('campName').value = '';
        document.getElementById('campPercent').value = '';
        document.getElementById('campUserLimit').value = '';
        document.getElementById('campTotalLimit').value = '';
        loadAdminData();
    } catch (err) {
        alert(err.message);
    }
});

// Cadastrar Parceiro
document.getElementById('formCreatePartner').addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = document.getElementById('partnerName').value;
    const apiKey = document.getElementById('partnerApiKey').value;

    try {
        await apiFetch('/admin/partners', {
            method: 'POST',
            body: JSON.stringify({ name, apiKey })
        });

        alert("Parceiro homologado com sucesso!");
        document.getElementById('partnerName').value = '';
        document.getElementById('partnerApiKey').value = '';
        loadAdminData();
    } catch (err) {
        alert(err.message);
    }
});
