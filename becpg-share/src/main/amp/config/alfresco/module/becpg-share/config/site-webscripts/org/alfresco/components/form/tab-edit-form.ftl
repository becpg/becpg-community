<@standalone>
   <@markup id="css" >
      <#include "form.css.ftl"/>
   </@>
   
   <@markup id="js">
      <#include "form.js.ftl"/>
   </@>
   
   <@markup id="widgets">
      <@createWidgets/>
   </@>

	   <@markup id="html">
	      <@uniqueIdDiv>
			  <#import "form.lib.ftl" as formlib />
	         
	         <#if error?exists>
	            <div class="error">${error}</div>
	         <#elseif form?exists>
				<#assign formId=args.htmlid?js_string + "-form">
	            <#assign formUI><#if args.formUI??>${args.formUI}<#else>true</#if></#assign>
				<#if formUI == "true">
				   <@formLib.renderFormsRuntime formId=formId />
				</#if>
				  <@formLib.renderFormContainer formId=formId>
				     <@formLib.renderTabbedForm form=form formId=formId/>
				  </@>
			</#if>
	  	 </@>
	</@>
</@>