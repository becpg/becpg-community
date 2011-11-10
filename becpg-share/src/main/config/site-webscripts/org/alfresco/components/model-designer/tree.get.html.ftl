<#assign id = args.htmlid>
<script type="text/javascript">//<![CDATA[
   new beCPG.component.DesignerTree("${id}").setOptions(
   {
      modelNodeRef: "${page.url.templateArgs.nodeRef!""}"
   }).setMessages(${messages});
//]]></script>
<div id="${id}-body" class="designerTree">
   <div id="${id}-headerBar" class="header-bar toolbar flat-button theme-bg-2">
      <div class="left">
      	  <button id="${id}-modelSelect-button" name="modelSelect-button">${msg("menu.select.model")}</button>
	   	  <div id="${id}-modelSelect-menu" class="yuimenu" style="visibility:hidden;">
	          <div class="bd">
	               <ul>
	                  <#list models as node >
	                     <li><a href="#"><span class="${node.nodeRef}">${node.displayName}</span></a></li>
	                  </#list>
	                </ul>
	          </div>
	     </div>
         <span id="${id}-newModelButton" class="yui-button yui-push-button new-model">
             <span class="first-child">
                 <button type="button">&nbsp;</button>
             </span>
         </span>
      </div>
   </div>
   
   <h2>${msg("header.model")}</h2>
   <div id="${id}-tree" class="filter"></div>

</div>
