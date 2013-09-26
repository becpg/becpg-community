
<@markup id="css" >
   <#-- CSS Dependencies -->
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/data-lists/toolbar.css" group="project-toolbar" />
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/components/entity-data-lists/entity-toolbar.css"  group="project-toolbar"/>
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/components/project/project-list-toolbar.css" group="project-toolbar" />
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
	<@script type="text/javascript" src="${url.context}/res/components/entity-data-lists/entity-toolbar.js" group="project-toolbar"></@script>
	<@script type="text/javascript" src="${url.context}/res/components/project/project-list-toolbar.js" group="project-toolbar"></@script>

</@>


<@markup id="html">
   <@uniqueIdDiv>
		<#assign el = args.htmlid?html>
		<#if page.url.args.view??>
				<#assign view=page.url.args.view?js_string>
		<#else>
			<#assign view="dataTable">
		</#if>
		<@inlineScript group="project-toolbar">
			   new beCPG.component.ProjectListToolbar("${el}","${view}").setMessages(
			      ${messages}
			   );
		</@>
		<div id="${el}-body" class="project-list-toolbar datalist-toolbar toolbar">
		   <div id="${el}-headerBar" class="header-bar flat-button theme-bg-2">
		      <div class="left">
		         <div id="toolbar-contribs" class="hidden" ></div>
		      </div>
		      <div class="right">
		         <div class="show-planning">
		            <span id="${el}-show-planning-button" class="yui-button yui-checkbox-button">
		               <span class="first-child">
		                  <button name="showPlanning"></button>
		               </span>
		            </span>
		         </div>
		         <div class="separator">&nbsp;</div>
		         <div class="show-gantt">
		            <span id="${el}-show-gantt-button" class="yui-button yui-checkbox-button">
		               <span class="first-child">
		                  <button name="showGantt"></button>
		               </span>
		            </span>
		         </div>
		         <div class="separator">&nbsp;</div>
		          <div class="export-csv">
		            <span id="${el}-export-csv-button" class="yui-button yui-checkbox-button">
		               <span class="first-child">
		                  <button name="exportCsv"></button>
		               </span>
		            </span>
		         </div>
		      </div>
		   </div>
		</div>
	</@>
</@>