<@standalone>
   
   <@markup id="css" >
      <#include "../preview/include/web-preview-css-dependencies.lib.ftl" />
  	  <@link href="${url.context}/res/components/entity-charact-views/reports-view.css" group="entity-datalists"/>
   </@>
   
   <@markup id="js" >
      <#include "../preview/include/web-preview-js-dependencies.lib.ftl" />
	  <@inlineScript group="web-preview">
		    beCPG.constants.SHOW_DOWNLOAD_LINKS = ${showAdditionalDownloadLinks?string};
	  </@>
      <@script src="${url.context}/res/components/entity-charact-views/reports-view.js" group="web-preview"/>
   </@>



   <@markup id="widgets">
      <#if node??>
         <@createWidgets group="${dependencyGroup}"/>
      </#if>
   </@>

   <@markup id="html">
      <@uniqueIdDiv>
         <#if node??>
            <#assign el=args.htmlid?html>
			<div class="hidden" id="toolbar-contribs-${el}">
				<#if reports?? && reports?size &gt; 0 >

                       <div class="entity-picker-report">
	                    <input id="${el}-entityReportPicker-button" type="button" name="${el}-entityReportPicker-button" value="${msg("picker.report.choose")}" ></input>
					      <select id="${el}-entityReportPicker-select"  name="${el}-entityReportPicker-select">
					      	<#list reports?sort_by("templateName") as report>
					      		<option value="${report.nodeRef}">${report.templateName?replace(".rptdesign", "")}</option>
					      	</#list>
					      </select>
						</div>
				   	</#if>
          </div>

         <div id="${el}-body" class="web-preview">
            <div id="${el}-previewer-div" class="previewer">
               <div class="message"></div>
            </div>
         </div>
         </#if>
      </@>
   </@>

</@standalone>