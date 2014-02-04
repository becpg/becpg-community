<#include "include/alfresco-template.ftl" />
<@templateHeader>
   <@script type="text/javascript" src="${url.context}/res/modules/documentlibrary/doclib-actions.js" group="document-details"/>
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/document-details/document-details-panel.css" group="document-details"/>
   <@templateHtmlEditorAssets />
</@>

<@templateBody>
   <@markup id="alf-hd">
   <div id="alf-hd">
      <@region id="mobile-app" scope="template"/>
      <@region scope="global" id="share-header" chromeless="true"/>
   </div>
   </@>
   <@markup id="bd">
   <div id="bd">
      <@region id="actions-common" scope="template"/>
      <@region id="actions" scope="template"/>
      <@region id="node-header" scope="template"/>
      <div class="yui-gc">
         <div class="yui-u first">
				<div id="preview-tab" class="hidden"><@region id="web-preview" scope="template"/></div>
				<div id="properties-tab" class="hidden"><@region id="document-metadata" scope="template"/></div>
            <@region id="comments" scope="template"/>
         </div>
         <div class="yui-u">
            <@region id="document-actions" scope="template"/>
            <@region id="document-tags" scope="template"/>
           <#-- <@region id="document-links" scope="template"/> -->
          <#--  <@region id="document-sync" scope="template"/> -->
          <#--  <@region id="document-permissions" scope="template"/> -->
            <@region id="entity-projects" scope="template"/>
            <@region id="document-workflows" scope="template"/>
            <@region id="document-versions" scope="template"/>
            <@region id="document-publishing" scope="template"/>
            <#if imapServerEnabled>
               <@region id="document-attachments" scope="template"/>
            </#if>
         </div>
         <@region id="archive-and-download" scope="template"/>
      </div>

      <@region id="html-upload" scope="template"/>
      <@region id="flash-upload" scope="template"/>
      <@region id="file-upload" scope="template"/>
      <@region id="dnd-upload" scope="template"/>
   </div>
   <@region id="doclib-custom" scope="template"/>
   </@>
</@>

<@templateFooter>
   <@markup id="alf-ft">
   <div id="alf-ft">
      <@region id="footer" scope="global"/>
   </div>
   </@>
</@>
