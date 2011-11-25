<#assign id = args.htmlid>
<script type="text/javascript">//<![CDATA[
   new beCPG.component.DesignerToolbar("${id}").setOptions(
   {
      itemType: "${page.url.templateArgs.itemType!""}",
      modelNodeRef: "${nodeRef!""}"
   }).setMessages(${messages});
//]]></script>
<div id="${args.htmlid}-body" class="designer-toolbar toolbar">
   <div id="${args.htmlid}-headerBar" class="header-bar flat-button theme-bg-2">
      <div class="left">
         <div class="new-row">
            <span id="${id}-newRowButton" class="yui-button yui-push-button">
               <span class="first-child">
                  <button type="button">${msg('button.new-row')}</button>
               </span>
            </span>
         </div>
          <div class="delete-row">
            <span id="${id}-deleteButton" class="yui-button yui-push-button">
               <span class="first-child">
                  <button type="button">${msg('button.delete-row')}</button>
               </span>
            </span>
         </div>
         <div class="publish">
            <span id="${id}-publishButton" class="yui-button yui-push-button">
               <span class="first-child">
                  <button type="button">${msg('button.publish')}</button>
               </span>
            </span>
         </div>
          <div class="preview">
            <span id="${id}-previewButton" class="yui-button yui-push-button">
               <span class="first-child">
                  <button type="button">${msg('button.preview')}</button>
               </span>
            </span>
         </div>
      </div>
   </div>
</div>