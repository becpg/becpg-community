
<@markup  id="customDynamicWelcome-css" target="css" action="replace">
 	
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/components/dashlets/dynamic-welcome.css" group="dashlets" />
	<#include "../form/form.css.ftl"/>
</@>


<@markup  id="customDynamicWelcome-js" target="js" action="replace">
   <@script type="text/javascript" src="${url.context}/res/components/dashlets/dynamic-welcome.js" group="dashlets"/> 
   <#include "../form/form.js.ftl"/>
   <@script type="text/javascript" src="${url.context}/res/modules/simple-dialog.js" group="dashlets"/>
   <@script type="text/javascript" src="${url.context}/res/components/dashlets/custom-dynamic-welcome.js" group="dashlets"/>
</@>
