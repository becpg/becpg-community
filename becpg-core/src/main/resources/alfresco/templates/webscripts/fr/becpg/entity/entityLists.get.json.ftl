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
	   "state"  :"${list.properties["bcpg:entityDataListState"]!"ToValidate"}",
	   "permissions":
	   {
	      "edit": ${hasWritePermission?string},
	      "delete": ${hasWritePermission?string}
	   }
	}<#if list_has_next>,</#if>
   </#list>
   ],
   "entity" : {
         "nodeRef": "${entity.nodeRef}",
         "parentNodeRef" : "${entity.parent.nodeRef}",
         "name": "${entity.name}",
         "userAccess":
         {
            "create": ${entity.hasPermission("CreateChildren")?string},
            "edit": ${entity.hasPermission("Write")?string},
            "delete": ${entity.hasPermission("Delete")?string}
         },
         "aspects": 
         [
         <#list entity.aspects as aspect>
            "${shortQName(aspect)}"<#if aspect_has_next>,</#if>
         </#list>
         ],
         "type": "${shortQName(entity.type)}",
         "path": "${entityPath}"
         <#if entity.hasAspect("bcpg:entityVariantAspect") && entity.childAssocs["bcpg:variants"]??>
         ,"variants" : [
         	 <#list entity.childAssocs["bcpg:variants"] as variant>
         	 	{
         	 	 "nodeRef" : "${variant.nodeRef}",
         	    "name": "${variant.name}",
         	    <#if variant.properties["bcpg:isDefaultVariant"]??>
         	     "isDefaultVariant" :${variant.properties["bcpg:isDefaultVariant"]?string}
         	    <#else>
         	     "isDefaultVariant" : false
         	    </#if>
         	 	}
         		<#if variant_has_next>,</#if></#list>]
         </#if>
 
    }
}
</#escape>