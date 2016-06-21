<#macro renderFormsRuntime formId>
   <script type="text/javascript">//<![CDATA[
      new Alfresco.FormUI("${formId}", "${args.htmlid?js_string}").setOptions(
      {
         mode: "${form.mode}",
         <#if form.mode == "view">
         arguments:
         {
            itemKind: "${(form.arguments.itemKind!"")?js_string}",
            itemId: "${(form.arguments.itemId!"")?js_string}",
            formId: "${(form.arguments.formId!"")?js_string}"
         }
         <#else>
         enctype: "${form.enctype}",
         fields:
         [
            <#list form.fields?keys as field>
            {
               id : "${form.fields[field].id}"
            }
            <#if field_has_next>,</#if>
            </#list>
         ],
         fieldConstraints: 
         [
            <#list form.constraints as constraint>
            {
               fieldId : "${args.htmlid?js_string}_${constraint.fieldId}", 
               handler : ${constraint.validationHandler}, 
               params : ${constraint.params}, 
               event : "${constraint.event}",
               message : <#if constraint.message??>"${constraint.message?js_string}"<#else>null</#if>
            }
            <#if constraint_has_next>,</#if>
            </#list>
         ],
         disableSubmitButton: <#if args.disableSubmitButton??>${args.disableSubmitButton?js_string}<#else>false</#if>
         </#if>
      }).setMessages(
         ${messages}
      );
   //]]></script>
</#macro> 

<#macro renderFormContainer formId>
   <div id="${formId}-container" class="form-container">
      <#if form.showCaption?? && form.showCaption>
         <div id="${formId}-caption" class="caption"><span class="mandatory-indicator">*</span>${msg("form.required.fields")}</div>
      </#if>
      
      <#if form.mode != "view">
         <form id="${formId}" method="${form.method}" accept-charset="utf-8" enctype="${form.enctype}" action="${form.submissionUrl?html}">
      </#if>
      
      <#if form.mode == "create" && form.destination?? && form.destination?length &gt; 0>
         <input id="${formId}-destination" name="alf_destination" type="hidden" value="${form.destination?html}" />
      </#if>
      
      <#if args.association??>
         <input id="${formId}-association" name="alf_association" type="hidden" value="${args.association}" />
      </#if>
      
      <#if form.mode != "view" && form.redirect?? && form.redirect?length &gt; 0>
         <input id="${formId}-redirect" name="alf_redirect" type="hidden" value="${form.redirect?html}" />
      </#if>
      
      <div id="${formId}-fields" class="form-fields">
         <#nested>
      </div>
      
      <#if form.mode != "view">
         <@renderFormButtons formId=formId />
         </form>
      </#if>
   </div>
</#macro>

<#macro renderFormButtons formId>         
   <div id="${formId}-buttons" class="form-buttons">
      <#if form.showSubmitButton?? && form.showSubmitButton>
         <input id="${formId}-submit" type="submit" value="${msg("form.button.submit.label")}" />&nbsp;
      </#if>
      <#if form.showResetButton?? && form.showResetButton>
         <input id="${formId}-reset" type="reset" value="${msg("form.button.reset.label")}" />&nbsp;
      </#if>
      <#if form.showCancelButton?? && form.showCancelButton>
         <input id="${formId}-cancel" type="button" value="${msg("form.button.cancel.label")}" />
      </#if>
   </div>
</#macro>   

<#macro renderField field>
   <#if field.control?? && field.control.template?? && field.transitory != true>
      <#assign fieldHtmlId=args.htmlid?html + "_" + field.id?html >
      <#include "${field.control.template}" />
   </#if>
</#macro>

<#macro renderSet set>
	<#-- beCPG : test that set has children to show -->

	<#if set.children?has_content >
	
		<#assign showSet=false>
		<#list set.children as item>
			<#if item?? && item.kind != "set" && form.fields[item.id].transitory == false >      
			   <#assign showSet=true>
			</#if>
			<#if item?? && item.kind == "set">
				<#assign showSet=true>
			</#if>
		</#list>
	
		<#if showSet>
		   <div class="set">
		   <#if set.appearance??>
		      <#if set.appearance == "fieldset">
		         <fieldset><legend>${set.label}</legend>
		      <#elseif set.appearance == "bordered-panel">
		         <div class="set-bordered-panel">
		            <div class="set-bordered-panel-heading">${set.label}</div>
		            <div class="set-bordered-panel-body">
		      <#elseif set.appearance == "panel">
		         <div class="set-panel">
		            <div class="set-panel-heading">${set.label}</div>
		            <div class="set-panel-body">
		      <#elseif set.appearance == "title">
		         <div class="set-title">${set.label}</div>
		      <#elseif set.appearance == "whitespace">
		         <div class="set-whitespace"></div>
		      </#if>
		   </#if>
		   
		   <#if set.template??>
		      <#include "${set.template}" />
		   <#else>
		      <#list set.children as item>
		         <#if item??>
			         <#if item.kind == "set">
			            <@renderSet set=item />
			         <#else>
			            <@renderField field=form.fields[item.id] />
			         </#if>
		         </#if>
		      </#list>
		   </#if>
		   
		   <#if set.appearance??>
		      <#if set.appearance == "fieldset">
		         </fieldset>
		      <#elseif set.appearance == "panel" || set.appearance == "bordered-panel">
		            </div>
		         </div>
		      </#if>
		   </#if>
		   </div>
		 </#if>
	   
	</#if>
</#macro>

<#macro renderBulkSet set fields>
	<#assign showSet=false>
	<#if set.children?has_content >
		
		<#list set.children as item>
			<#if item?? && item.kind != "set" && fields?contains(item.id)  && form.fields[item.id].transitory == false>      
			   <#assign showSet=true>
			</#if>
			<#if item?? && item.kind == "set">
				<#assign showSet=true>
			</#if>	
		</#list>
		
		<#if  showSet>
		   <div class="set">
		   <#if set.appearance??>
		      <#if set.appearance == "fieldset">
		         <fieldset><legend>${set.label}</legend>
		      <#elseif set.appearance == "bordered-panel">
		         <div class="set-bordered-panel">
		            <div class="set-bordered-panel-heading">${set.label}</div>
		            <div class="set-bordered-panel-body">
		      <#elseif set.appearance == "panel">
		         <div class="set-panel">
		            <div class="set-panel-heading">${set.label}</div>
		            <div class="set-panel-body">
		      <#elseif set.appearance == "title">
		         <div class="set-title">${set.label}</div>
		      <#elseif set.appearance == "whitespace">
		         <div class="set-whitespace"></div>
		      </#if>
		   </#if>
		   
		    <#list set.children as item>
		     <#if item??>
		        <#if item.kind == "set">
		           <@renderBulkSet set=item fields=fields />
		        <#else>
						<#if fields?contains(item.id) > 
						     <@renderField field=form.fields[item.id] />
						 </#if>
		       </#if>
		       </#if>
		   </#list>
		   
		  
		   <#if set.appearance??>
		      <#if set.appearance == "fieldset">
		         </fieldset>
		      <#elseif set.appearance == "panel" || set.appearance == "bordered-panel">
		            </div>
		         </div>
		      </#if>
		   </#if>
		   </div>
		</#if>
	</#if>
</#macro>

<#macro renderFieldHelp field>
   <#if field.help?? && field.help?length &gt; 0>
      <span class="help-icon">
         <img class="icon16" id="${fieldHtmlId}-help-icon" src="${url.context}/res/components/form/images/help.png" title="${msg("form.field.help")}" tabindex="-1"/>
      </span>
      <div class="help-text" id="${fieldHtmlId}-help"><#if field.helpEncodeHtml>${field.help?html}<#else>${stringUtils.stripUnsafeHTML(field.help)}</#if></div>
   </#if>
</#macro>

<#macro renderLocaleImage field textarea=false>
  <#if field.transitory != true && field.dataType == "mltext" && form.mode == "edit" && form.arguments.itemKind == "node">
    <#assign localeshort = locale?substring(0,2)?lower_case >
    <#if form.arguments.itemId??>
	    <span class="locale-icon">
			<img class="icon16_11" id="${fieldHtmlId}-locale-icon" src="${url.context}/res/components/images/flags/${localeshort}.png" title="${msg("form.field.locale")}"  tabindex="-1"/>
		 </span>
		 <script type="text/javascript">//<![CDATA[
		 	YAHOO.util.Event.onAvailable("${fieldHtmlId}-locale-icon", function (){
		 
				Alfresco.util.useAsButton("${fieldHtmlId}-locale-icon", function (event, fieldId)
		            {
		                new Alfresco.module.SimpleDialog("${fieldHtmlId}-multilingualForm").setOptions({
		                  templateUrl : Alfresco.constants.URL_SERVICECONTEXT + "modules/multilingual-form/multilingual-form?nodeRef=${form.arguments.itemId}&field="+fieldId<#if textarea >+"&textarea=true"</#if>,
		                  actionUrl : Alfresco.constants.PROXY_URI + "becpg/form/multilingual/field/"+fieldId+"?nodeRef=${form.arguments.itemId}",
		                  validateOnSubmit : false,
		                  destroyOnHide : true,
		                  width: "33em",
		                  successMessage : "${msg("message.save.success")}"
		               }).show();
		
						}, "${field.id?replace("prop_","")}", this);
			
				},this);
			
			//]]></script>
	 </#if>
  </#if>
</#macro>

<#macro renderTabbedForm  form formId>
   <div id="${formId}-tabview" class="yui-navset"> 
	<ul class="yui-nav">
		<#list form.structure as item>
			<#if item.kind == "set" >
				<li <#if item_index == 0>class="selected"</#if>><a href="#tab_${item_index}"><em>${item.label}</em></a></li>
			</#if>
		</#list>
	</ul>     	
	<div class="yui-content">
		<#list form.structure as item>
		<#if item??>
		   <div id="tab_${item_index}">
		   		  <#if item.kind == "set">
		               <@formLib.renderSet set=item />
	              <#else>
		               <@formLib.renderField field=form.fields[item.id] />
		          </#if>
		   </div>	
		 </#if>	
	 	</#list>
		</div> 
	</div>
	<script type="text/javascript">//<![CDATA[
		YAHOO.util.Event.onAvailable('${formId}-tabview', function(){
			var tabView = new YAHOO.widget.TabView('${formId}-tabview');
		},this);
	//]]></script>
</#macro>	
