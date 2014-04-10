<#include "include/alfresco-template.ftl" />
<@templateHeader>
   <script type="text/javascript">//<![CDATA[
      new Alfresco.widget.Resizer("ncList");
   //]]></script>
    <@script type="text/javascript" src="${url.context}/res/modules/data-lists/datalist-actions.js"  />
</@>

<@templateBody>
   <div id="alf-hd">
	   <@region scope="global" id="share-header" chromeless="true"/>
   </div>
   <div id="bd">
      <div class="yui-t1" id="alfresco-nc-list">
         <div id="yui-main">
            <div class="yui-b" id="alf-content">
               <@region id="toolbar" scope="template" />
               <@region id="list" scope="template" />
            </div>
         </div>
         <div class="yui-b" id="alf-filters">
            <@region id="all-filter" scope="template" />
            <@region id="state-filter" scope="template" />
            <#-- <@region id="due-filter" scope="template" /> -->
            <@region id="started-filter" scope="template" />
            <@region id="assoc-filter" scope="template" />
            <#-- <@region id="priority-filter" scope="template" /> -->
            <@region id="tags" scope="template" />
         </div>
      </div>
   </div>
</@>

<@templateFooter>
   <div id="alf-ft">
      <@region id="footer" scope="global" />
   </div>
</@>