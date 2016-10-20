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
		<#if node.aspects??>
        ${indent}"aspects": 
        ${indent}[
           <#list node.aspects as aspect>
                 "${shortQName(aspect)}"
              <#if aspect_has_next>,</#if>
           </#list>
        
           ${indent}],
       </#if>
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
				<#if row.item.parent?? && row.item.parent.hasPermission("Read") >"parentName": "${row.item.parent.name!""}",</#if>
				"isContainer": ${row.item.isContainer?string},
				<#if row.container??>"container": "${row.container!""}",</#if>
				<#if row.item.typeShort == "bcpg:dynamicCharactList" >
				"name": "${row.item.properties["bcpg:dynamicCharactTitle"]!""}",
				"title": "${row.item.properties["bcpg:dynamicCharactTitle"]!""}",
				<#elseif row.item.typeShort == "bcpg:linkedValue" >
				"name": "${row.item.properties["bcpg:lkvValue"]!""}",
				"title": "${row.item.properties["bcpg:lkvValue"]!""}",
				<#elseif row.item.typeShort == "bcpg:ingTypeItem" >
				"name": "${row.item.properties["bcpg:lvValue"]!""}",
				"title": "${row.item.properties["bcpg:lvValue"]!""}",
				<#elseif row.item.typeShort == "bcpg:compoList" >				
				"name": "${row.item.assocs["bcpg:compoListProduct"][0].properties.name!""}",
				"title": "${row.item.assocs["bcpg:compoListProduct"][0].properties.name!""}",	
				<#elseif row.item.typeShort == "bcpg:nutList" >				
				"name": "${row.item.assocs["bcpg:nutListNut"][0].properties["bcpg:charactName"]!""}",
				"title": "${row.item.assocs["bcpg:nutListNut"][0].properties["bcpg:charactName"]!""}",
				<#elseif row.item.typeShort == "bcpg:costList" >				
				"name": "${row.item.assocs["bcpg:costListCost"][0].properties["bcpg:charactName"]!""}",
				"title": "${row.item.assocs["bcpg:costListCost"][0].properties["bcpg:charactName"]!""}",	
				<#elseif row.item.typeShort == "bcpg:ingList" >				
				"name": "${row.item.assocs["bcpg:ingListIng"][0].properties["bcpg:charactName"]!""}",
				"title": "${row.item.assocs["bcpg:ingListIng"][0].properties["bcpg:charactName"]!""}",					
				<#elseif row.item.typeShort == "pjt:taskList" >
				"name": "${row.item.properties["pjt:tlTaskName"]!""}",
				"title": "${row.item.properties["pjt:tlTaskName"]!""}",	
				<#elseif row.item.typeShort == "pjt:budgetList" >
				"name": "${row.item.properties["pjt:blItem"]!""}",
				"title": "${row.item.properties["pjt:blItem"]!""}",	
				<#elseif row.item.properties["bcpg:charactName"]?? >
				"name": "${row.item.properties["bcpg:charactName"]!""}",
				"title": "${row.item.properties.title!""}",				
				<#else>
				"name": "${row.item.properties.name!""}",
				"title":<#if row.item.properties["lnk:title"]??>"${row.item.properties["lnk:title"]}",
						<#elseif row.item.properties["ia:whatEvent"]??>"${row.item.properties["ia:whatEvent"]}",
						<#else>"${row.item.properties.title!""}",</#if>
				</#if>			
				"description": "${row.item.properties.description!""}",
				<#if row.item.properties.modified??>"modified": "${xmldate(row.item.properties.modified)}",</#if>
				<#if row.item.properties.modifier??>"modifier": "${row.item.properties.modifier}",</#if>
				<#if row.item.siteShortName??>"site": "${row.item.siteShortName}",</#if>
				<#if row.item.properties["ia:fromDate"]??>"fromDate": "${xmldate(row.item.properties["ia:fromDate"])}",</#if>
				"displayPath": "${row.item.displayPath!""}",
				<#if row.item.typeShort != "cm:person" && row.item.typeShort != "cm:authorityContainer">
					"userAccess":
					{
						"create": ${row.item.hasPermission("CreateChildren")?string},
						"edit": ${row.item.hasPermission("Write")?string},
						"delete": ${row.item.hasPermission("Delete")?string}
					},
				</#if>
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