<@standalone>
<@markup id="css" >
   <#-- CSS Dependencies -->
   <#include "../form/form.css.ftl"/>
   <@link href="${url.context}/res/components/document-details/document-metadata.css" group="properties-view"/>
   <@link href="${url.context}/res/components/comments/comments-list.css" group="properties-view"/>
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <#include "../form/form.js.ftl"/>  

   <@script src="${url.context}/res/components/entity-charact-views/custom-entity-toolbar.js" group="entity-toolbar"/>
   <@script src="${url.context}/res/components/comments/comments-list.js" group="properties-view"/>
   <@script src="${url.context}/res/components/document-details/document-metadata.js" group="properties-view"/>
 
</@>

<@markup id="widgets">
   <#if document??>
      <@createWidgets group="properties-view"/>

   </#if>
</@>

<@markup
 id="html">
   <@uniqueIdDiv>
      <#if document??>
         <!-- Parameters and libs -->
         <#include "../../include/alfresco-macros.lib.ftl" />
         <#assign el=args.htmlid?html>
		      <div class="yui-gc first">
		            <div  id="${el}-custom" class="yui-u first ">
				        <div id="${el}-custom-formContainer"></div>
				     </div>
		            <div id="${el}-body" class="yui-u comments-list">
		            	   <h2 class="thin dark">${msg("header.comments")}</h2>				            
                           <div id="${el}-add-comment">
				               <div id="${el}-add-form-container" class="theme-bg-color-4 hidden"></div>
				            </div>
				            <div class="comments-list-actions">
				               <div class="left">
				                  <div id="${el}-actions" class="hidden">
				                     <button class="alfresco-button" name=".onAddCommentClick">${msg("button.addComment")}</button>
				                  </div>
				               </div>
				               <div class="right">
				                  <div id="${el}-paginator-top"></div>
				               </div>
				               <div class="clear"></div>
				            </div>
				            <hr class="hidden"/>
				            <div id="${el}-comments-list"></div>
				            <hr class="hidden"/>
				            <div class="comments-list-actions">
				               <div class="left">
				               </div>
				               <div class="right">
				                  <div id="${el}-paginator-bottom"></div>
				               </div>
				               <div class="clear"></div>
				            </div>
		            </div>
		      </div>

      </#if>
   </@>
</@>
</@>
