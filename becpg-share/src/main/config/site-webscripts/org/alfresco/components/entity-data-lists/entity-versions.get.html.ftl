<@standalone>
   <@markup id="css" >
      <#-- CSS Dependencies -->
      <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/entity-data-lists/entity-versions.css" group="entity-version"/>
   </@>
   
   <@markup id="js">
      <#-- JavaScript Dependencies -->
      <@script type="text/javascript" src="${url.context}/res/components/entity-data-lists/entity-versions.js" group="entity-version"/>
   </@>
   
   <@markup id="widgets">
         <@createWidgets group="entity-version"/>
   </@>
   
   <@markup id="html">
      <@uniqueIdDiv>
               <#assign el=args.htmlid?html>
               <div id="${el}-body" class="entity-versions filter hidden">
				         <div class="nav flat-button">
								<a href="#" rel="previous" class="version-historic-nav prev">previous</a>
								<a href="#" rel="next" class="version-historic-nav next">next</a>
								<span id="${el}-versionNav-button" class="yui-button yui-push-button">
					            <span class="first-child">
					               <button name="version-historic-versionNav-menu"></button>
					            </span>
					         </span>
								<select id="${el}-versionNav-menu"></select>
							</div>
							<div class="clear"></div>
	                  <div class="panel-body">
	                     <div id="${el}-branches" class="version-list datagrid"></div>
	                  </div>
               </div>
      </@>
   </@>
</@>