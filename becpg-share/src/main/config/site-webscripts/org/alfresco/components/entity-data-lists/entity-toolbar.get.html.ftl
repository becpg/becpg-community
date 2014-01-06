
<@markup id="css" >
   <#-- CSS Dependencies -->
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/components/data-lists/toolbar.css" group="entity-toolbar"/>
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/components/entity-data-lists/entity-toolbar.css" group="entity-toolbar" />
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/components/entity-data-lists/variant-picker.css" group="entity-toolbar" />
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/components/entity-data-lists/rapidLink-toolbar.css" group="entity-toolbar" />
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <@script type="text/javascript" src="${url.context}/res/components/entity-data-lists/rapidLink-toolbar.js" group="entity-toolbar"/>
   <@script type="text/javascript" src="${url.context}/res/components/entity-data-lists/variant-picker.js" group="entity-toolbar"/>
   <@script type="text/javascript" src="${url.context}/res/components/entity-data-lists/entity-toolbar.js" group="entity-toolbar"/>
</@>

<@markup id="widgets">
  	<@createWidgets group="entity-toolbar"/>
</@>



<@markup id="html">
   <@uniqueIdDiv>
		<#assign el = args.htmlid?html>
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
