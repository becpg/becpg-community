<#include "../../../../org/alfresco/repository/admin/admin-template.ftl" />

<#macro sysField id>
	  <p class="info">${msg(id+".description")?html}</p>
	  <#assign attr = sysBeCPGAttributes?filter(a -> a.key == id)?first>
	  <@attrtextarea id=id attribute=attr maxlength=5000/>
      <@button label=msg("system-configuration.save") onclick="updateConf('${id?html}');" />
      <@button label=msg("system-configuration.reset") onclick="resetConf('${id?html}');" />
</#macro>

<@page title=msg("system-configuration.title") readonly=true>
   
    <style type="text/css">
      .config-tabs {
        display: flex;
        flex-wrap: wrap;
        border-bottom: 2px solid #ccc;
        margin-bottom: 20px;
      }
      .config-tab {
        padding: 10px 20px;
        cursor: pointer;
        background: #f5f5f5;
        border: 1px solid #ccc;
        border-bottom: none;
        margin-right: 2px;
        border-radius: 4px 4px 0 0;
      }
      .config-tab:hover {
        background: #e8e8e8;
      }
      .config-tab.active {
        background: #fff;
        border-bottom: 2px solid #fff;
        margin-bottom: -2px;
        font-weight: bold;
      }
      .config-tab-content {
        display: none;
      }
      .config-tab-content.active {
        display: block;
      }
    </style>
   
    <script type="text/javascript">//<![CDATA[
      
      var serviceContext = ${url.serviceContext};
      
      function updateConf(id){
           var field = el(id);
           
	       Admin.request({
	            url : serviceContext + '/becpg/admin/system/config/update',
	            method : "POST",
	            data : {"key": id ,"value": field.value},
	            fnSuccess : function(res)
	            {
	            	alert("${msg("system-configuration.conf-updated")}");
	            }
	        });
      }
      
      function resetConf(id){
           var field = el(id);
           
	       Admin.request({
	            url : serviceContext + '/becpg/admin/system/config/reset',
	            method : "POST",
	            data : {"key": id},
	            fnSuccess : function(res)
	            {
	            	alert("${msg("system-configuration.conf-reset")}");
	            	location.reload();
	            }
	        });
      }
      
      function showTab(tabId) {
          var tabs = document.querySelectorAll('.config-tab');
          var contents = document.querySelectorAll('.config-tab-content');
          
          for (var i = 0; i < tabs.length; i++) {
              tabs[i].className = tabs[i].className.replace(' active', '');
          }
          for (var i = 0; i < contents.length; i++) {
              contents[i].className = contents[i].className.replace(' active', '');
          }
          
          document.getElementById('tab-' + tabId).className += ' active';
          document.getElementById('content-' + tabId).className += ' active';
      }
      
      
    //]]></script>
   
   <div class="config-tabs">
      <div id="tab-system" class="config-tab active" onclick="showTab('system')">${msg("system-configuration.section.system.title")}</div>
      <div id="tab-activity" class="config-tab" onclick="showTab('activity')">${msg("system-configuration.section.activity.title")}</div>
      <div id="tab-format" class="config-tab" onclick="showTab('format')">${msg("system-configuration.section.format.title")}</div>
      <div id="tab-search" class="config-tab" onclick="showTab('search')">${msg("system-configuration.section.search.title")}</div>
      <div id="tab-export" class="config-tab" onclick="showTab('export')">${msg("system-configuration.section.export.title")}</div>
      <div id="tab-report" class="config-tab" onclick="showTab('report')">${msg("system-configuration.section.report.title")}</div>
      <div id="tab-formulation" class="config-tab" onclick="showTab('formulation')">${msg("system-configuration.section.formulation.title")}</div>
      <div id="tab-automatic-formulation" class="config-tab" onclick="showTab('automatic-formulation')">${msg("system-configuration.section.automatic-formulation.title")}</div>
      <div id="tab-project" class="config-tab" onclick="showTab('project')">${msg("system-configuration.section.project.title")}</div>
   </div>
   
   <div id="content-system" class="config-tab-content active">
      <div class="column-full">
      <@section label=msg("system-configuration.section.system.title") />
      <#list sysBeCPGAttributes as attr>
          <#if attr.set == "system">
              <@sysField id=attr.key />
          </#if>
      </#list>
      </div>
   </div>
   
   <div id="content-activity" class="config-tab-content">
      <div class="column-full">
      <@section label=msg("system-configuration.section.activity.title") />
      <#list sysBeCPGAttributes as attr>
          <#if attr.set == "activity">
              <@sysField id=attr.key />
          </#if>
      </#list>
      </div>
   </div>
   
   <div id="content-format" class="config-tab-content">
      <div class="column-full">
      <@section label=msg("system-configuration.section.format.title") />
      <#list sysBeCPGAttributes as attr>
          <#if attr.set == "format">
              <@sysField id=attr.key />
          </#if>
      </#list>
      </div>
   </div>
   
   <div id="content-search" class="config-tab-content">
      <div class="column-full">
      <@section label=msg("system-configuration.section.search.title") />
      <#list sysBeCPGAttributes as attr>
          <#if attr.set == "search">
              <@sysField id=attr.key />
          </#if>
      </#list>
      </div>
   </div>
   
   <div id="content-export" class="config-tab-content">
      <div class="column-full">
      <@section label=msg("system-configuration.section.export.title") />
      <#list sysBeCPGAttributes as attr>
          <#if attr.set == "export">
              <@sysField id=attr.key />
          </#if>
      </#list>
      </div>
   </div>
   
   <div id="content-report" class="config-tab-content">
      <div class="column-full">
      <@section label=msg("system-configuration.section.report.title") />
      <#list sysBeCPGAttributes as attr>
          <#if attr.set == "report">
              <@sysField id=attr.key />
          </#if>
      </#list>
      </div>
   </div>
   
   <div id="content-formulation" class="config-tab-content">
      <div class="column-full">
      <@section label=msg("system-configuration.section.formulation.title") />
      <#list sysBeCPGAttributes as attr>
          <#if attr.set == "formulation">
              <@sysField id=attr.key />
          </#if>
      </#list>
      </div>
   </div>
   
   <div id="content-automatic-formulation" class="config-tab-content">
      <div class="column-full">
      <@section label=msg("system-configuration.section.automatic-formulation.title") />
      <#list sysBeCPGAttributes as attr>
          <#if attr.set == "automatic-formulation">
              <@sysField id=attr.key />
          </#if>
      </#list>
      </div>
   </div>
   
   <div id="content-project" class="config-tab-content">
      <div class="column-full">
      <@section label=msg("system-configuration.section.project.title") />
      <#list sysBeCPGAttributes as attr>
          <#if attr.set == "project">
              <@sysField id=attr.key />
          </#if>
      </#list>
      </div>
   </div>
   
</@page>
