<#include "/org/alfresco/components/form/controls/common/utils.inc.ftl" />

<#if field.control.params.searchMode??>
   <#assign searchMode=field.control.params.searchMode>
   <#assign fieldName="${field.name}">
   <#assign fieldValue="">
<#else>
   <#assign searchMode="0">
   <#assign fieldName="${field.name}">
   <#assign fieldValue="${field.value}">
</#if>

<div class="form-field">
   <#if form.mode == "view" || (form.mode == "edit" && field.disabled)>
      <div class="viewmode-field">
         <span class="viewmode-label">${field.label?html}:</span>
         <span class="viewmode-value">
         	<@translateValue field.value />         
         </span>
      </div>
   <#else>
      <label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
      
      <select id="${fieldHtmlId}" name="${fieldName}" tabindex="0" size="1"
            <#if field.description??>title="${field.description}"</#if>
            <#if field.control.params.styleClass??>class="${field.control.params.styleClass}"</#if>
            <#if field.control.params.style??>style="${field.control.params.style}"</#if>
            <#if field.disabled>disabled="true"</#if>
            <#if searchMode == "1">multiple="multiple"</#if>>
                 
            <option value=""></option>
            <#list field.control.params.options?split(",") as nameValue>
            	<#if nameValue?index_of("|") == -1>
               	<option value="${nameValue?html}"<#if (fieldValue == nameValue)> selected="selected"</#if>><@translateValue nameValue /></option>
               <#else>
               	<#assign choice=nameValue?split("|")>
                	<option value="${choice[0]?html}"<#if (field.value?string?index_of(choice[0]) != -1)> selected="selected"</#if>>${msgValue(choice[1])?html}</option>
              	</#if>
          	</#list>            
      </select>
   </#if>
</div>

<#macro translateValue nameValue>
      <#if nameValue?string == "ToValidate">${msg("state.product.tovalidate")}
      <#elseif nameValue?string == "Valid">${msg("state.product.valid")}
      <#elseif nameValue?string == "Refused">${msg("state.product.refused")}
      <#elseif nameValue?string == "Archived">${msg("state.product.archived")}
      <#else>${nameValue?html}</#if>
</#macro>