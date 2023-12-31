<#assign el=args.htmlid?html>

<div id="${el}-dialog" class="detailsDialog">
   <div id="${el}-dialogTitle" class="hd">${msg("title")}</div>
   <div class="bd">
	   <div class="toolbar flat-button yui-ge">   
		      <input id="${el}-chartTypePicker-button" type="button" name="${el}-chartTypePicker-button" value="${msg("button.chart.choose")}" ></input>
		      <select id="${el}-chartTypePicker-select" name="${el}-chartTypePicker-select">
		      	<option value="barChart">${msg("picker.chart.type.bar")}</option>
		      	<option value="lineChart">${msg("picker.chart.type.line")}</option>
		      	<option value="columnChart">${msg("picker.chart.type.column")}</option>
		      	<option value="pieChart">${msg("picker.chart.type.pie")}</option>
		      	<option value="chartData">${msg("picker.chart.type.data")}</option>
		      </select>
		      <input id="${el}-level-button" type="button" value="${msg('button.level')}"></input>
		       <select id="${el}-levelbuttonselect"> 
	                <option value="0"></option> 
	                <option value="1">1</option> 
	                <option value="2">2</option>
	                <option value="3">3</option>         
	                <option value="All">${msg('button.level.all')}</option>                  
	           </select> 
		       <div class="export-csv">
		            <span id="${el}-export-csv" class="yui-button yui-push-button">
		               <span class="first-child">
		                  <button type="button">${msg('button.exportCSV')}</button>
		               </span>
		            </span>
		       </div>
               <div class="back-button">
		            <span id="${el}-back" class="yui-button yui-push-button">
		               <span class="first-child">
		                  <button type="button">${msg('button.back')}</button>
		               </span>
		            </span>
		        </div>
		       
	   </div>
		<div class="body">
			<div id="${el}-chartContainer" class="detailsContainer yui-content">
			    <div class="node-header ">
                  <div id="${el}-chartPath" class="datagrid node-path" ></div>
				</div>
				<div class="chart detailsChart datagrid" id="${el}-chart" ></div>
			</div>
		</div>
  </div>
</div>

<script type="text/javascript">//<![CDATA[
 (function () {
 
	var detailsChart = new beCPG.module.EntityCharactDetails("${el}").setOptions(
	   {
	    entityNodeRef:"${args.entityNodeRef}",
	    itemType: "${args.itemType}",
	    dataListItems: "${args.dataListItems}",
	    dataListName: "${args.dataListName}"
	    }).setMessages(${messages});

 })();
//]]></script>