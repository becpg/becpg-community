
<@markup id="css" >
   <#-- CSS Dependencies -->
   <#include "../form/form.css.ftl"/>
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/components/entity-data-lists/entity-datalists.css" group="entity-datalist" />
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <#include "../form/form.js.ftl"/>
	<@script type="text/javascript" src="${url.context}/res/modules/simple-dialog.js" group="entity-datalist"></@script>
	<@script type="text/javascript" src="${url.context}/res/components/data-lists/datalists.js" group="entity-datalist"></@script>
	<@script type="text/javascript" src="${url.context}/res/components/entity-data-lists/entity-datalists.js" group="entity-datalist"></@script>
</@>


<@markup id="html">
   <@uniqueIdDiv>
		<#assign el = args.htmlid?html>
		<script type="text/javascript">//<![CDATA[
		   new beCPG.component.EntityDataLists("${el}").setOptions(
		   {
		      entityNodeRef: "${page.url.args.nodeRef!""}",
		      listId: "${(page.url.args.list!"")?js_string}"
		   }).setMessages(${messages});
		//]]></script>
		<div id="${el}-body" class="datalists">
		   <div id="${el}-headerBar" class="header-bar toolbar flat-button theme-bg-2">
		      <div class="left">
		         <span id="${el}-newListButton" class="yui-button yui-push-button new-list">
		             <span class="first-child">
		                 <button type="button">${msg('button.new-list')}</button>
		             </span>
		         </span>
		      </div>
		   </div>
		   
		   <h2>${msg("header.lists")}</h2>
		   <div id="${el}-lists" class="filter"></div>
		
		   <div class="horiz-rule">&nbsp;</div>
		</div>
		</@>
</@>
