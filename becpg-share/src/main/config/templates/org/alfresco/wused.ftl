<#include "include/alfresco-template.ftl" />
<@templateHeader>
      <@script type="text/javascript" src="${url.context}/res/modules/data-lists/datalist-actions.js"  />
</@>

<@templateBody>
   <div id="alf-hd">
	   <@region scope="global" id="share-header" chromeless="true"/>
   </div>
   <div id="bd">
      <div class="yui-t1" id="alfresco-wused-list">
         <div id="yui-main">
            <div id="alf-content">
               <@region id="wused-form" scope="template" />
               <@region id="wused-toolbar" scope="template" />
               <@region id="wused-list" scope="template" />
            </div>
         </div>
      </div>
   </div>
</@>

<@templateFooter>
   <div id="alf-ft">
      <@region id="footer" scope="global" />
   </div>
</@>