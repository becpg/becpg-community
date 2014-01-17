<#escape x as jsonUtils.encodeJSONString(x)>
{	
   "result":
   [
<#list suggestions.results as result>
			{"value": "${result.value}"
			,"name": "${result.name}"
			,"cssClass":"${result.cssClass}"
			,"metadatas": [
			<#list result.metadatas?keys as key>
				{"key": "${key}",
				"value": "${result.metadatas[key]}"}
				<#if key_has_next >,</#if>
			</#list>
			]}
			<#if result_has_next >,</#if>
</#list>
   ]
   ,"page" : "${suggestions.pageNumber}"
   ,"pageSize":"${suggestions.objectsPerPage}"
   ,"fullListSize":"${suggestions.fullListSize}"
}</#escape>