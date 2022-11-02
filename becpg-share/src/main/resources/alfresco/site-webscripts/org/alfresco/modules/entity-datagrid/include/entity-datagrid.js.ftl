 <#include "../../../components/form/form.js.ftl"/>
 
<@script src="${url.context}/res/components/form/date-range.js" group="entity-datalists"/>
<@script src="${url.context}/res/modules/entity-datagrid/entity-columnRenderer.js" group="entity-datalists"></@script>
<@script src="${url.context}/res/modules/entity-datagrid/entity-actions.js" group="entity-datalists"></@script>
<@script src="${url.context}/res/modules/custom-entity-datagrid/custom-entity-actions.js" group="entity-datalists"></@script>
<@script src="${url.context}/res/modules/entity-datagrid/entity-datagrid.js" group="entity-datalists"></@script>
<@script src="${url.context}/res/modules/custom-entity-datagrid/custom-columnRenderers.js" group="entity-datalists"></@script>
<@script src="${url.context}/res/components/comments/comments-list.js" group="comments"/>


<#if config.scoped["EntityDataLists"]?exists && config.scoped["EntityDataLists"].dependencies?exists && config.scoped["EntityDataLists"].dependencies.js?exists>
   <#list config.scoped["EntityDataLists"].dependencies.js as jsFile>
      <@script type="text/javascript" src="${url.context}/res${jsFile}" group="form"/>
   </#list>
</#if>
