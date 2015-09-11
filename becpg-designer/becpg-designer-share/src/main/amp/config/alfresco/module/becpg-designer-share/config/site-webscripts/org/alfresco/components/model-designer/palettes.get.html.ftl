
<@markup id="css" >
   <#-- CSS Dependencies -->
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/components/model-designer/palettes.css" group="model-designer" />
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <@script type="text/javascript" src="${url.context}/res/components/model-designer/palettes.js" group="model-designer" ></@script>
</@>

<@markup id="html">
   <@uniqueIdDiv>
   	<#assign el=args.htmlid?html>
		<script type="text/javascript">//<![CDATA[
		   new beCPG.component.DesignerPalettes("${el}").setMessages(${messages});
		//]]></script>
		<div id="${el}-body" class="designerPalettes">
			<div class="designerPalettes-toolbar toolbar">
			   <div id="${el}-headerBar" class="header-bar flat-button theme-bg-2">
			 		
			   </div>
		   </div>
		   <div  class="filter  controls-container" >
			   <h2>${msg("header.controls")}</h2>
			   <ul id="${el}-form-controls" class="form-controls"></ul>
		   </div>
		   
		   <div  class="filter sets-container" >
			    <h2>${msg("header.sets")}</h2>
			    <ul id="${el}-form-sets" class="form-sets"></ul>
		   </div>
		   
		</div>
		</@>
</@>