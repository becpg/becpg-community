<#assign id = args.htmlid>
<#include "include/form.lib.ftl" />
<@formLibTemplate/>
<script type="text/javascript">//<![CDATA[
   new beCPG.component.DesignerForm("${id}").setMessages(${messages});
//]]></script>
<div id="${id}-dnd-instructions"></div>
<div class="designer-form" id="${id}-model-form"  >
${msg('model.please-select')}
</div>
