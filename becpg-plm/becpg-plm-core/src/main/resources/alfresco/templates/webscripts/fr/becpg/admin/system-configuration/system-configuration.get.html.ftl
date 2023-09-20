<#include "../../../../org/alfresco/repository/admin/admin-template.ftl" />

<#macro sysField id>
	  <p class="info">${msg(id+".description")?html}</p>
	  <@attrtext id=id attribute=sysBeCPGAttributes[id] />
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
	            	alert("Conf updated");
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
	            	alert("Conf resetted");
	            	location.reload();
	            }
	        });
      }
      
      
    //]]></script>
   
   <div class="column-full">
    <@section label=msg("system-configuration.section.core.title") />
	<#list sysBeCPGAttributes?keys as key>
		<#if sysBeCPGAttributes[key].set == "core">
			<@sysField id=key />
		</#if>
	</#list>
     </div>
   
    <div class="column-full">
    <@section label=msg("system-configuration.section.plm.title") />
	<#list sysBeCPGAttributes?keys as key>
		<#if sysBeCPGAttributes[key].set == "plm">
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
