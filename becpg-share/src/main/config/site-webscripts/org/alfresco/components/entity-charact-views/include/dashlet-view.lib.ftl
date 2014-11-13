<#include "../../../modules/entity-datagrid/include/entity-datagrid.lib.ftl" />

<#macro dataGridDashlet dashletId dashletName extra...>

<div id="${dashletId}">
<@uniqueIdDiv>
	<div class="dashlet datagrid" id="${dashletId}-body" >
		  	<div  class="title"><#if extra["dashletTitle"]??>${extra["dashletTitle"]?string}<#else><span id="${dashletId}-title"></span>&nbsp;(<span id="${dashletId}-description"></span>)</#if></div>
			 <div class="toolbar datagrid-bar flat-button">
			      <div class="left">
			          <#if !extra["hideFilter"]?? >
			      		<div class="filter-form" >
							<button id="${dashletId}-filterform-button">${msg("filterform.header")}</button>
							<div id="${dashletId}-filterform-panel"  class="yuimenu" >
								 <div class="bd">
								 		<div id="${dashletId}-filterform"  class="filterform" ></div>
								 		<div class="filterButtonsBar">
								 			<button id="${dashletId}-filterform-clear"   >${msg("filterform.clear")}</button>
								  			<button id="${dashletId}-filterform-submit"  >${msg("filterform.submit")}</button>
								  		</div>
								  </div>
							</div>
						</div>
					</#if>	
			         <@dataGridToolbarSelectedItem toolbarId=dashletId />
			         <@dataGridToolbarNewRow toolbarId=dashletId />
					</div>			
					 <div class="right">
			         <div id="${dashletId}-paginator" class="paginator hidden"></div>
			         <div class="items-per-page" style="display:none;">
			            <button id="${dashletId}-itemsPerPage-button">${msg("menu.items-per-page")}</button>
			         </div>
			      </div>
			      <div class="clear"></div>
			</div>
			<div  class="body scrollableList" <#if dashletPrefs?? && dashletPrefs[dashletId]?? && dashletPrefs[dashletId].height??>style="height: ${dashletPrefs[dashletId].height?string}px;"</#if> >
			   <div id="${dashletId}-grid" class="grid"></div>
			
			   <div id="${dashletId}-selectListMessage" class="hidden select-list-message">${msg("message.select-list")}</div>
			
			   <div id="${dashletId}-datagridBarBottom" class="hidden yui-ge datagrid-bar datagrid-bar-bottom flat-button" >
			      <div class="yui-u first align-center">
			         <div class="item-select">&nbsp;</div>
			         
			         <div id="${dashletId}-paginatorBottom" class="paginator hidden"></div>
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