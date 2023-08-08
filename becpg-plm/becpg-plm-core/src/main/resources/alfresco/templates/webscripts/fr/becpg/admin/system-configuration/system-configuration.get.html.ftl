<#include "../../../../org/alfresco/repository/admin/admin-template.ftl" />

<#macro sysField id>
	  <p class="info">${msg(id+".description")?html}</p>
	  <@attrtext id=id attribute=sysBeCPGAttributes[id] />
      <@button label=msg("system-configuration.save") onclick="save('${id?html}');" />
      <@button label=msg("system-configuration.reset") onclick="reset('${id?html}');" />
</#macro>

<@page title=msg("system-configuration.title") readonly=true>
   
    <script type="text/javascript">//<![CDATA[
      
      function save(id){
           var field = el(id);
           
           alert(field.value);
      
	       Admin.request({
	            url : serviceContext + '/becpg/admin/system/config/update',
	            method : "POST",
	            data : {"key": id ,"value": field.value},
	            fnSuccess : function(res)
	            {
	            	alert(OK);
	            }
	        });
      }
      
      function reset(id){
        Admin.request({
	            url : serviceContext + '/becpg/admin/system/config/reset',
	            method : "POST",
	            data : {"key": id },
	            fnSuccess : function(res)
	            {
	            	alert(OK);
	            }
	        });
      }
      
    //]]></script>
   
   <div class="column-full">
      <@section label=msg("system-configuration.section.core.title") />
      
      <p class="info">${msg("beCPG.charact.description")?html}</p>
      <@sysField id="beCPG.charact.name" />
	  <@sysField id="beCPG.datalist.effectiveFilterEnabled" />
	  <@sysField id="beCPG.defaultSearchTemplate" />
	  <@sysField id="beCPG.multilinguale.disabledMLTextFields" />
	  <@sysField id="beCPG.multilinguale.shouldExtractMLText" />
	  <@sysField id="beCPG.multilinguale.supportedLocales" />
	  <@sysField id="beCPG.report.datasource.maxSizeInBytes" />
	  <@sysField id="beCPG.report.image.maxSizeInBytes" />
	  <@sysField id="beCPG.spel.security.authorizedTypes" />
   </div>
   
      <div class="column-full">
      <@section label=msg("system-configuration.section.plm.title") />
    
    
       <@sysField id="beCPG.formulation.maxRclSourcesToKeep" />
       <@sysField id="beCPG.formulation.reqCtrlListaddChildRclSources" />


        </div>
   
      <div class="column-full">
      <@section label=msg("system-configuration.section.project.title") />
      
        </div>
   
   
   
</@page>
