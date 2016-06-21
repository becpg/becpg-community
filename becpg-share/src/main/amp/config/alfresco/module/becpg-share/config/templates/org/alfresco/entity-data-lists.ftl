<#include "include/alfresco-template.ftl" />
<@templateHeader>
   <@markup id="resizer">
    <@inlineScript group="entity-datalists">
      new Alfresco.widget.Resizer("DataLists").setOptions({initialWidth:200});
    </@inlineScript>
   </@>
   <@script type="text/javascript" src="${url.context}/res/modules/data-lists/datalist-actions.js" group="entity-datalists"/>
</@>

<@templateBody>
   <@markup id="alf-hd">
   <div id="alf-hd">
      <@region scope="global" id="share-header" chromeless="true"/>
      <@region id="actions-common" scope="template"/>
      <@region id="actions" scope="template"/>
      <@region id="node-header" scope="template"/>
   </div>
   </@>
   <@markup id="bd">
   <div id="bd" class="entity-data-lists">
        <div class="yui-t1" id="alfresco-data-lists">
           <div id="yui-main">
              <div class="yui-b" id="alf-content">
                <@region id="toolbar" scope="template"  />               
                <@region id="datagrid" scope="template" />
              </div>
           </div>
           <div class="yui-b" id="alf-filters">
               <@region id="datalists" scope="template"  />
               <@region id="document-versions" scope="template"/>
           </div>
        </div>
	    <@region id="html-upload" scope="template"/>
		<@region id="flash-upload" scope="template"/>
		<@region id="file-upload" scope="template"/>
		<@region id="dnd-upload" scope="template"/>
		<@region id="archive-and-download" scope="template"/>
   </div>
        <@region id="doclib-custom" scope="template"/>
   </@>
</@>

<@templateFooter>
   <@markup id="alf-ft">
   		<div id="alf-ft">
     	 <@region id="footer" scope="global" />
   		</div>
	</@>
</@>
