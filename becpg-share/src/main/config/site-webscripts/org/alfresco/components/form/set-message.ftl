<div class="set-info">
 ${set.label}
</div>
<#list set.children as item>
         <#if item.kind == "set">
            <@renderSet set=item />
         <#else>
            <@renderField field=form.fields[item.id] />
         </#if>
</#list>
