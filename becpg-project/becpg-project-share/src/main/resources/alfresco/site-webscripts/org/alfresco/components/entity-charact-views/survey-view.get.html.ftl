
<@markup id="css" >
   <#include "../../modules/entity-datagrid/include/entity-datagrid.css.ftl"/>
   
</@>

<@markup id="js">
    <#include "../../modules/entity-datagrid/include/entity-datagrid.js.ftl"/>
    
	<@script src="${url.context}/res/components/entity-charact-views/survey-view-toolbar.js" group="entity-datalists"/>
	<@script src="${url.context}/res/components/entity-charact-views/custom-entity-toolbar.js" group="entity-datalists"/>
	
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

