<#include "/org/alfresco/components/form/controls/common/utils.inc.ftl" />

<#if field.control.params.optionSeparator??>
   <#assign optionSeparator=field.control.params.optionSeparator>
<#else>
   <#assign optionSeparator=",">
</#if>
<#if field.control.params.labelSeparator??>
   <#assign labelSeparator=field.control.params.labelSeparator>
<#else>
   <#assign labelSeparator="|">
</#if>


<#assign fieldValue=field.value>

<#if fieldValue?string == "" && field.control.params.defaultValueContextProperty??>
   <#if context.properties[field.control.params.defaultValueContextProperty]??>
      <#assign fieldValue = context.properties[field.control.params.defaultValueContextProperty]>
   <#elseif args[field.control.params.defaultValueContextProperty]??>
      <#assign fieldValue = args[field.control.params.defaultValueContextProperty]>
   </#if>
</#if>

<#if field.control.params.isSearch?? && form.mode == "edit" || form.mode == "create" >
	<#if field.control.params.defaultValue ??>
		<#assign fieldValue=field.control.params.defaultValue>
	<#elseif field.control.params.isSearch?? >
		<#assign fieldValue="" />
	</#if>
</#if>

<div class="form-field">
   <#if form.mode == "view">
      <div class="viewmode-field">
         <#if field.mandatory && !(fieldValue?is_number) && fieldValue?string == "">
            <span class="incomplete-warning"><img class="icon16" src="${url.context}/res/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
         </#if>
         <span class="viewmode-label">${field.label?html}:</span>
         <#if fieldValue?string == "">
            <#assign valueToShow=msg("form.control.novalue")>
         <#else>
            <#assign valueToShow=fieldValue>
            <#if field.control.params.options?? && field.control.params.options != "">
               <#list field.control.params.options?split(optionSeparator) as nameValue>
                  <#if nameValue?index_of(labelSeparator) == -1>
                     <#if nameValue == fieldValue?string || (fieldValue?is_number && fieldValue?c == nameValue)>
                        <#assign valueToShow=nameValue>
                        <#break>
                     </#if>
                  <#else>
                     <#assign choice=nameValue?split(labelSeparator)>
                     <#if choice[0] == fieldValue?string || (fieldValue?is_number && fieldValue?c == choice[0])>
                     	<#if field.control.params.showCode?? && choice[0]?has_content && "-" != choice[0]>
                     	 <#assign valueToShow=choice[0]+" - "+msg(choice[1])>
                     	<#else>	
                     	  <#assign valueToShow=msg(choice[1])>
                   	    </#if>
                        <#break>
                     </#if>
                  </#if>
               </#list>
            </#if>
         </#if>
         <span class="viewmode-value">${valueToShow?html}</span>
      </div>
   <#else>
      <label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
      <#if field.control.params.options?? && field.control.params.options != "">
         <select id="${fieldHtmlId}" name="${field.name}" tabindex="0"
               <#if field.description??>title="${field.description}"</#if>
               <#if field.indexTokenisationMode??>class="non-tokenised"</#if>
               <#if field.control.params.size??>size="${field.control.params.size}"</#if> 
               <#if field.control.params.styleClass??>class="${field.control.params.styleClass}"</#if>
               <#if field.control.params.style??>style="${field.control.params.style}"</#if>
               <#if field.disabled  && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>disabled="true"</#if>>
               	<#if field.control.params.insertBlank??>
               		 <option value="" <#if fieldValue?length &lt; 1 >selected="selected"</#if> ></option>
               	</#if>
               <#list field.control.params.options?split(optionSeparator) as nameValue>
                  <#if nameValue?index_of(labelSeparator) == -1>
                     <option value="<#if field.control.params.isSearch?? && nameValue?has_content && "-" != nameValue>=</#if><#if field.control.params.isSearch?? && (!nameValue?has_content || "-" == nameValue)>""<#else><#if field.control.params.isSearch??>${nameValue?string?replace(" ","\\ ")?html}<#else>${nameValue?html}</#if></#if>"<#if (nameValue == fieldValue?string || (fieldValue?is_number && fieldValue?c == nameValue)) > selected="selected"</#if>>${nameValue?html}</option>
                  <#else>
                     <#assign choice=nameValue?split(labelSeparator)>
                     <option value="<#if field.control.params.isSearch?? && choice[0]?has_content && "-" != choice[0]>=</#if><#if field.control.params.isSearch?? && (!choice[0]?has_content || "-" == choice[0])>""<#else><#if field.control.params.isSearch??>${choice[0]?string?replace(" ","\\ ")?html}<#else>${choice[0]?html}</#if></#if>"<#if (choice[0] == fieldValue?string || (fieldValue?is_number && fieldValue?c == choice[0])) > selected="selected"</#if>><#if field.control.params.showCode?? && choice[0]?has_content && "-" != choice[0]>${choice[0]?html} - </#if>${msg(choice[1])?html}</option>
                  </#if>
               </#list>
         </select>
         <@formLib.renderFieldHelp field=field />
         <#if field.control.params.isStoreable?? && form.mode == "create">
		  <script type="text/javascript">
			if (typeof (Storage) !== "undefined") {
				
				YAHOO.util.Event.addListener("${fieldHtmlId}", "change", function() {
						if (localStorage != null) {
						 	var selEl = YAHOO.util.Dom.get("${fieldHtmlId}");
							localStorage.setItem('${field.name}', selEl.value);
							for(var e=0;e<selEl.options.length;e++){
							  selEl.options[e].defaultSelected=(selEl.selectedIndex==e);
							}
						}
					});
					
				YAHOO.util.Event.onAvailable("${fieldHtmlId}", function() {
					if (localStorage.getItem('${field.name}') != null) {
					  var selEl = YAHOO.util.Dom.get("${fieldHtmlId}");
						selEl.value = localStorage.getItem('${field.name}');
						for(var e=0;e<selEl.options.length;e++){
						 	selEl.options[e].defaultSelected=(selEl.selectedIndex==e);
						}
					}
				 });
				
			 }
			</script>
	       </#if>
	       <#if field.control.params.visibleFieldsValues??>
		       	<script type="text/javascript">
			       	(() => {
			       		const visibleFieldsValues = ${field.control.params.visibleFieldsValues};
			       		function toggleVisibility() {
							const value = YAHOO.util.Dom.get("${fieldHtmlId}")?.value;
							for (let [field, values] of Object.entries(visibleFieldsValues)) {
								field = YAHOO.util.Dom.get("${args.htmlid?js_string?html}_" + field).parentElement;
								if (!values.includes(value)) {
									field.style = "display: none";	
									field.value = undefined;
								} else {
									field.style = undefined;
								}
							}
			       		}
			       		YAHOO.util.Event.onAvailable("${fieldHtmlId}", toggleVisibility);
			       		YAHOO.util.Event.addListener("${fieldHtmlId}", "change", toggleVisibility);
			       	})();
				</script>
			</#if>
      <#else>
         <div id="${fieldHtmlId}" class="missing-options">${msg("form.control.selectone.missing-options")}</div>
      </#if>
   </#if>
</div>