<#escape x as jsonUtils.encodeJSONString(x)>
{   
   "reportTpls":
   [
   <#list reportTpls as reportTpl>
    {
	   "name": "${reportTpl.name?replace(".rptdesign", "")?replace(".xml", "")?replace(".xlsx", "")}",
	   "title": "${reportTpl.properties.title!""}",
	   "format": "${reportTpl.properties["rep:reportTplFormat"]!""}",
	   "nodeRef": "${reportTpl.nodeRef}"
	}<#if reportTpl_has_next>,</#if>
   </#list>
   ]
   ,"entities":"${entities!""}"
}
</#escape>