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
   <#if listTypes??>
   "listTypes" : [<#list listTypes as classdef>
      {
       <#if classdef.name??>"name": "${classdef.name.toPrefixString()}",</#if>
      "title": "${classdef.title!""}",
      "description": "${classdef.description!""}"
     }<#if classdef_has_next>,</#if></#list>],
    </#if>
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
	   "editableList": ${editableLists?seq_contains(list)?string("true", "false")},	   
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