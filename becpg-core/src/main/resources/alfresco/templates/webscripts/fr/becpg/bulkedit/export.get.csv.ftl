<#ftl strip_whitespace=true/>
<#macro renderData data>
<#compress>
 <#if data.displayValue?exists >
	      <#if data.displayValue?is_boolean>
			${data.displayValue?string}
	      <#elseif data.displayValue?is_number>
	 		<#setting locale="en_US">
			${"${data.displayValue}"?replace(',', ' ')}
	      <#else>
			${data.displayValue}
	      </#if>
	 <#else>
         <#if data.value?is_boolean>
			${data.value?string}
     	 <#elseif data.value?is_number>
			${data.value?c}
		 <#else>
		 	${data.value}
		 </#if>
	</#if>
</#compress>  
</#macro>
<#list data.fields as field>"${field}";</#list>	
<#compress>
<#list data.items as item>
<#list data.fields as field>
<#assign display=true >
<#list item.nodeData?keys as key><#assign itemData = item.nodeData[key]><#if key?contains(field?replace(":","_")) ><#assign display=false >"<#if itemData?is_sequence><#list itemData as data><@renderData data /><#if data_has_next>|</#if></#list><#else><@renderData itemData /></#if>";</#if></#list><#if display>"";</#if></#list>
<#if item_has_next>
</#if>
</#list>
</#compress>  