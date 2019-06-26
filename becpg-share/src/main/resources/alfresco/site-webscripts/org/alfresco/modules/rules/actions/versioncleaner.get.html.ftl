<#assign el=args.htmlid?html>
<div id="${el}-dialog" class="versioncleaner">
   <div class="hd">${msg("header")}</div>
   <div class="bd">
      <form id="${el}-form" method="POST" action="" enctype="application/json">
         <div class="yui-gd">
            <div class="yui-u first"><label for="${el}-version-type">${msg("label.version")}:</label></div>
            <div class="yui-u">
               <input id="${el}-version-type-minor" type="radio" name="versionType" tabindex="0" value="minor"/><label for="${el}-version-type-minor">${msg("label.minor")}</label>
               <input id="${el}-version-type-major" type="radio" name="versionType" tabindex="0" value="major"/><label for="${el}-version-type-major">${msg("label.major")}</label>
               <input id="${el}-version-type-all" type="radio" name="versionType" tabindex="0" value="all"/><label for="${el}-version-type-all">${msg("label.all")}</label>
            </div>
         </div>
          <div class="yui-gd">
            <div class="yui-u first"><label for="${el}-number-of-version">${msg("label.numberOfVersion")}:</label></div>
            <div class="yui-u">
               <input id="${el}-number-of-version" type="text" name="numberOfVersion" size="2" tabindex="0" />
            </div>
         </div>
         <div class="yui-gd">
            <div class="yui-u first"><label for="${el}-number-of-day">${msg("label.numberOfDay")}:</label></div>
            <div class="yui-u">
               <input id="${el}-number-of-day" type="text" name="numberOfDay" size="2" tabindex="0" />
            </div>
         </div>
         <div class="yui-gd">
            <div class="yui-u first"><label for="${el}-number-by-day">${msg("label.numberByDay")}:</label></div>
            <div class="yui-u">
               <input id="${el}-number-by-day" type="text" name="numberByDay" size="2" tabindex="0" />
            </div>
         </div>
         <div class="bdft">
            <input type="submit" id="${el}-ok-button" value="${msg("button.ok")}" tabindex="0"/>
            <input type="button" id="${el}-cancel-button" value="${msg("button.cancel")}" tabindex="0"/>
         </div>
      </form>
   </div>
</div>
<script type="text/javascript">//<![CDATA[
Alfresco.util.addMessages(${messages}, "Alfresco.module.RulesVersioningAction");
//]]></script>