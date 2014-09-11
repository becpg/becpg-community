<div class="form-field">
   <div class="viewmode-field">
      <span class="viewmode-label">${field.label?html}:</span>
      <span class="viewmode-value">
      	<#if field.value != "" 
      	   && (
      	       field.value == "A"
      	       || field.value == "B"
      	       || field.value == "C"
      	       || field.value == "D"
      	       || field.value == "E")>
	         <span class="<#if field.value == "A">selected </#if>nutrient-class-a">A</span>
	         <span class="<#if field.value == "B">selected </#if>nutrient-class-b">B</span>
	         <span class="<#if field.value == "C">selected </#if>nutrient-class-c">C</span>
	         <span class="<#if field.value == "D">selected </#if>nutrient-class-d">D</span>
	         <span class="<#if field.value == "E">selected </#if>nutrient-class-e">E</span>
	    <#elseif field.value != "">
	          <span class="nutrient-class-error" >${field.value?html}</span>
        </#if>
      </span>
   </div>
</div>