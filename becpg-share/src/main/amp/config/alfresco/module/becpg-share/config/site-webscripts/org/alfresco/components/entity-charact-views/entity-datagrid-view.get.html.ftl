
<@markup id="css" >
   <#include "../../modules/entity-datagrid/include/entity-datagrid.css.ftl"/>
   <@link type="text/css" href="${url.context}/res/ctools/tipsy.css" rel="stylesheet" group="entity-datalists" />
	<@link href="${url.context}/res/modules/entity-charact-details/entity-charact-details.css" group="entity-datalists" />
	<@link href="${url.context}/res/components/entity-charact-views/form-datagrid-view.css" group="entity-datalists"/>
</@>

<@markup id="js">
    <#include "../../modules/entity-datagrid/include/entity-datagrid.js.ftl"/>
    
	<@script src="${url.context}/res/ctools/jquery.js" type="text/javascript" group="entity-datalists"/>
    <@script src="${url.context}/res/ctools/jquery.tipsy.js" type="text/javascript" group="entity-datalists"/>
    <@script src="${url.context}/res/ctools/protovis.js" type="text/javascript" group="entity-datalists"/>
    <@script src="${url.context}/res/ctools/protovis-msie.js" type="text/javascript" group="entity-datalists"/>
    <@script src="${url.context}/res/ctools/tipsy.js" type="text/javascript" group="entity-datalists"/>
    <@script src="${url.context}/res/ctools/def.js" type="text/javascript" group="entity-datalists"/>
    <@script src="${url.context}/res/ctools/pvc.js" type="text/javascript" group="entity-datalists"/>
    
	<@script src="${url.context}/res/modules/entity-charact-details/entity-charact-details.js" group="entity-datalists"/>
	<@script src="${url.context}/res/components/entity-charact-views/form-datagrid-view.js" group="entity-datalists"/>
	
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
		<#include "../../modules/entity-datagrid/include/entity-datagrid.lib.ftl" />
		<!--[if IE]>
		<iframe id="yui-history-iframe" src="${url.context}/res/yui/history/assets/blank.html"></iframe> 
		<![endif]-->
		<input id="yui-history-field" type="hidden" />
		<div id="toolbar-contribs-${el}" style="display:none;">
			<@dataGridToolbar  toolbarId=el />
		</div>
		<@entityDataGrid showDataListTitle=true />
		<div class="hidden form-datagrid-view" id="${el}-formDatagridView" >
			<div class="edit-datagrid-form">
				 <span class="yui-button yui-push-button" id="${el}-edit-datagrid-form">
				     <span class="first-child">
				        <button type="button" tabindex="0" id="${el}-edit-datagrid-form-button" title="${msg("edit.form")}">&nbsp;</button>
				    </span>
				 </span>
			 </div>
		
			<div id="${el}-formContainer"></div>
		</div>
	</@>
</@>
