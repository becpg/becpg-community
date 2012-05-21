<#assign id = args.htmlid>
<script type="text/javascript">//<![CDATA[
   new beCPG.component.EntityDataListToolbar("${id}").setOptions(
   {
     siteId: "${page.url.templateArgs.site!""}",
	  entityNodeRef: "${page.url.args.nodeRef!""}",
	  showFormulate: ${showFormulate?string},
	  showECO: ${showECO?string}
   }).setMessages(${messages});
//]]></script>
<div id="${args.htmlid}-body" class="datalist-toolbar toolbar">
   <div id="${args.htmlid}-headerBar" class="header-bar flat-button theme-bg-2">
      <div class="left">
         <div id="toolbar-contribs" ></div>
			<div class="formulate">
	         <span id="${id}-formulateButton" class="yui-button yui-push-button">
	            <span class="first-child">
	               <button type="button" title="${msg('button.formulate.description')}">${msg('button.formulate')}</button>
	            </span>
	         </span>
	      </div>
	      <div class="eco-calculate-wused">
	         <span id="${id}-ecoCalculateWUsedButton" class="yui-button yui-push-button">
	            <span class="first-child">
	               <button type="button" title="${msg('button.eco-calculate-wused.description')}">${msg('button.eco-calculate-wused')}</button>
	            </span>
	         </span>
	      </div>
	      <div class="eco-do-simulation">
	         <span id="${id}-ecoDoSimulationButton" class="yui-button yui-push-button">
	            <span class="first-child">
	               <button type="button" title="${msg('button.eco-do-simulation.description')}">${msg('button.eco-do-simulation')}</button>
	            </span>
	         </span>
	      </div>
	      <div class="eco-apply">
	         <span id="${id}-ecoApplyButton" class="yui-button yui-push-button">
	            <span class="first-child">
	               <button type="button" title="${msg('button.eco-apply.description')}">${msg('button.eco-apply')}</button>
	            </span>
	         </span>
	      </div>
        			
		 	<div class="finish">
            <span id="${id}-finishButton" class="yui-button yui-push-button">
               <span class="first-child">
                  <button type="button" title="${msg('button.finish.description')}">${msg('button.finish')}</button>
               </span>
            </span>
         </div>
      </div>

      <div class="right" style="display: none;">
         <span id="${id}-printButton" class="yui-button yui-push-button print">
             <span class="first-child">
                 <button type="button">${msg('button.print')}</button>
             </span>
         </span>
         <span id="${id}-rssFeedButton" class="yui-button yui-push-button rss-feed">
             <span class="first-child">
                 <button type="button">${msg('button.rss-feed')}</button>
             </span>
         </span>
      </div>
   </div>
</div>
