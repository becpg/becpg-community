
<@markup id="customToolbar-buttons" target="uploadButton" action="after">
	<#assign el = args.htmlid?html>
	<div class="hideable toolbar-hidden DocListTree">
       <div class="bulk-edit">
         	<a id="${el}-bulkEdit-button" name="bulkEdit" href="${url.context}/page/bulk-edit?nodeRef={nodeRef}" >${msg("button.bulkEdit")}</a>
      </div>
	</div>
</@>
