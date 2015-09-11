<#assign el=args.htmlid?html>
<#assign siteId=args.siteId!"">
<#if siteId != ""><#assign targetLinkTemplate='${url.context}/page/site/${siteId}/document-details?nodeRef={nodeRef}'><#else><#assign targetLinkTemplate='${url.context}/page/document-details?nodeRef={nodeRef}'></#if>

<div id="${el}-dialog" class="change-type">
   <div id="${el}-dialogTitle" class="hd">${msg("title")}</div>
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
			</div>
         <div class="bdft">
            <input type="button" id="${el}-ok" value="${msg("button.ok")}" tabindex="0" />
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
 		dsStr:"becpg/remote/proxy/becpg/autocomplete/targetassoc/associations/bcpg:supplier"
  });

 new beCPG.component.AutoCompletePicker('${el}-entities', '${el}-entities-field', true).setOptions(
   {
 		mode: "edit",
      multipleSelectMode: true, 
 		targetLinkTemplate: "${targetLinkTemplate}" ,
 		dsStr:"becpg/remote/proxy/becpg/autocomplete/product?classNames=bcpg:rawMaterial,bcpg:finishedProduct,bcpg:localSemiFinishedProduct,bcpg:semiFinishedProduct&extra.filterByAssoc=bcpg:suppliers",
 		parentFieldHtmlId:"${el}-supplier"
  });

})();

//]]></script>