<#macro renderParent node indent="   ">
	<#escape x as jsonUtils.encodeJSONString(x)>
	${indent}"parent":
	${indent}{
	<#if (node != rootNode) && node.parent??>
		<@renderParent node.parent indent+"   " />
	</#if>
		${indent}"type": "${node.typeShort}",
		${indent}"isContainer": ${node.isContainer?string},
		${indent}"name": "${node.properties.name!""}",
		${indent}"title": "${node.properties.title!""}",
		${indent}"description": "${node.properties.description!""}",
		<#if node.properties.modified??>${indent}"modified": "${xmldate(node.properties.modified)}",</#if>
		<#if node.properties.modifier??>${indent}"modifier": "${node.properties.modifier}",</#if>
		${indent}"displayPath": "${node.displayPath!""}",
		${indent}"nodeRef": "${node.nodeRef}"
	${indent}},
	</#escape>
</#macro>

<#macro pickerResultsJSON results>
	<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data":
	{
<#if parent??>
	<@renderParent parent />
</#if>
		"items":
		[
		<#list results as row>
			{
				"type": "${row.item.typeShort}",
				"parentType": "${row.item.parentTypeShort!""}",
				"isContainer": ${row.item.isContainer?string},
				<#if row.item.typeShort == "bcpg:dynamicCharactList" >
				"name": "${row.item.properties["bcpg:dynamicCharactTitle"]!""}",
				"title": "${row.item.properties["bcpg:dynamicCharactTitle"]!""}",
				<#elseif row.item.typeShort == "bcpg:linkedValue" >
				"name": "${row.item.properties["bcpg:lkvValue"]!""}",
				"title": "${row.item.properties["bcpg:lkvValue"]!""}",
				<#elseif row.item.typeShort == "bcpg:compoList" >				
				"name": "${row.item.assocs["bcpg:compoListProduct"][0].properties.name!""}",
				"title": "${row.item.assocs["bcpg:compoListProduct"][0].properties.name!""}",	
				<#elseif row.item.typeShort == "bcpg:nutList" >				
				"name": "${row.item.assocs["bcpg:nutListNut"][0].properties.name!""}",
				"title": "${row.item.assocs["bcpg:nutListNut"][0].properties.name!""}",	
				<#elseif row.item.typeShort == "bcpg:ingList" >				
				"name": "${row.item.assocs["bcpg:ingListIng"][0].properties.name!""}",
				"title": "${row.item.assocs["bcpg:ingListIng"][0].properties.name!""}",					
				<#elseif row.item.typeShort == "pjt:taskList" >
				"name": "${row.item.properties["pjt:tlTaskName"]!""}",
				"title": "${row.item.properties["pjt:tlTaskName"]!""}",			
				<#else>
				"name": "${row.item.properties.name!""}",
				"title": "${row.item.properties.title!""}",
				</#if>			
				"description": "${row.item.properties.description!""}",
				<#if row.item.properties.modified??>"modified": "${xmldate(row.item.properties.modified)}",</#if>
				<#if row.item.properties.modifier??>"modifier": "${row.item.properties.modifier}",</#if>
				<#if row.item.siteShortName??>"site": "${row.item.siteShortName}",</#if>
				"displayPath": "${row.item.displayPath!""}",
				"nodeRef": "${row.item.nodeRef}"<#if row.selectable?exists>,
				"selectable" : ${row.selectable?string}</#if>
				<#if row.item.aspects??>
				,"aspects": 
		         [
		         <#list row.item.aspects as aspect>
		            "${shortQName(aspect)}"<#if aspect_has_next>,</#if>
		         </#list>
		         ]
		       </#if>
			}<#if row_has_next>,</#if>
		</#list>
		]
	}
}
	</#escape>
</#macro>