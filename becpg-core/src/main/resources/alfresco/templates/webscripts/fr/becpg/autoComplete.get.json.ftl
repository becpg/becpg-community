<#escape x as jsonUtils.encodeJSONString(x)>
{	
   "result":
   [
	<#list suggestions?keys as key>      	
		{"value": "${key}","name": "${suggestions[key]}"}<#if key_has_next>,</#if>
	</#list>
   
   ]
}
</#escape>