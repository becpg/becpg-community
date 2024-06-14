<#escape x as jsonUtils.encodeJSONString(x)>
{
   "status": "${status}"<#if authenticated??>,
   "authenticated": true,
	   "systemInfo" : {
	   		"diskFreeSpace": ${diskFreeSpace?c},
	   		"diskTotalSpace": ${diskTotalSpace?c},
	   		"totalMemory": ${totalMemory?c},
			"freeMemory": ${freeMemory?c},
			"maxMemory": ${maxMemory?c},
			"nonHeapMemoryUsage": ${nonHeapMemoryUsage?c},
			"connectedUsers": ${connectedUsers?c},
			"concurrentReadUsers": ${concurrentReadUsers?c},
			"concurrentWriteUsers": ${concurrentWriteUsers?c},
			"concurrentSupplierUsers": ${concurrentSupplierUsers?c},
			"namedReadUsers": ${namedReadUsers?c},
			"namedWriteUsers": ${namedWriteUsers?c},
			"allowedConcurrentRead": ${allowedConcurrentRead?c},
			"allowedConcurrentWrite": ${allowedConcurrentWrite?c},
			"allowedConcurrentSupplier": ${allowedConcurrentSupplier?c},
			"allowedNamedWrite": ${allowedNamedWrite?c},
			"allowedNamedRead": ${allowedNamedRead?c},
			"licenseName": "${licenseName}",
			"withoutLicenseUsers": ${withoutLicenseUsers?c},
			"becpgSchema": "${becpgSchema}",
	   }
	</#if>
}
</#escape>
