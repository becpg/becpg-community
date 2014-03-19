<#include "include/alfresco-template.ftl" />
<@templateHeader>
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/templates/designer/designer.css" group="model-designer" />
	<@script type="text/javascript" src="${url.context}/res/modules/data-lists/datalist-actions.js" group="model-designer" ></@script>
	<@script type="text/javascript" src="${url.context}/res/templates/designer/designer.js" group="model-designer"></@script>
  
   <@markup id="resizer">
	   <script type="text/javascript">//<![CDATA[
	      (new Alfresco.widget.Resizer("modelTree")).DEFAULT_FILTER_PANEL_WIDTH = 300;  
	       new beCPG.widget.separator("dsg-content-sep");
	   //]]></script>
   </@>
   
</@>

<@templateBody>
	 <@markup id="alf-hd">
	   <div id="alf-hd">
	      <@region scope="global" id="share-header" chromeless="true"/>
	   </div>
  	 </@>
    <@markup id="bd">
	   <div id="bd">
	        <div class="yui-t1" id="becpg-model-designer">
	           <div id="yui-main">
	              <div class="yui-b" id="alf-content">
		              <div class="yui-ge">
		                <div class="yui-u first" id="dsg-content">
			                <@region id="toolbar" scope="template" protected=true />
			                <div class="designer-form-container">  
				                <div id="modelFormDiv"  >       
				               	 <@region id="modelForm" scope="template" protected=true />
				                </div>
				                <div id="modelFormDataGridDiv" style="display:none;"  >       
				                	<@region id="modelFormDataGrid" scope="template" protected=true />
				                </div>
			                </div>
			                <div id="dsg-content-sep"  >
								 	<div class="dsg-content-sep-handle"></div>
								</div>
		                </div>
		                 <div class="yui-u" id="dsg-palettes">
			               <@region id="modelPalettes" scope="template" protected=true />
			            </div>
		               </div>
	              </div>
	           </div>
	           <div class="yui-b" id="alf-filters">
	               <@region id="modelTree" scope="template" protected=true />
	           </div>
	        </div>
	   </div>
	  </@>
</@>

<@templateFooter>
   <@markup id="alf-ft">
	   <div id="alf-ft">
	      <@region id="footer" scope="global" />
	   </div>
   </@>
</@>
