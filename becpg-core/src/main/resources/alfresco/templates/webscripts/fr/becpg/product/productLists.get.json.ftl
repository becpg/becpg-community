<#escape x as jsonUtils.encodeJSONString(x)>
{
   "container": "${container.nodeRef?string}",
   "permissions":
   {
      "create": ${hasWritePermission?string}
   },
   "datalists":
   [
   <#list lists as list>
    {
    	"productName" : "${product.name}",
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
   <#if showWUsedItems>
   		<#if lists?size != 0>
   	,
		</#if>
	{
		"productName" : "${product.name}",
	   "name": "WUsed",
	   "title": "${message('product-datalist-wused-title')}",
	   "description": "${message('product-datalist-wused-description')}",
	   "nodeRef": "",
	   "itemType": "bcpg:compoList",
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