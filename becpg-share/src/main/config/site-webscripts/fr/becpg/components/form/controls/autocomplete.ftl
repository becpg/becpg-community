<#if field.control.params.ds?exists><#assign ds=field.control.params.ds><#else><#assign ds=''></#if>
<#if field.control.params.prevFieldName?exists><#assign prevFieldName=field.control.params.prevFieldName><#else><#assign prevFieldName=''></#if>
<#if field.control.params.style?exists><#assign style=field.control.params.style><#else><#assign style='width:30em;'></#if>

<div class="form-field">
   <#if form.mode == "view">
      <div class="viewmode-field">
         <#if field.mandatory && !(field.value?is_number) && field.value == "">
            <span class="incomplete-warning"><img src="${url.context}/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
         </#if>
         <span class="viewmode-label">${field.label?html}:</span>
         <#if field.control.params.activateLinks?? && field.control.params.activateLinks == "true">
            <#assign fieldValue=field.value?html?replace("((http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?\\^=%&:\\/~\\+#]*[\\w\\-\\@?\\^=%&\\/~\\+#])?)", "<a href=\"$1\" target=\"_blank\">$1</a>", "r")>
         <#else>
            <#assign fieldValue=field.value?html>
         </#if>
         <span class="viewmode-value">${fieldValue}</span>
      </div>
   <#else>
      <label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory!false><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
      <div style="padding-bottom: 2em;" class="yui-ac">
        <div id="${fieldHtmlId}-autocomplete" class="ac-body" style="${style}">
        <span id="${fieldHtmlId}-toggle-autocomplete" class="ac-toogle" ></span>
        <input id="${fieldHtmlId}" type="text" name="${field.name}" tabindex="0"
                <#if field.value?is_number>value="${field.value?c}"<#else>value="${field.value?html}"</#if>
                <#if field.description?exists>title="${field.description}"</#if>
                <#if field.control.params.maxLength?exists>maxlength="${field.control.params.maxLength}"</#if> 
                <#if field.control.params.size?exists>size="${field.control.params.size}"</#if> 
                <#if field.disabled>disabled="true"</#if> class="yui-ac-input"  />
                 <span class="clear" ></span>
         </div>
         <div id="${fieldHtmlId}-container"></div>
      </div>
   </#if>
</div>

<#if form.mode != "view">
<script type="text/javascript">//<![CDATA[
(function()
{
 var bAC = new beCPG.component.AutoCompletePicker('${fieldHtmlId}', '${fieldHtmlId}', false).setOptions(
   {
 		currentValue: "${field.value}",
 		mode: "${form.mode}",
        multipleSelectMode: false, 
        isMandatory : ${field.mandatory?string},
 		dsStr:"${ds}"
<#if field.control.params.parent?exists>
<#assign parentFieldHtmlId=args.htmlid + "_prop_" + field.control.params.parent >
 		,parentFieldHtmlId:"${parentFieldHtmlId}"
</#if>
  });

})();
//]]></script>
</#if>