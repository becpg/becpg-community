
<@markup id="css" >
   <#-- CSS Dependencies -->
   <#include "../form/form.css.ftl"/>
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/modules/entity-datagrid/entity-datagrid.css" group="nc-list" />
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/modules/custom-entity-datagrid/entity-datagrid.css" group="nc-list" />
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/modules/entity-charact-details/entity-charact-details.css" group="nc-list" />
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <#include "../form/form.js.ftl"/>
	<@script type="text/javascript" src="${url.context}/res/modules/entity-datagrid/entity-columnRenderer.js" group="nc-list"></@script>
	<@script type="text/javascript" src="${url.context}/res/modules/custom-entity-datagrid/columnRenderers.js" group="nc-list"></@script>
	<@script type="text/javascript" src="${url.context}/res/modules/entity-datagrid/entity-actions.js" group="nc-list"></@script>
	<@script type="text/javascript" src="${url.context}/res/modules/custom-entity-datagrid/custom-entity-actions.js" group="nc-list"></@script>
	<@script type="text/javascript" src="${url.context}/res/modules/entity-datagrid/entity-datagrid.js" group="nc-list"></@script>
	
	<@script type="text/javascript" src="${url.context}/res/yui/swf/swf.js" group="nc-list"></@script>
	<@script type="text/javascript" src="${url.context}/res/yui/charts/charts.js" group="nc-list"></@script>
	<@script type="text/javascript" src="${url.context}/res/modules/entity-charact-details/entity-charact-details.js" group="nc-list"></@script>
</@>


<@markup id="widgets">
  	   <@createWidgets group="nc-list"/>
</@>


<@markup id="html">
   <@uniqueIdDiv>
		<#assign el = args.htmlid?html>
		<#include "../../modules/entity-datagrid/include/entity-datagrid.lib.ftl" />
		<!--[if IE]>
		<iframe id="yui-history-iframe" src="${url.context}/res/yui/history/assets/blank.html"></iframe> 
		<![endif]-->
		<input id="yui-history-field" type="hidden" />
		<div id="toolbar-contribs-${el}" style="display:none;">
			<@dataGridToolbar  toolbarId=el />
		</div> 
		<@entityDataGrid/>
		</@>
</@>

