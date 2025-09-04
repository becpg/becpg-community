<#assign visible_index=0>
<div class="4-col-set">
<#list set.children as item>
	<#if form.mode == "view">
      <@formLib.renderField field=form.fields[item.id] />
   <#else>
		<#assign stacked=form.fields[item.id].control.params.stacked??>
		<#if item.kind != "set" && form.fields[item.id].transitory != true>
		   <#if (visible_index % 4) == 0>
		   <div class="yui-gb"><div class="yui-u first" style="width: 23%">
		   <#else>
		   <div class="yui-u" style="width: 23%">
		   </#if>
		   <@formLib.renderField field=form.fields[item.id] />
		   </div>
		   <#assign nextStacked=item?index != set.children?size - 1 && stacked && (form.fields[set.children[(item?index) + 1].id].control.params.stacked??)!false>
		   <#if !nextStacked>
		   		<#if (((visible_index % 4) == 3) || !item_has_next)></div></#if>
		   		<#assign visible_index++>
		   </#if>
		</#if>
   </#if>
</#list>
</div>