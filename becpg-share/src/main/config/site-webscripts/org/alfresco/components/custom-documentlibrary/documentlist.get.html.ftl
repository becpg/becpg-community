
<@markup id="customDocumentlist-css" target="css"  action="after">
   <#-- CSS Dependencies -->
  <@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/documentlibrary/custom-documentlist.css" group="documentlibrary"/>
</@>

<@markup id="customDocumentlist-js" target="js" action="after">
   <#-- JavaScript Dependencies -->
   <@script type="text/javascript" src="${page.url.context}/res/components/documentlibrary/becpg/fileIcons.js"  group="documentlibrary" />
	<@script type="text/javascript" src="${page.url.context}/res/components/documentlibrary/custom-documentlist.js"  group="documentlibrary" />
	
</@>

<@markup id="entityFolderInstructions" target="documentListContainer" action="before">
 	   <#assign el = args.htmlid?html>
	   <div id="${el}-becpg-entityFolder-instructions" class="hidden entityFolderInstructions">
	   	<div   id="${el}-becpg-entityFolder-message" class="entityFolderToolbar-message"> 
	   	</div>
	   	<div id="${el}-becpg-entityFolder-buttons" class="entityFolderToolbar-buttons flat-button hidden">
		   	<div class="entity-view-details" >
		   		<a id="${el}-viewEntityDetails-button" name="viewEntityDetails" href="entity-details?nodeRef={nodeRef}" >${msg("actions.entity.view-details")}</a>
		   	</div>
		   	<div class="entity-view-datalist">
		   		<a id="${el}-viewEntityLists-button" name="viewEntityLists" href="entity-data-lists?nodeRef={nodeRef}" >${msg("actions.entity.view-datalist")}</a>
		   	</div>
	   	</div>
	   </div>
 </@>
