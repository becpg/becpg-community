<#-- NPDs Instances collection -->

<#import "npd.lib.ftl" as npdLib />
{
   "data": 
   [
      <#list workflowInstances as workflowInstance>
      <@npdLib.workflowInstanceJSON workflowInstance=workflowInstance detailed=true/>
      <#if workflowInstance_has_next>,</#if>
      </#list>
   ]
   <#if paging??>,
   "paging": 
   <@npdLib.pagingJSON paging=paging />
   </#if>
}