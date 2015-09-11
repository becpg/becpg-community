<#macro renderDeliverables deliverables>
	<#list deliverables as deliverable>
		<#if deliverable?? && deliverable.hasPermission("Read")>
			{
				"name": "${deliverable.properties["pjt:dlDescription"]!""}",
				"nodeRef": "${deliverable.nodeRef}",
				"state": "${deliverable.properties["pjt:dlState"]!""}",
				"url": "${deliverable.properties["pjt:dlUrl"]!""}",
		   		"completionPercent": "${deliverable.properties["pjt:completionPercent"]!""}",
		   		"commentCount":"${deliverable.properties["fm:commentCount"]!""}",
		   		"contents": [
	   			<#if deliverable.assocs["pjt:dlContent"]?exists>
		   			<#list deliverable.assocs["pjt:dlContent"] as content>
		                  <#if content.hasPermission("Read")>
			   				{
								"name": "${content.properties.name!""}",
								"nodeRef": "${content.nodeRef}",
								"type": "${content.typeShort}",
								"siteId": "${content.getSiteShortName()!""}",
								"path": "${content.displayPath!""}",
                                "isContainer" : ${content.isContainer?string}
								}
			   				<#if content_has_next>,</#if>
						</#if>
		   			</#list>
	   			</#if>
	   			] 
			}<#if deliverable_has_next>,</#if>	
		</#if>	
	</#list>
</#macro>

<#if task?? && task.hasPermission("Read")>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"task":
	{
		<#if task.parent.hasPermission("Read") &&  task.parent.parent.hasPermission("Read") &&  task.parent.parent.parent.hasPermission("Read") >
		 "entityNodeRef" : "${task.parent.parent.parent.nodeRef}",
		<#else>
		  "entityNodeRef" : "#access_forbidden",
		</#if>
		"name": "${task.properties["pjt:tlTaskName"]!""}",
		"description": "${task.properties["pjt:tlTaskDescription"]!""}",
	   "state": "${task.properties["pjt:tlState"]!""}",
	   "completionPercent": "${task.properties["pjt:completionPercent"]!""}",
	   "commentCount":"${task.properties["fm:commentCount"]!""}",
	   "nodeRef": "${task.nodeRef}",
	   "isRefusedEnabled": <#if task.assocs["pjt:tlRefusedTaskRef"]?? && task.assocs["pjt:tlRefusedTaskRef"]?size &gt; 0 >true<#else>false</#if>,
	   "deliverables":
		[
		<#if deliverables??>
			<@renderDeliverables deliverables/>
		</#if>
		]
	}
}
</#escape>
</#if>