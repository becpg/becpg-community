<#if field.control.params.format??><#assign format=field.control.params.format><#else><#assign format="0.####"></#if>

<#if field.control.params.unit??>
  <#assign unit=field.control.params.unit>
  <#assign currUnit=field.control.params.unit> 
<#else>
   <#assign unit="kg">
   <#assign currUnit="kg">
</#if>
<#assign currValue=field.value>
 <#if unit=="perc">
  	<#assign currUnits="ppm,perc">
   <#elseif unit=="kg">
	<#assign currUnits="mg,g,kg">
   <#elseif unit=="d">
	<#assign currUnits="d,mo">
 </#if>

<#if field.value?is_number>
   <#if unit=="perc">
     <#if field.value &lt; 0.1  >
		<#assign currUnit="ppm">
		<#assign currValue=field.value*10000>
     </#if>
   <#elseif unit=="kg">
	  <#if field.value &lt; 0.001  >
		<#assign currUnit="mg">
		<#assign currValue=field.value*1000000>
     <#elseif field.value &lt; 1  >
		<#assign currUnit="g" >
		<#assign currValue=field.value*1000 >
	 </#if>
   <#elseif unit=="d">
		<#if field.value/30 &gt; 1  >
			<#assign currUnit="mo">
			<#assign currValue=field.value/30 >
		</#if>
    </#if>
</#if>

<#assign currLabel = field.label?replace("\\(.*\\)","("+msg("becpg.forms.unit."+currUnit)+")","r")>

<div class="form-field">
	<#if field.dataKeyName?? && field.dataType??>
	   <#if form.mode == "view">
	      <div class="viewmode-field">
	         <#if field.mandatory && !(field.value?is_number) && field.value == "">
	            <span class="incomplete-warning"><img class="icon16" src="${url.context}/res/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
	         </#if>
	         <span class="viewmode-label">${currLabel?html}:</span>
	         <span class="viewmode-value"><#if currValue?is_number>${currValue?string("${format}")}<#elseif field.value == "">${msg("form.control.novalue")}<#else>${currValue?html}</#if></span>
	      </div>
	   <#else>
	      <label id="${fieldHtmlId}-label"  for="${fieldHtmlId}">${currLabel?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
	      <input id="${fieldHtmlId}" type="text" name="-" tabindex="0"
	             class="number<#if field.control.params.styleClass??> ${field.control.params.styleClass}</#if>"
	             <#if field.control.params.style??>style="${field.control.params.style}"</#if>	
	             <#if field.value?is_number>value="${currValue?c}"<#else>value="${currValue?html}"</#if>
	             <#if field.description??>title="${field.description}"</#if>
	             <#if field.control.params.maxLength??>maxlength="${field.control.params.maxLength}"</#if> 
	             <#if field.control.params.size??>size="${field.control.params.size}"</#if> 
	             <#if field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>disabled="true"</#if> />
	      <input  id="${fieldHtmlId}-val"  name="${field.name}" type="hidden"  <#if field.value?is_number>value="${field.value?c}"<#else>value="${field.value?html}"</#if>>       
	      <select id="${fieldHtmlId}-unit" name="-" tabindex="0" >
	               <#list currUnits?split(",") as nameValue>
	                  <option value="${nameValue?html}"<#if nameValue == currUnit?string> selected="selected"</#if>>${msg("becpg.forms.unit."+nameValue)}</option>
	               </#list>
	        </select>
	        <script type="text/javascript">//<![CDATA[
			(function()
			{
	         	YAHOO.Bubbling.on("beforeFormRuntimeInit", function (layer, args) {	
	         	
	         			var updateVal = function (){
	         				var unit  = YAHOO.util.Dom.get("${fieldHtmlId}-unit").value;
				         	var val = YAHOO.util.Dom.get("${fieldHtmlId}").value;
				         	if(val!=null){
				         	  if(unit == "mo"){
				         	    val = val * 30;
				         	  } else if(unit == "ppm"){
				         	   val = val / 10000;
				         	  } else if(unit == "g"){
				         	    val = val / 1000;
				         	  } else if(unit == "mg"){
				         	    val = val / 1000000;
				         	  }
				         	}
				    
				         	YAHOO.util.Dom.get("${fieldHtmlId}-val").value = val;
				         	YAHOO.util.Dom.get("${fieldHtmlId}-label").innerHTML =
				         	    YAHOO.util.Dom.get("${fieldHtmlId}-label").innerHTML.replace(/\(.*\)/g,"("+unit+")");
				         	
	         			};
	         	
			         	YAHOO.util.Event.addListener("${fieldHtmlId}-unit", "change", updateVal);
			         	YAHOO.util.Event.addListener("${fieldHtmlId}", "change", updateVal);
			         	
	         	}, this);
	         	})();
			//]]></script>
			<@formLib.renderFieldHelp field=field />

	   </#if>
   </#if>
</div>
