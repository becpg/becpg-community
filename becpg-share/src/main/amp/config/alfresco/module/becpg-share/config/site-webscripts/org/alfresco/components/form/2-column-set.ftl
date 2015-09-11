<#assign visible_index=0>
<div class="2-col-set">
<#list set.children as item>
	<#if form.mode == "view">
      <@formLib.renderField field=form.fields[item.id] />
   <#else>
		<#if item.kind != "set" && form.fields[item.id].transitory != true>			
		   <#if (visible_index % 2) == 0>
		   <div class="yui-g"><div class="yui-u first">
		   <#else>
		   <div class="yui-u">
		   </#if>
		   <@formLib.renderField field=form.fields[item.id] />
		   </div>
		   <#if ((visible_index % 2) != 0) || !item_has_next></div></#if>
		   <#assign visible_index=visible_index+1>
		</#if>
   </#if>
</#list>
</div>