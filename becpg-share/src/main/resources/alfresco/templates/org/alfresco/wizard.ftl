<#include "include/alfresco-template.ftl" />
<@templateHeader />

<@templateBody>
   <@markup id="alf-hd">
   <div id="alf-hd">
      <@region scope="global" id="share-header" chromeless="true"/>
       <@region id="actions-common" scope="template"/>
   </div>
   </@>
   <@markup id="bd">
   <div id="bd">
      <div class="share-form">
        <@region id="wizard-mgr" scope="template" />
        <@region id="html-upload" scope="template"/>
		<@region id="flash-upload" scope="template"/>
		<@region id="file-upload" scope="template"/>
		<@region id="dnd-upload" scope="template"/>
		<@region id="archive-and-download" scope="template"/>
      </div>
      <@region id="doclib-custom" scope="template"/>
   </div>
   </@>
</@>

<@templateFooter>
   <@markup id="alf-ft">
   <div id="alf-ft">
      <@region id="footer" scope="global" />
      
   </div>
   </@>
</@>
