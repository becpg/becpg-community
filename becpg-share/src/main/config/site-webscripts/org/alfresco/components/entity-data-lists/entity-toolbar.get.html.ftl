
<@markup id="css" >
   <#-- CSS Dependencies -->
	<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/data-lists/toolbar.css" group="entity-toolbar"/>
	<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/entity-data-lists/entity-toolbar.css" group="entity-toolbar" />
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
  <@script type="text/javascript" src="${page.url.context}/res/components/entity-data-lists/entity-toolbar.js" group="entity-toolbar"/>
</@>


<@markup id="html">
   <@uniqueIdDiv>
		<#assign el = args.htmlid?html>
		<@inlineScript group="entity-toolbar">//<![CDATA[
		   new beCPG.component.EntityDataListToolbar("${el}").setOptions(
		   {
		     siteId: "${page.url.templateArgs.site!""}",
			  entityNodeRef: "${page.url.args.nodeRef!""}"
		   }).setMessages(${messages});
		//]]></@>
		<@script type="text/javascript" src="${page.url.context}/res/components/entity-data-lists/custom-entity-toolbar.js" group="entity-toolbar"></@script>
		<div id="${args.htmlid}-body" class="datalist-toolbar toolbar">
		   <div id="${args.htmlid}-headerBar" class="header-bar flat-button theme-bg-2">
		      <div class="left">
		         <div id="toolbar-contribs" ></div>
		         <div id="${el}-toolbar-buttons-left" ></div>
		      </div>
		
		      <div class="right">
		   	 	<div id="${el}-toolbar-buttons-right" ></div>
		      </div>
		   </div>
		</div>
		<#-- template -->
		<div id="${el}-toolBar-template-button" class="hidden" >
			    <span class="yui-button yui-push-button">
			       <span class="first-child">
			             <button type="button" ></button>
			       </span>
			    </span>
		</div>
		</@>
</@>
