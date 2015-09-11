<@markup id="css" >
   <#-- CSS Dependencies -->
   <#include "../form/form.css.ftl"/>
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/model-designer/form.css" group="model-designer" />
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <#include "../form/form.js.ftl"/>
   <@script type="text/javascript" src="${url.context}/res/modules/simple-dialog.js" group="model-designer"></@script>
	<@script type="text/javascript" src="${url.context}/res/components/model-designer/form.js" group="model-designer"></@script>
</@>


<@markup id="html">
   <@uniqueIdDiv>
  		<#include "include/form.lib.ftl" />
   	<#assign el=args.htmlid?html>
		<@formLibTemplate/>
		<script type="text/javascript">//<![CDATA[
		(function() {
			new beCPG.component.DesignerForm("${el}").setMessages(${messages});  
		})();
		//]]></script>
		<div id="${el}-dnd-instructions"></div>
		<div class="designer-form" id="${el}-model-form"  >
				${msg('model.please-select')}
		</div>
		</@>
</@>