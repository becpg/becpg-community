<#macro renderDeliverables deliverables>
	<#list deliverables as deliverable>
		<#if deliverable?? && deliverable.hasPermission("Read")>
			<#escape x as jsonUtils.encodeJSONString(x)>
				{
					"name": "${deliverable.properties["pjt:dlDescription"]!""}",
					"sort": ${(deliverable.properties["bcpg:sort"]!0)?c},
					"nodeRef": "${deliverable.nodeRef}",
					"state": "${deliverable.properties["pjt:dlState"]!""}",
					"url": "${urlMap[deliverable.nodeRef.toString()]!""}",
			   		"completionPercent": "${deliverable.properties["pjt:completionPercent"]!""}",
			   		"commentCount":"${deliverable.properties["fm:commentCount"]!""}",
			   		"contents": [
		   			<#if contentMap[deliverable.nodeRef.toString()]?exists>
			   			<#list contentMap[deliverable.nodeRef.toString()] as content>
			                  <#if content?? && content != "" && content.hasPermission("Read")>
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
			</#escape>
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