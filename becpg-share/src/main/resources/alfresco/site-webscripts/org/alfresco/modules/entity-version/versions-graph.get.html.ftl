
<#assign el=args.htmlid?html>
<div id="${el}-dialog" class="versions-graph">
   <div class="hd">
      <span id="${el}-header-span">${msg("header.version-graph")}</span>
   </div>
   <div class="bd">
      <div id="versions-graph-filter" class="versions-graph-filter">
         <div class="filter-item">
            <input id="versions-graph-filter-name" type="text" placeholder="${msg("label.filter-by-name")}" />
         </div>
         <div class="filter-item">
            <select id="versions-graph-filter-system-state">
               <option value="">${msg("label.all-states")}</option>
            </select>
         </div>
         <div id="versions-graph-simpleDetailed" class="simple-detailed yui-buttongroup inline">
            <span id="versions-graph-filter-view-simple" class="yui-button yui-radio-button simple-view">
               <span class="first-child">
                  <button type="button" tabindex="0" title="${msg("button.view.simple")}"></button>
               </span>
            </span>
            <span id="versions-graph-filter-view-full" class="yui-button yui-radio-button detailed-view yui-button-checked yui-radio-button-checked">
               <span class="first-child">
                  <button type="button" tabindex="0" title="${msg("button.view.detailed")}"></button>
               </span>
            </span>
         </div>
      </div>
      <div id="${el}-versionsGraph"> 
         <div id="${el}-graphNodes" class="graph-nodes">
            <canvas id="${el}-graphCanvas" height="2000" width="100"></canvas>
         </div>
         <div id="${el}-graphContent" class="graph-content datagrid"></div>
      </div>
   </div>
</div>

<script type="text/javascript">//<![CDATA[
Alfresco.util.addMessages(${messages}, "beCPG.module.VersionsGraph");
beCPG.module.VersionsGraph.CONFIG = {
   types: [
   <#list types as type>
      { name: "${type.name}", states: "${type.states}" }<#if type_has_next>,</#if>
   </#list>
   ],
   states: [
   <#list states as state>
      { value: "${state.value}", label: "${state.label}" }<#if state_has_next>,</#if>
   </#list>
   ]
};
//]]></script>
