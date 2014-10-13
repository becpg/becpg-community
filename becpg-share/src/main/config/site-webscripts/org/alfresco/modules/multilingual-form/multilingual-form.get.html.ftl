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
			         <select id="${el}-locale-picker" name="-" onChange="addFormFieldForLocale(this.form)">
							<#assign h = config.scoped["Languages"]["languages"]>
							<#list  h.getChildren("language") as language>
							     <#assign key = language.getAttribute("locale")>				
									<option value=${key?split("_")[0]} <#if key?contains(locale)>selected="true"</#if> >${msg("locale.name.${key}")}</option>
							</#list>
						</select>
					</div>

	         <#list mlFields?reverse as mlField>
	        		 <#assign label=mlField.label!""?html>
					<#assign description=mlField.description!""?html>
	         	<div class="form-field">
      				<label for="${el}-${mlField.locale}">${mlField.label!""?html}:&nbsp;
      						<span class="locale-icon"><img  title="${mlField.locale}" tabindex="0" src="${url.context}/res/components/images/flags/${mlField.locale}.png"><span>
      				</label>
	      			<#if args.textarea??>
	      				<textarea rows="2" cols="60" title="${mlField.description!""?html}" tabindex="0"
		      				 name="${mlField.locale}" id="${el}-${mlField.locale}" >${mlField.value!""}</textarea>
	      			<#else>
		      			<input type="text" title="${mlField.description!""?html}" tabindex="0"
		      				 name="${mlField.locale}" id="${el}-${mlField.locale}" value="${mlField.value!""}"></input>
	      			</#if>	 
	      		</div>
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

var addFormFieldForLocale = function(form){
  
	  	var select = YAHOO.util.Dom.get("${el}-locale-picker"),
	  		index = select.selectedIndex,
	  		lc = select.options[index].value,
	  		container = YAHOO.util.Dom.get("${el}-added-locale-container"),
	  		varHtml = "";
	  		
	  		if(YAHOO.util.Dom.get("${el}-"+lc) == null){
   
	   	 varHtml +="<div class=\"form-field\"><label for=\"${el}-"+lc+"\">${label?js_string}:&nbsp;<span class=\"locale-icon\"><img  tabindex=\"0\" src=\"${url.context}/res/components/images/flags/"+lc+".png\"/></span></label>";
	   	 <#if args.textarea??>
	   	 	 varHtml+="<textarea rows=\"2\" cols=\"60\" title=\"${description?js_string}\" tabindex=\"0\"	 name=\""+lc+"\" id=\"${el}-"+lc+"\"></textarea>";
	   	 <#else>
	   		 varHtml+="<input type=\"text\" title=\"${description?js_string}\" tabindex=\"0\"	 name=\""+lc+"\" id=\"${el}-"+lc+"\"></input>";
	  		</#if>
	  		 varHtml +="</div>";
	  		 
	  		 container.innerHTML += varHtml;
  		 }
	      			    
}

//]]></script>