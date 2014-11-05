<#include "include/alfresco-template.ftl" />
<@templateHeader>
  
	<@link href="${url.context}/res/css/beCPG.css" group="print-details"  media="print" />
	<@link href="${url.context}/res/yui/calendar/assets/calendar.css" group="print-details" media="print"/>
	<@link href="${url.context}/res/components/object-finder/object-finder.css" group="print-details" media="print"/>

	<#if config.global.forms?exists && config.global.forms.dependencies?exists && config.global.forms.dependencies.css?exists>
	   <#list config.global.forms.dependencies.css as cssFile>
	      <@link href="${url.context}/res${cssFile}" group="print-details" media="print"/>
	   </#list>
	</#if>
 	<@link href="${url.context}/res/components/entity-details/entity-print.css" group="print-details" media="print" />

</@>

<@templateBody>
   <@markup id="bd">
	   <div id="bd">
	     <@region id="document-metadata" scope="template"/>
	   </div>
	</@>
</@>
