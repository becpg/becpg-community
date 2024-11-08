
<@markup id="css" >
   <#-- CSS Dependencies -->
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/data-lists/toolbar.css" group="product-toolbar" />
    <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/data-lists/toolbar.css" group="product-toolbar" />
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/components/search/savedsearch-picker.css"  group="product-toolbar"/>
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/components/product/product-list-toolbar.css" group="product-toolbar" />
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
	<@script type="text/javascript" src="${url.context}/res/components/entity-data-lists/entity-toolbar.js" group="product-toolbar"></@script>
	<@script type="text/javascript" src="${url.context}/res/js/async-download.js" group="product-toolbar"/>
	<@script type="text/javascript" src="${url.context}/res/components/search/savedsearch-picker.js" group="product-toolbar"></@script>
	<@script type="text/javascript" src="${url.context}/res/components/product/product-list-toolbar.js" group="product-toolbar"></@script>
   
</@>

<@markup id="widgets">
  	<@createWidgets group="product-toolbar"/>
</@>

<@markup id="html">
   <@uniqueIdDiv>
		<#assign el = args.htmlid?html>
		<div id="${el}-body" class="product-list-toolbar datalist-toolbar toolbar">
		   <div id="${el}-headerBar" class="header-bar flat-button theme-bg-2">
		      <div class="left">   
		      	 <div id="toolbar-contribs" class="hidden" ></div>
	              <div class="product-types">  
		             <span class="align-left yui-button yui-menu-button" id="${el}-types">
			            <span class="first-child">
			               <button type="button" tabindex="0"></button>
			            </span>
			         </span>
			         <select id="${el}-types-menu">
			         <#list types as type>
			            <option value="${type.name?html}">${msg("type." + type.name)}</option>
			         </#list>
			         </select>
			      </div>   
			      <div class="product-filters">  
			        <div id="${el}-savedSearchPicker" class="savedSearchPicker"></div>
				 </div>
		      </div>
		      <div class="right">
		         
		         <div class="export-csv">
		            <span id="${el}-export-csv-button" class="yui-button yui-checkbox-button">
		               <span class="first-child">
		                  <button name="exportCsv"></button>
		               </span>
		            </span>
		         </div>
		         <div class="show-thumbnails"> 
                    <span id="${el}-show-thumbnails" class="yui-button yui-checkbox-button">
                          <span class="first-child">
                              <button type="button" ></button>
                          </span>
                      </span>
                  </div>
		         <div class="separator">&nbsp;</div>
		         <div class="reporting-menu">
					    <span class="yui-button yui-push-button" id="${el}-reporting-menu-button">
					         <span class="first-child">
					         	<button name="downloadReport"></button>
					         </span>
					    </span>
					    <select id="${el}-reporting-menu"  ></select>
		         </div>
		          
		      </div>
		   </div>
		</div>
		<#-- template -->
        <div id="custom-toolBar-template-button" class="hidden" >
                <span class="yui-button yui-push-button">
                   <span class="first-child">
                         <button type="button" ></button>
                   </span>
                </span>
        </div>
	</@>
</@>