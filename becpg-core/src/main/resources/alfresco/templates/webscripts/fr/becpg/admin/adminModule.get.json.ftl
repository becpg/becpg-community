<#escape x as jsonUtils.encodeJSONString(x)>
{
   "status": "${status}"
   ,  "systemInfo" : {
   	"totalMemory": ${totalMemory?c},
		"freeMemory": ${freeMemory?c},
		"maxMemory": ${maxMemory?c},
		"nonHeapMemoryUsage": ${nonHeapMemoryUsage?c}
   }
	<#if items?? >
	,"items":
	   [
	      <#list items as item>
	      {
	         "nodeRef" : "${item.nodeRef}",
	         "name" : "${item.name}" 
	      }<#if item_has_next>,</#if>
	     </#list>
	   ]
	  </#if>
}
</#escape>