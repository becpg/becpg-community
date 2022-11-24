<#assign el=args.htmlid?html>
<#assign siteId=args.siteId!"">
<#if siteId != ""><#assign targetLinkTemplate='${url.context}/page/site/${siteId}/entity-data-lists?list=View-properties&nodeRef={nodeRef}'><#else><#assign targetLinkTemplate='${url.context}/page/entity-data-lists?list=View-properties&nodeRef={nodeRef}'></#if>
<script type="text/javascript">//<![CDATA[

(function()
{

 new beCPG.component.AutoCompletePicker('${el}-entities', '${el}-entities-field', true).setOptions(
   {
        currentValue: "${entities}",
 		mode: "edit",
        multipleSelectMode: true, 
 		targetLinkTemplate: "${targetLinkTemplate}" ,
 		dsStr: "becpg/autocomplete/targetassoc/associations/bcpg:entityV2"
  });
  
 new beCPG.component.AutoCompletePicker('${el}-versions', '${el}-versions-field', true).setOptions(
   {
        currentValue: "${versions}",
 		mode: "edit",
        multipleSelectMode: true, 
 		targetLinkTemplate: "${targetLinkTemplate}" ,
 		dsStr: "becpg/autocomplete/versions?entityNodeRef=${entity}"
  });

})();

//]]></script>
<div id="${el}-dialog" class="change-type">
   <div id="${el}-dialogTitle" class="hd">${msg("title")}</div>
   <div class="bd">
      <form id="${el}-form" action="" method="post" class="form-container">
         <div class="form-fields">
	         <div class="set">
		          <div class="form-field">
		     			<label for="${el}-entities">${msg("label.entities")}:</label>     
						<div id="${el}-entities" class="object-finder">        
						<div class="yui-ac" >
							 <div id="${el}-entities-field-autocomplete" class="ac-body" >
										 <span id="${el}-entities-field-toggle-autocomplete" class="ac-toogle"></span>
										 <span id="${el}-entities-basket" class="viewmode-value current-values"></span>										
										 <input id="${el}-entities-field" onfocus="this.hasFocus=true" onblur="this.hasFocus=false" type="text" name="-" tabindex="0"  class="yui-ac-input multi-assoc" />
										 <span class="clear" ></span>
								</div>			
								<div id="${el}-entities-field-container"></div>
								<input type="hidden" id="${el}-entities-added" name="entities" value="${entities}" />
						   </div>
					   </div>
					</div>
		          <div class="form-field">
		     			<label for="${el}-versions">${msg("label.versions")}:</label>     
						<div id="${el}-versions" class="object-finder">        
						<div class="yui-ac" >
							 <div id="${el}-versions-field-autocomplete" class="ac-body" >
										 <span id="${el}-versions-field-toggle-autocomplete" class="ac-toogle"></span>
										 <span id="${el}-versions-basket" class="viewmode-value current-values"></span>										
										 <input id="${el}-versions-field" onfocus="this.hasFocus=true" onblur="this.hasFocus=false" type="text" name="-" tabindex="0"  class="yui-ac-input multi-assoc" />
										 <span class="clear" ></span>
								</div>			
								<div id="${el}-versions-field-container"></div>
								<input type="hidden" id="${el}-versions-added" name="versions" value="${versions}" />
						   </div>
					   </div>
					</div>
				</div>
				<div class="form-field">
					<label for="${el}-reportTemplate">${msg("label.reportTemplates")}:<span class="mandatory-indicator">*</span></label>
					<select id="${el}-reportTemplate" >
				         <#list reportTpls as reportTemplate>
				          <option value="${reportTemplate.nodeRef}" fileName="${reportTemplate.name}.${reportTemplate.format?lower_case}">${reportTemplate.name}</option>
				        </#list>
				    </select>
				</div>
			</div>
         <div class="bdft">
            <input type="button" id="${el}-ok" value="${msg("button.ok")}" tabindex="0" />
            <input type="button" id="${el}-cancel" value="${msg("button.cancel")}" tabindex="0" />
         </div>
      </form>
   </div>
</div>

