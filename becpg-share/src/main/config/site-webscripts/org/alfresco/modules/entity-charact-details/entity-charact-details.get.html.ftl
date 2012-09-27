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
		       <div class="export-csv">
		            <span id="${el}-export-csv" class="yui-button yui-push-button">
		               <span class="first-child">
		                  <button type="button">${msg('button.exportCSV')}</button>
		               </span>
		            </span>
		        </div>
	   </div>
		<div class="body">
			<div id="${el}-chartContainer" class="yui-content">
				<div class="chart detailsChart" id="${el}-chart" >
					<div class="empty"><h3>${msg("empty.title")}</h3><span>${msg("empty.description")}</span></div>
				</div>
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