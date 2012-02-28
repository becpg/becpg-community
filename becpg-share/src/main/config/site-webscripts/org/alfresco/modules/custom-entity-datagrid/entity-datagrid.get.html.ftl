<#include "../../components/component.head.inc">

<@markup id="customEntityResources" target="resources" action="after">
   <@script type="text/javascript" src="${url.context}/res/modules/custom-entity-datagrid/columnRenderers.js"></@script>
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/modules/custom-entity-datagrid/entity-datagrid.css" />
</@markup>