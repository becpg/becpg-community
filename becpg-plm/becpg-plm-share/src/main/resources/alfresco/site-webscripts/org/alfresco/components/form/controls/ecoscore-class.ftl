<#assign fieldValue = field.value>
<div class="form-field">
   <div class="viewmode-field">
      <span class="viewmode-label">${field.label?html}:</span>
      <#if fieldValue != "" && fieldValue?eval??>
		<#assign ecoScore = fieldValue?eval>
	    <#assign displayValue = msg("ecoscore.score", ecoScore.ecoScore)
								+ "\n"
								+ msg("ecoscore.class", ecoScore.scoreClass)
								+ "\n"
								+ msg("ecoscore.acvScore", ecoScore.acvScore)
								+ "\n"
								+ msg("ecoscore.claimBonus", ecoScore.claimBonus)
								+ "\n"
								+ msg("ecoscore.transportScore", ecoScore.transportScore)
								+ "\n"
								+ msg("ecoscore.politicalScore", ecoScore.politicalScore)
								+ "\n"
								+ msg("ecoscore.packagingMalus", ecoScore.packagingMalus)
								+ "\n"
	    >

        <span <#if ecoScore??>title="${displayValue?html}"</#if> class="viewmode-value ecoscore-class">
	      	<#if ecoScore.scoreClass != "" 
	      	   && (
	      	       ecoScore.scoreClass == "A"
	      	       || ecoScore.scoreClass == "B"
	      	       || ecoScore.scoreClass == "C"
	      	       || ecoScore.scoreClass == "D"
	      	       || ecoScore.scoreClass == "E")>
		         <#if ecoScore.scoreClass == "A"><span class="ecoscore-class-a"></span> </#if>
		         <#if ecoScore.scoreClass == "B"><span class="ecoscore-class-b"></span> </#if>
		         <#if ecoScore.scoreClass == "C"><span class="ecoscore-class-c"></span> </#if>
		         <#if ecoScore.scoreClass == "D"><span class="ecoscore-class-d"></span> </#if>
		         <#if ecoScore.scoreClass == "E"><span class="ecoscore-class-e"></span> </#if>
		    <#elseif ecoScore.scoreClass != "">
		          <span class="ecoscore-class-error" >${ecoScore.scoreClass?html}</span>
	        </#if>
     	 </span>
       </#if>
   </div>
</div>