<#escape x as jsonUtils.encodeJSONString(x)>
{
  "items":
   [
   <#list items as item>
      {
         "nodeRef": "${item}"
      }<#if item_has_next>,</#if>
   </#list>
   ],
   "status" : "OK"
}
</#escape>