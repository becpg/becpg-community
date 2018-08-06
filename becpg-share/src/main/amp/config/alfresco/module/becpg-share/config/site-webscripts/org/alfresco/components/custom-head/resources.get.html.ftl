
<@markup id="custom-alfrescoResources" target="alfrescoResources" action="after">
	<@script type="text/javascript" src="${url.context}/res/modules/entity-process/entity-process.js" />
 	<@script type="text/javascript" src="${url.context}/res/js/beCPG.js" group="template-common"/>
    <@script type="text/javascript" src="${url.context}/res/js/beCPGUtils.js" group="template-common"/>
</@markup>


<@markup id="custom-css-alfrescoResources" target="resources" action="after">
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/modules/entity-process/entity-process.css" />
    <@link rel="stylesheet" type="text/css" href="${url.context}/res/css/beCPG.css" group="template-common" />
	<#if user.capabilities["isbeCPGExternalUser"]?? && user.capabilities["isbeCPGExternalUser"] >
    	<@link rel="stylesheet" type="text/css" href="${url.context}/res/css/external.css" group="template-common" />
   </#if>
</@markup>

