<#assign el=args.htmlid?js_string>
<div id="${el}-body" class="npd-list-toolbar toolbar">
   <div id="${el}-headerBar" class="header-bar flat-button theme-bg-2">
      <div class="left">
         <div class="hideable hidden">
            <div class="start-npd"><button id="${el}-startNpd-button" name="startNpd">${msg("button.startNpd")}</button></div>
         </div>
      </div>
      <div class="right">
      </div>
   </div>
</div>
<script type="text/javascript">//<![CDATA[
   new beCPG.component.NpdListToolbar("${el}").setMessages(
      ${messages}
   );
//]]></script>