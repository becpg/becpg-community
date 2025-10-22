
<#if entity?? && entity.hasPermission("Read")>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"nodeRef": "${entity.nodeRef}"
}
</#escape>
</#if>