<#assign controlId = fieldHtmlId + "-cntrl">
<#if field.control.params.rows??><#assign rows=field.control.params.rows><#else><#assign rows=2></#if>
<#if field.control.params.columns??><#assign columns=field.control.params.columns><#else><#assign columns=60></#if>
<script type="text/javascript">//<![CDATA[
(function()
{
		   new beCPG.ConfigEditor("${controlId}", "${fieldHtmlId}").setOptions(
		   {
		    <#if form.mode == "view" || (field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true"))>disabled: true,</#if>
		      field: "${field.name}",
		    <#if field.mandatory??>
		      mandatory: ${field.mandatory?string},
		    </#if>
		    <#if args.entityNodeRef??>
		      entityNodeRef : "${args.entityNodeRef}",
		    </#if>
		    <#if args.dataListsName??>
		      currentList : "${args.dataListsName}",
		    </#if>
		    <#if field.control.params.syntax??>
		      syntax: "${field.control.params.syntax}",
		    </#if>
		      currentValue: "${field.value?js_string}",
		   }).setMessages( ${messages});
})();
//]]></script>
<div class="form-field">
   <#if form.mode == "view">
      <div id="${controlId}" class="viewmode-field">
         <#if (field.endpointMandatory!false || field.mandatory!false) && field.value == "">
            <span class="incomplete-warning"><img class="icon16" src="${url.context}/res/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
         </#if>
         <span class="viewmode-label">${field.label?html}:</span>
         <div id="${controlId}-currentValueDisplay" class="viewmode-value current-formula"></div>
      </div>
   <#else>
      <label for="${controlId}">${field.label?html}:<#if field.endpointMandatory!false || field.mandatory!false><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
      
      <div id="${controlId}" class="spel-editor-control">
         
         <div id="${controlId}-currentValueDisplay" class="current-formula"></div>
         <input type="hidden"  name="${field.name}" id="${fieldHtmlId}" value="${field.value?html}" />
         
         <#if field.disabled == false>
            <div id="${controlId}-showEditorAction" class="show-editor"></div>
         
               <#assign editorId = controlId + "-editor">
					<div id="${editorId}" class="editor yui-panel" style="visibility:hidden;">
					   <div id="${editorId}-head" class="hd">${msg("form.control.config-editor.header")}</div>
					
					   <div id="${editorId}-body" class="bd">
					      <div class="editor-textarea yui-b">
					          <span class="editor-hint">${msg("form.control.spel-editor.hint")}</span>
					      	 <textarea id="${editorId}-textarea" rows="${rows}" name="-"> </textarea>
					      </div>
					      <div class="bdft">
					         <button id="${controlId}-ok" tabindex="0">${msg("button.ok")}</button>
					         <button id="${controlId}-cancel" tabindex="0">${msg("button.cancel")}</button>
					      </div>
					   </div>
					</div>
         </#if>
      </div>
   </#if>
</div>
