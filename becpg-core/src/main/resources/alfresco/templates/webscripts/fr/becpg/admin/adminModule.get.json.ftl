<#escape x as jsonUtils.encodeJSONString(x)>
{
   "status": "${status}"
	<#if items?? >
	,"items":
	   [
	      <#list items as item>
	      {
	         "nodeRef" : "${item.nodeRef}",
	         "name" : "${item.name}" 
	      }<#if item_has_next>,</#if>
	     </#list>
	   ]
	  </#if>
}
</#escape>