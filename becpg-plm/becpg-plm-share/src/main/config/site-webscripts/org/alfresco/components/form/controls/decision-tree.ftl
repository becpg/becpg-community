<#assign controlId = fieldHtmlId + "-cntrl">

<#--
<@markup id="css" >
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/components/form/controls/decision-tree.css" group="form" />
</@>

<@markup id="js">
	<@script type="text/javascript" src="${url.context}/res/components/form/controls/decision-tree.js" group="form" />
</@>
-->


<@markup id="widgets">
   	<@inlineScript group="form">
		   new beCPG.component.DecisionTree("${controlId}", "${fieldHtmlId}").setOptions(
		   {
		   	<#if form.mode == "view" || (field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true"))>
		   	  disabled: true,
		   	</#if>
		   	  prefix : "${field.control.params.prefix?string}",
		      data: ${field.control.params.data?string}
		      <#if field.value?? && field.value != "" >
		      ,currentValue: ${field.value?string}
		      </#if>
		   }).setMessages(
		      ${messages}
		   );
		</@>
</@>

<div class="form-field">
   <#if form.mode == "view">
      <div id="${controlId}" class="viewmode-field">
         <#if (field.endpointMandatory!false || field.mandatory!false) && field.value == "">
            <span class="incomplete-warning"><img src="${url.context}/res/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
         </#if>
         <span class="viewmode-label">${field.label?html}:</span>
         <div id="${controlId}-body" ></div>
      </div>
   <#else>
      <label for="${controlId}">${field.label?html}:</label>
      <div id="${controlId}" class="decision-tree-control">
         <div id="${controlId}-body" ></div>
         <input type="hidden"  name="${field.name}" id="${fieldHtmlId}" value="${field.value?html}" />
      </div>
   </#if>
</div>
