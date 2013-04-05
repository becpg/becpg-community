<#if task??>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"task":
	{
		"entityNodeRef" : "${task.parent.parent.nodeRef}",
		"name": "${task.properties["pjt:tlTaskName"]!""}",
	   "state": "${task.properties["pjt:tlState"]!""}",
	   "completionPercent": "${task.properties["pjt:completionPercent"]!""}",
	   "nodeRef": "${task.nodeRef}",
		"deliverables":
		[
		<#if deliverables??>
			<#list deliverables as deliverable>
				{
					"name": "${deliverable.properties["pjt:dlDescription"]!""}",
					"nodeRef": "${deliverable.nodeRef}",
					"state": "${deliverable.properties["pjt:dlState"]!""}",
		   		"completionPercent": "${deliverable.properties["pjt:completionPercent"]!""}",
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
			</#list>
		</#if>
		]
	}
}
</#escape>
</#if>