<@markup id="css" >
	<#include "../../modules/entity-datagrid/include/entity-datagrid.css.ftl"/>
	
   <#-- CSS Dependencies -->
   <@link href="${url.context}/res/components/wizard/wizard-mgr.css" group="wizard"/>
</@>

<@markup id="js">
 	<@script type="text/javascript" src="${url.context}/res/modules/data-lists/datalist-actions.js"></@script>
	<#include "../../modules/entity-datagrid/include/entity-datagrid.js.ftl"/>
	<@script type="text/javascript" src="${url.context}/res/components/entity-charact-views/dashlet-resizer.js" />

   <#-- JavaScript Dependencies -->
   <@script src="${url.context}/res/components/wizard/jquery.js" group="wizard"/>
   <@script src="${url.context}/res/components/wizard/jquery-steps.js" group="wizard"/>
   <@script src="${url.context}/res/components/wizard/wizard-mgr.js" group="wizard"/>
</@>

<@markup id="widgets">
   <@inlineScript group="wizard">
		    Alfresco.constants.DASHLET_RESIZE = true && YAHOO.env.ua.mobile === null;
   </@>
   <@createWidgets group="wizard"/>
</@>

<@markup id="html">
   <@uniqueIdDiv>
	   	<#assign el=args.htmlid?html>
	    <div class="wizard-mgr">
	          <h1 id="${el}-wizardTitle" class="hidden"></h1>
	          <div id="${el}-wizard"></div>
	     </div>
   </@>
</@>
