
<div class="filter">
      <h2>${msg("header." + args.assoc?replace(":","_"))}</h2>
      <ul class="filterLink">
      <#list filters as filter>
         <li><span class="filterform"><a class="filter-link" rel="{assoc_${args.assoc?replace(":","_")}_added:'${filter.nodeRef?js_string}'}" href="#">${filter.itemData["prop_cm_name"].displayValue?html}</a></span></li>
      </#list>
      </ul>
 </div>

<script type="text/javascript">//<![CDATA[
      new Alfresco.component.BaseFilter("Alfresco.component.AllFilter", "${args.htmlid?js_string}").setFilterIds(["args.assoc"]);
   //]]>
</script>

   