<@markup id="css" >
   <#-- CSS Dependencies -->
   <#include "../form/form.css.ftl"/>
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/modules/entity-datagrid/entity-datagrid.css" group="entity-datagrid" />
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/modules/custom-entity-datagrid/entity-datagrid.css" group="entity-datagrid" />
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/components/entity-charact-views/formulation-view.css" group="formulation-view" />
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <#include "../form/form.js.ftl"/>
	
	<@script type="text/javascript" src="${url.context}/res/modules/entity-datagrid/entity-columnRenderer.js" group="entity-datagrid"></@script>
	<@script type="text/javascript" src="${url.context}/res/modules/custom-entity-datagrid/columnRenderers.js" group="entity-datagrid"></@script>
	
	<@script type="text/javascript" src="${url.context}/res/modules/entity-datagrid/entity-actions.js" group="entity-datagrid"></@script>
	<@script type="text/javascript" src="${url.context}/res/modules/custom-entity-datagrid/custom-entity-actions.js" group="entity-datagrid"></@script>
	<@script type="text/javascript" src="${url.context}/res/modules/entity-datagrid/entity-datagrid.js" group="entity-datagrid"></@script>
	
	<@script type="text/javascript" src="${url.context}/res/components/entity-charact-views/formulation-view.js" group="formulation-view"></@script>
</@>

<@markup id="widgets">
   	<@inlineScript group="formulation-view">
		      Alfresco.constants.DASHLET_RESIZE = true && YAHOO.env.ua.mobile === null;
		</@>
  		<@createWidgets group="formulation-view"/>
</@>


<@markup id="html">
   <@uniqueIdDiv>
		<#assign el = args.htmlid?html>
		<#include "include/formulation.lib.ftl" />
		<!--[if IE]>
		<iframe id="yui-history-iframe" src="${url.context}/res/yui/history/assets/blank.html"></iframe> 
		<![endif]-->
		<input id="yui-history-field" type="hidden" />
		
		<div id="toolbar-contribs-${el}" style="display:none;">  
		  <div class="formulate">
			    <span id="${el}-formulateButton" class="yui-button yui-push-button">
			         <span class="first-child">
			             <button type="button" title="${msg('button.formulate.description')}">${msg('button.formulate')}</button>
			         </span>
			    </span>
		   </div>
		   <div class="import">
			    <span id="${el}-importButton" class="yui-button yui-push-button">
			         <span class="first-child">
			             <button type="button" title="${msg('button.import.description')}">${msg('button.import')}</button>
			         </span>
			    </span>
		   </div>
		</div>
		<div id="main-view-${el}">
				<@dataGridDashlet dashletName="compoListDashlet" dashletId="compoList-${el}" />
				<div class="yui-g formulation">
					<div class="yui-u first dynamicCharactList">
						<@dataGridDashlet  dashletName="dynamicCharactListDashlet"
							dashletId="dynamicCharactList-${el}" 
							dashletTitle=msg("dashlet.dynamicCharactList.title")  
							itemType="bcpg:dynamicCharactList"  />
					</div>
				   <div class="yui-u constraintsList">
				   <@dataGridDashlet dashletName="constraintsListDashlet"
				   	dashletId="constraintsList-${el}" 
				   	dashletTitle=msg("dashlet.constraintsList.title")
				   	itemType="bcpg:reqCtrlList"  />
				   </div>
				 </div>
		 </div>
 	</@>
 </@>
 
 