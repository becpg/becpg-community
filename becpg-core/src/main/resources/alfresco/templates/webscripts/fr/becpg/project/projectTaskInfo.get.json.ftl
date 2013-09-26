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
	   "state": "${task.properties["pjt:tlState"]!""}",
	   "completionPercent": "${task.properties["pjt:completionPercent"]!""}",
	   "commentCount":"${task.properties["fm:commentCount"]!""}",
	   "nodeRef": "${task.nodeRef}",
		"deliverables":
		[
		<#if deliverables??>
			<#list deliverables as deliverable>
			<#if deliverable?? && deliverable.hasPermission("Read")>
				{
					"name": "${deliverable.properties["pjt:dlDescription"]!""}",
					"nodeRef": "${deliverable.nodeRef}",
					"state": "${deliverable.properties["pjt:dlState"]!""}",
		   		"completionPercent": "${deliverable.properties["pjt:completionPercent"]!""}",
		   		"commentCount":"${deliverable.properties["fm:commentCount"]!""}",
		   		"contents": [
		   			<#if deliverable.assocs["pjt:dlContent"]?exists>
			   			<#list deliverable.assocs["pjt:dlContent"] as content>
			   				{
								"name": "${content.properties.name!""}",
								"nodeRef": "${content.nodeRef}",
								"type": "${content.typeShort}"
								}
			   				<#if content_has_next>,</#if>
			   			</#list>
		   			</#if>
		   		] 
				}<#if deliverable_has_next>,</#if>	
			</#if>	
			</#list>
		</#if>
		]
	}
}
</#escape>
</#if>