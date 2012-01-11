<#assign id = args.htmlid>
<script type="text/javascript">//<![CDATA[
   new beCPG.component.DesignerTree("${id}").setOptions(
   {
      modelNodeRef: "${nodeRef!""}"
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
	      <button id="${id}-configSelect-button" name="configSelect-button">${msg("menu.select.config")}</button>
	   	  <div id="${id}-configSelect-menu" class="yuimenu" style="visibility:hidden;">
	          <div class="bd">
	               <ul>
	                  <#list configs as node >
	                     <li><a href="#"><span class="${node.nodeRef}">${node.displayName}</span></a></li>
	                  </#list>
	                </ul>
	          </div>
	     </div>
      </div>
   </div>
   
   <div class="filter  tree-container" >
   	   <h2>${msg("header.config")}</h2>
	   <div id="${id}-form-tree" class="filter"></div>
	   <h2>${msg("header.model")}</h2>
	   <div id="${id}-model-tree" class="filter"></div>
   </div>
   
</div>
