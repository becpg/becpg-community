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
 */
var AuditViewer = {
    currentTab: "formulation",
    dbAsc: false,
    sortBy: null,
    sortAsc: false,
    lastRecords: [],
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

    updateNoticeBar: function(filterParam, filterCol, limit, resultCount) {
        var noticeBar = document.getElementById("audit-notice-bar");
        if (!noticeBar) {
            return;
        }

        var limitNum = parseInt(limit, 10) || 50;
        var hasReachedLimit = (typeof resultCount === "number" && resultCount >= limitNum);

        var isBusinessSort = !!(this.sortBy && this.sortBy !== "startedAt" && this.sortBy !== "prop_cm_created");
        var isFilterActive = !!(filterParam && filterParam.length > 0);

        if ((!isBusinessSort && !isFilterActive) || !hasReachedLimit) {
            noticeBar.style.display = "none";
            noticeBar.innerHTML = "";
            return;
        }

        var sortColLabel = isBusinessSort ? this.getColumnLabel(this.sortBy) : "";
        var filterColLabel = isFilterActive ? (filterCol ? this.getColumnLabel(filterCol) : "") : "";

        var msgText = "";
        if (isBusinessSort && isFilterActive) {
            msgText = this.substitute(this.msg.noticeBoth || "Filtering and sorting on business columns are restricted to the {0} retrieved records.", [limit]);
        } else if (isBusinessSort) {
            msgText = this.substitute(this.msg.noticeSort || "Sorting on business column \"{0}\" is restricted to the {1} retrieved records.", [sortColLabel, limit]);
        } else if (isFilterActive) {
            msgText = this.substitute(this.msg.noticeFilter || "Filtering on business column \"{0}\" is restricted to the limit of {1} retrieved records.", [filterColLabel, limit]);
        }

        noticeBar.innerHTML = '<span style="font-weight:bold;margin-right:6px;">\u2139\uFE0F</span>' + this.escapeHtml(msgText);
        noticeBar.style.display = "block";
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
        this.renderHeader();
        this.reload();
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
            this.reload();
        } else {
            if (input) {
                input.disabled = false;
                input.focus();
            }
            if (input && input.value.trim().length > 0) {
                this.reload();
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
        this.reload();
    },

    sortByColumn: function(colKey) {
        if (!colKey || colKey === "prop_bcpg_alData") {
            return;
        }

        var isDbCol = (colKey === "startedAt" || colKey === "prop_cm_created");

        if (isDbCol) {
            this.sortBy = null;
            this.dbAsc = !this.dbAsc;
        } else {
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
        }

        this.renderHeader();
        this.reload();
    },

    renderHeader: function() {
        var cols = this.columns[this.currentTab] || [];
        var thead = document.getElementById("audit-table-head");
        if (!thead) {
            return;
        }

        var html = "<tr>";
        for (var i = 0; i < cols.length; i++) {
            var col = cols[i];
            var style = col.width ? ' style="width:' + col.width + '"' : "";
            var isSortable = col.key && col.format !== "inspect";
            var isCurrentSort = this.sortBy === col.key;
            var isDbCol = !!col.isDb;

            var thClass = (isSortable ? "sortable" : "") + (isDbCol ? " col-db" : "");
            var sortIcon = "";

            if (isDbCol && !this.sortBy) {
                thClass += this.dbAsc ? " sorted-asc" : " sorted-desc";
                sortIcon = '<span class="sort-icon">' + (this.dbAsc ? "▲" : "▼") + '</span><span class="db-badge">DB</span>';
            } else if (isCurrentSort) {
                thClass += this.sortAsc ? " sorted-asc" : " sorted-desc";
                sortIcon = '<span class="sort-icon">' + (this.sortAsc ? "▲" : "▼") + '</span>';
            } else if (isSortable) {
                sortIcon = '<span class="sort-icon" style="opacity:0.25;">▲▼</span>' + (isDbCol ? '<span class="db-badge">DB</span>' : '');
            }

            var titleAttr = isDbCol ? ' title="' + (this.msg.dbTooltip || "Direct database query sort") + '"' : '';
            var onclickAttr = isSortable ? ' onclick="AuditViewer.sortByColumn(\'' + col.key + '\')"' : "";
            html += "<th class=\"" + thClass + "\"" + style + titleAttr + onclickAttr + ">" + this.escapeHtml(col.label) + sortIcon + "</th>";
        }
        html += "</tr>";
        thead.innerHTML = html;
    },

    reload: function() {
        var self = this;
        var tbody = document.getElementById("audit-table-body");
        var cols = this.columns[this.currentTab] || [];
        if (tbody) {
            tbody.innerHTML = '<tr><td colspan="' + cols.length + '" class="audit-loading">' + (this.msg.loading || "Loading...") + '</td></tr>';
        }

        var filterColSelect = document.getElementById("audit-filter-column-select");
        var filterValInput = document.getElementById("audit-filter-value-input");
        var limitSelect = document.getElementById("audit-limit-select");

        var filterCol = filterColSelect ? filterColSelect.value : "";
        var filterVal = filterValInput ? filterValInput.value.trim() : "";
        var limit = limitSelect ? limitSelect.value : "50";

        var url = this.serviceContext + "/becpg/stats/" + encodeURIComponent(this.currentTab) +
                  "?maxResults=" + encodeURIComponent(limit) +
                  "&dbAsc=" + (this.dbAsc ? "true" : "false");

        // In-memory column sorting
        if (this.sortBy) {
            url += "&sortBy=" + encodeURIComponent(this.sortBy) +
                   "&asc=" + (this.sortAsc ? "true" : "false");
        }

        // Filtering
        var filterParam = "";
        if (filterVal) {
            if (filterCol) {
                filterParam = filterCol + "=" + filterVal;
            } else if (filterVal.indexOf("=") !== -1) {
                filterParam = filterVal;
            }
        }

        if (filterParam) {
            url += "&filter=" + encodeURIComponent(filterParam);
        }

        var startTime = new Date().getTime();

        Admin.request({
            url: url,
            method: "GET",
            fnSuccess: function(res) {
                var duration = new Date().getTime() - startTime;
                var data = [];
                try {
                    var json = res.responseJSON || (res.responseText ? JSON.parse(res.responseText) : {});
                    data = json.statistics || [];
                } catch (e) {
                    data = [];
                }
                self.lastRecords = data;
                self.renderBody(data);
                self.updateNoticeBar(filterParam, filterCol, limit, data.length);
                var statusBar = document.getElementById("audit-status-bar");
                if (statusBar) {
                    statusBar.textContent = data.length + " " + (self.msg.recordsFound || "records found") + " (" + duration + " ms)";
                }
            },
            fnFailure: function(res) {
                self.updateNoticeBar("", "", limit, 0);
                if (tbody) {
                    tbody.innerHTML = '<tr><td colspan="' + cols.length + '" class="audit-empty-msg" style="color:#d9534f;">' + (self.msg.errorLoading || "Error loading audit entries") + '</td></tr>';
                }
                var statusBar = document.getElementById("audit-status-bar");
                if (statusBar) {
                    statusBar.textContent = "";
                }
            }
        });
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
                var tdClass = col.isDb ? ' class="col-db"' : "";
                html += "<td" + tdClass + ">" + this.formatValue(rawVal, col.format, row, i, col.key) + "</td>";
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
