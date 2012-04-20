<#include "include/formulation.lib.ftl" />
<#assign id = args.htmlid>
<!--[if IE]>
<iframe id="yui-history-iframe" src="${url.context}/res/yui/history/assets/blank.html"></iframe> 
<![endif]-->
<input id="yui-history-field" type="hidden" />
<script type="text/javascript">//<![CDATA[
    Alfresco.constants.DASHLET_RESIZE = YAHOO.env.ua.mobile === null;
   
   (function()
   {
    var dataGridModuleCount = 1;
    
    YAHOO.Bubbling.on("dataGridReady", function (layer, args){
    		if(dataGridModuleCount == 3){
   			// Initialize the browser history management library
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
			}
			dataGridModuleCount++;
		});
   
    })();
   
   //]]></script>
<div id="yui-main">
		  <@dataGridDashlet dashletId="compoList-${id}" />
		<div class="yui-g formulation">
			<div class="yui-u first">
				<@dataGridDashlet 
					dashletId="dynamicCharachList-${id}" 
					dashletTitle=msg("dashlet.dynamicCharachList.title")  
					itemType="bcpg:dynamicCharachList"  />
			</div>
		   <div class="yui-u">
		   <@dataGridDashlet 
		   	dashletId="constraintsList-${id}" 
		   	dashletTitle=msg("dashlet.constraintsList.title")
		   	itemType="bcpg:forbiddenIngList"  />
		   </div>
		 </div>
 </div>
 
 
 