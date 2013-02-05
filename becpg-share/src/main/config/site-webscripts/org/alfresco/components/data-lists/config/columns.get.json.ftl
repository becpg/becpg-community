<#macro displayColumns columns>
"columns":
   [
   <#list columns as col>
      {
         "type": "${col.type}",
         "name": "${col.name}",
         "formsName": "<#if col.type == "association">assoc<#else>prop</#if>_${col.name?replace(":", "_")}",
         <#if col.label??>
         "label": "${msg(col.label)}",
         <#else>
         "label": "",
         </#if>
      <#if col.dataType??>
         "dataType": "${col.dataType}"
      <#else>
         "dataType": "${col.endpointType}"
      </#if>
      <#if col.columns??>
      	 ,<@displayColumns col.columns/>
      </#if>
      
      }<#if col_has_next>,</#if>
   </#list>
   ]

</#macro>
<#escape x as jsonUtils.encodeJSONString(x)>
{
<#if error??>
   "error": "${error}"
<#else>
   <@displayColumns columns/>
</#if>
}
</#escape>


