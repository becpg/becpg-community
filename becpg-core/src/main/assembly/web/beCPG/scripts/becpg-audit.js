/*******************************************************************************
 *  Copyright (C) 2010-2026 beCPG. 
 *   
 *  This file is part of beCPG 
 *   
 *  beCPG is free software: you can redistribute it and/or modify 
 *  it under the terms of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation, either version 3 of the License, or 
 *  (at your option) any later version. 
 *   
 *  beCPG is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 *  GNU Lesser General Public License for more details. 
 *   
 *  You should have received a copy of the GNU Lesser General Public License along with beCPG.
 *   If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

/**
 * beCPG Audit Viewer Component for Repository Admin Console.
 *
 * The audit table is read by keyset paging, newest entries first, a thousand entries per request:
 * a request scans a bounded number of entry identifier windows and hands back the identifier the
 * next one starts after. The entries read are shown a hundred per page; a page lying beyond them reads
 * the next thousand. Ordering on a column is applied on the entries read only.
 *
 * Every filter row must match. The server reads the database with the most selective one and
 * applies the other ones on the entries it reads.
 */
var AuditViewer = {
    currentTab: "formulation",
    filterRowCount: 0,
    sortBy: null,
    sortAsc: false,
    FETCH_SIZE: 1000,
    PAGE_SIZE: 100,
    fetchedRecords: [],
    lastRecords: [],
    nextStartAfterId: null,
    scanInterrupted: false,
    pageIndex: 0,
    requestSeq: 0,
    isLoading: false,
    serviceContext: "",
    msg: {},
    columns: {},

    init: function(config) {
        if (config) {
            this.serviceContext = config.serviceContext || "";
            this.msg = config.msg || {};
            this.columns = {};
            if (config.plugins) {
                for (var i = 0; i < config.plugins.length; i++) {
                    var p = config.plugins[i];
                    this.columns[p.id] = p.columns || [];
                }
            } else if (config.columns) {
                this.columns = config.columns;
            }
            this.defaultTab = config.defaultTab || (config.plugins && config.plugins.length > 0 ? config.plugins[0].id : "formulation");
        }
        this.selectTab(this.defaultTab || "formulation");
    },

    escapeHtml: function(str) {
        if (str === null || str === undefined) {
            return "";
        }
        var s = String(str);
        return s.replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/"/g, "&quot;")
                .replace(/'/g, "&#39;");
    },

    substitute: function(str, args) {
        if (!str) {
            return "";
        }
        var res = str;
        for (var i = 0; i < args.length; i++) {
            res = res.replace(new RegExp("\\{" + i + "\\}", "g"), args[i]);
        }
        return res;
    },

    getColumnLabel: function(colKey) {
        var cols = this.columns[this.currentTab] || [];
        for (var i = 0; i < cols.length; i++) {
            if (cols[i].key === colKey) {
                return cols[i].label;
            }
        }
        return colKey || "";
    },

    getColumnFormat: function(colKey) {
        var cols = this.columns[this.currentTab] || [];
        for (var i = 0; i < cols.length; i++) {
            if (cols[i].key === colKey) {
                return cols[i].format || "";
            }
        }
        return "";
    },

    isSortableColumn: function(colKey) {
        return !!colKey && this.getColumnFormat(colKey) !== "inspect";
    },

    selectTab: function(tabName) {
        this.currentTab = tabName;
        this.sortBy = null;
        this.sortAsc = false;

        var tabs = document.querySelectorAll(".audit-tab");
        for (var i = 0; i < tabs.length; i++) {
            tabs[i].className = tabs[i].className.replace(" active", "");
        }
        var activeTab = document.getElementById("tab-" + tabName);
        if (activeTab) {
            activeTab.className += " active";
        }

        this.resetFilterRows();
        this.renderHeader();
        this.search();
    },

    /**
     * The columns a filter row can match on: the ones holding a plain value. Dates are filtered
     * with the date range instead.
     */
    getFilterableColumns: function() {
        var cols = this.columns[this.currentTab] || [];
        var filterable = [];
        for (var i = 0; i < cols.length; i++) {
            var col = cols[i];
            if (col.key && col.key !== "id" && col.format !== "inspect" && col.format !== "date") {
                filterable.push(col);
            }
        }
        return filterable;
    },

    getFilterRows: function() {
        var container = document.getElementById("audit-filter-rows");
        return container ? container.querySelectorAll(".audit-filter-row") : [];
    },

    resetFilterRows: function() {
        var container = document.getElementById("audit-filter-rows");
        if (container) {
            container.innerHTML = "";
        }
        this.addFilterRow();
    },

    addFilterRow: function() {
        var container = document.getElementById("audit-filter-rows");
        if (!container) {
            return;
        }
        var rowId = this.filterRowCount++;
        container.insertAdjacentHTML("beforeend",
            '<div class="audit-filter-row" data-row-id="' + rowId + '">' +
            '<select onchange="AuditViewer.onFilterColumnChange(' + rowId + ');"></select>' +
            '<input type="text" disabled="disabled" placeholder="' + this.escapeHtml(this.msg.filterPlaceholder || "") + '"' +
            ' onkeypress="if(event.keyCode===13){AuditViewer.search();}" />' +
            '<button type="button" class="audit-filter-btn audit-filter-remove" title="' + this.escapeHtml(this.msg.removeFilter || "") + '"' +
            ' onclick="AuditViewer.removeFilterRow(' + rowId + ');">&times;</button>' +
            '<button type="button" class="audit-filter-btn audit-filter-add" title="' + this.escapeHtml(this.msg.addFilter || "") + '"' +
            ' onclick="AuditViewer.addFilterRow();">+</button>' +
            '</div>');
        this.refreshFilterColumnOptions();
    },

    getFilterRow: function(rowId) {
        var container = document.getElementById("audit-filter-rows");
        return container ? container.querySelector('.audit-filter-row[data-row-id="' + rowId + '"]') : null;
    },

    /**
     * Offer in each row the columns no other row filters on: two filters on the same column could
     * only match an entry holding both values, that is none.
     */
    refreshFilterColumnOptions: function() {
        var rows = this.getFilterRows();
        var cols = this.getFilterableColumns();
        for (var i = 0; i < rows.length; i++) {
            var select = rows[i].querySelector("select");
            var selected = select.value;
            var usedElsewhere = this.getSelectedFilterColumns(rows[i]);
            var html = '<option value="">--</option>';
            for (var j = 0; j < cols.length; j++) {
                if (!usedElsewhere[cols[j].key]) {
                    html += '<option value="' + this.escapeHtml(cols[j].key) + '">' + this.escapeHtml(cols[j].label) + '</option>';
                }
            }
            select.innerHTML = html;
            select.value = selected;
        }
    },

    getSelectedFilterColumns: function(excludedRow) {
        var rows = this.getFilterRows();
        var selected = {};
        for (var i = 0; i < rows.length; i++) {
            var colKey = rows[i].querySelector("select").value;
            if (rows[i] !== excludedRow && colKey) {
                selected[colKey] = true;
            }
        }
        return selected;
    },

    onFilterColumnChange: function(rowId) {
        var row = this.getFilterRow(rowId);
        if (!row) {
            return;
        }
        var col = row.querySelector("select").value;
        var input = row.querySelector("input");

        this.refreshFilterColumnOptions();
        if (!col) {
            input.value = "";
            input.disabled = true;
            this.search();
            return;
        }
        input.disabled = false;
        input.focus();
        if (input.value.trim().length > 0) {
            this.search();
        }
    },

    /**
     * Remove a filter row. The last row is emptied rather than removed, so that a filter can
     * always be entered.
     */
    removeFilterRow: function(rowId) {
        var row = this.getFilterRow(rowId);
        if (!row) {
            return;
        }
        if (this.getFilterRows().length > 1) {
            row.parentNode.removeChild(row);
            this.refreshFilterColumnOptions();
        } else {
            row.querySelector("select").value = "";
            row.querySelector("input").value = "";
            row.querySelector("input").disabled = true;
            this.refreshFilterColumnOptions();
        }
        this.search();
    },

    /**
     * Keep the date pickers from offering a reversed range, which the server rejects.
     */
    onDateRangeChange: function() {
        var fromInput = document.getElementById("audit-from-date-input");
        var toInput = document.getElementById("audit-to-date-input");
        if (fromInput && toInput) {
            toInput.min = fromInput.value;
            fromInput.max = toInput.value;
        }
        this.search();
    },

    /**
     * Order the retrieved records on a column. The values a business column holds are not
     * something the audit query can order on, so this stays a client side ordering.
     */
    sortByColumn: function(colKey) {
        if (this.isLoading || !this.isSortableColumn(colKey)) {
            return;
        }

        if (this.sortBy === colKey) {
            if (!this.sortAsc) {
                this.sortAsc = true;
            } else {
                this.sortBy = null;
                this.sortAsc = false;
            }
        } else {
            this.sortBy = colKey;
            this.sortAsc = false;
        }

        this.renderHeader();
        this.lastRecords = this.sortRecords(this.fetchedRecords);
        this.pageIndex = 0;
        this.renderPage(null);
    },

    sortRecords: function(records) {
        if (!records || !this.sortBy) {
            return records || [];
        }

        var self = this;
        var sortBy = this.sortBy;
        var format = this.getColumnFormat(sortBy);
        var factor = this.sortAsc ? 1 : -1;
        var sorted = records.slice(0);

        sorted.sort(function(left, right) {
            return factor * self.compareValues(left[sortBy], right[sortBy], format);
        });
        return sorted;
    },

    compareValues: function(left, right, format) {
        var isLeftEmpty = (left === null || left === undefined || left === "");
        var isRightEmpty = (right === null || right === undefined || right === "");

        if (isLeftEmpty || isRightEmpty) {
            if (isLeftEmpty && isRightEmpty) {
                return 0;
            }
            return isLeftEmpty ? -1 : 1;
        }
        if (format === "date") {
            return this.compareNumbers(new Date(left).getTime(), new Date(right).getTime());
        }
        if (format === "duration" || format === "size" || format === "errors") {
            return this.compareNumbers(parseFloat(left), parseFloat(right));
        }
        return this.compareStrings(String(left), String(right));
    },

    compareNumbers: function(left, right) {
        if (isNaN(left) || isNaN(right)) {
            return 0;
        }
        return left < right ? -1 : (left > right ? 1 : 0);
    },

    compareStrings: function(left, right) {
        var lower = left.toLowerCase();
        var other = right.toLowerCase();
        return lower < other ? -1 : (lower > other ? 1 : 0);
    },

    renderHeader: function() {
        var thead = document.getElementById("audit-table-head");
        if (!thead) {
            return;
        }

        var cols = this.columns[this.currentTab] || [];
        var html = "<tr>";
        for (var i = 0; i < cols.length; i++) {
            html += this.renderHeaderCell(cols[i]);
        }
        thead.innerHTML = html + "</tr>";
    },

    renderHeaderCell: function(col) {
        var style = col.width ? ' style="width:' + col.width + '"' : "";
        var isSortable = this.isSortableColumn(col.key);
        var thClass = isSortable ? "sortable" : "";
        var sortIcon = "";

        if (this.sortBy === col.key) {
            thClass += this.sortAsc ? " sorted-asc" : " sorted-desc";
            sortIcon = '<span class="sort-icon">' + (this.sortAsc ? "▲" : "▼") + '</span>';
        } else if (isSortable) {
            sortIcon = '<span class="sort-icon" style="opacity:0.25;">▲▼</span>';
        }

        var onclickAttr = isSortable ? ' onclick="AuditViewer.sortByColumn(\'' + col.key + '\')"' : "";
        return '<th class="' + thClass + '"' + style + onclickAttr + '>' + this.escapeHtml(col.label) + sortIcon + '</th>';
    },

    /**
     * Start a new search, from the newest entry of the audit application. The entries read by the
     * previous search are dropped.
     */
    search: function() {
        this.fetchedRecords = [];
        this.lastRecords = [];
        this.nextStartAfterId = null;
        this.scanInterrupted = false;
        this.pageIndex = 0;
        this.load(null, 0);
    },

    /**
     * Show the next page, reading the next thousand entries when the page lies beyond the ones
     * already read.
     */
    nextPage: function() {
        var nextPageIndex = this.pageIndex + 1;
        if (nextPageIndex * this.PAGE_SIZE < this.lastRecords.length) {
            this.pageIndex = nextPageIndex;
            this.renderPage(null);
        } else if (this.nextStartAfterId !== null) {
            this.load(this.nextStartAfterId, nextPageIndex);
        }
    },

    previousPage: function() {
        if (this.pageIndex > 0) {
            this.pageIndex--;
            this.renderPage(null);
        }
    },

    hasNextPage: function() {
        return ((this.pageIndex + 1) * this.PAGE_SIZE < this.lastRecords.length) || (this.nextStartAfterId !== null);
    },

    getFilterParams: function() {
        var rows = this.getFilterRows();
        var params = [];
        for (var i = 0; i < rows.length; i++) {
            var col = rows[i].querySelector("select").value;
            var val = rows[i].querySelector("input").value.trim();
            if (col && val) {
                params.push(col + "=" + val);
            }
        }
        return params;
    },

    /**
     * The bound of a date picker as an ISO 8601 instant: the start of the day picked for the lower
     * bound, its very end for the upper one, so that both days are included.
     */
    getDateBound: function(inputId, isEndOfDay) {
        var input = document.getElementById(inputId);
        var value = input ? input.value : "";
        var parts = value.split("-");
        if (parts.length !== 3) {
            return "";
        }
        var date = isEndOfDay
            ? new Date(parseInt(parts[0], 10), parseInt(parts[1], 10) - 1, parseInt(parts[2], 10), 23, 59, 59, 999)
            : new Date(parseInt(parts[0], 10), parseInt(parts[1], 10) - 1, parseInt(parts[2], 10));
        return isNaN(date.getTime()) ? "" : date.toISOString();
    },

    buildUrl: function(startAfterId) {
        var url = this.serviceContext + "/becpg/stats/" + encodeURIComponent(this.currentTab) +
                  "?maxResults=" + this.FETCH_SIZE +
                  "&dbAsc=false";

        var filterParams = this.getFilterParams();
        for (var i = 0; i < filterParams.length; i++) {
            url += "&filter=" + encodeURIComponent(filterParams[i]);
        }

        var fromDate = this.getDateBound("audit-from-date-input", false);
        if (fromDate) {
            url += "&fromDate=" + encodeURIComponent(fromDate);
        }
        var toDate = this.getDateBound("audit-to-date-input", true);
        if (toDate) {
            url += "&toDate=" + encodeURIComponent(toDate);
        }

        if (startAfterId !== null) {
            url += "&startAfterId=" + encodeURIComponent(startAfterId);
        }
        return url;
    },

    /**
     * Read the next thousand entries, then show the requested page. A response arriving after a
     * newer request was sent is dropped, so that a slow request never overwrites a newer search.
     */
    load: function(startAfterId, requestedPageIndex) {
        var self = this;
        var requestSeq = ++this.requestSeq;

        if (this.fetchedRecords.length === 0) {
            this.renderMessageRow(this.msg.loading || "Loading...", "audit-loading");
        }
        this.isLoading = true;
        this.updatePager();

        var startTime = new Date().getTime();

        Admin.request({
            url: this.buildUrl(startAfterId),
            method: "GET",
            fnSuccess: function(res) {
                if (requestSeq === self.requestSeq) {
                    self.onEntriesLoaded(res, requestedPageIndex, new Date().getTime() - startTime);
                }
            },
            fnFailure: function() {
                if (requestSeq === self.requestSeq) {
                    self.onEntriesFailed();
                }
            }
        });
    },

    /**
     * Add the entries read to the ones already read. The requested page is shown when it now holds
     * entries; a scan cut short may have read none, the current page then stays shown. When the
     * entries are ordered on a column, the new ones mix with the pages already shown: the first
     * page is shown again so that none of them is skipped.
     */
    onEntriesLoaded: function(res, requestedPageIndex, duration) {
        var json = {};
        try {
            json = res.responseJSON || (res.responseText ? JSON.parse(res.responseText) : {});
        } catch (e) {
            json = {};
        }

        this.isLoading = false;
        this.fetchedRecords = this.fetchedRecords.concat(json.statistics || []);
        this.nextStartAfterId = (typeof json.nextStartAfterId === "undefined") ? null : json.nextStartAfterId;
        this.scanInterrupted = (json.scanInterrupted === true);
        this.lastRecords = this.sortRecords(this.fetchedRecords);

        if (this.sortBy && requestedPageIndex > 0) {
            this.pageIndex = 0;
        } else if (requestedPageIndex * this.PAGE_SIZE < this.lastRecords.length) {
            this.pageIndex = requestedPageIndex;
        }
        this.renderPage(duration);
    },

    onEntriesFailed: function() {
        this.isLoading = false;
        if (this.fetchedRecords.length === 0) {
            this.renderMessageRow(this.msg.errorLoading || "Error loading audit entries", "audit-empty-msg audit-error-msg");
        }
        this.showNotice(this.msg.errorLoading || "Error loading audit entries");
        this.updatePager();

        var statusBar = document.getElementById("audit-status-bar");
        if (statusBar) {
            statusBar.textContent = "";
        }
    },

    renderPage: function(duration) {
        var offset = this.pageIndex * this.PAGE_SIZE;
        this.renderBody(this.lastRecords.slice(offset, offset + this.PAGE_SIZE), offset);
        this.updateNoticeBar();
        this.updateStatusBar(duration);
        this.updatePager();
    },

    renderMessageRow: function(text, cssClass) {
        var cols = this.columns[this.currentTab] || [];
        var tbody = document.getElementById("audit-table-body");
        if (tbody) {
            tbody.innerHTML = '<tr><td colspan="' + cols.length + '" class="' + cssClass + '">' + this.escapeHtml(text) + '</td></tr>';
        }
    },

    updateStatusBar: function(duration) {
        var statusBar = document.getElementById("audit-status-bar");
        if (!statusBar) {
            return;
        }
        var total = this.lastRecords.length;
        var first = total === 0 ? 0 : this.pageIndex * this.PAGE_SIZE + 1;
        var last = Math.min((this.pageIndex + 1) * this.PAGE_SIZE, total);
        statusBar.textContent = this.substitute(this.msg.recordsRange || "{0}-{1} / {2}", [first, last, total]) +
            (duration !== null ? " (" + duration + " ms)" : "");
    },

    updatePager: function() {
        var previousButton = document.getElementById("audit-previous-btn");
        var nextButton = document.getElementById("audit-next-btn");

        if (previousButton) {
            previousButton.disabled = this.isLoading || this.pageIndex === 0;
        }
        if (nextButton) {
            nextButton.disabled = this.isLoading || !this.hasNextPage();
        }
        this.renderPageNumbers();
    },

    /**
     * One button per page read, followed by an ellipsis while the server has entries left to read:
     * those pages only exist once "Next" has read them.
     */
    renderPageNumbers: function() {
        var container = document.getElementById("audit-page-numbers");
        if (!container) {
            return;
        }

        var pageCount = Math.max(1, Math.ceil(this.lastRecords.length / this.PAGE_SIZE));
        var html = "";
        for (var i = 0; i < pageCount; i++) {
            var cssClass = "audit-page-number" + (i === this.pageIndex ? " current" : "");
            var disabled = this.isLoading ? ' disabled="disabled"' : "";
            html += '<button type="button" class="' + cssClass + '"' + disabled + ' onclick="AuditViewer.goToPage(' + i + ');">' + (i + 1) + '</button>';
        }
        if (this.nextStartAfterId !== null) {
            html += '<span class="audit-page-more" title="' + this.escapeHtml(this.msg.moreToRead || "") + '">&hellip;</span>';
        }
        container.innerHTML = html;
    },

    goToPage: function(pageIndex) {
        if (!this.isLoading && pageIndex >= 0 && pageIndex * this.PAGE_SIZE < this.lastRecords.length) {
            this.pageIndex = pageIndex;
            this.renderPage(null);
        }
    },

    /**
     * The only notice worth showing: the last request stopped on the window budget of its scan
     * and the last page read is shown, so the next page has to be asked for to look further.
     */
    updateNoticeBar: function() {
        var isLastPageRead = (this.pageIndex + 1) * this.PAGE_SIZE >= this.lastRecords.length;
        if (this.scanInterrupted && isLastPageRead) {
            this.showNotice(this.substitute(this.msg.noticeInterrupted || "", [this.msg.next || ""]));
        } else {
            this.showNotice("");
        }
    },

    showNotice: function(text) {
        var noticeBar = document.getElementById("audit-notice-bar");
        if (!noticeBar) {
            return;
        }
        if (!text) {
            noticeBar.style.display = "none";
            noticeBar.innerHTML = "";
            return;
        }
        noticeBar.innerHTML = '<span style="font-weight:bold;margin-right:6px;">⚠️</span>' + this.escapeHtml(text);
        noticeBar.style.display = "block";
    },

    renderBody: function(records, offset) {
        var tbody = document.getElementById("audit-table-body");
        var cols = this.columns[this.currentTab] || [];
        if (!tbody) {
            return;
        }

        if (!records || records.length === 0) {
            tbody.innerHTML = '<tr><td colspan="' + cols.length + '" class="audit-empty-msg">' + (this.msg.noRecords || "No records found") + '</td></tr>';
            return;
        }

        var html = "";
        for (var i = 0; i < records.length; i++) {
            var row = records[i];
            html += "<tr>";
            for (var j = 0; j < cols.length; j++) {
                var col = cols[j];
                var rawVal = typeof row[col.key] !== "undefined" ? row[col.key] : "";
                html += "<td>" + this.formatValue(rawVal, col.format, row, offset + i, col.key) + "</td>";
            }
            html += "</tr>";
        }
        tbody.innerHTML = html;
    },

    formatValue: function(val, format, row, rowIndex, colKey) {
        if (val === null || val === undefined || val === "") {
            return '<span style="color:#bbb;">-</span>';
        }

        if (format === "date") {
            return this.formatDate(val);
        } else if (format === "duration") {
            return this.formatDuration(val);
        } else if (format === "size") {
            return this.formatSize(val);
        } else if (format === "completed") {
            var isComp = String(val) === "true";
            return isComp
                ? '<span class="badge badge-success">' + (this.msg.yes || "Yes") + '</span>'
                : '<span class="badge badge-warning">' + (this.msg.no || "No") + '</span>';
        } else if (format === "async") {
            var isAsync = String(val) === "true";
            return isAsync
                ? '<span class="badge badge-info">Async</span>'
                : '<span class="badge badge-gray">Sync</span>';
        } else if (format === "errors") {
            var num = parseInt(val, 10);
            if (num > 0) {
                return '<span class="badge badge-danger">' + num + '</span>';
            }
            return '<span class="badge badge-success">0</span>';
        } else if (format === "badge") {
            return '<span class="badge badge-gray">' + this.escapeHtml(String(val)) + '</span>';
        } else if (format === "noderef") {
            var nodeStr = String(val);
            return '<span class="noderef-cell" title="' + this.escapeHtml(nodeStr) + '">' + this.escapeHtml(nodeStr) + '</span>';
        } else if (format === "inspect") {
            return '<button type="button" class="btn-inspect" onclick="AuditViewer.openInspectModal(' + rowIndex + ', \'' + (colKey || "") + '\')">' + (this.msg.inspect || "Inspect") + '</button>';
        }

        return this.escapeHtml(String(val));
    },

    formatDate: function(val) {
        try {
            var d = new Date(val);
            if (isNaN(d.getTime())) {
                return this.escapeHtml(String(val));
            }
            var year = d.getFullYear();
            var month = (d.getMonth() + 1 < 10 ? "0" : "") + (d.getMonth() + 1);
            var day = (d.getDate() < 10 ? "0" : "") + d.getDate();
            var hours = (d.getHours() < 10 ? "0" : "") + d.getHours();
            var minutes = (d.getMinutes() < 10 ? "0" : "") + d.getMinutes();
            var seconds = (d.getSeconds() < 10 ? "0" : "") + d.getSeconds();
            return year + "-" + month + "-" + day + " " + hours + ":" + minutes + ":" + seconds;
        } catch (e) {
            return this.escapeHtml(String(val));
        }
    },

    formatDuration: function(val) {
        var ms = parseInt(val, 10);
        if (isNaN(ms)) {
            return this.escapeHtml(String(val));
        }
        if (ms < 1000) {
            return ms + " ms";
        }
        return (ms / 1000).toFixed(2) + " s";
    },

    formatSize: function(val) {
        var bytes = parseInt(val, 10);
        if (isNaN(bytes)) {
            return this.escapeHtml(String(val));
        }
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return (bytes / 1024).toFixed(1) + " KB";
        }
        return (bytes / (1024 * 1024)).toFixed(2) + " MB";
    },

    openInspectModal: function(rowIndex, fieldKey) {
        var row = this.lastRecords[rowIndex];
        if (!row) {
            return;
        }
        var rawVal = row[fieldKey];
        var displayVal = "";
        try {
            if (typeof rawVal === "string" && (rawVal.charAt(0) === "{" || rawVal.charAt(0) === "[")) {
                displayVal = JSON.stringify(JSON.parse(rawVal), null, 2);
            } else if (typeof rawVal === "object") {
                displayVal = JSON.stringify(rawVal, null, 2);
            } else {
                displayVal = String(rawVal);
            }
        } catch (e) {
            displayVal = String(rawVal);
        }

        var modalContent = document.getElementById("audit-modal-content");
        if (modalContent) {
            modalContent.textContent = displayVal;
        }
        var backdrop = document.getElementById("audit-modal-backdrop");
        if (backdrop) {
            backdrop.style.display = "flex";
        }
    },

    closeModal: function() {
        var backdrop = document.getElementById("audit-modal-backdrop");
        if (backdrop) {
            backdrop.style.display = "none";
        }
    },

    copyModalContent: function() {
        var self = this;
        var modalContent = document.getElementById("audit-modal-content");
        if (modalContent && navigator.clipboard) {
            navigator.clipboard.writeText(modalContent.textContent).then(function() {
                alert(self.msg.copied || "Copied to clipboard!");
            });
        }
    }
};
