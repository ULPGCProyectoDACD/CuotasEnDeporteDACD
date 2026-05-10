(() => {
    'use strict';
    const API_BASE = '';
    const REFRESH_INTERVAL_MS = 30_000;
    let currentView = 'global';
    let filtersData = { teams: [], bookmakers: [] };
    let refreshTimer = null;
    const dom = {
        tabs: document.querySelectorAll('.nav-tab'),
        filterBar: document.getElementById('filter-bar'),
        filterLabel: document.getElementById('filter-label'),
        filterSelect: document.getElementById('filter-select'),
        tableBody: document.getElementById('table-body'),
        tableTitle: document.getElementById('table-title'),
        resultCount: document.getElementById('result-count'),
        tableContainer: document.getElementById('table-container'),
        emptyState: document.getElementById('empty-state'),
        statsRow: document.getElementById('stats-row'),
        statTotal: document.querySelector('#stat-total .stat-value'),
        statValueBets: document.querySelector('#stat-value-bets .stat-value'),
        statBestIndex: document.querySelector('#stat-best-index .stat-value'),
        statAvgIndex: document.querySelector('#stat-avg-index .stat-value'),
        statusDot: document.getElementById('status-dot'),
        statusText: document.getElementById('status-text'),
    };
    async function init() {
        bindNavigation();
        bindFilterChange();
        await loadFilters();
        await loadPredictions();
        startAutoRefresh();
    }
    function bindNavigation() {
        dom.tabs.forEach(tab => {
            tab.addEventListener('click', () => {
                const view = tab.dataset.view;
                if (view === currentView) return;
                switchView(view);
            });
        });
    }
    function switchView(view) {
        currentView = view;
        dom.tabs.forEach(t => t.classList.remove('active'));
        document.querySelector(`[data-view="${view}"]`).classList.add('active');
        if (view === 'global') {
            dom.filterBar.classList.add('hidden');
        } else {
            dom.filterBar.classList.remove('hidden');
            populateFilterSelect(view);
        }
        updateTableTitle(view);
        dom.tableContainer.classList.remove('view-transition');
        void dom.tableContainer.offsetWidth;
        dom.tableContainer.classList.add('view-transition');
        loadPredictions();
    }
    function updateTableTitle(view) {
        const titles = {
            global: '🏆 Mejores Cuotas — Ranking Global de Riesgo',
            team: '⚽ Cuotas por Equipo — Análisis de Riesgo',
            bookmaker: '🏛️ Cuotas por Casa de Apuestas — Análisis de Riesgo',
        };
        dom.tableTitle.textContent = titles[view] || titles.global;
    }
    async function loadFilters() {
        try {
            const res = await fetch(`${API_BASE}/api/filters`);
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            filtersData = await res.json();
        } catch (err) {
            console.error('Error loading filters:', err);
            filtersData = { teams: [], bookmakers: [] };
        }
    }
    function populateFilterSelect(view) {
        const items = view === 'team' ? filtersData.teams : filtersData.bookmakers;
        const label = view === 'team' ? 'Selecciona un equipo:' : 'Selecciona una casa de apuestas:';
        const placeholder = view === 'team' ? '— Todos los equipos —' : '— Todas las casas de apuestas —';
        dom.filterLabel.textContent = label;
        dom.filterSelect.innerHTML = `<option value="">${placeholder}</option>`;
        items.forEach(item => {
            const opt = document.createElement('option');
            opt.value = item;
            opt.textContent = item;
            dom.filterSelect.appendChild(opt);
        });
    }
    function bindFilterChange() {
        dom.filterSelect.addEventListener('change', () => {
            loadPredictions();
        });
    }
    async function loadPredictions() {
        showLoading();
        const url = buildApiUrl();
        try {
            const res = await fetch(url);
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            const predictions = await res.json();
            setOnlineStatus(true);
            if (predictions.length === 0) {
                showEmptyState();
            } else {
                renderTable(predictions);
                updateStats(predictions);
                showTable();
            }
        } catch (err) {
            console.error('Error loading predictions:', err);
            setOnlineStatus(false);
            showEmptyState();
        }
    }
    function buildApiUrl() {
        let url = `${API_BASE}/api/predictions`;
        const selectedValue = dom.filterSelect.value;
        if (currentView === 'team' && selectedValue) {
            url += `?team=${encodeURIComponent(selectedValue)}`;
        } else if (currentView === 'bookmaker' && selectedValue) {
            url += `?bookmaker=${encodeURIComponent(selectedValue)}`;
        }
        return url;
    }
    function renderTable(predictions) {
        dom.tableBody.innerHTML = '';
        dom.resultCount.textContent = `${predictions.length} resultado${predictions.length !== 1 ? 's' : ''}`;
        predictions.forEach((p, index) => {
            const tr = document.createElement('tr');
            tr.style.animationDelay = `${Math.min(index * 25, 500)}ms`;
            tr.innerHTML = `
                <td class="rank-cell ${index < 3 ? 'top-3' : ''}">${index + 1}</td>
                <td>
                    <div class="match-cell">
                        <span class="match-home">${escapeHtml(p.homeTeam)}</span>
                        <span class="match-vs">vs</span>
                        <span class="match-away">${escapeHtml(p.awayTeam)}</span>
                    </div>
                </td>
                <td class="date-cell">${formatDate(p.matchDate)}</td>
                <td class="bookmaker-cell">${escapeHtml(p.bookmaker)}</td>
                <td>${renderOutcomeBadge(p.outcome)}</td>
                <td><span class="odds-value">${p.oddPrice.toFixed(2)}</span></td>
                <td class="prob-value">${formatPercent(p.probHome)}</td>
                <td class="prob-value">${formatPercent(p.probDraw)}</td>
                <td class="prob-value">${formatPercent(p.probAway)}</td>
                <td>${renderRiskBadge(p.benefitRiskIndex)}</td>
            `;
            dom.tableBody.appendChild(tr);
        });
    }
    function renderOutcomeBadge(outcome) {
        const isDraw = outcome.toLowerCase() === 'draw' || outcome.toLowerCase() === 'empate';
        const cls = isDraw ? 'outcome-badge draw' : 'outcome-badge';
        const label = isDraw ? 'Empate' : escapeHtml(outcome);
        return `<span class="${cls}">${label}</span>`;
    }
    function renderRiskBadge(index) {
        let cls, icon;
        if (index > 0.3) {
            cls = 'risk-badge positive strong';
            icon = '🔥';
        } else if (index > 0) {
            cls = 'risk-badge positive';
            icon = '▲';
        } else if (index > -0.3) {
            cls = 'risk-badge neutral';
            icon = '●';
        } else {
            cls = 'risk-badge negative';
            icon = '▼';
        }
        return `<span class="${cls}">${icon} ${index >= 0 ? '+' : ''}${index.toFixed(3)}</span>`;
    }
    function updateStats(predictions) {
        const total = predictions.length;
        const valueBets = predictions.filter(p => p.benefitRiskIndex > 0).length;
        const bestIndex = predictions.length > 0
            ? Math.max(...predictions.map(p => p.benefitRiskIndex))
            : 0;
        const avgIndex = predictions.length > 0
            ? predictions.reduce((sum, p) => sum + p.benefitRiskIndex, 0) / total
            : 0;
        animateValue(dom.statTotal, total, false);
        animateValue(dom.statValueBets, valueBets, false);
        dom.statBestIndex.textContent = bestIndex >= 0 ? `+${bestIndex.toFixed(3)}` : bestIndex.toFixed(3);
        dom.statAvgIndex.textContent = avgIndex >= 0 ? `+${avgIndex.toFixed(3)}` : avgIndex.toFixed(3);
    }
    function animateValue(element, target, isDecimal) {
        const duration = 600;
        const start = parseInt(element.textContent) || 0;
        const range = target - start;
        const startTime = performance.now();
        function update(currentTime) {
            const elapsed = currentTime - startTime;
            const progress = Math.min(elapsed / duration, 1);
            const eased = 1 - Math.pow(1 - progress, 3);
            const current = Math.round(start + range * eased);
            element.textContent = isDecimal ? current.toFixed(3) : current;
            if (progress < 1) {
                requestAnimationFrame(update);
            }
        }
        requestAnimationFrame(update);
    }
    function showLoading() {
        dom.emptyState.classList.add('hidden');
        dom.tableContainer.style.display = '';
        dom.tableBody.innerHTML = `
            <tr class="loading-row">
                <td colspan="10">
                    <div class="loader"></div>
                    <span>Cargando predicciones...</span>
                </td>
            </tr>
        `;
    }
    function showTable() {
        dom.tableContainer.style.display = '';
        dom.emptyState.classList.add('hidden');
    }
    function showEmptyState() {
        dom.tableContainer.style.display = 'none';
        dom.emptyState.classList.remove('hidden');
        dom.resultCount.textContent = '';
        dom.statTotal.textContent = '0';
        dom.statValueBets.textContent = '0';
        dom.statBestIndex.textContent = '—';
        dom.statAvgIndex.textContent = '—';
    }
    function setOnlineStatus(online) {
        if (online) {
            dom.statusDot.className = 'status-dot online';
            dom.statusText.textContent = 'Conectado';
        } else {
            dom.statusDot.className = 'status-dot offline';
            dom.statusText.textContent = 'Sin conexión';
        }
    }
    function formatDate(dateStr) {
        if (!dateStr) return '—';
        try {
            const d = new Date(dateStr);
            if (isNaN(d.getTime())) return dateStr;
            return d.toLocaleDateString('es-ES', {
                day: '2-digit', month: 'short', year: 'numeric',
                hour: '2-digit', minute: '2-digit'
            });
        } catch {
            return dateStr;
        }
    }
    function formatPercent(value) {
        return `${(value * 100).toFixed(1)}%`;
    }
    function escapeHtml(str) {
        if (!str) return '';
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }
    function startAutoRefresh() {
        if (refreshTimer) clearInterval(refreshTimer);
        refreshTimer = setInterval(async () => {
            await loadFilters();
            await loadPredictions();
        }, REFRESH_INTERVAL_MS);
    }
    document.addEventListener('DOMContentLoaded', init);
})();