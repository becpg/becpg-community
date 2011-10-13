<#escape x as jsonUtils.encodeJSONString(x)>
{	
   "result":
   [
	<#list suggestions.results?keys as key>      	
		{"value": "${key}","name": "${suggestions.results[key]}","type":"${companyhome.nodeByReference[key].type?replace("{http://www.bcpg.fr/model/becpg/1.0}","")}"}<#if key_has_next>,</#if>
	</#list>
   ]
   ,"page" : "${suggestions.pageNumber}"
   ,"pageSize":"${suggestions.objectsPerPage}"
   ,"fullListSize":"${suggestions.fullListSize}"
}
</#escape>