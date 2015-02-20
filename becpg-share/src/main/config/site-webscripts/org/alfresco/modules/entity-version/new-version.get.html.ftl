<#assign el=args.htmlid?html>
<div id="${el}-dialog" class="new-version">
   <div class="hd">
      <span id="${el}-header-span"></span>
   </div>
   <div class="bd">
      <form id="${el}-NewEntityVersion-form" method="POST"
            action="${url.context}/proxy/alfresco/becpg/entity/form-checkin">

    	  <input type="hidden" id="${el}-version-hidden" name="version" value=""/>
         <input type="hidden" id="${el}-nodeRef-hidden" name="nodeRef" value=""/>

         <div id="${el}-versionSection-div">
            <#if args.merge?? && args.merge=="true">
            <div class="yui-gd">
            	 <div class="yui-u first">
		     		<label for="${el}-branchToNodeRef">${msg("label.merge.entity")}:<span class="mandatory-indicator">*</span></label>     
		     	</div>
				<div class="yui-u">
 	                <div id="${el}-entities" class="object-finder">        
						<div class="yui-ac" >
							 <div id="${el}-entities-field-autocomplete" class="ac-body" >
										 <span id="${el}-entities-field-toggle-autocomplete" class="ac-toogle"></span>					
										 <input id="${el}-entities-field" type="text" name="-" tabindex="0"  class="yui-ac-input multi-assoc" />
										 <span class="clear" ></span>
							   </div>			
							   <div id="${el}-entities-field-container"></div>
							   <input type="hidden" id="${el}-entities-added" name="branchToNodeRef" />
					   </div>
					</div>
              </div>
			</div>
         </#if>
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
