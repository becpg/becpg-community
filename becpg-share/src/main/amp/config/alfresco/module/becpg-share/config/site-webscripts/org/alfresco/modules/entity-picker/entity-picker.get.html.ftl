<#assign el=args.htmlid?html>
<#assign siteId=args.siteId!"">
<#if siteId != ""><#assign targetLinkTemplate='${url.context}/page/site/${siteId}/entity-data-lists?list=View-properties&nodeRef={nodeRef}'><#else><#assign targetLinkTemplate='${url.context}/page/entity-data-lists?list=View-properties&nodeRef={nodeRef}'></#if>
<script type="text/javascript">//<![CDATA[

(function()
{

 new beCPG.component.AutoCompletePicker('${el}-entity', '${el}-entity-field', true).setOptions(
   {
        currentValue: "",
 		mode: "edit",
        multipleSelectMode: false, 
 		targetLinkTemplate: "${targetLinkTemplate}" ,
 		dsStr: "becpg/autocomplete/targetassoc/associations/bcpg:product?classNames=inc_bcpg:entityTplAspect&amp;excludeProps=bcpg:entityTplEnabled%7Cfalse"
  });

})();

//]]></script>
<div id="${el}-dialog" class="change-type">
   <div id="${el}-dialogTitle" class="hd">${args.title}</div>
   <div class="bd">
      <form id="${el}-form" action="" method="post" class="form-container">
         <div class="form-fields">
	         <div class="set">
		          <div class="form-field">
		     			<label for="${el}-entity">${msg("label.entity")}:</label>     
						<div id="${el}-entity" class="object-finder">        
						<div class="yui-ac" >
							 <div id="${el}-entity-field-autocomplete" class="ac-body" >
										 <span id="${el}-entity-field-toggle-autocomplete" class="ac-toogle"></span>									
										 <input id="${el}-entity-field" type="text" name="-" tabindex="0" onfocus="this.hasFocus=true" onblur="this.hasFocus=false" class="yui-ac-input" />
										 <span class="clear" ></span>
								</div>			
								<div id="${el}-entity-field-container"></div>
								<input type="hidden" id="${el}-entity-added" name="entity" />
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

