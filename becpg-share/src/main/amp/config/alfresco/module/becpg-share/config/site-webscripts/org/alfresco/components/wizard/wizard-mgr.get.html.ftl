<@markup id="css" >
	<#include "../../modules/entity-datagrid/include/entity-datagrid.css.ftl"/>
    <@link href="${url.context}/res/components/wizard/wizard-mgr.css" group="wizard"/>
</@>

<@markup id="js">
 	<@script  src="${url.context}/res/modules/data-lists/datalist-actions.js"/>
	<#include "../../modules/entity-datagrid/include/entity-datagrid.js.ftl"/>
	<@script src="${url.context}/res/components/entity-charact-views/dashlet-resizer.js" />

    <@script src="${url.context}/res/components/wizard/jquery.js" group="wizard"/>
    <@script src="${url.context}/res/components/wizard/jquery-steps.js" group="wizard"/>
    <@script src="${url.context}/res/components/wizard/wizard-mgr.js" group="wizard"/>
</@>

<@markup id="widgets">
   <@inlineScript group="wizard">
		    Alfresco.constants.DASHLET_RESIZE = true && YAHOO.env.ua.mobile === null;
   </@>
 <@inlineScript group="comment">
  	Alfresco.CommentsList.prototype.onReady =  function CommentList_onReady()
      {
         var editFormWrapper = document.createElement("div");
         Dom.addClass(editFormWrapper, "comments-list");
         Dom.addClass(editFormWrapper, "hidden");
         Dom.get(this.id + "-body").appendChild(editFormWrapper);
         this.widgets.editFormWrapper = editFormWrapper;
         
         //beCPG
         this.widgets.onAddCommentClick = Alfresco.util.createYUIButton(this, "add-comment-button", this.onAddCommentClick);
         
         YAHOO.util.Event.addListener(window, "resize", function ()
         {
            if (this.currentEditedRowId)
            {
               this.synchronizeElements(this.widgets.editFormWrapper, this.currentEditedRowId + "-form-container");
            }
            this.resizeCommentDetails();
         }, this, true);
         
         this.setupCommentList();
         this.setupAddCommentForm();
      }
   </@> 
   <@createWidgets group="wizard"/>
</@>

<@markup id="html">
   <@uniqueIdDiv>
	   	<#assign el=args.htmlid?html>
	   	<div id="${el}">
	    	  <#if comments>
	    	 <div class="yui-ge" >
			         <div  class="yui-u first">
			  </#if>  
			  			<div class="clear"></div>
			   			<div class="wizard-mgr">     
				          <h1 id="${el}-wizardTitle" class="hidden"></h1>
				          <div id="${el}-wizard"></div>		
						</div>          		          
	         	<#if comments>
	         		 </div>
	        		  <div class="yui-u wizard-comments">
	          				
					         <div id="${el}-body" class="comments-list">
					            <div id="${el}-add-comment">
					               <div id="${el}-add-form-container" class="theme-bg-color-4 hidden"></div>
					            </div>
					            <div class="comments-list-actions">
					               <div class="left">
					                  <div id="${el}-actions" class="hidden">
					                     <button id="${el}-add-comment-button">${msg("button.addComment")}</button>
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
	         		 
			</#if>
	</div>		    
   </@>
</@>
