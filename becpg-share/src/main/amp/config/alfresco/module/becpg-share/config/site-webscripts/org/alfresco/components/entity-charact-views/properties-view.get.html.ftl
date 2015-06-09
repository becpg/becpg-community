<@standalone>
<@markup id="css" >
   <#-- CSS Dependencies -->
   <#include "../form/form.css.ftl"/>
   <@link href="${url.context}/res/components/comments/comments-list.css" group="properties-view"/>
   <@link href="${url.context}/res/components/entity-charact-views/properties-view.css" group="properties-view"/>
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <#include "../form/form.js.ftl"/>  
   <@script src="${url.context}/res/components/entity-charact-views/custom-entity-toolbar.js" group="entity-toolbar"/>
   <@script src="${url.context}/res/components/comments/comments-list.js" group="properties-view"/>
   <@script src="${url.context}/res/components/entity-charact-views/properties-view.js" group="properties-view"/>
   

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
		      <div id="{el}">
			      <div class="yui-gc properties-view" >
			            <div  class="yui-u first ">
					        <div id="${el}-formContainer">
					        </div>
				     </div>
		            <div class="yui-u properties-details">
					     <table >
		           				<tr><td>
									<a id="${el}-Logo-button" class="upload-logo-action" title="${msg("actions.entity.upload-logo")}" href="#">
					               	  	<img src="${thumbnailUrl}"  title="${displayName}" class="node-thumbnail"/>
					               	  	<span class="upload-logo-span" >&nbsp;</span>
					               </a>
			                     </td>
			                     <td>
			                        <h2 class="thin dark">${displayName}</h2>
			                        <div>
					                 <span class="item-modifier">
					                     <#assign modifyUser = node.properties["cm:modifier"]>
					                     <#assign modifyDate = node.properties["cm:modified"]>
					                     <#assign modifierLink = userProfileLink(modifyUser.userName, modifyUser.displayName, 'class="theme-color-1"') >
					                     ${msg("label.modified-by-user-on-date", modifierLink, "<span id='${el}-modifyDate'>${modifyDate.iso8601}</span>")}
					                 </span>
                     				<span id="${el}-favourite" class="item"></span>
                   					<span class="item item-separator item-social">
					                        <a href="#" name="@commentNode" rel="${item.nodeRef?html}" class="theme-color-1 comment<#if commentCount??> hasComments</#if> ${el}" title="${msg("comment.document.tip")}" tabindex="0">${msg("comment.document.label")}</a><#if commentCount??><span class="comment-count">${commentCount}</span></#if>
				                     </span>
                    			  </div>
								</td></tr>
					   </table>   
						<div  id="${el}-body"  class="comments-list">
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
		      </div>
		     </div> 
      </#if>
   </@>
</@>
</@>
