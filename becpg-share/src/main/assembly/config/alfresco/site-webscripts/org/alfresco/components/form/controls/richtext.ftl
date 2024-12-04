<#include "common/editorparams.inc.ftl" />

<#if field.control.params.rows??><#assign rows=field.control.params.rows><#else><#assign rows=8></#if>
<#if field.control.params.columns??><#assign columns=field.control.params.columns><#else><#assign columns=60></#if>
<#if field.control.params.showHtml?? && field.control.params.showHtml == "true"><#assign showHtml=true><#else><#assign showHtml=false></#if>

<div class="form-field">
   <#if form.mode == "view">
   <div class="viewmode-field">
      <#if field.mandatory && field.value == "">
      <span class="incomplete-warning"><img src="${url.context}/res/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
      </#if>
      <span class="viewmode-label">${field.label?html}:&nbsp;
		<#if field.dataType == "mltext">
	      	<span id="${fieldHtmlId}#${form.arguments.itemId}#${field.name}" class="show-translation"></span>
		    	<script type="text/javascript">
					YAHOO.util.Event.addListener("${fieldHtmlId}#${form.arguments.itemId}#${field.name}", "click", function() {
							var nodeRef = "${form.arguments.itemId}" , field="${field.name?replace("prop_","")}";
							new Alfresco.module.SimpleDialog(nodeRef+"-multilingualForm").setOptions({
				              templateUrl : Alfresco.constants.URL_SERVICECONTEXT + "modules/multilingual-form/multilingual-form?nodeRef=" + nodeRef + "&field=" + field + "&readonly=true" + "&title=${field.label?url}&hideCancel=true",
				              actionUrl : Alfresco.constants.PROXY_URI + "becpg/form/multilingual/field/" + field + "?nodeRef=" + nodeRef,
				              validateOnSubmit : false,
				              destroyOnHide : true,
				              destroyFormContainer: false,
				              width: "33em",
				              doBeforeFormSubmit : {
				              	fn: function(){
				                	//Don't delete
				                 }
				              }
				           }).show();
							
						});
				</script>
	      	</span>
	  	</#if>
      </span>
      <span class="viewmode-value"><#if field.value == "">${msg("form.control.novalue")}<#else><#if !showHtml>${field.value?html}<#else>${stringUtils.stripUnsafeHTML(field.value)}</#if></#if></span>
   </div>
   <#else>
   
   <#assign fieldValue=field.value>
	   <#if fieldValue?string == "" && field.control.params.defaultValue ??>
			<#assign fieldValue=field.control.params.defaultValue>
	   </#if>
	   
   <script type="text/javascript">//<![CDATA[
   (function() {
      new Alfresco.RichTextControl("${fieldHtmlId}").setOptions(
      {
         <#if form.mode == "view" || (field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true"))>disabled: true,</#if>
         currentValue: "${fieldValue?js_string}",
         mandatory: ${field.mandatory?string},
         <@editorParameters field />
      }).setMessages(${messages});
   })();
   //]]></script>
   
  
   <label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if>
 	<#if  field.dataType == "mltext"><@formLib.renderLocaleImage field=field textarea=true htmlEditor=true/></#if>  
   </label>
   <textarea id="${fieldHtmlId}" name="${field.name}" rows="${rows}" columns="${columns}" tabindex="0"
      <#if field.description??>title="${field.description}"</#if>
      <#if field.control.params.styleClass??>class="${field.control.params.styleClass}"</#if>
      <#if field.control.params.style??>style="${field.control.params.style}"</#if>
      <#if field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>disabled="true"</#if>>${fieldValue?html}</textarea>
   </#if>
</div>