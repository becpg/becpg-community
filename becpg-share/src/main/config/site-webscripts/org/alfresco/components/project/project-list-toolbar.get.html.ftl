<#assign el=args.htmlid?js_string>
<#if page.url.args.view??>
		<#assign view=page.url.args.view?js_string>
<#else>
	<#assign view="dataTable">
</#if>
<div id="${el}-body" class="project-list-toolbar datalist-toolbar toolbar">
   <div id="${el}-headerBar" class="header-bar flat-button theme-bg-2">
      <div class="left">
         <div id="toolbar-contribs" class="hidden" ></div>
      </div>
      <div class="right">
         <div class="show-planning">
            <span id="${el}-show-planning-button" class="yui-button yui-checkbox-button">
               <span class="first-child">
                  <button name="showPlanning"></button>
               </span>
            </span>
         </div>
         <div class="separator">&nbsp;</div>
         <div class="show-gantt">
            <span id="${el}-show-gantt-button" class="yui-button yui-checkbox-button">
               <span class="first-child">
                  <button name="showGantt"></button>
               </span>
            </span>
         </div>
         <div class="separator">&nbsp;</div>
          <div class="export-csv">
            <span id="${el}-export-csv-button" class="yui-button yui-checkbox-button">
               <span class="first-child">
                  <button name="exportCsv"></button>
               </span>
            </span>
         </div>
      </div>
   </div>
</div>
<script type="text/javascript">//<![CDATA[
   new beCPG.component.ProjectListToolbar("${el}","${view}").setMessages(
      ${messages}
   );
//]]></script>