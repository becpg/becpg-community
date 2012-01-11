<#import "/org/alfresco/components/form/form.lib.ftl" as formLib />

<#if error?exists>
   <div class="error">${error}</div>
<#elseif form?exists>

<#assign formId=args.htmlid?js_string + "-form">
<#assign formUI><#if args.formUI??>${args.formUI}<#else>true</#if></#assign>
 <#if formUI == "true">
   <@formLib.renderFormsRuntime formId=formId />
</#if>
      
      <@formLib.renderFormContainer formId=formId>
        <div id="${formId}-tabview" class="yui-navset"> 
			<ul class="yui-nav">
				<#list form.structure as item>
					<#if item.kind == "set">
						<li <#if item_index == 0>class="selected"</#if>><a href="#tab_${item_index}"><em>${item.label}</em></a></li>
					</#if>
				</#list>
			</ul>     				
			<div class="yui-content">
				<#list form.structure as item>
					   <div id="tab_${item_index}">
					   		  <#if item.kind == "set">
				               <@formLib.renderSet set=item />
				            <#else>
				               <@formLib.renderField field=form.fields[item.id] />
				            </#if>
					   </div>		
				 </#list>
			</div> 
         </div>
        </@>
</#if>

<script type="text/javascript">//<![CDATA[
	YAHOO.util.Event.onAvailable('${formId}-tabview', function(){
		var tabView = new YAHOO.widget.TabView('${formId}-tabview');
	},this);
//]]>
</script>
