<@standalone>

   
   <@markup id="widgets">
       <@createWidgets group="survey"/>
   </@>
   
   <@markup id="html">
      <@uniqueIdDiv>
            <#assign formId=args.htmlid?js_string?html + "-form">
            <#assign controlId = args.htmlid?js_string?html +"-control" >
            <#assign fieldHtmlId = args.htmlid?js_string?html +"-survey" >
                  <script type="text/javascript">//<![CDATA[
				      new Alfresco.FormUI("${formId}", "${args.htmlid?js_string}").setOptions(
				      {
				         mode: "POST",
				         enctype: "application/json",
				         fields:
				         [{id:"${fieldHtmlId}"}],
				         fieldConstraints: 
				         [ ],
				         disableSubmitButton: false
				      }).setMessages(
				         ${messages}
				      );
				   //]]></script>
                  
               
              <div id="${formId}-container" class="form-container">
			         <div id="${formId}-caption" class="caption"><span class="mandatory-indicator">*</span>${msg("form.required.fields")}</div>
			         <form id="${formId}" method="POST" accept-charset="utf-8" enctype="application/json" action="${url.context}/proxy/alfresco/becpg/survey?entityNodeRef=${nodeRef}&dataListName=${list}">
			      
					      <div id="${formId}-fields" class="form-fields">
		
							    <div id="${controlId}" class="decision-tree-control">
							      <div id="${controlId}-body" ></div>
							       <input type="hidden"  name="data" id="${fieldHtmlId}" value="${currentValue?html}" />
							    </div>
		                
					        </div>
					      
					        <div id="${formId}-buttons" class="form-buttons">
							     <input id="${formId}-submit" type="submit" value="${msg("form.button.submit.label")}" />
							</div>
					   </form>
			 </div> 
    
      </@>
   </@>
</@>
