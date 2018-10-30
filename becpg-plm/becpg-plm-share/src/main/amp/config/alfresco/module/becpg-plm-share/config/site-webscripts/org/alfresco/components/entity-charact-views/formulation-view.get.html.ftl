<@markup id="css" >
	<#include "../../modules/entity-datagrid/include/entity-datagrid.css.ftl"/>
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/components/entity-data-lists/variant-picker.css" group="entity-datalists" />
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/components/entity-data-lists/product-notifications.css" group="entity-datalists" />
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/components/entity-data-lists/rapidLink-toolbar.css" group="entity-datalists" />
	
	<@link href="${url.context}/res/modules/entity-charact-details/entity-charact-details.css" group="entity-datalists" />
	<@link href="${url.context}/res/components/entity-charact-views/formulation-view.css" group="entity-datalists" />
	
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/ctools/tipsy.css" group="entity-datalists" />
	
</@>

<@markup id="js">
   <#include "../../modules/entity-datagrid/include/entity-datagrid.js.ftl"/>
   
   <@script type="text/javascript" src="${url.context}/res/components/entity-data-lists/rapidLink-toolbar.js" group="entity-datalists"/>
   <@script type="text/javascript" src="${url.context}/res/components/entity-data-lists/variant-picker.js" group="entity-datalists"/>
   <@script type="text/javascript" src="${url.context}/res/components/entity-data-lists/product-notifications.js" group="entity-datalists"/>
 
    
   <@script src="${url.context}/res/modules/entity-charact-details/entity-charact-details.js" group="entity-datalists"/>

   <@script src="${url.context}/res/modules/custom-entity-datagrid/product-columnRenderers.js" group="entity-datalists"/>
   <@script src="${url.context}/res/modules/custom-entity-datagrid/product-entity-toolbar.js" group="entity-datalists"/>
  
   <@script src="${url.context}/res/components/entity-charact-views/dashlet-resizer.js" group="entity-datalists"/>
   <@script src="${url.context}/res/components/entity-charact-views/formulation-view.js" group="entity-datalists"/>


</@>

<@markup id="resources">
   <!-- Additional entity resources -->
</@markup>

<@markup id="widgets">
  	<@createWidgets group="entity-datalists"/>
</@>


<@markup id="html">
   <@uniqueIdDiv>
			<#assign el = args.htmlid?html>
			<#include "include/dashlet-view.lib.ftl" />
			<!--[if IE]>
			<iframe id="yui-history-iframe" src="${url.context}/res/yui/history/assets/blank.html"></iframe> 
			<![endif]-->
			<input id="yui-history-field" type="hidden" ></input>
			<div id="toolbar-contribs-compoList-${el}" style="display:none;">
				<div class="effectivityCheckbox" id="${el}-effectivityCheckbox"></div>
				<@dataGridToolbar filter="true" toolbarId="compoList-${el}" />
			</div>
			<div id="main-view-${el}" class="formulation-view">
					<div id="full-screen-form" class=" hidden"></div>
					<@dataGridDashlet dashletName="compoListDashlet" dashletId="compoList-${el}" hideFilter="true" hideTitle="true" hideToolbar="true" >
						
					</@dataGridDashlet>
					<div class="yui-gd">
						<div class="yui-u first dynamicCharactList">
							<@dataGridDashlet  dashletName="dynamicCharactListDashlet"
								dashletId="dynamicCharactList-${el}" 
								dashletTitle=msg("dashlet.dynamicCharactList.title") 
								itemType="bcpg:dynamicCharactList" 
								hideTitle="true"
								  />
							<div class="dynamicCharactList-prop-panel">
  					   			<span>
					   				<input  id="dynamicCharactList-${el}-colCheckbox" type="checkbox"/>
					   				<label  for="dynamicCharactList-${el}-colCheckbox">${msg("dashlet.dynamicCharactList.colCheckbox")}</label>
								</span>
					   		</div>
						</div>
					   <div class="yui-u ">
					   		<div class="customList">
					   		   <@dataGridDashlet dashletName="customListDashlet"
							   	dashletId="customList-${el}" 
							   	dashletTitle=msg("dashlet.customList.title")
							   	hideTitle="true"
							   	itemType="${customListType}" >
									<div class="customListMenu">
										<span class="yui-button yui-menu-button" id="${el}-customLists">
								            <span class="first-child">
								               <button type="button" tabindex="0"></button>
								            </span>
								         </span>
								         <select id="${el}-customLists-menu">
									         <#list customLists as customList>
									            <option value="${customList.id?html}">${msg("dashlet.customList." + customList.id+ ".title")}</option>
									         </#list>
								         </select>
							         </div>
								</@dataGridDashlet>
							</div>
					   </div>
					</div>
			</div>
 	</@>
 </@>
 
 