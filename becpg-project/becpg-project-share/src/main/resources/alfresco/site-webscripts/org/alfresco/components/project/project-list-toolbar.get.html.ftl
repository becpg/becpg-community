
<@markup id="css" >
   <#-- CSS Dependencies -->
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/data-lists/toolbar.css" group="project-toolbar" />
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/components/entity-data-lists/entity-toolbar.css"  group="project-toolbar"/>
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/components/project/project-list-toolbar.css" group="project-toolbar" />
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
	<@script type="text/javascript" src="${url.context}/res/components/entity-data-lists/entity-toolbar.js" group="project-toolbar"></@script>
	<@script type="text/javascript" src="${url.context}/res/js/async-download.js" group="project-toolbar"/>
	<@script type="text/javascript" src="${url.context}/res/components/project/project-list-toolbar.js" group="project-toolbar"></@script>

</@>

<@markup id="widgets">
  		<@createWidgets group="project-toolbar"/>
</@>

<#assign prefSimpleView = preferences.simpleView!false >

<@markup id="html">
   <@uniqueIdDiv>
		<#assign el = args.htmlid?html>
		<div id="${el}-body" class="project-list-toolbar datalist-toolbar toolbar">
		   <div id="${el}-headerBar" class="header-bar flat-button theme-bg-2">
		      <div class="left">   
		      	 <div id="toolbar-contribs" class="hidden" ></div>
		      	 <#if view == "dataTable">
		      	 <div id="${el}-simpleDetailed" class="align-right simple-detailed yui-buttongroup inline">
			            <span class="yui-button yui-radio-button simple-view<#if prefSimpleView> yui-button-checked yui-radio-button-checked</#if>">
			               <span class="first-child">
			                  <button type="button" tabindex="0" title="${msg("button.view.simple")}"></button>
			               </span>
			            </span>
			            <span class="yui-button yui-radio-button detailed-view<#if !prefSimpleView> yui-button-checked yui-radio-button-checked</#if>">
			               <span class="first-child">
			                  <button type="button" tabindex="0" title="${msg("button.view.detailed")}"></button>
			               </span>
			            </span>
			       </div>	  
			   </#if>
				  <div class="full-screen">
		            <span id="${el}-full-screen-button" class="yui-button yui-checkbox-button">
		               <span class="first-child">
		                  <button name="fullScreen"></button>
		               </span>
		            </span>
		         </div>
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
		         <div class="show-tasks">
		            <span id="${el}-show-tasks-button" class="yui-button yui-checkbox-button">
		               <span class="first-child">
		                  <button name="showTasks"></button>
		               </span>
		            </span>
		         </div>
		          <div class="separator">&nbsp;</div>
		         <div class="show-resources">
		            <span id="${el}-show-resources-button" class="yui-button yui-checkbox-button">
		               <span class="first-child">
		                  <button name="showResources"></button>
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
		         <div class="separator">&nbsp;</div>
		         <div class="reporting-menu">
					    <span class="yui-button yui-push-button" id="${el}-reporting-menu-button">
					         <span class="first-child">
					         	<button name="downloadReport"></button>
					         </span>
					    </span>
					    <select id="${el}-reporting-menu"  ></select>
		         </div>
		          
		      </div>
		   </div>
		</div>
	</@>
</@>