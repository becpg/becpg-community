<#include "include/alfresco-template.ftl" />
<@templateHeader>
   <script type="text/javascript">//<![CDATA[
      new Alfresco.widget.Resizer("npdList");
   //]]></script>
</@>

<@templateBody>
   <div id="alf-hd">
   <@region id="header" scope="global" protected=true/>
   <@region id="task-title" scope="template" protected=true />
   </div>
   <div id="bd">
      <div class="yui-t1" id="alfresco-npd-list">
         <div id="yui-main">
            <div class="yui-b" id="alf-content">
               <@region id="toolbar" scope="template" protected=true />
               <@region id="list" scope="template" protected=true />
            </div>
         </div>
         <div class="yui-b" id="alf-filters">
            <@region id="all-filter" scope="template" protected=true />
            <@region id="due-filter" scope="template" protected=true />
            <@region id="started-filter" scope="template" protected=true />
            <@region id="priority-filter" scope="template" protected=true />
         </div>
      </div>
   </div>
</@>

<@templateFooter>
   <div id="alf-ft">
      <@region id="footer" scope="global" protected=true />
   </div>
</@>