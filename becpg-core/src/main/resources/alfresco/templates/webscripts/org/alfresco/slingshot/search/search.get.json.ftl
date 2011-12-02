<#macro translateValue nameValue>
<#escape x as jsonUtils.encodeJSONString(x)>
      <#if nameValue?string == "ToValidate">"${msg("state.product.tovalidate")}"
      <#elseif nameValue?string == "Valid">"${msg("state.product.valid")}"
      <#elseif nameValue?string == "Refused">"${msg("state.product.refused")}"
      <#elseif nameValue?string == "Archived">"${msg("state.product.archived")}"
      <#else>${nameValue?html}</#if>
</#escape>
</#macro>
<#macro dateFormat date>${date?string("dd MMM yyyy HH:mm:ss 'GMT'Z '('zzz')'")}</#macro>
<#macro renderData key data>
<#escape x as jsonUtils.encodeJSONString(x)>
{
      <#if data.value?is_boolean>
   "value": ${data.value?string},
      <#elseif data.value?is_number>
   "value": ${data.value?c},
      <#else>
   "value": "${data.value}",
      </#if>
      <#if data.label??>
    "label":  "${data.label}",
      </#if>
      <#if data.metadata??>
   "metadata": "${data.metadata}",
      </#if>
       <#if key?string == "prop_bcpg_productState">
      	 "displayValue": <@translateValue data.value />
      <#else>
	      <#if data.displayValue?is_boolean>
	   "displayValue": ${data.displayValue?string}
	      <#elseif data.displayValue?is_number>
	   "displayValue": ${data.displayValue?c}
	      <#else>
	   "displayValue": "${data.displayValue}"
	      </#if>
	   </#if>
}
</#escape>
</#macro>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"items":
	[
		<#list data.items as item>
		{
			"nodeRef": "${item.nodeRef}",
			"type": "${item.type}",
			"itemType": "${item.itemType}",
			"name": "${item.name!''}",
			"displayName": "${item.displayName!''}",
			<#if item.title??>
			"title": "${item.title}",
			</#if>
			"description": "${item.description!''}",
			"modifiedOn": "<@dateFormat item.modifiedOn />",
			"modifiedByUser": "${item.modifiedByUser}",
			"modifiedBy": "${item.modifiedBy}",
			"size": ${item.size?c},
			<#if item.site??>
			"site":
			{
				"shortName": "${item.site.shortName}",
				"title": "${item.site.title}"
			},
			"container": "${item.container}",
			</#if>
			<#if item.nodeData??>
			 "itemData":
		         {
		      <#list item.nodeData?keys as key>
		         <#assign itemData = item.nodeData[key]>
		            "${key}":
		         <#if itemData?is_sequence>
		            [
		            <#list itemData as data>
		               <@renderData key data /><#if data_has_next>,</#if>
		            </#list>
		            ]
		         <#else>
		            <@renderData key itemData />
		         </#if><#if key_has_next>,</#if>
		      </#list>
		         },
			</#if>
			<#if item.path??>
			"path": "${item.path}",
			</#if>
			"tags": [<#list item.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list>]
		}<#if item_has_next>,</#if>
		</#list>
	]
}
</#escape>