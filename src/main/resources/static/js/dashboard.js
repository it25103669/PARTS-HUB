/**
 * PartsHub Pro – dashboard.js
 *
 * Reads chart data JSON injected into data-chart-json attributes by
 * DashboardController (via Thymeleaf), then initialises each Chart.js chart.
 *
 * Chart.js is loaded from CDN in the HTML template.
 */

document.addEventListener('DOMContentLoaded', function () {

    // Shared Chart.js defaults for the dark theme
    Chart.defaults.color             = '#94A3B8';
    Chart.defaults.borderColor       = '#263244';
    Chart.defaults.font.family       = "'Inter', sans-serif";
    Chart.defaults.font.size         = 12;
    Chart.defaults.plugins.legend.labels.boxWidth = 12;

    /**
     * Reads the chart JSON from a canvas element's data attribute
     * and builds the Chart.js config object.
     */
    function initChart(canvasId) {
        const canvas = document.getElementById(canvasId);
        if (!canvas) return null;

        const raw  = canvas.getAttribute('data-chart-json');
        if (!raw)  return null;

        let data;
        try { data = JSON.parse(raw); } catch (e) { console.error('Chart parse error:', canvasId, e); return null; }

        const datasets = data.datasets.map(function (points, idx) {
            const label  = data.datasetLabels ? data.datasetLabels[idx] : 'Dataset ' + (idx + 1);
            const color  = data.backgroundColors ? data.backgroundColors[idx % data.backgroundColors.length] : '#3B82F6';
            const base   = {
                label:           label,
                data:            points,
                backgroundColor: data.chartType === 'line' ? 'rgba(59,130,246,0.1)' : color,
                borderColor:     color,
                borderWidth:     data.chartType === 'line' ? 2 : 0,
                borderRadius:    data.chartType === 'bar'  ? 4 : 0,
                tension:         0.4,
                fill:            data.chartType === 'line',
                pointRadius:     data.chartType === 'line' ? 3 : 0,
                hoverOffset:     6
            };
            // Doughnut/pie: use all background colours for segments
            if (data.chartType === 'doughnut' || data.chartType === 'pie') {
                base.backgroundColor = data.backgroundColors || ['#2563EB', '#22C55E', '#EF4444'];
                base.borderColor     = '#0B0F14';
                base.borderWidth     = 3;
            }
            return base;
        });

        // For multi-dataset bar charts, colour each dataset separately
        if (data.chartType === 'bar' && data.datasets.length > 1) {
            datasets.forEach(function (ds, idx) {
                const color = data.backgroundColors ? data.backgroundColors[idx % data.backgroundColors.length] : '#3B82F6';
                ds.backgroundColor = color + 'CC'; // slight transparency
                ds.borderColor     = color;
                ds.borderWidth     = 1;
            });
        }

        const isHorizontal = canvasId === 'topProductsChart';

        const config = {
            type:    data.chartType === 'bar' && isHorizontal ? 'bar' : data.chartType,
            data:    { labels: data.labels, datasets: datasets },
            options: {
                indexAxis:   isHorizontal ? 'y' : 'x',
                responsive:  true,
                maintainAspectRatio: false,
                interaction: { mode: 'index', intersect: false },
                plugins: {
                    legend: {
                        display: data.chartType === 'doughnut' || data.datasets.length > 1,
                        position: 'bottom'
                    },
                    tooltip: {
                        backgroundColor: '#1B2430',
                        borderColor:     '#263244',
                        borderWidth:     1,
                        titleColor:      '#F8FAFC',
                        bodyColor:       '#94A3B8',
                        padding:         10
                    }
                },
                scales: (data.chartType === 'doughnut' || data.chartType === 'pie') ? {} : {
                    x: {
                        grid: { color: 'rgba(38,50,68,0.5)' },
                        ticks: { color: '#64748B', maxRotation: 45 }
                    },
                    y: {
                        grid: { color: 'rgba(38,50,68,0.5)' },
                        ticks: { color: '#64748B' },
                        beginAtZero: true
                    }
                }
            }
        };

        return new Chart(canvas, config);
    }

    // Initialise all six dashboard charts
    initChart('salesChart');
    initChart('stockChart');
    initChart('purchaseVsSalesChart');
    initChart('topProductsChart');
    initChart('supplierChart');
    initChart('returnsChart');
});
