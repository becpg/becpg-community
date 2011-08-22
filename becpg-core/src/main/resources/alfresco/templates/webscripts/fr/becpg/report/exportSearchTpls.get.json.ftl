<#escape x as jsonUtils.encodeJSONString(x)>
{   
   "reportTpls":
   [
   <#list reportTpls as reportTpl>
    {
	   "name": "${reportTpl.name}",
	   "title": "${reportTpl.properties.title!reportTpl.name}",
	   "description": "${reportTpl.properties.description!""}",
	   "nodeRef": "${reportTpl.nodeRef}"
	}<#if reportTpl_has_next>,</#if>
   </#list>
   ]
}
</#escape>