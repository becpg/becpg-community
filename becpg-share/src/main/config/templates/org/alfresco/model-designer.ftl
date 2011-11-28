<#include "include/alfresco-template.ftl" />
<@templateHeader>
   <script type="text/javascript">//<![CDATA[
      (new Alfresco.widget.Resizer("modelTree")).DEFAULT_FILTER_PANEL_WIDTH=190;
   //]]></script>
</@>

<@templateBody>
    <div id="alf-hd">
      <@region id="header" scope="global" protected=true />
      <@region id="title" scope="page" protected=true />
   </div>
   <div id="bd">
        <div class="yui-t1" id="becpg-model-designer">
           <div id="yui-main">
              <div class="yui-b" id="alf-content">
                <@region id="toolbar" scope="template" protected=true />         
                <@region id="modelForm" scope="template" protected=true />
              </div>
           </div>
           <div class="yui-b" id="alf-filters">
               <@region id="modelTree" scope="template" protected=true />
           </div>
        </div>
   </div>
</@>

<@templateFooter>
   <div id="alf-ft">
      <@region id="footer" scope="global" protected=true />
   </div>
</@>
