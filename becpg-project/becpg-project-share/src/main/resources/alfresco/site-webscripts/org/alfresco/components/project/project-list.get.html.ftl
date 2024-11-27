<@markup id="css" >
   <#include "../../modules/entity-datagrid/include/entity-datagrid.css.ftl"/>
   <@link href="${url.context}/res/modules/custom-entity-datagrid/project-entity-datagrid.css" group="project-datalists" />

	<@link href="${url.context}/res/components/project/jsgantt.css" group="project-list" />
	<@link href="${url.context}/res/components/project/project-commons.css" group="project-list" />
	<@link href="${url.context}/res/components/project/project-list.css" group="project-list" />
	<@link href="${url.context}/res/components/project/tooltip.css" group="project-list" />
	
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/ctools/tipsy.css" group="project-details" />
	
  	<@link href="${url.context}/res/modules/project-details/project-details.css" group="project-details" />
</@>

<@markup id="js">
   <#include "../form/form.js.ftl"/>
	<@script src="${url.context}/res/modules/entity-datagrid/entity-columnRenderer.js" group="project-list" />
	<@script src="${url.context}/res/modules/custom-entity-datagrid/custom-columnRenderers.js" group="project-list" />

	<@script src="${url.context}/res/components/project/columnRenderers.js" group="project-list" />
	<@script src="${url.context}/res/modules/entity-datagrid/entity-actions.js" group="project-list" />
	<@script src="${url.context}/res/modules/custom-entity-datagrid/custom-entity-actions.js" group="project-list"></@script>
	<@script src="${url.context}/res/components/form/date-range.js" group="project-list"/>
    <@script src="${url.context}/res/components/form/number-range.js" group="project-list"/>
	<@script src="${url.context}/res/modules/entity-datagrid/groupeddatatable.js" group="project-list" />
	<@script src="${url.context}/res/modules/entity-datagrid/entity-datagrid.js" group="project-list" />
	
	<@script src="${url.context}/res/components/workflow/workflow-actions.js" group="project-list" />
	
	<@script src="${url.context}/res/components/project/jsgantt.js" group="project-list" />
	<@script src="${url.context}/res/components/project/project-commons.js" group="project-list" />
	<@script src="${url.context}/res/components/project/project-list.js" group="project-list" />
	<@script src="${url.context}/res/components/comments/comments-list.js" group="comments"/>
	
    <@script src="${url.context}/res/modules/project-details/project-details.js"  group="project-details"></@script>
</@>

<@markup id="widgets">
  	<@createWidgets group="project-list"/>
</@>

<@markup id="html">
   <@uniqueIdDiv>
		<#assign el = args.htmlid?html>
		<#assign filter=true >
		<#if page.url.args.view?? >
				<#assign view=page.url.args.view?js_string>
		<#else>
			<#assign view="dataTable">
		</#if>
		
		<#include "../../modules/entity-datagrid/include/entity-datagrid.lib.ftl" />
		<!--[if IE]>
		<iframe id="yui-history-iframe" src="${url.context}/res/yui/history/assets/blank.html"></iframe> 
		<![endif]-->
		<input id="yui-history-field" type="hidden" />
		<div id="toolbar-contribs-${el}" style="display:none;">
			<@dataGridToolbar  toolbarId=el filter=true />
		</div>
	
		
		<div id="${el}-body" class="project-list datagrid">
		    <div class="yui-gf project-list-bar datagrid-bar  flat-button">
		      <div class="yui-u first">
		    	  <span class="yui-button yui-menu-button" id="${el}-filters">
			            <span class="first-child">
			               <button type="button" tabindex="0"></button>
			            </span>
			         </span>
			      	 <select id="${el}-filters-menu">
				         <#list filters as filter>
				         	<#if filter.data?? && filter.data?length gt 0 >
				           		 <option value="${filter.id+"|"+filter.data}">${msg("filter."+filter.id+"."+filter.data)}</option>
				            <#else>
				           		 <option value="${filter.id}">${msg("filter."+filter.id)}</option>
				            </#if>
				            
				         </#list>
				      </select>	

		       </div> 
			    <div class="yui-u">
			         <div id="${el}-legend" class="legend">&nbsp;</div>
			          <div class="right">
						         <div id="${el}-paginator" class="paginator hidden"></div>
						         <div class="items-per-page" style="display:none;">
						            <button id="${el}-itemsPerPage-button">${msg("menu.items-per-page")}</button>
						         </div>
						  </div>
			     </div>
			  </div>
			   <div id="${el}-grid"  class="projects grid" <#if view=="gantt" || view=="resources" >style="display:none;"</#if>> </div>
			   <div id="${el}-gantt" class="projects ${view}" <#if view!="gantt" && view!="resources" >style="display:none;"</#if>> </div>
						
			   <div id="${el}-selectListMessage" class="hidden select-list-message">${msg("message.select-list")}</div>
			
			   <div id="${el}-datagridBarBottom" class="yui-gf datagrid-bar datagrid-bar-bottom project-list-bar-bottom flat-button">
			      <div class="yui-u first ">&nbsp;</div>
			      <div  class="yui-u">
			        	<div class="item-select">&nbsp;</div>
			      	 <div class="right">
			         	<div id="${el}-paginatorBottom" class="paginator hidden"></div>
			         </div>
			      </div>
			   </div>
			
			   <!-- Action Sets -->
			   <div style="display:none">
			      <!-- Action Set "More..." container -->
			      <div id="${el}-moreActions">
			         <div class="onActionShowMore"><a href="#" class="${el}-show-more show-more" title="${msg("actions.more")}"><span>&nbsp;</span></a></div>
			         <div class="more-actions hidden"></div>
			      </div>
			
			      <!-- Action Set Templates -->
			       <div id="${el}-actionSet" class="action-set simple">
				      <#list actionSet as action>
				         <div class="${action.id}"><a rel="${action.permission!""}" href="${action.href}" class="${el}-${action.type} ${action.type}" title="${msg(action.label)}"><span>${msg(action.label)}</span></a></div>
				      </#list>
			      </div>      
			   </div>
		</div>

</@>
</@>









