<#macro dataGridToolbarNewRow toolbarId>
	<div class="new-row">
      <span id="${toolbarId}-newRowButton" class="yui-button yui-push-button">
         <span class="first-child">
            <button type="button">${msg('button.new-row')}</button>
         </span>
      </span>
  </div>
</#macro>

<#macro dataGridToolbarSelectedItem toolbarId>
	<div class="selected-items">
      <button class="no-access-check" id="${toolbarId}-selectedItems-button" name="doclist-selectedItems-button">${msg("menu.selected-items")}</button>
      <div id="${toolbarId}-selectedItems-menu" class="yuimenu" style="visibility:hidden">
         <div class="bd">
            <ul>
            <#list actionSetToolbar as action>
               <li><a type="${action.asset!""}" rel="${action.permission!""}" href="${action.href}"><span class="${action.id}">${msg(action.label)}</span></a></li>
            </#list>
               <li><a href="#"><span class="onActionDeselectAll">${msg("menu.selected-items.deselect-all")}</span></a></li>
            </ul>
         </div>
      </div>			
   </div>
</#macro>

<#macro dataGridToolbar toolbarId>
   <@dataGridToolbarSelectedItem toolbarId=toolbarId />
   <@dataGridToolbarNewRow toolbarId=toolbarId />
</#macro>

<#macro entityDataGrid showToolBar=false showDataListTitle=true>
<div id="${el}-body" class="datagrid<#if listName??> ${listName}</#if>">
   <#if showDataListTitle>
   <div class="datagrid-meta">
      <h2 id="${el}-title"></h2>
      <div id="${el}-description" class="datagrid-description"></div>
   </div>
   </#if>
   <div id="${el}-datagridBar" class="yui-gc datagrid-bar flat-button">
      <div class="yui-u first align-center">
         <#if args.filter?? && args.filter?starts_with("true") >
         <div class="filter-form" >
				<button id="${el}-filterform-button">${msg("filterform.header")}</button>
				<div id="${el}-filterform-panel"  class="yuimenu" >
					 <div class="bd">
					 		<div id="${el}-filterform"  class="filterform" ></div>
					 		<div class="filterButtonsBar">
					 			<button id="${el}-filterform-clear"   >${msg("filterform.clear")}</button>
					  			<button id="${el}-filterform-submit"  >${msg("filterform.submit")}</button>
					  		</div>
					  </div>
				</div>
			</div>
         </#if>
         <#if showToolBar><@dataGridToolbar  toolbarId=el /></#if>
      </div>
      <div class="yui-u align-right">
		<#if args.pagination?? && args.pagination?starts_with("true")>
        	 <div id="${el}-paginator" class="paginator hidden"></div>
         </#if>
         <div class="items-per-page" style="visibility: hidden;">
            <button id="${el}-itemsPerPage-button">${msg("menu.items-per-page")}</button>
         </div>
      </div>
   </div>

   <div id="${el}-grid" class="grid"></div>

   <div id="${el}-selectListMessage" class="hidden select-list-message">${msg("message.select-list")}</div>

   <div id="${el}-datagridBarBottom" class="yui-gc datagrid-bar datagrid-bar-bottom flat-button">
      <div class="yui-u first align-center">
         <div class="item-select">&nbsp;</div>
      </div>
     <div class="yui-u align-right">
     	<#if args.pagination?? && args.pagination?starts_with("true")>
         <div id="${el}-paginatorBottom" class="paginator hidden"></div>
         </#if>
     </div>
   </div>

   <!-- Action Sets -->
   <div style="display:none">
      <!-- Action Set "More..." container -->
      <div id="${args.htmlid}-moreActions">
         <div class="onActionShowMore"><a href="#" class="${args.htmlid}-show-more show-more" title="${msg("actions.more")}"><span>${msg("actions.more")}</span></a></div>
         <div class="more-actions hidden"></div>
      </div>

      <!-- Action Set Templates -->
      <div id="${args.htmlid}-actionSet" class="action-set simple">
      <#list actionSet as action>
         <div class="${action.id}"><a rel="${action.permission!""}" href="${action.href}" class="${args.htmlid}-${action.type} ${action.type}" title="${msg(action.label)}"><span>${msg(action.label)}</span></a></div>
      </#list>
      </div>
   </div>
</div>

</#macro>

