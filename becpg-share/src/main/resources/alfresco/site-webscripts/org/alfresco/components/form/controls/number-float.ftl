<#if field.control.params.format??><#assign format=field.control.params.format><#else><#assign format="0.####"></#if>
<div class="form-field">
	<#if field.dataKeyName?? && field.dataType??>
	   <#if form.mode == "view">
	      <div class="viewmode-field">
	         <#if field.mandatory && !(field.value?is_number) && field.value == "">
	            <span class="incomplete-warning"><img class="icon16" src="${url.context}/res/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
	         </#if>
	         <span class="viewmode-label">${field.label?html}:</span>
	         <span class="viewmode-value"><#if field.value?is_number>${field.value?string("${format}")}<#elseif field.value == "">${msg("form.control.novalue")}<#else>${field.value?html}</#if></span>
	      </div>
	   <#else>
	      <label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
	      <input id="${fieldHtmlId}" type="text" name="${field.name}" tabindex="0"
	             class="number<#if field.control.params.styleClass??> ${field.control.params.styleClass}</#if>"
	             <#if field.control.params.style??>style="${field.control.params.style}"</#if>	
				 <#setting locale="en_US">
	             <#if field.value?is_number>value="${field.value?string("${format}")}"<#else>value="${field.value?html}"</#if>
	             <#if field.description??>title="${field.description}"</#if>
	             <#if field.control.params.maxLength??>maxlength="${field.control.params.maxLength}"</#if> 
	             <#if field.control.params.size??>size="${field.control.params.size}"</#if> 
	             <#if field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>disabled="true"</#if> />
	             <#if !field.disabled || (field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>
			      	<@formLib.renderFieldHelp field=field />
			     </#if>
	   </#if>
   </#if>
</div>