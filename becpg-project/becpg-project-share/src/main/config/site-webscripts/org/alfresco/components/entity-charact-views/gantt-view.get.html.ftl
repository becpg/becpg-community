
<@markup id="css" >
   <#include "../../modules/entity-datagrid/include/entity-datagrid.css.ftl"/>
   
    <@link href="${url.context}/res/modules/custom-entity-datagrid/project-entity-datagrid.css" group="entity-datagrid" />
    <@link href="${url.context}/res/components/project/jsgantt.css" group="entity-datagrid" />
	<@link href="${url.context}/res/components/project/project-commons.css" group="entity-datagrid" />
    <@link href="${url.context}/res/components/entity-charact-views/gantt-view.css" group="entity-datagrid" />
</@>

<@markup id="js">
    <#include "../../modules/entity-datagrid/include/entity-datagrid.js.ftl"/>
    
	<@script src="${url.context}/res/components/entity-charact-views/gantt-view-toolbar.js" group="entity-toolbar"/>
	<@script src="${url.context}/res/components/entity-charact-views/custom-entity-toolbar.js" group="entity-toolbar"/>
	
	<@script src="${url.context}/res/modules/custom-entity-datagrid/project-columnRenderers.js" group="entity-datagrid"/>
	
    <@script src="${url.context}/res/components/project/jsgantt.js" group="entity-datagrid" />
	<@script src="${url.context}/res/components/project/project-commons.js" group="entity-datagrid" />

	<@script src="${url.context}/res/components/entity-charact-views/gantt-view.js" group="entity-datagrid" />
    <@script src="${url.context}/res/components/entity-charact-views/gantt-columnRenderers.js" group="entity-datagrid" />
	
</@>

<@markup id="resources">
   <!-- Additional entity resources -->
</@markup>


<@markup id="widgets">
  	<@createWidgets group="entity-datagrid"/>
</@>


<@markup id="html">
   <@uniqueIdDiv>
		<#assign el = args.htmlid?html>
		<#include "../../modules/entity-datagrid/include/entity-datagrid.lib.ftl" />
		<!--[if IE]>
		<iframe id="yui-history-iframe" src="${url.context}/res/yui/history/assets/blank.html"></iframe> 
		<![endif]-->
		<input id="yui-history-field" type="hidden" /><div id="${el}-legend" class="project-list legend hidden">&nbsp;</div>
		
		<div id="toolbar-contribs-${el}" style="display:none;">
			<@dataGridToolbar  toolbarId=el />
		</div>
		<@entityDataGrid />
		<div class="project-list"> 
			
	    	<div id="${el}-gantt" class="projects hidden" > </div>
	    </div>
	</@>
</@>

