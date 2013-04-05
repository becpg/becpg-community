
<@markup id="css" >
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/components/project/project-list.css" group="project-list" />
</@>

<@markup id="js">
	<@script type="text/javascript" src="${url.context}/res/modules/simple-dialog.js" group="form"></@script>
	<@script type="text/javascript" src="${url.context}/res/components/project/project-commons.js" group="form" />
	<@script type="text/javascript" src="${url.context}/components/form/controls/workflow/project-task.js" group="form" />
</@>

<#assign controlId = fieldHtmlId + "-cntrl">

<script type="text/javascript">//<![CDATA[
(function()
{
  new beCPG.component.ProjectTask("${controlId}").setOptions({taskNodeRef:"${field.value!""}"<#if form.mode == "view">,readOnly:true</#if> }).setMessages(${messages});
})();
//]]></script>

<div class="form-field project-list project-task">
      <div id="${controlId}" class="viewmode-field">
         <#if (field.endpointMandatory!false || field.mandatory!false) && field.value == "">
            <span class="incomplete-warning"><img src="${url.context}/res/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
         </#if>
         <span class="viewmode-label">${field.label?html}:</span>
         <span id="${controlId}-currentTask" class="viewmode-value current-values"></span>
      </div>
      <#if field.value?? && field.value != "">
      	<div  class="viewmode-field">
      		<span class="viewmode-label">${msg("form.control.project-task.deliverables")}:</span>
        		<div id="${controlId}-currentDeliverableList" class="viewmode-value current-values"></div>
      	</div>
      </#if>
</div>
