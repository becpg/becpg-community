
<#-- Renders a process instance. -->
<#macro processInstanceJSON processInstance detailed=false>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   <#if processInstance.id??>
   "id": "${processInstance.id}"
   <#else>
   "nodeRef": <#if processInstance.nodeRef??>"${processInstance.nodeRef?string}"<#else>null</#if>
   </#if>,
   "type": "${processInstance.type}",
   "title": "${processInstance.title!""}",
   "isActive": ${processInstance.isActive?string},
   "state": <#if processInstance.state??>"${processInstance.state?string}"<#else>null</#if>,
   "message": <#if processInstance.message??>"${processInstance.message}"<#else>null</#if>,
   "startDate":<#if processInstance.startDate??>"${processInstance.startDate}"<#else>null</#if>,
   "dueDate": <#if processInstance.dueDate??>"${processInstance.dueDate}"<#else>null</#if>,
   "initiator": 
   <#if processInstance.initiator??>
   {
      "userName": "${processInstance.initiator.userName}"<#if processInstance.initiator.firstName??>,
      "firstName": "${processInstance.initiator.firstName}"</#if><#if processInstance.initiator.lastName??>,
      "lastName": "${processInstance.initiator.lastName}"</#if><#if processInstance.initiator.avatarUrl??>,
      "avatarUrl": "${processInstance.initiator.avatarUrl}"</#if>
   }
   <#else>
   null
   </#if>
}
</#escape>
</#macro>
