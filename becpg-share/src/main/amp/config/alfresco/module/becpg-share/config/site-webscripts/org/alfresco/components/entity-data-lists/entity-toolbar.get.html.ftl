
<@markup id="css" >
   <#-- CSS Dependencies -->
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/components/data-lists/toolbar.css" group="entity-datalists"/>
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/components/entity-data-lists/entity-toolbar.css" group="entity-datalists" />
    <@link rel="stylesheet" type="text/css" href="${url.context}/res/modules/entity-workflows/entity-workflows.css" group="entity-datalists" />
</@>

<@markup id="js">
   <#-- JavaScript Dependencies --> 
    <@script type="text/javascript" src="${url.context}/res/modules/documentlibrary/doclib-actions.js" group="entity-datalists"/> 
    <@script type="text/javascript" src="${url.context}/res/components/entity-data-lists/entity-toolbar.js" group="entity-datalists"/>
</@>

<@markup id="widgets">
  	<@createWidgets group="entity-datalists"/>
</@>

<@markup id="custom-entity-toolbar-js">
   <@script src="${url.context}/res/components/documentlibrary/becpg/docLibAction.js" group="entity-datalists"/>
   <@script src="${url.context}/res/components/entity-charact-views/custom-entity-toolbar.js" group="entity-datalists"/>
</@markup>


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
		   	 	<div id="${el}-toolbar-buttons-right" >
		   	 	</div>
				<#if nodeRef??>
		   	 	<div>
					<div class="action-set-menu doclist">
      				   <button  id="${el}-action-set-button" >${msg("button.entity-actions")}&nbsp;&#9662;</button>
				   	   <div id="${el}-action-set-menu" class="yuimenu" >
				         <div class="bd">
				            <ul id="${el}-actionSet" ></ul>
				         </div>
				      </div>			
				 	</div>
				</div>
				</#if>	
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
