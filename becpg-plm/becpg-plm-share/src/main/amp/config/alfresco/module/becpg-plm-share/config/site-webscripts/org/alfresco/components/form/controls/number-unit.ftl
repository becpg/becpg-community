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
  	<#assign currUnits="perc,pp,ppm">
   <#elseif unit=="kg">
	<#assign currUnits="mg,g,kg">
   <#elseif unit=="L">
	<#assign currUnits="mL,L">
   <#elseif unit=="d">
	<#assign currUnits="d,mo">
   <#elseif unit=="-">
	<#assign currUnits="-,mega,milli,micro">
 </#if>

<#if field.value?is_number>
   <#if unit=="perc">
     <#if field.value &lt; 0.1  >
		<#assign currUnit="ppm">
		<#assign currValue=field.value*10000>
     <#elseif field.value &lt; 1  >
		<#assign currUnit="pp" >
		<#assign currValue=field.value*10 >
	 </#if>
   <#elseif unit=="kg">
	  <#if field.value &lt; 0.001  >
		<#assign currUnit="mg">
		<#assign currValue=field.value*1000000>
     <#elseif field.value &lt; 1  >
		<#assign currUnit="g" >
		<#assign currValue=field.value*1000 >
	 </#if>
   <#elseif unit=="L">
	  <#if field.value &lt; 1  >
		<#assign currUnit="mL" >
		<#assign currValue=field.value*1000 >
  	 </#if>
   <#elseif unit=="d">
		<#if field.value/30 &gt; 1  >
			<#assign currUnit="mo">
			<#assign currValue=field.value/30 >
		</#if>
    <#elseif unit=="-">
		 <#if field.value &lt; 0.001  >
			<#assign currUnit="micro">
			<#assign currValue=field.value*1000000>
	     <#elseif field.value &lt; 1  >
			<#assign currUnit="milli" >
			<#assign currValue=field.value*1000 >
		 <#elseif field.value &gt;= 1000000  >
			<#assign currUnit="mega" >
			<#assign currValue=field.value/1000000 >
		 </#if>
    </#if>
</#if>

<#assign currLabel = field.label?replace("\\(.*\\)","("+msg("becpg.forms.unit."+currUnit?replace("-","empty"))+")","r")>

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
	             class="number number-unit<#if field.control.params.styleClass??> ${field.control.params.styleClass}</#if>"
	             <#if field.control.params.style??>style="${field.control.params.style}"</#if>	
	             <#if field.value?is_number>value="${currValue?c}"<#else>value="${currValue?html}"</#if>
	             <#if field.description??>title="${field.description}"</#if>
	             <#if field.control.params.maxLength??>maxlength="${field.control.params.maxLength}"</#if> 
	             <#if field.control.params.size??>size="${field.control.params.size}"</#if> 
	             <#if field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>disabled="true"</#if> />
	        <select id="${fieldHtmlId}-unit" name="-" tabindex="0" class="number-unit">
	               <#list currUnits?split(",") as nameValue>
	                  <option value="${nameValue?html}"<#if nameValue == currUnit?string> selected="selected"</#if>>${msg("becpg.forms.unit."+nameValue?replace("-","empty"))}</option>
	               </#list>
	        </select>
	        <input  id="${fieldHtmlId}-val"  name="${field.name}" type="hidden"  <#if field.value?is_number>value="${field.value?c}"<#else>value="${field.value?html}"</#if>> 
	        <script type="text/javascript">//<![CDATA[
			(function()
			{
	         	YAHOO.Bubbling.on("beforeFormRuntimeInit", function (layer, args) {	
	         	
	         			var updateVal = function (){
	         			
	         				var sel =  YAHOO.util.Dom.get("${fieldHtmlId}-unit");
	         				var unit  = sel.value;
				         	var val = YAHOO.util.Dom.get("${fieldHtmlId}").value;
				         	if(val!=null && val!=""){
				         	  if(unit == "mo"){
				         	    val = val * 30;
				         	  } else if(unit == "ppm"){
				         	   val = val / 10000;
				         	  } else if(unit == "pp"){
				         	   val = val / 10;		         	   
				         	  } else if(unit == "g" || unit == "milli" || unit == "mL"){
				         	    val = val / 1000;
				         	  } else if(unit == "mg" || unit == "micro"){
				         	    val = val / 1000000;
				         	  } else if(unit == "mega"){
				         	    val = val * 1000000;
				         	  }
				         	}
				         	
				         	YAHOO.util.Dom.get("${fieldHtmlId}-val").value = val;
				         	YAHOO.util.Dom.get("${fieldHtmlId}-label").innerHTML =
				         	    YAHOO.util.Dom.get("${fieldHtmlId}-label").innerHTML.replace(/\(.*\)/g,"("+sel.options[sel.selectedIndex].innerHTML+")");
				         	
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
