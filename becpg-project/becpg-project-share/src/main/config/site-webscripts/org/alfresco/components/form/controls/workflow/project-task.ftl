
<@markup id="css" >
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/components/project/project-commons.css" group="project-list" />
    <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/form/controls/workflow/project-task.css" group="project-list" />
	<@link href="${url.context}/res/components/comments/comments-list.css" group="comments"/>
</@>

<@markup id="js">
	<@script type="text/javascript" src="${url.context}/res/components/project/project-commons.js" group="form" />
	<@script type="text/javascript" src="${url.context}/res/components/form/controls/workflow/project-task.js" group="form" />
	<@script src="${url.context}/res/components/comments/comments-list.js" group="comments"/>
</@>

<#assign controlId = fieldHtmlId + "-cntrl">

<@markup id="widgets">
   	<@inlineScript group="form">
  			new beCPG.component.ProjectTask("${controlId}").setOptions({taskNodeRef:"${field.value!""}"<#if form.mode == "view">,readOnly:true</#if> }).setMessages(${messages});
		</@>
</@>

<div class="form-field project-list project-task ">
	<div class="set">
		<div class="2-col-set">	
			<div class="yui-g">				
				<div id="${controlId}" class="viewmode-field">
		         <#if (field.endpointMandatory!false || field.mandatory!false) && field.value == "">
		            <span class="incomplete-warning"><img src="${url.context}/res/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
		         </#if>
		         <span class="viewmode-label">${field.label?html}:</span>
		         <span id="${controlId}-currentTask" class="viewmode-value current-values"></span>
		      </div>

				<div class="viewmode-field">
		      		<span class="viewmode-label">${msg("label.description")}:</span>
		     		<span id="${controlId}-currentTask-description" class="viewmode-value current-values"></span>
		   		</div>	
		   		
			</div>
		
			<#if field.value?? && field.value != "">
				<div id="${controlId}-deliverableList" class="viewmode-field hidden">
			      	<span class="viewmode-label">${msg("form.control.project-task.deliverables")}:</span>
			        <div id="${controlId}-currentDeliverableList" class="viewmode-value current-values"></div>
			     </div>
 			</#if>	
 			
 		</div> 
	</div>         
</div>
