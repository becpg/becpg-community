
<@markup id="custom-alfrescoResources" target="alfrescoResources" action="after">
 	<@script type="text/javascript" src="${url.context}/res/js/beCPG.js" group="template-common"/>
    <@script type="text/javascript" src="${url.context}/res/js/beCPGUtils.js" group="template-common"/>
</@markup>


<@markup id="custom-css-alfrescoResources" target="resources" action="after">
    <@link rel="stylesheet" type="text/css" href="${url.context}/res/css/beCPG.css" group="template-common" />
</@markup>

<#-- Please do not remove 
<@markup id="google-analytics"  target="js" action="after">
   <#assign fc=config.scoped["Analytics"]["provider"]>
   <#if fc.getChildValue("provider-id") == "beCPG">
	  <img src="${fc.getChildValue("provider-url")}" alt="becpg-analytics"></img>
   </#if>
</@markup>
-->
