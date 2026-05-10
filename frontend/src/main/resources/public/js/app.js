(() => {
    'use strict';

    
    const API_BASE         = '';
    const REFRESH_MS       = 30_000;
    const PAGE_SIZE        = 25;     
    const TOP_N            = 5;      
    const BOOKMAKER_CHART_N = 8;     

    
    let currentView    = 'global';
    let filtersData    = { teams: [], bookmakers: [] };
    let allPredictions = [];
    let currentPage    = 1;
    let refreshTimer   = null;
    let donutChart     = null;
    let barChart       = null;
    let chartsReady    = false;

    
    const $ = id => document.getElementById(id);
    const $$ = sel => document.querySelector(sel);

    const dom = {
        tabs:          document.querySelectorAll('.nav-tab'),
        filterBar:     $('filter-bar'),
        filterLabel:   $('filter-label'),
        filterSelect:  $('filter-select'),
        tableBody:     $('table-body'),
        tableTitle:    $('table-title'),
        tableRange:    $('table-range'),
        resultCount:   $('result-count'),
        tableContainer:$('table-container'),
        emptyState:    $('empty-state'),
        top5Section:   $('top5-section'),
        top5Grid:      $('top5-grid'),
        statTotal:     $$('#stat-total    .stat-value'),
        statValueBets: $$('#stat-value-bets .stat-value'),
        statBestIndex: $$('#stat-best-index .stat-value'),
        statAvgIndex:  $$('#stat-avg-index  .stat-value'),
        statusDot:     $('status-dot'),
        statusText:    $('status-text'),
        lastUpdate:    $('last-update'),
        pagination:    $('pagination-controls'),
        btnPrev:       $('btn-prev-page'),
        btnNext:       $('btn-next-page'),
        pageInfo:      $('page-info'),
        tickerInner:   $('ticker-inner'),
        donutCenter:   $('donut-center'),
    };

    
    async function init() {
        bindNavigation();
        bindFilterChange();
        bindPagination();

        
        waitForChartJs(() => {
            initCharts();
            chartsReady = true;
            if (allPredictions.length) updateCharts(allPredictions);
        });

        await loadFilters();
        await loadPredictions();
        startAutoRefresh();
    }

    function waitForChartJs(cb) {
        if (typeof Chart !== 'undefined') { cb(); return; }
        const t = setInterval(() => {
            if (typeof Chart !== 'undefined') { clearInterval(t); cb(); }
        }, 50);
    }

    
    function initCharts() {
        Chart.defaults.color = '#38384e';
        Chart.defaults.font.family = "'JetBrains Mono', monospace";

        
        const dCtx = $('donut-chart').getContext('2d');
        donutChart = new Chart(dCtx, {
            type: 'doughnut',
            data: {
                labels: ['Value Bets', 'Sin valor'],
                datasets: [{
                    data: [0, 100],
                    backgroundColor: [
                        'rgba(0,255,157,0.85)',
                        'rgba(168,85,247,0.25)',
                    ],
                    borderColor: [
                        'rgba(0,255,157,0.4)',
                        'rgba(168,85,247,0.1)',
                    ],
                    borderWidth: 1,
                    hoverBackgroundColor: [
                        'rgba(0,255,157,1)',
                        'rgba(168,85,247,0.45)',
                    ],
                    hoverBorderColor: 'transparent',
                }],
            },
            options: {
                cutout: '74%',
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: 'rgba(10,10,20,0.95)',
                        titleColor: '#00ff9d',
                        bodyColor: '#7878a0',
                        borderColor: 'rgba(0,255,157,0.18)',
                        borderWidth: 1,
                        padding: 10,
                        callbacks: {
                            label: ctx => `  ${ctx.label}: ${ctx.parsed.toFixed(1)}%`,
                        },
                    },
                },
                animation: { duration: 900, easing: 'easeInOutQuart' },
            },
        });

        
        const bCtx = $('bar-chart').getContext('2d');
        barChart = new Chart(bCtx, {
            type: 'bar',
            data: { labels: [], datasets: [{ data: [], backgroundColor: [], borderColor: [], borderWidth: 1, borderRadius: 3 }] },
            options: {
                indexAxis: 'y',
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: 'rgba(10,10,20,0.95)',
                        titleColor: '#eeeef5',
                        bodyColor: '#7878a0',
                        borderColor: 'rgba(255,255,255,0.06)',
                        borderWidth: 1,
                        padding: 10,
                        callbacks: {
                            label: ctx => `  Índice medio: ${ctx.parsed.x >= 0 ? '+' : ''}${ctx.parsed.x.toFixed(4)}`,
                        },
                    },
                },
                scales: {
                    x: {
                        grid: { color: 'rgba(255,255,255,0.04)', drawTicks: false },
                        ticks: {
                            color: '#38384e',
                            font: { family: "'JetBrains Mono', monospace", size: 10 },
                            callback: v => (v >= 0 ? '+' : '') + v.toFixed(2),
                        },
                        border: { color: 'rgba(255,255,255,0.05)', dash: [3,3] },
                    },
                    y: {
                        grid: { display: false },
                        ticks: {
                            color: '#7878a0',
                            font: { family: "'DM Sans', sans-serif", size: 11 },
                        },
                        border: { color: 'rgba(255,255,255,0.05)' },
                    },
                },
                animation: { duration: 700 },
            },
        });
    }

    function updateCharts(predictions) {
        if (!chartsReady) return;

        const total     = predictions.length;
        const valueCnt  = predictions.filter(p => p.benefitRiskIndex > 0).length;
        const valuePct  = total > 0 ? (valueCnt / total) * 100 : 0;
        const noPct     = 100 - valuePct;

        
        donutChart.data.datasets[0].data = [valuePct, noPct];
        donutChart.update();
        dom.donutCenter.querySelector('.donut-pct').textContent = `${Math.round(valuePct)}%`;

        
        const bmap = {};
        predictions.forEach(p => {
            if (!bmap[p.bookmaker]) bmap[p.bookmaker] = { sum: 0, n: 0 };
            bmap[p.bookmaker].sum += p.benefitRiskIndex;
            bmap[p.bookmaker].n++;
        });

        const entries = Object.entries(bmap)
            .map(([name, { sum, n }]) => ({ name, avg: sum / n }))
            .sort((a, b) => b.avg - a.avg)
            .slice(0, BOOKMAKER_CHART_N);

        const labels  = entries.map(e => e.name);
        const data    = entries.map(e => +e.avg.toFixed(4));
        const bgCol   = data.map(v => v > 0 ? 'rgba(0,255,157,0.65)'  : 'rgba(168,85,247,0.55)');
        const bdCol   = data.map(v => v > 0 ? 'rgba(0,255,157,0.9)'   : 'rgba(168,85,247,0.85)');

        barChart.data.labels                        = labels;
        barChart.data.datasets[0].data              = data;
        barChart.data.datasets[0].backgroundColor   = bgCol;
        barChart.data.datasets[0].borderColor       = bdCol;
        barChart.update();
    }

    
    function bindNavigation() {
        dom.tabs.forEach(tab => {
            tab.addEventListener('click', () => {
                const v = tab.dataset.view;
                if (v === currentView) return;
                switchView(v);
            });
        });
    }

    function switchView(view) {
        currentView = view;
        dom.tabs.forEach(t => t.classList.toggle('active', t.dataset.view === view));

        const isFiltered = view !== 'global';
        dom.filterBar.classList.toggle('hidden', !isFiltered);
        if (isFiltered) populateFilterSelect(view);

        const titles = {
            global:    'Ranking Global de Riesgo',
            team:      'Análisis por Equipo',
            bookmaker: 'Análisis por Casa de Apuestas',
        };
        if (dom.tableTitle) dom.tableTitle.textContent = titles[view] ?? titles.global;

        currentPage = 1;
        loadPredictions();
    }

    
    async function loadFilters() {
        try {
            const res = await fetch(`${API_BASE}/api/filters`);
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            filtersData = await res.json();
        } catch (err) {
            console.error('[Filters]', err);
            filtersData = { teams: [], bookmakers: [] };
        }
    }

    function populateFilterSelect(view) {
        const items = view === 'team' ? filtersData.teams : filtersData.bookmakers;
        dom.filterLabel.textContent = view === 'team' ? 'Equipo:' : 'Casa de Apuestas:';
        dom.filterSelect.innerHTML  = `<option value="">${view === 'team' ? '— Todos los equipos —' : '— Todas las casas —'}</option>`;
        items.forEach(item => {
            const opt = Object.assign(document.createElement('option'), { value: item, textContent: item });
            dom.filterSelect.appendChild(opt);
        });
    }

    function bindFilterChange() {
        dom.filterSelect.addEventListener('change', () => { currentPage = 1; loadPredictions(); });
    }

    
    async function loadPredictions(isRefresh = false) {
        if (!isRefresh) showLoading();

        try {
            const res = await fetch(buildUrl());
            if (!res.ok) throw new Error(`HTTP ${res.status}`);

            allPredictions = await res.json();
            setStatus(true);

            if (allPredictions.length === 0) {
                showEmpty();
            } else {
                updateStats(allPredictions);
                updateCharts(allPredictions);
                renderTicker(allPredictions.slice(0, 16));
                renderTop5(allPredictions.slice(0, TOP_N));
                renderTable(allPredictions.slice(TOP_N));
                dom.top5Section.classList.remove('hidden');
                dom.emptyState.classList.add('hidden');
            }

            const now = new Date();
            dom.lastUpdate.textContent = `Actualizado: ${now.toLocaleTimeString('es-ES')}`;

        } catch (err) {
            console.error('[Predictions]', err);
            setStatus(false);
            showEmpty();
        }
    }

    function buildUrl() {
        const sel = dom.filterSelect.value;
        let url = `${API_BASE}/api/predictions`;
        if (currentView === 'team'      && sel) url += `?team=${encodeURIComponent(sel)}`;
        if (currentView === 'bookmaker' && sel) url += `?bookmaker=${encodeURIComponent(sel)}`;
        return url;
    }

    
    function renderTicker(predictions) {
        if (!dom.tickerInner || !predictions.length) return;

        const html = predictions.map(p => {
            const idx  = p.benefitRiskIndex;
            const sign = idx >= 0 ? '+' : '';
            const cls  = idx >= 0 ? 'ticker-idx-p' : 'ticker-idx-n';
            return `
                <span class="ticker-item">
                    <span class="ticker-match">${esc(p.homeTeam)} vs ${esc(p.awayTeam)}</span>
                    <span class="ticker-sep">·</span>
                    <span>${esc(p.outcome)}</span>
                    <span class="ticker-sep">·</span>
                    <span class="ticker-odds">@${p.oddPrice.toFixed(2)}</span>
                    <span class="ticker-sep">·</span>
                    <span class="${cls}">${sign}${idx.toFixed(3)}</span>
                </span>`;
        }).join('');

        
        dom.tickerInner.innerHTML = html + html;
    }

    
    function renderTop5(predictions) {
        dom.top5Grid.innerHTML = '';

        predictions.forEach((p, i) => {
            const rank   = i + 1;
            const isHero = rank === 1;
            const sign   = p.benefitRiskIndex >= 0 ? '+' : '';
            const rCls   = riskColorClass(p.benefitRiskIndex);
            const isDraw = isDrawOutcome(p.outcome);

            const card = document.createElement('div');
            card.className = `top-card rank-${rank}`;

            card.innerHTML = `
                <div class="card-rank">
                    <span class="rank-number">#${rank}</span>
                    ${isHero ? '<span class="rank-badge">MEJOR CUOTA</span>' : ''}
                </div>

                <div class="card-match">
                    <div class="card-team">${esc(p.homeTeam)}</div>
                    <div class="card-vs">vs</div>
                    <div class="card-team">${esc(p.awayTeam)}</div>
                </div>

                <div class="card-meta">
                    <span class="card-meta-item">
                        <i class="meta-icon">📅</i> ${formatDate(p.matchDate)}
                    </span>
                    <span class="card-meta-item">
                        <i class="meta-icon">🏛</i> ${esc(p.bookmaker)}
                    </span>
                </div>

                <span class="card-outcome ${isDraw ? 'draw' : ''}">
                    ${isDraw ? 'Empate' : esc(p.outcome)}
                </span>

                <div class="card-bottom">
                    <div class="card-odds-block">
                        <div class="card-lbl">Cuota</div>
                        <div class="card-odds-value">${p.oddPrice.toFixed(2)}</div>
                    </div>

                    ${isHero ? probBarsHTML(p) : ''}

                    <div class="card-risk-block">
                        <div class="card-lbl">Índice</div>
                        <div class="card-risk-value ${rCls}">${sign}${p.benefitRiskIndex.toFixed(3)}</div>
                    </div>
                </div>`;

            dom.top5Grid.appendChild(card);
        });
    }

    function probBarsHTML(p) {
        return `
            <div class="prob-bars">
                ${probRowHTML('L', p.probHome, 'home')}
                ${probRowHTML('E', p.probDraw, 'draw')}
                ${probRowHTML('V', p.probAway, 'away')}
            </div>`;
    }

    function probRowHTML(lbl, val, cls) {
        const pct = (val * 100).toFixed(1);
        return `
            <div class="prob-row">
                <span class="prob-lbl">${lbl}</span>
                <div class="prob-track">
                    <div class="prob-fill ${cls}" style="width:${pct}%"></div>
                </div>
                <span class="prob-val">${pct}%</span>
            </div>`;
    }

    
    function renderTable(rest) {
        dom.tableBody.innerHTML = '';

        if (!rest.length) {
            dom.tableContainer.classList.add('hidden');
            return;
        }

        dom.tableContainer.classList.remove('hidden');

        const total      = rest.length;
        const totalPages = Math.ceil(total / PAGE_SIZE);
        if (currentPage < 1) currentPage = 1;
        if (currentPage > totalPages) currentPage = totalPages;

        const start = (currentPage - 1) * PAGE_SIZE;
        const end   = Math.min(start + PAGE_SIZE, total);
        const items = rest.slice(start, end);

        dom.tableRange.textContent = `${start + 1}–${end} de ${total}`;
        dom.resultCount.textContent = `${allPredictions.length} predicciones`;

        items.forEach((p, i) => {
            const globalRank = TOP_N + start + i + 1;
            const sign  = p.benefitRiskIndex >= 0 ? '+' : '';
            const bCls  = riskBadgeClass(p.benefitRiskIndex);
            const isDraw = isDrawOutcome(p.outcome);

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td class="rank-cell">${globalRank}</td>
                <td>
                    <div class="match-cell">
                        <span class="match-home">${esc(p.homeTeam)}</span>
                        <span class="match-vs">vs</span>
                        <span class="match-away">${esc(p.awayTeam)}</span>
                    </div>
                </td>
                <td class="date-cell">${formatDateShort(p.matchDate)}</td>
                <td class="bookmaker-cell">${esc(p.bookmaker)}</td>
                <td><span class="outcome-badge ${isDraw ? 'draw' : ''}">${isDraw ? 'Empate' : esc(p.outcome)}</span></td>
                <td class="odds-cell">${p.oddPrice.toFixed(2)}</td>
                <td>${miniProbBarsHTML(p)}</td>
                <td><span class="risk-badge ${bCls}"><span class="rdot"></span>${sign}${p.benefitRiskIndex.toFixed(3)}</span></td>`;

            dom.tableBody.appendChild(tr);
        });

        renderPagination(totalPages);
    }

    function miniProbBarsHTML(p) {
        const rows = [
            { lbl: 'L', val: p.probHome, cls: 'home' },
            { lbl: 'E', val: p.probDraw, cls: 'draw' },
            { lbl: 'V', val: p.probAway, cls: 'away' },
        ];
        return `<div class="prob-mini">${rows.map(r => {
            const pct = (r.val * 100).toFixed(1);
            return `<div class="prob-mini-row">
                        <span class="prob-mini-lbl">${r.lbl}</span>
                        <div class="prob-mini-track">
                            <div class="prob-mini-fill ${r.cls}" style="width:${pct}%"></div>
                        </div>
                        <span class="prob-mini-val">${pct}%</span>
                    </div>`;
        }).join('')}</div>`;
    }

    function renderPagination(totalPages) {
        if (!dom.pagination) return;
        if (totalPages <= 1) {
            dom.pagination.classList.add('hidden');
            return;
        }
        dom.pagination.classList.remove('hidden');
        dom.pageInfo.textContent  = `${currentPage} / ${totalPages}`;
        dom.btnPrev.disabled = currentPage === 1;
        dom.btnNext.disabled = currentPage === totalPages;
    }

    function bindPagination() {
        dom.btnPrev?.addEventListener('click', () => {
            if (currentPage > 1) { currentPage--; renderTable(allPredictions.slice(TOP_N)); scrollToTable(); }
        });
        dom.btnNext?.addEventListener('click', () => {
            const max = Math.ceil((allPredictions.length - TOP_N) / PAGE_SIZE);
            if (currentPage < max) { currentPage++; renderTable(allPredictions.slice(TOP_N)); scrollToTable(); }
        });
    }

    function scrollToTable() {
        const el = $('table-container');
        if (el) window.scrollTo({ top: el.offsetTop - 120, behavior: 'smooth' });
    }

    
    function updateStats(predictions) {
        const total    = predictions.length;
        const valCnt   = predictions.filter(p => p.benefitRiskIndex > 0).length;
        const best     = total > 0 ? Math.max(...predictions.map(p => p.benefitRiskIndex)) : 0;
        const avg      = total > 0 ? predictions.reduce((s, p) => s + p.benefitRiskIndex, 0) / total : 0;
        const sign     = v => (v >= 0 ? '+' : '');

        countUp(dom.statTotal,     total);
        countUp(dom.statValueBets, valCnt);

        dom.statBestIndex.textContent = sign(best) + best.toFixed(3);
        dom.statBestIndex.className   = 'stat-value mono ' + (best > 0 ? 'neon' : '');

        dom.statAvgIndex.textContent  = sign(avg) + avg.toFixed(3);
        dom.statAvgIndex.className    = 'stat-value mono ' + (avg > 0 ? 'neon' : avg < 0 ? 'violet' : '');
    }

    function countUp(el, target) {
        const from = parseInt(el.textContent) || 0;
        const dur  = 550;
        const t0   = performance.now();
        const step = t => {
            const p = Math.min((t - t0) / dur, 1);
            el.textContent = Math.round(from + (target - from) * (1 - Math.pow(1 - p, 3)));
            if (p < 1) requestAnimationFrame(step);
        };
        requestAnimationFrame(step);
    }

    
    function showLoading() {
        dom.emptyState.classList.add('hidden');
        dom.top5Section.classList.add('hidden');
        dom.tableContainer.classList.remove('hidden');
        dom.tableBody.innerHTML = `
            <tr class="loading-row">
                <td colspan="8">
                    <div class="spinner"></div>
                    <span>Cargando predicciones…</span>
                </td>
            </tr>`;
        dom.top5Grid.innerHTML = '';
    }

    function showEmpty() {
        dom.tableContainer.classList.add('hidden');
        dom.top5Section.classList.add('hidden');
        dom.emptyState.classList.remove('hidden');
        dom.resultCount.textContent   = '';
        dom.statTotal.textContent     = '0';
        dom.statValueBets.textContent = '0';
        dom.statBestIndex.textContent = '—';
        dom.statAvgIndex.textContent  = '—';
        if (donutChart) {
            donutChart.data.datasets[0].data = [0, 100];
            donutChart.update();
        }
        if (dom.donutCenter) dom.donutCenter.querySelector('.donut-pct').textContent = '—';
    }

    
    function setStatus(online) {
        dom.statusDot.className = `status-dot ${online ? 'online' : 'offline'}`;
        dom.statusText.textContent = online ? 'En línea' : 'Sin conexión';
    }

    
    function startAutoRefresh() {
        if (refreshTimer) clearInterval(refreshTimer);
        refreshTimer = setInterval(async () => {
            await loadFilters();
            await loadPredictions(true);
        }, REFRESH_MS);
    }

    
    function riskColorClass(idx) {
        if (idx >  0.15) return 'risk-pos';
        if (idx >= 0)    return 'risk-pos';
        if (idx > -0.15) return 'risk-neu';
        return 'risk-neg';
    }

    function riskBadgeClass(idx) {
        if (idx >  0.3)  return 'pos-strong';
        if (idx >  0)    return 'pos';
        if (idx > -0.3)  return 'neu';
        if (idx > -0.5)  return 'neg';
        return 'neg-strong';
    }

    function isDrawOutcome(outcome) {
        const o = (outcome || '').toLowerCase();
        return o === 'draw' || o === 'empate';
    }

    function formatDate(str) {
        if (!str) return '—';
        try {
            const d = new Date(str);
            if (isNaN(d)) return str;
            return d.toLocaleDateString('es-ES', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' });
        } catch { return str; }
    }

    function formatDateShort(str) {
        if (!str) return '—';
        try {
            const d = new Date(str);
            if (isNaN(d)) return str;
            return d.toLocaleDateString('es-ES', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' });
        } catch { return str; }
    }

    function esc(str) {
        if (!str) return '';
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }

    
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();