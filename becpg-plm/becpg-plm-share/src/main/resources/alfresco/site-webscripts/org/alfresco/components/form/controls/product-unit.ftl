<#include "/org/alfresco/components/form/controls/common/utils.inc.ftl" />

<#if field.control.params.optionSeparator??>
   <#assign optionSeparator=field.control.params.optionSeparator>
<#else>
   <#assign optionSeparator=",">
</#if>
<#if field.control.params.labelSeparator??>
   <#assign labelSeparator=field.control.params.labelSeparator>
<#else>
   <#assign labelSeparator="|">
</#if>


<#assign fieldValue=field.value>

<#if fieldValue?string == "" && field.control.params.defaultValueContextProperty??>
   <#if context.properties[field.control.params.defaultValueContextProperty]??>
      <#assign fieldValue = context.properties[field.control.params.defaultValueContextProperty]>
   <#elseif args[field.control.params.defaultValueContextProperty]??>
      <#assign fieldValue = args[field.control.params.defaultValueContextProperty]>
   </#if>
</#if>

<#if field.control.params.isSearch?? && form.mode == "edit" || form.mode == "create" >
	<#if field.control.params.defaultValue ??>
		<#assign fieldValue=field.control.params.defaultValue>
	<#else>
		<#assign fieldValue="" />
	</#if>
</#if>


<div class="form-field">
   <#if form.mode == "view">
      <div class="viewmode-field">
         <#if field.mandatory && !(fieldValue?is_number) && fieldValue?string == "">
            <span class="incomplete-warning"><img class="icon16" src="${url.context}/res/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
         </#if>
         <span class="viewmode-label">${field.label?html}:</span>
         <#if fieldValue?string == "">
            <#assign valueToShow=msg("form.control.novalue")>
         <#else>
            <#assign valueToShow=fieldValue>
            <#if field.control.params.options?? && field.control.params.options != "">
               <#list field.control.params.options?split(optionSeparator) as nameValue>
                  <#if nameValue?index_of(labelSeparator) == -1>
                     <#if nameValue == fieldValue?string || (fieldValue?is_number && fieldValue?c == nameValue)>
                        <#assign valueToShow=nameValue>
                        <#break>
                     </#if>
                  <#else>
                     <#assign choice=nameValue?split(labelSeparator)>
                     <#if choice[0] == fieldValue?string || (fieldValue?is_number && fieldValue?c == choice[0])>
                        <#assign valueToShow=msg(choice[1])>
                        <#break>
                     </#if>
                  </#if>
               </#list>
            </#if>
         </#if>
         <span class="viewmode-value">${valueToShow?html}</span>
      </div>
   <#else>
      <label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
      <#if field.control.params.options?? && field.control.params.options != "">
         <select id="${fieldHtmlId}" name="${field.name}" tabindex="0"
               <#if field.description??>title="${field.description}"</#if>
               <#if field.indexTokenisationMode??>class="non-tokenised"</#if>
               <#if field.control.params.size??>size="${field.control.params.size}"</#if> 
               <#if field.control.params.styleClass??>class="${field.control.params.styleClass}"</#if>
               <#if field.control.params.style??>style="${field.control.params.style}"</#if>
               <#if field.disabled  && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>disabled="true"</#if>>
               	<#if field.control.params.insertBlank??>
               		 <option value="" <#if fieldValue?length &lt; 1 >selected="selected"</#if> ></option>
               	</#if>
               <#list field.control.params.options?split(optionSeparator) as nameValue>
                  <#if nameValue?index_of(labelSeparator) == -1>
                     <option value="<#if field.control.params.isSearch?? && nameValue?length &gt; 0 >=</#if><#if field.control.params.isSearch??>${nameValue?string?replace(" ","\\ ")?html}<#else>${nameValue?html}</#if>"<#if (nameValue == fieldValue?string || (fieldValue?is_number && fieldValue?c == nameValue)) > selected="selected"</#if>>${nameValue?html}</option>
                  <#else>
                     <#assign choice=nameValue?split(labelSeparator)>
                     <option value="<#if field.control.params.isSearch?? && choice[0]?length &gt; 0 >=</#if><#if field.control.params.isSearch??>${choice[0]?string?replace(" ","\\ ")?html}<#else>${choice[0]?html}</#if>"<#if (choice[0] == fieldValue?string || (fieldValue?is_number && fieldValue?c == choice[0])) > selected="selected"</#if>>${msg(choice[1])?html}</option>
                  </#if>
               </#list>
         </select>
         <script type="text/javascript">
			function productUnitToogleVisible(){
				var val = Dom.get("${fieldHtmlId}").value;
						if(YAHOO.util.Dom.get("${args.htmlid}_prop_bcpg_netWeight")!=null){
								if(val == "kg" || val == "g" || val == "lb" || val == "oz"){
								   	YAHOO.util.Dom.addClass(YAHOO.util.Dom.get("${args.htmlid}_prop_bcpg_netWeight").parentNode,"hidden");
								    YAHOO.util.Dom.get("${args.htmlid}_prop_bcpg_netWeight").value = "";
									<#-- if(YAHOO.util.Dom.get("${args.htmlid}_prop_bcpg_netWeight-val")!=null){
									   YAHOO.util.Dom.get("${args.htmlid}_prop_bcpg_netWeight-val").value = "";
									}-->
								 } else {
					         			YAHOO.util.Dom.removeClass(YAHOO.util.Dom.get("${args.htmlid}_prop_bcpg_netWeight").parentNode,"hidden");
					         	}
			         	}
			         	if(YAHOO.util.Dom.get("${args.htmlid}_prop_bcpg_netVolume")!=null){
				         	if(val != "P"){
							    YAHOO.util.Dom.addClass(YAHOO.util.Dom.get("${args.htmlid}_prop_bcpg_netVolume").parentNode,"hidden");
							    YAHOO.util.Dom.get("${args.htmlid}_prop_bcpg_netVolume").value = "";
	 						    <#-- if(YAHOO.util.Dom.get("${args.htmlid}_prop_bcpg_netVolume-val")!=null){
							    	YAHOO.util.Dom.get("${args.htmlid}_prop_bcpg_netVolume-val").value = "";
							    } -->
							 } else {
				         		YAHOO.util.Dom.removeClass(YAHOO.util.Dom.get("${args.htmlid}_prop_bcpg_netVolume").parentNode,"hidden");
				         	}
			         	}
			         	
			}
         	YAHOO.Bubbling.on("beforeFormRuntimeInit", function (layer, args)
     			 {	
		            productUnitToogleVisible();
					YAHOO.util.Event.addListener("${fieldHtmlId}", "change", function(){
		               productUnitToogleVisible();
		         	});
         	}, this);
         </script>
         <@formLib.renderFieldHelp field=field />
      <#else>
         <div id="${fieldHtmlId}" class="missing-options">${msg("form.control.selectone.missing-options")}</div>
      </#if>
   </#if>
</div>
