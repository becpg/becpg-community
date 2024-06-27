<div class="form-field">
   <#if form.mode == "view">
      <div class="viewmode-field">
         <#if field.mandatory && !(field.value?is_number) && field.value == "">
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
         <#if field.control.params.activateLinks?? && field.control.params.activateLinks == "true">
            <#assign fieldValue=field.value?html?replace("&amp;","&")?replace("((http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?\\^=%&:\\/~\\+#]*[\\w\\-\\@?\\^=%&\\/~\\+#])?)", "<a href=\"$1\" target=\"_blank\">$1</a>", "r")>
         <#else>
            <#if field.value?is_number>
               <#assign fieldValue=field.value?c>
            <#else>
               <#assign fieldValue=field.value?html>
            </#if>
         </#if>
        
         <span id="${fieldHtmlId}-${field.id?replace("prop_","")}" class="viewmode-value <#if field.dataType == "mltext">viewmode-mltext</#if>"><#if fieldValue == "">${msg("form.control.novalue")}<#else>${fieldValue}</#if></span>
      </div>
   <#else>
   
	   <#assign fieldValue=field.value>
	   <#if fieldValue?string == "" && field.control.params.defaultValue??>
           <#assign fieldValue=field.control.params.defaultValue>
	   </#if>
      <label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if> <#if field.control.params.maxLength??><@formLib.renderLocaleImage field=field maxLength=field.control.params.maxLength?number/> <#else> <@formLib.renderLocaleImage field=field/></#if></label>
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