<#import "workflow.lib.ftl" as workflow/>
<#import "filter/filter.lib.ftl" as filter/>
<#assign el=args.htmlid?js_string>
<div id="${el}-body" class="npd-list">
   <div class="yui-g npd-list-bar flat-button">
      <div class="yui-u first">
         <h2 id="${el}-filterTitle" class="thin">
            &nbsp;
         </h2>
      </div>
      <div class="yui-u">
         <div id="${el}-paginator" class="paginator">&nbsp;</div>
      </div>
   </div>
   <div id="${el}-npds" class="npds"></div>
</div>
<script type="text/javascript">//<![CDATA[
   new beCPG.component.NpdList("${el}").setOptions(
   {
      filterParameters: <@filter.jsonParameterFilter filterParameters />,
      hiddenWorkflowNames: <@workflow.jsonHiddenTaskTypes hiddenWorkflowNames/>,
      workflowDefinitions: <@workflow.jsonWorkflowDefinitions workflowDefinitions/>,
      maxItems: ${maxItems!"50"}
   }).setMessages(
      ${messages}
   );
//]]></script>
