<#assign el=args.htmlid?html>

<div id="${el}" class="detailsDialog">
   <div id="${el}-dialogTitle" class="hd">${msg("header.projects")}</div>
   <div class="bd">
		<div id="${el}-documents"></div>
   </div>
</div>   

<script type="text/javascript">//<![CDATA[
 (function () {
	var entityProjects = new beCPG.component.EntityProjects("${el}").setOptions(
	   {
	   nodeRef: "${args.nodeRef}",
        maxItems : 15
	   }).setMessages(${messages});

 })();
//]]></script>