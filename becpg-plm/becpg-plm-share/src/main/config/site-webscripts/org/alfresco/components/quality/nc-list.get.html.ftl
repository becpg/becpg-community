
<@markup id="css" >
   <#include "../entity-charact-views/include/entity-datagrid.css.ftl"/>
	<@link href="${url.context}/res/components/quality/nc-list.css" group="nc-list" />
	<@link href="${url.context}/res/components/comments/comments-list.css" group="comments"/>
</@>

<@markup id="js">
   <#include "../entity-charact-views/include/entity-datagrid.js.ftl"/>
   
	<@script type="text/javascript" src="${url.context}/res/components/quality/nc-list.js" group="nc-list"></@script>
	<@script src="${url.context}/res/components/comments/comments-list.js" group="comments"/>
</@>



<@markup id="widgets">
  	   <@createWidgets group="nc-list"/>
  	   <@inlineScript group="nc-list">
   		YAHOO.Bubbling.fire("activeDataListChanged", {dataList:{name:"ncList", itemType:"qa:nc"}});
		</@>
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
			<@dataGridToolbar toolbarId=el />
		</div> 
		<@entityDataGrid showDataListTitle=false/>
	</@>
</@>

