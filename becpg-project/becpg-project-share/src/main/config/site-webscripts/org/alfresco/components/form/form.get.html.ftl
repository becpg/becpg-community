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
         <#import "form.lib.ftl" as formLib />
         
         <#if error?exists>
            <div class="error">${error}</div>
         <#elseif form?exists>
            <#assign formId=args.htmlid?js_string + "-form">
            <#assign formUI><#if args.formUI??>${args.formUI}<#else>true</#if></#assign>
            
           <#if (args.formId?? && args.formId == "bulk-edit") || (args.bulkEdit?? && args.bulkEdit="true") >
      			<#include "../bulk-edit/forms/bulkedit-form.ftl" />
      	  <#elseif (args.formId?? && args.formId == "popup-edit") || (args.popup?? && args.popup="true") >
      			<#include "./popupedit-form.ftl" />	
  		     <#elseif args.formId?? && args.formId == "filter">
      			<#include "../../modules/entity-datagrid/forms/filter-form.ftl" />
           <#elseif form.viewTemplate?? && form.mode == "view">
               <#include "${form.viewTemplate}" />
            <#elseif form.editTemplate?? && form.mode == "edit">
               <#include "${form.editTemplate}" />
            <#elseif form.createTemplate?? && form.mode == "create">
               <#include "${form.createTemplate}" />
            <#else>
               <#if formUI == "true">
                  <@formLib.renderFormsRuntime formId=formId />
               </#if>
               
               <@formLib.renderFormContainer formId=formId>
                  <#list form.structure as item>
                     <#if item.kind == "set" >
                       	 <@formLib.renderSet set=item />
                     <#else>
                        <@formLib.renderField field=form.fields[item.id] />
                     </#if>
                  </#list>
               </@>
            </#if>
         <#else>
            <div class="form-container">${msg("form.not.present")}</div>
         </#if>
      </@>
   </@>
</@>

