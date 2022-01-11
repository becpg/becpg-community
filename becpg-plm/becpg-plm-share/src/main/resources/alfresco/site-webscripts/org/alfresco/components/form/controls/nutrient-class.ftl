<#assign fieldValue = field.value>
<div class="form-field">
   <div class="viewmode-field">
      <span class="viewmode-label">${field.label?html}:</span
      <#if fieldValue != "" && fieldValue?eval??>
		<#assign nutrientScore = fieldValue?eval>
	      <span class="viewmode-value nutrient-class">
	      	<#if nutrientScore.class != "" 
	      	   && (
	      	       nutrientScore.class == "A"
	      	       || nutrientScore.class == "B"
	      	       || nutrientScore.class == "C"
	      	       || nutrientScore.class == "D"
	      	       || nutrientScore.class == "E")>
		         <span <#if nutrientScore.prettyScore??>title="${nutrientScore.prettyScore?html}"</#if> class="<#if nutrientScore.class == "A">selected </#if>nutrient-class-a">A</span>
		         <span <#if nutrientScore.prettyScore??>title="${nutrientScore.prettyScore?html}"</#if> class="<#if nutrientScore.class == "B">selected </#if>nutrient-class-b">B</span>
		         <span <#if nutrientScore.prettyScore??>title="${nutrientScore.prettyScore?html}"</#if> class="<#if nutrientScore.class == "C">selected </#if>nutrient-class-c">C</span>
		         <span <#if nutrientScore.prettyScore??>title="${nutrientScore.prettyScore?html}"</#if> class="<#if nutrientScore.class == "D">selected </#if>nutrient-class-d">D</span>
		         <span <#if nutrientScore.prettyScore??>title="${nutrientScore.prettyScore?html}"</#if> class="<#if nutrientScore.class == "E">selected </#if>nutrient-class-e">E</span>
		    <#elseif nutrientScore.class != "">
		          <span class="nutrient-class-error" >${nutrientScore.class?html}</span>
	        </#if>
	      </span>
       </#if>
   </div>
</div>