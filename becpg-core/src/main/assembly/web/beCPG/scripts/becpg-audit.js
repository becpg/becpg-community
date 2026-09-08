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
 * The audit table is read by keyset paging: a request scans a bounded number of entry identifier
 * windows and hands back the identifier the next one starts after. The database order is chosen
 * with the order button, whereas ordering on a column is applied on the retrieved records only.
 */
var AuditViewer = {
    currentTab: "formulation",
    dbAsc: false,
    sortBy: null,
    sortAsc: false,
    fetchedRecords: [],
    lastRecords: [],
    nextStartAfterId: null,
    scanInterrupted: false,
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
        this.dbAsc = false;
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

        this.updateFilterColumnsSelect();
        this.renderDbOrderButton();
        this.renderHeader();
        this.search();
    },

    updateFilterColumnsSelect: function() {
        var select = document.getElementById("audit-filter-column-select");
        var input = document.getElementById("audit-filter-value-input");
        if (!select) {
            return;
        }

        var cols = this.columns[this.currentTab] || [];
        var html = '<option value="">--</option>';

        for (var i = 0; i < cols.length; i++) {
            var col = cols[i];
            var isDateCol = (col.key === "startedAt" || col.key === "completedAt" || col.key === "prop_cm_created");
            if (col.key && col.format !== "inspect" && col.key !== "id" && !isDateCol) {
                html += '<option value="' + this.escapeHtml(col.key) + '">' + this.escapeHtml(col.label) + '</option>';
            }
        }

        select.innerHTML = html;
        select.value = "";

        if (input) {
            input.value = "";
            input.disabled = true;
        }
    },

    onFilterColumnChange: function() {
        var select = document.getElementById("audit-filter-column-select");
        var input = document.getElementById("audit-filter-value-input");
        var col = select ? select.value : "";

        if (!col) {
            if (input) {
                input.value = "";
                input.disabled = true;
            }
            this.search();
        } else {
            if (input) {
                input.disabled = false;
                input.focus();
            }
            if (input && input.value.trim().length > 0) {
                this.search();
            }
        }
    },

    resetFilter: function() {
        var colSelect = document.getElementById("audit-filter-column-select");
        var valInput = document.getElementById("audit-filter-value-input");
        if (colSelect) {
            colSelect.value = "";
        }
        if (valInput) {
            valInput.value = "";
            valInput.disabled = true;
        }
        this.search();
    },

    /**
     * Order the audit table on the database side, which can only be the order of the entry
     * identifiers, hence the order the entries were written in.
     */
    toggleDbOrder: function() {
        this.dbAsc = !this.dbAsc;
        this.renderDbOrderButton();
        this.search();
    },

    renderDbOrderButton: function() {
        var button = document.getElementById("audit-db-order-btn");
        if (!button) {
            return;
        }
        button.innerHTML = this.dbAsc
            ? "▲ " + this.escapeHtml(this.msg.oldestFirst || "Oldest")
            : "▼ " + this.escapeHtml(this.msg.newestFirst || "Newest");
    },

    /**
     * Order the retrieved records on a column. The values a business column holds are not
     * something the audit query can order on, so this stays a client side ordering.
     */
    sortByColumn: function(colKey) {
        if (!this.isSortableColumn(colKey)) {
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
        this.renderBody(this.lastRecords);
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
     * Start a new scan, from the first entry of the audit application in the current database
     * order.
     */
    search: function() {
        this.fetchedRecords = [];
        this.lastRecords = [];
        this.nextStartAfterId = null;
        this.scanInterrupted = false;
        this.load(false);
    },

    /**
     * Carry the scan on, from the identifier the previous page stopped after.
     */
    continueSearch: function() {
        if (this.nextStartAfterId === null) {
            return;
        }
        this.load(true);
    },

    getLimit: function() {
        var limitSelect = document.getElementById("audit-limit-select");
        return limitSelect ? limitSelect.value : "50";
    },

    getFilterParam: function() {
        var filterColSelect = document.getElementById("audit-filter-column-select");
        var filterValInput = document.getElementById("audit-filter-value-input");
        var filterCol = filterColSelect ? filterColSelect.value : "";
        var filterVal = filterValInput ? filterValInput.value.trim() : "";

        if (!filterVal) {
            return "";
        }
        if (filterCol) {
            return filterCol + "=" + filterVal;
        }
        if (filterVal.indexOf("=") !== -1) {
            return filterVal;
        }
        return "";
    },

    buildUrl: function(append) {
        var url = this.serviceContext + "/becpg/stats/" + encodeURIComponent(this.currentTab) +
                  "?maxResults=" + encodeURIComponent(this.getLimit()) +
                  "&dbAsc=" + (this.dbAsc ? "true" : "false");

        var filterParam = this.getFilterParam();
        if (filterParam) {
            url += "&filter=" + encodeURIComponent(filterParam);
        }
        if (append && this.nextStartAfterId !== null) {
            url += "&startAfterId=" + encodeURIComponent(this.nextStartAfterId);
        }
        return url;
    },

    load: function(append) {
        var self = this;
        var cols = this.columns[this.currentTab] || [];
        var tbody = document.getElementById("audit-table-body");

        if (tbody && !append) {
            tbody.innerHTML = '<tr><td colspan="' + cols.length + '" class="audit-loading">' + (this.msg.loading || "Loading...") + '</td></tr>';
        }

        var startTime = new Date().getTime();

        Admin.request({
            url: this.buildUrl(append),
            method: "GET",
            fnSuccess: function(res) {
                self.onPageLoaded(res, append, new Date().getTime() - startTime);
            },
            fnFailure: function() {
                self.onPageFailed();
            }
        });
    },

    onPageLoaded: function(res, append, duration) {
        var json = {};
        try {
            json = res.responseJSON || (res.responseText ? JSON.parse(res.responseText) : {});
        } catch (e) {
            json = {};
        }

        var page = json.statistics || [];
        this.nextStartAfterId = (typeof json.nextStartAfterId === "undefined") ? null : json.nextStartAfterId;
        this.scanInterrupted = (json.scanInterrupted === true);
        this.fetchedRecords = append ? this.fetchedRecords.concat(page) : page;
        this.lastRecords = this.sortRecords(this.fetchedRecords);

        this.renderBody(this.lastRecords);
        this.updateNoticeBar();
        this.updateStatusBar(duration);
        this.updateContinueButton();
    },

    onPageFailed: function() {
        var cols = this.columns[this.currentTab] || [];
        var tbody = document.getElementById("audit-table-body");

        this.nextStartAfterId = null;
        this.scanInterrupted = false;

        if (tbody) {
            tbody.innerHTML = '<tr><td colspan="' + cols.length + '" class="audit-empty-msg" style="color:#d9534f;">' + (this.msg.errorLoading || "Error loading audit entries") + '</td></tr>';
        }
        this.updateNoticeBar();
        this.updateContinueButton();

        var statusBar = document.getElementById("audit-status-bar");
        if (statusBar) {
            statusBar.textContent = "";
        }
    },

    updateStatusBar: function(duration) {
        var statusBar = document.getElementById("audit-status-bar");
        if (statusBar) {
            statusBar.textContent = this.lastRecords.length + " " + (this.msg.recordsFound || "records found") + " (" + duration + " ms)";
        }
    },

    /**
     * Carrying a search on is offered only when the server stopped short of reading the whole
     * audit table. A page the server filled needs no such button: it holds what was asked for.
     */
    updateContinueButton: function() {
        var button = document.getElementById("audit-continue-btn");
        if (button) {
            button.style.display = (this.scanInterrupted && (this.nextStartAfterId !== null)) ? "" : "none";
        }
    },

    /**
     * The only notice worth showing: the scan stopped on its window budget, so the audit table has
     * not been read to its end and the search has to be carried on.
     */
    updateNoticeBar: function() {
        var noticeBar = document.getElementById("audit-notice-bar");
        if (!noticeBar) {
            return;
        }

        if (!this.scanInterrupted) {
            noticeBar.style.display = "none";
            noticeBar.innerHTML = "";
            return;
        }

        var text = this.substitute(this.msg.noticeInterrupted || "", [this.msg.continueSearch || ""]);
        noticeBar.innerHTML = '<span style="font-weight:bold;margin-right:6px;">⚠️</span>' + this.escapeHtml(text);
        noticeBar.style.display = "block";
    },

    renderBody: function(records) {
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
                html += "<td>" + this.formatValue(rawVal, col.format, row, i, col.key) + "</td>";
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
