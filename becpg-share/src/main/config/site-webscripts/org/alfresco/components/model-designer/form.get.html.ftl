<#assign id = args.htmlid>
<script type="text/javascript">//<![CDATA[
   new beCPG.component.DesignerForm("${id}").setOptions(
   {
      modelNodeRef: "${nodeRef!""}"
   }).setMessages(${messages});
//]]></script>
<div id="${id}-model-form"  >
${msg('model.please-select')}
</div>