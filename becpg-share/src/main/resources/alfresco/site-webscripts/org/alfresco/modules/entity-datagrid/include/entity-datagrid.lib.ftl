<#macro dataGridToolbarNewRow toolbarId>
	<div class="new-row">
		<#if (args.mode!"") != "view">
      		<span id="${toolbarId}-newRowButton" class="yui-button yui-push-button">
         		<span class="first-child">
            		<button type="button" title="${msg('button.new-row')}">&nbsp;</button>
         		</span>
      		</span>
		</#if>
	</div>
</#macro>

<#macro dataGridToolbarSelectedItem toolbarId>
	<div class="selected-items">
      <button class="no-access-check" id="${toolbarId}-selectedItems-button" name="doclist-selectedItems-button" title="${msg("menu.selected-items")}">&nbsp;&#9662;</button>
      <div id="${toolbarId}-selectedItems-menu" class="yuimenu" style="visibility:hidden">
         <div class="bd">
            <ul>
            <#list actionSetToolbar as action>
               <li><a type="${action.asset!""}" rel="${action.permission!""}" href="${action.href}"><span class="${action.id}">${msg(action.label)}</span></a></li>
            </#list>
            </ul>
         </div>
      </div>			
   </div>
</#macro>


<#macro dataGridToolbarFilter toolbarId>
	<div class="filter-form" >
			<button id="${toolbarId}-filterform-button" title="${msg("filterform.header")}">&nbsp;</button>
			<div id="${toolbarId}-filterform-panel"  class="yuimenu filterform-panel" >
						<div class="bd">
							    <div class="filterButtonsBar">
								 		<button id="${toolbarId}-filterform-clear"   >${msg("filterform.clear")}</button>
								  		<button id="${toolbarId}-filterform-submit"  >${msg("filterform.submit")}</button>
								  	</div>
								 	<div id="${toolbarId}-filterform"  class="filterform" >
								 		<img class="icon16" src="${url.context}/components/images/lightbox/loading.gif" />				 		
								 	</div>
						  </div>
				</div>
		</div>
</#macro>


<#macro dataGridToolbar toolbarId filter=false >
	<#if filter?? && filter >
         <@dataGridToolbarFilter  toolbarId=toolbarId />
    </#if>
    <@dataGridToolbarSelectedItem toolbarId=toolbarId />
    <@dataGridToolbarNewRow toolbarId=toolbarId />
</#macro>

<#macro entityDataGrid showToolBar=false showDataListTitle=true>
<div id="${el}-body" class="datagrid<#if listName??> ${listName}</#if>">
  
   <div id="${el}-message" class="hidden warning"></div>
  
   <#if showDataListTitle>
   <div class="datagrid-meta">
      <h2 id="${el}-title"></h2>
      <div id="${el}-description" class="datagrid-description"></div>
   </div>
   </#if>
   <div id="${el}-datagridBar" class="yui-gc datagrid-bar flat-button">
      <div class="yui-u first align-center">
         <#if showToolBar><@dataGridToolbar  toolbarId=el /></#if>
      </div>
      <div class="yui-u align-right">
		 <#if pagination?? && pagination>
        	 <div id="${el}-paginator" class="paginator hidden"></div>
          </#if>
      </div>
   </div>
   
   <div id="${el}-grid" class="grid"></div>

   <div id="${el}-selectListMessage" class="hidden select-list-message">${msg("message.select-list")}</div>

   <div id="${el}-datagridBarBottom" class="yui-gc datagrid-bar datagrid-bar-bottom flat-button">
      <div class="yui-u first align-center">
         <div class="item-select">&nbsp;</div>
      </div>
     <div class="yui-u align-right">
     	<#if pagination?? && pagination>
         <div id="${el}-paginatorBottom" class="paginator hidden"></div>
         </#if>
     </div>
   </div>

   <!-- Action Sets -->
   <div style="display:none">
      <!-- Action Set "More..." container -->
      <div id="${args.htmlid}-moreActions">
         <div class="onActionShowMore"><a href="#" class="${args.htmlid}-show-more show-more" title="${msg("actions.more")}"><span>&nbsp;</span></a></div>
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

