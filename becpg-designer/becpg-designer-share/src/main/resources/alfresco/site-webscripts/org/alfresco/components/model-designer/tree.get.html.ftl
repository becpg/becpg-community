
<@markup id="css" >
   <#-- CSS Dependencies -->
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/yui/treeview/assets/skins/sam/treeview.css" group="model-designer" />
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/components/model-designer/tree.css" group="model-designer" />
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <@script type="text/javascript" src="${url.context}/res/yui/treeview/treeview.js" group="model-designer"></@script>
	<@script type="text/javascript" src="${url.context}/res/components/model-designer/tree.js" group="model-designer"></@script>
</@>

<@markup id="html">
   <@uniqueIdDiv>
   	<#assign el=args.htmlid?html>
		<script type="text/javascript">//<![CDATA[
		   new beCPG.component.DesignerTree("${el}").setOptions(
		   {
		      modelNodeRef: "${nodeRef!""}"
		   }).setMessages(${messages});
		//]]></script>
		<div id="${el}-body" class="designerTree">
		   <div id="${el}-headerBar" class="header-bar toolbar flat-button theme-bg-2">
		      <div class="left">
		      	  <button id="${el}-modelSelect-button" name="modelSelect-button">${msg("menu.select.model")}&nbsp;&#9662;</button>
			   	  <div id="${el}-modelSelect-menu" class="yuimenu" style="visibility:hidden;">
			          <div class="bd">
			               <ul>
			                  <#list models as node >
			                     <li><a href="#"><span class="${node.nodeRef}<#if node.readOnly??>|${node.readOnly?string}</#if>">${node.displayName}</span></a></li>
			                  </#list>
			                </ul>
			          </div>
			     </div>
			      <button id="${el}-configSelect-button" name="configSelect-button">${msg("menu.select.config")}&nbsp;&#9662;</button>
			   	  <div id="${el}-configSelect-menu" class="yuimenu" style="visibility:hidden;">
			          <div class="bd">
			               <ul>
			                  <#list configs as node >
			                     <li><a href="#"><span class="${node.nodeRef}<#if node.readOnly??>|${node.readOnly?string}</#if>">${node.displayName}</span></a></li>
			                  </#list>
			                </ul>
			          </div>
			     </div>
		      </div>
		   </div>
		   
		   <div class="filter  tree-container" >
		   	   <h2>${msg("header.config")}</h2>
			   <div id="${el}-form-tree" class="filter"></div>
			   <h2>${msg("header.model")}</h2>
			   <div id="${el}-model-tree" class="filter"></div>
		   </div>
		   
		</div>
		</@>
</@>