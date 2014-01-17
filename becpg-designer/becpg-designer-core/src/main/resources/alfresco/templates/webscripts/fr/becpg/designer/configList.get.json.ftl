<#escape x as jsonUtils.encodeJSONString(x)>
{
   "items":
   [
   <#if items??> 
   <#list items as node>
    {
       "nodeRef": "${node.nodeRef}",
       "displayName": "${node.name}"
        <#if node.properties["dsg:readOnlyFile"]??>
        		,"readOnly": ${node.properties["dsg:readOnlyFile"]?string}
        </#if>
	}<#if node_has_next>,</#if>
   </#list>
    </#if>
   ]
}
</#escape>