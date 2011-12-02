<#import "item.lib.ftl" as itemLib />
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "totalRecords": ${data.paging.totalRecords?c},
   "startIndex": ${data.paging.startIndex?c},
   "itemTypes" : 
   [
      <#list data.itemTypes as item>
        "${item}"
      <#if item_has_next>,</#if>
      </#list>
   ],
   "items":
   [
      <#list data.items as item>
      {
         <@itemLib.itemJSON item />
      }<#if item_has_next>,</#if>
      </#list>
   ]
}
</#escape>