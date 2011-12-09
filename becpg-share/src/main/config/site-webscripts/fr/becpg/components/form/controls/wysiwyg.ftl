<#if field.control.params.height?exists><#assign height=field.control.params.height><#else><#assign height=100></#if>
<#if field.control.params.width?exists><#assign width=field.control.params.width><#else><#assign width=420></#if>
<#if field.control.params.appearance?exists><#assign appearance=field.control.params.appearance><#else><#assign appearance="default"></#if>

<script type="text/javascript">//<![CDATA[
(function()
{
 new Alfresco.util.RichEditor(Alfresco.constants.HTML_EDITOR, "${fieldHtmlId}",
 {
	 height: ${height},
	 width: ${width},
	 theme: 'advanced',
	 <#if appearance == "full">	
		 theme_advanced_buttons1:"bold,italic,underline,strikethrough,separator,fontselect,fontsizeselect",
		 theme_advanced_buttons2:"link,unlink,image,separator,justifyleft,justifycenter,justifyright,justifyfull,separator,bullist,numlist,separator,undo,redo,separator,forecolor,backcolor",
		 theme_advanced_buttons3: null,
	 <#elseif appearance == "plugins">
		 theme_advanced_buttons1:"bold,italic,underline,strikethrough,separator,fontselect,fontsizeselect",
		 theme_advanced_buttons2:"link,unlink,image,separator,justifyleft,justifycenter,justifyright,justifyfull,separator,bullist,numlist,separator,undo,redo,separator,forecolor,backcolor",
		 plugins:"fullscreen,table,emotions",
		 theme_advanced_buttons3: "fullscreen,table,emotions",			 
	 <#else>
		 theme_advanced_buttons1: "bold,italic,underline,|,bullist,numlist,|,forecolor,backcolor,|,undo,redo,removeformat",
		 theme_advanced_buttons2: null,
		 theme_advanced_buttons3: null,
	 </#if>
	 theme_advanced_toolbar_location: "top",
	 theme_advanced_toolbar_align: "left",
	 theme_advanced_statusbar_location: "bottom",
	 theme_advanced_resizing: true,
	 theme_advanced_path: false,
	 language: 'en'         
 }).render();
})();
//]]></script>

<div class="form-field">
   <#if form.mode == "view">
      <div class="viewmode-field">
         <#if field.mandatory && field.value == "">
            <span class="incomplete-warning"><img src="${url.context}/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
         </#if>
         <span class="viewmode-label">${field.label?html}:</span>
         <span class="viewmode-value">${field.value}</span>
      </div>
   <#else>
      <label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
      <textarea id="${fieldHtmlId}" name="${field.name}" height="2" width="60"
                <#if field.description?exists>title="${field.description}"</#if>
                <#if field.control.params.styleClass?exists>class="${field.control.params.styleClass}"</#if>
                <#if field.disabled>disabled="true"</#if>>${field.value}</textarea>
   </#if>
</div>