<@markup id="css" >
   <#include "../../modules/entity-datagrid/include/entity-datagrid.css.ftl"/>
    <@link href="${url.context}/res/modules/custom-entity-datagrid/project-entity-datagrid.css" group="entity-datalists" />
    <@link  href="${url.context}/res/components/entity-data-lists/product-notifications.css" group="entity-datalists" />
    <@link href="${url.context}/res/components/product/product-list.css" group="product-list" />
</@>

<@markup id="js">
    <#include "../../modules/entity-datagrid/include/entity-datagrid.js.ftl"/>
    <@script src="${url.context}/res/components/entity-data-lists/product-notifications.js" group="entity-datalists"/>
    <@script src="${url.context}/res/components/project/columnRenderers.js" group="entity-datalists" />
    <@script src="${url.context}/res/modules/custom-entity-datagrid/product-columnRenderers.js" group="entity-datalists"/>
    <@script src="${url.context}/res/modules/custom-entity-datagrid/product-entity-toolbar.js" group="entity-datalists"/>
	<@script src="${url.context}/res/components/product/product-list.js" group="product-list" />
</@>

<@markup id="widgets">
  	<@createWidgets group="product-list"/>
</@>


<@markup id="resources">
   <!-- Additional entity resources -->
</@markup>



<@markup id="html">
   <@uniqueIdDiv>
		<#assign el = args.htmlid?html>
		<#assign filter=true >
		
		<#include "../../modules/entity-datagrid/include/entity-datagrid.lib.ftl" />
		<!--[if IE]>
		<iframe id="yui-history-iframe" src="${url.context}/res/yui/history/assets/blank.html"></iframe> 
		<![endif]-->
		<input id="yui-history-field" type="hidden" />
		<div id="toolbar-contribs-${el}" style="display:none;">
			<@dataGridToolbar  toolbarId=el filter=true />
		</div>
	
		<div class="product-list">
			<@entityDataGrid showDataListTitle=false />		
		</div>	
</@>
</@>









