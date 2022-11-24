<#assign fieldValue = field.value>
<div class="form-field">
   <div class="viewmode-field">
      <span class="viewmode-label">${field.label?html}:</span>
      <#if fieldValue != "" && fieldValue?eval??>
		<#assign nutrientDetails = fieldValue?eval>
	      <span class="viewmode-value nutrient-class">
	      <#assign class = nutrientDetails.nutrientClass>
	      
	      <#if nutrientDetails.category == "Fats">
	      	<#assign totalFat = msg("nutriscore.display.totalfat", nutrientDetails.parts['FAT'].lowerValue, nutrientDetails.parts['FAT'].value, nutrientDetails.parts['FAT'].upperValue, nutrientDetails.parts['FAT'].score)
	      						+ "\n">
	        <#assign satFat = "">
	      <#else>
	        <#assign totalFat = "">
	        <#assign satFat = msg("nutriscore.display.satfat", nutrientDetails.parts['FASAT'].lowerValue, nutrientDetails.parts['FASAT'].value, nutrientDetails.parts['FASAT'].upperValue, nutrientDetails.parts['FASAT'].score)
	      						+ "\n">
	      </#if>
	      
	      <#assign protein = "">
	      
	      <#if nutrientDetails.hasProteinScore == true>
		      <#assign protein = msg("nutriscore.display.protein", nutrientDetails.parts['PRO-'].lowerValue, nutrientDetails.parts['PRO-'].value, nutrientDetails.parts['PRO-'].upperValue, nutrientDetails.parts['PRO-'].score)
		      						+ "\n">
	      </#if>
	      
	      <#assign displayValue = msg("nutriscore.display.negative") 
	      						+ "\n"
	      						+ msg("nutriscore.display.energy", nutrientDetails.parts['ENER-KJO'].lowerValue, nutrientDetails.parts['ENER-KJO'].value, nutrientDetails.parts['ENER-KJO'].upperValue, nutrientDetails.parts['ENER-KJO'].score)
	      						+ "\n"
	      						+ satFat
	      						+ totalFat
	      						+ msg("nutriscore.display.totalsugar", nutrientDetails.parts['SUGAR'].lowerValue, nutrientDetails.parts['SUGAR'].value, nutrientDetails.parts['SUGAR'].upperValue, nutrientDetails.parts['SUGAR'].score)
	      						+ "\n"
	      						+ msg("nutriscore.display.sodium", nutrientDetails.parts['NA'].lowerValue, nutrientDetails.parts['NA'].value, nutrientDetails.parts['NA'].upperValue, nutrientDetails.parts['NA'].score)
	      						+ "\n"
	      						+ "\n"
	      						+ msg("nutriscore.display.positive")
	      						+ "\n"
	      						+ protein
	      						+ msg("nutriscore.display.percfruitsandveg", nutrientDetails.parts['FRUIT_VEGETABLE'].lowerValue, nutrientDetails.parts['FRUIT_VEGETABLE'].value, nutrientDetails.parts['FRUIT_VEGETABLE'].upperValue, nutrientDetails.parts['FRUIT_VEGETABLE'].score)
	      						+ "\n"
	      						+ msg("nutriscore.display.nspfibre", nutrientDetails.parts['PSACNS'].lowerValue, nutrientDetails.parts['PSACNS'].value, nutrientDetails.parts['PSACNS'].upperValue, nutrientDetails.parts['PSACNS'].score)
	      						+ "\n"
	      						+ msg("nutriscore.display.aoacfibre", nutrientDetails.parts['FIBTG'].lowerValue, nutrientDetails.parts['FIBTG'].value, nutrientDetails.parts['FIBTG'].upperValue, nutrientDetails.parts['FIBTG'].score)
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