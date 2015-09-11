<#if nodeRef??>
   <#assign el=args.htmlid?html>
     <div id="${el}-dialog" class="change-type">
			<div id="${el}-dialogTitle" class="hd">${msg("header.comments")}</div>
			<div class="bd">
			 <div id="${el}">
	         <div id="${el}-body" class="comments-list">
	            <div id="${el}-add-comment">
	               <div id="${el}-add-form-container" class="theme-bg-color-4 hidden"></div>
	            </div>
	            <div class="comments-list-actions">
	               <div class="left">
	                  <div id="${el}-actions" class="hidden">
	                     <button class="alfresco-button" name=".onAddCommentClick">${msg("button.addComment")}</button>
	                  </div>
	               </div>
	               <div class="right">
	                  <div id="${el}-paginator-top"></div>
	               </div>
	               <div class="clear"></div>
	            </div>
	            <hr class="hidden"/>
	            <div id="${el}-comments-list"></div>
	            <hr class="hidden"/>
	            <div class="comments-list-actions">
	               <div class="left">
	               </div>
	               <div class="right">
	                  <div id="${el}-paginator-bottom"></div>
	               </div>
	               <div class="clear"></div>
	            </div>
	         </div>
         </div>
   </div>
</div>



	<script type="text/javascript">//<![CDATA[
			(function()
			{
			
			Alfresco.CommentsList.prototype.synchronizeElements =  function synchronizeElements(syncEl, sourceEl)
	      {
	         var sourceYuiEl = new YAHOO.util.Element(sourceEl),
            syncYuiEl = new YAHOO.util.Element(syncEl),
            region = YAHOO.util.Dom.getRegion(sourceYuiEl.get("id"));
				region2 = YAHOO.util.Dom.getRegion("${el}-dialog");

	         syncYuiEl.setStyle("position", "absolute");
	         syncYuiEl.setStyle("left", (region.left-region2.left) + "px");
	         syncYuiEl.setStyle("top", (region.top-region2.top) + "px");
	         syncYuiEl.setStyle("width", region.width + "px");
	         syncYuiEl.setStyle("height", region.height + "px");
	      }
	      
	       new Alfresco.CommentsList('${el}').setOptions(
				   {
				        nodeRef : "${nodeRef?string}",
				        siteId : "${site!""?string}",
				        maxItems : 10,
				        <#if activityParameters??>
					      activity :{
					            itemTitle: "${activityParameters.itemTitle}",
					            page: "${activityParameters.page}",
					            pageParams:
					            {
					               nodeRef: "${activityParameters.pageParams.nodeRef}"
					               <#if activityParameters.pageParams.list??>, list : "${activityParameters.pageParams.list}"</#if>
					            }
					         },
				         </#if>
				         editorConfig : {
				            menu: {},
          					toolbar: "bold italic underline | bullist numlist | forecolor backcolor | undo redo removeformat",
           					statusbar: false,
				            language: "${localeString!""?string}"
				          }
				   }).setMessages(${messages});
			   })();
		//]]></script>
		
 </#if>