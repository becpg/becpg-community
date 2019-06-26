<#assign isTrue=false>
<#if field.value??>
 <#if field.value?is_boolean>
    <#assign isTrue=field.value>
 <#elseif field.value?is_string && field.value == "true">
    <#assign isTrue=true>
 </#if>
</#if>

<div class="form-field">
   <#if form.mode == "view">
      <div class="viewmode-field">
         <span class="viewmode-label">${field.label?html}:</span>
         <span class="viewmode-value"><#if isTrue>${msg("form.control.checkbox.yes")}<#else>${msg("form.control.checkbox.no")}</#if></span>
      </div>
   <#else>
      <input id="${fieldHtmlId}" type="hidden" name="${field.name}" value="<#if isTrue>true<#else>false</#if>" />
      <input class="formsCheckBox" id="${fieldHtmlId}-entry" type="checkbox" tabindex="0" name="-" <#if field.description??>title="${field.description}"</#if>
             <#if isTrue> value="true" checked="checked"</#if> 
             <#if field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>disabled="true"</#if> 
             <#if field.control.params.styleClass??>class="${field.control.params.styleClass}"</#if>
             <#if field.control.params.style??>style="${field.control.params.style}"</#if>  />
      <label for="${fieldHtmlId}-entry" class="checkbox">${field.label?html}</label>
      <script type="text/javascript">
			function allergenVoluntaryToogleVisible(){
			
			var val = YAHOO.util.Dom.get("${fieldHtmlId}-entry").checked;
			YAHOO.util.Dom.get("${fieldHtmlId}").value=val;
			if(YAHOO.util.Dom.get("${args.htmlid}_prop_bcpg_allergenListDecisionTree")){
				if(val){
					YAHOO.util.Dom.addClass(YAHOO.util.Dom.get("${args.htmlid}_prop_bcpg_allergenListDecisionTree-cntrl").parentNode,"hidden");
					YAHOO.util.Dom.get("${args.htmlid}_prop_bcpg_allergenListDecisionTree").value = "";
				    YAHOO.util.Dom.addClass(YAHOO.util.Dom.get("${args.htmlid}_prop_bcpg_allergenListInVoluntary").parentNode,"hidden");
					YAHOO.util.Dom.get("${args.htmlid}_prop_bcpg_allergenListInVoluntary").value = false;
					YAHOO.util.Dom.get("${args.htmlid}_prop_bcpg_allergenListInVoluntary-entry").checked = false;
				} else {
				 if(YAHOO.util.Dom.hasClass(YAHOO.util.Dom.get("${args.htmlid}_prop_bcpg_allergenListDecisionTree-cntrl").parentNode,"hidden")){
				   YAHOO.util.Dom.removeClass(YAHOO.util.Dom.get("${args.htmlid}_prop_bcpg_allergenListDecisionTree-cntrl").parentNode,"hidden");
	 			   YAHOO.util.Dom.removeClass(YAHOO.util.Dom.get("${args.htmlid}_prop_bcpg_allergenListInVoluntary").parentNode,"hidden");
	 			   YAHOO.Bubbling.fire("refreshDecisionTree");	
	 			  }
				}
			}	
			
           }
           YAHOO.Bubbling.on("beforeFormRuntimeInit", function (layer, args)
     			 {	
		            allergenVoluntaryToogleVisible();
					YAHOO.util.Event.addListener("${fieldHtmlId}-entry", "change", function(){
		               allergenVoluntaryToogleVisible();
		         	});
         	}, this);
         </script>
      <@formLib.renderFieldHelp field=field />
   </#if>
</div>



