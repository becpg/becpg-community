<#--
   Green-Score badge drawn from the historical eco score detail (bcpg:ecoScoreDetails), which the
   formulation keeps writing whether or not the score definitions are imported (#37232). The detail
   is mapped onto the normalized score format so the badge is the one of the score list: the
   official artwork when the GREENSCORE badges are imported, the shipped drawing otherwise.
-->
<#assign fieldValue = (field.value)!"">
<#assign controlId = fieldHtmlId + "-greenScore">
<div class="form-field">
   <div class="viewmode-field">
      <span class="viewmode-label">${field.label?html}:</span>
      <#-- Only attempt the eval on a non empty value: "?eval" on an empty string throws, and
           FreeMarker logs the failure of an #attempt block even when #recover handles it. -->
      <#assign ecoScore = "">
      <#if fieldValue?trim?has_content>
         <#attempt>
            <#assign ecoScore = fieldValue?eval>
         <#recover>
            <#assign ecoScore = "">
         </#attempt>
      </#if>
      <#if ecoScore?is_hash && ((ecoScore.scoreClass)!"")?has_content>
         <span class="viewmode-value" id="${controlId}"></span>
         <script type="text/javascript">//<![CDATA[
         (function() {
            var container = document.getElementById("${controlId}");
            if (!container || !beCPG.util.score) {
               return;
            }

            var details = {
               code: "GREENSCORE",
               scale: "Letter",
               "class": "${ecoScore.scoreClass?js_string}",
               value: <#if (ecoScore.ecoScore)?? && ecoScore.ecoScore?is_number>${ecoScore.ecoScore?c}<#else>null</#if>
            };

            var scope = {
               msg: function(key) {
                  return Alfresco.util.message(key) || key;
               }
            };

            container.innerHTML = beCPG.util.score.renderBadge(details, scope, null);
         })();
         //]]></script>
      </#if>
   </div>
</div>
