<#assign controlId = fieldHtmlId + "-cntrl">
<#if field.control.params.rows??><#assign rows=field.control.params.rows><#else><#assign rows=2></#if>
<#if field.control.params.columns??><#assign columns=field.control.params.columns><#else><#assign columns=60></#if>

<#--
<@markup id="css" >
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/modules/spel-editor/spel-editor.css" group="form" />
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/modules/spel-editor/shThemeDefault.css" group="form" />
</@>

<@markup id="js">
	<@script type="text/javascript" src="${url.context}/res/yui/editor/editor.js" group="form" />
	<@script type="text/javascript" src="${url.context}/res/modules/spel-editor/spel-editor.js" group="form" />
	<@script type="text/javascript" src="${url.context}/res/modules/spel-editor/shCore.js" group="form" />
	<@script type="text/javascript" src="${url.context}/res/modules/spel-editor/shBrushJScript.js" group="form" />
</@>
-->


<@markup id="widgets">
   	<@inlineScript group="form">
		   new beCPG.SpelEditor("${controlId}", "${fieldHtmlId}").setOptions(
		   {
		      <#if form.mode == "view" || (field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true"))>disabled: true,</#if>
		      field: "${field.name}",
		   <#if field.mandatory??>
		      mandatory: ${field.mandatory?string},
		    </#if>
		    <#if args.entityNodeRef??>
		      entityNodeRef : "${args.entityNodeRef}",
		    </#if>
		    <#if args.dataListsName??>
		      currentList : "${args.dataListsName}",
		    </#if>
		      currentValue: "${field.value?js_string}",
		   }).setMessages( ${messages});
		</@>
</@>

<div class="form-field">
   <#if form.mode == "view">
      <div id="${controlId}" class="viewmode-field">
         <#if (field.endpointMandatory!false || field.mandatory!false) && field.value == "">
            <span class="incomplete-warning"><img src="${url.context}/res/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
         </#if>
         <span class="viewmode-label">${field.label?html}:</span>
         <span id="${controlId}-currentValueDisplay" class="viewmode-value current-formula"></span>
      </div>
   <#else>
      <label for="${controlId}">${field.label?html}:<#if field.endpointMandatory!false || field.mandatory!false><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
      
      <div id="${controlId}" class="spel-editor-control">
         
         <div id="${controlId}-currentValueDisplay" class="current-formula"></div>
         <input type="hidden"  name="${field.name}" id="${fieldHtmlId}" value="${field.value?html}" />
         
         <#if field.disabled == false>
            <div id="${controlId}-showEditorAction" class="show-editor"></div>
         
               <#assign editorId = controlId + "-editor">
					<div id="${editorId}" class="editor yui-panel" style="visibility:hidden;">
					   <div id="${editorId}-head" class="hd">${msg("form.control.spel-editor.header")}</div>
					
					   <div id="${editorId}-body" class="bd">
					      <div class="editor-textarea yui-b">
					      	 <textarea id="${editorId}-textarea" rows="${rows}" name="-"> </textarea>
					      </div>
					      <div class="editor-header">
					          <div id="${editorId}-itemTypeContainer" class="navigator">	
					            <button id="${editorId}-itemType"></button>
					            <div id="${editorId}-itemTypeMenu" class="yuimenu">
					               <div class="bd">
					                  <ul id="${editorId}-itemTypeItems" class="itemType-items-list"></ul>
					               </div>
					            </div>
					         </div>
					         <div id="${editorId}-searchContainer" class="search">
					            <input type="text" class="search-input" name="-" id="${editorId}-searchText" value="" maxlength="256" />
					            <span class="search-button"><button id="${editorId}-searchButton">${msg("form.control.spel-editor.search")}</button></span>
					         </div>
					      </div>
					      <div class="yui-g">
					         <div id="${editorId}-left" class="yui-u first panel-left">
					            <div id="${editorId}-itemList1" class="editor-items">
					            </div>
					         </div>
					         <div id="${editorId}-right" class="yui-u panel-right">
					            <div id="${editorId}-itemList2" class="editor-items"></div>
					         </div>
					      </div>
					      <div class="bdft">
					         <button id="${controlId}-ok" tabindex="0">${msg("button.ok")}</button>
					         <button id="${controlId}-cancel" tabindex="0">${msg("button.cancel")}</button>
					      </div>
					   </div>
					</div>
         </#if>
      </div>
   </#if>
</div>
