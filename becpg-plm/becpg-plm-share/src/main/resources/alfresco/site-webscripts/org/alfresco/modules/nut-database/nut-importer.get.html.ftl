<#assign el=args.htmlid?html>
<#assign siteId=args.siteId!"">
<#if siteId != ""><#assign targetLinkTemplate='${url.context}/page/site/${siteId}/document-details?nodeRef={nodeRef}'><#else><#assign targetLinkTemplate='${url.context}/page/document-details?nodeRef={nodeRef}'></#if>

<div id="${el}-dialog" class="change-type">
   <div id="${el}-dialogTitle" class="hd"><#if args.nutsCompare??>${msg("title_nuts")}<#else>${msg("title")}</#if></div>
   <div class="bd">
      <form id="${el}-form" action="" method="post" class="form-container">
         <div class="form-fields">
	         <div class="set">
		          <div class="form-field">
		     			<label for="${el}-supplier">${msg("label.supplier")}:</label>     
						<div id="${el}-supplier" class="object-finder">        
						<div class="yui-ac" >
							 <div id="${el}-supplier-field-autocomplete" class="ac-body" >
										 <span id="${el}-supplier-field-toggle-autocomplete" class="ac-toogle"></span>									
										 <input id="${el}-supplier-field" type="text" name="-" tabindex="0"  class="yui-ac-input" />
										 <span class="clear" ></span>
								</div>			
								<div id="${el}-supplier-field-container"></div>
								<input type="hidden" id="${el}-supplier-added" name="supplier" />
						   </div>
					   </div>
					</div>
					
		          <div class="form-field">
		     			<label for="${el}-entities">${msg("label.entities")}:<span class="mandatory-indicator">*</span></label>     
						<div id="${el}-entities" class="object-finder">        
						<div class="yui-ac" >
							 <div id="${el}-entities-field-autocomplete" class="ac-body" >
										 <span id="${el}-entities-field-toggle-autocomplete" class="ac-toogle"></span>
										 <span id="${el}-entities-basket" class="viewmode-value current-values"></span>										
										 <input id="${el}-entities-field" type="text" name="-" tabindex="0"  class="yui-ac-input multi-assoc" />
										 <span class="clear" ></span>
								</div>			
								<div id="${el}-entities-field-container"></div>
								<input type="hidden" id="${el}-entities-added" name="entities" />
						   </div>
					   </div>
					</div>
				</div>	
				 <div class="set">	
					<div class="form-field">
					      <input id="${el}-addAsReferenceNutrient" type="hidden" name="addAsReferenceNutrient" value="false">
					      <input class="formsCheckBox" id="${el}-addAsReferenceNutrient-entry" type="checkbox" tabindex="0" name="-" onchange="javascript:YAHOO.util.Dom.get(&quot;${el}-addAsReferenceNutrient&quot;).value=YAHOO.util.Dom.get(&quot;${el}-addAsReferenceNutrient-entry&quot;).checked;">
					      <label for="${el}-addAsReferenceNutrient-entry" class="checkbox">${msg("label.addAsReferenceNutrient")}</label>
					</div>		   
					
					<#assign h = config.scoped["Languages"]["languages"]>
					<div class="form-field">
					   <label for="${el}-referenceNutrientLocales-entry">${msg("label.referenceNutrientLocales")}</label>
					   <input id="${el}-referenceNutrientLocales" type="hidden" name="referenceNutrientLocales" value="">
					   <select id="${el}-referenceNutrientLocales-entry" name="-" multiple="multiple" size="5" index="0" onchange="javascript:Alfresco.util.updateMultiSelectListValue('${el}-referenceNutrientLocales-entry', '${el}-referenceNutrientLocales', false);">
					      <#list h.getChildren("language") as language>
					         <#assign key = language.getAttribute("locale")>
					         <option value="${key}">${msg("locale.name.${key}")}</option>
					      </#list>             
					   </select>
					</div>


				</div>
			</div>
         <div class="bdft">
         	<#if args.nutsCompare??>
         		<span id="${el}-show" class="yui-button yui-push-button">
         			<span class="first-child">
         				<button type="button" id="${el}-show-button"  tabindex="0">${msg("button.compare-nuts")}</button>
         			</span>
         		</span>
         	</#if>
         	<#if args.writePermission??>
         		<input type="button" id="${el}-ok" value="<#if args.nutsCompare??>${msg("button.load")}<#else>${msg("button.ok")}</#if>" tabindex="0" />
     		</#if>
            <input type="button" id="${el}-cancel" value="${msg("button.cancel")}" tabindex="0" />
         </div>
      </form>
   </div>
</div>

<script type="text/javascript">//<![CDATA[

(function()
{

 new beCPG.component.AutoCompletePicker('${el}-supplier', '${el}-supplier-field', true).setOptions(
   {
 		mode: "edit",
      multipleSelectMode: false, 
 		targetLinkTemplate: "${targetLinkTemplate}" ,
 		dsStr:"becpg/autocomplete/nutDatabaseSuppliers"
  });

 new beCPG.component.AutoCompletePicker('${el}-entities', '${el}-entities-field', true).setOptions(
   {
 		mode: "edit",
      multipleSelectMode: true, 
 		targetLinkTemplate: "${targetLinkTemplate}" ,
 		dsStr:"becpg/autocomplete/nutDataBase",
 		parentFieldHtmlId:"${el}-supplier"
  });
  
  <#if args.nutsCompare??>
  YAHOO.util.Event.on("${el}-show-button","click",function(e){
    			Alfresco.util.Ajax.request({
				url : Alfresco.constants.URL_SERVICECONTEXT + "modules/nut-database/nut-comparer?base=${args.entityNodeRef}&supplier="+YAHOO.util.Dom.get("${el}-supplier-added").value+"&entities="+YAHOO.util.Dom.get("${el}-entities-added").value,
				dataObj : {
					htmlid : "${el}-show-popup"
				},
				successCallback : {
					fn : function(response) {
						// Inject the template from the XHR request into a new
						// DIV
						// element
						var containerDiv = document.createElement("div");
						containerDiv.innerHTML = response.serverResponse.responseText;

						// The panel is created from the HTML returned in the
						// XHR
						// request, not the container
						var panelDiv = YAHOO.util.Dom.getFirstChild(containerDiv);
						
						Alfresco.util.createYUIPanel(panelDiv, {
							draggable : true,
							width : "550px"
						}).show();

						//console.log(this.nut-panel);
						//nut-panel.show();

					},
					scope : this
				},
				failureMessage : "Could not load dialog template from ...",
				scope : this,
				execScripts : true
			});
  });
  </#if>
  

})();

//]]></script>