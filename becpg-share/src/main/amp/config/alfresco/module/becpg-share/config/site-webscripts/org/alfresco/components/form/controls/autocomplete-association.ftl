<#if field.control.params.ds??>
	<#assign ds=field.control.params.ds>
<#else>
	<#if field.dataType??>
		<#assign ds='becpg/autocomplete/targetassoc/associations/${field.dataType}'/>
	<#else>
		<#assign ds=''/>
	</#if>
</#if>
<#if field.control.params.style??><#assign style=field.control.params.style></#if>
<#assign siteId=args.siteId!"">
<#if !field.control.params.showLink?? || field.control.params.showLink !="false">
	<#if field.control.params.pageLinkTemplate??>
		<#assign pageLinkTemplate='${field.control.params.pageLinkTemplate}'/>
	<#else>
		<#assign pageLinkTemplate='document-details?nodeRef={nodeRef}'/>
	</#if>
</#if>
<#if args.entityNodeRef?? >
	<#if ds?contains("?")>
		<#assign ds=ds+"&entityNodeRef="+args.entityNodeRef>
	<#else>
		<#assign ds=ds+"?entityNodeRef="+args.entityNodeRef>
	</#if>
</#if>
<#assign controlId = fieldHtmlId + "-cntrl">
<script type="text/javascript">//<![CDATA[
(function()
{
  new beCPG.component.AutoCompletePicker('${controlId}', '${fieldHtmlId}', true).setOptions(
		   {
		 		currentValue: "${field.value}",
		 		mode: "${form.mode}",
		 		readOnly : ${field.disabled?string},
		        multipleSelectMode: ${field.endpointMany?string}, 
		        isMandatory : ${field.mandatory?string},
		      <#if pageLinkTemplate??>
		 		targetLinkTemplate: "${pageLinkTemplate}" ,
		 		</#if>
		 		dsStr:"${ds}"
				<#if field.control.params.parent??>
					<#assign parentFieldHtmlId=args.htmlid + "_prop_" + field.control.params.parent >
		 		,parentFieldHtmlId:"${parentFieldHtmlId}"
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
				urlParamsToPass:"${urlParamsToPass}"
			</#if>
  });
})();
//]]></script>
<div class="form-field">
   <#if form.mode == "view"  ||  field.disabled == true>
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
			<div class="yui-ac" >
					 <div id="${fieldHtmlId}-autocomplete" class="ac-body" <#if style??>style="${style}"</#if>>
					 <span id="${fieldHtmlId}-toggle-autocomplete" class="ac-toogle"></span>
					  <#if field.endpointMany>
					  		<span id="${controlId}-basket" class="viewmode-value current-values"></span>										
					  </#if>
				      <input id="${fieldHtmlId}" type="text" name="-" onfocus="this.hasFocus=true" onblur="this.hasFocus=false" tabindex="0"
				             <#if field.description??>title="${field.description}"</#if>
				             <#if field.control.params.maxLength??>maxlength="${field.control.params.maxLength}"</#if> 
				             <#if field.control.params.size??>size="${field.control.params.size}"</#if> 
				             <#if field.disabled>disabled="true"</#if> 
				             class="yui-ac-input<#if field.endpointMany> multi-assoc</#if>" <#if !field.endpointMany>value="${field.value}" </#if> />				      
			        </div>			
			      <div id="${fieldHtmlId}-container"></div>
			 	
			  	 <input type="hidden" id="${controlId}-removed" name="${field.name}_removed" />
		       <input type="hidden" id="${controlId}-orig" name="-" value="${field.value?html}" />
		       <input type="hidden" id="${controlId}-added" name="${field.name}_added" />
		        
			<@formLib.renderFieldHelp field=field />
			</div>
      </div>
   </#if>
</div>
