<div class="form-field">
   <#if form.mode == "view">
      <div class="viewmode-field">
         <#if field.mandatory && !(field.value?is_number) && field.value == "">
            <span class="incomplete-warning"><img src="${url.context}/res/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
         </#if>
         <span class="viewmode-label">${field.label?html}:</span>
         <#if field.control.params.activateLinks?? && field.control.params.activateLinks == "true">
            <#assign fieldValue=field.value?html?replace("((http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?\\^=%&:\\/~\\+#]*[\\w\\-\\@?\\^=%&\\/~\\+#])?)", "<a href=\"$1\" target=\"_blank\">$1</a>", "r")>
         <#else>
            <#if field.value?is_number>
               <#assign fieldValue=field.value?c>
            <#else>
               <#assign fieldValue=field.value?html>
            </#if>
         </#if>
         <span class="viewmode-value"><#if fieldValue == "">${msg("form.control.novalue")}<#else>${fieldValue}</#if></span>
      </div>
   <#else>

      <label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
      <input style="width:6em!important" id="${fieldHtmlId}" name="${field.name}" type="color"
             <#if field.value?is_number>value="${field.value?c}"<#else>value="${field.value?html}"</#if>
             <#if field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>disabled="true"</#if></input>
      <span id="${fieldHtmlId}-color-button" class="select-color-picker" title="${msg("actions.select-color")}">&nbsp;</span>
  
      
      <script type="text/javascript">
	  (function()
		{
		 YAHOO.util.Event.on("${fieldHtmlId}-color-button", 'click', function (){
	   	  		beCPG.module.getColorPickerInstance().show({
					fieldId : "${fieldHtmlId}"
	             });
	         });
	   	    
   	    } )();
   	 </script>
      
      <@formLib.renderFieldHelp field=field />
   </#if>
</div>