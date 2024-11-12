<#assign isTrue=false>
<#assign isFalse=false>
<#if field.value?? && !(field.control.params.isSearch??)>
 <#if field.value?is_boolean>
    <#assign isTrue=field.value>
    <#assign isFalse=!field.value>
 <#elseif field.value?is_string && field.value == "true">
    <#assign isTrue=true>
  <#elseif field.value?is_string && field.value == "false">
    <#assign isFalse=true>
 </#if>
</#if>

<div class="form-field">
   <#if form.mode == "view">
      <div class="viewmode-field">
         <span class="viewmode-label">${field.label?html}:</span>
         <span class="viewmode-value"><#if isTrue>${msg("data.boolean.true")}<#elseif isFalse>${msg("data.boolean.false")}<#else>${msg("data.boolean.empty")}</#if></span>
      </div>
   <#else>
        <label for="${fieldHtmlId}">${field.label?html}:</label>
        <div class="form-field-boolean"  >
             <input id="${fieldHtmlId}" type="hidden" name="${field.name}" value="<#if isTrue><#if field.control.params.isSearch??>=</#if>true<#elseif isFalse><#if field.control.params.isSearch??>=</#if>false<#else></#if>" />
             
		    <input type="radio" id="true-${fieldHtmlId}" name="${field.name}-entry" tabindex="0" value="<#if field.control.params.isSearch??>=</#if>true" 
		        <#if field.description??>title="${field.description}"</#if>
		        <#if field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>disabled="true"</#if>
		        <#if isTrue>checked="checked"</#if>
		        onchange='javascript:var el=YAHOO.util.Dom.get("${fieldHtmlId}"); el.value=this.value; el.dispatchEvent(new Event("change"));'
		         > 
		     <label for="true-${fieldHtmlId}"> ${msg("data.boolean.true")} </label>
		   
	
	
		    <input type="radio" id="false-${fieldHtmlId}"  name="${field.name}-entry" tabindex="0"  value="<#if field.control.params.isSearch??>=</#if>false" 
		    <#if field.description??>title="${field.description}"</#if>
		    <#if field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>disabled="true"</#if> 
		    <#if isFalse> checked="checked"</#if> 
		    onchange='javascript:var el=YAHOO.util.Dom.get("${fieldHtmlId}"); el.value=this.value; el.dispatchEvent(new Event("change"));'
		    > 
		    <label for="false-${fieldHtmlId}"> ${msg("data.boolean.false")} </label>
		    
		          
		    <input type="radio" id="empty-${fieldHtmlId}"  name="${field.name}-entry"   tabindex="0" value="" 
		    <#if field.description??>title="${field.description}"</#if>
		    <#if field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>disabled="true"</#if> 
		    <#if !isTrue && !isFalse>checked="checked"</#if>  
		    onchange='javascript:var el=YAHOO.util.Dom.get("${fieldHtmlId}"); el.value=this.value; el.dispatchEvent(new Event("change"));'
		    >
		    <label for="empty-${fieldHtmlId}"> ${msg("data.boolean.empty")} </label>
		
		</div>
      	<@formLib.renderFieldHelp field=field />
   </#if>
</div>