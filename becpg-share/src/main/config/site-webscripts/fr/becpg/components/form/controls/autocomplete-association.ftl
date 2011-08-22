<#if field.control.params.ds?exists><#assign ds=field.control.params.ds><#else><#assign ds='becpg/autocomplete/targetassoc/associations/${field.dataType}'></#if>
<#if field.control.params.style?exists><#assign style=field.control.params.style><#else><#assign style='width:30em;'></#if>
<#if field.control.params.styleClass?exists><#assign styleClass=field.control.params.styleClass><#else><#assign styleClass='yui-ac-input'></#if>
<#assign siteId=args.siteId!"">
<#if siteId != ""><#assign targetLinkTemplate='${url.context}/page/site/${siteId}/document-details?nodeRef={nodeRef}'><#else><#assign targetLinkTemplate='${url.context}/page/document-details?nodeRef={nodeRef}'></#if>

<#assign controlId = fieldHtmlId + "-cntrl">

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
})();
//]]></script>

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
			      <input id="${fieldHtmlId}" type="text" name="-" tabindex="0"
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
         </#if>
      </div>
   </#if>
</div>


<script type="text/javascript">//<![CDATA[

//variable used to update url of webservice
var oAC${field.id} = null;

function attachDataSource${field.id}()
{
	var dsStr = '${ds}';	

   //parent
   var sParent = '';	
	<#if field.control.params.parent?exists>
		<#assign parentFieldHtmlId=args.htmlid + "_prop_" + field.control.params.parent >
		var parentElem = YAHOO.util.Dom.get("${parentFieldHtmlId}");
		if(parentElem != null)				
			sParent = parentElem.value;
	</#if>

   // Use an XHRDataSource   
   var oDS = new YAHOO.util.XHRDataSource(Alfresco.constants.PROXY_URI + dsStr);  
   
   // Set the responseType
   oDS.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
   
   // Define the schema of the JSON results
   oDS.responseSchema = 
   {
      resultsList : "result",
      fields : ["value", "name"]
   };

   // Instantiate the AutoComplete
   var oAC = new YAHOO.widget.AutoComplete("${fieldHtmlId}", "${fieldHtmlId}-container", oDS);
	oAC._bFocused = true;
   
   // Throttle requests sent
   oAC.queryDelay = .5;

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

      <#if ds?contains("?")>
         return "&q=" + sQuery + "&parent=" + sParent ;
      <#else>
         return "?q=" + sQuery + "&parent=" + sParent ;
      </#if>
   };
   
   oAC${field.id} = oAC;   
   
   return {
      oDS: oDS,
      oAC: oAC
   };
}

//TODO à mettre dans AutoCompletePicker
setTimeout(function()
{
   attachDataSource${field.id}();
}, 500);

<#if field.control.params.parent?exists>

(function() {

	//Callback for parent select change event.
	function onChangeValueCallback(e) {		
		
		var sParent = e.target.value;			
		oAC${field.id}.generateRequest = function(sQuery) 
	   {
	      <#if ds?contains("?")>
	         return "&q=" + sQuery + "&parent=" + sParent ;
	      <#else>
	         return "?q=" + sQuery + "&parent=" + sParent ;
	      </#if>
	   };
	} 

	<#assign parentFieldHtmlId=args.htmlid + "_prop_" + field.control.params.parent >
	var parentElem = YAHOO.util.Dom.get("${parentFieldHtmlId}");		

	//Add Listner for parent select change event.
	YAHOO.util.Event.addListener(parentElem, "change", onChangeValueCallback);			
})();	
</#if>

</script>