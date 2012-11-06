<#include "../component.head.inc">
<#include "../form/form.get.head.ftl">


<!-- DataGrid -->

<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/modules/entity-datagrid/entity-datagrid.css" />
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/modules/custom-entity-datagrid/entity-datagrid.css" />

<@script type="text/javascript" src="${page.url.context}/res/modules/entity-datagrid/entity-columnRenderer.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/components/project/columnRenderers.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/modules/entity-datagrid/entity-actions.js"></@script>

<@script type="text/javascript" src="${page.url.context}/res/modules/entity-datagrid/groupeddatatable.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/modules/entity-datagrid/entity-datagrid.js"></@script>

<!-- Common Workflow Actions -->
<@script type="text/javascript" src="${page.url.context}/res/components/workflow/workflow-actions.js"></@script>

<!-- Gantt -->
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/project/jsgantt.css" />
<@script type="text/javascript" src="${page.url.context}/res/components/project/jsgantt.js"></@script>

<!-- Project List -->
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/project/project-list.css" />
<@script type="text/javascript" src="${page.url.context}/res/components/project/project-list.js"></@script>

