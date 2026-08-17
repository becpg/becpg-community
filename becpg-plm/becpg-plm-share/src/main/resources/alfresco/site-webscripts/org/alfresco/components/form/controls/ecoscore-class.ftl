<#assign fieldValue = (field.value)!"">
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
      <#if ecoScore?is_hash>
         <#assign scoreClass = (ecoScore.scoreClass)!"">
         <#assign displayValue = msg("ecoscore.score", (ecoScore.ecoScore)!"")
                              + "\n"
                              + msg("ecoscore.class", scoreClass)
                              + "\n"
                              + msg("ecoscore.acvScore", (ecoScore.acvScore)!"")
                              + "\n"
                              + msg("ecoscore.claimBonus", (ecoScore.claimBonus)!"")
                              + "\n"
                              + msg("ecoscore.transportScore", (ecoScore.transportScore)!"")
                              + "\n"
                              + msg("ecoscore.politicalScore", (ecoScore.politicalScore)!"")
                              + "\n"
                              + msg("ecoscore.packagingMalus", (ecoScore.packagingMalus)!"")
                              + "\n"
         >

         <span title="${displayValue?html}" class="viewmode-value ecoscore-class">
            <#if scoreClass == "A"><span class="ecoscore-class-a"></span>
            <#elseif scoreClass == "B"><span class="ecoscore-class-b"></span>
            <#elseif scoreClass == "C"><span class="ecoscore-class-c"></span>
            <#elseif scoreClass == "D"><span class="ecoscore-class-d"></span>
            <#elseif scoreClass == "E"><span class="ecoscore-class-e"></span>
            <#elseif scoreClass != "">
               <span class="ecoscore-class-error">${scoreClass?html}</span>
            </#if>
         </span>
      </#if>
   </div>
</div>
