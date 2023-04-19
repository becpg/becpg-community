<#escape x as jsonUtils.encodeJSONString(x)>
{	
   "result":
   [
	<#list columns as col>      	
		{"value": "${col.name}","name": "${col.label}","type": "${col.type}"}<#if col_has_next>,</#if>
	</#list>
   ]
   ,"page" : "${pageNumber}"
   ,"pageSize":"${pageSize}"
   ,"fullListSize":"${fullListSize}"
}
</#escape>