<#escape x as jsonUtils.encodeJSONString(x)>
{
   "status": "${status}",
	<#if diskFreeSpace??>
	   "systemInfo" : {
	   		"diskFreeSpace": ${diskFreeSpace?c},
	   		"diskTotalSpace": ${diskTotalSpace?c},
	   		"totalMemory": ${totalMemory?c},
			"freeMemory": ${freeMemory?c},
			"maxMemory": ${maxMemory?c},
			"connectedUsers": ${connectedUsers?c},
			"nonHeapMemoryUsage": ${nonHeapMemoryUsage?c}
	   }
	</#if>
}
</#escape>