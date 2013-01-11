

<@markup id="css" >
   <#-- CSS Dependencies -->
	<#include "../form/form.css.ftl"/>
	<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/modules/entity-datagrid/entity-datagrid.css" group="project-list" />
	<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/modules/custom-entity-datagrid/entity-datagrid.css" group="project-list" />

	<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/project/jsgantt.css" group="project-list" />
	
	<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/project/project-list.css" group="project-list" />
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <#include "../form/form.js.ftl"/>
	<@script type="text/javascript" src="${page.url.context}/res/modules/entity-datagrid/entity-columnRenderer.js" group="project-list" />
	<@script type="text/javascript" src="${page.url.context}/res/components/project/columnRenderers.js" group="project-list" />
	<@script type="text/javascript" src="${page.url.context}/res/modules/entity-datagrid/entity-actions.js" group="project-list" />
	
	<@script type="text/javascript" src="${page.url.context}/res/modules/entity-datagrid/groupeddatatable.js" group="project-list" />
	<@script type="text/javascript" src="${page.url.context}/res/modules/entity-datagrid/entity-datagrid.js" group="project-list" />
	
	<@script type="text/javascript" src="${page.url.context}/res/components/workflow/workflow-actions.js" group="project-list" />
	
	<@script type="text/javascript" src="${page.url.context}/res/components/project/jsgantt.js" group="project-list" />
	
	<@script type="text/javascript" src="${page.url.context}/res/components/project/project-list.js" group="project-list" />

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
		   <div class="yui-gd project-list-bar datagrid-bar  flat-button">
		      <div class="yui-u first">
		         <h2 id="${el}-filterTitle" class="thin">
		            &nbsp;
		         </h2>
					<div class="item-select" <#if view=="gantt" >style="display:none;"</#if>>
		            <button id="${el}-itemSelect-button" name="datagrid-itemSelect-button">${msg("menu.select")}</button>
		            <div id="${el}-itemSelect-menu" class="yuimenu">
		               <div class="bd">
		                  <ul>
		                     <li><a href="#"><span class="selectAll">${msg("menu.select.all")}</span></a></li>
		                     <li><a href="#"><span class="selectInvert">${msg("menu.select.invert")}</span></a></li>
		                     <li><a href="#"><span class="selectNone">${msg("menu.select.none")}</span></a></li>
		                  </ul>
		               </div>
		            </div>
		         </div>
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
						         <div id="${el}-paginator" class="paginator"></div>
						         <div class="items-per-page" style="display:none;">
						            <button id="${el}-itemsPerPage-button">${msg("menu.items-per-page")}</button>
						         </div>
						  </div>
			      </div>
				</div>
			   <div id="${el}-grid"  class="projects grid" <#if view=="gantt" >style="display:none;"</#if>> </div>
			   <div id="${el}-gantt" class="projects" <#if view!="gantt" >style="display:none;"</#if>> </div>
						
			   <div id="${el}-selectListMessage" class="hidden select-list-message">${msg("message.select-list")}</div>
			
			   <div id="${el}-datagridBarBottom" class="yui-ge datagrid-bar datagrid-bar-bottom project-list-bar-bottom flat-button">
			      <div class="yui-u first align-center">
			         <div class="item-select">&nbsp;</div>
			         
			         <div id="${el}-paginatorBottom" class="paginator"></div>
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
		
		
		<script type="text/javascript">//<![CDATA[
		(function() {
		
		
			new beCPG.component.ProjectList("${el}", "${view}").setOptions(
			   {
			      siteId: "${page.url.templateArgs.site!""}",
			      usePagination: true,
			      useFilter: true,
			      itemType : "pjt:project",
				   list: "projectList",
				   sortable : false,
					sortUrl : Alfresco.constants.PROXY_URI + "becpg/entity/datalists/sort/node",
				   dataUrl : Alfresco.constants.PROXY_URI + "becpg/entity/datalists/data/node",
				   itemUrl : Alfresco.constants.PROXY_URI + "becpg/entity/datalists/item/node/",
				   groupBy : "prop_pjt_projectHierarchy1",
				   hiddenColumns : ["prop_bcpg_code","prop_pjt_completionPercent", "prop_pjt_projectCompletionDate","prop_pjt_projectDueDate"]
			   }).setMessages(${messages});
		
			
		
		   
		    
		    // Initialize the browser history management library
		    YAHOO.Bubbling.on("dataGridReady", function (layer, args){
				  try {
					      YAHOO.util.History.initialize("yui-history-field", "yui-history-iframe");
				      } catch (e2) {
					      	/*
								* The only exception that gets thrown here is when the
								* browser is not supported (Opera, or not A-grade)
								*/
					        Alfresco.logger.error(this.name + ": Couldn't initialize HistoryManager.", e2);
					        var obj = args[1];
			              if ((obj !== null) && (obj.entityDataGridModule !== null))
			              {
							     obj.entityDataGridModule.onHistoryManagerReady();
							  }
					}
					//Toolbar contribs
					var controls = YAHOO.util.Dom.getChildren("toolbar-contribs-${el}")
				   for(var el in controls){
				   	(new  YAHOO.util.Element("toolbar-contribs")).appendChild(controls[el]);
				   }
				});
		  
		})();
		//]]>
		</script>
</@>
</@>









