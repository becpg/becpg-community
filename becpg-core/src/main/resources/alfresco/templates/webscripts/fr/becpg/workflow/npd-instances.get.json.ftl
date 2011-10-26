<#-- Workflow Instances collection -->

<#-- Renders a paging object. -->
<#macro pagingJSON paging>
<#escape x as jsonUtils.encodeJSONString(x)>
   {
      "totalItems": ${paging.totalItems?c},
      "maxItems": ${paging.maxItems?c},
      "skipCount": ${paging.skipCount?c}
   }
</#escape>
</#macro>

<#-- Renders a npd instance. -->
<#macro workflowInstanceJSON workflowInstance detailed=false>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "id": "${workflowInstance.id}",
   "url": "${workflowInstance.url}",
   "name": "${workflowInstance.name}",
   "title": "${workflowInstance.title!""}",
   "description": "${workflowInstance.description!""}",
   "isActive": ${workflowInstance.isActive?string},
   "startDate": "${workflowInstance.startDate}",
   "priority": <#if workflowInstance.priority??>${workflowInstance.priority?c}<#else>2</#if>,
   "message": <#if workflowInstance.message?? && workflowInstance.message?length &gt; 0>"${workflowInstance.message}"<#else>null</#if>,
   "endDate": <#if workflowInstance.endDate??>"${workflowInstance.endDate}"<#else>null</#if>,
   "dueDate": <#if workflowInstance.dueDate??>"${workflowInstance.dueDate}"<#else>null</#if>,
   "context": <#if workflowInstance.context??>"${workflowInstance.context}"<#else>null</#if>,
   "package": <#if workflowInstance.package??>"${workflowInstance.package}"<#else>null</#if>,
   "npdStatus":<#if workflowInstance.npdwf_npdStatus??>"${workflowInstance.npdwf_npdStatus}"<#else>null</#if>,
   "npdProductName":<#if workflowInstance.npdwf_npdStatus??>"${workflowInstance.npdwf_npdProductName}"<#else>null</#if>,
   "npdNumber":<#if workflowInstance.npdwf_npdStatus??>"${workflowInstance.npdwf_npdNumber}"<#else>null</#if>,
   "npdType":<#if workflowInstance.npdwf_npdStatus??>"${workflowInstance.npdwf_npdType}"<#else>null</#if>,
   "initiator": 
   <#if workflowInstance.initiator??>
   {
      "userName": "${workflowInstance.initiator.userName}"<#if workflowInstance.initiator.firstName??>,
      "firstName": "${workflowInstance.initiator.firstName}"</#if><#if workflowInstance.initiator.lastName??>,
      "lastName": "${workflowInstance.initiator.lastName}"</#if><#if workflowInstance.initiator.avatarUrl??>,
      "avatarUrl": "${workflowInstance.initiator.avatarUrl}"</#if>
   },
   <#else>
   null,
   </#if>
   "definitionUrl": "${workflowInstance.definitionUrl}"
}
</#escape>
</#macro>

{
   "data": 
   [
      <#list workflowInstances as workflowInstance>
      <@workflowInstanceJSON workflowInstance=workflowInstance />
      <#if workflowInstance_has_next>,</#if>
      </#list>
   ]
   <#if paging??>,
   "paging": 
   <@pagingJSON paging=paging />
   </#if>
}