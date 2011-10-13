<#if field.control.params.ds?exists><#assign ds=field.control.params.ds><#else><#if field.dataType??><#assign ds='becpg/autocomplete/targetassoc/associations/${field.dataType}'></#if></#if>
<#if field.control.params.style?exists><#assign style=field.control.params.style><#else><#assign style='width:100%'></#if>
<#assign siteId=args.siteId!"">
<#if siteId != ""><#assign targetLinkTemplate='${url.context}/page/site/${siteId}/document-details?nodeRef={nodeRef}'><#else><#assign targetLinkTemplate='${url.context}/page/document-details?nodeRef={nodeRef}'></#if>

<#assign controlId = fieldHtmlId + "-cntrl">


<div class="form-field">
   <#if form.mode == "view">
      <div id="${controlId}" class="viewmode-field">
         <#if (field.endpointMandatory!false || field.mandatory!false) && field.value == "">
            <span class="incomplete-warning"><img src="${url.context}/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
         </#if>
         <span class="viewmode-label">${field.label?html}:</span>
         <span id="${fieldHtmlId}-values" class="viewmode-value current-values"></span>
      </div>
   <#else>
      <label for="${controlId}">${field.label?html}:<#if field.endpointMandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
      
      <div id="${controlId}" class="object-finder">        
         
         <#if form.mode != "view" && field.disabled == false>
			<div style="padding-bottom: 2em;" >
					 <div id="${fieldHtmlId}-autocomplete" class="ac-body" style="${style}">
					 <span id="${fieldHtmlId}-toggle-autocomplete" class="ac-toogle"></span>
					  <#if field.endpointMany>
					  		<span id="${controlId}-basket" class="viewmode-value current-values"></span>										
					  </#if>
				      <input id="${fieldHtmlId}" type="text" name="-" tabindex="0"
				             <#if field.description?exists>title="${field.description}"</#if>
				             <#if field.control.params.maxLength?exists>maxlength="${field.control.params.maxLength}"</#if> 
				             <#if field.control.params.size?exists>size="${field.control.params.size}"</#if> 
				             <#if field.disabled>disabled="true"</#if> 
				             class="yui-ac-input" >
				       </input>
			        </div>			
			      <div id="${fieldHtmlId}-container"></div>
			 	
			 
		         <input type="hidden" id="${controlId}-orig" name="-" value="${field.value?html}" />
		         <input type="hidden" id="${controlId}-added" name="${field.name}_added" />
		         <input type="hidden" id="${controlId}-removed" name="${field.name}_removed" />
			</div>
			<#else>

				<input id="${fieldHtmlId}" type="text" name="-" tabindex="0"
			             <#if field.description?exists>title="${field.description}"</#if>
			             <#if field.control.params.maxLength?exists>maxlength="${field.control.params.maxLength}"</#if> 
			             <#if field.control.params.size?exists>size="${field.control.params.size}"</#if> 
			             <#if field.disabled>disabled="true"</#if> 
			             class="${styleClass}"
			             style="${style}; position:relative;"
			              >	
			             
         </#if>
      </div>
   </#if>
</div>

<script type="text/javascript">//<![CDATA[

(function()
{

 var bAC = new beCPG.component.AutoCompletePicker('${controlId}', '${fieldHtmlId}', true).setOptions(
   {
 		currentValue: "${field.value}",
 		mode: "${form.mode}",
        multipleSelectMode: ${field.endpointMany?string}, 
 		targetLinkTemplate: "${targetLinkTemplate}" ,
 		dsStr:"${ds}"
<#if field.control.params.parent?exists>
<#assign parentFieldHtmlId=args.htmlid + "_prop_" + field.control.params.parent >
 		,parentFieldHtmlId:"${parentFieldHtmlId}"
</#if>
  });

<#if form.mode != "view">
YAHOO.util.Event.onAvailable("${fieldHtmlId}-container",function(){
	bAC.render();
});
<#else>
YAHOO.util.Event.onAvailable("${fieldHtmlId}-values",function(){
	bAC.render();
});
</#if>


})();

//]]></script>