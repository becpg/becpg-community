<@standalone>
   <@markup id="css" >
      <#-- CSS Dependencies -->
      <@link href="${url.context}/res/components/node-details/node-header.css" group="node-header"/>
  	  <@link href="${url.context}/res/components/entity-details/entity-header.css" group="node-header"/>
   </@>

   <@markup id="js">
      <#-- JavaScript Dependencies -->
      <@script src="${url.context}/res/components/node-details/node-header.js" group="node-header"/>
      <@script src="${url.context}/res/components/entity-details/entity-header.js" group="node-header"/>
   </@>

   <@markup id="widgets">
      <#if item??>
      <@createWidgets group="node-header"/>
      </#if>
   </@>

   <@markup id="html">
      <@uniqueIdDiv>
         <#if item??>
            <#include "../../include/alfresco-macros.lib.ftl" />
            
            <#assign el = args.htmlid?html>
            <#assign displayName = (item.displayName!item.fileName)?html>
            <#assign modifyLabel = "label.modified-by-user-on-date">
            <div class="node-header">
               <!-- Message banner -->
               <#if showOnlyLocation == "false">
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
               </#if>
               <div class="node-info">
               <#if showPath == "true">
                  <!-- Path-->
                  <div class="node-path">
                     <@renderPaths paths />
                  </div>
               </#if>
               <#if showOnlyLocation == "false">
                  <#assign idx=node.type?index_of(":")+1 />
                  <a id="${el}-uploadLogo-button" class="upload-logo-action" title="${msg("actions.entity.upload-logo")}" href="#">
               	  	<img src="${thumbnailUrl}"  title="${displayName}" class="node-thumbnail" width="48" />
               	  	<span class="upload-logo-span" >&nbsp;</span>
               	  </a>
                  <h1 class="thin dark">
                     ${displayName}<span id="document-version" class="document-version">${item.version}</span>
                  </h1>
                  <!-- Modified & Social -->
                  <div>
                     <span class="item-modifier">
                     <#assign modifyUser = node.properties["cm:modifier"]>
                     <#assign modifyDate = node.properties["cm:modified"]>
                     <#assign modifierLink = userProfileLink(modifyUser.userName, modifyUser.displayName, 'class="theme-color-1"') >
                     ${msg(modifyLabel, modifierLink, "<span id='${el}-modifyDate'>${modifyDate.iso8601}</span>")}
                     </span>
                     <#if showFavourite == "true">
                     <span id="${el}-favourite" class="item item-separator"></span>
                     </#if>
                     <#if showLikes == "true">
                     <span id="${el}-like" class="item item-separator"></span>
                     </#if>
                     <#if showComments == "true">
                     <span class="item item-separator item-social">
                        <a href="#" name="@commentNode" rel="${item.nodeRef?html}" class="theme-color-1 comment<#if commentCount??> hasComments</#if> ${el}" title="${msg("comment.document.tip")}" tabindex="0">${msg("comment.document.label")}</a><#if commentCount??><span class="comment-count">${commentCount}</span></#if>
                     </span>
                     </#if>
                     <#if showQuickShare == "true">
                     <span id="${el}-quickshare" class="item item-separator"></span>
                     </#if>
					 <span class="item item-separator item-social">
						<a  href="#"  id="${el}-print-button" class="item-print" rel="${item.nodeRef?html}" >${msg("print.label")}</a>
					 </span>
                  </div>
                  </#if>
               </div>
               <#if showOnlyLocation == "false">
               <div class="node-action">
              
               	<#if reports?? && reports?size &gt; 0 >
	                  <input id="${el}-entityReportPicker-button" type="button" name="${el}-entityReportPicker-button" value="${msg("picker.report.choose")}" ></input>
					      <select id="${el}-entityReportPicker-select"  name="${el}-entityReportPicker-select">      
					      	<option value="properties">${msg("picker.report.properties")}</option>
					      	<#list reports as report>
					      		<option value="${report.nodeRef}">${report.templateName?replace(".rptdesign", "")}</option>
					      	</#list>
					      </select>
		               
		               <div class="entity-download-report">
					   		<a id="${el}-downloadEntityReport-button" title="${msg("actions.entity.download-report")}"  href="#" >&nbsp;</a>
					   	</div>
				   	</#if>
				   	
	               
	               <div class="entity-view-datalist" >
			   			<a id="${el}-viewEntityDatalist-button" title="${msg("actions.entity.view-datalists")}"  href="#" >${msg("actions.entity.view-datalists.short")}</a>
				   	</div>
				   	<div class="entity-view-documents">
				   		<a id="${el}-viewEntityDocuments-button" title="${msg("actions.entity.view-documents")}"  href="#" >${msg("actions.entity.view-documents.short")}</a>
				   	</div>
	               
              </div>
              </#if>
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
