<#assign id = args.htmlid>
<script type="text/javascript">//<![CDATA[
   new beCPG.component.DesignerPalettes("${id}").setMessages(${messages});
//]]></script>
<div id="${id}-body" class="designerPalettes">
	<div class="designerPalettes-toolbar toolbar">
	   <div id="${args.htmlid}-headerBar" class="header-bar flat-button theme-bg-2">
	 		
	   </div>
   </div>
   <div  class="filter  controls-container" >
	   <h2>${msg("header.controls")}</h2>
	   <ul id="${id}-form-controls" class="form-controls"></ul>
   </div>
   
   <div  class="filter sets-container" >
	    <h2>${msg("header.sets")}</h2>
	    <ul id="${id}-form-sets" class="form-sets"></ul>
   </div>
   
</div>
