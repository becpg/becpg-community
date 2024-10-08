<#if field.control.params.ds?exists><#assign ds=field.control.params.ds><#else><#assign ds=''></#if>
<#if field.control.params.multiple?exists><#assign multiple=field.control.params.multiple?matches('true')><#else><#assign multiple=false></#if>
<#if field.control.params.style?exists><#assign style=field.control.params.style></#if>
<#if field.control.params.isSearch?exists><#assign isSearch=field.control.params.isSearch></#if>
<#assign formId=args.htmlid?js_string?html + "-form">

<#if args.entityNodeRef?? >
	<#if ds?contains("?")>
		<#assign ds=ds+"&entityNodeRef="+args.entityNodeRef>
	<#else>
		<#assign ds=ds+"?entityNodeRef="+args.entityNodeRef>
	</#if>
</#if>
<#if isSearch?? >
	<#if ds?contains("?")>
		<#assign ds=ds+"&isSearch="+isSearch>
	<#else>
		<#assign ds=ds+"?isSearch="+isSearch>
	</#if>
</#if>

<div class="form-field">
   <#if form.mode == "view" || (field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true")) >
      <div id="${fieldHtmlId}" class="viewmode-field">
         <#if field.mandatory && !(field.value?is_number) && field.value == "">
            <span class="incomplete-warning"><img class="icon16" src="${url.context}/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
         </#if>
         <span class="viewmode-label">${field.label?html}:</span>
         <#if field.control.params.activateLinks?? && field.control.params.activateLinks == "true">
            <#assign fieldValue=field.value?html?replace("((http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?\\^=%&:\\/~\\+#]*[\\w\\-\\@?\\^=%&\\/~\\+#])?)", "<a href=\"$1\" target=\"_blank\">$1</a>", "r")>
         <#else>
            <#assign fieldValue=field.value?html>
         </#if>
         <span id="${fieldHtmlId}-values" class="viewmode-value current-values hidden">${fieldValue}</span>
      </div>
   <#else>
      <label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory!false><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
      <div  class="yui-ac" style="display:inline-block;">
        <div id="${fieldHtmlId}-autocomplete" class="ac-body" <#if style??>style="${style}"</#if>>
        <span id="${fieldHtmlId}-toggle-autocomplete" class="ac-toogle" ></span>
	         <#if field.repeating || field.dataType == 'noderef' || field.control.params.parentMode?exists || multiple>
	         	<#if field.dataType != 'noderef' || field.repeating || field.control.params.parentMode?exists || multiple>
			 			<span id="${fieldHtmlId}-basket" class="viewmode-value current-values"></span>
			 		</#if>
			 		<input id="${fieldHtmlId}" type="text" name="-" onfocus="this.hasFocus=true" onblur="this.hasFocus=false" tabindex="0"
	                <#if field.description?exists>title="${field.description}"</#if>
	                <#if field.control.params.maxLength?exists>maxlength="${field.control.params.maxLength}"</#if> 
	                <#if field.control.params.size?exists>size="${field.control.params.size}"</#if> 
	                <#if (field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true"))>disabled="true"</#if> 
	                 class="yui-ac-input multi-assoc hidden"  />										
				<#else>
		        	<input id="${fieldHtmlId}" type="text" name="${field.name}" tabindex="0"
		            <#if field.value?is_number>value="${field.value?c}"<#else>value="${field.value?html}"</#if>
		            <#if field.description?exists>title="${field.description}"</#if>
		            <#if field.control.params.maxLength?exists>maxlength="${field.control.params.maxLength}"</#if> 
		            <#if field.control.params.size?exists>size="${field.control.params.size}"</#if> 
		            <#if (field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true"))>disabled="true"</#if> 
		             class="yui-ac-input"  />
         	</#if> 
         	 <span class="clear" ></span>        	
         </div>
         
	
	
         <div id="${fieldHtmlId}-container"></div>
         <#if field.repeating ||  field.dataType == 'noderef' || field.control.params.parentMode?exists || multiple>
        	<input type="hidden" id="${fieldHtmlId}-added" name="${field.name}" <#if field.value?is_number>value="${field.value?c}"<#else>value="${field.value?html}"</#if> />
         </#if>
      </div>
      <@formLib.renderFieldHelp field=field />
   </#if>
</div>
<script type="text/javascript">//<![CDATA[
new beCPG.component.AutoCompletePicker('${fieldHtmlId}', '${fieldHtmlId}', <#if field.dataType == 'noderef' >true<#else>false</#if>).setOptions(
			   {
			 		currentValue: "${field.value}",
			 		mode: "${form.mode}",
			 		formId: "${formId}",
			 		readOnly : ${(field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true"))?string},
			<#if field.control.params.parentMode?exists>				 		
                    multipleSelectMode:true,
                    isParentMode : true,
			<#elseif multiple>
					multipleSelectMode: true,
			<#else>
			      multipleSelectMode: ${field.repeating?string}, 
			</#if>
			      isMandatory : ${field.mandatory?string},
			 		dsStr:"${ds}"
			<#if field.control.params.parent??>
					<#assign parentFieldHtmlId=args.htmlid + "_prop_" + field.control.params.parent >
		 		,parentFieldHtmlId:"${parentFieldHtmlId}"
				<#elseif field.control.params.parentAssoc??>
					<#assign parentFieldHtmlId=args.htmlid + "_assoc_" + field.control.params.parentAssoc >
		 		,parentFieldHtmlId:"${parentFieldHtmlId}-cntrl"
			</#if> 
			<#if field.control.params.local?exists>
			 		,isLocalProxy:${field.control.params.local?string}
			</#if>
			<#if field.control.params.showColor?exists>
			 		,showColor:${field.control.params.showColor?string}
			</#if>
			<#if field.control.params.showTooltip?exists>
			 		,showToolTip:${field.control.params.showTooltip?string}
			</#if>
			<#if field.control.params.showPage?exists>
			 		,showPage:${field.control.params.showPage?string}
			</#if>
			<#if field.control.params.saveTitle?exists>
			 		,saveTitle:${field.control.params.saveTitle?string}
			</#if>
			<#if field.control.params.urlParamsToPass?exists>
				<#assign firstLabel=true>			
				<#assign urlParamsToPass="">
				,<#list field.control.params.urlParamsToPass?split(',') as urlParam>
					<#assign arg="args." + urlParam >
					<#if arg?eval?? >
						<#if !firstLabel>
				      	<#assign urlParamsToPass=urlParamsToPass+"&">
				     	<#else>
				        	<#assign firstLabel=false>
				     	</#if>		
			    	 	<#assign urlParamsToPass=urlParamsToPass + "extra." +urlParam + "=" + arg?eval>		
			     	</#if>
				</#list>
				urlParamsToPass:"${urlParamsToPass}"
			</#if>
	 });
//]]></script>
