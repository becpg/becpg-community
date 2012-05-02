<#include "../../modules/entity-datagrid/include/entity-datagrid.lib.ftl" />
<#assign id = args.htmlid>
<!--[if IE]>
<iframe id="yui-history-iframe" src="${url.context}/res/yui/history/assets/blank.html"></iframe> 
<![endif]-->
<input id="yui-history-field" type="hidden" />
<div id="toolbar-contribs-${id}" style="display:none;">
	<@dataGridToolbar  toolbarId=id />
</div>
<script type="text/javascript">//<![CDATA[
(function() {

	new beCPG.module.EntityDataGrid('${id}').setOptions(
	   {
	      siteId: "${page.url.templateArgs.site!""}",
	      usePagination: ${(args.pagination!false)?string},
	      useFilter: ${(args.filter!false)?string},
		   entityNodeRef: "${page.url.args.nodeRef!""}",
		   list: "${page.url.args.list!""}",
		   sortable : true,
			sortUrl : Alfresco.constants.PROXY_URI + "becpg/entity/datalists/sort/node",
		   dataUrl : Alfresco.constants.PROXY_URI + "${(args.dataUrl!"slingshot/datalists/data/node/")}",
		   itemUrl : Alfresco.constants.PROXY_URI + "${(args.itemUrl!"slingshot/datalists/data/item/")}"
	   }).setMessages(${messages});

	var Dom = YAHOO.util.Dom;
	//Toolbar contribs
	var controls = Dom.getChildren("toolbar-contribs-${id}")
   for(var el in controls){
   	(new  YAHOO.util.Element("toolbar-contribs")).appendChild(controls[el]);
   }
   
    
    // Initialize the browser history management library
    YAHOO.Bubbling.on("dataGridReady", function (layer, args){
		  try {
			      YAHOO.util.History.initialize("yui-history-field", "yui-history-iframe");
		      } catch (e2) {
			      	/*
						* The only exception that gets thrown here is when the
						* browser is not supported (Opera, or not A-grade)
						*/
			        Alfresco.logger.error(this.name + ": Couldn't initialize HistoryManager.", e2);
			        var obj = args[1];
	              if ((obj !== null) && (obj.entityDataGridModule !== null))
	              {
					     obj.entityDataGridModule.onHistoryManagerReady();
					  }
			}
		});
  
})();
//]]>
</script>
<@entityDataGrid/>


