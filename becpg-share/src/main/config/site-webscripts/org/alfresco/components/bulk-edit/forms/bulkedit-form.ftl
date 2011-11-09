<#assign id=args.htmlid>
<#if args.fields??>
	<#assign fields=args.fields>
</#if>
<#if formUI == "true">
   <@formLib.renderFormsRuntime formId=formId />
</#if>


<#macro renderBulkSet set fields>
	<#assign showSet=false>
	
	<#list set.children as item>
		<#if item.kind != "set" && fields?contains(item.id)  && form.fields[item.id].transitory == false>      
		   <#assign showSet=true>
		</#if>
	</#list>

	<#if set.children?has_content && showSet>
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
	         <#if item.kind == "set">
	            <@renderBulkSet set=item />
	         <#else>
				<#if fields?contains(item.id) > 
				     <@formLib.renderField field=form.fields[item.id] />
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
</#macro>



<div id="${id}-dialog">
   <div id="${id}-dialogTitle" class="hd">${msg("title")}</div>
   <div class="bd">

      <div id="${formId}-container" class="form-container">

         <#if form.showCaption?exists && form.showCaption>
            <div id="${formId}-caption" class="caption"><span class="mandatory-indicator">*</span>${msg("form.required.fields")}</div>
         </#if>
      
         <form id="${formId}" method="${form.method}" accept-charset="utf-8" enctype="${form.enctype}" action="${form.submissionUrl}">
   
         <#if form.destination??>
            <input id="${formId}-destination" name="alf_destination" type="hidden" value="${form.destination}" />
         </#if>
   
            <div id="${formId}-fields" class="form-fields">

               <#list form.structure as item>
                  <#if item.kind == "set">
                  	<#if fields??>
                     	<@renderBulkSet set=item fields=fields/>
                     <#else>
		       	  		 <@formLib.renderSet set=item  />
		         	 </#if>  
                  <#else>
                      <#if fields??>
				          <#if fields?contains(item.id) > 
				            <@formLib.renderField field=form.fields[item.id] />
				      	 </#if>
			          <#else>
		       	  		 <@formLib.renderField field=form.fields[item.id] />
		         	 </#if> 
                  </#if>
               </#list>

            </div>

            <div class="bdft">
               <input id="${formId}-bulkAction" type="checkbox">&nbsp;<span id="${formId}-bulkAction-msg">&nbsp;</span></input>
               <input id="${formId}-submit" type="submit" value="${msg("form.button.submit.label")}" />
               &nbsp;<input id="${formId}-cancel" type="button" value="${msg("form.button.cancel.label")}" />
            </div>
      
         </form>

      </div>
   </div>
</div>