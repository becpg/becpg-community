<#escape x as jsonUtils.encodeJSONString(x)>
{   
   "reportTpls":
   [
   <#list reportTpls as reportTpl>
    {
	   "name": "${reportTpl.name?replace(".rptdesign", "")?replace(".xml", "")?replace(".xlsx", "")?replace(".xlsm", "")}",
	   "title": "${reportTpl.properties.title!""}",
	   "reportTplName":"${reportTpl.properties.name!""}",
	   <#if reportTpl.name?contains(".xlsm") >
	   "format": "XLSM",
	   <#else>
	   "format": "${reportTpl.properties["rep:reportTplFormat"]!""}",
	   </#if>
	   "nodeRef": "${reportTpl.nodeRef}"
	}<#if reportTpl_has_next>,</#if>
   </#list>
   ]
   ,"entities":"${entities!""}"
}
</#escape>