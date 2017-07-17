<#assign el=args.htmlid?html>
<#include "../../include/alfresco-macros.lib.ftl" />
<div id="${el}-dialog" class="detailsDialog">
   <div id="${el}-dialogTitle" class="hd">${msg("header.workflows")}</div>
   <div class="bd entity-workflows">
		 <div class="info">
               <#if workflows?size == 0>
                  ${msg("label.partOfNoWorkflows")}
               <#else>
                  ${msg("label.partOfWorkflows")}
               </#if>
           </div>
        <#if workflows?size &gt; 0>
           <hr/>
           <div class="workflows">
                  <#list workflows as workflow>
                     <div class="workflow <#if !workflow_has_next>workflow-last</#if>">
                        <#if workflow.initiator?? && workflow.initiator.avatarUrl??>
                        <img src="${url.context}/proxy/alfresco/${workflow.initiator.avatarUrl}" alt="${msg("label.avatar")}"/>
                        <#else>
                        <img src="${url.context}/res/components/images/no-user-photo-64.png" alt="${msg("label.avatar")}"/>
                        </#if>
                        <a href="${siteURL("workflow-details?workflowId=" + workflow.id?js_string + "&nodeRef=" + (args.nodeRef!""))}"><#if workflow.message?? && workflow.message?length &gt; 0>${workflow.message?html}<#else>${msg("workflow.no_message")?html}</#if></a>
                        <div class="title">${workflow.title?html}</div>
                        <div class="clear"></div>
                     </div>
                  </#list>
              </div>
       </#if>
    </div>
</div>


