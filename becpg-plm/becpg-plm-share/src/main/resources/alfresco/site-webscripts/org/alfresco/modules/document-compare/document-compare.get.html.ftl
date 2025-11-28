<#assign el=args.htmlid?html>
<#assign document="">
<#assign comparisonType="">
<script type="text/javascript">//<![CDATA[

(function()
{

 new beCPG.component.AutoCompletePicker('${el}-document', '${el}-document-field', true).setOptions(
   {
 		mode: "edit",
        multipleSelectMode: false, 
 		dsStr: "becpg/autocomplete/document?extra.extensions=pdf"
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
		     			<label for="${el}-document">${msg("label.document")}:</label>     
						<div id="${el}-document" class="object-finder">        
						<div class="yui-ac" >
							 <div id="${el}-document-field-autocomplete" class="ac-body" >
										 <span id="${el}-document-field-toggle-autocomplete" class="ac-toogle"></span>
										 <span id="${el}-document-basket" class="viewmode-value current-values"></span>										
										 <input id="${el}-document-field" onfocus="this.hasFocus=true" onblur="this.hasFocus=false" type="text" name="-" tabindex="0"  class="yui-ac-input multi-assoc" />
										 <span class="clear" ></span>
								</div>			
								<div id="${el}-document-field-container"></div>
								<input type="hidden" id="${el}-document-added" name="document" value="${document}" />
						   </div>
					   </div>
					</div>
					<div class="form-field">
						<label for="${el}-comparison-type">${msg("label.comparison.type")}:</label>
						<select id="${el}-comparison-type" name="comparisonType" tabindex="0">
							<option value="text">${msg("label.comparison.type.text")}</option>
							<option value="overlay">${msg("label.comparison.type.overlay")}</option>
						</select>
						<input type="hidden" id="${el}-comparison-type" name="comparisonType" value="${comparisonType}" />
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

