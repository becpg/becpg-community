
<@markup id="css" >
   <#-- CSS Dependencies -->
   <#include "../form/form.css.ftl"/>
  <@link rel="stylesheet" type="text/css" href="${url.context}/res/modules/documentlibrary/global-folder.css"  group="bulk-edit"/>
  <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/bulk-edit/bulk-edit.css" group="bulk-edit" />
  
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <#include "../form/form.js.ftl"/>
   <@script src="${url.context}/res/modules/documentlibrary/global-folder.js"  group="bulk-edit"/>
   <@script src="${url.context}/res/modules/entity-datagrid/entity-columnRenderer.js" group="bulk-edit"></@script>
   <@script src="${url.context}/res/components/bulk-edit/bulk-edit.js" group="bulk-edit"></@script>
   <@script src="${url.context}/res/modules/custom-entity-datagrid/custom-columnRenderers.js" group="bulk-edit"></@script>
   
   
</@>

<@markup id="widgets">
   <@createWidgets group="bulk-edit"/>
</@>

<@markup id="html">
   <@uniqueIdDiv>
		<#assign el = args.htmlid?html>
		<#assign formId=args.htmlid?js_string + "-form">
		<#if error?exists>
		   <div class="error">${error}</div>
		</#if>
		<div id="${el}-body" class="bulk-edit">
			    <div class="bulk-edit-meta">
			     <div class="item-type-select">
			      <button id="${el}-itemTypeSelect-button" name="bulk-edit-itemSelect-button">${msg("menu.select.type")}&nbsp;&#9662;</button>
			   	   <div id="${el}-itemTypeSelect-menu" class="yuimenu" style="visibility:hidden;">
			               <div class="bd">
			                  <ul>
			                  <#list itemTypes as itemType >
			                     <li><a href="#"><span class="${itemType.name}#${itemType.formId}#${itemType.editSelectedFormId}">${itemType.label}</span></a></li>
			                  </#list>
			                  </ul>
			               </div>
			         </div>
			         <button id="${el}-show-button">${msg("menu.show")}</button>
			      </div>
			      <div id="${el}-itemProps-container" class="bulk-edit-props-container"></div>
			   </div>
			   <div id="${el}-bulk-editBar" class="yui-gc bulk-edit-bar flat-button theme-bg-color-3">
			      <div class="yui-u first align-center">
			         <div class="item-select">
			            <button id="${el}-itemSelect-button" name="bulk-edit-itemSelect-button">${msg("menu.select")}&nbsp;&#9662;</button>
			            <div id="${el}-itemSelect-menu" class="yuimenu" style="visibility:hidden;">
			               <div class="bd">
			                  <ul>
			                     <li><a href="#"><span class="selectAll">${msg("menu.select.all")}</span></a></li>
			                     <li><a href="#"><span class="selectInvert">${msg("menu.select.invert")}</span></a></li>
			                     <li><a href="#"><span class="selectNone">${msg("menu.select.none")}</span></a></li>
			                     <li><a href="#"><span class="selectAllPages">${msg("menu.select.allPages")}</span></a></li>
			                  </ul>
			               </div>
			            </div>
			         </div>
			          <div class="edit-selected">
				            <span id="${el}-edit-selected" class="yui-button yui-push-button">
				               <span class="first-child">
				                  <button type="button" >${msg("button.edit-selected")}</button>
				               </span>
				            </span>
				      </div>
				      <div class="simulate-selected">
				            <span id="${el}-simulate-selected" class="yui-button yui-push-button">
				               <span class="first-child">
				                  <button type="button" >${msg("button.simulate-selected")}</button>
				               </span>
				            </span>
				      </div>
				      <div id="${el}-paginatorTop" class="paginator"></div>
			      </div>
			      <div class="yui-u align-right">
			    	   <div class="items-per-page" style="visibility:hidden;">
			            <button id="${el}-itemsPerPage-button">${msg("menu.items-per-page")}</button>
			         </div>
			         <div class="export-csv">
			            <span id="${el}-export-csv" class="yui-button yui-push-button">
			               <span class="first-child">
			                  <button type="button">${msg('button.exportCSV')}</button>
			               </span>
			            </span>
			         </div>
			          <div class="show-thumbnails"> 
				            <span id="${el}-show-thumbnails" class="yui-button yui-checkbox-button">
				               <span class="first-child">
				                  <button type="button" >${msg("button.show-thumbnails")}</button>
				               </span>
				            </span>
				      </div>
			      </div>
			   </div>
			
			   <div id="${el}-bulk-editor"></div>
			   
			   <div id="${el}-message" class="warning hidden"></div>
				
			   <div id="${el}-grid" class="grid"></div>
			
			   <div id="${el}-selectTypeMessage" class="hidden select-type-message">${msg("message.select-type")}</div>
			
			   <div id="${el}-bulk-editBarBottom" class="yui-ge bulk-edit-bar bulk-edit-bar-bottom flat-button">
			      <div class="yui-u first align-center">
			         <div class="item-select">&nbsp;</div>
			         <div id="${el}-paginatorBottom" class="paginator"></div>
			      </div>
			   </div>
		</div>
	</@>
</@>