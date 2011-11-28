<#escape x as jsonUtils.encodeJSONString(x)>
{
   "items":
   [
   <#if items??> 
   <#list items as node>
    {
       "nodeRef": "${node.nodeRef}",
       "displayName": "${node.name}"
	}<#if node_has_next>,</#if>
   </#list>
    </#if>
   ]
}
</#escape>