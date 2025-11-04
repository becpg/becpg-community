
<#assign el=args.htmlid?html>
<div id="${el}-dialog" class="versions-graph">
   <div class="hd">
      <span id="${el}-header-span">${msg("header.version-graph")}</span>
   </div>
   <div class="bd">
   		<div id="versions-graph-filter" class="yui-gc form-row versions-graph-filter">
	   		<div class="yui-g first" style="width: 23%">
		   		<div class="align-left yui-button yui-menu-button">
		   			<input id="versions-graph-filter-name" />
		   		</div>
		   	</div>
		   	<div class="yui-g" style="width: 26%">
		   		<div class="align-left yui-button yui-menu-button">
		   			<select id="versions-graph-filter-system-state">
		   				<option/>
		   			<#list systemStates![] as systemState>
		   				<option value="${systemState}">${msg("listconstraint.bcpg_systemState." + systemState)}</option>
		   			</#list>	
		   			</select>
		   		</div>
		   	</div>
		   	<div class="yui-g">
		   		<div class="form-field-boolean">
	   				<input id="versions-graph-filter-view-simple" name="versions-graph-filter-view" type="radio" />
	   				<label for="versions-graph-filter-view-simple">${msg("label.versions-graph-filter-view-simple")}</label>
	   				<input id="versions-graph-filter-view-full" name="versions-graph-filter-view" type="radio" />
	   				<label for="versions-graph-filter-view-full">${msg("label.versions-graph-filter-view-full")}</label>
		   		</div>
		   	</div>
   		</div>
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
