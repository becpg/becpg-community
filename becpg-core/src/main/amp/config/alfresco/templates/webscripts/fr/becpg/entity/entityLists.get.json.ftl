<#escape x as jsonUtils.encodeJSONString(x)>
{
   <#if aclType??>"aclType":"${aclType}",</#if>
   <#if container??>
   "container": "${container.nodeRef?string}",
   </#if>
   "permissions":
   {
      "create": <#if hasWritePermission && !entity.isLocked>true<#else>false</#if>
   },
   <#if listTypes??>
   "listTypes" : [<#list listTypes as classdef>
      {
       <#if classdef.name??>"name": "${classdef.name.toPrefixString()}",</#if>
      "title": "${classdef.title!""}",
      "description": "${classdef.description!""}"
     },</#list>
     {
	     "name":"CustomView",
	     "title":"${msg('entity-datalist-customview-title')}",
	     "description":"${msg('entity-datalist-customview-description')}"
     }
     ],
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
	      "edit": <#if hasWritePermission && !entity.isLocked>true<#else>false</#if>,
	      "delete": <#if hasWritePermission && !entity.isLocked>true<#else>false</#if>,
	      "changeState": <#if hasChangeStatePermission && !entity.isLocked>true<#else>false</#if>
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
            "create": <#if entity.hasPermission("CreateChildren") && !entity.isLocked>true<#else>false</#if>,
            "edit": <#if entity.hasPermission("Write") && !entity.isLocked>true<#else>false</#if>,
            "delete":<#if entity.hasPermission("Delete") && !entity.isLocked>true<#else>false</#if>
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
         <#if entity.hasAspect("bcpg:compareWithAspect") && entity.assocs["bcpg:compareWithEntities"]??>
         ,"compareWithEntities" : [
            <#list entity.assocs["bcpg:compareWithEntities"] as compareWithEntity>
         	{
         	 "nodeRef" : "${compareWithEntity.nodeRef}",
         	 "name": "${compareWithEntity.name}"
         	}
         		<#if compareWithEntity_has_next>,</#if></#list>]
         </#if>
 
    }
}
</#escape>