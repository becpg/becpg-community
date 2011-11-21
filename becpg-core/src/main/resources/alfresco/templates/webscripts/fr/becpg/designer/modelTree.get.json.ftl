<#macro render tree >
    <#if tree.type??>"type":"${tree.type?string}",</#if>
    <#if tree.name??>"name":"${tree.name?string}",</#if>
    <#if tree.title??>"title":"${tree.title?string}",</#if>
    <#if tree.description??>"title":"${tree.description?string}",</#if>
    <#if tree.nodeRef??>"nodeRef":"${tree.nodeRef?string}",</#if>
 <#if tree.childrens??>   "childrens":
   [
   <#list tree.childrens as child>
    {
       <@render child />
	}<#if child_has_next>,</#if>
   </#list>
   ]
   </#if>
</#macro>

<#escape x as jsonUtils.encodeJSONString(x)>
{
  <@render tree/>
}
</#escape>