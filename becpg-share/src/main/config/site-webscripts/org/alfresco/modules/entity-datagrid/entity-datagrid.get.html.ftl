


<@markup id="css" >
	<#include "./include/entity-datagrid.css.ftl"/>

   <@link rel="stylesheet" type="text/css" href="${url.context}/res/modules/entity-datagrid/entity-datagrid.css" />
</@>

<@markup id="js">
	<#include "./include/entity-datagrid.js.ftl"/>

   <@script type="text/javascript" src="${url.context}/res/modules/entity-datagrid/entity-columnRenderer.js"></@script>
	<@script type="text/javascript" src="${url.context}/res/modules/data-lists/datalist-actions.js"></@script>
	<@script type="text/javascript" src="${url.context}/res/modules/entity-datagrid/entity-actions.js"></@script>
	<@script type="text/javascript" src="${url.context}/res/modules/entity-datagrid/entity-datagrid.js"></@script>
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
	   <#include "include/entity-datagrid.lib.ftl" />
		<!--[if IE]>
		   <iframe id="yui-history-iframe" src="${url.context}/res/yui/history/assets/blank.html"></iframe> 
		<![endif]-->
		<input id="yui-history-field" type="hidden" />
		
		<@entityDataGrid showToolBar=true/>
	</@>
</@>

