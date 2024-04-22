<#include "../../../../org/alfresco/repository/admin/admin-template.ftl" />

<#macro sysField id>
	  <p class="info">${msg(id+".description")?html}</p>
	  <@attrtextarea id=id attribute=sysBeCPGAttributes[id] />
      <@button label=msg("system-configuration.save") onclick="updateConf('${id?html}');" />
      <@button label=msg("system-configuration.reset") onclick="resetConf('${id?html}');" />
</#macro>

<@page title=msg("system-configuration.title") readonly=true>
   
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
      
      
    //]]></script>
   
   <div class="column-full">
    <@section label=msg("system-configuration.section.system.title") />
	<#list sysBeCPGAttributes?keys as key>
		<#if sysBeCPGAttributes[key].set == "system">
			<@sysField id=key />
		</#if>
	</#list>
     </div>
   
    <div class="column-full">
    <@section label=msg("system-configuration.section.format.title") />
	<#list sysBeCPGAttributes?keys as key>
		<#if sysBeCPGAttributes[key].set == "format">
			<@sysField id=key />
		</#if>
	</#list>
    </div>
   
    <div class="column-full">
    <@section label=msg("system-configuration.section.search.title") />
	<#list sysBeCPGAttributes?keys as key>
		<#if sysBeCPGAttributes[key].set == "search">
			<@sysField id=key />
		</#if>
	</#list>
    </div>
   
    <div class="column-full">
    <@section label=msg("system-configuration.section.export.title") />
	<#list sysBeCPGAttributes?keys as key>
		<#if sysBeCPGAttributes[key].set == "export">
			<@sysField id=key />
		</#if>
	</#list>
    </div>
   
    <div class="column-full">
    <@section label=msg("system-configuration.section.report.title") />
	<#list sysBeCPGAttributes?keys as key>
		<#if sysBeCPGAttributes[key].set == "report">
			<@sysField id=key />
		</#if>
	</#list>
    </div>
   
    <div class="column-full">
    <@section label=msg("system-configuration.section.formulation.title") />
	<#list sysBeCPGAttributes?keys as key>
		<#if sysBeCPGAttributes[key].set == "formulation">
			<@sysField id=key />
		</#if>
	</#list>
    </div>
   
    <div class="column-full">
    <@section label=msg("system-configuration.section.change-order.title") />
	<#list sysBeCPGAttributes?keys as key>
		<#if sysBeCPGAttributes[key].set == "change-order">
			<@sysField id=key />
		</#if>
	</#list>
    </div>
   
    <div class="column-full">
    <@section label=msg("system-configuration.section.project.title") />
	<#list sysBeCPGAttributes?keys as key>
		<#if sysBeCPGAttributes[key].set == "project">
			<@sysField id=key />
		</#if>
	</#list>
    </div>
   
   
   
</@page>
