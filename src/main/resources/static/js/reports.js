/**
 * PartsHub Pro – reports.js
 * Handles report type column hint display and filter form behaviour.
 */

document.addEventListener('DOMContentLoaded', function () {

    const reportTypeSelect = document.getElementById('reportType');
    const reportHint       = document.getElementById('reportTypeHint');

    const hints = {
        'INVENTORY':            'Columns: Product, Category, Current Qty, Reorder Level, Stock Status',
        'SALES':                'Columns: Date, Invoice #, Product, Qty, Unit Price, Total',
        'PURCHASE':             'Columns: PO Number, Supplier, Date, Status, Amount',
        'LOW_STOCK':            'Columns: Product, Category, Current Qty, Reorder Level, Urgency',
        'SUPPLIER_PERFORMANCE': 'Columns: Supplier, Total Orders, Completed, Avg Delivery Days, Reliability',
        'PRODUCT_PERFORMANCE':  'Columns: Product, Qty Sold, Revenue, Return Count, Performance',
        'GROSS_PROFIT':         'Columns: Product / Period, Sales Revenue, Purchase Cost, Est. Gross Profit'
    };

    if (reportTypeSelect && reportHint) {
        reportTypeSelect.addEventListener('change', function () {
            const val = this.value;
            reportHint.textContent = hints[val] || '';
        });
    }

    // ── Reset button ───────────────────────────────────────
    const resetBtn = document.getElementById('resetFiltersBtn');
    if (resetBtn) {
        resetBtn.addEventListener('click', function () {
            window.location.href = '/reports';
        });
    }

    // ── Real Export File Downloads ─────────────────────────
    const exportPdfBtn   = document.getElementById('exportPdfBtn');
    const exportExcelBtn = document.getElementById('exportExcelBtn');

    function triggerExport(format) {
        const type = reportTypeSelect ? reportTypeSelect.value : 'INVENTORY';
        const dateFrom = document.getElementById('dateFrom') ? document.getElementById('dateFrom').value : '';
        const dateTo = document.getElementById('dateTo') ? document.getElementById('dateTo').value : '';
        const productName = document.getElementById('productName') ? document.getElementById('productName').value : '';
        const supplierName = document.getElementById('supplierName') ? document.getElementById('supplierName').value : '';

        const params = new URLSearchParams({
            reportType: type,
            dateFrom: dateFrom,
            dateTo: dateTo,
            productName: productName,
            supplierName: supplierName
        });

        window.location.href = '/reports/export/' + format + '?' + params.toString();
    }

    if (exportPdfBtn) {
        exportPdfBtn.addEventListener('click', function (e) {
            e.preventDefault();
            triggerExport('pdf');
        });
    }
    if (exportExcelBtn) {
        exportExcelBtn.addEventListener('click', function (e) {
            e.preventDefault();
            triggerExport('excel');
        });
    }
});
