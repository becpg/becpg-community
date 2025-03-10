<div class="form-field">
<#assign fieldValue = field.value>
   <#if form.mode == "view">
      <div class="viewmode-field">
         <#if field.mandatory && !(field.value?is_number) && field.value == "">
            <span class="incomplete-warning">
               <img class="icon16" src="${url.context}/res/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" />
            </span>
         </#if>
         <span class="viewmode-label">${field.label?html}:&nbsp;</span>
         <span id="${fieldHtmlId}-${field.id?replace("prop_", "")}" class="viewmode-value">${msg("form.control.novalue")}</span>
           <#if fieldValue?? >
             <script type="text/javascript">
				//<![CDATA[
				(function() {
				    YAHOO.util.Event.onAvailable('${fieldHtmlId}-${field.id?replace("prop_", "")}', function() {
				        YAHOO.util.Dom.get('${fieldHtmlId}-${field.id?replace("prop_", "")}').innerHTML = beCPG.util.renderHttpLink('${fieldValue?js_string}');
				    });
				})();
				//]]>
				</script>
            </#if>
      </div>
   <#else>
      <#if fieldValue?string == "" && field.control.params.defaultValue??>
         <#assign fieldValue = field.control.params.defaultValue>
      </#if>
      <label for="${fieldHtmlId}">
         ${field.label?html}:<#if field.mandatory>
            <span class="mandatory-indicator">${msg("form.required.fields.marker")}</span>
         </#if>
      </label>
      <input id="${fieldHtmlId}" name="${field.name}" tabindex="0" autocomplete="off"
             <#if field.control.params.password??>type="password"<#else>type="text"</#if>
             <#if field.control.params.styleClass??>class="${field.control.params.styleClass}"</#if>
             <#if field.control.params.style??>style="${field.control.params.style}"</#if>
             <#if fieldValue?is_number>value="${fieldValue?c}"<#else>value="${fieldValue?html}"</#if>
             <#if field.description??>title="${field.description}"</#if>
             <#if field.control.params.maxLength??>maxlength="${field.control.params.maxLength}"<#else>maxlength="10000"</#if> 
             <#if field.control.params.size??>size="${field.control.params.size}"</#if> 
             <#if field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>disabled="true"</#if> />
      <@formLib.renderFieldHelp field=field />
   </#if>
</div>
