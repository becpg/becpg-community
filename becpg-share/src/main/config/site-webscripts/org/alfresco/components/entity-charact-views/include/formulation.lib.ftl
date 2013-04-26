<#include "../../../modules/entity-datagrid/include/entity-datagrid.lib.ftl" />

<#macro dataGridDashlet dashletId dashletName usePagination=true useFilter=true showCreateButton=false extra...>
<@inlineScript group="formulation-view">
	

		new Alfresco.widget.DashletResizer("${dashletId}", "${dashletName}");
	   new Alfresco.widget.DashletTitleBarActions("${dashletId}").setOptions(
			   {
			      actions:
			      [
			         {
			            cssClass: "help",
			            bubbleOnClick:
			            {
			              	<#if extra["itemType"]??>
			               	message: "${msg("dashlet.help." + extra["itemType"]?replace(":", "_"))?js_string}"
								<#else>
			               	message: "${msg("dashlet.help.composition")?js_string}"
								</#if>
			            },
			            tooltip: "${msg("dashlet.help.tooltip")?js_string}"
			         }
			      ]
			   });
			   
	   new beCPG.module.EntityDataGrid('${dashletId}'<#if extra["itemType"]??>,true</#if>).setOptions(
			   {
			       entityNodeRef: "${page.url.args.nodeRef!""}",
			       siteId: "${page.url.templateArgs.site!""}",
			       list: "${page.url.args.list!""}",
				    dataUrl : Alfresco.constants.PROXY_URI + "${(args.dataUrl!"slingshot/datalists/data/node/")}",
				    itemUrl : Alfresco.constants.PROXY_URI + "${(args.itemUrl!"slingshot/datalists/data/item/")}",
			       usePagination: "${usePagination?string}",
			       displayBottomPagination : false,
			       useFilter: "${useFilter?string}",
			       showCreateButton : "${showCreateButton?string}",
			       sortable : true,
			       sortUrl : Alfresco.constants.PROXY_URI + "becpg/entity/datalists/sort/node",
			       <#if extra["itemType"]??>
			       	itemType : "${extra["itemType"]?string}",
			       </#if>
			       saveFieldUrl : Alfresco.constants.PROXY_URI + "becpg/bulkedit/save",
			       hiddenColumns : ["prop_bcpg_depthLevel"]
			   }).setMessages(${messages});

</@>
	
<div id="${dashletId}">
<@uniqueIdDiv>
	<div class="dashlet datagrid" id="${dashletId}-body" >
		  	<div  class="title"><#if extra["dashletTitle"]??>${extra["dashletTitle"]?string}<#else><span id="${dashletId}-title"></span>&nbsp;(<span id="${dashletId}-description"></span>)</#if></div>
			<div  class="toolbar datagrid-bar flat-button">
			      <div class="left">
			         <div class="item-select">
			            <button id="${dashletId}-itemSelect-button" name="datagrid-itemSelect-button">${msg("menu.select")}</button>
			            <div id="${dashletId}-itemSelect-menu" class="yuimenu">
			               <div class="bd">
			                  <ul>
			                     <li><a href="#"><span class="selectAll">${msg("menu.select.all")}</span></a></li>
			                     <li><a href="#"><span class="selectInvert">${msg("menu.select.invert")}</span></a></li>
			                     <li><a href="#"><span class="selectNone">${msg("menu.select.none")}</span></a></li>
			                  </ul>
			               </div>
			            </div>
			         </div>
			         <@dataGridToolbarNewRow toolbarId=dashletId />
			         <@dataGridToolbarSelectedItem toolbarId=dashletId />
			         <div class="filter-form" >
							<button id="${dashletId}-filterform-button">${msg("filterform.header")}</button>
							<div id="${dashletId}-filterform-panel"  class="yuimenu" >
								 <div class="bd">
								 		<div id="${dashletId}-filterform"  class="filterform" ></div>
								 		<div class="filterButtonsBar">
								 			<button id="${dashletId}-filterform-clear"   >${msg("filterform.clear")}</button>
								  			<button id="${dashletId}-filterform-submit"   >${msg("filterform.submit")}</button>
								  		</div>
								  </div>
							</div>
						</div>
					</div>
					 <div class="right">
			         <div id="${dashletId}-paginator" class="paginator"></div>
			         <div class="items-per-page" style="display:none;">
			            <button id="${dashletId}-itemsPerPage-button">${msg("menu.items-per-page")}</button>
			         </div>
			      </div>
			      <div class="clear"></div>
			</div>
			<div  class="body scrollableList" >
		  	
			   <div id="${dashletId}-grid" class="grid"></div>
			
			   <div id="${dashletId}-selectListMessage" class="hidden select-list-message">${msg("message.select-list")}</div>
			
			   <div id="${dashletId}-datagridBarBottom" class="yui-ge datagrid-bar datagrid-bar-bottom flat-button">
			      <div class="yui-u first align-center">
			         <div class="item-select">&nbsp;</div>
			         
			         <div id="${dashletId}-paginatorBottom" class="paginator"></div>
			      </div>
			   </div>
			
			   <!-- Action Sets -->
			   <div style="display:none">
			      <!-- Action Set "More..." container -->
			      <div id="${dashletId}-moreActions">
			         <div class="onActionShowMore"><a href="#" class="${dashletId}-show-more show-more" title="${msg("actions.more")}"><span>${msg("actions.more")}</span></a></div>
			         <div class="more-actions hidden"></div>
			      </div>
			
			      <!-- Action Set Templates -->
     		       <div id="${dashletId}-actionSet" class="action-set simple">
				      <#list actionSet as action>
				         <div class="${action.id}"><a rel="${action.permission!""}" href="${action.href}" class="${dashletId}-${action.type} ${action.type}" title="${msg(action.label)}"><span>${msg(action.label)}</span></a></div>
				      </#list>
			      </div>      
			   </div>
			</div>
		</div>
	</@>	
</div>

</#macro>