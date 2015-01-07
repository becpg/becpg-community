


<@markup id="customRepositoryDocListTree-css" target="css"  action="after">
   <#-- CSS Dependencies -->
  <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/documentlibrary/custom-tree.css" group="documentlibrary"/>
</@>

 <#-- TODO bug replace -->
<@markup id="customRepositoryDocListTree-js" target="js" action="replace">
   <#-- JavaScript Dependencies -->
   <@script type="text/javascript" src="${url.context}/res/components/documentlibrary/tree.js" group="documentlibrary"/>
   <@script type="text/javascript" src="${url.context}/res/components/documentlibrary/repo-tree.js" group="documentlibrary"/>
	<@script type="text/javascript" src="${url.context}/res/components/documentlibrary/custom-repo-tree.js"  group="documentlibrary" />
</@>


