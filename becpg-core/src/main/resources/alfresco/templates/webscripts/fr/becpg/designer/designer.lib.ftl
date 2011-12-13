<#macro render tree >
    <#if tree.type??>"type":"${tree.type}",</#if>
    <#if tree.name??>"name":"${tree.name}",</#if>
    <#if tree.title??>"title":"${tree.title}",</#if>
    <#if tree.description??>"description":"${tree.description}",</#if>
    <#if tree.nodeRef??>"nodeRef":"${tree.nodeRef}",</#if>
    <#if tree.hasError??>"hasError":"${tree.hasError?string}",</#if>
    <#if tree.isDraggable??>"draggable":"${tree.isDraggable?string}",</#if>
    <#if tree.formId??>"formId":"${tree.formId?string}",</#if>
    <#if tree.subType??>"subType":"${tree.subType?string}",</#if>
	<#if tree.childrens??>"childrens":[
	   <#list tree.childrens as child>
	    {
	       <@render child />
		}<#if child_has_next>,</#if>
	   </#list>]
  	 </#if>
   	<#if tree.accepts??>,"accepts":
	   [
	   <#list tree.accepts as accept>
	    "${accept}"<#if accept_has_next>,</#if>
	   </#list>
	   ]
	 </#if>
</#macro>