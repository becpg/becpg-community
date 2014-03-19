

<@markup id="css" >
   <#-- CSS Dependencies -->
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/components/model-designer/toolbar.css"  group="model-designer"/>
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <@script type="text/javascript" src="${url.context}/res/components/model-designer/toolbar.js" group="model-designer"/>
	<@script type="text/javascript" src="${url.context}/res/modules/model-designer/create-element.js" group="model-designer"/>

</@>

<@markup id="html">
   <@uniqueIdDiv>
   	<#assign el=args.htmlid?html>
		<script type="text/javascript">//<![CDATA[
		   new beCPG.component.DesignerToolbar("${el}").setMessages(${messages});
		//]]></script>
		<div id="${el}-body" class="designer-toolbar toolbar">
		   <div id="${el}-headerBar" class="header-bar flat-button theme-bg-2">
		      <div class="left">
		         <div class="new-row">
		            <span id="${el}-newRowButton" class="yui-button yui-push-button">
		               <span class="first-child">
		                  <button type="button">${msg('button.new-row')}</button>
		               </span>
		            </span>
		         </div>
		          <div class="delete-row">
		            <span id="${el}-deleteButton" class="yui-button yui-push-button">
		               <span class="first-child">
		                  <button type="button">${msg('button.delete-row')}</button>
		               </span>
		            </span>
		         </div>
		         <div class="publish">
		            <span id="${el}-publishButton" class="yui-button yui-push-button">
		               <span class="first-child">
		                  <button type="button">${msg('button.publish')}</button>
		               </span>
		            </span>
		         </div>
		         <div class="unpublish">
		            <span id="${el}-unPublishButton" class="yui-button yui-push-button">
		               <span class="first-child">
		                  <button type="button">${msg('button.unpublish')}</button>
		               </span>
		            </span>
		         </div>
		          <div class="preview">
		            <span id="${el}-previewButton" class="yui-button yui-push-button">
		               <span class="first-child">
		                  <button type="button">${msg('button.preview')}</button>
		               </span>
		            </span>
		         </div>
		      </div>
		   </div>
		</div>
 </@>
</@>