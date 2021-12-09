<#include "common/editorparams.inc.ftl" />

<#if field.control.params.rows??><#assign rows=field.control.params.rows><#else><#assign rows=8></#if>
<#if field.control.params.columns??><#assign columns=field.control.params.columns><#else><#assign columns=60></#if>

<div class="form-field">
   <#if form.mode == "view">
   <div class="viewmode-field">
      <#if field.mandatory && field.value == "">
      <span class="incomplete-warning"><img src="${url.context}/res/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
      </#if>
      <span class="viewmode-label">${field.label?html}:</span>
      <span class="viewmode-value"><#if field.value == "">${msg("form.control.novalue")}<#else>${field.value?html}</#if></span>
   </div>
   <#else>
   
   <#assign fieldValue=field.value>
	   <#if fieldValue?string == "" && field.control.params.defaultValue ??>
			<#assign fieldValue=field.control.params.defaultValue>
	   </#if>
	   
   <script type="text/javascript">//<![CDATA[
   (function() {
      new Alfresco.RichTextControl("${fieldHtmlId}").setOptions(
      {
         <#if form.mode == "view" || (field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true"))>disabled: true,</#if>
         currentValue: "${fieldValue?js_string}",
         mandatory: ${field.mandatory?string},
         <@editorParameters field />
      }).setMessages(${messages});
   })();
   //]]></script>
   
  
   <label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if>
 	<#if  field.dataType == "mltext"><@formLib.renderLocaleImage field=field textarea=true htmlEditor=true/></#if>  
   </label>
   <textarea id="${fieldHtmlId}" name="${field.name}" rows="${rows}" columns="${columns}" tabindex="0"
      <#if field.description??>title="${field.description}"</#if>
      <#if field.control.params.styleClass??>class="${field.control.params.styleClass}"</#if>
      <#if field.control.params.style??>style="${field.control.params.style}"</#if>
      <#if field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>disabled="true"</#if>>${fieldValue?html}</textarea>
   </#if>
</div>