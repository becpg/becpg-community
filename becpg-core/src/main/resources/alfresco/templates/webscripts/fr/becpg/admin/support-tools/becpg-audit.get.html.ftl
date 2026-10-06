<#include "../../../../org/alfresco/repository/admin/admin-template.ftl" />

<@page title=msg("audit.title") readonly=true>

    <link rel="stylesheet" type="text/css" href="${url.context}/beCPG/css/becpg-audit.css" />
    <script type="text/javascript" src="${url.context}/beCPG/scripts/becpg-audit.js"></script>

    <div class="column-full">
      <@section label=msg("audit.header.title") />
      <p class="info">${msg("audit.header.description")}</p>

      <div class="audit-tabs">
        <#list plugins as plugin>
          <div id="tab-${plugin.id}" class="audit-tab<#if plugin_index == 0> active</#if>" onclick="AuditViewer.selectTab('${plugin.id}')">${plugin.label}</div>
        </#list>
      </div>

      <div class="audit-toolbar">
        <div class="toolbar-group audit-filter-group">
          <label>${msg("audit.toolbar.filter")}:</label>
          <div id="audit-filter-rows" class="audit-filter-rows"></div>
        </div>
        <div class="toolbar-group" title="${msg("audit.toolbar.dateRange.tooltip")}">
          <label for="audit-from-date-input">${msg("audit.toolbar.fromDate")}:</label>
          <input type="date" id="audit-from-date-input" onchange="AuditViewer.onDateRangeChange();" />
          <label for="audit-to-date-input">${msg("audit.toolbar.toDate")}:</label>
          <input type="date" id="audit-to-date-input" onchange="AuditViewer.onDateRangeChange();" />
        </div>
        <div id="audit-status-bar" class="audit-status-bar"></div>
      </div>

      <div id="audit-notice-bar" class="audit-notice-bar" style="display:none;"></div>

      <div class="audit-table-wrapper">
        <table class="audit-table" id="audit-data-table">
          <thead id="audit-table-head"></thead>
          <tbody id="audit-table-body">
            <tr><td colspan="8" class="audit-loading">${msg("audit.loading")}</td></tr>
          </tbody>
        </table>
      </div>

      <div class="audit-pager">
        <button type="button" id="audit-previous-btn" class="audit-btn audit-btn-secondary" disabled="disabled" onclick="AuditViewer.previousPage();">&lsaquo; ${msg("audit.btn.previous")}</button>
        <div id="audit-page-numbers" class="audit-page-numbers"></div>
        <button type="button" id="audit-next-btn" class="audit-btn audit-btn-secondary" disabled="disabled" onclick="AuditViewer.nextPage();">${msg("audit.btn.next")} &rsaquo;</button>
      </div>
    </div>

    <!-- Details Modal -->
    <div id="audit-modal-backdrop" class="audit-modal-backdrop" onclick="AuditViewer.closeModal(event);">
      <div class="audit-modal" onclick="event.stopPropagation();">
        <div class="audit-modal-header">
          <span id="audit-modal-title">${msg("audit.modal.title")}</span>
          <button type="button" style="border:none;background:none;font-size:18px;cursor:pointer;" onclick="AuditViewer.closeModal();">&times;</button>
        </div>
        <div class="audit-modal-body" id="audit-modal-content"></div>
        <div class="audit-modal-footer">
          <button type="button" class="audit-btn" onclick="AuditViewer.copyModalContent();">${msg("audit.modal.copy")}</button>
          <button type="button" class="audit-btn audit-btn-danger" onclick="AuditViewer.closeModal();">${msg("audit.modal.close")}</button>
        </div>
      </div>
    </div>

    <script type="text/javascript">//<![CDATA[
      AuditViewer.init({
        serviceContext: "${url.serviceContext}",
        defaultTab: "${plugins[0].id}",
        plugins: [
          <#list plugins as plugin>
          {
            id: "${plugin.id}",
            label: "${plugin.label?js_string}",
            columns: [
              <#list plugin.columns as col>
              {
                key: "${col.key}",
                label: "${col.label?js_string}"<#if col.format??>,
                format: "${col.format}"</#if><#if col.width??>,
                width: "${col.width}"</#if>
              }<#if col_has_next>,</#if>
              </#list>
            ]
          }<#if plugin_has_next>,</#if>
          </#list>
        ],
        msg: {
          loading: "${msg("audit.loading")?js_string}",
          noRecords: "${msg("audit.no-records")?js_string}",
          errorLoading: "${msg("audit.error.loading")?js_string}",
          recordsRange: "${msg("audit.records-range")?js_string}",
          moreToRead: "${msg("audit.page.more")?js_string}",
          next: "${msg("audit.btn.next")?js_string}",
          filterPlaceholder: "${msg("audit.toolbar.filter.placeholder")?js_string}",
          removeFilter: "${msg("audit.filter.remove")?js_string}",
          addFilter: "${msg("audit.filter.add")?js_string}",
          noticeInterrupted: "${msg("audit.notice.interrupted")?js_string}",
          inspect: "${msg("audit.btn.inspect")?js_string}",
          copied: "${msg("audit.copied")?js_string}",
          yes: "${msg("data.boolean.true")?js_string}",
          no: "${msg("data.boolean.false")?js_string}"
        }
      });
    //]]></script>

</@page>
