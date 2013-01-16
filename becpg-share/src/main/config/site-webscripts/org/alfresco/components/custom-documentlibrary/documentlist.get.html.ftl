
<@markup id="customDocumentlist-css" target="css"  action="after">
   <#-- CSS Dependencies -->
  <@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/documentlibrary/custom-documentlist.css" group="documentlibrary"/>
</@>

<@markup id="customDocumentlist-js" target="js" action="after">
   <#-- JavaScript Dependencies -->
   <@script type="text/javascript" src="${page.url.context}/res/components/documentlibrary/becpg/fileIcons.js"  group="documentlibrary" />
	<@script type="text/javascript" src="${page.url.context}/res/components/documentlibrary/custom-documentlist.js"  group="documentlibrary" />
	
</@>
template_x002e_documentlist_x002e_documentlibrary_x0023_default-becpg-entityFolder-instructions

<@markup id="entityFolderInstructions" target="documentListContainer" action="before">
 	   <#assign el = args.htmlid?html>
	   <div id="${el}-becpg-entityFolder-instructions" class="hidden entityFolderInstructions"></div>
 </@>
