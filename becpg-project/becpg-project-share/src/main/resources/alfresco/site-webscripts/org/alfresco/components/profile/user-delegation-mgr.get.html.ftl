<#include "../../include/alfresco-macros.lib.ftl" />
<script type="text/javascript">//<![CDATA[
   new Alfresco.component.ShareFormManager("${args.htmlid}").setOptions(
   {
      failureMessage: "user-delegation-mgr.update.failed",
      defaultUrl: "${siteURL("profile")}"
   }).setMessages(${messages});
//]]></script>
<div class="form-manager">
</div>