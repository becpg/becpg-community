
<@markup id="css" >
   <#include "../../modules/entity-datagrid/include/entity-datagrid.css.ftl"/>
   
    <@link href="${url.context}/res/modules/custom-entity-datagrid/project-entity-datagrid.css" group="entity-datalists" />
    <@link href="${url.context}/res/components/project/jsgantt.css" group="entity-datalists" />
	<@link href="${url.context}/res/components/project/project-commons.css" group="entity-datalists" />
    <@link href="${url.context}/res/components/entity-charact-views/gantt-view.css" group="entity-datalists" />
    
    <@link rel="stylesheet" type="text/css" href="${url.context}/res/ctools/tipsy.css" group="project-details" />
  	<@link href="${url.context}/res/modules/project-details/project-details.css" group="project-details" />
</@>

<@markup id="js">
    <#include "../../modules/entity-datagrid/include/entity-datagrid.js.ftl"/>
    
	<@script src="${url.context}/res/components/entity-charact-views/gantt-view-toolbar.js" group="entity-datalists"/>
	<@script src="${url.context}/res/components/entity-charact-views/custom-entity-toolbar.js" group="entity-datalists"/>
	
	<@script src="${url.context}/res/modules/custom-entity-datagrid/project-columnRenderers.js" group="entity-datalists"/>
	
    <@script src="${url.context}/res/components/project/jsgantt.js" group="entity-datalists" />
	<@script src="${url.context}/res/components/project/project-commons.js" group="entity-datalists" />

	<@script src="${url.context}/res/components/entity-charact-views/gantt-view.js" group="entity-datalists" />
    <@script src="${url.context}/res/components/entity-charact-views/gantt-columnRenderers.js" group="entity-datalists" />

	 <@script src="${url.context}/res/modules/project-details/project-details.js"  group="project-details"></@script>
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
   		<div id="${el}-project-list" class="project-list" >
			<#include "../../modules/entity-datagrid/include/entity-datagrid.lib.ftl" />
			<!--[if IE]>
			<iframe id="yui-history-iframe" src="${url.context}/res/yui/history/assets/blank.html"></iframe> 
			<![endif]-->
			<input id="yui-history-field" type="hidden" />
			<div id="${el}-legend" class="legend hidden">&nbsp;</div>
			<div id="toolbar-contribs-${el}" style="display:none;">
				<@dataGridToolbar  toolbarId=el filter=filter />
			</div>
			<@entityDataGrid showToolBar=false  showDataListTitle=false/>
			<div id="${el}-gantt" class="projects hidden" > </div>
			
		</div>
		<div id="${el}-project-details" class="hidden"  ></div>
	</@>
</@>

