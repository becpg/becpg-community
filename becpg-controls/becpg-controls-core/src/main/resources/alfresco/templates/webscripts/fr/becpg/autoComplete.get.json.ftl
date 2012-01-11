<#escape x as jsonUtils.encodeJSONString(x)>
{	
   "result":
   [
<#list suggestions.results?keys as key>
			{"value": "${key}"
			,"name": "${suggestions.results[key]}"
			<#if suggestions.isNodeRef >
			,"type":"${companyhome.nodeByReference[key].type?replace("{http://www.bcpg.fr/model/becpg/1.0}","")}"
			<#else>
			,"type":"${suggestions.type}"
			</#if>}
			<#if key_has_next >,</#if>
</#list>
   ]
   ,"page" : "${suggestions.pageNumber}"
   ,"pageSize":"${suggestions.objectsPerPage}"
   ,"fullListSize":"${suggestions.fullListSize}"
}
</#escape>