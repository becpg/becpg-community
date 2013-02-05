


<@markup id="customToolbar-css" target="css"  action="after">
   <#-- CSS Dependencies -->
  <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/documentlibrary/custom-toolbar.css" group="documentlibrary"/>
</@>

<@markup id="customToolbar-js" target="js" action="after">
   <#-- JavaScript Dependencies -->
	<@script type="text/javascript" src="${url.context}/res/components/documentlibrary/custom-toolbar.js"  group="documentlibrary" />
</@>



<@markup id="customToolbar-buttons" target="createContent" action="after">
	<#assign el = args.htmlid?html>
	<div class="hideable toolbar-hidden DocListTree">
       <div class="bulk-edit">
         	<a id="${el}-bulkEdit-button" name="bulkEdit" href="${url.context}/page/bulk-edit?nodeRef={nodeRef}" >${msg("button.bulkEdit")}</a>
      </div>
      <#-- <div class="separator">&nbsp;</div> -->
	</div>
</@>