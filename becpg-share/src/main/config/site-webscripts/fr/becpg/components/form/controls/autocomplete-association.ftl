<#if field.control.params.ds?exists><#assign ds=field.control.params.ds><#else><#if field.dataType??><#assign ds='becpg/autocomplete/targetassoc/associations/${field.dataType}'></#if></#if>
<#if field.control.params.style?exists><#assign style=field.control.params.style><#else><#assign style='width:27em;'></#if>
<#if field.control.params.styleClass?exists><#assign styleClass=field.control.params.styleClass><#else><#assign styleClass='yui-ac-input'></#if>
<#assign siteId=args.siteId!"">
<#if siteId != ""><#assign targetLinkTemplate='${url.context}/page/site/${siteId}/document-details?nodeRef={nodeRef}'><#else><#assign targetLinkTemplate='${url.context}/page/document-details?nodeRef={nodeRef}'></#if>

<#assign controlId = fieldHtmlId + "-cntrl">


<div class="form-field">
   <#if field.dataKeyName?? && field.dataType??>
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
			
					<!-- basket -->
					<#if field.endpointMany>
						<span id="${controlId}-basket" class="viewmode-value current-values"></span>										
						<span class="remove-icon">
							<img src="${url.context}/res/components/form/images/remove-icon-16.png" title="${msg("form.field.remove")}" 
								 onclick="javascript:beCPG.util.removeAutoCompleteSelection('${fieldHtmlId}');" />
						</span>
					</#if>
	
					<!-- apply autocomplete style -->			
					<div id="${fieldHtmlId}-autocomplete" style='padding-bottom: 1em;' class="yui-ac">
					  <span id="${fieldHtmlId}-toggle-autocomplete"></span><input id="${fieldHtmlId}" type="text" name="-" tabindex="0"
					         <#if field.description?exists>title="${field.description}"</#if>
					         <#if field.control.params.maxLength?exists>maxlength="${field.control.params.maxLength}"</#if> 
					         <#if field.control.params.size?exists>size="${field.control.params.size}"</#if> 
					         <#if field.disabled>disabled="true"</#if> 
					         class="${styleClass}"
					         style="${style}; position:relative;"
					         <#if field.value != "">value="-"</#if> >				
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
					         <#if field.value != "">value="-"</#if> >	
					         
		     </#if>
		  </div>
	   </#if>
   </#if>
</div>

<script type="text/javascript">//<![CDATA[


(function()
{

 new beCPG.component.AutoCompletePicker('${controlId}', '${fieldHtmlId}').setOptions(
   {
 		currentValue: "${field.value}",
 		mode: "${form.mode}",
        multipleSelectMode: ${field.endpointMany?string},
		targetLinkTemplate: "${targetLinkTemplate}"
   });

	
 var Dom = YAHOO.util.Dom,
      Event = YAHOO.util.Event,
      Lang = YAHOO.util.Lang;

setTimeout(function(){

	var dsStr = '${ds}';	

   // Use an XHRDataSource   
   var oDS = new YAHOO.util.XHRDataSource(Alfresco.constants.PROXY_URI + dsStr);  
   
   // Set the responseType
   oDS.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
 
   // Define the schema of the JSON results
   oDS.responseSchema = 
   {
      resultsList : "result",
      fields : ["value", "name","type"]
   };
   

   // Instantiate the AutoComplete
   var oAC = new YAHOO.widget.AutoComplete("${fieldHtmlId}", "${fieldHtmlId}-container", oDS);
   
   oAC.autoHighlight = true;
   oAC.allowBrowserAutocomplete = false;
   oAC.queryDelay = .5;
   
   var oParentField = '';	
<#if field.control.params.parent?exists>

	<#assign parentFieldHtmlId=args.htmlid + "_prop_" + field.control.params.parent >
	var parentElem = Dom.get("${parentFieldHtmlId}");	
	if(parentElem != null){
			oParentField = parentElem.value;
	}	
	
	//Callback for parent select change event.
	Event.addListener(parentElem, "change", function(e) {		
	   oParentField = e.target.value;			
	} );			
</#if>
   


   // The webservice needs additional parameters
   oAC.generateRequest = function(sQuery) 
   {
   		<#if field.endpointMany>
			if(sQuery.indexOf("%2C%20") > 0)
			{
				var arrQuery = sQuery.split("%2C%20");
				sQuery = arrQuery[arrQuery.length - 1];
			}
		</#if>
   
   
	 var q = Lang.substitute("q={query}&parent={parent}",{query:sQuery,parent:oParentField});
	
      <#if ds?contains("?")>
         return "&" + q;
      <#else>
         return "?" + q;
      </#if>
   };
   
  
   oAC.formatResult = function(oResultData, sQuery, sResultMatch) {
   		return "<span class='"+oResultData[2]+"' style='padding-left: 20px;' >"+oResultData[1]+"</span>";
   }
   
    // Toggle button 
	var bToggler = Dom.get("${fieldHtmlId}-toggle-autocomplete"); 
	var oPushButtonB = new YAHOO.widget.Button({container:bToggler}); 
	var toggleB = function(e) { 
	 //Event.stopEvent(e); 
	 if(!Dom.hasClass(bToggler, "openToggle")) { 
	     Dom.addClass(bToggler, "openToggle") 
	  } 
	 // Is open 
	 if(oAC.isContainerOpen()) { 
	         oAC.collapseContainer(); 
	  } 
	  // Is closed 
	   else { 
	     oAC.getInputEl().focus(); // Needed to keep widget active 
	     setTimeout(function() { // For IE 
	     oAC.sendQuery("*"); 
	      },0); 
	    } 
	  } 
	  oPushButtonB.on("click", toggleB); 
	 
	  oAC.containerCollapseEvent.subscribe(function(){Dom.removeClass(bToggler, "openToggle")}); 

},500);

})();
</script>