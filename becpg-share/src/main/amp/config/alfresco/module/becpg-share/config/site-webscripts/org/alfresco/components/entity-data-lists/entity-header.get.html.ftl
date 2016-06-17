<@standalone>
   <@markup id="css" >
      <#-- CSS Dependencies -->
      <@link href="${url.context}/res/components/node-details/node-header.css" group="entity-datalists"/>
  	  <@link href="${url.context}/res/components/entity-data-lists/entity-header.css" group="entity-datalists"/>
   </@>

   <@markup id="js">
      <#-- JavaScript Dependencies -->
      <@script src="${url.context}/res/components/node-details/node-header.js" group="entity-datalists"/>
      <@script src="${url.context}/res/components/entity-data-lists/entity-header.js" group="entity-datalists"/>
   </@>

   <@markup id="widgets">
      <#if item??>
      <@createWidgets group="entity-datalists"/>
      </#if>
   </@>

   <@markup id="html">
      <@uniqueIdDiv>
         <#if item??>
            <#include "../../include/alfresco-macros.lib.ftl" />
            
            <#assign el = args.htmlid?html>
            <#assign displayName = (item.displayName!item.fileName)?html>
            <#assign modifyLabel = "label.modified-by-user-on-date">
            <div class="node-header entity-header">
               <!-- Message banner -->
               <#if item.workingCopy??>
                  <#assign modifyLabel = "label.editing-started-on-date-by-user">
                  <#if item.workingCopy.isWorkingCopy??>
                     <#assign lockUser = node.properties["cm:workingCopyOwner"]>
                  <#else>
                     <#assign lockUser = node.properties["cm:lockOwner"]>
                  </#if>
                  <#if lockUser??>
                     <div class="status-banner theme-bg-color-2 theme-border-4">
                     <#assign lockedByLink = userProfileLink(lockUser.userName, lockUser.displayName, 'class="theme-color-1"') >
                     <#if (item.workingCopy.googleDocUrl!"")?length != 0 >
                        <#assign link><a href="${item.workingCopy.googleDocUrl}" target="_blank" class="theme-color-1">${msg("banner.google-docs.link")}</a></#assign>
                        <#if lockUser.userName == user.name>
                           <span class="google-docs-owner">${msg("banner.google-docs-owner", link)}</span>
                        <#else>
                           <span class="google-docs-locked">${msg("banner.google-docs-locked", lockedByLink, link)}</span>
                        </#if>
                     <#else>
                        <#if lockUser.userName == user.name>
                           <#assign status><#if node.isLocked>lock-owner<#else>editing</#if></#assign>
                           <span class="${status}">${msg("banner." + status)}</span>
                        <#else>
                           <span class="locked">${msg("banner.locked", lockedByLink)}</span>
                        </#if>
                     </#if>
                     </div>
                  </#if>
               <#elseif (node.isLocked && (node.properties["cm:lockType"]!"") == "WRITE_LOCK")>
                  <#assign lockUser = node.properties["cm:lockOwner"]>
                  <#if lockUser??>
                     <div class="status-banner theme-bg-color-2 theme-border-4">
                     <#assign lockedByLink = userProfileLink(lockUser.userName, lockUser.displayName, 'class="theme-color-1"') >
                     <#if lockUser.userName == user.name>
                        <span class="lock-owner">${msg("banner.lock-owner")}</span>
                     <#else>
                        <span class="locked">${msg("banner.locked", lockedByLink)}</span>
                     </#if>
                     </div>
                  </#if>
               </#if>
               <div class="node-info">
               <div id="${el}-bcpath" class="node-bcpath hidden"></div>
               <#if showPath == "true">
                  <!-- Path-->
                  <div class="node-path">
                  	<#list paths as path>
				      <#if path_index != 0>
				         <span class="separator"> &gt; </span>
				      </#if>
				      <span class="${path.cssClass?html}">
				     	  <#if path_has_next>
				          	<a href="${siteURL(path.href?html)}">${path.label?html}</a>
				         <#else> 
						 	${path.label?html}
						 </#if>
				      </span>
				   </#list>
                  
		             	<#if showOnlyLocation == "true" && productState??><span class="product-state ${productState}">[${msg("status."+productState)}]</span></#if>
						<#if showOnlyLocation == "true" && item.version?? && item.version?length &gt; 0 ><span class="document-version">${item.version}</span></#if>
                  </div>
               </#if>
               </div>
               <div id="${el}-node-users" class="node-users"></div>
            <div class="clear"></div>
         </div>
         
            
         <#else>
            <div class="node-header">
               <div class="status-banner theme-bg-color-2 theme-border-4">
               ${msg("banner.not-found")}
               </div>
            </div>
         </#if>
      </@>
   </@>
</@>
