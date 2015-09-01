<@markup id="css" >
   <#include "../../modules/entity-datagrid/include/entity-datagrid.css.ftl"/>

	<@link href="${url.context}/res/components/project/jsgantt.css" group="project-list" />
	<@link href="${url.context}/res/components/project/project-commons.css" group="project-list" />
	<@link href="${url.context}/res/components/project/project-list.css" group="project-list" />
</@>

<@markup id="js">
   <#include "../form/form.js.ftl"/>
	<@script src="${url.context}/res/modules/entity-datagrid/entity-columnRenderer.js" group="project-list" />
	<@script src="${url.context}/res/components/project/columnRenderers.js" group="project-list" />
	<@script src="${url.context}/res/modules/entity-datagrid/entity-actions.js" group="project-list" />
	<@script src="${url.context}/res/modules/custom-entity-datagrid/custom-entity-actions.js" group="project-list"></@script>
	<@script src="${url.context}/res/components/form/date-range.js" group="nc-list"/>
    <@script src="${url.context}/res/components/form/number-range.js" group="nc-list"/>
	<@script src="${url.context}/res/modules/entity-datagrid/groupeddatatable.js" group="project-list" />
	<@script src="${url.context}/res/modules/entity-datagrid/entity-datagrid.js" group="project-list" />
	
	<@script src="${url.context}/res/components/workflow/workflow-actions.js" group="project-list" />
	
	<@script src="${url.context}/res/components/project/jsgantt.js" group="project-list" />
	<@script src="${url.context}/res/components/project/project-commons.js" group="project-list" />
	<@script src="${url.context}/res/components/project/project-list.js" group="project-list" />
	<@script src="${url.context}/res/components/comments/comments-list.js" group="comments"/>
</@>

<@markup id="widgets">
  	<@createWidgets group="project-list"/>
</@>

<@markup id="html">
   <@uniqueIdDiv>
		<#assign el = args.htmlid?html>
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
			<@dataGridToolbar  toolbarId=el />
		</div>
		
		<div id="${el}-body" class="project-list datagrid">
		   <div class="yui-gf project-list-bar datagrid-bar  flat-button">
		      <div class="yui-u first">
		         <h2 id="${el}-filterTitle" class="thin">
		            &nbsp;
		         </h2>
		         <div class="filter-form" >
						<button id="${el}-filterform-button">${msg("filterform.header")}</button>
						<div id="${el}-filterform-panel"  class="yuimenu" >
							 <div class="bd">
							 		<div id="${el}-filterform"  class="filterform" ></div>
							 		<div class="filterButtonsBar">
							 			<button id="${el}-filterform-clear"   >${msg("filterform.clear")}</button>
							  			<button id="${el}-filterform-submit"   >${msg("filterform.submit")}</button>
							  		</div>
							  </div>
						</div>
					</div>
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
			         <div class="onActionShowMore"><a href="#" class="${el}-show-more show-more" title="${msg("actions.more")}"><span>${msg("actions.more")}</span></a></div>
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









