<#-- MNT-20195 (LM-190214): strip out and return error code from given error message. -->
<#--
   beCPG: the stock version calls message?substring(0, 8) as soon as the message is a string, so a
   message shorter than 8 characters - an empty one in particular - makes the *error page itself*
   fail. The web script then answers "07170090 Failed to process template html.status.ftl" and the
   real cause is lost, which is what made the random 500s on the form and datalist components
   impossible to diagnose. Check the length before cutting.
-->
<#function getErrorCode message>
   <#assign code = "">

   <#if message?? && message?is_string && message?length gte 8>
      <!-- substring first 8 characters from message that usually be the error code id. -->
      <#assign instanceId = message?substring(0, 8)>
      <!-- check if these codes are 'numeric'. -->
      <#if instanceId?matches("^[0-9]*$")>
         <#assign code = instanceId>
      </#if>
   </#if>

   <#return code>
</#function>
