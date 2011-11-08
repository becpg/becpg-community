<#escape x as jsonUtils.encodeJSONString(x)>
{
  
   <#if aclType??>"aclType":"${aclType}",</#if>
   <#if container??>
   "container": "${container.nodeRef?string}",
   </#if>
   "permissions":
   {
      "create": ${hasWritePermission?string}
   },
   "datalists":
   [
   <#list lists as list>
    {
       "entityName" : "${entity.name}",
	   "name": "${list.name}",
	   "title": "${list.properties.title!list.name}",
	   "description": "${list.properties.description!""}",
	   "nodeRef": "${list.nodeRef}",
	   "itemType": "${list.properties["dl:dataListItemType"]!""}",
	   "permissions":
	   {
	      "edit": ${hasWritePermission?string},
	      "delete": ${hasWritePermission?string}
	   }
	}<#if list_has_next>,</#if>
   </#list>
   <#if wUsedList??>
   		<#if lists?size != 0>
   	,
		</#if>
	{
	   "entityName" : "${entity.name}",
	   "name": "WUsed",
	   "title": "${message('entity-datalist-wused-title')}",
	   "description": "${message('entity-datalist-wused-description')}",
	   "nodeRef": "",
	   "itemType": "${wUsedList}",
	   "permissions":
	   {
	      "edit": false,
	      "delete": false
	   }
	}
	</#if>
   ]
}
</#escape>