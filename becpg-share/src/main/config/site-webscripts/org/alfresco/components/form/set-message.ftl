<div class="set-info">
	<#if form.data['prop_message']??>
	 <script type="text/javascript">
	   document.write("${msg(set.label,form.data['prop_message']?split(" - ")[0])?js_string}".replace(/#hostname/g,window.location.hostname ));
	 </script>
	<#else>
	  ${msg(set.label)}
	</#if>
</div>
<#list set.children as item>
         <#if item.kind == "set">
            <@renderSet set=item />
         <#else>
            <@renderField field=form.fields[item.id] />
         </#if>
</#list>
