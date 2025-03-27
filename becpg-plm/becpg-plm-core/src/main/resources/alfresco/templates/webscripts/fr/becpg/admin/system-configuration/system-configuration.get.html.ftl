<#include "../../../../org/alfresco/repository/admin/admin-template.ftl" />

<#macro sysField id>
	  <p class="info">${msg(id+".description")?html}</p>
	  <#assign attr = sysBeCPGAttributes?filter(a -> a.key == id)?first>
	  <@attrtextarea id=id attribute=attr maxlength=5000/>
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
	<#list sysBeCPGAttributes as attr>
	    <#if attr.set == "system">
	        <@sysField id=attr.key />
	    </#if>
	</#list>
     </div>
   
    <div class="column-full">
    <@section label=msg("system-configuration.section.format.title") />
	<#list sysBeCPGAttributes as attr>
	    <#if attr.set == "format">
	        <@sysField id=attr.key />
	    </#if>
	</#list>
    </div>
   
    <div class="column-full">
    <@section label=msg("system-configuration.section.search.title") />
	<#list sysBeCPGAttributes as attr>
	    <#if attr.set == "search">
	        <@sysField id=attr.key />
	    </#if>
	</#list>
    </div>
   
    <div class="column-full">
    <@section label=msg("system-configuration.section.export.title") />
	<#list sysBeCPGAttributes as attr>
	    <#if attr.set == "export">
	        <@sysField id=attr.key />
	    </#if>
	</#list>
    </div>
   
    <div class="column-full">
    <@section label=msg("system-configuration.section.report.title") />
	<#list sysBeCPGAttributes as attr>
	    <#if attr.set == "report">
	        <@sysField id=attr.key />
	    </#if>
	</#list>
    </div>
   
    <div class="column-full">
    <@section label=msg("system-configuration.section.formulation.title") />
	<#list sysBeCPGAttributes as attr>
	    <#if attr.set == "formulation">
	        <@sysField id=attr.key />
	    </#if>
	</#list>
    </div>
   
    <div class="column-full">
    <@section label=msg("system-configuration.section.automatic-formulation.title") />
	<#list sysBeCPGAttributes as attr>
	    <#if attr.set == "automatic-formulation">
	        <@sysField id=attr.key />
	    </#if>
	</#list>
    </div>
   
    <div class="column-full">
    <@section label=msg("system-configuration.section.project.title") />
	<#list sysBeCPGAttributes as attr>
	    <#if attr.set == "project">
	        <@sysField id=attr.key />
	    </#if>
	</#list>
    </div>
   
   
   
</@page>
