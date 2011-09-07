<#assign el=args.htmlid?html>
<div id="${el}-dialog" class="new-version">
   <div class="hd">
      <span id="${el}-header-span"></span>
   </div>
   <div class="bd">
      <form id="${el}-NewEntityVersion-form" method="POST"
            action="${url.context}/proxy/alfresco/becpg/entity/form-checkin">
         <input type="hidden" id="${el}-nodeRef-hidden" name="nodeRef" value=""/>
         <input type="hidden" id="${el}-version-hidden" name="version" value=""/>

         <div id="${el}-versionSection-div">
            <div class="yui-gd">
               <div class="yui-u first">
                  <label for="${el}-minorVersion-radioButton">${msg("label.version")}</label>
               </div>
               <div class="yui-u">
                  <input id="${el}-minorVersion-radioButton" type="radio" name="majorVersion" checked="checked" value="false"/>
                  <label for="${el}-minorVersion-radioButton" id="${el}-minorVersion">${msg("label.minorVersion")}</label>                  
               </div>
            </div>
            <div class="yui-gd">
               <div class="yui-u first">&nbsp;
               </div>
               <div class="yui-u">
                  <input id="${el}-majorVersion-radioButton" type="radio" name="majorVersion" value="true"/>
                  <label for="${el}-majorVersion-radioButton" id="${el}-majorVersion">${msg("label.majorVersion")}</label>
               </div>
            </div>
            <div class="yui-gd">
               <div class="yui-u first">
                  <label for="${el}-description-textarea">${msg("label.comments")}</label>
               </div>
               <div class="yui-u">
                  <textarea id="${el}-description-textarea" name="description" rows="4"></textarea>
               </div>
            </div>
         </div>

         <div class="bdft">
            <input id="${el}-ok-button" type="button" value="${msg("button.ok")}" />
            <input id="${el}-cancel-button" type="button" value="${msg("button.cancel")}" />
         </div>

      </form>

   </div>
</div>

<script type="text/javascript">//<![CDATA[
Alfresco.util.addMessages(${messages}, "Alfresco.module.NewEntityVersion");
//]]></script>
