<#escape x as jsonUtils.encodeJSONString(x)>
{
   "status": "${status}",
   "systemInfo" : {
   		"totalMemory": ${totalMemory?c},
		"freeMemory": ${freeMemory?c},
		"maxMemory": ${maxMemory?c},
		"connectedUsers": ${connectedUsers?c},
		"nonHeapMemoryUsage": ${nonHeapMemoryUsage?c},
		"becpgSchema": "${becpgSchema?js_string}"
   }
   <#if sites??>
   , "sites" :
   [
    <#list sites as site>
     {
      "sitePreset": "${site.sitePreset}",
	   "shortName": "${site.shortName}"
     }
    </#list>
   ]
   </#if>
   <#if users?? >
	,"users":
	   [
	      <#list users as item>
	      {
	      <#attempt>
	      	<#if people.getPerson(item) ??>
	     	 <#assign currentPerson = people.getPerson(item)>
	         	 "username" : "${item}",
		         "fullName" : "${currentPerson.properties["cm:firstName"]} ${currentPerson.properties["cm:lastName"]}",
		         "email" : "${currentPerson.properties["cm:email"]}"
		     <#else>
		     	  "username" : "${item}",
		      	  "fullName" : "${item}",
	        	  "email" : ""  
	         </#if>
	      <#recover>
	      	"username" : "${item}",
	        "fullName" : "${item}",
	        "email" : ""
	      </#attempt>   
	      }<#if item_has_next>,</#if>
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