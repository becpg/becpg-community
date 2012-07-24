<#assign id = args.htmlid>
<#include "../component.head.inc">
<script type="text/javascript">//<![CDATA[
   new beCPG.component.EntityDataListToolbar("${id}").setOptions(
   {
     siteId: "${page.url.templateArgs.site!""}",
	  entityNodeRef: "${page.url.args.nodeRef!""}"
   }).setMessages(${messages});
//]]></script>
<@script type="text/javascript" src="${page.url.context}/res/components/entity-data-lists/custom-entity-toolbar.js"></@script>
<div id="${args.htmlid}-body" class="datalist-toolbar toolbar">
   <div id="${args.htmlid}-headerBar" class="header-bar flat-button theme-bg-2">
      <div class="left">
         <div id="toolbar-contribs" ></div>
         <div id="${id}-toolbar-buttons" >
			    <div id="${id}-toolBar-template-button" class="hidden" >
				     <span class="yui-button yui-push-button">
				        <span class="first-child">
				              <button type="button" ></button>
				       </span>
				     </span>
				 </div>
			 </div>
      </div>

      <div class="right" style="display: none;">
      <#--
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
         -->
      </div>
   </div>
</div>
