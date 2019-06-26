<#assign el=args.htmlid?html>
<script type="text/javascript">//<![CDATA[
new beCPG.module.CreateDesignerElement('${el}').setMessages(${messages});
//]]></script>
<div id="${el}" class="create-element">
   <div id="${el}-dialogTitle" class="hd">${msg("title")}</div>
   <div class="bd">
      <form id="${el}-form" action="" method="post">
      	 <div class="yui-gd"  >
            <div class="yui-u first"><label for="${el}-name">${msg("label.name")}:</label></div>
            <div class="yui-u"><input id="${el}-name" type="text" name="name"  />&nbsp;*</div>
         </div>
         <div class="yui-gd"  id="${el}-assocType-container" style="display:none;">	
            <div class="yui-u first"><label for="${el}-assocType">${msg("label.assocType")}:</label></div>
            <div class="yui-u">
               <select id="${el}-assocType" type="text" name="assocType"  >
                  <option value="-" >${msg("label.select.assocType")}</option>
               <#list lists.selectable.assocs as t>
                  <option value="${t.value}">${msg("assocType." + t.name?replace(":", "_"))}</option>
               </#list>
               </select>&nbsp;*
            </div>
         </div>
          <div class="yui-gd" id="${el}-type-container" style="display:none;" >	
            <div class="yui-u first"><label for="${el}-type">${msg("label.type")}:</label></div>
            <div class="yui-u">
               <select id="${el}-type" type="text" name="type" >
                  <option value="-">${msg("label.select.type")}</option>
               <#list lists.selectable.types as t>
                  <option value="${t.value}">${msg("type." + t.name?replace(":", "_"))}</option>
               </#list>
               </select>&nbsp;*
            </div>
         </div>
          <div class="yui-gd" id="${el}-model-container" style="display:none;">	
            <div class="yui-u first"><label for="${el}-model">${msg("label.model")}:</label></div>
            <div class="yui-u">
               <select id="${el}-model" type="text" name="model" >
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
