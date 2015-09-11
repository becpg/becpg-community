

<@markup id="js">
   <#-- JavaScript Dependencies -->
	<@script type="text/javascript" src="${url.context}/res/yui/swf/swf.js" group="dashlets"></@script>
	<@script type="text/javascript" src="${url.context}/res/yui/charts/charts.js" group="dashlets"></@script>
	<@script type="text/javascript" src="${url.context}/res/components/dashlets/my-olapchart.js" group="dashlets"></@script>
</@>

<@markup id="css" >
   <#-- CSS Dependencies -->
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/components/dashlets/my-olapchart.css" group="dashlets"/>
</@>
<#--
<@markup id="widgets">
   <@createWidgets group="dashlets"/>
</@>
-->
<@markup id="html">
   <@uniqueIdDiv>
		<#assign el = args.htmlid?html>
		<script type="text/javascript">//<![CDATA[
		(function()
		{
		   var olapChart = new beCPG.dashlet.OlapChart("${el}").setOptions(
		   {
		      siteId: "${page.url.templateArgs.site!""}",
		      regionId: "${args['region-id']?js_string}"
		   }).setMessages(${messages});
		 
		   new Alfresco.widget.DashletResizer("${el}", "${instance.object.id}");
		
		   var saikuAccessEvent = new YAHOO.util.CustomEvent("openSaikuClick");
		   saikuAccessEvent.subscribe(olapChart.openSaikuClick, olapChart, true);
		   
		    new Alfresco.widget.DashletTitleBarActions("${el}").setOptions(
		   {
		      actions:
		      [
		         {
		            cssClass: "saiku",
		            eventOnClick: saikuAccessEvent,
		            tooltip: "${msg("link.access.bi")?js_string}"
		         },
		         {
		            cssClass: "help",
		            bubbleOnClick:
		            {
		               message: "${msg("dashlet.help")?js_string}"
		            },
		            tooltip: "${msg("dashlet.help.tooltip")?js_string}"
		         }
		      ]
		   });
		   })();
		   
		//]]></script>
		<div class="dashlet my-olapchart">
		  <div class="title">${msg("header.myOlapChart")}</div>
		  <div class="toolbar flat-button">   
		      <input id="${el}-charPicker-button" type="button" name="${el}-charPicker-button" value="${msg("button.chart.choose")}" ></input>
		      <select id="${el}-charPicker-select" name="${el}-charPicker-select"></select>
		      <input id="${el}-chartTypePicker-button" type="button" name="${el}-chartTypePicker-button" value="${msg("button.chart.choose")}" ></input>
		      <select id="${el}-chartTypePicker-select" name="${el}-chartTypePicker-select">
		      	<option value="barChart">${msg("picker.chart.type.bar")}</option>
		      	<option value="lineChart">${msg("picker.chart.type.line")}</option>
		      	<option value="columnChart">${msg("picker.chart.type.column")}</option>
		      	<option value="pieChart">${msg("picker.chart.type.pie")}</option>
		      	<option value="chartData">${msg("picker.chart.type.data")}</option>
		      </select>
		   </div>
			<div class="body"  <#if args.height??>style="height: ${args.height}px;"</#if> >
				<div id="${el}-chartContainer" class="yui-content">
					<div class="chart olapChart" id="${el}-chart" >
						<div class="empty"><h3>${msg("empty.title")}</h3><span>${msg("empty.description")}</span></div>
					</div>
				</div>
			</div>
		</div>


	</@>

</@>
