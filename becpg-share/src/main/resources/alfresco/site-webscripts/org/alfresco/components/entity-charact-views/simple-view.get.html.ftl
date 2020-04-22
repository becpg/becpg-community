
<@markup id="html">
   <@uniqueIdDiv>
			<#assign el = args.htmlid?html>
			<#include "include/dashlet-view.lib.ftl" />
			<script type="text/javascript">//<![CDATA[
			(function()
			{
			   new beCPG.module.EntityDataGrid("simpleView-${el}",true).setOptions(
			   {
			      entityNodeRef: "${args.nodeRef}" ,
			      list: "${args.list}",
			      dataUrl :  Alfresco.constants.PROXY_URI+ "becpg/entity/datalists/data/node/",
			      itemUrl :  Alfresco.constants.PROXY_URI+ "becpg/entity/datalists/item/node/",
			      usePagination: true,
			      displayBottomPagination : false,
			      useFilter: false,
			      sortable : true,
			      sortUrl :  Alfresco.constants.PROXY_URI+ "becpg/entity/datalists/sort/node",
			      itemType : "${args.itemType}",
			      columnFormId: "${args.formId!""}",
			      saveFieldUrl :  Alfresco.constants.PROXY_URI+ "becpg/bulkedit/save",
			      hiddenColumns : ["prop_bcpg_depthLevel"],
			      useHistoryManager : false,
			      configurableColumns : false
			   }).setMessages(${messages});
			   
			 
			   new beCPG.widget.DashletResizer("simpleView-${el}", "simpleViewDashlet");
	
			 })();
			   
			//]]></script>
				<@dataGridDashlet dashletName="simpleViewDashlet" dashletId="simpleView-${el}" dashletTitle="${args.title}"  
						 hideTitle="true"  hideFilter="true" itemType="${args.itemType}" />	
 	</@>
 </@>
 
 