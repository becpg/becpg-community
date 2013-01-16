
<@markup id="customToolbar-css" target="css"  action="after">
   <#-- CSS Dependencies -->
  <@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/documentlibrary/custom-toolbar.css" group="documentlibrary"/>
</@>

 <#-- BUG HERE Why should I use replace ? -->
<@markup id="customToolbar-js" target="js" action="replace">
   <#-- JavaScript Dependencies -->
   <@script type="text/javascript" src="${url.context}/res/components/documentlibrary/toolbar.js" group="documentlibrary"/>
   <@script type="text/javascript" src="${url.context}/res/components/documentlibrary/repo-toolbar.js" group="documentlibrary"/>
	<@script type="text/javascript" src="${page.url.context}/res/components/documentlibrary/custom-repo-toolbar.js"  group="documentlibrary" />
</@>



<@markup id="customToolbar-buttons" target="createContent" action="after">
	<#assign el = args.htmlid?html>
	<div class="hideable toolbar-hidden DocListTree">
       <div class="bulk-edit">
         	<a id="${el}-bulkEdit-button" name="bulkEdit" href="${page.url.context}/page/bulk-edit?nodeRef={nodeRef}" >${msg("button.bulkEdit")}</a>
      </div>
      <#-- <div class="separator">&nbsp;</div> -->
	</div>
</@>