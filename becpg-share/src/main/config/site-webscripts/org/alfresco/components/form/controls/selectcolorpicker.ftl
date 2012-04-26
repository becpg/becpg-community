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
   	<script>
   	  function onChangeColorPicker(selectInstance){
   	  		selectInstance.style.background='#'+selectInstance.options[selectInstance.selectedIndex].value;
   	  }
   	  
   	</script>
   
      <label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
      <select id="${fieldHtmlId}" name="${field.name}" style=" width: 100px;background-color:#${field.value?html};"
             <#if field.value?is_number>value="${field.value?c}"<#else>value="${field.value?html}"</#if>
             <#if field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>disabled="true"</#if>
             onchange="javascript:onChangeColorPicker(this);" 
              >
             <option value="${field.value?html}" style="background-color:#${field.value?html};" selected></option>
				 <option value="FFFFFF" style="border:1px solid;background-color:#FFFFFF;"></option>
				 <option value="FFDFDF" style="border:1px solid;background-color:#FFDFDF;"></option>
				 <option value="FFBFBF" style="border:1px solid;background-color:#FFBFBF;"></option>
				 <option value="FF9F9F" style="border:1px solid;background-color:#FF9F9F;"></option>
				 <option value="FF7F7F" style="border:1px solid;background-color:#FF7F7F;"></option>
				 <option value="FF5F5F" style="border:1px solid;background-color:#FF5F5F;"></option>
				 <option value="FF3F3F" style="border:1px solid;background-color:#FF3F3F;"></option>
				 <option value="FF1F1F" style="border:1px solid;background-color:#FF1F1F;"></option>
				 <option value="FF0000" style="border:1px solid;background-color:#FF0000;"></option>
				 <option value="DF1F00" style="border:1px solid;background-color:#DF1F00;"></option>
				 <option value="C33B00" style="border:1px solid;background-color:#C33B00;"></option>
				 <option value="A75700" style="border:1px solid;background-color:#A75700;"></option>
				 <option value="8B7300" style="border:1px solid;background-color:#8B7300;"></option>
				 <option value="6F8F00" style="border:1px solid;background-color:#6F8F00;"></option>
				 <option value="53AB00" style="border:1px solid;background-color:#53AB00;"></option>
				 <option value="37C700" style="border:1px solid;background-color:#37C700;"></option>
				 <option value="1BE300" style="border:1px solid;background-color:#1BE300;"></option>
				 <option value="00FF00" style="border:1px solid;background-color:#00FF00;"></option>
				 <option value="00DF1F" style="border:1px solid;background-color:#00DF1F;"></option>
				 <option value="00C33B" style="border:1px solid;background-color:#00C33B;"></option>
				 <option value="00A757" style="border:1px solid;background-color:#00A757;"></option>
				<option value="008B73" style="border:1px solid;background-color:#008B73;"></option>
				<option value="006F8F" style="border:1px solid;background-color:#006F8F;"></option>
				<option value="0053AB" style="border:1px solid;background-color:#0053AB;"></option>
				<option value="0037C7" style="border:1px solid;background-color:#0037C7;"></option>
				<option value="001BE3" style="border:1px solid;background-color:#001BE3;"></option>
				<option value="0000FF" style="border:1px solid;background-color:#0000FF;"></option>
				<option value="0000df" style="border:1px solid;background-color:#0000df;"></option>
				<option value="0000c3" style="border:1px solid;background-color:#0000c3;"></option>
				<option value="0000a7" style="border:1px solid;background-color:#0000a7;"></option>
				<option value="00008b" style="border:1px solid;background-color:#00008b;"></option>
				<option value="00006f" style="border:1px solid;background-color:#00006f;"></option>
				<option value="000053" style="border:1px solid;background-color:#000053;"></option>
				<option value="000037" style="border:1px solid;background-color:#000037;"></option>
				<option value="00001b" style="border:1px solid;background-color:#00001b;"></option>
				<option value="000000" style="border:1px solid;background-color:#000000;"></option>
      </select>
      <@formLib.renderFieldHelp field=field />
   </#if>
</div>