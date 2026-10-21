<@standalone>
   <@markup id="css" >
      <@link rel="stylesheet" type="text/css" href="${url.context}/res/modules/entity-version/versions-graph.css" group="entity-datalists"/>
      <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/entity-data-lists/entity-versions.css" group="entity-datalists"/>
      <@link rel="stylesheet" type="text/css" href="${url.context}/res/modules/entity-version/new-version.css" group="entity-datalists" />
   </@>
   
   <@markup id="js">
      <@script type="text/javascript" src="${url.context}/res/modules/entity-version/versions-graph.js" group="entity-datalists"/>
      <@script type="text/javascript" src="${url.context}/res/components/entity-data-lists/entity-versions.js" group="entity-datalists"/>
      <@script type="text/javascript" src="${url.context}/res/modules/entity-version/new-version.js" group="entity-datalists"/>
   </@>
   
   <@markup id="widgets">
         <@createWidgets group="entity-datalists"/>
   </@>
   
   <@markup id="html">
      <@uniqueIdDiv>
               <#assign el=args.htmlid?html>
               <div id="${el}-body" class="entity-versions filter hidden">
				         <div class="nav flat-button">
								<a href="#" rel="previous" class="version-historic-nav prev disabled">previous</a>
								<a href="#" rel="next" class="version-historic-nav next disabled">next</a>
								<span id="${el}-versionNav-button" class="yui-button yui-push-button">
					            <span class="first-child">
					               <button name="version-historic-versionNav-menu"></button>
					            </span>
					         </span>
								<select id="${el}-versionNav-menu"></select>
							</div>
							<div class="clear"></div>
							<div id="${el}-branchFilter" class="branch-filter hidden">
								<span id="${el}-branchFilter-count" class="branch-filter-count"></span>
								<a href="#" id="${el}-branchFilter-toggle" class="branch-filter-toggle">
									<svg class="branch-filter-eye eye-open" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
									<svg class="branch-filter-eye eye-closed" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/><path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/><path d="M14.12 14.12a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
									<span id="${el}-branchFilter-toggle-label"></span>
								</a>
							</div>
	                  <div class="panel-body">
	                     <div id="${el}-branches" class="version-list datagrid"></div>
	                  </div>
               </div>
      </@>
   </@>
</@>