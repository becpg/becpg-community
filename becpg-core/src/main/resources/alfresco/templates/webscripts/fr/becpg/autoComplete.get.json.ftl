<#escape x as jsonUtils.encodeJSONString(x)>
{	
   "result":
   [
	<#list suggestions?keys as key>      	
		{"value": "${key}","name": "${suggestions[key]}","type":"${companyhome.nodeByReference[key].type?replace("{http://www.bcpg.fr/model/becpg/1.0}","")}"}<#if key_has_next>,</#if>
	</#list>
   
   ]
}
</#escape>