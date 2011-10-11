<#if field.control.params.ds?exists><#assign ds=field.control.params.ds><#else><#assign ds=''></#if>
<#if field.control.params.prevFieldName?exists><#assign prevFieldName=field.control.params.prevFieldName><#else><#assign prevFieldName=''></#if>
<#if field.control.params.width?exists><#assign width=field.control.params.width><#else><#assign width='30em'></#if>


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
      <div id="${fieldHtmlId}-autocomplete" style="width: ${width}; padding-bottom: 2em;">
        <span id="${fieldHtmlId}-toggle-autocomplete"></span><input id="${fieldHtmlId}" type="text" name="${field.name}" tabindex="0"
                <#if field.control.params.styleClass?exists>class="${field.control.params.styleClass}"</#if>
                <#if field.value?is_number>value="${field.value?c}"<#else>value="${field.value?html}"</#if>
                <#if field.description?exists>title="${field.description}"</#if>
                <#if field.control.params.maxLength?exists>maxlength="${field.control.params.maxLength}"</#if> 
                <#if field.control.params.size?exists>size="${field.control.params.size}"</#if> 
                <#if field.disabled>disabled="true"</#if> />
         <div id="${fieldHtmlId}-container"></div>
      </div>
   </#if>
</div>


<script type="text/javascript">//<![CDATA[


(function()
{
 var Dom = YAHOO.util.Dom,
      Event = YAHOO.util.Event,
      Lang = YAHOO.util.Lang;

//Event.onDOMReady due to Popup
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
