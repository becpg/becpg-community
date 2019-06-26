
<#assign el=args.htmlid?html>
<div id="${el}-dialog" class="versions-graph">
   <div class="hd">
      <span id="${el}-header-span">${msg("header.version-graph")}</span>
   </div>
   <div class="bd">
      <div id="${el}-versionsGraph"> 
	       <div id="${el}-graphNodes"  class="graph-nodes">
				<canvas id="${el}-graphCanvas" height="2000" width="100"></canvas>
			</div>
			<div id="${el}-graphContent" class="graph-content datagrid"></div>
		</div>
   </div>
</div>

<script type="text/javascript">//<![CDATA[
Alfresco.util.addMessages(${messages}, "beCPG.module.VersionsGraph");
//]]></script>
