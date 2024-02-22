<#if field.control.params.format??><#assign format=field.control.params.format><#else><#assign format="0.####"></#if>


<#if field.control.params.unit??>
  <#assign unit=field.control.params.unit>
  <#assign currUnit=field.control.params.unit> 
<#else>
   <#assign unit="kg">
   <#assign currUnit="kg">
</#if>


<#if locale="en_US">
 <#if unit=="kg">
    <#assign currUnit="lb">
 <#elseif unit=="g">
    <#assign currUnit="oz">
 <#elseif unit=="ml">
    <#assign currUnit="cup">
 <#elseif unit=="L">
    <#assign currUnit="gal">
  <#elseif unit=="m">
    <#assign currUnit="ft">
  <#elseif unit=="mm">
    <#assign currUnit="in">
 </#if>
</#if>


<#assign currValue=field.value>
 	<#if unit=="perc" || unit=="ppm">
  	 <#assign currUnits="perc,ppm">
   <#elseif unit=="kg" >
	<#assign currUnits="mg,g,kg,lb,oz">
   <#elseif unit=="g" >
     <#assign currUnits="g,oz">
   <#elseif unit=="L" >
	<#assign currUnits="mL,cL,L,fl_oz,cp,gal">
   <#elseif unit=="m" || unit=="mm" >	
	<#assign currUnits="mil,in,ft,mm,cm,m">
   <#elseif unit=="d" ||  unit=="mo" || unit=="y">
	<#assign currUnits="d,mo,y">
   <#elseif unit=="-">
	<#assign currUnits="-,mega,milli,micro">
 </#if>

<#if field.value?is_number>
   <#if currUnit=="perc" || currUnit=="ppm">
	 <#if field.value == 0  >
        <#assign currUnit="perc">
     <#elseif field.value?abs &lt; 0.01  >
		<#assign currUnit="ppm">
		<#assign currValue=field.value*10000>
     </#if>
   <#elseif currUnit=="kg">
	  <#if field.value == 0  >
        <#assign currUnit="kg">
     <#elseif field.value?abs &lt; 0.001  >
		<#assign currUnit="mg">
		<#assign currValue=field.value*1000000>
     <#elseif field.value?abs &lt; 1  >
		<#assign currUnit="g" >
		<#assign currValue=field.value*1000 >
	 </#if>
   <#elseif currUnit=="lb">
	 <#assign currValue=field.value*2.204622622 >
	 <#if currValue == 0  >
        <#assign currUnit="lb">
     <#elseif currValue?abs &lt; 1  >
		<#assign currUnit="oz" >
		<#assign currValue=field.value*35.27396195 >
	 </#if>
    <#elseif currUnit=="gal">
	 <#assign currValue=field.value*0.264172 >
	 <#if currValue == 0  >
        <#assign currUnit="gal">
     <#elseif currValue?abs &lt; 1  >
		<#assign currUnit="fl_oz" >
		<#assign currValue=field.value*33.814 >
	 </#if> 
     <#elseif currUnit=="m">
	  <#if field.value == 0  >
        <#assign currUnit="m">
     <#elseif field.value?abs &lt; 1  >
		<#assign currUnit="mm" >
		<#assign currValue=field.value*1000 >
  	 </#if>
      <#elseif currUnit=="ft">
	 <#assign currValue=field.value* 3.28084 >
	 <#if currValue == 0  >
        <#assign currUnit="ft">
     <#elseif currValue?abs &lt; 1  >
		<#assign currUnit="in" >
		<#assign currValue=field.value*39.37008 >
	 </#if> 
	 <#elseif currUnit=="in">
	 <#assign currValue=field.value/25.4 >
	 <#if currValue == 0  >
        <#assign currUnit="in">
     <#elseif currValue?abs &lt; 1  >
		<#assign currUnit="mil" >
		<#assign currValue=field.value*39.37008 >
	 </#if>
   <#elseif currUnit=="L">
	  <#if field.value == 0  >
        <#assign currUnit="L">
     <#elseif field.value?abs &lt; 1  >
		<#assign currUnit="mL" >
		<#assign currValue=field.value*1000 >
  	 </#if>
   <#elseif currUnit=="d" || currUnit=="mo" || currUnit=="y">
		<#if field.value == 0  >
       		 <#assign currUnit="d">
		<#elseif field.value &gt; 364 && field.value%365 == 0>
			<#assign currUnit="y">
			<#assign currValue=field.value/365 >	
	   	<#elseif field.value/30 &gt; 1 && field.value%30 == 0>
			<#assign currUnit="mo">
			<#assign currValue=field.value/30 >
		<#else>
		  	 <#assign currUnit="d">
		</#if>
    <#elseif currUnit=="-">
		 <#if field.value == 0  >
       		 <#assign currUnit="-">
     	 <#elseif field.value?abs &lt; 0.001  >
			<#assign currUnit="micro">
			<#assign currValue=field.value*1000000>
	     <#elseif field.value?abs &lt; 1  >
			<#assign currUnit="milli" >
			<#assign currValue=field.value*1000 >
		 <#elseif field.value?abs &gt;= 1000000  >
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
	      <input id="${fieldHtmlId}" type="text" name="-" tabindex="0" autocomplete="off"
	             class="number number-unit<#if field.control.params.styleClass??> ${field.control.params.styleClass}</#if>"
	             <#if field.control.params.style??>style="${field.control.params.style}"</#if>	
	             <#if field.value?is_number>value="${currValue?c}"<#else>value="${currValue?html}"</#if>
	             <#if field.description??>title="${field.description}"</#if>
	             <#if field.control.params.maxLength??>maxlength="${field.control.params.maxLength}"</#if> 
	             <#if field.control.params.size??>size="${field.control.params.size}"</#if> 
	             <#if field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>disabled="true"</#if> />
				 <#if !field.disabled || (field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>	             
	        <select id="${fieldHtmlId}-unit" name="-" tabindex="0" class="number-unit">
	               <#list currUnits?split(",") as nameValue>
	                  <option value="${nameValue?html}"<#if nameValue == currUnit?string> selected="selected"</#if>>${msg("becpg.forms.unit."+nameValue?replace("-","empty"))}</option>
	               </#list>
	        </select>
	        	</#if>
	        <input  id="${fieldHtmlId}-val"  name="${field.name}" type="hidden"  <#if field.value?is_number>value="${field.value?c}"<#else>value="${field.value?html}"</#if> /> 
	        <script type="text/javascript">//<![CDATA[
			(function()
			{
	         	YAHOO.Bubbling.on("beforeFormRuntimeInit", function (layer, args) {	
	         	
	         			var updateVal = function (){

							var sel = YAHOO.util.Dom.get("${fieldHtmlId}-unit");
				         	YAHOO.util.Dom.get("${fieldHtmlId}-val").value = 
				         		beCPG.util.convertUnit(YAHOO.util.Dom.get("${fieldHtmlId}").value, sel.value,"${unit}");
				         	YAHOO.util.Dom.get("${fieldHtmlId}-label").innerHTML =	
				         		YAHOO.util.Dom.get("${fieldHtmlId}-label").innerHTML.replace(/ *\([^)]*\) */g," ("+sel.options[sel.selectedIndex].innerHTML+")");
				         	return true;
	         			};
	         	
			         	YAHOO.util.Event.addListener("${fieldHtmlId}-unit", "change", updateVal);
			         	YAHOO.util.Event.addListener("${fieldHtmlId}", "change", updateVal);
			         	
			         	YAHOO.util.Event.addListener("${fieldHtmlId}", "keypress", function(e){
						    if (e.keyCode == 13){
						        updateVal();
						    }
						 });

	         	}, this);
	         	})();
			//]]></script>
			<@formLib.renderFieldHelp field=field />

	   </#if>
   </#if>
</div>
