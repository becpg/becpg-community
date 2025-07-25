
<@markup id="css" >
   <#-- CSS Dependencies -->
   <#include "../form/form.css.ftl"/>
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/components/entity-data-lists/entity-datalists.css" group="entity-datalists" />
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <#include "../form/form.js.ftl"/>
	<@script type="text/javascript" src="${url.context}/res/components/data-lists/datalists.js" group="entity-datalists"></@script>
	<@script type="text/javascript" src="${url.context}/res/components/entity-data-lists/entity-datalists.js" group="entity-datalists"></@script>
    <@script type="text/javascript" src="${url.context}/res/components/entity-data-lists/entity-datalists.js" group="entity-datalists"></@script>
</@>


<@markup id="widgets">
  	<@createWidgets group="entity-datalists"/>
</@>

<@markup id="html">
   <@uniqueIdDiv>
		<#assign el = args.htmlid?html>
		<div id="${el}-body" class="datalists">
		   <div id="${el}-headerBar" class="header-bar toolbar flat-button theme-bg-2">
		      <div class="left">
		      
		      	<#if showCreate?? && showCreate == true>
		         <span id="${el}-newListButton" class="yui-button yui-push-button new-list">
		             <span class="first-child">
		                 <button type="button">${msg('button.new-list')}</button>
		             </span>
		         </span>
				<#elseif itemType??>	
					<span class="entity ${itemType?split(":")[1]!""}">${msg("type."+itemType?replace(":","_"))}</span>
            	</#if>
		      </div>
		   </div>
		   
		   <div id="${el}-lists" class="filter "></div>
		
		   <div class="horiz-rule">&nbsp;</div>
		</div>
		</@>
</@>
