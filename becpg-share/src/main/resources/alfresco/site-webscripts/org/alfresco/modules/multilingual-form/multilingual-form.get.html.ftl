<#include "/org/alfresco/components/form/controls/common/editorparams.inc.ftl" />

<#assign el=args.htmlid?html>
<#assign label=args.label!"">
<#assign description="">



<div id="${el}-dialog" class="multilingual-form">
	<#if !args.readonly??>   
   <div id="${el}-dialogTitle" class="hd">${msg("title")}</div>
   <#else>
   <div id="${el}-dialogTitle" class="hd">${msg("title.read")}</div>
   </#if>
   <div class="bd">
      <form id="${el}-form" action="" method="post" class="form-container">
         <div class="form-fields">
	         <div class="set">	
	         <#if !args.readonly??>      
	         	<#if !showAll>
		         <div class="form-field">
			         <select id="${el}-locale-picker" name="-" onChange="addFormFieldForLocale();return false;">
                            <option value="-" >${msg("locale.choose")}</option>
							<#list langs?sort_by(["label"]) as language>
                              <#if currentLocale != language.key>
								<option value="${language.key}"  >${language.label}</option>
                               </#if>
							</#list>
						</select>
					</div>
				</#if>
					
	         <#list mlFields?sort_by(["localeLabel"]) as mlField>
				<#assign description=mlField.description!""?html>
                <#if mlField.locale != currentLocale>
	         	<div class="form-field">
      				<label for="${el}-${mlField.locale}"><#if args.label?? >${label?html}<#elseif mlField.label??>${mlField.label?html}<#else> ${mlField.localeLabel}</#if>:&nbsp;
      						<span class="locale-icon"><img class="icon16_11" title="${mlField.localeLabel}" tabindex="0" src="${url.context}/res/components/images/flags/${mlField.country?lower_case}.png">&nbsp;(${mlField.localeLabel})<span>&nbsp;&nbsp;
      						<span class="translate-icon" onClick="suggestTranslate('${el}-${mlField.locale}','${mlField.locale}');" ><img class="icon16" title="${msg("translate.suggest")}" tabindex="0" src="${url.context}/res/components/images/translate-16.png"><span>
      				</label>
      				<#if !args.htmlEditor??>
		      			<#if args.textarea??>
		      				<textarea rows="2" cols="60" title="${mlField.description!""?html}" tabindex="0"
			      				 name="${mlField.locale}" id="${el}-${mlField.locale}" >${mlField.value!""}</textarea>
		      			<#else>
			      			<input type="text" title="${mlField.description!""?html}" tabindex="0"
			      				 name="${mlField.locale}" id="${el}-${mlField.locale}"  value="${mlField.value!""}"></input>
		      			</#if>
	      			<#else>
			      	 	<#assign mlFieldHtmlId = "${el}-${mlField.locale}">
			      	 	<script type="text/javascript">//<![CDATA[
						   (function() {
						   
							 if(this.editors === undefined){
							    this.editors = {};
							 }
							
						     var editor = new Alfresco.RichTextControl("${mlFieldHtmlId}").setOptions(
						      {
						         currentValue: "${mlField.value?js_string}",
						         <@editorParameters mlField />
						      }).setMessages(${messages});
						      
						      this.editors["${mlFieldHtmlId}"] = editor;
						   
						   })();
						   //]]></script>
						   
						   <textarea rows="2" columns="60" title="${mlField.description!""?html}" tabindex="0"
						   		id="${mlFieldHtmlId}" name="${mlField.locale}" > ${mlField.value} </textarea>
			      	 </#if>		
	      		</div>
               </#if>
	      	</#list>
	      	
	      	<#else>	
		         <#list mlFields?sort_by(["localeLabel"]) as mlField>
					<#assign description=mlField.description!""?html>
	                <#if mlField.locale != currentLocale>
		         	<div class="form-field">
		         	 
	      				<label for="${el}-${mlField.locale}"><#if args.label?? >${label?html}<#elseif mlField.label??>${mlField.label?html}<#else> ${mlField.localeLabel}</#if>:&nbsp;
	      						<span class="locale-icon"><img class="icon16_11" title="${mlField.localeLabel}" tabindex="0" src="${url.context}/res/components/images/flags/${mlField.country?lower_case}.png">&nbsp;(${mlField.localeLabel})<span>&nbsp;&nbsp;
	      				</label>
	      			
		      				<div rows="2" cols="60" title="${mlField.description!""?html}" tabindex="0" readonly
			      				 name="${mlField.locale}" id="${el}-${mlField.locale}" >${mlField.value!""}</div>
			      	 
		      		</div>
	               </#if>
		      	</#list>
	      	</#if>
	      	
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
							if(!(this.editors === undefined) && this.editors[fieldHtmlId]){
								this.editors[fieldHtmlId].editor.setContent(resp.json.translatedText);
							}
						    document.getElementById(fieldHtmlId).value = resp.json.translatedText;
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
	  	var lbl = select.options[index].text;
	  	var container = document.getElementById("${el}-added-locale-container");
	  	var	varHtml = "";
	  	var country = lc.toLowerCase();
	  	if(lc.indexOf("_")>0){
	  	 country = lc.split("_")[1].toLowerCase();
	  	}
	  	if(document.getElementById("${el}-"+lc) == null && lc!="-"){
   
		   	 varHtml +="<div class=\"form-field\"><label for=\"${el}-"+lc
		   	         +"\">${label?js_string}:&nbsp;<span class=\"locale-icon\">"
		   	         +"<img class=\"icon16_11\" tabindex=\"0\" src=\"${url.context}/res/components/images/flags/"+country+".png\"/>&nbsp;("+lbl+")</span>&nbsp;&nbsp;"
		   	         +"<span class=\"translate-icon\" onClick=\"suggestTranslate('${el}-"+lc+"','"+lc+"');\" >"
		   	         +"<img class=\"icon16\" title=\"${msg("translate.suggest")}\" tabindex=\"0\" src=\"${url.context}/res/components/images/translate-16.png\">"
		   	         +"<span></label>";
		   	 <#if args.textarea??>
			   	 <#if args.htmlEditor??>
		   	 	 <#assign mlField = {"control": {"params": {"editorAppearance": "default"}}}>
			   	 var mlFieldHtmlId = "${el}-"+lc;
				 if(this.editors === undefined){
				    this.editors = {};
				 }
			     var editor = new Alfresco.RichTextControl(mlFieldHtmlId).setOptions(
			      {
			         currentValue: "",
			         <@editorParameters mlField />
			      }).setMessages(${messages});
			      
			      this.editors[mlFieldHtmlId] = editor;
				</#if>
				
		   	 varHtml+="<textarea rows=\"2\" cols=\"60\" title=\"${description?js_string}\" tabindex=\"0\"	 name=\""+lc+"\" id=\"${el}-"+lc+"\"></textarea>";
		   	 <#else>
		   	 varHtml+="<input type=\"text\" title=\"${description?js_string}\" tabindex=\"0\"	 name=\""+lc+"\" id=\"${el}-"+lc+"\"></input>";
		  	 </#if>
		  	 varHtml +="</div>";
		  	 var htmlEl = document.createElement("div");
				htmlEl.innerHTML = varHtml;
		  	 
		  	 container.appendChild(htmlEl);
 		}
  		
    
};

//]]></script>
