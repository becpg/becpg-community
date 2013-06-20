<@markup id="css" >
	<#include "./include/entity-datagrid.css.ftl"/>
	
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/components/entity-charact-views/formulation-view.css" group="formulation-view" />
</@>

<@markup id="js">
   <#include "./include/entity-datagrid.js.ftl"/>
   
   <@script type="text/javascript" src="${url.context}/res/components/entity-charact-views/custom-entity-toolbar.js" group="entity-toolbar"/>
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
 
 