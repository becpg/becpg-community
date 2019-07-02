<#include "../../include/alfresco-macros.lib.ftl" />
<@markup id="js">
  <@script src="${url.context}/res/components/entity-catalog/entity-catalog.js" group="edit-metadata"/>
</@>

<@markup id="css">
  <@link href="${url.context}/res/components/entity-catalog/entity-catalog.css" group="edit-metadata"/>
</@>

<@markup id="widgets">
      <@createWidgets group="edit-metadata"/>	
</@>

<script type="text/javascript">//<![CDATA[
   new Alfresco.component.ShareFormManager("${args.htmlid}").setOptions(
   {
      failureMessage: "edit-metadata-mgr.update.failed",
      defaultUrl: "${siteURL((nodeType!"document") + "-details?nodeRef=" + (nodeRef!page.url.args.nodeRef)?js_string)}"
   }).setMessages(${messages});
//]]></script>

<div class="form-manager">
   <h1>${msg("edit-metadata-mgr.heading", fileName?html)}</h1>
   
   <#if hasScore>
   		<#assign el=args.htmlid?html>	
   		<div id="${el}-entity-catalog" class="hidden" ></div> 
   </#if>
</div>
