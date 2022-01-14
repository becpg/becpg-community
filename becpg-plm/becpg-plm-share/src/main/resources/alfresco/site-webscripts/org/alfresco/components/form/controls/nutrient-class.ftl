<#assign fieldValue = field.value>
<div class="form-field">
   <div class="viewmode-field">
      <span class="viewmode-label">${field.label?html}:</span
      <#if fieldValue != "" && fieldValue?eval??>
		<#assign nutrientDetails = fieldValue?eval>
	      <span class="viewmode-value nutrient-class">
	      <#assign class = nutrientDetails.value.class.result>
	      <#assign displayValue = nutrientDetails.displayValue>
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