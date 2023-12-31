
<@markup id="customSearch-css" target="css"  action="after">
   <#-- CSS Dependencies -->
  <#include "../form/form.css.ftl"/>
  
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/documentlibrary/actions.css" group="search"/>
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/modules/documentlibrary/global-folder.css" group="search"/>
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/people-finder/people-finder.css" group="search"/>
	
	
  <@link href="${url.context}/res/css/beCPG.css" group="search"/>
  <@link href="${url.context}/res/components/search/custom-search.css" group="search"/>
</@>

<@markup id="customSearch-js" target="js" action="replace">
   <#-- JavaScript Dependencies -->
   <#include "../form/form.js.ftl"/>
    <@script src="${url.context}/res/components/form/date-range.js" group="form"/>
    <@script src="${url.context}/res/components/form/number-range.js" group="form"/>
   
    <@script type="text/javascript" src="${url.context}/res/components/documentlibrary/actions.js" group="search"/>
    <@script type="text/javascript" src="${url.context}/res/modules/documentlibrary/doclib-actions.js" group="search"/> 
	<@script type="text/javascript" src="${url.context}/res/components/common/common-component-style-filter-chain.js" group="search"/>
	<@script type="text/javascript" src="${url.context}/res/components/documentlibrary/actions-util.js" group="search"/>
	<@script type="text/javascript" src="${url.context}/res/modules/simple-dialog.js" group="search"/>
	<@script type="text/javascript" src="${url.context}/res/modules/documentlibrary/global-folder.js" group="search"/>
	<@script type="text/javascript" src="${url.context}/res/modules/documentlibrary/copy-move-to.js" group="search"/>
	<@script type="text/javascript" src="${url.context}/res/components/people-finder/people-finder.js" group="search"/>

    <@script src="${url.context}/res/js/async-download.js" group="search"/>

	<@script src="${url.context}/res/components/search/search-lib.js" group="search"/>
	<@script src="${url.context}/res/components/search/search.js" group="search"/>

    <@script src="${url.context}/res/components/search/custom-search.js"  group="search" />
  
   <@script src="${url.context}/res/components/search/advsearch.js" group="search"/>
   
</@>
<#--
<@markup id="customAction-js" target="widgets" action="after">
  <@script type="text/javascript" src="${url.context}/res/components/documentlibrary/becpg/core-docLibAction.js" group="custom-actions"/> 
  <@script type="text/javascript" src="${url.context}/res/components/documentlibrary/becpg/plm-docLibAction.js" group="custom-actions"/> 
</@>	
-->

<@markup id="customSearch-html" target="html" action="replace">
   <@uniqueIdDiv>
      <#assign el=args.htmlid>
      <#assign searchconfig=config.scoped['Search']['search']>
      <div id="${el}-body" class="search">
         <#assign context=searchconfig.getChildValue('repository-search')!"context">
         <#if context != "always">
         <div class="search-sites">
            <span <#if context == "none">class="hidden"</#if>><a id="${el}-repo-link" href="#" <#if searchRepo>class="bold"</#if>>${msg('message.repository')}</a></span><#if context != "none"> |</#if>
            <a id="${el}-all-sites-link" href="#" <#if searchAllSites && !searchRepo>class="bold"</#if>>${msg('message.allsites')}</a>
            <#if siteId?length != 0>| <a id="${el}-site-link" href="#" <#if !searchAllSites && !searchRepo>class="bold"</#if>>${msg('message.singlesite', siteTitle)?html}</a></#if>
         </div>
         </#if>
         <div class="search-box-adv">
            <div>
               <input type="text" class="terms" name="${el}-search-text" id="${el}-search-text" value="" maxlength="1024" />
            </div>
            <div>  
               <#-- component to show list of forms, displays current form -->
               <span class="selected-form-button">
                  <span id="${el}-selected-form-button" class="yui-button yui-menu-button">
                     <span class="first-child">
                        <button type="button" tabindex="0"></button>
                     </span>
                  </span>
               </span>
               <#-- menu list of available forms -->
               <div id="${el}-selected-form-list"  style="visibility:hidden" class="yuimenu">
                  <div class="bd">
                     <ul>
                        <#list searchForms as f>
                        <li>
                           <span class="form-type-name" tabindex="0">${f.label?html}</span>
                          <#-- <span class="form-type-description">${f.description?html}</span> -->
                        </li>
                        </#list>
                     </ul>
                  </div>
               </div>
            </div>
            <div>
         	  <span class="adv-search-button">
                  <span id="${el}-adv-search-button" class="yui-button yui-menu-button">
                     <span class="first-child">
                        <button type="button" tabindex="0"></button>
                     </span>
                  </span>
               </span>
               <#-- menu list of available forms -->
               <div id="${el}-adv-search-panel" style="visibility:hidden"  class="yuimenu"> 
	                  <div class="bd">
				         <div id="${el}-forms" class="forms-container form-fields"></div>        
				         <div class="yui-gc form-row">
				            <div class="yui-u first"></div>
				            <#-- search button -->
				            <div class="yui-u align-right">
				               <span id="${el}-search-button-2" class="yui-button yui-push-button search-icon">
				                  <span class="first-child">
				                     <button type="button">${msg('button.search')}</button>
				                  </span>
				               </span>
				            </div>
				         </div>
				      </div>
		         </div>
		     </div>
            <div>
               <span id="${el}-search-button-1" class="yui-button yui-push-button search-icon">
                  <span class="first-child">
                     <button type="button">${msg('button.search')}</button>
                  </span>
               </span>
            </div>

         </div>
        
         
         <div class="yui-ge search-bar theme-bg-color-3">
            <div class="yui-u first">
               <div class="item-select">
			          <button id="${el}-itemSelect-button" name="bulk-edit-itemSelect-button">${msg("menu.select")}&nbsp;&#9662;</button>
			            <div id="${el}-itemSelect-menu" class="yuimenu" style="visibility:hidden;">
			               <div class="bd">
			                  <ul>
			                     <li><a href="#"><span class="selectAll">${msg("menu.select.all")}</span></a></li>
			                     <li><a href="#"><span class="selectInvert">${msg("menu.select.invert")}</span></a></li>
			                     <li><a href="#"><span class="selectNone">${msg("menu.select.none")}</span></a></li>
			                  </ul>
			               </div>
			            </div>
			         </div>
				      <div class="selected-items">
					      <button class="no-access-check" id="${el}-selectedItems-button" name="bulkedit-selectedItems-button">${msg("menu.selected-items")}&nbsp;&#9662;</button>
					      <div id="${el}-selectedItems-menu" class="yuimenu" style="visibility:hidden">
					         <div class="bd">
					             <ul>			                
					                 <li><a type="" rel="" href="#" ><span class="onActionDownload">${msg("menu.selected-items.download")}</span></a></li>
					                 <li><a type="" rel="" href="#" ><span class="onActionAssignWorkflow">${msg("menu.selected-items.assign-process")}</span></a></li>
					                 <li><a type="" rel="content" href="#" ><span class="onActionQuickShare">${msg("menu.selected-items.quick-share")}</span></a></li>
					                 <li><a type="" rel="content" href="#" ><span class="onActionAddToBasket">${msg("menu.selected-items.basket")}</span></a></li>
	                                 <#-- <li><a type="" rel="" href="#" ><span class="onActionCopyTo">${msg("menu.selected-items.copy")}</span></a></li> -->
	                                 <#-- <li><a type="" rel="delete" href="#" ><span class="onActionMoveTo">${msg("menu.selected-items.move")}</span></a></li> -->
	                                 <li><a type="" rel="delete" href="#" ><span class="onActionDeleteMultiple">${msg("menu.selected-items.delete")}</span></a></li>
	                                         
					            </ul>
     				         </div>
					      </div>			
					   </div>
					   
					<#-- beCPG : export -->
					<#if exportSearchTpls?has_content >
				    <span class="yui-button yui-push-button" id="${el}-export-menubutton">
				            <span class="first-child"><button></button></span>
				    </span>
				    <select id="${el}-export-menu" >
				          <option value="-"></option>
				          <#list exportSearchTpls as exportSearchTpl>
				          <option value="${exportSearchTpl.nodeRef}" fileName="${exportSearchTpl.name}.${exportSearchTpl.format?lower_case}" reportTplName="${exportSearchTpl.reportTplName}">${exportSearchTpl.name}</option>
				          </#list>
				    </select>
				    </#if>
					   <#-- beCPG : bulkedit -->
				<span id="${el}-bulk-edit" class="yui-button yui-push-button bulk-edit-button">
				   <button>
						<span>${msg("button.bulkEdit")}</span>
				   </button>
				</span>
				
				<#if showWused >
				<span id="${el}-wused" class="yui-button yui-push-button wused-button">
				   <button>
						<span>${msg("button.wused")}</span>
				   </button>
				</span>
				</#if>
				 <div class="yui-ge">
			      <div class="yui-u first align-center">
	               <div id="${el}-search-info" class="search-info">${msg("search.info.searching")}</div>
	               <div id="${el}-paginator-top" class="paginator hidden"></div>
               	  </div>
                </div>
            </div>
            <div class="yui-u align-right">
               <span class="yui-button yui-push-button" id="${el}-sort-menubutton">
                  <span class="first-child"><button></button></span>
               </span>
               <select id="${el}-sort-menu" class="yuimenu hidden">
                  <#list sortFields as sort>
                  <option value="${sort.type!""}">${sort.label}</option>
                  </#list>
               </select>
              
				
            </div>
         </div>
         
         <div id="${el}-help" class="yui-g theme-bg-color-2 help hidden">
            <span class="title">${msg("help.title")}</span>
            <div class="yui-u first">
               <span class="subtitle">${msg("help.subtitle1")}</span>
               <span>${msg("help.info1")}</span>
               <span class="example">${msg("help.example1")}</span>
               <span>${msg("help.result1")}</span>
               <span>${msg("help.info2")}</span>
               <span class="example">${msg("help.example2")}</span>
               <span>${msg("help.result2")}</span>
               <span>${msg("help.info3")}</span>
               <span class="example">${msg("help.example3")}</span>
               <span>${msg("help.result3")}</span>
               <span>${msg("help.info4")}</span>
               <span class="example">${msg("help.example4")}</span>
               <span>${msg("help.result4")}</span>
               <span>${msg("help.info5")}</span>
               <span class="example">${msg("help.example5")}</span>
            </div>
            <div class="yui-u">
               <span class="subtitle">${msg("help.subtitle2")}</span>
               <span>${msg("help.info6")}</span>
               <span class="example">${msg("help.example6")}</span>
               <span>${msg("help.result6")}</span>
               <span>${msg("help.info7")}</span>
               <span class="example">${msg("help.example7")}</span>
               <span>${msg("help.result7")}</span>
               <span>${msg("help.info8")}</span>
               <span>${msg("help.info9")}</span>
               <span class="example">${msg("help.example9")}</span>
               <span>${msg("help.result9")}</span>
               <span>${msg("help.info10")}</span>
               <span class="example">${msg("help.example10")}</span>
               <span>${msg("help.result10")}</span>
            </div>
         </div>
         
         <div id="${el}-results" class="results"></div>
         
         <div id="${el}-search-bar-bottom" class="yui-gc search-bar search-bar-bottom theme-bg-color-3 hidden">
            <div class="yui-u first">
               <div class="search-info">&nbsp;</div>
               <div id="${args.htmlid}-paginator-bottom" class="paginator paginator-bottom"></div>
            </div>
         </div>
      </div>
   </@>
</@>

