<#--
   Generic score control: renders the badge and the breakdown of a computed score from its
   normalized detail, whatever produced it. The scale carried by the detail drives the
   rendering, so a new score needs no template of its own.
-->
<#assign fieldValue = (field.value)!"">
<#assign controlId = fieldHtmlId + "-scoreBadge">
<div class="form-field">
   <div class="viewmode-field">
      <span class="viewmode-label">${field.label?html}:</span>
      <#-- Only attempt the eval on a non empty value: "?eval" on an empty string throws, and
           FreeMarker logs the failure of an #attempt block even when #recover handles it. -->
      <#assign isValid = false>
      <#if fieldValue?trim?has_content>
         <#attempt>
            <#assign parsed = fieldValue?eval>
            <#assign isValid = true>
         <#recover>
            <#assign isValid = false>
         </#attempt>
      </#if>
      <#if isValid>
         <span class="viewmode-value" id="${controlId}"></span>
         <script type="text/javascript">//<![CDATA[
         (function() {
            var container = document.getElementById("${controlId}");
            if (!container || !beCPG.util.score) {
               return;
            }

            var details = beCPG.util.score.parseDetails(${fieldValue});
            if (!details) {
               return;
            }

            var scope = {
               msg: function(key) {
                  return Alfresco.util.message(key) || key;
               }
            };

            container.innerHTML = beCPG.util.score.renderBadge(details)
               + beCPG.util.score.renderDetails(scope, details);
         })();
         //]]></script>
      </#if>
   </div>
</div>
