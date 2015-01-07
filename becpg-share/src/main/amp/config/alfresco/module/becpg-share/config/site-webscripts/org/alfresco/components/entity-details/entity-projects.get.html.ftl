
<#if show??>
	<#assign el=args.htmlid?html>
	
	<@markup id="css" >
	  <@link href="${url.context}/res/components/project/project-commons.css" group="entity-details" />
	  <@link href="${url.context}/res/components/entity-details/entity-projects.css" group="entity-details" />
	</@>
	
	<@markup id="js">
	   <@script src="${url.context}/res/components/project/project-commons.js"  group="entity-details"/>
		<@script src="${url.context}/res/components/entity-details/entity-projects.js"  group="entity-details"/>
	</@>
	
	
	<@markup id="widgets">
		<@createWidgets group="entity-details"/>
	   <@inlineScript group="entity-details">
	         YAHOO.util.Event.onContentReady("${el}-heading", function() {
	            Alfresco.util.createTwister("${el}-heading", "EntityProjects");
	         });
	    </@>
	</@>
	
	<@markup id="html">
	   <@uniqueIdDiv>
	         <div id="${el}-body" class="entity-projects document-details-panel project-list">
	            <h2 id="${el}-heading" class="thin dark">
	               ${msg("header.projects")}
	            </h2>
	            <div class="panel-body">
	              <div id="${el}-documents"></div>
	            </div>
	         </div>
	   </@>
	</@>

</#if>