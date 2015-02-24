<#include "/org/alfresco/components/form/controls/common/utils.inc.ftl" />
<#assign fieldValue=field.value>

<#if fieldValue?string != "">
   <#assign values=fieldValue?split(",")>
<#else>
   <#assign values=[]>
</#if>

<#if field.label?replace(":","_") != field.name >

<div class="form-field">
   <#if form.mode == "view">
      <div class="viewmode-field">
         <span class="viewmode-label">${field.label?html}:</span>
         <#if fieldValue?string == "">
            <span class="viewmode-value">${msg("form.control.novalue")}</span></span>
         <#else>
           <span class="viewmode-value">
			   <#list values as value>
			      <img  src="${url.context}/res/components/images/flags/${value}.png" />
			  </#list>
			</span>
         </#if>
      </div>
   <#else>
      <label for="${fieldHtmlId}-entry">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
      <input id="${fieldHtmlId}" type="hidden" name="${field.name}" value="${fieldValue?string}" />
         <select id="${fieldHtmlId}-entry" name="-" multiple="multiple" size="5" index="0"
               onchange="javascript:Alfresco.util.updateMultiSelectListValue('${fieldHtmlId}-entry', '${fieldHtmlId}', <#if field.mandatory>true<#else>false</#if>);"
               <#if field.description??>title="${field.description}"</#if> 
               <#if field.control.params.styleClass??>class="${field.control.params.styleClass}"</#if>
               <#if field.control.params.style??>style="${field.control.params.style}"</#if>
               <#if field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>disabled="true"</#if>>
			   <#assign h = config.scoped["Languages"]["languages"]>
                <#if field.control.params.insertBlank??>
               		 <option value="" <#if field.control.params.isSearch?? >selected="selected"</#if> ></option>
               </#if>
			   <#list  h.getChildren("language") as language>
				    <#assign key = language.getAttribute("locale")>				
					<option value=${key?split("_")[0]} <#if isSelected(key?split("_")[0])>selected="true"</#if> >${msg("locale.name.${key}")}</option>
			  </#list>             
         </select>
         <@formLib.renderFieldHelp field=field />
	</#if>
</div>
</#if>

<#function isSelected optionValue>
   <#list values as value>
      <#if optionValue == value?string || (value?is_number && value?c == optionValue) && !field.control.params.isSearch?? >
         <#return true>
      </#if>
   </#list>
   <#return false>
</#function>
