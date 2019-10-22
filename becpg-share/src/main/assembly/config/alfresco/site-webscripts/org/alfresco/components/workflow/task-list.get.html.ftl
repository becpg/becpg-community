<@markup id="css" >
   <#-- CSS Dependencies -->
   <@link href="${url.context}/res/modules/entity-datagrid/entity-datagrid.css" group="entity-datalists" />
   <@link href="${url.context}/res/modules/custom-entity-datagrid/custom-entity-datagrid.css" group="entity-datalists" />
   <@link href="${url.context}/res/modules/custom-entity-datagrid/project-columnRenderers.css" group="entity-datalists" />
   <@link href="${url.context}/res/components/workflow/task-list.css" group="workflow"/>
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <@script src="${url.context}/res/modules/entity-datagrid/entity-columnRenderer.js" group="entity-datalists"></@script>
   <@script src="${url.context}/res/modules/custom-entity-datagrid/custom-columnRenderers.js" group="entity-datalists"></@script>
   <@script src="${url.context}/res/modules/custom-entity-datagrid/project-columnRenderers.js" group="entity-datalists"></@script>
   
   <@script src="${url.context}/res/components/workflow/workflow-actions.js" group="workflow"/>
   <@script src="${url.context}/res/components/workflow/task-list.js" group="workflow"/>
</@>

<@markup id="widgets">
   <@createWidgets group="workflow"/>
</@>

<@markup id="html">
   <@uniqueIdDiv>
      <#import "workflow.lib.ftl" as workflow/>
      <#import "filter/filter.lib.ftl" as filter/>
      <#assign el=args.htmlid?html>
      <div id="${el}-body" class="task-list">
         <div class="yui-g task-list-bar flat-button theme-bg-color-1">
            <div class="yui-u first">
	             <h2 id="${el}-filterTitle" class="thin">
                  &nbsp;
               </h2>
            </div>
            <div class="yui-u">
               <div id="${el}-paginator" class="paginator">&nbsp;</div>
            </div>
         </div>
         <div id="${el}-tasks" class="tasks"></div>
         <div class="yui-u">
			<div id="${el}-paginatorBottom" class="paginator">&nbsp;</div>
		</div>
      </div>
   </@>
</@>