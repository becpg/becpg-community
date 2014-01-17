<#assign el=args.htmlid?html>
<#if args.fields??>
	<#assign fields=args.fields>
</#if>
<#if formUI == "true">
   <@formLib.renderFormsRuntime formId=formId />
</#if>


<div id="${el}-dialog">
   <div id="${el}-dialogTitle" class="hd">${msg("title")}</div>
   <div class="bd">

      <div id="${formId}-container" class="form-container">

         <#if form.showCaption?exists && form.showCaption>
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

               <#list form.structure as item>
                  <#if item.kind == "set">
                  	<#if fields??>
                       <@formLib.renderBulkSet set=item fields=fields/>
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
               <input id="${formId}-submit" type="submit" value="${msg("form.button.submit.label")}" />
               &nbsp;<input id="${formId}-cancel" type="button" value="${msg("form.button.cancel.label")}" />
            </div>
      
         </form>

      </div>
   </div>
</div>