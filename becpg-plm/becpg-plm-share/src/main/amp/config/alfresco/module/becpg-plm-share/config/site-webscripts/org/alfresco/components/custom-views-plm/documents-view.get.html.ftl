
<@markup id="customToolbar-buttons" target="uploadButton" action="after">
	<#assign el = args.htmlid?html>
	<div class="hideable toolbar-hidden DocListTree">
       <div class="bulk-edit">
         	<span id="${el}-bulkEdit-button" class="yui-button yui-push-button">
               <span class="first-child">
                     <button name="fileUpload">${msg("button.bulkEdit")}</button>
                </span>
            </span>
      </div>
	</div>
</@>