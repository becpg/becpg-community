<#assign visible_index=0>
<div class="2-col-set">
<#list set.children as item>
	<#if form.mode == "view">
      <@formLib.renderField field=form.fields[item.id] />
   <#else>
		<#if item.kind != "set" && form.fields[item.id].transitory != true>			
		   
		   <div class="yui-g">
		   <div class="row">
		   
		   <@formLib.renderField field=form.fields[item.id] />
		   </div>
			</div>
		</#if>
   </#if>
</#list>
</div>