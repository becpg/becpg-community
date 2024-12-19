<#if field.control.params.rows??><#assign rows=field.control.params.rows><#else><#assign rows=2></#if>
<#if field.control.params.columns??><#assign columns=field.control.params.columns><#else><#assign columns=60></#if>

<div class="form-field alf-textarea">
	<#if field.dataKeyName?? && field.dataType??>
	   <#if form.mode == "view">
	      <div class="viewmode-field">
	         <#if field.mandatory && field.value == "">
	            <span class="incomplete-warning"><img class="icon16" src="${url.context}/res/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
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
	         </#if>
         </span>
         
         <#assign tmpFieldValue=field.value?html?replace("\n", "<br>")>
         <#if field.control.params.saveLineBreaks?? && field.control.params.saveLineBreaks == "false">
            <#assign tmpFieldValue=field.value?html>
         </#if>
	         <#if field.control.params.activateLinks?? && field.control.params.activateLinks == "true">
            <#assign fieldValue=tmpFieldValue?replace("((http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?\\^=%&:\\/~\\+#]*[\\w\\-\\@?\\^=%&\\/~\\+#])?)", "<a href=\"$1\" target=\"_blank\">$1</a>", "r")>
	         <#else>
            <#assign fieldValue=tmpFieldValue>
	         </#if>
	         <span  id="${fieldHtmlId}-${field.id?replace("prop_","")}" class="viewmode-value <#if field.dataType == "mltext">viewmode-mltext</#if>" ><#if fieldValue == "">${msg("form.control.novalue")}<#else>${fieldValue}</#if></span>
	      </div>
	   <#else>
	   
	   	   <#assign fieldValue=field.value>
		   <#if fieldValue?string == "" && field.control.params.defaultValue ??>
				<#assign fieldValue=field.control.params.defaultValue>
		   </#if>

      <label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if><#if field.control.params.maxLength??><@formLib.renderLocaleImage field=field textarea=true maxLength=field.control.params.maxLength?number/> <#else><@formLib.renderLocaleImage field=field textarea=true/></#if></label>
	      <textarea id="${fieldHtmlId}" name="${field.name}" rows="${rows}" cols="${columns}" tabindex="0"
	                <#if field.description??>title="${field.description}"</#if>
	                <#if field.control.params.styleClass??>class="${field.control.params.styleClass}"</#if>
	                <#if field.control.params.style??>style="${field.control.params.style}"</#if>
                    <#if field.control.params.maxLength??>maxlength="${field.control.params.maxLength}"<#else>maxlength="10000"</#if>
	                <#if field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>disabled="true"</#if>>${fieldValue?html}</textarea>
	     <#if !(field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true"))>
	       <@formLib.renderFieldHelp field=field />
	      </#if>
              
	   </#if>
   </#if>
</div>
