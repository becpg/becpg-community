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
         <#if col.mandatory??>
         "mandatory": ${col.mandatory?string},
         </#if>
         <#if col.readOnly??>
        	"readOnly": ${col.readOnly?string},
         </#if>
         	 <#if col.protectedField??>
         "protectedField": ${col.protectedField?string},
      	 </#if>
      <#if col.dataType??>
          <#if col.constraints??>
          "constraints": [
                <#list col.constraints as cnstrnt>
                { "type": "${cnstrnt.type}"
                <#if cnstrnt.parameters??>,
                  "parameters": ${jsonUtils.toJSONString(cnstrnt.parameters)}
                  </#if>}<#if cnstrnt_has_next>,</#if>
              </#list>],
          </#if>
         <#if col.repeating??>
         "repeating": ${col.repeating?string},
          </#if>
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


