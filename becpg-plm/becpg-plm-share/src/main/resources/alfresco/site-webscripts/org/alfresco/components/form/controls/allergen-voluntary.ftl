<#assign isTrue=false>
<#assign isFalse=false>
<#if field.value??>
 <#if field.value?is_boolean>
    <#assign isTrue=field.value>
    <#assign isFalse=!field.value>
 <#elseif field.value?is_string && field.value == "true">
    <#assign isTrue=true>
  <#elseif field.value?is_string && field.value == "false">
    <#assign isFalse=true>
 </#if>
</#if>

<div class="form-field">
   <#if form.mode == "view">
      <div class="viewmode-field">
         <span class="viewmode-label">${field.label?html}:</span>
         <span class="viewmode-value"><#if isTrue>${msg("data.boolean.true")}<#elseif isFalse>${msg("data.boolean.false")}<#else>${msg("data.boolean.empty")}</#if></span>
      </div>
   <#else>
        <label>${field.label?html}:</label>
        <div class="form-field-boolean" id="${fieldHtmlId}" >
		 
		    <input type="radio" id="${fieldHtmlId}-true" name="${field.name}" tabindex="0" value="true" 
		        <#if field.description??>title="${field.description}"</#if>
		        <#if field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>disabled="true"</#if>
		        <#if isTrue>checked="checked"</#if> > 
		     <label for="${fieldHtmlId}-true"> ${msg("data.boolean.true")} </label>
		   
	
		    <input type="radio" id="${fieldHtmlId}-false"  name="${field.name}" tabindex="0"  value="false" 
		    <#if field.description??>title="${field.description}"</#if>
		    <#if field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>disabled="true"</#if> 
		    <#if isFalse> checked="checked"</#if> > 
		    <label for="${fieldHtmlId}-false"> ${msg("data.boolean.false")} </label>
		    
		          
		    <input type="radio" id="${fieldHtmlId}-empty"  name="${field.name}"   tabindex="0" value="" 
		    <#if field.description??>title="${field.description}"</#if>
		    <#if field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>disabled="true"</#if> 
		    <#if !isTrue && !isFalse>checked="checked"</#if>  >
		     <label for="${fieldHtmlId}-empty"> ${msg("data.boolean.empty")} </label>
		
		</div>
        <@formLib.renderFieldHelp field=field />
		
      <script type="text/javascript">
          (function(){
				function allergenVoluntaryToogleVisible(){
					var val = YAHOO.util.Dom.get("${fieldHtmlId}-true").checked;
					if(YAHOO.util.Dom.get("${args.htmlid}_prop_bcpg_allergenListDecisionTree")){
						if(val){
							YAHOO.util.Dom.addClass(YAHOO.util.Dom.get("${args.htmlid}_prop_bcpg_allergenListDecisionTree-cntrl").parentNode,"hidden");
							YAHOO.util.Dom.get("${args.htmlid}_prop_bcpg_allergenListDecisionTree").value = "";
						    YAHOO.util.Dom.addClass(YAHOO.util.Dom.get("${args.htmlid}_prop_bcpg_allergenListInVoluntary").parentNode,"hidden");
							YAHOO.util.Dom.get("${args.htmlid}_prop_bcpg_allergenListInVoluntary-false").checked = true;
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
						YAHOO.util.Event.addListener("${fieldHtmlId}-true", "change", function(){
			               allergenVoluntaryToogleVisible();
			         	});
			            YAHOO.util.Event.addListener("${fieldHtmlId}-false", "change", function(){
			               allergenVoluntaryToogleVisible();
			         	});
			         	YAHOO.util.Event.addListener("${fieldHtmlId}-empty", "change", function(){
			               allergenVoluntaryToogleVisible();
			         	});
	         	}, this);
         	})();
         </script>
   </#if>
</div>



