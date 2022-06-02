<#assign fieldValue = field.value>
<div class="form-field">
   <div class="viewmode-field">
      <span class="viewmode-label">${field.label?html}:</span
      <#if fieldValue != "" && fieldValue?eval??>
		<#assign nutrientDetails = fieldValue?eval>
	      <span class="viewmode-value nutrient-class">
	      <#assign class = nutrientDetails.nutrientClass>
	      
	      <#if nutrientDetails.category == "Fats">
	      	<#assign totalFat = msg("nutriscore.display.totalfat", nutrientDetails.totalFat.lowerValue, nutrientDetails.totalFat.value, nutrientDetails.totalFat.upperValue, nutrientDetails.totalFat.score)
	      						+ "\n">
	        <#assign satFat = "">
	      <#else>
	        <#assign totalFat = "">
	        <#assign satFat = msg("nutriscore.display.satfat", nutrientDetails.satFat.lowerValue, nutrientDetails.satFat.value, nutrientDetails.satFat.upperValue, nutrientDetails.satFat.score)
	      						+ "\n">
	      </#if>
	      
	      <#assign displayValue = msg("nutriscore.display.negative") 
	      						+ "\n"
	      						+ msg("nutriscore.display.energy", nutrientDetails.energy.lowerValue, nutrientDetails.energy.value, nutrientDetails.energy.upperValue, nutrientDetails.energy.score)
	      						+ "\n"
	      						+ satFat
	      						+ totalFat
	      						+ msg("nutriscore.display.totalsugar", nutrientDetails.totalSugar.lowerValue, nutrientDetails.totalSugar.value, nutrientDetails.totalSugar.upperValue, nutrientDetails.totalSugar.score)
	      						+ "\n"
	      						+ msg("nutriscore.display.sodium", nutrientDetails.sodium.lowerValue, nutrientDetails.sodium.value, nutrientDetails.sodium.upperValue, nutrientDetails.sodium.score)
	      						+ "\n"
	      						+ "\n"
	      						+ msg("nutriscore.display.positive")
	      						+ "\n"
	      						+ msg("nutriscore.display.protein", nutrientDetails.protein.lowerValue, nutrientDetails.protein.value, nutrientDetails.protein.upperValue, nutrientDetails.protein.score)
	      						+ "\n"
	      						+ msg("nutriscore.display.percfruitsandveg", nutrientDetails.percFruitsAndVetgs.lowerValue, nutrientDetails.percFruitsAndVetgs.value, nutrientDetails.percFruitsAndVetgs.upperValue, nutrientDetails.percFruitsAndVetgs.score)
	      						+ "\n"
	      						+ msg("nutriscore.display.nspfibre", nutrientDetails.nspFibre.lowerValue, nutrientDetails.nspFibre.value, nutrientDetails.nspFibre.upperValue, nutrientDetails.nspFibre.score)
	      						+ "\n"
	      						+ msg("nutriscore.display.aoacfibre", nutrientDetails.aoacFibre.lowerValue, nutrientDetails.aoacFibre.value, nutrientDetails.aoacFibre.upperValue, nutrientDetails.aoacFibre.score)
	      						+ "\n"
	      						+ "\n"
	      						+ msg("nutriscore.display.finalScore", nutrientDetails.aScore, nutrientDetails.cScore, nutrientDetails.nutriScore)
	      						+ "\n"
	      						+ "\n"
	      						+ msg("nutriscore.display.class", nutrientDetails.classLowerValue, nutrientDetails.nutriScore, nutrientDetails.classUpperValue, nutrientDetails.nutrientClass)
	      >
	      	<#if class != "" 
	      	   && (
	      	       class == "A"
	      	       || class == "B"
	      	       || class == "C"
	      	       || class == "D"
	      	       || class == "E")>
		         <span <#if displayValue??>title="${displayValue?html}"</#if> class="<#if class == "A">selected </#if>nutrient-class-a">A</span>
		         <span <#if displayValue??>title="${displayValue?html}"</#if> class="<#if class == "B">selected </#if>nutrient-class-b">B</span>
		         <span <#if displayValue??>title="${displayValue?html}"</#if> class="<#if class == "C">selected </#if>nutrient-class-c">C</span>
		         <span <#if displayValue??>title="${displayValue?html}"</#if> class="<#if class == "D">selected </#if>nutrient-class-d">D</span>
		         <span <#if displayValue??>title="${displayValue?html}"</#if> class="<#if class == "E">selected </#if>nutrient-class-e">E</span>
		    <#elseif class != "">
		          <span class="nutrient-class-error" >${class?html}</span>
	        </#if>
	      </span>
       </#if>
   </div>
</div>