<#-- Alfresco bug here replace -->
<@markup  id="customDataGrid-js" target="js" action="replace">
   <#-- JavaScript Dependencies -->
   <#include "../form/form.js.ftl"/>
   <@script src="${url.context}/res/components/data-lists/datagrid.js" group="datalists" />
    <@script src="${url.context}/res/components/data-lists/custom-datagrid.js" group="datalists" />
</@>
