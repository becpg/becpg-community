<@markup id="css" >
  <@link  href="${url.context}/res/components/wused/wused-form.css" group="wused-form" />
</@>

<@markup id="js">
	<@script  src="${url.context}/res/components/wused/wused-form.js" group="wused-form"/>
</@>

<@markup id="widgets">
  	<@createWidgets group="entity-datagrid"/>
</@>

<@markup id="html">
   <@uniqueIdDiv>
	 <#include "../../include/alfresco-macros.lib.ftl" />
    <#assign el=args.htmlid?html>
       <div class="share-toolbar theme-bg-2 wused-form-header">
			<div class="navigation-bar">
				<div>
					<span class="backLink">
						<a href="#" onclick="javascript:window.history.back();">${msg("back.link")}</a>
					</span>
				</div>
			</div>
		</div>
		<div class="wused-form form-container"> 
			<#if wusedTypes??>
			 <div class="form-fields item-type-select">
				<button id="${el}-wusedTypeSelect-button" name="wusedType-itemSelect-button">${msg("menu.select.type")}</button>
				<div id="${el}-wusedTypeSelect-menu" class="yuimenu" style="visibility:hidden;">
				         <div class="bd">
				             <ul>
				              <#list wusedTypes as itemType >
				                  <li><a href="#"><span class="${itemType.name}">${itemType.label}</span></a></li>
				              </#list>
				            </ul>
				        </div>
				  </div>
			</div>
			<#else>
				<#if !searchQuery?? >
					<div class="form-fields wused-entities">
						  <label for="${el}-entities">${msg("label.entities")}:<span class="mandatory-indicator">*</span></label>     
						  <div id="${el}-entities" class="object-finder">        
								  <div class="yui-ac" >
										 <div id="${el}-entities-field-autocomplete" class="ac-body" >
													 <span id="${el}-entities-field-toggle-autocomplete" class="ac-toogle"></span>
													 <span id="${el}-entities-basket" class="viewmode-value current-values"></span>										
													 <input id="${el}-entities-field" type="text" name="-" tabindex="0"  class="yui-ac-input multi-assoc" />
													 <span class="clear" ></span>
											</div>			
										<div id="${el}-entities-field-container"></div>
										<input type="hidden" id="${el}-entities-removed" name="entities_removed" />
								        <input type="hidden" id="${el}-entities-orig" name="-" value="${nodeRefs}" />
								        <input type="hidden" id="${el}-entities-added" name="entities_added" />
						   		</div>
						</div>
				  </div>
				</#if>  
			      <div class="form-fields item-type-select">
				      <button id="${el}-itemTypeSelect-button" name="bulk-edit-itemSelect-button">${msg("menu.select.type")}</button>
				   	   <div id="${el}-itemTypeSelect-menu" class="yuimenu" style="visibility:hidden;">
				               <div class="bd">
				                  <ul>
				                  <#list itemTypes as itemType >
				                     <li><a href="#"><span class="${itemType.itemType}#${itemType.assocType}<#if itemType.selected??>#selected</#if>">${itemType.label}</span></a></li>
				                  </#list>
				                  </ul>
				               </div>
				         </div>
				         <span class="align-left yui-button yui-menu-button" id="${el}-operators">
				            <span class="first-child">
				               <button type="button" tabindex="0"></button>
				            </span>
				         </span>
				         <select id="${el}-operators-menu">
							<option value="OR" >${msg("operator.or")}</option>
							<option value="AND" >${msg("operator.and")}</option>
						 </select>
				         <button id="${el}-show-button">${msg("menu.show")}</button>
				    </div>
			</#if>
		</div>
		</@>
	</@>