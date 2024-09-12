<#include "include/alfresco-template.ftl" />
<@templateHeader>
   <script type="text/javascript">//<![CDATA[
      new Alfresco.widget.Resizer("productList");
   //]]></script>
    <@script type="text/javascript" src="${url.context}/res/modules/data-lists/datalist-actions.js" />
</@>

<@templateBody>
   <div id="alf-hd">
	  <@region scope="global" id="share-header" chromeless="true"/>
   </div>
   <div id="bd">
         <div id="yui-main">
            <div  class="yui-b" id="alf-content">
               <@region id="toolbar" scope="template" />
               <@region id="list" scope="template" />
               <@region id="archive-and-download" scope="template"/>
            </div>
         </div>
   </div>
</@>

<@templateFooter>
   <div id="alf-ft">
      <@region id="footer" scope="global" />
   </div>
</@>