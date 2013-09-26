<#escape x as jsonUtils.encodeJSONString(x)>
{
   "status": "${status}"
   ,  "systemInfo" : {
   	"totalMemory": ${totalMemory?c},
		"freeMemory": ${freeMemory?c},
		"maxMemory": ${maxMemory?c},
		"connectedUsers": ${connectedUsers?c},
		"nonHeapMemoryUsage": ${nonHeapMemoryUsage?c}
   }
	<#if systemEntities?? >
	,"systemEntities":
	   [
	      <#list systemEntities as item>
	      {
	         "nodeRef" : "${item.nodeRef}",
	         "name" : "${item.name}",
	         "title" : "${item.properties.title!""}",
				"description": "${item.properties.description!""}"
	      }<#if item_has_next>,</#if>
	     </#list>
	   ]
	  </#if>
	  <#if systemFolders?? >
	,"systemFolders":
	   [
	      <#list systemFolders as item>
	      {
	         "nodeRef" : "${item.nodeRef}",
	         "name" : "${item.name}",
	         "title" : "${item.properties.title!""}",
				"description": "${item.properties.description!""}",
	         "path": "${item.displayPath}"
	      }<#if item_has_next>,</#if>
	     </#list>
	   ]
	  </#if>
}
</#escape>