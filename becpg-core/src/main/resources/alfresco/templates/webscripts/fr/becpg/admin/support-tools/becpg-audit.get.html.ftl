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
        <div class="toolbar-group">
          <label for="audit-filter-column-select">${msg("audit.toolbar.filter")}:</label>
          <select id="audit-filter-column-select" onchange="AuditViewer.onFilterColumnChange();">
            <option value="">--</option>
          </select>
          <input type="text" id="audit-filter-value-input" disabled="disabled" placeholder="${msg("audit.toolbar.filter.placeholder")}" onkeypress="if(event.keyCode===13){AuditViewer.reload();}" />
          <button type="button" class="audit-btn audit-btn-secondary" onclick="AuditViewer.resetFilter();" title="${msg("audit.filter.reset")}">&times;</button>
        </div>
        <div class="toolbar-group">
          <label for="audit-limit-select">${msg("audit.toolbar.limit")}:</label>
          <select id="audit-limit-select" onchange="AuditViewer.reload();">
            <option value="25">25</option>
            <option value="50" selected="selected">50</option>
            <option value="100">100</option>
            <option value="250">250</option>
            <option value="500">500</option>
            <option value="1000">1000</option>
          </select>
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
                format: "${col.format}"</#if><#if col.isDb?? && col.isDb>,
                isDb: true</#if><#if col.width??>,
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
          recordsFound: "${msg("audit.records-found")?js_string}",
          dbTooltip: "${msg("audit.col.db-tooltip")?js_string}",
          noticeSort: "${msg("audit.notice.sort")?js_string}",
          noticeFilter: "${msg("audit.notice.filter")?js_string}",
          noticeBoth: "${msg("audit.notice.both")?js_string}",
          inspect: "${msg("audit.btn.inspect")?js_string}",
          copied: "${msg("audit.copied")?js_string}",
          yes: "${msg("data.boolean.true")?js_string}",
          no: "${msg("data.boolean.false")?js_string}"
        }
      });
    //]]></script>

</@page>
