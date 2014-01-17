
<@markup id="css" >
   <#-- CSS Dependencies -->
   <#include "../form/form.css.ftl"/>
  <@link href="${url.context}/res/css/fixForm.css" group="project-list" />
  <@link href="${url.context}/res/components/project/project-commons.css" group="project-list" />
  <@link href="${url.context}/res/components/dashlets/project-list.css" group="project-list" />
  <@link href="${url.context}/res/components/comments/comments-list.css" group="comments"/>
  
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
    <#include "../form/form.js.ftl"/>
  	<@script src="${url.context}/res/components/documentlibrary/becpg/fileIcons.js"  group="project-list"></@script>
   <@script src="${url.context}/res/components/project/project-commons.js"  group="project-list"></@script>
	<@script src="${url.context}/res/components/dashlets/project-dashlet.js"  group="project-list"></@script>
	<@script src="${url.context}/res/components/comments/comments-list.js" group="comments"/>
</@>


<@markup id="html">
   <@uniqueIdDiv>
		<#include "../../include/alfresco-macros.lib.ftl" />
		<#assign el=args.htmlid?html>
		<#assign prefFilter = preferences.filter!"InProgress">
		<#assign prefView = preferences.view!"projects">
		<#assign prefSimpleView = preferences.simpleView!true>
		<script type="text/javascript">//<![CDATA[
		(function()
		{
		   new beCPG.dashlet.ProjectDashlet("${el}").setOptions(
		   {
		      filter: "${prefFilter?js_string}",
		      view:"${prefView?js_string}",
		      validFilters: [<#list filters as filter>"${filter.type?js_string}"<#if filter_has_next>,</#if></#list>],
		      validViews: [<#list views as view>"${view.type?js_string}"<#if view_has_next>,</#if></#list>],
		      simpleView: ${prefSimpleView?string?js_string},
		      maxItems: ${maxItems?c}
		   }).setMessages(${messages});
		   new Alfresco.widget.DashletResizer("${el}", "${instance.object.id}");
		   new Alfresco.widget.DashletTitleBarActions("${el}").setOptions(
		   {
		      actions:
		      [
		         {
		            cssClass: "help",
		            bubbleOnClick:
		            {
		               message: "${msg("dashlet.help")?js_string}"
		            },
		            tooltip: "${msg("dashlet.help.tooltip")?js_string}"
		         }
		      ]
		   });
		})();
		//]]></script>
		
		<div class="dashlet project-list">
		   <div class="title">${msg("header")}</div>
		   <div class="toolbar flat-button">
		      <div class="hidden">
		      	<div class="search-box">
			         <span id="${el}-search_more" class="yui-button yui-menu-button">
			            <span class="first-child" style="background-image: url(${url.context}/res/components/images/header/search-menu.png)">
			               <button type="button" title="${msg("header.search.description")}" tabindex="0"></button>
			            </span>
			         </span>
			         <input id="${el}-searchText" type="text" maxlength="1024" />
			      </div>
			      <div id="${el}-searchmenu_more" class="yuimenu yui-overlay yui-overlay-hidden">
			         <div class="bd">
			            <ul class="first-of-type">
			               <li><a style="background-image: url(${url.context}/res/components/images/header/advanced-search.png); background-repeat: no-repeat;" title="${msg("header.advanced-search.description")}" href="${siteURL("advsearch")}">${msg("header.advanced-search.label")}</a></li>
			            </ul>
			         </div>
			      </div>
		         <span class="align-left yui-button yui-menu-button" id="${el}-views">
		            <span class="first-child">
		               <button type="button" tabindex="0"></button>
		            </span>
		         </span>
		         <select id="${el}-views-menu">
		         <#list views as view>
		            <option value="${view.type?html}">${msg("view." + view.type)}</option>
		         </#list>
		         </select>
		          <span class="align-left yui-button yui-menu-button" id="${el}-filters">
		            <span class="first-child">
		               <button type="button" tabindex="0"></button>
		            </span>
		         </span>
		         <select id="${el}-filters-menu">
		         <#list filters as filter>
		            <option value="${filter.type?html}">${msg("filter." + filter.type)}</option>
		         </#list>
		         </select>
		         <div id="${el}-paginator" class="paginator"></div>
		         <div id="${el}-simpleDetailed" class="align-right simple-detailed yui-buttongroup inline">
		            <span class="yui-button yui-radio-button simple-view<#if prefSimpleView> yui-button-checked yui-radio-button-checked</#if>">
		               <span class="first-child">
		                  <button type="button" tabindex="0" title="${msg("button.view.simple")}"></button>
		               </span>
		            </span>
		            <span class="yui-button yui-radio-button detailed-view<#if !prefSimpleView> yui-button-checked yui-radio-button-checked</#if>">
		               <span class="first-child">
		                  <button type="button" tabindex="0" title="${msg("button.view.detailed")}"></button>
		               </span>
		            </span>
		         </div>
		         <div class="clear"></div>
		      </div>
		   </div>
		   <div class="body scrollableList" <#if args.height??>style="height: ${args.height}px;"</#if>>
		      <div id="${el}-documents"></div>
		   </div>
		</div>

	</@>

</@>