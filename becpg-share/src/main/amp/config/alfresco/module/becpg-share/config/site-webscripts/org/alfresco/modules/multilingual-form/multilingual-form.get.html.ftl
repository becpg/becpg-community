<#assign el=args.htmlid?html>
<#assign label="">
<#assign description="">


<div id="${el}-dialog" class="multilingual-form">
   <div id="${el}-dialogTitle" class="hd">${msg("title")}</div>
   <div class="bd">
      <form id="${el}-form" action="" method="post" class="form-container">
         <div class="form-fields">
	         <div class="set">	         
		         <div class="form-field">
			         <select id="${el}-locale-picker" name="-" onChange="addFormFieldForLocale();">
                            <option value="-" >${msg("locale.choose")}</option>
							<#assign h = config.scoped["Languages"]["languages"]>
							<#list  h.getChildren("language") as language>
							     <#assign key = language.getAttribute("locale")>	
                              <#if !key?contains(locale)>
								<option value=${key?split("_")[0]}  >${msg("locale.name.${key}")}</option>
                               </#if>
							</#list>
						</select>
					</div>

	         <#list mlFields?reverse as mlField>
	        		 <#assign label=mlField.label!""?html>
					<#assign description=mlField.description!""?html>
                 <#if mlField.locale != locale>
	         	<div class="form-field">
      				<label for="${el}-${mlField.locale}">${mlField.label!""?html}:&nbsp;
      						<span class="locale-icon"><img class="icon16_11" title="${mlField.locale}" tabindex="0" src="${url.context}/res/components/images/flags/${mlField.locale}.png"><span>&nbsp;&nbsp;
      						<span class="translate-icon" onClick="suggestTranslate('${el}-${mlField.locale}','${mlField.locale}');" ><img class="icon16" title="${msg("translate.suggest")}" tabindex="0" src="${url.context}/res/components/images/translate-16.png"><span>
      				</label>
	      			<#if args.textarea??>
	      				<textarea rows="2" cols="60" title="${mlField.description!""?html}" tabindex="0"
		      				 name="${mlField.locale}" id="${el}-${mlField.locale}" >${mlField.value!""}</textarea>
	      			<#else>
		      			<input type="text" title="${mlField.description!""?html}" tabindex="0"
		      				 name="${mlField.locale}" id="${el}-${mlField.locale}"  value="${mlField.value!""}"></input>
	      			</#if>	 
	      		</div>
               </#if>
	      	</#list>	
	      	
		      	<div id="${el}-added-locale-container"></div>
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

var suggestTranslate = function(fieldHtmlId, targetLocale){
 	Alfresco.util.Ajax.request({
				method : Alfresco.util.Ajax.GET,
				url : Alfresco.constants.PROXY_URI + "becpg/form/multilingual/field/${field}?suggest=true&nodeRef=${nodeRef}&target="+targetLocale,
				successCallback : {
					fn : function(resp) {
						if(resp.json && resp.json.translatedText){
							document.getElementById(fieldHtmlId).innerHTML = resp.json.translatedText;
						}
					},
					scope : this
				}
		});
 }; 		

var addFormFieldForLocale = function(){
  	
  	  	var select = document.getElementById("${el}-locale-picker");
	  	var index = select.selectedIndex;
	  	var lc = select.options[index].value;
	  	var container = document.getElementById("${el}-added-locale-container");
	  	var	varHtml = "";
	  	if(document.getElementById("${el}-"+lc) == null && lc!="-"){
   
	   	 varHtml +="<div class=\"form-field\"><label for=\"${el}-"+lc
	   	         +"\">${label?js_string}:&nbsp;<span class=\"locale-icon\">"
	   	         +"<img class=\"icon16_11\" tabindex=\"0\" src=\"${url.context}/res/components/images/flags/"+lc+".png\"/></span>&nbsp;&nbsp;"
	   	         +"<span class=\"translate-icon\" onClick=\"suggestTranslate('${el}-"+lc+"','"+lc+"');\" >"
	   	         +"<img class=\"icon16\" title=\"${msg("translate.suggest")}\" tabindex=\"0\" src=\"${url.context}/res/components/images/translate-16.png\">"
	   	         +"<span></label>";
	   	 <#if args.textarea??>
	   	 varHtml+="<textarea rows=\"2\" cols=\"60\" title=\"${description?js_string}\" tabindex=\"0\"	 name=\""+lc+"\" id=\"${el}-"+lc+"\"></textarea>";
	   	 <#else>
	   	 varHtml+="<input type=\"text\" title=\"${description?js_string}\" tabindex=\"0\"	 name=\""+lc+"\" id=\"${el}-"+lc+"\"></input>";
	  	 </#if>
	  	 varHtml +="</div>";
	  	 container.innerHTML += varHtml;
 };
  		

  		
  		
	     			    
};

//]]></script>
