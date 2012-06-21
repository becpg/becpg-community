<#include "include/formulation.lib.ftl" />
<#assign id = args.htmlid>
<!--[if IE]>
<iframe id="yui-history-iframe" src="${url.context}/res/yui/history/assets/blank.html"></iframe> 
<![endif]-->
<input id="yui-history-field" type="hidden" />
<script type="text/javascript">//<![CDATA[
    Alfresco.constants.DASHLET_RESIZE = YAHOO.env.ua.mobile === null;
   
      new beCPG.component.FormulationView("${id}").setOptions(
	   {
	     siteId: "${page.url.templateArgs.site!""}",
		  entityNodeRef: "${page.url.args.nodeRef!""}"
	   }).setMessages(${messages});

//]]></script>
<div id="toolbar-contribs-${id}" style="display:none;">  
  <div class="formulate">
	    <span id="${id}-formulateButton" class="yui-button yui-push-button">
	         <span class="first-child">
	             <button type="button" title="${msg('button.formulate.description')}">${msg('button.formulate')}</button>
	         </span>
	    </span>
   </div>
</div>
<div id="yui-main">
		  <@dataGridDashlet dashletId="compoList-${id}" />
		<div class="yui-g formulation">
			<div class="yui-u first dynamicCharachList">
				<@dataGridDashlet 
					dashletId="dynamicCharachList-${id}" 
					dashletTitle=msg("dashlet.dynamicCharachList.title")  
					itemType="bcpg:dynamicCharachList"  />
			</div>
		   <div class="yui-u constraintsList">
		   <@dataGridDashlet 
		   	dashletId="constraintsList-${id}" 
		   	dashletTitle=msg("dashlet.constraintsList.title")
		   	itemType="bcpg:reqCtrlList"  />
		   </div>
		 </div>
 </div>
 
 
 