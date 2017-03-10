<@markup id="css" >
	<#include "../../modules/entity-datagrid/include/entity-datagrid.css.ftl"/>
	<@link href="${url.context}/res/components/entity-charact-views/labeling-view.css" group="entity-datalists" />
	<@link href="${url.context}/res/components/entity-data-lists/product-notifications.css" group="entity-datalists" />
</@>

<@markup id="js">
   <#include "../../modules/entity-datagrid/include/entity-datagrid.js.ftl"/>
   
   <@script src="${url.context}/res/modules/custom-entity-datagrid/product-columnRenderers.js" group="entity-datalists"></@script>
   <@script src="${url.context}/res/modules/custom-entity-datagrid/product-entity-toolbar.js" group="entity-datalists"/>
   <@script src="${url.context}/res/components/entity-data-lists/product-notifications.js" group="entity-datalists"/>
   <@script src="${url.context}/res/components/entity-charact-views/dashlet-resizer.js" group="entity-datalists"/>
   <@script src="${url.context}/res/components/entity-charact-views/labeling-view.js" group="entity-datalists"/>
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
			<div id="main-view-${el}" class="labeling-view">
					<@dataGridDashlet  dashletName="ingLabelingListDashlet" dashletId="ingLabelingList-${el}" />
					<div class="yui-g">
						<div  class="yui-u first labelingRuleList" >
							<@dataGridDashlet  dashletName="labelingRuleListDashlet"
								dashletId="labelingRuleList-${el}" 
								itemType="bcpg:labelingRuleList"
								dashletTitle=msg("dashlet.labelingRuleList.title")  
								  />
						</div>
						<div class="yui-u compoList">
						  <@dataGridDashlet  dashletName="compoListDashlet"
								dashletId="compoList-${el}" 
								itemType="bcpg:compoList"
								dashletTitle=msg("dashlet.compoList.title")  
								  />
						</div>
						
					</div>
			</div>
 	</@>
 </@>
 
 