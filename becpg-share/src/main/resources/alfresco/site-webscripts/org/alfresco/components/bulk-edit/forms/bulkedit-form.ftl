<#assign el=args.htmlid?html>
<#if args.fields??>
	<#assign fields=args.fields>
</#if>
<#if formUI == "true">
   <@formLib.renderFormsRuntime formId=formId />
</#if>

<#assign isTabbed = (form.editTemplate?? && (form.editTemplate == "tab-edit" || form.editTemplate?ends_with("tab-edit-form.ftl"))) || (form.createTemplate?? && (form.createTemplate == "tab-edit" || form.createTemplate?ends_with("tab-edit-form.ftl"))) />


<div id="${el}-dialog">
   <div id="${el}-dialogTitle" class="hd">${msg("title")}</div>
   <div class="bd">

      <div id="${formId}-container" class="form-container">

         <#if form.showCaption?exists && form.showCaption && !isTabbed>
            <div id="${formId}-caption" class="caption"><span class="mandatory-indicator">*</span>${msg("form.required.fields")}</div>
         </#if>
      
         <form id="${formId}" method="${form.method}" accept-charset="utf-8" enctype="${form.enctype}" action="${form.submissionUrl}">
         <#if form.destination??>
            <input id="${formId}-destination" name="alf_destination" type="hidden" value="${form.destination}" />
         </#if>
         <#if args.association??>
	         <input id="${formId}-association" name="alf_association" type="hidden" value="${args.association}" />
	     </#if>

  		 	<div id="${formId}-fields" class="form-fields">
   			<#if !isTabbed>
               <#list form.structure as item>
                  <#if item.kind == "set">
                  	<#if fields??>
                     	<@formLib.renderBulkSet set=item fields=fields/>
                     <#else>
		       	  		 <@formLib.renderSet set=item  />
		         	 </#if>  
                  <#else>
                      <#if fields??>
				          <#if fields?contains(item.id+",") || fields?ends_with(item.id) > 
				            <@formLib.renderField field=form.fields[item.id] />
				      	 </#if>
			          <#else>
		       	  		 <@formLib.renderField field=form.fields[item.id] />
		         	 </#if> 
                  </#if>
               </#list>
			<#else>	
				<@formLib.renderTabbedForm form=form formId=formId/>
			</#if>
            </div>

            <div class="bdft">
               <input id="${formId}-bulkAction" name="-" type="checkbox">&nbsp;<span id="${formId}-bulkAction-msg">&nbsp;</span></input>
               <input id="${formId}-submit" type="submit" value="${msg("form.button.submit.label")}" />
               &nbsp;<input id="${formId}-cancel" type="button" value="${msg("form.button.cancel.label")}" />
            </div>
      
         </form>

      </div>
   </div>
</div>