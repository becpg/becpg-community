<@markup id="css" >
	<#include "../../modules/entity-datagrid/include/entity-datagrid.css.ftl"/>
	
	<@link href="${url.context}/res/modules/custom-entity-datagrid/product-entity-datagrid.css" group="entity-datagrid" />
	<@link href="${url.context}/res/components/entity-charact-views/formulation-view.css" group="formulation-view" />
	
</@>

<@markup id="js">
   <#include "../../modules/entity-datagrid/include/entity-datagrid.js.ftl"/>
   
   <@script src="${url.context}/res/modules/custom-entity-datagrid/product-columnRenderers.js" group="entity-datagrid"></@script>
   <@script src="${url.context}/res/modules/custom-entity-datagrid/product-entity-toolbar.js" group="entity-toolbar"/>
  
   <@script src="${url.context}/res/components/entity-charact-views/custom-entity-toolbar.js" group="entity-toolbar"/>
    
   <@script src="${url.context}/res/components/entity-charact-views/dashlet-resizer.js" group="formulation-view"/>
   <@script src="${url.context}/res/components/entity-charact-views/formulation-view.js" group="formulation-view"/>
</@>

<@markup id="resources">
   <!-- Additional entity resources -->
</@markup>

<@markup id="widgets">
   	<@inlineScript group="formulation-view">
		    Alfresco.constants.DASHLET_RESIZE = true && YAHOO.env.ua.mobile === null;
		</@>
  		<@createWidgets group="formulation-view"/>
</@>


<@markup id="html">
   <@uniqueIdDiv>
			<#assign el = args.htmlid?html>
			<#include "include/dashlet-view.lib.ftl" />
			<!--[if IE]>
			<iframe id="yui-history-iframe" src="${url.context}/res/yui/history/assets/blank.html"></iframe> 
			<![endif]-->
			<input id="yui-history-field" type="hidden" ></input>
			<div id="main-view-${el}" class="formulation-view">
					<div id="full-screen-form" class=" hidden"></div>
					<@dataGridDashlet dashletName="compoListDashlet" dashletId="compoList-${el}" />
					<div class="yui-gc">
						<div class="yui-u first dynamicCharactList">
							<@dataGridDashlet  dashletName="dynamicCharactListDashlet"
								dashletId="dynamicCharactList-${el}" 
								dashletTitle=msg("dashlet.dynamicCharactList.title") 
								itemType="bcpg:dynamicCharactList"  />
							<div class="dynamicCharactList-prop-panel">
  					   			<label  for="${el}-dynamicCharactList-colCheckbox">${msg("dashlet.dynamicCharactList.colCheckbox")}</label>
					   			<input  id="${el}-dynamicCharactList-colCheckbox" type="checkbox"/>
					   		</div>
						</div>
					   <div class="yui-u ">
					   		<div class="constraintsList">
							   <@dataGridDashlet dashletName="constraintsListDashlet"
							   	dashletId="constraintsList-${el}" 
							   	dashletTitle=msg("dashlet.constraintsList.title")
							   	itemType="bcpg:reqCtrlList"  />
							</div>
					   </div>
					</div>
			</div>
 	</@>
 </@>
 
 