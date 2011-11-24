<#assign el=args.htmlid?html>
<div id="${el}-dialog" class="change-type">
   <div id="${el}-dialogTitle" class="hd">${msg("title")}</div>
   <div class="bd">
      <form id="${el}-form" action="" method="post">
      	 <div class="yui-gd">
            <div class="yui-u first"><label for="${el}-name">${msg("label.name")}:</label></div>
            <div class="yui-u"><input id="${el}-name" type="text" name="name" tabindex="0" />&nbsp;*</div>
         </div>
         <div class="yui-gd">	
            <div class="yui-u first"><label for="${el}-assocType">${msg("label.assocType")}:</label></div>
            <div class="yui-u">
               <select id="${el}-assocType" type="text" name="assocType" tabindex="0" >
                  <option value="-">${msg("label.select.assocType")}</option>
               <#list lists.selectable.assocs as t>
                  <option value="${t.value}">${msg("assocType." + t.name?replace(":", "_"))}</option>
               </#list>
               </select>&nbsp;*
            </div>
         </div>
          <div class="yui-gd">	
            <div class="yui-u first"><label for="${el}-type">${msg("label.type")}:</label></div>
            <div class="yui-u">
               <select id="${el}-type" type="text" name="type" tabindex="0" >
                  <option value="-">${msg("label.select.type")}</option>
               <#list lists.selectable.types as t>
                  <option value="${t.value}">${msg("type." + t.name?replace(":", "_"))}</option>
               </#list>
               </select>&nbsp;*
            </div>
         </div>
          <div class="yui-gd">	
            <div class="yui-u first"><label for="${el}-model">${msg("label.model")}:</label></div>
            <div class="yui-u">
               <select id="${el}-model" type="text" name="model" tabindex="0" >
                  <option value="-">${msg("label.select.model")}</option>
               <#list lists.selectable.models as t>
                  <option value="${t.value}">${msg("model." + t.name?replace(":", "_"))}</option>
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
<script type="text/javascript">//<![CDATA[
 (function (){
  	
   var Dom = YAHOO.util.Dom,
      Event = YAHOO.util.Event;
  	
  	function initLinkedSelect(sel1,sel2,sel3 ) {
		var sel2Options = new Array();
		var sel3Options = new Array();
		for (var i=0; i < sel2.options.length; i++) {
			sel2Options[i] = new Array(sel2.options[i].text,sel2.options[i].value);
		}
		for (var i=0; i < sel3.options.length; i++) {
			sel3Options[i] = new Array(sel3.options[i].text,sel3.options[i].value);
		}
		sel2.options.length = 0;
		sel2.options[0] = new Option(sel2Options[0][0],sel2Options[0][1]);
		sel3.options.length = 0;
		sel3.options[0] = new Option(sel3Options[0][0],sel3Options[0][1]);
		
		Event.addListener(sel1, "change", function() {
			var fromCode = sel1.options[sel1.selectedIndex].value;
			sel2.options.length = 0;
			sel3.options.length = 0;
			sel3.options[0] = new Option(sel3Options[0][0],sel3Options[0][1]);
			for (i = 0; i < sel2Options.length; i++) {
				if (sel2Options[i][1].indexOf(fromCode) == 0 || sel2Options[i][1]=="-" ) {
					sel2.options[sel2.options.length] = new Option(sel2Options[i][0],sel2Options[i][1].split("-")[1]);
				}
			}
			sel2.options[0].selected = true;
		});
		
		Event.addListener(sel2, "change", function() {
			var fromCode = sel2.options[sel2.selectedIndex].value;
			sel3.options.length = 0;
			for (i = 0; i < sel3Options.length; i++) {
				if (sel3Options[i][1].indexOf(fromCode) == 0 || sel3Options[i][1]=="-") {
					sel3.options[sel3.options.length] = new Option(sel3Options[i][0],sel3Options[i][1].split("-")[2]);
				}
			}
			sel3.options[0].selected = true;
		});
		
		
		
		
	} 
	
	Event.onAvailable("${el}-type",function(e){
			initLinkedSelect(Dom.get("${el}-assocType"),Dom.get("${el}-type"),Dom.get("${el}-model"));
		});

  	
  })();
//]]></script>