<#macro displayColumns columns>
"columns":
   [
   <#list columns as col>
      {
         "type": "${col.type}",
         "name": "${col.name}",
         "formsName": "<#if col.type == "association">assoc<#else>prop</#if>_${col.name?replace(":", "_")}",
         <#if col.checked??>
         	"checked": ${col.checked?string},
      	 </#if>
         <#if col.label??>
         "label": "${jsonUtils.encodeJSONString(msg(col.label))}",
         <#else>
         "label": "",
         </#if>
         <#if col.help??>
          "options": "${jsonUtils.encodeJSONString(col.help)}",
         </#if>
         <#if col.mandatory??>
         "mandatory": ${col.mandatory?string},
         </#if>
         <#if col.readOnly??>
          "readOnly": ${col.readOnly?string},
         </#if>
          <#-- Only present when the caller asked for it with withDefaults=true. -->
          <#if withDefaults?? && withDefaults && col.value??>
         "defaultValue": ${jsonUtils.toJSONString(col.value)},
          </#if>
          <#-- Only present when the caller asked for it with withControls=true. -->
          <#if col.control??>
         "control": { "template": <#if col.control.template??>"${jsonUtils.encodeJSONString(col.control.template)}"<#else>null</#if>, "params": {<#list col.control.params?keys as paramName>"${jsonUtils.encodeJSONString(paramName)}": "${jsonUtils.encodeJSONString(col.control.params[paramName])}"<#if paramName_has_next>, </#if></#list>} },
      	 </#if>
          <#-- Only present when the caller asked for it with withSets=true. -->
          <#if col.set??>
         "set": "${jsonUtils.encodeJSONString(col.set)}",
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
<#-- The label-id wins over the literal label, the order Share's FormUIGet resolves a set label in:
     the section then reads as in the form. -->
<#function setLabel formSet>
   <#if formSet.labelId??>
      <#return msg(formSet.labelId)>
   </#if>
   <#return formSet.label>
</#function>
<#macro displaySets sets>
"sets":
   [
   <#list sets as formSet>
      {
         "id": "${jsonUtils.encodeJSONString(formSet.id)}",
         "label": "${jsonUtils.encodeJSONString(setLabel(formSet))}"<#if formSet.parentId??>,
         "parentId": "${jsonUtils.encodeJSONString(formSet.parentId)}"</#if>
      }<#if formSet_has_next>,</#if>
   </#list>
   ]
</#macro>
<#escape x as jsonUtils.encodeJSONString(x)>
{
<#if error??>
   "error": "${error}"
<#else>
   <@displayColumns columns/>
   <#if sets??>
   ,<@displaySets sets/>
   </#if>
</#if>
}
</#escape>


