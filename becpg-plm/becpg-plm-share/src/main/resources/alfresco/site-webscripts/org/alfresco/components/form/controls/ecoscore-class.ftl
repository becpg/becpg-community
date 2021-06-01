<div class="form-field">
   <div class="viewmode-field">
      <span class="viewmode-label">${field.label?html}:</span>
      <span class="viewmode-value ecoscore-class">
      	<#if field.value != "" 
      	   && (
      	       field.value == "A"
      	       || field.value == "B"
      	       || field.value == "C"
      	       || field.value == "D"
      	       || field.value == "E")>
	         <#if field.value == "A"><span class="ecoscore-class-a"></span> </#if>
	         <#if field.value == "B"><span class="ecoscore-class-b"></span> </#if>
	         <#if field.value == "C"><span class="ecoscore-class-c"></span> </#if>
	         <#if field.value == "D"><span class="ecoscore-class-d"></span> </#if>
	         <#if field.value == "E"><span class="ecoscore-class-e"></span> </#if>
	    <#elseif field.value != "">
	          <span class="ecoscore-class-error" >${field.value?html}</span>
        </#if>
      </span>
   </div>
</div>