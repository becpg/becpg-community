<#if field.control.params.ds?exists><#assign ds=field.control.params.ds><#else><#assign ds=''></#if>
<#if field.control.params.style?exists><#assign style=field.control.params.style></#if>
<#if args.entityNodeRef?? >
	<#if ds?contains("?")>
		<#assign ds=ds+"&entityNodeRef="+args.entityNodeRef>
	<#else>
		<#assign ds=ds+"?entityNodeRef="+args.entityNodeRef>
	</#if>
</#if>

<div class="form-field">
   <#if form.mode == "view" || field.disabled >
      <div id="${fieldHtmlId}" class="viewmode-field">
         <#if field.mandatory && !(field.value?is_number) && field.value == "">
            <span class="incomplete-warning"><img src="${url.context}/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
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
      <div  class="yui-ac">
        <div id="${fieldHtmlId}-autocomplete" class="ac-body" <#if style??>style="${style}"</#if>>
        <span id="${fieldHtmlId}-toggle-autocomplete" class="ac-toogle" ></span>
	         <#if field.repeating || field.dataType == 'noderef' || field.control.params.parentMode?exists >
	         	<#if field.dataType != 'noderef' || field.repeating || field.control.params.parentMode?exists >
			 			<span id="${fieldHtmlId}-basket" class="viewmode-value current-values"></span>
			 		</#if>
			 		<input id="${fieldHtmlId}" type="text" name="-" tabindex="0"
	                <#if field.description?exists>title="${field.description}"</#if>
	                <#if field.control.params.maxLength?exists>maxlength="${field.control.params.maxLength}"</#if> 
	                <#if field.control.params.size?exists>size="${field.control.params.size}"</#if> 
	                <#if field.disabled>disabled="true"</#if> 
	                 class="yui-ac-input multi-assoc hidden"  />										
				<#else>
		        	<input id="${fieldHtmlId}" type="text" name="${field.name}" tabindex="0"
		            <#if field.value?is_number>value="${field.value?c}"<#else>value="${field.value?html}"</#if>
		            <#if field.description?exists>title="${field.description}"</#if>
		            <#if field.control.params.maxLength?exists>maxlength="${field.control.params.maxLength}"</#if> 
		            <#if field.control.params.size?exists>size="${field.control.params.size}"</#if> 
		            <#if field.disabled>disabled="true"</#if> 
		             class="yui-ac-input"  />
         	</#if>
         <span class="clear" ></span>
         </div>
         <div id="${fieldHtmlId}-container"></div>
         <#if field.repeating ||  field.dataType == 'noderef' || field.control.params.parentMode?exists>
        	<input type="hidden" id="${fieldHtmlId}-added" name="${field.name}" <#if field.value?is_number>value="${field.value?c}"<#else>value="${field.value?html}"</#if> />
         </#if>
      </div>
   </#if>
</div>

<@inlineScript group="form">
	new beCPG.component.AutoCompletePicker('${fieldHtmlId}', '${fieldHtmlId}', <#if field.dataType == 'noderef' >true<#else>false</#if>).setOptions(
			   {
			 		currentValue: "${field.value}",
			 		mode: "${form.mode}",
			 		readOnly : ${field.disabled?string},
			<#if field.control.params.parentMode?exists>				 		
                    multipleSelectMode:true,
                    isParentMode : true,
			<#else>
			      multipleSelectMode: ${field.repeating?string}, 
			</#if>
			      isMandatory : ${field.mandatory?string},
			 		dsStr:"${ds}"
			<#if field.control.params.parent?exists>
			<#assign parentFieldHtmlId=args.htmlid + "_prop_" + field.control.params.parent >
			 		,parentFieldHtmlId:"${parentFieldHtmlId}"
			</#if>
			<#if field.control.params.local?exists>
			 		,isLocalProxy:${field.control.params.local?string}
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
</@>
