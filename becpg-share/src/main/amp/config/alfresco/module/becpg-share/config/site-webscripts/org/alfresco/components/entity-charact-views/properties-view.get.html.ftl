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
   <@script src="${url.context}/res/components/comments/comments-list.js" group="properties-view"/>
  <@script src="${url.context}/res/components/entity-catalog/entity-catalog.js" group="properties-view"/>
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
									<a id="${el}-uploadLogo-button" class="upload-logo-action" title="${msg("actions.entity.upload-logo")}" href="#">
					               	  	<img id="${el}-productLogo" src="${thumbnailUrl}"  title="${displayName}" class="node-thumbnail"/>
					               	  	<span class="upload-logo-span" >&nbsp;</span>
					               </a>
			                     </td>
			                     <td>
			                        <div>
					                 <span class="item-modifier">
					                     <#assign modifyUser = node.properties["cm:modifier"]>
					                     <#assign modifyDate = node.properties["cm:modified"]>
					                     <#assign modifierLink = userProfileLink(modifyUser.userName, modifyUser.displayName, 'class="theme-color-1"') >
					                     ${msg("label.modified-by-user-on-date", modifierLink, "<span id='${el}-modifyDate'>${modifyDate.iso8601}</span>")}
					                 </span>
                     				<span id="${el}-favourite" class="item"></span>                   					
                    			  </div>
								</td></tr>
					   </table>   
					   
					   <div id="${el}-properties-tabview" class="yui-navset">
						   	<ul class="yui-nav" >
						   		<li class="selected"><a href="#${el}-comments" ><em>${msg("header.comments")}</em></a></li>
						   		<li ><a href="#${el}-catalogs"><em>${msg("label.property_completion")}</em></a></li>
						   	</ul>
						   	
						   	<div class="yui-content">
						   		<div id="tab_${el}-comments">
							   		<div  id="${el}-body"  class="comments-list">
				            	  				            
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
							   	<div id="tab_${el}-catlogs">
						   			<div id="${el}-entity-catalog"></div> 
						   		</div>
						   	</div>
					   </div>
		            </div>
		      </div>
		     </div> 
      </#if>
   </@>
</@>
</@>
