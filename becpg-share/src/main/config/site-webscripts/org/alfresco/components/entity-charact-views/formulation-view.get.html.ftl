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


<@markup id="html">
   <@uniqueIdDiv>
		<#assign el = args.htmlid?html>
		<#include "include/formulation.lib.ftl" />
		<!--[if IE]>
		<iframe id="yui-history-iframe" src="${url.context}/res/yui/history/assets/blank.html"></iframe> 
		<![endif]-->
		<input id="yui-history-field" type="hidden" />
		<script type="text/javascript">//<![CDATA[
		    Alfresco.constants.DASHLET_RESIZE = YAHOO.env.ua.mobile === null;
		   
		      new beCPG.component.FormulationView("${el}").setOptions(
			   {
			     siteId: "${page.url.templateArgs.site!""}",
				  entityNodeRef: "${page.url.args.nodeRef!""}"
			   }).setMessages(${messages});
		
		//]]></script>
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
				  <@dataGridDashlet dashletId="compoList-${el}" />
				<div class="yui-g formulation">
					<div class="yui-u first dynamicCharactList">
						<@dataGridDashlet 
							dashletId="dynamicCharactList-${el}" 
							dashletTitle=msg("dashlet.dynamicCharactList.title")  
							itemType="bcpg:dynamicCharactList"  />
					</div>
				   <div class="yui-u constraintsList">
				   <@dataGridDashlet 
				   	dashletId="constraintsList-${el}" 
				   	dashletTitle=msg("dashlet.constraintsList.title")
				   	itemType="bcpg:reqCtrlList"  />
				   </div>
				 </div>
		 </div>
 	</@>
 </@>
 
 