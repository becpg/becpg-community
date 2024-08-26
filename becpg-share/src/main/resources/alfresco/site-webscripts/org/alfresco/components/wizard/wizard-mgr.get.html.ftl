<#include "../documentlibrary/include/documentlist_v2.lib.ftl" />
<#include "../form/form.dependencies.inc">

<@markup id="css" >
	  
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/documentlibrary/toolbar.css" group="documentlibrary"/>
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/documentlibrary/documentlist_v2.css" group="documentlibrary"/>
   <@viewRenderererCssDeps/>
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/documentlibrary/custom-documentlist.css" group="documentlibrary"/>
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/documentlibrary/custom-toolbar.css" group="documentlibrary"/>
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/entity-charact-views/documents-view.css" group="documentlibrary"/>
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/wizard/wizard-toolbar.css" group="wizard-toolbar" />
   

	<#include "../../modules/entity-datagrid/include/entity-datagrid.css.ftl"/>
    <@link href="${url.context}/res/components/wizard/wizard-mgr.css" group="wizard"/>
    <@link href="${url.context}/res/components/entity-catalog/entity-catalog.css" group="wizard"/>
  
</@>

<@markup id="js">
   <@script type="text/javascript" src="${url.context}/res/modules/documentlibrary/doclib-actions.js" group="documentlibrary"/> 
   <@script type="text/javascript" src="${url.context}/res/components/entity-charact-views/documents-view.js" group="documentlibrary"/>
   <@script type="text/javascript" src="${url.context}/res/components/documentlibrary/toolbar.js" group="documentlibrary"/>
   <@script type="text/javascript" src="${url.context}/res/components/documentlibrary/documentlist.js" group="documentlibrary"/>
   <@viewRenderererJsDeps/>
   <@script type="text/javascript" src="${url.context}/res/components/documentlibrary/custom-documentlist.js"  group="documentlibrary" />
   <@script type="text/javascript" src="${url.context}/res/components/documentlibrary/custom-toolbar.js"  group="documentlibrary" />


 	<@script  src="${url.context}/res/modules/data-lists/datalist-actions.js"/>
	<#include "../../modules/entity-datagrid/include/entity-datagrid.js.ftl"/>
	<@script src="${url.context}/res/components/entity-charact-views/dashlet-resizer.js" />
	<@script src="${url.context}/res/components/entity-catalog/entity-catalog.js" group="wizard"/>

    <@script src="${url.context}/res/js/lib/jquery-1.12.4/jquery-1.12.4.min.js" group="wizard"/>
    <@script src="${url.context}/res/components/wizard/jquery-steps.js" group="wizard"/>
    <@script src="${url.context}/res/components/wizard/wizard-mgr.js" group="wizard"/>
    <@script src="${url.context}/res/components/wizard/wizard-toolbar.js" group="wizard-toolbar"/>

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
    <div class="wizard-container">
      <div class="wizard-content">
         <div class="datalist-toolbar toolbar">
       		 <div class="header-bar flat-button theme-bg-2">
       			 <div class="left"><h1 id="${el}-wizardTitle" class="hidden"></h1></div>
       		     <div id="${el}-wizard-toolbar"  class="right"></div>
       		</div>		 
        </div>
        <div class="clear"></div>
        <div class="wizard-mgr">
           <div id="${el}-wizard"></div>
        </div>
        <#if catalogId??>
          <div class="wizard-catalog" class="hidden">
             <div id="${el}-step-step1_cat">
                 <div id="${el}-step-step1_cat-entity-catalog"></div>
                </div>
             </div>
         </#if>
        <#if comments>
          <div  id="${el}-commentsList" class="wizard-comments hidden">
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
              <hr class="hidden" />
              <div id="${el}-comments-list"></div>
              <hr class="hidden" />
              <div class="comments-list-actions">
                <div class="left"></div>
                <div class="right">
                  <div id="${el}-paginator-bottom"></div>
                </div>
                <div class="clear"></div>
              </div>
            </div>
          </div>
        </#if>
        	<#-- template -->
		    <div id="custom-toolBar-template-button" class="hidden" >
			    <span class="yui-button yui-push-button">
			       <span class="first-child">
			             <button type="button" ></button>
			       </span>
			</span>
		</div>
      </div>
    </div>
  </@uniqueIdDiv>
</@markup>

