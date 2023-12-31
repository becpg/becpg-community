<@markup  id="custom-topic" target="js" action="replace" scope="template">
    <#-- JavaScript Dependencies -->
    <@script src="${url.context}/res/components/discussions/createtopic.js" group="discussions" />
 	<@script src="${url.context}/res/components/create-topic/custom-createtopic.js" group="discussions" />
 	<@script src="${url.context}/res/components/people-finder/people-finder.js" group="workflow"/>
</@>


<@markup id="custom-topic" target="html" action="replace" scope="template">
<@link href="${url.context}/res/components/people-finder/people-finder.css" group="discussions"/>

   <@uniqueIdDiv>
   	  <#include "../../include/alfresco-macros.lib.ftl" />
      <#assign el=args.htmlid?html />
      <#assign editMode = ((page.url.args.topicId!"") != "") />
      <div class="page-form-header">
         <h1><#if editMode>${msg("header.edit")}<#else>${msg("header.create")}</#if></h1>
         <hr/>
      </div>
      <div class="page-form-body hidden" id ="${args.htmlid}-topic-create-div">
         <form id="${args.htmlid}-form" method="post" action="">
            <fieldset>
               <input type="hidden" id="${args.htmlid}-topicId" name="topic" value="" />
               <input type="hidden" id="${args.htmlid}-site" name="site" value="" />
               <input type="hidden" id="${args.htmlid}-container" name="container" value="" />
               <input type="hidden" id="${args.htmlid}-page" name="page" value="discussions-topicview" />
      
               <div class="yui-gd">
                  <div class="yui-u first">
                     <label for="${args.htmlid}-title">${msg("label.title")}:</label>
                  </div>
                  <div class="yui-u">
                     <input class="wide" type="text" id="${args.htmlid}-title" name="title" size="80" value=""/>&nbsp;*
                  </div>
               </div>
      
               <div class="yui-gd">
                  <div class="yui-u first">
                     <label for="${args.htmlid}-content">${msg("topicText")}:</label>
                  </div>
                  <div class="yui-u">
                     <textarea rows="8" cols="80" id="${args.htmlid}-content" name="content" class="yuieditor"></textarea>
                  </div>
               </div>
    			
    			<div class="yui-gd">
                  <div class="yui-u first">
                     <label for="${htmlid}-tag-input-field"> ${msg("label.supplierAccountRef")} :</label>
                  </div>
                  <div class="yui-u">
                        <span><input readonly="readonly" type="text" maxlength="256" size="30" id="${el}-supplier" name="supplierUserName" value="" /></span>
                        <span ><input type="button" id="${el}-reassign" value="${msg("button.select")}"></input></span>    
                  </div>
               </div>
      			
               <div class="yui-gd">
                  <div class="yui-u first">
                     <label for="${htmlid}-tag-input-field">${msg("label.tags")}:</label>
                  </div>
                  <div class="yui-u">
                     <#import "/org/alfresco/modules/taglibrary/taglibrary.lib.ftl" as taglibraryLib/>
                     <@taglibraryLib.renderTagLibraryHTML htmlid=args.htmlid />
                  </div>
               </div>
      
               <div class="yui-gd">
                  <div class="yui-u first">&nbsp;</div>
                  <div class="yui-u">
                     <input type="submit" id="${args.htmlid}-submit" value="${msg('action.save')}" />
                     <input type="reset" id="${args.htmlid}-cancel" value="${msg('action.cancel')}" />
                  </div>
               </div>
               <!-- People Finder Dialog -->
             <div style="display: none;">
                <div id="${el}-reassignPanel" class="substitute-edit-header reassign-panel">
                   <div class="hd">${msg("selectSupplier")}</div>
                   <div class="bd">
                      <div style="margin: auto 10px;">
                         <div id="${el}-peopleFinder"></div>
                      </div>
                   </div>
                </div>
             </div>
		         
            </fieldset>
         </form>
         
      </div>
   </@>
</@>