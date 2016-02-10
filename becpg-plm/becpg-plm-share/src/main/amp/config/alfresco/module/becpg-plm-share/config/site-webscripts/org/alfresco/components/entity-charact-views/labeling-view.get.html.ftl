<@markup id="css" >
	<#include "../../modules/entity-datagrid/include/entity-datagrid.css.ftl"/>
	<@link href="${url.context}/res/components/entity-charact-views/labeling-view.css" group="entity-datalists" />
</@>

<@markup id="js">
   <#include "../../modules/entity-datagrid/include/entity-datagrid.js.ftl"/>
   
   <@script src="${url.context}/res/modules/custom-entity-datagrid/product-columnRenderers.js" group="entity-datalists"></@script>
   <@script src="${url.context}/res/modules/custom-entity-datagrid/product-entity-toolbar.js" group="entity-datalists"/>
   
   <@script src="${url.context}/res/components/entity-charact-views/dashlet-resizer.js" group="entity-datalists"/>
</@>

<@markup id="resources">
   <!-- Additional entity resources -->
</@markup>

<@markup id="widgets">
  		<@createWidgets group="entity-datalists"/>
  		<@inlineScript group="entity-datalists">
  		  (function() {
		      var dataGridModuleCount = 1;
		      YAHOO.Bubbling.on("dataGridReady", function(layer, args) {
		         if (dataGridModuleCount == 3) {
		            try {
		               YAHOO.util.History.initialize("yui-history-field", "yui-history-iframe");
		            } catch (e2) {
		               var obj = args[1];
		               if ((obj !== null) && (obj.entityDataGridModule !== null)) {
		                  obj.entityDataGridModule.onHistoryManagerReady();
		               }
		            }
		         }
		         dataGridModuleCount++;
	      });
			})();
  		</@>
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
 
 