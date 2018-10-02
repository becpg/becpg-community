
{
	"code": "${code}",
	"properties": {
		<@serializeHash hash=properties/>
	}

}

<#macro serializeHash hash>
<#escape x as jsonUtils.encodeJSONString(x)>
<#local first = true>
<#list hash?keys as key>
	<#if hash[key]??>
		<#local val = hash[key]>
		<#if !first>,<#else><#local first = false></#if>"${key}":
		<#if val?is_date>"${xmldate(val)}"
		<#elseif val?is_boolean>${val?string}
		<#elseif val?is_number>${val?c}
		<#else>"${val}"
		</#if>
	</#if>
</#list>
</#escape>
</#macro>