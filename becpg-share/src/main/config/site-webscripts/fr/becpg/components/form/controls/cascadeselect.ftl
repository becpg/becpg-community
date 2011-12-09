<#if field.control.params.ds?exists><#assign ds=field.control.params.ds><#else><#assign ds=''></#if>

<div class="form-field">
   <#if form.mode == "view">
      <div class="viewmode-field">
         <#if field.mandatory && field.value == "">
            <span class="incomplete-warning"><img src="${url.context}/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
         </#if>
         <span class="viewmode-label">${field.label?html}:</span>
         <span class="viewmode-value">${field.value?html}</span>
      </div>
   <#else>
      <label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
	  <select id="${fieldHtmlId}" name="${field.name}" 
		   <#if field.description?exists>title="${field.description}"</#if>
		   <#if field.control.params.size?exists>size="${field.control.params.size}"</#if> 
		   <#if field.control.params.styleClass?exists>class="${field.control.params.styleClass}"</#if>
		   <#if field.disabled>disabled="true"</#if>>
	  </select>
   </#if>
</div>

<script type="text/javascript">

//create a new custom event, to be fired
var onOptionsPopulated${field.id} = new YAHOO.util.CustomEvent("onOptionsPopulated");

(function() {
     
	var callbackSelector =
	{
	  success: function(o){
				var responseJson = eval("("+o.responseText+")");
				var selectElem = YAHOO.util.Dom.get("${fieldHtmlId}");		
				// Populate the options
				selectElem.options.length=0;		
				for ( var i = 0 ; i < responseJson.length ; i ++ ) {		
					if ( responseJson[i] == '${field.value}' ) {			
						selectElem.options[i] = new Option(responseJson[i], responseJson[i], false, true);				
					} else {			
						selectElem.options[i] = new Option(responseJson[i], responseJson[i], false, false);			
					}					
				}		
				onOptionsPopulated${field.id}.fire({value:'${field.value}'});		
			   },
	  failure: function(o){},
	  argument: {}
	} 

	var dsStr = '${ds}';
	
	var parentVal = '';
	
	<#if field.control.params.parent?exists>
		<#assign parentFieldHtmlId=args.htmlid + "_prop_" + field.control.params.parent >
		var parentElem = YAHOO.util.Dom.get("${parentFieldHtmlId}");		
		parentVal = parentElem.value;
		dsStr = dsStr.replace('{parent}',parentVal);
	</#if>
	
	<#if field.control.params.parent?exists>
		if (parentVal != null && parentVal != "") {
			var request  = YAHOO.util.Connect.asyncRequest('GET', dsStr, callbackSelector);
		}
	<#else>	
			var request  = YAHOO.util.Connect.asyncRequest('GET', dsStr, callbackSelector);
	</#if>

})();

<#if field.control.params.parent?exists>

(function() {
	var callbackSelector =
	{
	  success: function(o){
					var responseJson = eval("("+o.responseText+")");
					var selectElem = YAHOO.util.Dom.get("${fieldHtmlId}");
					// Populate the options
					selectElem.options.length=0;
					for ( var i = 0 ; i < responseJson.length ; i ++ ) {
						if ( responseJson[i] == '${field.value}' ) {
							selectElem.options[i] = new Option(responseJson[i], responseJson[i], false, true);
						} else {
							selectElem.options[i] = new Option(responseJson[i], responseJson[i], false, false);
						}
					}
				},
	  failure: function(o){},
	  argument: {}
	} 

	//Callback for parent select change event.
	function onChangeValueCallback(e) {	
		var dsStr = '${ds}';
		var parentVal = '';
		dsStr = dsStr.replace('{parent}',e.target.value);
		var request  = YAHOO.util.Connect.asyncRequest('GET', dsStr, callbackSelector);
	} 
	
	//Callback for custom event which is fired once the options for parent select get populated.	
	function onParentPopulated (type, args) {
		var dsStr = '${ds}';
		var parentVal = '';
		dsStr = dsStr.replace('{parent}',args[0].value);
		var request  = YAHOO.util.Connect.asyncRequest('GET', dsStr, callbackSelector);
    }

	<#assign parentFieldHtmlId=args.htmlid + "_prop_" + field.control.params.parent >
	var parentElem = YAHOO.util.Dom.get("${parentFieldHtmlId}");		
	//Add Listner for parent select change event.
	YAHOO.util.Event.addListener(parentElem, "change", onChangeValueCallback);		
	//Add Listner for custom event which is fired once the options for parent select get populated.	
	onOptionsPopulatedprop_${field.control.params.parent}.subscribe(onParentPopulated);	
})();	
</#if>

</script>