<#escape x as jsonUtils.encodeJSONString(x)>
{
   "status": "${status}",
   "systemInfo" : {
  		"diskFreeSpace": ${diskFreeSpace?c},
  		"diskTotalSpace": ${diskTotalSpace?c},
   		"totalMemory": ${totalMemory?c},
		"freeMemory": ${freeMemory?c},
		"maxMemory": ${maxMemory?c},
		"connectedUsers": ${connectedUsers?c},
		"concurrentReadUsers": ${concurrentReadUsers?c},
		"concurrentSupplierUsers": ${concurrentSupplierUsers?c},
		"concurrentWriteUsers": ${concurrentWriteUsers?c},
	    "namedReadUsers": ${namedReadUsers?c},
		"namedWriteUsers": ${namedWriteUsers?c},
		"withoutLicenseUsers": ${withoutLicenseUsers?c},
		"license" : {
		    "name":"${licenseName}",
			"allowedConcurrentRead": ${allowedConcurrentRead?c},
			"allowedConcurrentWrite": ${allowedConcurrentWrite?c},
			"allowedConcurrentSupplier": ${allowedConcurrentSupplier?c},
			"allowedNamedWrite": ${allowedNamedWrite?c},
			"allowedNamedRead": ${allowedNamedRead?c}
		},
		"nonHeapMemoryUsage": ${nonHeapMemoryUsage?c},
		"becpgSchema": "${becpgSchema?js_string}",
		"batchCounts": ${batchCounts?c}
   }
   <#if sites??>
   , "sites" :
   [
    <#list sites as site>
     {
      "sitePreset": "${site.sitePreset}",
	   "shortName": "${site.shortName}"
     }<#if site_has_next>,</#if>
    </#list>
   ]
   </#if>
   <#if users?? >
	,"users":
	   [
	      <#list users as userItem>
	      <#if userItem?is_hash>
	      	<#assign item = userItem.username>
	      	<#assign licenseGroup = userItem.licenseGroup!"" >
	      	<#assign customFullName = userItem.fullName!"" >
	      <#else>
	      	<#assign item = userItem>
	      	<#assign licenseGroup = "" >
	      	<#assign customFullName = "" >
	      </#if>
	      {
	      <#attempt>
	      	<#if people.getPerson(item) ??>
	     	 <#assign currentPerson = people.getPerson(item)>
	         	 "username" : "${item?js_string}",
		         "fullName" : "${((currentPerson.properties["cm:firstName"]!"") + " " + (currentPerson.properties["cm:lastName"]!""))?js_string}",
		         "email" : "${(currentPerson.properties["cm:email"]!"")?js_string}",
		         "licenseGroup" : "${licenseGroup?js_string}"
		     <#else>
		     	  "username" : "${item?js_string}",
		      	  "fullName" : "<#if customFullName != "">${customFullName?js_string}<#else>${item?js_string}</#if>",
	        	  "email" : "",
	        	  "licenseGroup" : "${licenseGroup?js_string}"
	         </#if>
	      <#recover>
	      	"username" : "${item?js_string}",
	        "fullName" : "<#if customFullName != "">${customFullName?js_string}<#else>${item?js_string}</#if>",
	        "email" : "",
	        "licenseGroup" : "${licenseGroup?js_string}"
	      </#attempt>   
	      }<#if userItem_has_next>,</#if>
	     </#list>
	   ]
	  </#if>
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