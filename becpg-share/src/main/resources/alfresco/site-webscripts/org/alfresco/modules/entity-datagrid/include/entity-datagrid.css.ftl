<#include "../../../components/form/form.css.ftl"/>

<@link href="${url.context}/res/css/fixForm.css" group="entity-datalists" />   
<@link href="${url.context}/res/modules/entity-datagrid/entity-datagrid.css" group="entity-datalists" />
<@link href="${url.context}/res/modules/custom-entity-datagrid/custom-entity-datagrid.css" group="entity-datalists" />
<@link href="${url.context}/res/components/comments/comments-list.css" group="comments"/>



<#if config.scoped["EntityDataLists"]?exists && config.scoped["EntityDataLists"].dependencies?exists && config.scoped["EntityDataLists"].dependencies.getChildren("css")?exists>
   <#list config.scoped["EntityDataLists"].dependencies.getChildren("css") as cssFile>
      <@link  type="text/javascript" src="${url.context}/res${cssFile.getAttribute('src')}" group="entity-datalists"/>
   </#list>
</#if>