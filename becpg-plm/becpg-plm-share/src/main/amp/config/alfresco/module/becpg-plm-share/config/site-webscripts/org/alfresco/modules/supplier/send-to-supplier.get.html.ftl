<#assign el=args.htmlid?html>
<#assign siteId=args.siteId!"">
<#if siteId != ""><#assign targetLinkTemplate='${url.context}/page/site/${siteId}/entity-data-lists?list=View-properties&nodeRef={nodeRef}'><#else><#assign targetLinkTemplate='${url.context}/page/entity-data-lists?list=View-properties&nodeRef={nodeRef}'></#if>

<div id="${el}-dialog" class="change-type">
   <div id="${el}-dialogTitle" class="hd">${msg("title")}</div>
   <div class="bd">
      <form id="${el}-form" action="" method="post" class="form-container">
         <div class="form-fields">
	         <div class="set">
		           <div class="form-field">
		     			<label for="${el}-projectTpl">${msg("label.projectTpl")}:</label>     
						<div id="${el}-projectTpl" class="object-finder">        
						<div class="yui-ac" >
							 <div id="${el}-projectTpl-field-autocomplete" class="ac-body" >
										 <span id="${el}-projectTpl-field-toggle-autocomplete" class="ac-toogle"></span>									
										 <input id="${el}-projectTpl-field" type="text" name="-" tabindex="0"  class="yui-ac-input" />
										 <span class="clear" ></span>
								</div>			
								<div id="${el}-projectTpl-field-container"></div>
								<input type="hidden" id="${el}-projectTpl-added" name="projectTpl" />
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

  
  new beCPG.component.AutoCompletePicker('${el}-projectTpl', '${el}-projectTpl-field', true).setOptions(
   {
 		mode: "edit",
        multipleSelectMode: false, 
 		targetLinkTemplate: "${targetLinkTemplate}" ,
 		dsStr:"becpg/autocomplete/targetassoc/associations/pjt:project?classNames=pjt:project,bcpg:entityTplAspect,bcpg:suppliersAspect&amp;excludeProps=bcpg:entityTplEnabled%7Cfalse"
  });


})();

//]]></script>
