<#if field.control.params.isSearch?exists><#assign isSearch=field.control.params.isSearch></#if>
<#if field.control.params.ds??>
	<#assign ds=field.control.params.ds>
<#else>
	<#if field.dataType??>
		<#assign ds='becpg/autocomplete/targetassoc/associations/${field.dataType}'/>
	<#else>
		<#assign ds=''/>
	</#if>
</#if>
<#if isSearch?? >
	<#if ds?contains("?")>
		<#assign ds=ds+"&isSearch="+isSearch>
	<#else>
		<#assign ds=ds+"?isSearch="+isSearch>
	</#if>
</#if>
<#if field.control.params.style??><#assign style=field.control.params.style></#if>
<#assign siteId=args.siteId!"">
<#if !field.control.params.showLink?? || field.control.params.showLink !="false">
	<#if field.control.params.pageLinkTemplate??>
		<#assign pageLinkTemplate='${field.control.params.pageLinkTemplate}'/>
	<#else>
		<#assign pageLinkTemplate='entity-data-lists?list=View-properties&nodeRef={nodeRef}'/>
	</#if>
<#elseif field.control.params.showLink?? && field.control.params.showLink =="false">
	<#assign pageLinkTemplate='null'/>
</#if>
<#if args.entityNodeRef?? >
	<#if ds?contains("?")>
		<#assign ds=ds+"&entityNodeRef="+args.entityNodeRef>
	<#else>
		<#assign ds=ds+"?entityNodeRef="+args.entityNodeRef>
	</#if>
</#if>
<#if field.control.params.excludeSources?? && "true" == field.control.params.excludeSources>
	<#if ds?contains("?")>
		<#assign ds=ds+"&excludeSources=true">
	<#else>
		<#assign ds=ds+"?excludeSources=true">
	</#if>
	<#assign ds=ds+"&itemId="+args.itemId>
	<#assign ds=ds+"&fieldName="+field.name>
</#if>
<#assign formId=args.htmlid?js_string?html + "-form">
<#if field.control.params.multipleSelectMode?? && "true" == field.control.params.multipleSelectMode>
	<#assign multipleSelectMode=true>
<#elseif field.control.params.multipleSelectMode?? && "false" == field.control.params.multipleSelectMode>
	<#assign multipleSelectMode=false>
<#else>
	<#assign multipleSelectMode=field.endpointMany />
</#if>

<#assign controlId = fieldHtmlId + "-cntrl">
<div class="form-field">
   <#if form.mode == "view"  ||  (field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true"))>
      <div id="${controlId}" class="viewmode-field">
         <#if (field.endpointMandatory!false || field.mandatory!false) && field.value == "">
            <span class="incomplete-warning"><img class="icon16" src="${url.context}/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
         </#if>
         <span class="viewmode-label">${field.label?html}:</span>
         <span id="${fieldHtmlId}-values" class="viewmode-value current-values hidden"></span>
      </div>
   <#else>
      <label for="${controlId}">${field.label?html}:<#if field.endpointMandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>     
      <div id="${controlId}" class="object-finder">        
			<div class="yui-ac" style="display:inline-block;" >
					 <div id="${fieldHtmlId}-autocomplete" class="ac-body" <#if style??>style="${style}"</#if>>
					 <span id="${fieldHtmlId}-toggle-autocomplete" class="ac-toogle"></span>
					  <#if multipleSelectMode>
					  		<span id="${controlId}-basket" class="viewmode-value current-values"></span>										
					  </#if>
				      <input id="${fieldHtmlId}" type="text" name="-" onfocus="this.hasFocus=true" onblur="this.hasFocus=false" tabindex="0"
				             <#if field.description??>title="${field.description}"</#if>
				             <#if field.control.params.maxLength??>maxlength="${field.control.params.maxLength}"</#if> 
				             <#if field.control.params.size??>size="${field.control.params.size}"</#if> 
				             <#if (field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true"))>disabled="true"</#if> 
				             class="yui-ac-input<#if multipleSelectMode> multi-assoc</#if>" <#if !multipleSelectMode>value="${field.value}" </#if> />
				        <span class="clear" ></span>				      
			      </div>			
			      <div id="${fieldHtmlId}-container"></div>
			 	
			  	<input type="hidden" id="${controlId}-removed" name="${field.name}_removed" />
		        <input type="hidden" id="${controlId}-orig" name="-" value="${field.value?html}" />
		        <input type="hidden" id="${controlId}-added" name="${field.name}_added" />
			</div>
			 <@formLib.renderFieldHelp field=field />
      </div>
     
   </#if>
</div>
<script type="text/javascript">//<![CDATA[
	new beCPG.component.AutoCompletePicker('${controlId}', '${fieldHtmlId}', true).setOptions({
 		currentValue: "${field.value}",
 		mode: "${form.mode}",
 		formId: "${formId}",
 		readOnly : ${(field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true"))?string},
        multipleSelectMode: ${multipleSelectMode?string}, 
        isMandatory : ${field.mandatory?string},
      <#if pageLinkTemplate?? && pageLinkTemplate !="null">
 		targetLinkTemplate: "${pageLinkTemplate}" ,
 		</#if>
 		dsStr:"${ds}"
		<#if field.control.params.parent??>
			<#assign parentFieldHtmlId=args.htmlid + "_prop_" + field.control.params.parent >
 		,parentFieldHtmlId:"${parentFieldHtmlId}"
		<#elseif field.control.params.parentAssoc??>
			<#assign parentFieldHtmlId=args.htmlid + "_assoc_" + field.control.params.parentAssoc >
 		,parentFieldHtmlId:"${parentFieldHtmlId}-cntrl"
		</#if>
		<#if field.control.params.showColor?exists>
	 	,showColor:${field.control.params.showColor?string}
		</#if>
		<#if field.control.params.showTooltip??>
		 ,showToolTip:${field.control.params.showTooltip?string}
		</#if>
		<#if field.control.params.urlParamsToPass??>
		<#assign firstLabel=true>			
		<#assign urlParamsToPass="">
		,<#list field.control.params.urlParamsToPass?split(',') as urlParam>
			<#assign arg="args." + urlParam >
            <#if !arg?eval?? >
				<#assign arg="page.url.args." + urlParam >				
			</#if>
			<#if arg?eval?? >
				<#if !firstLabel>
		        	<#assign urlParamsToPass=urlParamsToPass+"&">
		     	<#else>
		        	<#assign firstLabel=false>
		     	</#if>		
	    	 	<#assign urlParamsToPass=urlParamsToPass + "extra." +urlParam + "=" + arg?eval!"">			
			</#if>
		</#list>
		urlParamsToPass: {
 			toString() {
 				return "${urlParamsToPass}<#if field.control.params.extraPaths??>&extra.paths=" + document.querySelector('[name="${field.control.params.extraPaths}"]').value?.split(',').map(str => str.split(':')[1] + "_Hierarchy")<#else>"</#if>;
 			}
 		}
 		<#elseif field.control.params.extraPaths??>
 		,urlParamsToPass: {
 			toString() {
 				return "extra.paths=" + document.querySelector('[name="${field.control.params.extraPaths}"]').value?.split(',').map(str => str.split(':')[1] + "_Hierarchy");
 			}
 		}
		</#if>
  });
//]]></script>
