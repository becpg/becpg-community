<#-- Process Instances collection for NodeRef-->

<#import "process.lib.ftl" as processLib />
{ "data":
  { 
   "processInstances": 
   [
      <#list processInstances as processInstance>
      <@processLib.processInstanceJSON processInstance=processInstance />
      <#if processInstance_has_next>,</#if>
      </#list>
   ],
   
   "processTypes": 
   	[
   		<#list processTypes as processType>
   			"${processType}"
   			<#if processType_has_next>,</#if>
   		</#list>
   	]
  }
}