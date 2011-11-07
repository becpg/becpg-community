<#assign el=args.htmlid>
<#assign formId=args.htmlid?js_string + "-form">
<input id="yui-history-field" type="hidden" />
<script type="text/javascript">//<![CDATA[
   new  beCPG.component.BulkEdit("${el}").setOptions(
   {
      searchQuery: <#if nodeRef??>"${searchQuery?js_string}"<#else>""</#if>,
      nodeRef: <#if nodeRef??>"${nodeRef?js_string}"<#else>""</#if>,
      usePagination: true
   }).setMessages(${messages});
   
//]]></script>
<#if error?exists>
   <div class="error">${error}</div>
</#if>
<div id="${el}-body" class="bulk-edit">
	    <div class="bulk-edit-meta">
	     <div class="item-type-select">
	      <button id="${el}-itemTypeSelect-button" name="bulk-edit-itemSelect-button">${msg("menu.select.type")}</button>
	   	   <div id="${el}-itemTypeSelect-menu" class="yuimenu" style="visibility:hidden;">
	               <div class="bd">
	                  <ul>
	                  <#list itemTypes as itemType >
	                     <li><a href="#"><span class="${itemType.name}">${itemType.label}</span></a></li>
	                  </#list>
	                  </ul>
	               </div>
	         </div>
	         <button id="${el}-show-button">${msg("menu.show")}</button>
	      </div>
	      <div id="${el}-itemProps-container" class="bulk-edit-props-container"></div>
	   </div>
	   <div id="${el}-bulk-editBar" class="yui-ge bulk-edit-bar flat-button theme-bg-color-3">
	      <div class="yui-u first align-center">
	         <div class="item-select">
	            <button id="${el}-itemSelect-button" name="bulk-edit-itemSelect-button">${msg("menu.select")}</button>
	            <div id="${el}-itemSelect-menu" class="yuimenu" style="visibility:hidden;">
	               <div class="bd">
	                  <ul>
	                     <li><a href="#"><span class="selectAll">${msg("menu.select.all")}</span></a></li>
	                     <li><a href="#"><span class="selectInvert">${msg("menu.select.invert")}</span></a></li>
	                     <li><a href="#"><span class="selectNone">${msg("menu.select.none")}</span></a></li>
	                  </ul>
	               </div>
	            </div>
	            <button id="${el}-edit-selected" >${msg("button.edit-selected")}</button>
	         </div>
	         
	         <div id="${el}-paginatorTop" class="paginator"></div>
	      </div>
	      <div class="yui-u align-right">
	    	 <div class="items-per-page" style="visibility:hidden;">
	            <button id="${el}-itemsPerPage-button">${msg("menu.items-per-page")}</button>
	         </div>
	      </div>
	   </div>
	
	   <div id="${el}-bulk-editor"></div>
		
	   <div id="${el}-grid" class="grid"></div>
	
	   <div id="${el}-selectTypeMessage" class="hidden select-type-message">${msg("message.select-type")}</div>
	
	   <div id="${el}-bulk-editBarBottom" class="yui-ge bulk-edit-bar bulk-edit-bar-bottom flat-button">
	      <div class="yui-u first align-center">
	         <div class="item-select">&nbsp;</div>
	         <div id="${el}-paginatorBottom" class="paginator"></div>
	      </div>
	   </div>
</div>
