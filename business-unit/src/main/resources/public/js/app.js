(() => {
    'use strict';

    const API_BASE = '';
    const REFRESH_MS = 30_000;
    const PAGE_SIZE = 15;
    const TOP_N = 5;

    let currentView = 'global';
    let filtersData = { teams: [], bookmakers: [] };
    let allPredictions = [];
    let matchGroups = [];
    let currentPage = 1;
    let refreshTimer = null;
    let chartsReady = false;
    let charts = { primary: null, secondary: null };

    const THEME = {
        amber: '#f59e0b',
        amberDim: 'rgba(245, 158, 11, 0.2)',
        cyan: '#06b6d4',
        cyanDim: 'rgba(6, 182, 212, 0.2)',
        rose: '#f43f5e',
        roseDim: 'rgba(244, 63, 94, 0.2)',
        slate: '#94a3b8',
        slateDim: 'rgba(148, 163, 184, 0.2)',
        surface: 'rgba(10, 10, 19, 0.93)',
        grid: 'rgba(255, 255, 255, 0.05)',
        textMain: '#f0f0f7',
        textMuted: '#6b6b8c'
    };

    const $ = id => document.getElementById(id);
    const $q = sel => document.querySelector(sel);

    const dom = {
        tabs: document.querySelectorAll('.module-card'),
        filterBar: $('filter-bar'),
        filterLabel: $('filter-label'),
        filterSelect: $('filter-select'),
        filterTrigger: $('filter-trigger'),
        filterTriggerText: $('filter-trigger-text'),
        filterMenu: $('filter-menu'),
        tableBody: $('table-body'),
        tableTitle: $('table-title'),
        tableRange: $('table-range'),
        resultCount: $('result-count'),
        tableContainer: $('table-container'),
        emptyState: $('empty-state'),
        top5Section: $('top5-section'),
        top5Grid: $('top5-grid'),
        statTotal: $q('#stat-total .stat-value'),
        statValueBets: $q('#stat-value-bets .stat-value'),
        statBestIndex: $q('#stat-best-index .stat-value'),
        statAvgIndex: $q('#stat-avg-index .stat-value'),
        statusDot: $('status-dot'),
        statusText: $('status-text'),
        lastUpdate: $('last-update'),
        pagination: $('pagination-controls'),
        btnPrev: $('btn-prev-page'),
        btnNext: $('btn-next-page'),
        pageInfo: $('page-info'),
        primaryTitle: $('primary-chart-title'),
        primarySubtitle: $('primary-chart-subtitle'),
        secondaryTitle: $('secondary-chart-title'),
        secondarySubtitle: $('secondary-chart-subtitle'),
        oddsModal: $('odds-modal'),
        modalClose: $('modal-close'),
        modalMatchTitle: $('modal-match-title'),
        modalOddsList: $('modal-odds-list'),
        sectionBadge: $('section-badge'),
    };

    const SVG = {
        calendar: `<svg class="meta-svg" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="3" width="14" height="12" rx="2"/><path d="M1 7h14M5 1v4M11 1v4"/></svg>`,
        building: `<svg class="meta-svg" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M2 14h12M3 14V7m10 7V7M1 7h14L8 2 1 7z"/><rect x="6" y="10" width="4" height="4" stroke-width="1.25"/></svg>`,
        calendarDate: `<svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" style="width:11px;height:11px;flex-shrink:0;stroke:var(--t3)"><rect x="1" y="3" width="14" height="12" rx="2"/><path d="M1 7h14M5 1v4M11 1v4"/></svg>`,
    };

    async function init() {
        document.body.dataset.view = currentView;
        bindNavigation();
        bindFilterChange();
        bindCustomSelect();
        bindPagination();
        bindModal();
        animateInitialLayout();

        waitForChartJs(() => {
            initChartDefaults();
            chartsReady = true;
            if (allPredictions.length) updateCharts(allPredictions);
        });

        await loadFilters();
        await loadPredictions();
        startAutoRefresh();
    }

    function waitForChartJs(cb) {
        const isReady = () => {
            try {
                return typeof Chart !== 'undefined';
            } catch (err) {
                return false;
            }
        };

        if (isReady()) return cb();
        const t = setInterval(() => {
            if (isReady()) { clearInterval(t); cb(); }
        }, 50);
    }

    function initChartDefaults() {
        Chart.defaults.color = THEME.textMuted;
        Chart.defaults.font.family = "'JetBrains Mono', monospace";
        Chart.defaults.plugins.tooltip.backgroundColor = THEME.surface;
        Chart.defaults.plugins.tooltip.borderColor = 'rgba(255,255,255,0.08)';
        Chart.defaults.plugins.tooltip.borderWidth = 1;
        Chart.defaults.plugins.tooltip.padding = 10;
        Chart.defaults.plugins.tooltip.titleFont = { family: "'Syne', sans-serif", size: 14, weight: '700' };
        Chart.defaults.plugins.tooltip.bodyFont = { family: "'JetBrains Mono', monospace", size: 12 };

        const centerTextPlugin = {
            id: 'centerText',
            beforeDraw: function (chart) {
                if (chart.config.options.elements && chart.config.options.elements.center) {
                    const ctx = chart.ctx;
                    const centerConfig = chart.config.options.elements.center;
                    ctx.save();
                    ctx.font = "bold 46px 'Syne', sans-serif";
                    ctx.fillStyle = centerConfig.color || THEME.amber;
                    ctx.textAlign = 'center';
                    ctx.textBaseline = 'middle';
                    const centerX = (chart.chartArea.left + chart.chartArea.right) / 2;
                    const centerY = (chart.chartArea.top + chart.chartArea.bottom) / 2;
                    ctx.fillText(centerConfig.text, centerX, centerY - 4);

                    if (centerConfig.subText) {
                        ctx.font = "600 12px 'Syne', sans-serif";
                        ctx.fillStyle = THEME.textMuted;
                        ctx.fillText(centerConfig.subText, centerX, centerY + 28);
                    }
                    ctx.restore();
                }
            }
        };
        Chart.register(centerTextPlugin);
    }

    function bindNavigation() {
        dom.tabs.forEach(item => {
            item.addEventListener('click', () => {
                const view = item.dataset.view;
                if (!view || view === currentView) return;
                switchView(view);
            });
        });
    }

    function switchView(view) {
        currentView = view;
        currentPage = 1;
        syncActiveView();
        document.body.dataset.view = view;

        const isFiltered = view !== 'global';
        dom.filterBar.classList.toggle('hidden', !isFiltered);
        if (isFiltered) populateFilterSelect(view);
        if (!isFiltered) {
            dom.filterSelect.value = '';
            syncCustomSelect();
        }

        updateViewTitle();
        loadPredictions();
    }

    function syncActiveView() {
        dom.tabs.forEach(card => card.classList.toggle('active', card.dataset.view === currentView));
        animateViewSwitch();
    }

    function updateViewTitle() {
        const selected = dom.filterSelect.value;
        const titles = {
            global: 'Cuotas recomendadas',
            team: selected ? `Cuotas recomendadas para ${selected}` : 'Cuotas recomendadas por equipo',
            bookmaker: selected ? `Cuotas recomendadas para ${selected}` : 'Cuotas recomendadas por casa',
        };
        dom.tableTitle.textContent = titles[currentView] || titles.global;
    }

    async function loadFilters() {
        try {
            const res = await fetch(`${API_BASE}/api/filters`);
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            filtersData = await res.json();
        } catch (err) {
            filtersData = { teams: ['Real Madrid', 'FC Barcelona', 'Sevilla', 'Athletic Club'], bookmakers: ['Bet365', 'Bwin', '1xBet'] };
        }
    }

    function populateFilterSelect(view) {
        const current = dom.filterSelect.value;
        const items = view === 'team' ? filtersData.teams : filtersData.bookmakers;
        dom.filterLabel.textContent = view === 'team' ? 'Equipo:' : 'Casa de Apuestas:';
        dom.filterSelect.innerHTML = '';
        items.forEach(item => {
            const opt = Object.assign(document.createElement('option'), { value: item, textContent: item });
            dom.filterSelect.appendChild(opt);
        });
        dom.filterSelect.value = items.includes(current) ? current : (items[0] || '');
        renderCustomOptions(items);
        syncCustomSelect();
    }

    function bindFilterChange() {
        dom.filterSelect.addEventListener('change', () => {
            currentPage = 1;
            syncCustomSelect();
            updateViewTitle();
            loadPredictions();
        });
    }

    function bindCustomSelect() {
        dom.filterTrigger?.addEventListener('click', () => setCustomSelectOpen(dom.filterTrigger.getAttribute('aria-expanded') !== 'true'));
        document.addEventListener('click', e => { if (!e.target.closest('.select-wrapper')) setCustomSelectOpen(false); });
    }

    function renderCustomOptions(items) {
        if (!dom.filterMenu) return;
        dom.filterMenu.innerHTML = '';
        items.forEach((item, i) => {
            const option = document.createElement('button');
            option.type = 'button';
            option.className = 'custom-select-option';
            option.dataset.value = item;
            option.innerHTML = `<span class="custom-option-index mono">${String(i + 1).padStart(2, '0')}</span><span class="custom-option-label">${esc(item)}</span>`;
            option.addEventListener('click', () => { dom.filterSelect.value = item; setCustomSelectOpen(false); dom.filterSelect.dispatchEvent(new Event('change')); });
            dom.filterMenu.appendChild(option);
        });
    }

    function syncCustomSelect() {
        if (!dom.filterTriggerText) return;
        const selected = dom.filterSelect.value;
        dom.filterTriggerText.textContent = selected || 'Selecciona una opción';
        dom.filterMenu?.querySelectorAll('.custom-select-option').forEach(opt => opt.classList.toggle('selected', opt.dataset.value === selected));
    }

    function setCustomSelectOpen(open) {
        dom.filterTrigger?.setAttribute('aria-expanded', String(open));
        dom.filterMenu?.classList.toggle('hidden', !open);
        dom.filterTrigger?.classList.toggle('open', open);
    }

    async function loadPredictions(isRefresh = false) {
        if (!isRefresh) showLoading();

        try {
            const res = await fetch(buildUrl());
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            allPredictions = await res.json();
            setStatus(true);
        } catch (err) {
            allPredictions = generateMockData(currentView, dom.filterSelect.value);
            setStatus(false);
        }

        matchGroups = groupByMatch(allPredictions);

        if (!allPredictions.length) {
            showEmpty();
        } else {
            const topLimit = currentView === 'team' ? 3 : TOP_N;
            const topSlice = matchGroups.slice(0, topLimit);
            updateStats(allPredictions);
            updateCharts(allPredictions);
            renderTop5(topSlice);

            if (dom.sectionBadge) {
                dom.sectionBadge.textContent = `TOP ${topSlice.length}`;
            }
            renderTable(matchGroups.slice(topLimit));

            dom.resultCount.textContent = `${matchGroups.length} partidos · ${allPredictions.length} predicciones`;
            dom.top5Section.classList.remove('hidden');
            dom.emptyState.classList.add('hidden');
            if (!isRefresh) animateDataBlocks();
        }

        dom.lastUpdate.textContent = `Actualizado: ${new Date().toLocaleTimeString('es-ES')}`;
    }

    function buildUrl() {
        const selected = dom.filterSelect.value;
        let url = `${API_BASE}/api/predictions`;
        if (currentView === 'team' && selected) url += `?team=${encodeURIComponent(selected)}`;
        if (currentView === 'bookmaker' && selected) url += `?bookmaker=${encodeURIComponent(selected)}`;
        return url;
    }

    function updateStats(predictions) {
        const total = predictions.length;
        const valCnt = predictions.filter(p => p.benefitRiskIndex > 0.2).length;
        const best = total ? Math.max(...predictions.map(p => p.benefitRiskIndex)) : 0;
        const avgVal = total ? predictions.reduce((s, p) => s + p.benefitRiskIndex, 0) / total : 0;
        const sign = v => (parseFloat(v.toFixed(3)) > 0 ? '+' : '');

        countUp(dom.statTotal, total, 0);
        countUp(dom.statValueBets, valCnt, 0);
        setSignedStat(dom.statBestIndex, best, sign(best) + best.toFixed(3));
        setSignedStat(dom.statAvgIndex, avgVal, sign(avgVal) + avgVal.toFixed(3));
    }

    function updateCharts(predictions) {
        if (!chartsReady) return;
        destroyCharts();

        if (currentView === 'global') renderGlobalCharts(predictions);
        else if (currentView === 'team') renderTeamCharts(predictions);
        else if (currentView === 'bookmaker') renderBookmakerCharts(predictions);
    }

    function getRiskColor(idx) {
        if (idx > 0.2) return THEME.amber;
        if (idx >= -0.2) return THEME.slate;
        return THEME.rose;
    }

    function setChartCopy(pT, pS, sT, sS) {
        dom.primaryTitle.textContent = pT; dom.primarySubtitle.textContent = pS;
        dom.secondaryTitle.textContent = sT; dom.secondarySubtitle.textContent = sS;
    }

    function renderGlobalCharts(predictions) {
        setChartCopy(
            'Mejores Casas de Apuestas', 'Ganancia esperada según la casa de apuestas',
            'Distribución de Value Bets', 'Volumen de Value Bets detectadas en el mercado'
        );

        const bookmakerRisk = avgEntries(predictions, p => p.bookmaker, p => p.benefitRiskIndex)
            .sort((a, b) => b.value - a.value);

        // Mostrar solo los 10 mejores para coincidir con el título "Mejores Casas"
        const top10 = bookmakerRisk.slice(0, 10);

        charts.primary = new Chart($('primary-chart'), horizontalBarConfig(
            top10.map(e => e.label), top10.map(e => fixed(e.value)), 'Ventaja Matemática'
        ));

        const valueCnt = predictions.filter(p => p.benefitRiskIndex > 0.2).length;
        const total = predictions.length;
        const pct = total > 0 ? Math.round((valueCnt / total) * 100) : 0;
        charts.secondary = new Chart($('secondary-chart'), doughnutConfig(
            ['Value Bets', 'Mercado sin Ventaja'], [valueCnt, total - valueCnt], [THEME.amber, THEME.slate],
            `${pct}%`, 'VALUE BETS'
        ));
    }

    function renderTeamCharts(predictions) {
        const team = dom.filterSelect.value || 'Seleccionado';
        setChartCopy(
            'Volumen de Cuotas por Resultado', 'Cantidad de cuotas analizadas a favor del Local, Empate o Visitante',
            'Predicción vs Casa de Apuestas', 'Porcentajes de acierto por resultado'
        );

        const stackData = (oStr) => {
            const preds = predictions.filter(p => (oStr === 'E' ? isDrawOutcome(p.outcome) : (oStr === 'L' ? p.outcome === p.homeTeam : p.outcome === p.awayTeam)));
            return {
                pos: preds.filter(p => p.benefitRiskIndex > 0.2).length,
                neu: preds.filter(p => p.benefitRiskIndex <= 0.2 && p.benefitRiskIndex >= -0.2).length,
                neg: preds.filter(p => p.benefitRiskIndex < -0.2).length
            };
        };
        const dL = stackData('L'), dE = stackData('E'), dV = stackData('V');

        charts.primary = new Chart($('primary-chart'), stackedBarConfig(
            ['Victoria', 'Empate', 'Derrota'],
            [dL.pos, dE.pos, dV.pos], [dL.neu, dE.neu, dV.neu], [dL.neg, dE.neg, dV.neg]
        ));

        const getProbs = (outcomeStr) => {
            const preds = predictions.filter(p => {
                if (outcomeStr === 'E') return isDrawOutcome(p.outcome);
                if (outcomeStr === 'L') return p.outcome === p.homeTeam;
                return p.outcome === p.awayTeam;
            });
            if (!preds.length) return { mod: 0, casa: 0 };
            return {
                mod: avg(preds, p => modelProbabilityForOutcome(p)) * 100,
                casa: avg(preds, p => (1 / p.oddPrice)) * 100
            };
        };

        const local = getProbs('L'), empate = getProbs('E'), visitante = getProbs('V');
        charts.secondary = new Chart($('secondary-chart'), groupedBarConfig(
            ['Victoria', 'Empate', 'Derrota'],
            [fixed(local.mod), fixed(empate.mod), fixed(visitante.mod)],
            [fixed(local.casa), fixed(empate.casa), fixed(visitante.casa)]
        ));
    }

    function renderBookmakerCharts(predictions) {
        setChartCopy(
            'Partidos Más Rentables', 'Partidos con mayor ventaja para apostar',
            'Evolución de las Oportunidades', 'Cambios de las mejores cuotas a lo largo de los días'
        );

        const byMatch = avgEntries(predictions, p => matchLabel(p), p => p.benefitRiskIndex)
            .sort((a, b) => b.value - a.value).slice(0, 10);
        charts.primary = new Chart($('primary-chart'), horizontalBarConfig(
            byMatch.map(e => e.label), byMatch.map(e => fixed(e.value)), 'Ventaja'
        ));

        const byDate = [...predictions].sort((a, b) => new Date(a.matchDate) - new Date(b.matchDate));
        const timeData = maxEntries(byDate, p => formatDateShort(p.matchDate), p => p.benefitRiskIndex).slice(0, 15);

        charts.secondary = new Chart($('secondary-chart'), lineConfig(
            timeData.map(d => d.label),
            timeData.map(d => fixed(d.value)),
            timeData.map(d => d.match)
        ));
    }

    function destroyCharts() {
        Object.keys(charts).forEach(key => {
            if (charts[key]) charts[key].destroy();
            charts[key] = null;
        });
    }


    function lineConfig(labels, data, matches = []) {
        return {
            type: 'line',
            data: {
                labels,
                datasets: [{
                    label: 'Ventaja', data,
                    borderColor: THEME.amber, backgroundColor: THEME.amberDim,
                    fill: true, tension: 0.3, pointBackgroundColor: data.map(v => getRiskColor(v)),
                    matches
                }]
            },
            options: {
                responsive: true, maintainAspectRatio: false,
                interaction: { intersect: false, mode: 'index' },
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        enabled: false,
                        external: (context) => {
                            const canvas = context.chart.canvas;
                            const container = canvas.parentElement;
                            let tooltipEl = container.querySelector('.chart-tooltip');

                            if (!tooltipEl) {
                                tooltipEl = document.createElement('div');
                                tooltipEl.className = 'chart-tooltip';
                                container.appendChild(tooltipEl);
                            }

                            const tooltipModel = context.tooltip;
                            if (tooltipModel.opacity === 0) {
                                tooltipEl.style.display = 'none';
                                return;
                            }

                            if (tooltipModel.body) {
                                const dataPoint = tooltipModel.dataPoints[0];
                                const match = dataPoint.dataset.matches[dataPoint.dataIndex];
                                if (match) {
                                    tooltipEl.innerHTML = `
                                        <div class="tooltip-header">${dataPoint.label}</div>
                                        <div class="tooltip-match">
                                            <div class="tooltip-team">${teamLogoHTML(match.homeTeam)}<span>${esc(match.homeTeam)}</span></div>
                                            <div class="tooltip-vs">VS</div>
                                            <div class="tooltip-team">${teamLogoHTML(match.awayTeam)}<span>${esc(match.awayTeam)}</span></div>
                                        </div>
                                        <div class="tooltip-value">
                                            <span class="tooltip-lbl">VALOR</span>
                                            <span class="tooltip-val">${fixed(match.benefitRiskIndex)}</span>
                                        </div>
                                    `;
                                }
                            }

                            tooltipEl.style.display = 'block';
                            tooltipEl.style.left = tooltipModel.caretX + 'px';
                            tooltipEl.style.top = tooltipModel.caretY + 'px';
                        }
                    }
                },
                scales: {
                    x: { grid: { color: THEME.grid }, ticks: { color: THEME.textMuted, font: { size: 10 } } },
                    y: { grid: { color: THEME.grid }, ticks: { color: THEME.textMuted } }
                }
            }
        };
    }

    function groupedBarConfig(labels, dataMod, dataCasa) {
        return {
            type: 'bar',
            data: {
                labels,
                datasets: [
                    { label: 'Predicción (%)', data: dataMod, backgroundColor: THEME.amber, borderRadius: 3 },
                    { label: 'Casas de apuestas (%)', data: dataCasa, backgroundColor: THEME.slate, borderRadius: 3 }
                ]
            },
            options: {
                responsive: true, maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'top',
                        padding: 80,
                        labels: { color: THEME.textMuted, usePointStyle: true }
                    }
                },
                scales: {
                    x: { grid: { display: false }, ticks: { color: THEME.textMuted } },
                    y: { grid: { color: THEME.grid }, ticks: { color: THEME.textMuted } }
                }
            }
        };
    }

    function stackedBarConfig(labels, dPos, dNeu, dNeg) {
        return {
            type: 'bar',
            data: {
                labels,
                datasets: [
                    { label: 'Value Bets', data: dPos, backgroundColor: THEME.amber },
                    { label: 'Cuotas Neutras', data: dNeu, backgroundColor: THEME.slate },
                    { label: 'Cuotas Desfavorables', data: dNeg, backgroundColor: THEME.rose }
                ]
            },
            options: {
                responsive: true, maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'top',
                        padding: 80,
                        labels: { color: THEME.textMuted, usePointStyle: true }
                    }
                },
                scales: {
                    x: { stacked: true, grid: { display: false }, ticks: { color: THEME.textMuted } },
                    y: {
                        stacked: true,
                        grid: { color: THEME.grid },
                        ticks: { color: THEME.textMuted },
                        grace: '15%',
                        title: { display: true, text: 'Cantidad de Cuotas', color: THEME.textMuted, font: { size: 12, family: "'Syne', sans-serif" } }
                    }
                }
            }
        };
    }

    function horizontalBarConfig(labels, data, label) {
        return {
            type: 'bar',
            data: {
                labels,
                datasets: [{
                    label, data,
                    backgroundColor: data.map(v => v >= 0 ? THEME.amber : THEME.rose),
                    borderRadius: 4,
                }],
            },
            options: axisOptions('y', label, true),
        };
    }

    function doughnutConfig(labels, data, colors, centerText, centerSubText) {
        return {
            type: 'doughnut',
            data: { labels, datasets: [{ data, backgroundColor: colors, borderColor: THEME.surface, borderWidth: 2 }] },
            options: {
                responsive: true, maintainAspectRatio: false, cutout: '76%',
                plugins: { legend: { position: 'bottom', labels: { color: THEME.textMuted, usePointStyle: true } } },
                elements: { center: { text: centerText, subText: centerSubText, color: THEME.amber } }
            }
        };
    }

    function axisOptions(indexAxis, label, signed) {
        return {
            indexAxis, responsive: true, maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                x: { grid: { color: THEME.grid }, ticks: { color: THEME.textMuted } },
                y: { grid: { color: indexAxis === 'y' ? 'transparent' : THEME.grid }, ticks: { color: THEME.textMuted } },
            }
        };
    }

    function groupByMatch(predictions) {
        const map = {};
        predictions.forEach(p => {
            const day = p.matchDate ? p.matchDate.slice(0, 10) : '';
            const key = `${p.homeTeam}|${p.awayTeam}|${day}`;
            if (!map[key]) map[key] = [];
            map[key].push(p);
        });

        return Object.values(map)
            .map(all => {
                all.sort((a, b) => b.benefitRiskIndex - a.benefitRiskIndex);
                return { best: all[0], all };
            })
            .sort((a, b) => b.best.benefitRiskIndex - a.best.benefitRiskIndex);
    }

    function renderTop5(groups) {
        dom.top5Grid.innerHTML = '';
        dom.top5Grid.dataset.count = groups.length;
        groups.forEach((group, i) => {
            const rank = i + 1; const p = group.best;
            const extra = group.all.length - 1;
            const isHero = (rank === 1 || (groups.length === 2 && rank === 2)) && currentView !== 'team';
            const isMainHero = rank === 1 && currentView !== 'team';

            const card = document.createElement('div');
            card.className = `top-card rank-${rank}${isHero ? ' hero-style' : ''}${isMainHero ? ' main-hero' : ''}`;
            card.setAttribute('role', 'button'); card.setAttribute('tabindex', '0');

            card.innerHTML = `
                <div class="card-rank-row">
                    <div class="rank-info">
                        <span class="rank-number">#${rank}</span>
                        ${isHero ? '<span class="rank-badge-hero">MEJOR OPCIÓN</span>' : ''}
                        ${extra > 0 ? `<span class="more-badge">${extra + 1} casas</span>` : ''}
                    </div>
                    <div class="card-meta-compact">
                        <span>${formatDate(p.matchDate)}</span>
                    </div>
                </div>

                <div class="card-match-compact">
                    <div class="compact-team">${teamLogoHTML(p.homeTeam)}<span>${esc(p.homeTeam)}</span></div>
                    <span class="vs-mini">vs</span>
                    <div class="compact-team">${teamLogoHTML(p.awayTeam)}<span>${esc(p.awayTeam)}</span></div>
                </div>

                <div class="bet-zone ${getOutcomeClass(p)}">
                    <div class="bet-team-info">
                        <span class="bet-tag">PREDICCIÓN RECOMENDADA</span>
                        <span class="bet-name">${isDrawOutcome(p.outcome) ? 'Empate' : esc(p.outcome)}</span>
                    </div>
                    <div class="bet-price-info">
                        <span class="bet-tag">${esc(p.bookmaker)} • CUOTA</span>
                        <span class="bet-value">${p.oddPrice.toFixed(2)}</span>
                    </div>
                </div>

                <div class="risk-highlight-zone">
                    <div class="risk-tag">ÍNDICE DE RIESGO</div>
                    <div class="risk-hero-value ${riskColorClass(p.benefitRiskIndex)}">
                        ${p.benefitRiskIndex >= 0 ? '+' : ''}${p.benefitRiskIndex.toFixed(3)}
                    </div>
                </div>

                <div class="prob-bars-footer">
                    ${probRowHTML('V', p.probHome, 'home')}
                    ${probRowHTML('E', p.probDraw, 'draw')}
                    ${probRowHTML('D', p.probAway, 'away')}
                </div>
                
                ${extra > 0 ? `<div class="card-cta">${isMainHero ? 'ANALIZAR MERCADO COMPLETO &rarr;' : 'Analizar mercado completo &rarr;'}</div>` : ''}`;

            card.addEventListener('click', () => openModal(group));
            dom.top5Grid.appendChild(card);
        });
    }

    function renderTable(groups) {
        dom.tableBody.innerHTML = '';
        if (!groups.length) return dom.tableContainer.classList.add('hidden');
        dom.tableContainer.classList.remove('hidden');

        const totalPages = Math.ceil(groups.length / PAGE_SIZE);
        currentPage = Math.max(1, Math.min(currentPage, totalPages));
        const items = groups.slice((currentPage - 1) * PAGE_SIZE, currentPage * PAGE_SIZE);

        dom.tableRange.textContent = `${((currentPage - 1) * PAGE_SIZE) + 1}-${((currentPage - 1) * PAGE_SIZE) + items.length} de ${groups.length} partidos`;

        items.forEach((group, i) => {
            const p = group.best;
            const globalRank = TOP_N + ((currentPage - 1) * PAGE_SIZE) + i + 1;
            const extra = group.all.length - 1;

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td class="rank-cell">${globalRank}</td>
                <td><div class="match-cell">
                    <div class="match-team-row">${teamLogoHTML(p.homeTeam)}<span class="match-home">${esc(p.homeTeam)}</span></div>
                    <span class="match-vs">vs</span>
                    <div class="match-team-row">${teamLogoHTML(p.awayTeam)}<span class="match-away">${esc(p.awayTeam)}</span></div>
                </div></td>
                <td class="date-cell">${formatDateShort(p.matchDate)}</td>
                <td class="bookmaker-cell">${esc(p.bookmaker)}${extra > 0 ? `<span class="more-count">+${extra}</span>` : ''}</td>
                <td><span class="outcome-badge ${getOutcomeClass(p)}">${isDrawOutcome(p.outcome) ? 'Empate' : esc(p.outcome)}</span></td>
                <td><span class="risk-badge ${riskBadgeClass(p.benefitRiskIndex)}"><span class="rdot"></span>${p.benefitRiskIndex >= 0 ? '+' : ''}${p.benefitRiskIndex.toFixed(3)}</span></td>
                <td><div class="prob-mini">${miniProbRowHTML('V', p.probHome, 'home')}${miniProbRowHTML('E', p.probDraw, 'draw')}${miniProbRowHTML('D', p.probAway, 'away')}</div></td>
                <td class="odds-cell">${p.oddPrice.toFixed(2)}</td>`;

            tr.addEventListener('click', () => openModal(group));
            dom.tableBody.appendChild(tr);
        });
        renderPagination(totalPages);
    }

    function probRowHTML(lbl, val, cls) {
        return `<div class="prob-row"><span class="prob-lbl">${lbl}</span><div class="prob-track"><div class="prob-fill ${cls}" style="width:${(val * 100).toFixed(1)}%"></div></div><span class="prob-val">${(val * 100).toFixed(1)}%</span></div>`;
    }
    function miniProbRowHTML(lbl, val, cls) {
        return `<div class="prob-mini-row"><span class="prob-mini-lbl">${lbl}</span><div class="prob-mini-track"><div class="prob-mini-fill ${cls}" style="width:${(val * 100).toFixed(1)}%"></div></div><span class="prob-mini-val">${(val * 100).toFixed(1)}%</span></div>`;
    }

    function renderPagination(totalPages) {
        if (!dom.pagination) return;
        dom.pagination.classList.toggle('hidden', totalPages <= 1);
        dom.pageInfo.textContent = `${currentPage} / ${totalPages}`;
        dom.btnPrev.disabled = currentPage === 1;
        dom.btnNext.disabled = currentPage === totalPages;
    }

    function bindPagination() {
        dom.btnPrev?.addEventListener('click', () => { if (currentPage > 1) { currentPage--; renderTable(matchGroups.slice(TOP_N)); } });
        dom.btnNext?.addEventListener('click', () => { if (currentPage < Math.ceil((matchGroups.length - TOP_N) / PAGE_SIZE)) { currentPage++; renderTable(matchGroups.slice(TOP_N)); } });
    }

    function openModal(group) {
        if (!dom.oddsModal) return;
        const p = group.best;
        dom.modalMatchTitle.innerHTML = `<div class="modal-home">${esc(p.homeTeam)}</div><div class="modal-vs">vs</div><div class="modal-away">${esc(p.awayTeam)}</div><div class="modal-date">${SVG.calendarDate} ${formatDate(p.matchDate)}</div>`;
        dom.modalOddsList.innerHTML = group.all.map((pred, i) => `
            <div class="modal-odds-row${i === 0 ? ' best' : ''}">
                ${i === 0 ? '<span class="modal-best-tag">MEJOR</span>' : ''}
                <div class="modal-bookmaker">${esc(pred.bookmaker)}</div>
                <span class="modal-outcome ${getOutcomeClass(pred)}">${isDrawOutcome(pred.outcome) ? 'Empate' : esc(pred.outcome)}</span>
                <span class="risk-badge ${riskBadgeClass(pred.benefitRiskIndex)}"><span class="rdot"></span>${pred.benefitRiskIndex >= 0 ? '+' : ''}${pred.benefitRiskIndex.toFixed(3)}</span>
                <div class="modal-odds-num">${pred.oddPrice.toFixed(2)}</div>
            </div>`).join('');
        dom.oddsModal.classList.remove('hidden'); document.body.style.overflow = 'hidden';
        animateModal();
    }

    function closeModal() { dom.oddsModal?.classList.add('hidden'); document.body.style.overflow = ''; }

    function bindModal() {
        dom.modalClose?.addEventListener('click', closeModal);
        dom.oddsModal?.addEventListener('click', e => { if (e.target === dom.oddsModal) closeModal(); });
        document.addEventListener('keydown', e => { if (e.key === 'Escape') closeModal(); });
    }

    function animateInitialLayout() {
        if (!window.anime) return;
        anime({ targets: '.hero-panel, .module-card, .chart-panel', opacity: [0, 1], translateY: [18, 0], delay: anime.stagger(70), duration: 760, easing: 'easeOutCubic' });
    }

    function animateViewSwitch() {
        if (!window.anime) return;
        anime({ targets: '#filter-bar:not(.hidden), .analytics-grid', opacity: [0.35, 1], translateY: [10, 0], duration: 420, easing: 'easeOutCubic' });
        anime({ targets: '.module-card.active .module-arrow', translateX: [0, 6, 0], duration: 520, easing: 'easeOutCubic' });
    }

    function animateDataBlocks() {
        if (!window.anime) return;
        anime({ targets: '.stat-card, .top-card, tbody tr', opacity: [0, 1], translateY: [10, 0], delay: anime.stagger(24), duration: 460, easing: 'easeOutCubic' });
    }

    function animateModal() {
        if (!window.anime) return;
        anime({ targets: '.modal-box', opacity: [0, 1], scale: [0.96, 1], translateY: [16, 0], duration: 320, easing: 'easeOutCubic' });
        anime({ targets: '.modal-odds-row', opacity: [0, 1], translateX: [12, 0], delay: anime.stagger(45), duration: 360, easing: 'easeOutCubic' });
    }

    function countUp(el, target) {
        const from = parseInt(el.textContent, 10) || 0;
        if (window.anime) {
            anime({ targets: el, innerHTML: [from, target], round: 1, easing: 'easeOutExpo', duration: 1000 });
        } else el.textContent = target;
    }

    function setSignedStat(el, value, text) {
        const rounded = parseFloat(value.toFixed(3));
        el.textContent = rounded === 0 ? '0.000' : text;
        el.className = `stat-value mono ${rounded > 0 ? 'neon' : rounded < 0 ? 'danger' : ''}`;
    }

    function showLoading() {
        dom.emptyState.classList.add('hidden'); dom.top5Section.classList.add('hidden'); dom.tableContainer.classList.remove('hidden');
        dom.tableBody.innerHTML = `<tr class="loading-row"><td colspan="8"><div class="spinner"></div><span>Calculando matrices de riesgo...</span></td></tr>`;
        dom.top5Grid.innerHTML = '';
    }

    function showEmpty() {
        matchGroups = []; dom.tableContainer.classList.add('hidden'); dom.top5Section.classList.add('hidden'); dom.emptyState.classList.remove('hidden');
        dom.resultCount.textContent = ''; dom.statTotal.textContent = '0'; dom.statValueBets.textContent = '0'; dom.statBestIndex.textContent = '-'; dom.statAvgIndex.textContent = '-';
        destroyCharts();
    }

    function setStatus(online) { dom.statusDot.className = `status-dot ${online ? 'online' : 'offline'}`; dom.statusText.textContent = online ? 'Data Syncing' : 'Local Mode'; }
    function startAutoRefresh() { if (refreshTimer) clearInterval(refreshTimer); refreshTimer = setInterval(() => loadPredictions(true), REFRESH_MS); }

    function avgEntries(items, keyFn, valFn) {
        const map = new Map();
        items.forEach(item => {
            const key = keyFn(item); if (!key) return;
            if (!map.has(key)) map.set(key, { sum: 0, n: 0 });
            const entry = map.get(key); entry.sum += valFn(item); entry.n++;
        });
        return [...map.entries()].map(([label, { sum, n }]) => ({ label, value: n ? sum / n : 0 }));
    }

    function avgEntriesWithItems(items, keyFn, valFn) {
        const map = new Map();
        items.forEach(item => {
            const key = keyFn(item); if (!key) return;
            if (!map.has(key)) map.set(key, { sum: 0, n: 0, first: item });
            const entry = map.get(key); entry.sum += valFn(item); entry.n++;
        });
        return [...map.entries()].map(([label, { sum, n, first }]) => ({ label, value: n ? sum / n : 0, first }));
    }

    function maxEntries(items, keyFn, valFn) {
        const map = new Map();
        items.forEach(item => {
            const key = keyFn(item); if (!key) return;
            const val = valFn(item);
            if (!map.has(key) || val > map.get(key).value) map.set(key, { value: val, match: item });
        });
        return [...map.entries()].map(([label, data]) => ({ label, value: data.value, match: data.match }));
    }

    function rivalName(p, team) { return (!team) ? `${p.homeTeam} vs ${p.awayTeam}` : (p.homeTeam === team ? p.awayTeam : p.homeTeam); }
    function matchLabel(p) { return `${p.homeTeam} vs ${p.awayTeam}`; }
    function modelProbabilityForOutcome(p) { return isDrawOutcome(p.outcome) ? p.probDraw : (p.outcome === p.homeTeam ? p.probHome : p.probAway); }
    function avg(items, fn) { return items.length ? items.reduce((s, item) => s + fn(item), 0) / items.length : 0; }
    function fixed(value) { return Number(value.toFixed(3)); }
    function riskColorClass(idx) { return idx > 0.2 ? 'risk-pos' : (idx >= -0.2 ? 'risk-neu' : 'risk-neg'); }
    function riskBadgeClass(idx) { return idx > 0.4 ? 'pos-strong' : (idx > 0.2 ? 'pos' : (idx >= -0.2 ? 'neu' : (idx >= -0.5 ? 'neg' : 'neg-strong'))); }
    function isDrawOutcome(outcome) { const o = (outcome || '').toLowerCase(); return o === 'draw' || o === 'empate' || o === 'x'; }
    function getOutcomeClass(p) {
        if (isDrawOutcome(p.outcome)) return 'draw';
        if (p.outcome === p.awayTeam) return 'away';
        return 'home';
    }
    function formatDate(str) { try { const d = new Date(str); return isNaN(d) ? str : d.toLocaleDateString('es-ES', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' }); } catch { return str; } }
    function formatDateShort(str) { try { const d = new Date(str); return isNaN(d) ? str : d.toLocaleDateString('es-ES', { day: '2-digit', month: 'short' }); } catch { return str; } }
    function esc(str) { const d = document.createElement('div'); d.textContent = str || ''; return d.innerHTML; }

    window.handleImgErr = function (img, teamName) {
        img.onerror = null;
        img.outerHTML = `<div class="team-logo-fallback">${esc(teamName.charAt(0))}</div>`;
    };
    function teamLogoHTML(teamName) {
        if (!teamName) return '';
        const fileName = teamName.toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "").replace(/[^a-z0-9]/g, "-").replace(/-+/g, "-").replace(/^-|-$/g, '');
        return `<img src="img/teams/${fileName}.svg" alt="" class="team-logo" onerror="handleImgErr(this, '${esc(teamName).replace(/'/g, "\\'")}')">`;
    }

    function generateMockData(view, filter) {
        const teams = ['Real Madrid', 'FC Barcelona', 'Sevilla', 'Athletic Club'];
        const data = [];
        for (let i = 0; i < 45; i++) {
            let h = view === 'team' ? filter : teams[i % 4];
            let a = teams[(i + 1) % 4];
            let probH = Math.random() * 0.6 + 0.2, probD = Math.random() * 0.3, probA = 1 - probH - probD;
            let oPrice = (1 / probH) + (Math.random() * 0.5 - 0.2);
            data.push({
                matchDate: new Date(Date.now() + i * 86400000).toISOString(),
                homeTeam: h, awayTeam: a, bookmaker: view === 'bookmaker' ? filter : ['Bet365', 'Bwin', '1xBet'][i % 3],
                outcome: i % 2 === 0 ? h : 'Draw', oddPrice: oPrice, probHome: probH, probDraw: probD, probAway: probA,
                benefitRiskIndex: (probH * oPrice) - 1 + (Math.random() * 0.4 - 0.2)
            });
        }
        return data;
    }

    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init); else init();
})();